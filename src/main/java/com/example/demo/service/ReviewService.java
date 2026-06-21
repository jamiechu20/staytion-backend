package com.example.demo.service;

import com.example.demo.dto.HotelReviewSummaryDto;
import com.example.demo.dto.UnreviewedOrderDto;
import com.example.demo.dto.request.CreateReviewRequestDto;
import com.example.demo.dto.response.ReviewResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private HotelRepository hotelRepo;

    @Autowired
    private OrderItemRepository orderItemRepo;

    // 取得指定飯店評論
    public HotelReviewSummaryDto getByHotel(Long hotelId) {
        List<ReviewResponseDto> reviews = reviewRepo.findByHotelId(hotelId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        Double avg = reviewRepo.findAverageScoreByHotelId(hotelId);
        Long count = reviewRepo.countByHotelId(hotelId);
        return new HotelReviewSummaryDto(
                avg != null ? avg : 0.0,
                count != null ? count : 0L,
                reviews
        );

    }

    // 取得指定使用者的評論
    public List<ReviewResponseDto> getByUser(Long userId) {
        return reviewRepo.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //取得尚未評論
    public List<UnreviewedOrderDto> findUnreviewedOrdersByUser(Long userId) {
        return reviewRepo.findUnreviewedOrdersByUserId(userId);
    }

    // 新增評論
    public ReviewResponseDto createReview(Long userId, CreateReviewRequestDto req) {
        if (reviewRepo.existsByOrderId(req.getOrderId())) {
            throw new IllegalArgumentException("該訂單已經有評論了");
        }
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        Order order = orderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        List<String> hotelNames = orderItemRepo.findDistinctHotelNamesByOrderId(req.getOrderId());
        if (hotelNames.isEmpty()) {
            throw new IllegalArgumentException("該訂單沒有任何訂單項目或未找到飯店");
        }
        String hname = hotelNames.get(0);
        Hotel hotel = hotelRepo.findByHname(hname)
                .orElseThrow(() -> new IllegalArgumentException("飯店不存在"));
        Review review = new Review();
        review.setOrder(order);
        review.setUser(user);
        review.setHotel(hotel);
        review.setScore(req.getScore());
        review.setComment(req.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepo.save(review);
        return toResponse(saved);
    }

    // 更新評論（只能更新自己）
    public ReviewResponseDto updateReview(Long userId, Long reviewId, CreateReviewRequestDto req) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評論不存在"));
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("無權限編輯他人評論");
        }
        review.setScore(req.getScore());
        review.setComment(req.getComment());

        Review updated = reviewRepo.save(review);
        return toResponse(updated);
    }

    // 刪除評論（只能刪自己）
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評論不存在"));
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("無權限刪除他人評論");
        }
        reviewRepo.delete(review);
    }

    // 管理員/房東：回覆評論（依據 orderId 查找評論）
    public ReviewResponseDto replyReview(Long orderId, String reply) {
        Review review = reviewRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該訂單對應的評論"));
        review.setReply(reply);
        Review updated = reviewRepo.save(review);
        return toResponse(updated);
    }

    // 後台分頁查詢所有評論
    public Page<ReviewResponseDto> searchReviewsForHost(
            Long ownerId,
            String firstName,
            String comment,
            String hotelName,
            Integer minScore,
            Integer maxScore,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable

    ) {
        Specification<Review> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and((root, query, cb) -> cb.equal(root.get("hotel").get("owner").get("id"), ownerId));
        if (firstName != null && !firstName.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("user").get("firstName"), "%" + firstName + "%"));
        }
        if (comment != null && !comment.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("comment"), "%" + comment + "%"));
        }

        if (hotelName != null && !hotelName.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("hotel").get("hname"), "%" + hotelName + "%"));
        }

        if (minScore != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("score"), minScore));
        }

        if (maxScore != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("score"), maxScore));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
        }

        if (endDate != null) {
            LocalDateTime endOfDay = endDate.atTime(LocalTime.MAX);
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
        }
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }
        return reviewRepo.findAll(spec, pageable)
                .map(this::toResponse);
    }

    public Double getAverageScoreByHotel(Long hotelId) {
        Double avg = reviewRepo.findAverageScoreByHotelId(hotelId);
        return avg == null ? 0.0 : avg;
    }

    // Entity -> Response DTO
    private ReviewResponseDto toResponse(Review review) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setOrderId(review.getOrder().getId());
        dto.setHotelId(review.getHotel().getId());
        dto.setHotelName(review.getHotel().getHname());
        dto.setScore(review.getScore());
        dto.setComment(review.getComment());
        dto.setReply(review.getReply());
        dto.setFirstName(review.getUser().getFirstName());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUserId(review.getUser().getId());
        List<Image> images = review.getHotel().getImages();
        if (images != null) {
            String coverUrl = images.stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                    .map(Image::getImgUrl)
                    .findFirst()
                    .orElse("/images/default-hotel.jpg");
            dto.setImgUrl(coverUrl);
        } else {
            dto.setImgUrl("/images/default-hotel.jpg");
        }
        return dto;
    }
}