package com.staytion.backend.specification;

import com.staytion.backend.entity.Amenity;
import com.staytion.backend.entity.City;
import com.staytion.backend.entity.District;
import com.staytion.backend.entity.Hotel;
import com.staytion.backend.entity.Review;
import com.staytion.backend.entity.RoomType;
import jakarta.persistence.criteria.CommonAbstractCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HotelSpecifications {
    public static Specification<Hotel> hasCapacityFor(Integer totalPeople){
        return (root, query, cb)->{
            if(totalPeople == null) return null;
            Objects.requireNonNull(query, "hasCapacityFor only supports select queries");
            // 用 EXISTS 子查詢而不是 join + distinct：join 一對多會讓每間飯店重複出現，
            // 而 PostgreSQL 的 SELECT DISTINCT 不允許 ORDER BY 不在 select list 裡的運算式（orderBy 用到子查詢）
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<RoomType> roomTypes = subquery.from(RoomType.class);
            subquery.select(roomTypes.get("id"))
                    .where(
                            cb.equal(roomTypes.get("hotel"), root),
                            cb.greaterThanOrEqualTo(roomTypes.get("capacity"), totalPeople)
                    );
            return cb.exists(subquery);
        };
    }
    public static Specification<Hotel> hasCity(String city){
        return (root, query, cb)-> {
            if (city == null || city.isEmpty()) return null;
            Join<Hotel, District> districts = root.join("district");
            Join<District, City> cityJoin = districts.join("city");
            return cb.equal(cb.lower(cityJoin.get("cname")), city.toLowerCase());
        };
    }
    public static Specification<Hotel> hasArea(String area){
        return (root, query, cb) -> {
            if (area == null || area.isEmpty()) return null;
            Join<Hotel, District> districts = root.join("district");
            return cb.equal(cb.lower(districts.get("dname")), area.toLowerCase());
        };
    }
    public static Specification<Hotel> hasPriceInRange(String priceStr){
        return (root, query, cb) -> {
            if (priceStr == null || priceStr.isEmpty()) return null;
            Objects.requireNonNull(query, "hasPriceInRange only supports select queries");
            int min = 0, max = Integer.MAX_VALUE;
            if (priceStr.contains("-")) {
                String[] range =  priceStr.split("-");
                min = Integer.parseInt(range[0]);
                max = Integer.parseInt(range[1]);
            } else if  (priceStr.endsWith("up")) {
                min = Integer.parseInt(priceStr.replace("+", "").replace("up", ""));
            }

            return cb.between(minPrice(root, query, cb), BigDecimal.valueOf(min), BigDecimal.valueOf(max));
        };
    }
    // 篩選條件是飯店設施（hotel_amenities)
    public static Specification<Hotel> hasAmenities(List<Long> amenityIds){
        return (root, query, cb) -> {
            if (amenityIds == null || amenityIds.isEmpty()) return null;
            Objects.requireNonNull(query, "hasAmenities only supports select queries");

            Set<Long> ids = new HashSet<>(amenityIds);
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Hotel> subHotel = subquery.from(Hotel.class);
            Join<Hotel, Amenity> amenity = subHotel.join("amenities");
            subquery.select(cb.countDistinct(amenity.get("id")))
                    .where(
                            cb.equal(subHotel, root),
                            amenity.get("id").in(ids)
                    );
            return cb.equal(subquery, (long) ids.size());
        };
    }
    public static Specification<Hotel> hasMinScore(Integer score){
        return (root, query, cb) -> {
            if (score == null) return null;
            Objects.requireNonNull(query, "hasMinScore only supports select queries");
            return cb.greaterThanOrEqualTo(avgScore(root, query, cb), score.doubleValue());
        };
    }

    // 排序也交給資料庫，才能搭配 Pageable 在 SQL 層分頁；最後加 id 當 tie-breaker，確保分頁結果穩定
    public static Specification<Hotel> sortBy(String sort){
        return (root, query, cb) -> {
            Objects.requireNonNull(query, "sortBy only supports select queries");
            // count query 不需要排序
            if (Long.class == query.getResultType()) return null;

            List<Order> orders = new ArrayList<>();
            if ("price_highest".equals(sort)) {
                orders.add(cb.desc(minPrice(root, query, cb)));
            } else if ("rating_highest".equals(sort)) {
                orders.add(cb.desc(avgScore(root, query, cb)));
            }
            orders.add(cb.asc(root.get("id")));
            query.orderBy(orders);
            return null;
        };
    }

    // 飯店最低房價；沒有房型時視為 0
    private static Expression<BigDecimal> minPrice(Root<Hotel> root, CommonAbstractCriteria query, CriteriaBuilder cb){
        Subquery<BigDecimal> subquery = query.subquery(BigDecimal.class);
        Root<RoomType> roomTypes = subquery.from(RoomType.class);
        subquery.select(cb.min(roomTypes.<BigDecimal>get("price")))
                .where(cb.equal(roomTypes.get("hotel"), root));
        return cb.coalesce(subquery, BigDecimal.ZERO);
    }

    // 飯店平均評分；沒有評論時視為 0.0
    private static Expression<Double> avgScore(Root<Hotel> root, CommonAbstractCriteria query, CriteriaBuilder cb){
        Subquery<Double> subquery = query.subquery(Double.class);
        Root<Review> reviews = subquery.from(Review.class);
        subquery.select(cb.avg(reviews.<Integer>get("score")))
                .where(cb.equal(reviews.get("hotel"), root));
        return cb.coalesce(subquery, 0.0);
    }

    private HotelSpecifications(){}

}
