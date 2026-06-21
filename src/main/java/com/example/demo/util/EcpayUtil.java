package com.example.demo.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class EcpayUtil {

    public static String generateCheckMacValue(Map<String, String> params, String hashKey, String hashIV) {
        // 1. 排序參數
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

        // 2. 拼接字串
        StringBuilder sb = new StringBuilder("HashKey=").append(hashKey);
        for (String key : keys) {
            sb.append("&").append(key).append("=").append(params.get(key));
        }
        sb.append("&HashIV=").append(hashIV);

        // 3. URL Encode 並做格式轉換
        String encoded = urlEncode(sb.toString()).toLowerCase()
                .replace("%21", "!")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%2a", "*")
                .replace("%2d", "-")
                .replace("%2e", ".")
                .replace("%5f", "_");

        // 4. SHA256 加密
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(encoded.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("CheckMacValue 加密失敗", e);
        }
    }

    private static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL 編碼錯誤", e);
        }
    }

    public static String generateAutoSubmitForm(String actionUrl, Map<String, String> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("<form id='ecpayForm' method='POST' action='").append(actionUrl).append("'>");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            sb.append("<input type='hidden' name='").append(entry.getKey()).append("' value='").append(entry.getValue()).append("'>");
        }
        sb.append("</form>");
        sb.append("<script>document.getElementById('ecpayForm').submit();</script>");
        return sb.toString();
    }
}

