# Staytion 後端專案稽核報告

> **撰寫日期**：2026-06-22（狀態最後同步：2026-07-09）  
> **稽核範圍**：`master` 分支，全部後端原始碼 + git 歷史記錄  
> **嚴重程度標示**：🔴 High（高風險）｜🟡 Med（中風險）｜🟢 Low（低風險）  
> **完成狀態標示**：✅ 已完成｜🚧 進行中（部分完成）｜⬜ 未開始
>
> 每個小節的表格第一列會標註目前狀態；詳細異動歷史見 [CHANGELOG.md](./CHANGELOG.md)。
> 狀態是依照程式碼現況覆核後標註的，而非單純依照「有沒有寫過」判斷。

---

## 目錄

1. [安全性殘留問題](#1-安全性殘留問題)
2. [架構與系統設計](#2-架構與系統設計)
3. [設計模式與反模式](#3-設計模式與反模式)
4. [程式碼結構](#4-程式碼結構)
5. [資料庫設計與 MySQL → PostgreSQL 遷移評估](#5-資料庫設計與-mysql--postgresql-遷移評估)
6. [未完成的第三方整合（Firebase / Google Maps）](#6-未完成的第三方整合firebase--google-maps)
7. [優先路線圖（Roadmap）](#7-優先路線圖roadmap)

---

## 1. 安全性殘留問題

### 1.1 Firebase 私鑰仍存在 git 歷史中

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（2026-07-09，Firebase Console 手動廢止，非 git 追蹤） — git 歷史已重建（目前僅 5 個 commit，稽核提到的舊 commit `86f20eb` 已不存在），本地 `src/main/resources/firebase/` 也已清空，且兩支私鑰皆已在 Firebase Console 廢止。 |
| **位置** | `src/main/resources/firebase/staytion-serviceAccountKey.json`（本地檔案）+ git 歷史多個 commit |
| **嚴重度** | 🔴 High |

**問題說明**：  
稽核 git 歷史後，確認至少有 **兩支不同的 Firebase Service Account 私鑰**曾被 commit 進 repo：

- `staytion-76988`（舊帳號，最早見於 commit `86f20eb`）— 私鑰完整出現在 git 歷史中
- `staytion-68e13`（你後來重新整合 Firebase 時建立的新帳號）— 私鑰完整存在於**目前本地檔案**中

`.gitignore` 現在已正確加入 `src/main/resources/firebase/`，但這只能阻止**未來**的 commit，**無法消除過去歷史中已存在的私鑰**。BFG Repo Cleaner（`bfg-1.14.0.jar` 就在根目錄）只被用來替換 `application.properties` 裡的值，Firebase JSON 並未被清除。只要 `git log` 存在，這把金鑰就等同於公開狀態。

**建議方向**：  
① 立即到 Firebase Console 手動廢止（revoke）上述兩個 Service Account 的私鑰；② 若打算保留 Firebase 整合，再重新產生新金鑰；③ 考慮用 BFG 從所有歷史 commit 中刪除該 JSON 檔案（`--delete-files staytion-serviceAccountKey.json`），並 force push 所有分支後告知所有協作者重新 clone。

---

### 1.2 本地 `.env` 中的 JWT Secret 強度過低

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成 — `.env` 已更換為高熵隨機值（base64，64+ bytes）。`.env` 不受 git 追蹤，故無對應 commit 可查。 |
| **位置** | `.env`（第 4 行：`JWT_SECRET=mySecretKey123456789abcdefghijklmnopqrstuvwxyz`） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`.env` 現在已被 gitignore 保護，不會被 commit。但目前的值 `mySecretKey123456789abcdefghijklmnopqrstuvwxyz` 是一個**可預測的弱密鑰**：它不是隨機生成的，符合常見字典模式。若此 secret 曾在任何 commit 中出現，攻擊者可以偽造任意使用者的 JWT token。  

根據 `ebd47dc` 的 diff（`jwt.secret=***REMOVED***`），顯示 BFG 已對這個值做過替換，代表原始值確實曾出現在 git 歷史中。

**建議方向**：  
使用 `openssl rand -base64 64` 重新產生至少 64 個字元的隨機 secret，更新本地 `.env`，並確保從不在 git 中出現。

---

### 1.3 所有 API 端點完全開放，JWT 驗證被完整繞過

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（commit `40018c0`，2026-06-26） — `JwtAuthenticationFilter` 已恢復，`authorizeHttpRequests` 已正確設定公開/需驗證路徑。 |
| **位置** | `src/main/java/com/example/demo/config/SecurityConfig.java:64-68` |
| **嚴重度** | 🔴 High |

**問題說明**：  
`SecurityFilterChain` 目前設定為 `.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())`，意即**所有路徑、所有請求，無需任何身份驗證**。原本的 `JwtAuthenticationFilter` 整個被註解掉。這讓後台管理路徑（如 `/api/host/reviews`、`/api/admin/**`）、使用者資料修改、訂單操作等敏感 API 在技術上完全對外開放。

部分控制器（如 `ReviewController`、`UserController`）自行解析 `Authorization` header 做身份確認，屬於臨時補丁，但並不可靠。

**建議方向**：  
恢復 `JwtAuthenticationFilter` 並在 `filterChain` 中正確設定 `authorizeHttpRequests`，區分公開路徑（登入、註冊）與需要驗證的路徑。

---

### 1.4 Debug 用控制器暴露在正式環境

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成 — 兩個檔案已從專案中刪除。這兩支檔案從未進入 git 歷史（本地未追蹤檔案），故無對應 commit。 |
| **位置** | `FirebaseTestController.java`、`TestController.java` |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`FirebaseTestController` 對外暴露 `/api/firebase-test/verify-email`（可查詢任意 email 是否在 Firebase 中存在）；`TestController` 的 `/api/test/config` 回傳後端設定資訊（Google Client ID 長度、mail 帳號是否設定、Firebase 設定檔是否存在）。這些是開發測試用的，不應出現在正式程式碼中。

**建議方向**：  
刪除這兩個控制器，或至少加上 `@Profile("dev")` 限制只在開發環境啟用。

---

### 1.5 CORS 設定不一致，WebSocket 完全開放

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — 仍有 14 處 `@CrossOrigin`，`WebSocketConfig` 仍為 `setAllowedOriginPatterns("*")`。 |
| **位置** | `SecurityConfig.java:71-83`、`WebSocketConfig.java:21`、各 controller 的 `@CrossOrigin` |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`SecurityConfig` 中已有全域 CORS 設定（僅允許 `127.0.0.1:5500`、`localhost:5500`），但多個 controller 還各自加上 `@CrossOrigin` 註解，形成雙重設定，且有些只允許單一 origin，邏輯容易混亂。更嚴重的是 `WebSocketConfig` 使用 `setAllowedOriginPatterns("*")`，完全不限制來源，與其他地方的策略矛盾。

**建議方向**：  
移除所有 controller 層的 `@CrossOrigin`，統一在 `SecurityConfig` 的 CORS configurer 中集中設定，WebSocket 也應一致收緊。

---

### 1.6 刪除評論 API 未從 JWT 驗證身份

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（commit `b0345f5`，2026-07-01） — 改用 `Authentication` 解析當前使用者，移除 path 中的 `{userId}`。 |
| **位置** | `ReviewController.java:114-121`（`deleteReview`） |
| **嚴重度** | 🔴 High |

**問題說明**：  
`DELETE /api/users/{userId}/reviews/{id}` 的 `userId` 直接從 URL path variable 讀取，沒有驗證「發出請求的人是否就是那個 userId」。在 JWT 驗證被完全停用的現況下，任何人都可以帶著別人的 userId 刪除任意評論。即使之後恢復 JWT 驗證，這個 endpoint 的設計也是錯的——應從 token 解析身份，而非信任 URL 參數。

**建議方向**：  
改為從 `Authorization` header 解析 userId（與 `putReview`、`getMyReviews` 同樣做法），移除 path 中的 `{userId}`。

---

### 1.7 EcpayController 硬編碼金流密鑰

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始（2026-07-30 新發現，處理 §3.2 建構子注入時順帶看到） |
| **位置** | `EcpayController.java:228-230` |
| **嚴重度** | 🔴 High |

**問題說明**：  
`MERCHANT_ID`、`HASH_KEY`、`HASH_IV` 三個綠界金流簽章用的值直接寫死在原始碼裡（`private final String HASH_KEY = "pwFHCqoQZGmho4w6";` 等）。`MERCHANT_ID = 3002607` 是綠界官方公開的測試商店代號，本身不算洩漏；但 `HASH_KEY`/`HASH_IV` 若之後串接正式環境會換成真實金鑰，寫死在 commit 歷史中就等同永久外洩，跟 §1.1 Firebase 私鑰是同一類問題。

**建議方向**：  
改用 `@Value("${ecpay.hash-key}")` 之類的寫法從 `.env`/`application.properties` 讀取，並確認 `.gitignore` 已涵蓋這些設定檔。

---

## 2. 架構與系統設計

### 2.1 Service 層回傳 ResponseEntity（職責錯誤）

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（2026-07-30 更新） — `loginWithPassword` 已改為回傳 `String` 並改拋 `InvalidCredentialsException`/`EmailNotVerifiedException`。`register` 已改為回傳 `void` 並改拋 `DuplicateEmailException`（409，已於 `GlobalExceptionHandler` 補上對應 handler），同時新增 `@Transactional` 確保寄信失敗時使用者資料一併 rollback。`sendResetPasswordEmail` 已改為回傳 `void`：找不到使用者時不拋例外、改用 `Optional.ifPresent()` 靜默略過寄信，`AuthController` 統一回傳同一句訊息 + 200（順帶修正下方補充提到的 email enumeration 風險），同時移除了原本印出完整 reset link 的 `System.out.println`。`resetPassword` 已改為回傳 `void`：新增 `PasswordConfirmationMismatchException`（400，密碼與確認密碼不一致）、`UserNotFoundException`（404，沿用既有類別）、`InvalidTokenException`（401，新增，`JwtService.getEmailFromToken()` 同步改為只 catch `JwtException` 並轉譯成 `InvalidTokenException`、保留原始 cause），原本吞掉所有例外的 `catch (Exception e)` 已移除。**`handleGoogleLogin` 已改為回傳 `GoogleLoginResult`**（新增的 domain record，放在 `dto/response/`，內含 `user`/`token`/`isNewUser`），`AuthController.googleLogin()` 改為自行組裝 JSON 回應。至此 `AuthService` 六個方法全部不再回傳 `ResponseEntity`，本項完成。同一輪一併處理：新增 `GoogleTokenVerifierService`（驗證前端傳來的 Google ID Token 簽章與 audience，取代原本完全信任前端傳入 email/name 的做法，詳見 §6）、移除 `handleGoogleLogin` 內的 `firebaseService.createFirebaseUser(...)` 呼叫。`GlobalExceptionHandler` 補上 `PasswordConfirmationMismatchException`、`InvalidTokenException`、`InvalidGoogleTokenException` 三個 handler。 |
| **位置** | `AuthService.java`（所有方法） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`AuthService` 的 `checkEmail()`、`loginWithPassword()`、`register()`、`handleGoogleLogin()`、`sendResetPasswordEmail()`、`resetPassword()` 全部回傳 `ResponseEntity<>`。`ResponseEntity` 是 Spring MVC 的 HTTP 層物件，屬於 controller 的職責；Service 層應只做業務邏輯、拋出有意義的例外（如 `DuplicateEmailException`、`InvalidCredentialsException`），由 Controller 決定 HTTP 狀態碼。這個問題讓 Service 難以被測試，也讓未來引入非 HTTP 情境（如 scheduled job、CLI 工具）時程式碼難以重用。

**建議方向**：  
Service 方法改為回傳 Domain 物件或拋出自定義例外；Controller 捕捉例外並映射到適當的 HTTP Response。

> **✅ 已修復（2026-07-23，隨 `sendResetPasswordEmail` 重構一併處理）**：原本「查無此 email 回 404」會洩漏帳號是否存在（email enumeration 風險）。現已改為：`AuthService.sendResetPasswordEmail()` 對「找不到使用者」不拋例外，而是用 `Optional.ifPresent()` 靜默略過寄信；`AuthController` 不論使用者存不存在，一律回傳同一句訊息「重設密碼郵件已發送」+ HTTP 200，外部無法區分兩種情況。

> **補充（2026-07-24 新增，處理 `resetPassword` 時發現）**：`JwtService.java` 裡除了 `getEmailFromToken()`（已改用 `InvalidTokenException`），`parseToken()`、`getUserIdFromToken()`、`extractUserIdFromAuthHeader()` 三個方法仍是原本的寫法——`catch (Exception e)` 或 `catch (JwtException e)` 之後直接 `throw new RuntimeException(...)`，沒有專屬例外型別，呼叫端也無法區別「token 失敗」跟其他未預期的錯誤。`getUserIdFromToken()` 裡還留著兩行 `System.out.println`（其中一行會印出完整 JWT claims），跟 §4.4 是同一類問題。這次重構範圍只涵蓋 `resetPassword()` 實際會呼叫到的 `getEmailFromToken()`，其餘三個方法留到下一輪 `JwtService` 專門處理時一併修。

---

### 2.2 搜尋邏輯載入全部資料到記憶體（致命效能問題）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — `searchHotels()` 第一行仍是 `hotelRepository.findAll()`。 |
| **位置** | `HotelServiceImpl.java:43-45`（`searchHotels` 方法） |
| **嚴重度** | 🔴 High |

**問題說明**：  
`searchHotels()` 的第一行是 `List<Hotel> hotels = hotelRepository.findAll()`。這代表每次使用者搜尋，後端會把**所有飯店、所有 RoomType、所有 Review、所有 Image** 從資料庫載入記憶體，然後在 Java Stream 中做篩選、排序、分頁。即使資料庫只有幾百筆飯店，這也是極度浪費的做法，且會因為 Lazy Loading 觸發大量的 N+1 查詢（每個 Hotel 再分別 fetch 其 district、city、roomTypes、reviews、images）。

**建議方向**：  
將篩選條件下推到資料庫層，使用 JPA Specification 或 JPQL 動態條件查詢，加上 `JOIN FETCH` 避免 N+1，並直接在資料庫做分頁（`Pageable`）。

---

### 2.3 Controller 直接存取 Repository

| 項目 | 內容 |
|------|------|
| **狀態** | 🚧 進行中（commit `378597c`，2026-07-09，隨 §2.1 一併處理） — `AuthController` 已移除 `UserRepository` 注入。`UserController` 仍直接注入 `UserRepository`（第 37 行），尚未處理。 |
| **位置** | `AuthController.java`（注入 `UserRepository`）、`UserController.java`（注入 `UserRepository`） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`AuthController` 的 `checkEmail()` 方法直接呼叫 `userRepository.existsByEmail()`，`UserController.getProfile()` 直接呼叫 `userRepository.findByEmail()`。Controller 不應知道 Repository 的存在，所有資料存取都應透過 Service 層，這樣才能統一套用業務規則、快取、異常處理邏輯。

**建議方向**：  
移除 Controller 中的 Repository 注入，改為在對應的 Service 中提供方法。

> **補充（2026-07-30 新增，處理 §3.2 時發現）**：本項「位置」清單原本只列 `AuthController`、`UserController`，但複查後發現 `ChatController`、`MessageController`、`LocationController` 也都直接注入 Repository（`ChatRoomRepository`、`UserRepository`、`HotelRepository`、`MessageRepository`、`CityRepository`、`DistrictRepository`）。這次只把它們改成建構子注入（§3.2 範圍），Repository 依賴本身尚未移除，待本項排入時一併處理。

---

### 2.4 Auth 流程：DB 寫入與 Firebase 建立無法原子性回滾

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — `register()` 仍先 `save()` 再呼叫 `FirebaseAuth`，無 `@Transactional`。 |
| **位置** | `AuthService.java:81-114`（`register` 方法） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`register()` 的流程是：先 `userRepository.save(user)`，再呼叫 `FirebaseAuth.getInstance().createUser()`。若 Firebase 那一步失敗，資料庫中的 User 記錄已存在，但 Firebase 沒有對應帳號，造成狀態不一致。此外，這個方法沒有 `@Transactional`，資料庫操作也無法回滾。同樣問題存在於登入流程：`loginWithPassword()` 強制要求 Firebase email 驗證，但**在 Firebase 整合加入之前就已經註冊的使用者在 Firebase 中不存在**，呼叫 `getUserByEmail()` 會拋出 `FirebaseAuthException`，被包成 500 錯誤回傳給前端。

**建議方向**：  
設計明確的處理策略：若 Firebase 不可達，是否允許降級登入？或完全依賴 Firebase 作為 Identity Provider？先決定架構方向，再修復不一致問題。

---

## 3. 設計模式與反模式

### 3.1 貧血領域模型（Anemic Domain Model）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | 所有 Entity：`User.java`、`Hotel.java`、`Review.java`、`Order.java` 等 |
| **嚴重度** | 🟢 Low（學術層面，初級職位可接受） |

**問題說明**：  
所有 Entity 都是純粹的資料容器（只有 getter/setter），沒有任何業務邏輯。例如 `Order` 的狀態轉換邏輯散落在多個 Service 中，`Review` 的「只有訂單結帳後才能評論」規則也在 Service 而非 Entity 內。這是典型的 Anemic Domain Model，與 DDD（Domain-Driven Design）的精神相違背，但在初階 Spring Boot 專案中很常見。

**建議方向**：  
將簡單的業務規則移進 Entity（例如 `order.cancel()` 方法封裝狀態轉換邏輯），不必全面 DDD，但至少讓 Entity 有基礎的自我保護能力。

---

### 3.2 @Autowired 欄位注入（Field Injection）

| 項目 | 內容 |
|------|------|
| **狀態** | 🚧 進行中（commit `e068d79`，2026-07-02；Controller 部分 2026-07-30 更新） — 全部 21 個 Service 類別已改為建構子注入（`OrderService.java` 除外，該檔為死碼待 §4.3 一併刪除）。**20 個 Controller 中，16 個已改為建構子注入 + `@RequiredArgsConstructor`**（`AmenityController`、`AdminOrderController`、`BookingController`、`ChatController`、`ChatRoomController`、`EcpayController`、`FavoriteController`、`HomeController`、`HotelController`、`LocationController`、`MessageController`、`ReviewController`、`RoomAvailabilityController`、`RoomTypeController`、`StatisticsController`、`UserController`、`UserOrderController`）。**剩餘 3 個維持原樣，皆為刻意跳過**：`ArvinHotelController`、`ArvinRoomTypeController`（待 §4.2 合併刪除時一併處理，避免白工）、`OrderController`（整檔死碼，待 §4.3 刪除）。`JwtAuthenticationFilter` 的 `userRepository` 複查後發現其實已經是 `final`（先前補充筆記已過時，可能在更早的修改中已一併處理），故此項已完成。 |
| **位置** | `ReviewService.java`、`ChatRoomService.java`、`HotelServiceImpl.java`、`UserService.java`、`AuthService.java`、`BookingService.java`、`SecurityConfig.java`、`AuthController.java`（混合）等 |
| **嚴重度** | 🟡 Med |

**問題說明**：  
大多數 Service 和部分 Config 類別使用 `@Autowired` 欄位注入。這種做法有幾個問題：① 依賴關係在建構時不透明（你看不出這個類別需要哪些依賴）；② 在單元測試時無法不啟動 Spring 容器就建立物件（需要 `@SpringBootTest` 或 `@MockBean`）；③ 依賴不可標記為 `final`，無法保證不可變性。`FavoriteServiceImpl` 已經正確使用**建構子注入**，是目前唯一符合最佳實踐的例子。`AuthController` 則更糟，混合了建構子注入（`MailService`）和欄位注入（`AuthService`、`UserRepository`）。

**建議方向**：  
所有類別統一改為建構子注入（配合 Lombok 的 `@RequiredArgsConstructor` 可大幅減少樣板程式碼）。

> **✅ 已修復（2026-07-03，隨 2.1 一併處理）**：`AuthController` 的混合注入（`MailService` 建構子注入 + `AuthService`/`UserRepository` 欄位注入）已統一改為建構子注入 + `@RequiredArgsConstructor`，並移除不再使用的 `UserRepository` 注入（對應 §2.3 的一部分）。至此本項清單列出的所有位置皆已完成。

> **補充（2026-07-03 新增）**：本項原本的「位置」清單只點名了 `AuthController`，但複查後發現其餘 20 個 controller 全部都還是 `@Autowired` 欄位注入，從未被納入本項範圍。已於 2026-07-30 一併處理，見上方狀態。

> **✅ 已修復（2026-07-30）**：16 個 Controller 已統一改為建構子注入 + `@RequiredArgsConstructor`。過程中一併發現：① `ChatController`、`MessageController`、`LocationController` 直接注入 Repository（跟 §2.3 是同一類問題，但 §2.3 目前只追蹤 `AuthController`/`UserController`，範圍記錄不完整，已在 §2.3 補記）；② `EcpayController.java` 有硬編碼的金流密鑰，已新增 §1.7 追蹤；③ `EcpayController.java` 前 215 行是整份被註解的舊版類別（死碼），已補進 §4.3。

---

### 3.3 GlobalExceptionHandler 完全被註解

| 項目 | 內容 |
|------|------|
| **狀態** | 🚧 進行中（commit `378597c`，2026-07-09） — 已解除註解啟用，共 4 個 `@ExceptionHandler`。重複樣板尚未抽成 helper method（使用者已評估決定暫緩，見下方補充）。 |
| **位置** | `exception/GlobalExceptionHandler.java`（整個檔案被 `//` 註解） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
原本設計了集中錯誤處理機制，但目前整個 `@ControllerAdvice` 類別都被註解掉。沒有全域例外處理器意味著：未預期的 RuntimeException 會被 Spring 包成 500 錯誤，且沒有統一的錯誤 JSON 結構，前端無法穩定解析錯誤訊息。

**建議方向**：  
啟用 `GlobalExceptionHandler`，補上常用的例外映射（`IllegalArgumentException → 400`、`RuntimeException → 500`），並定義統一的錯誤回應 DTO（`ErrorResponse { status, message, timestamp }`）。

> **✅ 部分已修復（2026-07-09，隨 §2.1 Auth 重構一併處理）**：`GlobalExceptionHandler` 已解除註解啟用，並新增 `InvalidCredentialsException`（401）、`EmailNotVerifiedException`（403）的映射，與既有的 `HotelNotFoundException`、`RoomTypeNotFoundException`（皆 404）並列。

> **待辦（2026-07-09 新增）**：目前 4 個 `@ExceptionHandler` 方法都是重複的樣板（各自建立 `HashMap`、塞入相同的 4 個 key，只有 status/error 字串/例外型別不同）。可以抽一個 private helper method（例如 `buildErrorResponse(HttpStatus status, String error, String message)`）收斂重複邏輯。使用者評估後決定不急著做，留到之後有空時處理，不影響目前 Auth 重構進度。

---

### 3.4 部分 Service 無 Interface，命名不一致

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `FavoriteService`（有 Interface）vs. `ReviewService`、`ChatRoomService`、`UserService`（無 Interface） |
| **嚴重度** | 🟢 Low |

**問題說明**：  
`FavoriteService` 有定義 interface + `FavoriteServiceImpl` 實作；`HotelService` 有 interface 但 impl 實際上是 `HotelServiceImpl`；其他 Service 則直接是 concrete class。不一致的命名模式讓人困惑：「什麼時候要用 interface？」對於大多數 Service 而言，若只有一個實作，加 interface 只是樣板；但若計畫用 Mockito 測試，interface 對解耦有幫助。此外，`HotelService.java` 裡面誤加了 `import org.springframework.beans.factory.annotation.Autowired` 和 `import org.springframework.stereotype.Service`——這是 interface，不應該有這些 import。

**建議方向**：  
選擇一個一致的策略（建議：Service class 直接用，不強制加 interface），然後統一整個 codebase。

---

## 4. 程式碼結構

### 4.1 Package 名稱仍是 Spring Initializr 預設值

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `com.example.demo`（全部原始碼） |
| **嚴重度** | 🟢 Low（但面試時會被問到） |

**問題說明**：  
`com.example.demo` 是 Spring Initializr 的預設 package name，代表沒有人想過合適的命名。這在 portfolio 專案中顯得不專業，且 `pom.xml` 的 `groupId` 是 `com.example`、`artifactId` 是 `staytion`，內部 package 名稱不一致。

**建議方向**：  
將 package 重新命名為 `com.staytion.backend`（或類似），同步更新 `pom.xml` 的 `groupId`。

---

### 4.2 隊友名字出現在類別名稱中

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `ArvinHotelService.java`、`ArvinHotelController.java`、`ArvinRoomTypeService.java`、`ArvinRoomTypeController.java` |
| **嚴重度** | 🟡 Med（Portfolio 專案中非常不妥） |

**問題說明**：  
這是團隊分工時用名字區分負責範圍的做法，但在作品集中留著，面試官看到會有疑問。這四個類別實作了飯店和房型的 CRUD，應該合併到 `HotelController`/`HotelService` 和 `RoomTypeController`/`RoomTypeService`（這些已存在）。

**建議方向**：  
將 Arvin 系列的功能合併到對應的無前綴 class 後，刪除這四個 class。

---

### 4.3 大量被註解的死碼

| 項目 | 內容 |
|------|------|
| **狀態** | 🚧 進行中 — `GlobalExceptionHandler.java` 已隨 §3.3 解除註解、`SecurityConfig.java` 舊 filterChain 也已隨 §1.3 清除。**`OrderService.java`（整檔）、`exception/JwtAuthException.java`（空類別死碼）、`BookingService.updateBooking` 內約 40 行註解區塊仍未清理**。 |
| **位置** | `OrderService.java`（整個檔案被註解）、`GlobalExceptionHandler.java`（整個檔案被註解）、`BookingService.java:183-222`（大塊 updateBooking 邏輯被註解）、`SecurityConfig.java:23-62`（舊的 filterChain 被註解） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
整個 `OrderService.java` 被 `//` 註解（251 行）但仍在 codebase 中。這不是「做到一半未完成」，而是已經被 `BookingService` 取代但忘了清理。死碼增加閱讀負擔，也讓讀者不確定哪個才是正在使用的版本。git 歷史本身就是「刪除前的紀錄」，不需要靠 comment 保存舊程式碼。

**建議方向**：  
刪除完全被註解的檔案和函式；`BookingService` 中未完成的 `updateBooking` 邏輯如果還需要完成，應開 TODO comment 而非留著整塊 comment block。

> **補充（2026-07-09 新增，處理 §3.3 時發現）**：`exception/JwtAuthException.java` 是個空類別（連 `extends RuntimeException` 都沒有），且全專案沒有任何地方真的拋出它，`GlobalExceptionHandler` 裡對應的 handler 也一直被註解著。屬於死碼，待本階段清理時一併刪除。

> **補充（2026-07-30 新增，處理 §3.2 時發現）**：`EcpayController.java` 第 1-215 行是一整份被註解掉的舊版 `EcpayController` 類別（含舊的 import、舊的 Ecpay 串接邏輯），實際生效的類別從第 220 行才開始。屬於死碼，待本階段清理時一併刪除。

---

### 4.4 System.out.println 滿佈 production 程式碼

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — 目前仍有 16 個檔案含 `System.out.println`，包含優先度最高的 `JwtAuthenticationFilter.java:44`（明碼列印完整 JWT token）。 |
| **位置** | 全 codebase 共 69 處（`grep -rn "System.out.println"` 結果） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
例如 `UserController.java:43-44` 的 `System.out.println("Authorization header: " + authHeader)` 會把完整的 JWT token 印到 log，這是敏感資訊洩漏。其他如 `AuthService.java:108`（印出 Firebase 驗證連結）、`JwtService.java:66`（印出完整 JWT claims）都有類似問題。

**建議方向**：  
全部替換為 SLF4J Logger（`LoggerFactory.getLogger(...)`），並為含 token 或 email 的 log 使用 DEBUG level，不在 production 中預設開啟。

> **補充（1.3 修復後新增）**：`JwtAuthenticationFilter.java:58` 的 `System.out.println("取得的 token: [" + token + "]")` 直接將完整 raw JWT token 印出，比其他地方更嚴重（token 本身可用來偽造身份），Stage 2 替換 logger 時應優先處理。

---

### 4.5 DTO 命名與分層不一致

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `dto/` 根目錄（平面放置）vs. `dto/request/`、`dto/response/` 子目錄 |
| **嚴重度** | 🟢 Low |

**問題說明**：  
部分 DTO 放在 `dto/` 根目錄（如 `LoginRequest.java`、`RegisterRequest.java`、`FavoriteDto.java`），部分放在 `dto/request/`、`dto/response/` 子目錄（如 `CreateReviewRequestDto.java`、`ReviewResponseDto.java`）。命名也不統一：有的加 `Dto` 後綴，有的加 `DTO`，有的不加。

**建議方向**：  
統一用 `dto/request/` 和 `dto/response/` 分類，命名一律用 `XxxRequest` / `XxxResponse` 不加後綴 DTO/Dto。

---

### 4.6 ReviewService 的日期過濾 bug

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — 複查後 bug 仍在：`endOfDay` 有算但第 174 行仍傳入 `endDate`。 |
| **位置** | `ReviewService.java:175-179`（`searchReviewsForHost` 方法） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
程式碼計算了 `endOfDay`（`endDate.atTime(LocalTime.MAX)`），但最後傳給 `cb.lessThanOrEqualTo` 的是原始的 `endDate`（`LocalDate`），而非 `endOfDay`（`LocalDateTime`）。這個 bug 會導致在查詢「結束日期當天」建立的評論時，因為時間比對不正確而遺漏資料。

**建議方向**：  
將 `endDate` 改為 `endOfDay`（`LocalDateTime`）傳入 Specification。

---

### 4.7 javax.annotation vs jakarta.annotation

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `FirebaseConfig.java:8`（`import javax.annotation.PostConstruct`） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
本專案使用 Spring Boot 3.5.0（Java EE → Jakarta EE 命名空間遷移後）。`javax.annotation.PostConstruct` 在 Spring Boot 3.x 中已被棄用，應改為 `jakarta.annotation.PostConstruct`。雖然部分情境仍可運作（若 classpath 中有相關依賴），但這是潛在的問題。

**建議方向**：  
將 `import javax.annotation.PostConstruct` 替換為 `import jakarta.annotation.PostConstruct`。

---

## 5. 資料庫設計與 MySQL → PostgreSQL 遷移評估

### 5.1 Hotel 搜尋 N+1 問題（最嚴重的效能問題）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `HotelServiceImpl.java`、`FavoriteServiceImpl.java:64-97` |
| **嚴重度** | 🔴 High |

**問題說明**：  

**HotelServiceImpl**：`findAll()` 後，Stream 中存取 `hotel.getDistrict().getCity()`、`hotel.getRoomTypes()`、`hotel.getReviews()`、`hotel.getImages()` 等 LAZY 關聯，每個 Hotel 都會各自觸發額外的 SELECT 查詢。若有 100 間飯店，單次搜尋可能產生 400+ 個 SQL 查詢。

**FavoriteServiceImpl.getFavoriteHotels()**：在 for-loop 中對每個收藏的飯店分別呼叫 5 個 repository 方法（`findById`、`findAverageScore`、`count`、`findMinPrice`、`findCoverImage`）。10 個收藏 = 50 個查詢。

**建議方向**：  
- Hotel 搜尋：移到資料庫做篩選（JPQL + JPA Specification），使用 `JOIN FETCH` 一次取得所需關聯資料
- Favorite 列表：改用單一 JPQL 查詢（含 JOIN），或使用 Projection 一次取得所有需要的欄位

---

### 5.2 MySQL 專屬語法在 Entity 中硬編碼

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — `Order.java` 仍為 MySQL `ENUM` columnDefinition。 |
| **位置** | `Order.java:32`（`columnDefinition`）、`HotelRepository.java:34-42`（native query） |
| **嚴重度** | 🔴 High（遷移到 PostgreSQL 前必須修） |

**問題說明**：

**Order.java**：
```java
@Column(name = "status", columnDefinition = "ENUM('CONFIRMED','CANCELED','PENDING') DEFAULT 'PENDING'")
```
`ENUM` 是 MySQL 特有的資料類型，PostgreSQL 沒有。遷移後這個 `columnDefinition` 會讓 Hibernate DDL 生成錯誤的 SQL。

**HotelRepository（native query）**：
```sql
AND i.is_cover = 1
```
MySQL 中 boolean 值用 `0/1` 表示，PostgreSQL 要求 `= true`（或 `IS TRUE`）。

**建議方向**：  
- `Order.status`：移除 `columnDefinition`，改用 `@Enumerated(EnumType.STRING)`（不加 columnDefinition），讓 Hibernate 自動處理（PostgreSQL 會映射為 VARCHAR）
- Native query：改用 JPQL，或針對 PostgreSQL 調整語法

---

### 5.3 缺少 Schema Migration 工具（Flyway / Liquibase）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 — `pom.xml` 尚未加入 Flyway 依賴。 |
| **位置** | `pom.xml`（未引入任何 migration 依賴） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
目前沒有任何 schema migration 工具。資料庫結構的變更完全靠手動執行 SQL 或依賴 `spring.jpa.hibernate.ddl-auto`（目前未明確設定，預設是 `create-drop` 或 `none`）。遷移到 PostgreSQL 時，需要重建所有 table，沒有工具記錄版本，兩人以上協作時極易造成環境不同步。

**建議方向**：  
引入 **Flyway**（較簡單，適合此規模的專案）：`pom.xml` 加入依賴，在 `src/main/resources/db/migration/` 建立 `V1__init_schema.sql`，把目前的建表 SQL 寫進去作為基線版本。

---

### 5.4 MySQL → PostgreSQL 遷移全面評估

**狀態**：⬜ 未開始（整個 Stage 3 尚未啟動）

這是一個有明確學習目標的重構項目，以下是遷移時需要處理的所有差異：

| 差異項目 | MySQL 做法 | PostgreSQL 做法 | 影響範圍 |
|---------|-----------|----------------|---------|
| 自動遞增主鍵 | `AUTO_INCREMENT` | `SERIAL` / `IDENTITY` | Hibernate 用 `GenerationType.IDENTITY` 可自動處理，但 native SQL 要改 |
| ENUM 型別 | `ENUM('A','B')` | `VARCHAR(x)` + `CHECK` constraint，或自訂 TYPE | `Order.status` 的 `columnDefinition` 必須修 |
| Boolean 值 | `TINYINT(1)` / `0`/`1` | `BOOLEAN` / `true`/`false` | Native query 中的 `= 1` 要改為 `= true` |
| 字串比較大小寫 | 預設不區分 | 預設區分（`LIKE` 也區分） | 搜尋飯店名的 `findByHnameContainingIgnoreCase` 需驗證 |
| 識別符大小寫 | 依 OS，通常不區分 | 預設區分 | Table/column 名稱確保小寫 |
| Hibernate Dialect | `MySQLDialect` 或自動偵測 | `PostgreSQLDialect` | `application.properties` 須新增或更新 dialect 設定 |
| JDBC Driver | `com.mysql:mysql-connector-j` | `org.postgresql:postgresql` | `pom.xml` 要替換 |
| JSON 欄位型別 | `JSON` | `JSONB`（建議）或 `JSON` | 目前專案沒有 JSON 欄位，不影響 |
| 分頁語法 | `LIMIT x OFFSET y` | 相同（PostgreSQL 也支援）| 無需改動 |

**遷移步驟建議**（概略）：  
1. 引入 Flyway，建立現有 MySQL schema 的 baseline migration 腳本
2. 修復所有 MySQL 專屬 `columnDefinition`（先移除，讓 Hibernate 自動生成）
3. 修復 native queries 中的 Boolean 值比對語法
4. `pom.xml` 替換 JDBC driver，`application.properties` 更新 datasource URL 和 dialect
5. 用 Docker 啟動 PostgreSQL 本地測試，執行 Flyway migration，驗證所有測試通過
6. 處理資料遷移（若需要從現有 MySQL 移資料）

---

### 5.5 缺少索引（可擴展性問題）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | 各 Entity（未在 `@Table` 中宣告索引） |
| **嚴重度** | 🟢 Low（資料量小時不明顯） |

**問題說明**：  
常用的查詢欄位（如 `reviews.hotel_id`、`reviews.user_id`、`orders.user_id`、`favorites.user_id`、`chat_rooms.user1_id`/`user2_id`）沒有在 Entity 中宣告 `@Index`。當資料量增長，這些 WHERE 條件的查詢效率會下降。

**建議方向**：  
在 `@Table` 的 `indexes` 屬性中為常用的外鍵欄位加入索引，或在 Flyway migration 腳本中加入 `CREATE INDEX`。

---

## 6. 未完成的第三方整合（Firebase / Google Maps）

### Firebase 整合現狀

**狀態**：🚧 進行中（2026-07-30 更新） — 方向已選定「選項 B：移除 Firebase」。`loginWithPassword`、`register`、`handleGoogleLogin` 三個方法都已不再呼叫 Firebase，Auth 流程本身已完全脫離 Firebase 依賴。**尚未完成**：`pom.xml` 仍保留 `firebase-admin`、`google-api-client`、`spring-boot-starter-oauth2-client` 依賴（`google-api-client` 現在其實還在用，是 `GoogleTokenVerifierService` 拿來驗證 ID Token；`firebase-admin`、`spring-boot-starter-oauth2-client` 才是真正可以移除的）；`AuthController` 的 `/api/auth/resend-verification`、`/api/auth/check-email-verified` 也仍直接呼叫 `FirebaseAuth`，尚未改用本地驗證邏輯。

**已完成的部分**：
- `FirebaseConfig`：讀取 service account JSON，初始化 `FirebaseApp`（有效，但需要本地金鑰檔案；已無任何 Auth 流程依賴它，僅 `resend-verification`/`check-email-verified` 兩個 endpoint 還間接需要）
- `FirebaseService`：`createFirebaseUser()`、`deleteFirebaseUser()` 方法，`AuthService` 已不再使用（`handleGoogleLogin` 於 2026-07-30 移除呼叫），僅 `UserService` 還在用
- `AuthService.loginWithPassword()`：已不再檢查 Firebase email 驗證狀態，改用本地 `User.emailVerified` 欄位
- `AuthService.register()`：已不再呼叫 Firebase，改為本地產生帶 `purpose` claim 的驗證信 token 並寄送本地驗證連結
- **`AuthService.handleGoogleLogin()`（2026-07-30 完成）**：已不再呼叫 `FirebaseService.createFirebaseUser()`。**Google Sign-In 身份驗證漏洞已修復**——新增 `GoogleTokenVerifierService`，改為後端用 `google-api-client` 的 `GoogleIdTokenVerifier` 驗證前端傳來的 Google ID Token（簽章、audience 皆會核對，audience 沿用既有的 `GOOGLE_CLIENT_ID`，新增獨立的 `google.client.id` property key，不與下方仍未使用的 `spring.security.oauth2.client.registration.*` 死配置耦合），並檢查 `email_verified`。`AuthController.googleLogin()` 的 request body 也已從 `{email, firstName, lastName}` 改為只接收 `{idToken}`。
- `AuthController`：`/api/auth/resend-verification`、`/api/auth/check-email-verified` 仍直接呼叫 Firebase（尚未跟進其餘方法的改法）

**未完成/有問題的部分**：
- **`FirebaseTestController`** 和 **`TestController`**：debug 用控制器遺留在正式程式碼中
- 根目錄有大量測試 HTML 頁面：`firebase-google-signin.html`、`firebase-setup-test.html`、`test-google-login.html` 等，全部應移除
- `bfg-1.14.0.jar` 遺留在根目錄
- `frontend/` 資料夾不應在後端 repo 中

> **補充（2026-07-30 新增，處理 §2.1 `handleGoogleLogin` 時發現）**：`spring-boot-starter-oauth2-client` 依賴 + `application.properties` 裡對應的 `spring.security.oauth2.client.registration.google.*` 設定，經確認**完全沒被使用**——`SecurityConfig` 沒有呼叫 `.oauth2Login()`，全專案沒有任何 `ClientRegistration`/`OAuth2User` 相關程式碼，這組是死配置。討論過改用 Google Identity Services（前端直接拿 ID Token，非導頁式 OAuth2 Authorization Code Flow）後決定沿用既有的 `GOOGLE_CLIENT_ID` 環境變數、但改用專屬的 `google.client.id` property key，不依賴這組死配置。是否移除這組依賴與設定，留到本項下方「執行」與「移除依賴」階段一併決定。

> **✅ 方向已決定（2026-07-08）**：選擇**選項 B：移除 Firebase 整合**。決定於 Stage 2（§2.1 AuthService 重構）進行到一半時提前拍板，因為 `loginWithPassword`、`register`、`handleGoogleLogin` 三個方法都直接呼叫 `FirebaseAuth`，若先照舊邏輯把回傳型別改完再拆 Firebase，等於做兩次工。執行順序：先在 §2.1 這一輪重構中，把三個方法的 Firebase 呼叫換成本地邏輯（`User.emailVerified` 欄位已於同一輪加入），下方 Stage 4 的「執行」與「移除依賴」項目待 AuthService 邏輯穩定後再收尾（避免中途編譯不過）。

**最小清理路徑（二選一）**：

**選項 A：完成 Firebase 整合**  
需要完成：① Google Sign-In 時驗證 Firebase ID Token（前端傳來的應是 token 而非 email/name）；② 修復 `loginWithPassword` 對舊帳號的相容性問題；③ 移除 debug 控制器；④ 清理根目錄的測試 HTML 檔案。工作量：約 1-2 天。

**選項 B：移除 Firebase 整合**  
完全移除 Firebase 依賴：① 刪除 `FirebaseConfig`、`FirebaseService`、`FirebaseTestController`、`TestController`；② `AuthService` 中的 Firebase 呼叫全部刪除；③ `pom.xml` 移除 firebase-admin 和 google-api-client 依賴；④ 移除 `application.properties` 的 OAuth2 設定（若不需要 Google 登入）。工作量：約 2-3 小時，但要先確定是否保留 Google OAuth 功能（Spring Security OAuth2 和 Firebase Admin SDK 是兩件不同的事）。

---

### Google Maps 整合現狀

**狀態**：⬜ 未開始（未有異動）

**已完成的部分**：
- `GeocodingService`：呼叫 Google Maps Geocoding API，將地址轉為經緯度（可獨立運作）
- `AddressParserService`：解析地址字串（工具類）
- `RoomType` 和 `Hotel` entity 有 `latitude`/`longitude` 欄位
- `HotelServiceImpl` 有 `CITY_CENTER_MAP` 和 `haversine()` 函式（本地計算，不呼叫 API）

**問題**：  
`GeocodingService` 存在且有 `@Value("${google.maps.api.key}")` 注入，但使用它的上層邏輯（何時呼叫 geocoding？建立飯店時自動取得座標？）不完整。`CITY_CENTER_MAP` 是硬編碼的地理中心，不依賴 Google Maps API。

**建議方向**：  
Google Maps 整合相對獨立、風險低。若保留，可在「建立/更新飯店」時呼叫 `GeocodingService` 自動填入座標；若資料已有座標，可直接跳過。決定是否繼續整合前，先確認 `GOOGLE_MAPS_API_KEY` 是否有效金鑰。

---

## 7. 優先路線圖（Roadmap）

---

### Stage 1：安全性與緊急修復（先完成，其他才有意義）

| 狀態 | 任務 | 模式 | 學習價值 | 稽核編號 |
|------|------|------|---------|---------|
| ✅ | 廢止 Firebase 私鑰，決定 BFG 清理歷史 | ⚡ Fix-it-faster | 低（操作性） | [§1.1](#11-firebase-私鑰仍存在-git-歷史中) |
| ✅ | 更換 JWT Secret 為高熵隨機值 | ⚡ Fix-it-faster | 低 | [§1.2](#12-本地-env-中的-jwt-secret-強度過低) |
| ✅ | 恢復 `JwtAuthenticationFilter` 並重啟 Security | 🎓 Learn-it-myself | **高**（核心 Spring Security 知識） | [§1.3](#13-所有-api-端點完全開放jwt-驗證被完整繞過) |
| ✅ | 修復 `deleteReview` 的身份驗證 | 🎓 Learn-it-myself | 高 | [§1.6](#16-刪除評論-api-未從-jwt-驗證身份) |
| ✅ | 刪除 `FirebaseTestController`、`TestController` | ⚡ Fix-it-faster | 低 | [§1.4](#14-debug-用控制器暴露在正式環境) |
| ✅ | 清理根目錄的測試 HTML、`bfg.jar`、`frontend/` | ⚡ Fix-it-faster | 低 | [§6](#6-未完成的第三方整合firebase--google-maps) |
| ⬜ | 移除 `EcpayController` 硬編碼的金流密鑰，改用設定檔讀取 | ⚡ Fix-it-faster | 低 | [§1.7](#17-ecpaycontroller-硬編碼金流密鑰) |

---

### Stage 2：DI 與架構清理

| 狀態 | 任務 | 模式 | 學習價值 | 稽核編號 |
|------|------|------|---------|---------|
| 🚧 | 所有 Service/Controller 改為建構子注入（可用 Lombok） | 🎓 Learn-it-myself | **高**（面試必考） | [§3.2](#32-autowired-欄位注入field-injection) |
| ✅ | `AuthService` 移除 `ResponseEntity`，改拋 custom exception | 🎓 Learn-it-myself | **高**（分層架構概念） | [§2.1](#21-service-層回傳-responseentity職責錯誤) |
| 🚧 | 啟用 `GlobalExceptionHandler`，統一錯誤回應格式 | 🎓 Learn-it-myself | 高 | [§3.3](#33-globalexceptionhandler-完全被註解) |
| 🚧 | 移除 Controller 中的 Repository 直接存取 | 🎓 Learn-it-myself | 高 | [§2.3](#23-controller-直接存取-repository) |
| ⬜ | 修復 `FirebaseConfig` 的 `javax` → `jakarta` import | ⚡ Fix-it-faster | 低 | [§4.7](#47-javaxannotation-vs-jakartaannotation) |
| ⬜ | 修復 `ReviewService` 日期過濾 bug | ⚡ Fix-it-faster | 低 | [§4.6](#46-reviewservice-的日期過濾-bug) |
| ⬜ | 統一 CORS 設定，移除分散的 `@CrossOrigin` | ⚡ Fix-it-faster | 低 | [§1.5](#15-cors-設定不一致websocket-完全開放) |
| 🚧 | 刪除死碼（`OrderService.java`、大量 comment blocks） | ⚡ Fix-it-faster | 低 | [§4.3](#43-大量被註解的死碼) |
| ⬜ | 替換 `System.out.println` 為 SLF4J Logger | ⚡ Fix-it-faster | 低（但面試表現重要） | [§4.4](#44-systemoutprintln-滿佈-production-程式碼) |
| ⬜ | 整合 Arvin 系列到對應 class，刪除命名類別 | ⚡ Fix-it-faster | 低 | [§4.2](#42-隊友名字出現在類別名稱中) |
| ⬜ | 重命名 package 為 `com.staytion.backend` | ⚡ Fix-it-faster | 低 | [§4.1](#41-package-名稱仍是-spring-initializr-預設值) |

---

### Stage 3：資料庫設計優化 + MySQL → PostgreSQL 遷移

| 狀態 | 任務 | 模式 | 學習價值 | 稽核編號 |
|------|------|------|---------|---------|
| ⬜ | 引入 Flyway，建立 baseline schema migration | 🎓 Learn-it-myself | **高**（重要業界工具） | [§5.3](#53-缺少-schema-migration-工具flyway--liquibase) |
| ⬜ | 修復 `Order.status` 的 MySQL ENUM `columnDefinition` | 🎓 Learn-it-myself | 高（JPA 映射原理） | [§5.2](#52-mysql-專屬語法在-entity-中硬編碼) |
| ⬜ | 修復 native query 中的 Boolean 語法 | ⚡ Fix-it-faster | 低 | [§5.2](#52-mysql-專屬語法在-entity-中硬編碼) |
| ⬜ | 替換 JDBC driver 和 datasource URL | ⚡ Fix-it-faster | 低 | [§5.4](#54-mysql--postgresql-遷移全面評估) |
| ⬜ | 修復 `HotelServiceImpl.searchHotels()` N+1 問題 | 🎓 Learn-it-myself | **高**（N+1 是面試熱點） | [§2.2](#22-搜尋邏輯載入全部資料到記憶體致命效能問題) / [§5.1](#51-hotel-搜尋-n1-問題最嚴重的效能問題) |
| ⬜ | 修復 `FavoriteServiceImpl.getFavoriteHotels()` N+1 | 🎓 Learn-it-myself | 高 | [§5.1](#51-hotel-搜尋-n1-問題最嚴重的效能問題) |
| ⬜ | 為常用查詢欄位加索引（Flyway migration） | 🎓 Learn-it-myself | 中（DB 設計概念） | [§5.5](#55-缺少索引可擴展性問題) |
| ⬜ | 本地 Docker PostgreSQL 驗證遷移正確性 | 🎓 Learn-it-myself | **高**（實際動手體驗） | [§5.4](#54-mysql--postgresql-遷移全面評估) |

---

### Stage 4：決定未完成 API 整合的命運

| 狀態 | 任務 | 模式 | 學習價值 | 稽核編號 |
|------|------|------|---------|---------|
| 🚧 | 選擇 Firebase 整合方向（保留/移除）並執行 | 🎓 Learn-it-myself | **高**（若保留：OAuth 流程設計） | [§6](#6-未完成的第三方整合firebase--google-maps) |
| ✅ | Google Sign-In 改為驗證 Google ID Token（選擇直接驗證，非透過 Firebase） | 🎓 Learn-it-myself | **高**（安全性核心） | [§6](#6-未完成的第三方整合firebase--google-maps) |
| ⬜ | 確認 Google Maps API Key 有效性，完善 GeocodingService 呼叫時機 | 🎓 Learn-it-myself | 中 | [§6](#6-未完成的第三方整合firebase--google-maps) |
| ⬜ | 決定是否保留 OAuth2 Client（Spring Security）並與 JWT 整合 | 🎓 Learn-it-myself | **高** | [§6](#6-未完成的第三方整合firebase--google-maps) |

---

### Stage 5：程式碼品質收尾（Portfolio 收尾）

| 狀態 | 任務 | 模式 | 學習價值 | 稽核編號 |
|------|------|------|---------|---------|
| ⬜ | 補齊 DTO 分層，統一命名規則 | ⚡ Fix-it-faster | 低 | [§4.5](#45-dto-命名與分層不一致) |
| ⬜ | 為 User Center 模組補單元測試 | 🎓 Learn-it-myself | **高**（面試必問） | — |
| ⬜ | 整合 `AuthService` 的 Firebase 和本地帳號狀態一致性 | 🎓 Learn-it-myself | 高 | [§2.4](#24-auth-流程db-寫入與-firebase-建立無法原子性回滾) |
| ⬜ | 為 Entity 加基礎業務邏輯（`order.cancel()`） | 🎓 Learn-it-myself | 中（DDD 入門） | [§3.1](#31-貧血領域模型anemic-domain-model) |
| ⬜ | 撰寫 README，說明架構、啟動方式、技術選型理由 | ⚡ Fix-it-faster | 低（但 Portfolio 必需） | — |

---

> **下一步行動建議**：Stage 1 全部完成後，才開始 Stage 2。Stage 1 的安全問題（特別是 1.3 JWT 驗證被停用）會影響你驗證其他功能是否正確運作的能力。建議從「恢復 JwtAuthenticationFilter」開始你的第一個 Learn-it-myself 任務。
