package com.example.demo.controller;

import com.example.demo.dto.HotelReviewSummaryDto;
import com.example.demo.dto.ReviewReplyDto;
import com.example.demo.dto.UnreviewedOrderDto;
import com.example.demo.dto.request.CreateReviewRequestDto;
import com.example.demo.dto.response.ReviewResponseDto;
import com.example.demo.service.JwtService;
import com.example.demo.service.ReviewService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://127.0.0.1:5500")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    // 房型詳情頁：取得某飯店的評論
    @GetMapping("/rooms/{hotelId}/reviews")
    @Operation(summary = "房型詳情頁：取得某飯店的評論+平均評分與評論總數")
    public HotelReviewSummaryDto getRoomReviews(@PathVariable("hotelId") Long hotelId) {
        return reviewService.getByHotel(hotelId);
    }

    // 使用者中心：取得自己的評論
    @GetMapping("/users/me/reviews")
    @Operation(summary = "使用者中心：取得自己的評論")
    public List<ReviewResponseDto> getMyReviews(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserIdFromToken(token);
        return reviewService.getByUser(userId);
    }


    // 使用者中心：取得尚未評論
    @GetMapping("/unreviewed")
    @Operation(summary = "取得尚未評論")
    public List<UnreviewedOrderDto> getUnreviewedOrders(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserIdFromToken(token);
        return reviewService.findUnreviewedOrdersByUser(userId);
    }

    //後台條件篩選分頁查詢
    @GetMapping("/host/reviews")
    @Operation(summary = "後台評論管理")
    public Page<ReviewResponseDto> searchReviewsForHost(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String hotelName,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject Pageable pageable
    ) {
        String token = authHeader.replace("Bearer ", "");
        Long ownerId = jwtService.getUserIdFromToken(token);
        return reviewService.searchReviewsForHost(ownerId, firstName, comment, hotelName, minScore, maxScore, startDate, endDate, pageable);
    }

    // 新增評論
    @PostMapping("/users/me/reviews")
    @Operation(summary = "新增房客評論")
    public ReviewResponseDto postReview(@RequestHeader("Authorization") String authHeader,
                                        @Valid @RequestBody CreateReviewRequestDto req) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserIdFromToken(token);
        return reviewService.createReview(userId, req);
    }

    // 新增房東回覆
    @PatchMapping("/reviews/{orderId}/reply")
    @Operation(summary = "新增房東回覆")
    public ReviewResponseDto replyReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("orderId") Long orderId,
            @RequestBody ReviewReplyDto dto
    ) {
        return reviewService.replyReview(orderId, dto.getReply());
    }

    // 更新評論
    @PutMapping("/users/me/updatereviews/{id}")
    @Operation(summary = "更新評論")
    public ReviewResponseDto putReview(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable("id") Long id,
                                       @Valid @RequestBody CreateReviewRequestDto req) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.getUserIdFromToken(token);
        return reviewService.updateReview(userId, id, req);
    }

    // 刪除評論
    @DeleteMapping("/users/{userId}/reviews/{id}")
    @Operation(summary = "刪除評論")
    public void deleteReview(
            @PathVariable("userId") Long userId,
            @PathVariable("id") Long id
    ) {
        reviewService.deleteReview(userId, id);
    }

    // 查詢飯店平均分數
    @GetMapping("/rooms/{hotelId}/reviews/average-score")
    @Operation(summary = "取得某飯店的平均評分")
    public Double getAverageScoreByHotel(@PathVariable("hotelId") Long hotelId) {
        return reviewService.getAverageScoreByHotel(hotelId);
    }
}