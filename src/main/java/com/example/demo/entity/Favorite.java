package com.example.demo.entity;

import jakarta.persistence.*; // 導入所有 JPA 相關的註解
import java.util.Objects; // 用於 hashCode 和 equals

@Entity // 標記為 JPA Entity
@Table(name = "favorites", // 映射到資料庫的 favorites 表
       uniqueConstraints = { // 定義唯一的組合約束
           @UniqueConstraint(columnNames = {"user_id", "hotel_id"}) // 確保 user_id 和 hotel_id 的組合是唯一的
       })
public class Favorite {

    @Id // 標記為主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 設定主鍵的生成策略為自增
    private Long id; // 單一主鍵

    @Column(name = "user_id", nullable = false) // 映射到 user_id 欄位，且不允許為空
    private Long userId; // 外鍵，但在此 Entity 中僅作為普通屬性

    @Column(name = "hotel_id", nullable = false) // 映射到 hotel_id 欄位，且不允許為空
    private Long hotelId; // 外鍵，但在此 Entity 中僅作為普通屬性

    // --- 可選：定義 ManyToOne 關聯，用於物件導向導航 ---
    // 如果你希望在獲取 Favorite 物件後，能直接訪問 User 和 Hotel 物件的詳細資訊
    // fetch = FetchType.LAZY 是推薦的，避免不必要的數據加載
    // insertable = false, updatable = false 表示這個外鍵的值由上面的 userId 和 hotelId 屬性控制
    // 而不是由 User/Hotel 物件本身來插入或更新。這在連接表中有單獨的主鍵時很常見。
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user; // 關聯的 User 物件

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Hotel hotel; // 關聯的 Hotel 物件

    // --- Constructors ---
    public Favorite() {} // JPA 需要一個無參建構子

    // 用於創建新 Favorite 物件的建構子，通常只傳入 ID
    public Favorite(Long userId, Long hotelId) {
        this.userId = userId;
        this.hotelId = hotelId;
    }

    // 如果你有 User 和 Hotel 物件，也可以提供一個用物件初始化的建構子
    public Favorite(User user, Hotel hotel) {
        this.user = user;
        this.hotel = hotel;
        // 同步 ID (重要，如果只設置物件，ID 可能不會自動填充)
        if (user != null) {
            this.userId = user.getId();
        }
        if (hotel != null) {
            this.hotelId = hotel.getId();
        }
    }


    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    // Getters 和 Setters for ManyToOne associations (如果解除註釋了 ManyToOne)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        // 同步 userId，確保雙向關係的一致性
        this.userId = (user != null) ? user.getId() : null;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
        // 同步 hotelId
        this.hotelId = (hotel != null) ? hotel.getId() : null;
    }


    // --- equals() and hashCode() ---
    // 對於 Entity，通常只基於 ID 屬性來判斷相等性。
    // 如果 ID 是自增的，那麼在持久化之前 ID 是 null，需要特殊處理。
    // 這裡的實現適用於 ID 為 null 的情況，保證新創建的物件在 equals 時不會與已持久化的物件混淆。
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favorite favorite = (Favorite) o;
        // 如果 ID 是唯一的標識符，則只基於 ID 判斷
        // 如果 ID 還沒生成 (例如新創建的 Entity)，可以選擇基於業務唯一鍵 (user_id, hotel_id) 判斷
        // 但最安全且 JPA 推薦的是基於 ID (除非是天然主鍵或複合主鍵)
        return id != null && Objects.equals(id, favorite.id);
    }

    @Override
    public int hashCode() {
        // 如果 ID 是唯一的標識符，則只基於 ID 生成 hashCode
        // 如果 ID 還沒生成，使用 super.hashCode() 或基於業務唯一鍵的 hash
        return id != null ? Objects.hash(id) : super.hashCode();
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "Favorite{" +
               "id=" + id +
                ", user=" + user.getId() + // <--- 避免直接引用關聯物件，特別是完整的物件，會導致循環引用
                ", hotel=" + hotel.getId() + // <--- 避免直接引用關聯物件
               '}';
    }
}