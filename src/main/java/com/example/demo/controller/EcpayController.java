package com.example.demo.controller;

import com.example.demo.dto.BookingResponse;
import com.example.demo.dto.PaymentProcessRequest;
import com.example.demo.entity.Order;
import com.example.demo.entity.Payment;
import com.example.demo.service.BookingService;
import com.example.demo.service.PaymentService;
import com.example.demo.util.EcpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class EcpayController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    private final String MERCHANT_ID = "3002607";
    private final String HASH_KEY = "pwFHCqoQZGmho4w6";
    private final String HASH_IV = "EkRm7iFT261dpevs";

    private final String ECPAY_ACTION_URL = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

    // 測試用網址 (您的 ngrok 或 cloudflare tunnel URL)
    private String TUNNEL_URL = "https://youth-exclude-cartridge-isa.trycloudflare.com";

    // --- ID 編碼/解碼邏輯 (使用 O<OrderID>P<PaymentID> 格式) ---
    private String encodeOrderAndPaymentIds(Long orderId, Long paymentId) {
        if (orderId == null || paymentId == null) {
            throw new IllegalArgumentException("Order ID and Payment ID cannot be null.");
        }

        String encodedOrderId = String.valueOf(orderId);
        String encodedPaymentId = String.valueOf(paymentId);

        String combinedId = "O" + encodedOrderId + "P" + encodedPaymentId;

        if (combinedId.length() > 20) {
            System.err.println("❌ 嚴重警告：編碼後的 MerchantTradeNo (" + combinedId + ") 長度為 " + combinedId.length()
                    + "，超過 20 字元限制！這將導致交易失敗或資料錯誤。");
            throw new RuntimeException("Encoded MerchantTradeNo exceeds 20 character limit. Current length: "
                    + combinedId.length() + ", Max allowed: 20");
        }
        return combinedId;
    }

    private Long[] decodeOrderAndPaymentIds(String encodedTradeNo) {
        Pattern pattern = Pattern.compile("^O([0-9]+)P([0-9]+)$");
        Matcher matcher = pattern.matcher(encodedTradeNo);

        if (matcher.matches() && matcher.groupCount() == 2) {
            try {
                String orderIdStr = matcher.group(1);
                String paymentIdStr = matcher.group(2);

                Long orderId = Long.parseLong(orderIdStr);
                Long paymentId = Long.parseLong(paymentIdStr);
                return new Long[]{orderId, paymentId};
            } catch (NumberFormatException e) {
                System.err.println("解碼 MerchantTradeNo 時數字格式錯誤: " + encodedTradeNo + " - " + e.getMessage());
                return null;
            }
        }
        System.err.println("解碼 MerchantTradeNo 格式不符或不完整: " + encodedTradeNo);
        return null;
    }

    /**
     * 處理前端發起的綠界支付請求 現在直接接收已存在的 orderId
     */
    @PostMapping("/process")
    public void processPayment(@RequestBody PaymentProcessRequest request, HttpServletResponse response)
            throws IOException {

        // 除錯日誌
        System.out.println("--- 收到 /api/payments/process 請求 ---");
        System.out.println("Request Body: " + request); // 這會印出 PaymentProcessRequest 的 toString()
        if (request != null) {
            System.out.println("Received orderId: " + request.getOrderId());
            System.out.println("Received paymentMethod: " + request.getPaymentMethod());
        }
        System.out.println("------------------------------------");

        Long orderDbId = request.getOrderId();
        String paymentMethod = request.getPaymentMethod();

        if (orderDbId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order ID is missing.");
            return;
        }

        if (!"ECPAY".equalsIgnoreCase(paymentMethod)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported payment method: " + paymentMethod);
            return;
        }

        System.out.println("收到支付請求，目標訂單 ID: " + orderDbId);

        Long paymentDbId = null;
        BigDecimal totalAmount = BigDecimal.ZERO;
        String itemName = "Staytion 訂房服務";

        try {
            // 1. 根據 orderDbId 從資料庫查詢 BookingResponse DTO
            BookingResponse bookingResponseDto = bookingService.getBookingById(orderDbId); // 假設 getBookingById 返回
            // BookingResponse

            if (bookingResponseDto == null) {
                System.err.println("找不到訂單 ID: " + orderDbId + " 對應的 BookingResponse。");
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found for ID: " + orderDbId);
                return;
            }

            // 直接從 BookingResponse DTO 獲取訂單狀態、總金額和支付 ID
            // 確保您的 BookingResponse DTO 有 getStatus(), getTotalPrice(), getPaymentId() 方法
            String orderStatusStr = bookingResponseDto.getStatus();
            totalAmount = bookingResponseDto.getTotalPrice();
            paymentDbId = bookingResponseDto.getPaymentId(); // 從 DTO 獲取 paymentId

            // 檢查訂單狀態
            if (!Order.OrderStatus.PENDING.name().equals(orderStatusStr)) {
                System.err.println("訂單 " + orderDbId + " 狀態不為 PENDING，無法支付。當前狀態: " + orderStatusStr);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order is not in PENDING status.");
                return;
            }

            // 檢查 paymentDbId 是否存在
            if (paymentDbId == null) {
                System.err.println("BookingResponse (ID: " + orderDbId + ") 中未包含 Payment ID。");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Payment ID not found within BookingResponse.");
                return;
            }

            System.out.println("已獲取訂單 (DB ID: " + orderDbId + ") 關聯的支付記錄 (DB ID: " + paymentDbId + ")");

        } catch (RuntimeException e) {
            System.err.println("處理訂單或獲取支付記錄失敗: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to process order or retrieve payment record. Error: " + e.getMessage());
            return;
        }

        // 3. 將資料庫的 Order ID 和 Payment ID 編碼成綠界所需的 MerchantTradeNo
        String merchantTradeNo = null;
        try {
            merchantTradeNo = encodeOrderAndPaymentIds(orderDbId, paymentDbId);
        } catch (RuntimeException e) {
            System.err.println("編碼 MerchantTradeNo 失敗: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to encode MerchantTradeNo: " + e.getMessage());
            return;
        }

        String tradeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        Map<String, String> ecpayParams = new HashMap<>();
        ecpayParams.put("MerchantID", MERCHANT_ID);
        ecpayParams.put("MerchantTradeNo", merchantTradeNo);
        ecpayParams.put("MerchantTradeDate", tradeDate);
        ecpayParams.put("PaymentType", "aio");
        ecpayParams.put("TotalAmount", String.valueOf(totalAmount.intValue()));
        ecpayParams.put("TradeDesc", "Staytion 訂房款項");
        ecpayParams.put("ItemName", itemName);

        ecpayParams.put("ReturnURL", TUNNEL_URL + "/api/payments/return");
        ecpayParams.put("OrderResultURL", TUNNEL_URL + "/api/payments/success");

        ecpayParams.put("ChoosePayment", "ALL");
        ecpayParams.put("EncryptType", "1");

        String checkMacValue = EcpayUtil.generateCheckMacValue(ecpayParams, HASH_KEY, HASH_IV);
        ecpayParams.put("CheckMacValue", checkMacValue);

        String htmlForm = EcpayUtil.generateAutoSubmitForm(ECPAY_ACTION_URL, ecpayParams);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(htmlForm);
        System.out.println("已生成綠界表單並回傳給前端，綠界交易號：" + merchantTradeNo);
    }

    /**
     * 處理綠界後台通知 (ReturnURL)
     */
    @RequestMapping(value = "/return", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String handleReturn(HttpServletRequest request) {
        System.out.println("收到綠界後台通知...");

        Map<String, String> ecpayResponseParams = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            ecpayResponseParams.put(paramName, request.getParameter(paramName));
        }

        String merchantTradeNo = ecpayResponseParams.get("MerchantTradeNo");
        String rtnCode = ecpayResponseParams.get("RtnCode");
        String tradeAmt = ecpayResponseParams.get("TradeAmt");
        String paymentDate = ecpayResponseParams.get("PaymentDate");

        System.out.println("綠界通知 - 綠界交易號 (編碼): " + merchantTradeNo + ", 狀態碼: " + rtnCode + ", 金額: " + tradeAmt
                + ", 交易時間: " + paymentDate);

        String receivedCheckMacValue = ecpayResponseParams.get("CheckMacValue");
        ecpayResponseParams.remove("CheckMacValue");

        String expectedCheckMacValue = EcpayUtil.generateCheckMacValue(ecpayResponseParams, HASH_KEY, HASH_IV);

        if (!expectedCheckMacValue.equalsIgnoreCase(receivedCheckMacValue)) {
            System.err.println("❌ CheckMacValue 驗證失敗，可能的偽造請求！綠界交易號 (編碼)：" + merchantTradeNo);
            return "0|CheckMacValue Error";
        }

        Long[] ids = decodeOrderAndPaymentIds(merchantTradeNo);
        if (ids == null || ids.length != 2) {
            System.err.println("❌ 無法從 MerchantTradeNo 解碼 Order ID 和 Payment ID：" + merchantTradeNo);
            return "0|Decode Error";
        }
        Long orderId = ids[0];
        Long paymentId = ids[1];

        Payment payment = null;
        try {
            payment = paymentService.findPaymentById(paymentId).orElse(null);
        } catch (Exception e) {
            System.err.println("查詢 Payment (ID: " + paymentId + ") 失敗: " + e.getMessage());
            return "0|DB Query Error";
        }

        if (payment == null) {
            System.err.println("❌ 資料庫中找不到對應的支付記錄！Payment ID：" + paymentId + ", Order ID: " + orderId);
            return "0|Payment Not Found";
        }

        // 這裡仍然需要 payment.getOrder() 來獲取 Order ID 和 TotalPrice 進行驗證
        // 如果您的 Payment 實體沒有直接關聯 Order，或者 Order 不包含 TotalPrice，
        // 您需要調整這裡的驗證邏輯，例如從 PaymentService 額外查詢 Order 資訊。
        if (payment.getOrder() == null || !payment.getOrder().getId().equals(orderId)) {
            System.err.println(
                    "❌ Payment 關聯的 Order ID 不符或 Order 不存在！Payment ID: " + paymentId + ", 解碼 Order ID: " + orderId);
            return "0|Order ID Mismatch";
        }

        BigDecimal orderTotalAmount = payment.getOrder().getTotalPrice();
        if (orderTotalAmount == null || orderTotalAmount.compareTo(new BigDecimal(tradeAmt)) != 0) {
            System.err.println(
                    "❌ 訂單金額不符！資料庫金額: " + orderTotalAmount + ", 綠界金額: " + tradeAmt + ", Payment ID：" + paymentId);
            return "0|Amount Mismatch";
        }

        if (payment.getStatus() == Payment.PaymentStatus.PAID) {
            System.out.println("⚠️ 訂單 " + orderId + " 已是已付款狀態，重複通知。Payment ID: " + paymentId);
            return "1|OK";
        }

        if ("1".equals(rtnCode)) { // 綠界回傳 "1" 代表交易成功
            paymentService.updatePaymentMethod(paymentId, Payment.PaymentMethod.ECPAY);
            paymentService.updatePaymentStatus(paymentId, Payment.PaymentStatus.PAID);
            System.out.println("✅ 訂單 " + orderId + " 已成功支付，更新支付記錄狀態為「已付款」。Payment ID: " + paymentId);

            // 新增：更新訂單狀態為 CONFIRMED (現在由 BookingService 處理)
            try {
                bookingService.updateOrderStatus(orderId, Order.OrderStatus.CONFIRMED);
                System.out.println("EcpayController: 訂單 " + orderId + " 狀態已更新為「CONFIRMED」。");
            } catch (Exception e) {
                System.err.println("EcpayController: 更新訂單 " + orderId + " 狀態為 CONFIRMED 失敗: " + e.getMessage());
            }

        } else { // 交易失敗或取消
            paymentService.updatePaymentStatus(paymentId, Payment.PaymentStatus.CANCELED);
            System.out.println("訂單 " + orderId + " 支付失敗，綠界狀態碼: " + rtnCode + "，更新支付記錄狀態。Payment ID: " + paymentId);
            // 支付失敗時，通常訂單狀態可以改為 CANCELLED
            try {
                bookingService.updateOrderStatus(orderId, Order.OrderStatus.CANCELED);
                System.out.println("EcpayController: 訂單 " + orderId + " 狀態已更新為「CANCELED」。");
            } catch (Exception e) {
                System.err.println("EcpayController: 更新訂單 " + orderId + " 狀態為 CANCELED 失敗: " + e.getMessage());
            }
        }

        return "1|OK";
    }

    /**
     * 處理用戶從綠界支付頁面導回您的網站 (OrderResultURL)
     */
    @RequestMapping(value = "/success", method = {RequestMethod.GET, RequestMethod.POST})
    public RedirectView showSuccessPage(HttpServletRequest request) {

//    	System.out.println("--- 收到 /api/payments/success 請求 ---");
//        System.out.println("用戶從綠界支付頁面導回，準備跳轉到訂房成功頁面。");
//
//        // 記錄請求方法
//        System.out.println("請求方法 (Method): " + request.getMethod());
//
//        // 記錄所有請求標頭
//        System.out.println("--- 請求標頭 (Headers) ---");
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            System.out.println(headerName + ": " + request.getHeader(headerName));
//        }
//
//        // 記錄所有請求參數 (無論是 GET 還是 POST 的 form-urlencoded 參數)
//        System.out.println("--- 請求參數 (Parameters) ---");
//        Map<String, String[]> parameterMap = request.getParameterMap();
//        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
//            System.out.println(entry.getKey() + ": " + String.join(", ", entry.getValue()));
//        }
//        System.out.println("------------------------------------");

        System.out.println("用戶從綠界支付頁面導回，準備跳轉到訂房成功頁面。");
        String merchantTradeNo = request.getParameter("MerchantTradeNo");
        String redirectUrl = "http://127.0.0.1:5500/pages/ecpay_success.html";
        if (merchantTradeNo != null && !merchantTradeNo.isEmpty()) {
            Long[] ids = decodeOrderAndPaymentIds(merchantTradeNo);
            if (ids != null && ids.length == 2) {
                redirectUrl += "?orderId=" + ids[0] + "&paymentId=" + ids[1];
            }
        }
        return new RedirectView(redirectUrl);
    }
}
