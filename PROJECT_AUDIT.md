# Staytion 後端專案稽核報告

> **撰寫日期**：2026-06-22（狀態最後同步：2026-08-23）  
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
| **狀態** | ✅ 已完成（2026-08-24 ～ 2026-08-26） — 10 個 controller 的 `@CrossOrigin` 已全部移除（`OrderController`、`ReviewController`、`EcpayController` 內原本被註解的 `@CrossOrigin` 屬於 §4.3 死碼範圍，不在本項處理）。`WebSocketConfig` 已從 `setAllowedOriginPatterns("*")` 收斂為與 `SecurityConfig` 一致的固定名單。**2026-08-26 完成第二輪修正**：發現原本的修法仍不完整——`WebConfig.java` 這個獨立的 `WebMvcConfigurer`（`/api/**`，設定跟 `SecurityConfig` 不同）當時沒被列進稽核範圍，跟 `SecurityConfig` 的 `corsConfigurer()` 同時存在，兩者路徑重疊（`/api/**` vs `/**`），實際合併/覆蓋行為不透明。已徹底改用 Spring Security 建議寫法：`SecurityConfig` 新增明確的 `CorsConfigurationSource` bean（`UrlBasedCorsConfigurationSource`，`/**` 對應單一份設定），`filterChain()` 改用 `.cors(cors -> cors.configurationSource(...))` 直接注入，取代原本靠 `.cors(Customizer.withDefaults())` 讓 Spring Security 反查 `WebMvcConfigurer` 的隱性機制；原本的 `corsConfigurer()` Bean 方法與 `WebConfig.java` 已整個刪除，CORS 設定現在只有唯一一份來源。 |
| **位置** | `SecurityConfig.java`（`corsConfigurationSource`、`filterChain`）、`WebSocketConfig.java`（`registerStompEndpoints`） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`SecurityConfig` 中已有全域 CORS 設定（僅允許 `127.0.0.1:5500`、`localhost:5500`），但多個 controller 還各自加上 `@CrossOrigin` 註解，形成雙重設定，且有些只允許單一 origin，邏輯容易混亂。更嚴重的是 `WebSocketConfig` 使用 `setAllowedOriginPatterns("*")`，完全不限制來源，與其他地方的策略矛盾。

**建議方向（已執行）**：  
移除所有 controller 層的 `@CrossOrigin`，統一用一個明確的 `CorsConfigurationSource` bean 集中設定（而非依賴 `WebMvcConfigurer` + Spring Security 自動反查），WebSocket 也已一致收緊為固定 origin 名單。

> **補充（2026-08-26，Learn-it-myself）**：這次順便釐清了 Spring 兩種 CORS 設定機制的差異——`WebMvcConfigurer.addCorsMappings()` 是 Spring MVC 層級的設定；`.cors(Customizer.withDefaults())` 在沒有明確 `CorsConfigurationSource` bean 時，會透過 `HandlerMappingIntrospector` 反查容器裡所有 `WebMvcConfigurer` bean 的設定，這也是原本 `WebConfig` + `SecurityConfig.corsConfigurer()` 兩者能同時生效、互相打架的根本原因。改用明確的 `CorsConfigurationSource` bean 直接餵給 `.cors(...)`，讓 CORS 規則只有一份、來源清楚，是業界對「有 Spring Security 的專案」的慣例寫法。

> **補充（2026-08-24）**：整理時發現兩個原本清單外的細節，皆已一併處理：① `EcpayController` 的 `@CrossOrigin` 多帶了兩個 `trycloudflare.com` 開發用 tunnel 網址（每次啟動 `cloudflared` 都會換），確認是過時的暫時性設定，已捨棄、未併入全域清單；② 同一個 `@CrossOrigin` 還列了 `https://payment-stage.ecpay.com.tw`（綠界測試站網域），雖然 CORS 只管瀏覽器發出的跨域請求、綠界對後端的 server-to-server 通知（`notify_url`）本不受 CORS 管控，但保守起見已一併加進 `SecurityConfig` 的全域 CORS 清單，避免日後有瀏覽器端流程需要用到時漏掉。

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

### 1.8 Review/Message 使用者輸入缺乏輸出端防護（Stored XSS 風險）（2026-08-25 新發現，聊天討論 CORS/CSRF/XSS 差異時）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `Review.comment`、`Message.content`（entity 欄位）；`ReviewController`、`MessageController`/`ChatController`（原封不動存入 DB、原封不動以 JSON 回傳） |
| **嚴重度** | 🟡 Med（後端本身不會執行任何程式碼，實際能否被觸發取決於前端渲染方式，前端不在本 repo 範圍內；但一旦前端用 `innerHTML` 等不安全方式渲染，就會變成能竊取其他使用者 JWT token 的 Stored XSS，故不能算低風險） |

**問題說明**：  
`Review.comment`、`Message.content` 存的是使用者原始輸入，後端目前沒有任何過濾或消毒，直接存進資料庫、直接透過 API 回傳給前端。如果攻擊者在評論或聊天訊息中寫入 `<script>...</script>`，且前端用 `element.innerHTML = comment` 這類方式渲染，瀏覽器會把它當成真的程式碼執行——這是 **Stored XSS（儲存型 XSS）**：惡意內容存在資料庫裡，之後每個瀏覽到這則評論/訊息的使用者都會中招，能拿到的東西包括其他人存在 `localStorage` 的 JWT token（詳見本次對話中 CORS/CSRF/XSS 的討論）。

專案裡剛好有個對照組：`src/main/resources/static/webSocketTest.html`（WebSocket 測試頁）第 65、102 行渲染訊息時用的是 `li.innerText = ...`，不是 `innerHTML`，即使內容包含 `<script>` 也只會被當純文字顯示——這是正確示範，但那只是一支測試頁，不是正式前端會怎麼做的保證。

**建議方向**：  
① 前端渲染使用者輸入時一律用 `textContent`/`innerText`，或改用會自動 escape 的框架（React/Vue 預設行為），絕不手動 `innerHTML` 拼接使用者資料——但這部分不在本 repo。② 後端可加一層防禦（defense in depth）：儲存前對明顯的 HTML tag 做過濾/escape，或至少在 API 回應上明確用 `Content-Type: application/json` 讓前端清楚知道這是純資料而非可信任的 HTML 片段（目前應該已經是 JSON，但沒有額外的內容過濾）。

---

### 1.9 MailService 用字串拼接組 HTML 信件（潛在地雷寫法）（2026-08-25 新發現，同上）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始 |
| **位置** | `MailService.sendHtmlMail()`；呼叫端 `AuthService.java`（`register()`、`sendResetPasswordEmail()`）、`AuthController.java`（`/api/auth/resend-verification`） |
| **嚴重度** | 🟢 Low（現況安全，是預防性問題——拼進 HTML 字串的目前只有後端自產的 JWT token 與設定檔網址，不是使用者可控的自由文字） |

**問題說明**：  
`sendHtmlMail()` 的呼叫端全部用字串拼接組 HTML，例如 `AuthService.java`：`"<p>請點擊以下連結驗證信箱：</p><a href=\"" + verifyLink + "\">驗證信箱</a>"`。目前拼進去的值都是後端自己產生的（JWT token、設定檔裡的驗證連結網址），不是使用者輸入，所以現況不構成可利用的漏洞。但「字串拼接組 HTML」這個寫法本身是危險模式：只要未來有人在這附近加一行、把使用者輸入（例如姓名）也拼進這段 HTML，就會立刻變成攻擊者能在寄給任意人的信件裡塞入惡意連結或指令碼的漏洞（可用於釣魚信、Email-based XSS）。這類問題不會在寫下的當下爆炸，是日後有人不知情地延伸這段程式碼時才會爆炸，屬於「地雷留在原地」型的技術債。

**建議方向**：  
若未來需要把使用者輸入放進 HTML 郵件內容，改用範本引擎（Thymeleaf 的 `TemplateEngine` 或類似工具）並確保有 HTML escape，而非手動字串拼接；目前的 code 可以先在該方法上加註解提醒「僅限拼接後端自產內容，勿放入使用者輸入」，或等改動時再一併處理。

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
| **狀態** | ✅ 已完成（commit `378597c`，2026-07-09，隨 §2.1 一併處理；2026-08-23 完成，尚未 commit） — `AuthController` 已移除 `UserRepository` 注入。`UserController` 已完成：新增 `UserService.getUserByEmail()`（找不到時拋 `UserNotFoundException`，`GlobalExceptionHandler` 已有對應 404 handler），`getProfile()` 改為呼叫該方法，`UserController` 的 `UserRepository` 欄位與 import 已移除。**`LocationController` 已完成**：新增 `LocationService`（`getAllCities()`、`getDistrictsByCity()`、`getAllDistricts()`、`searchLocations()`），新增 `dto/response/LocationOption.java`（record，含巢狀 `LocationType` enum，取代原本手動組 `Map<String,String>` 的做法），`LocationController` 四個方法皆已改為委派給 `LocationService`，`CityRepository`/`DistrictRepository` 注入與相關 import 已全部移除。**`ChatController` 已完成**：`sendMessage()` 改呼叫 `MessageService.sendMessage()`；原本的 `getChatRoomsByUser()`/`getChatRoomsByHotel()`（直接注入 `ChatRoomRepository`，且回傳裸 `ChatRoom` entity、洩漏巢狀 `User` 的 password hash，屬於 [§4.5.1](#451-controller-直接回傳-user-entity洩漏-password-hash2026-08-01-新發現並已修復) 同類問題）已整個搬到 `ChatRoomController`，改名為 `getChatRoomsByUser()`（`/api/chatrooms/my`，與既有的 `getMyChatRooms()` 合併，改用 JWT 解析身份而非信任 path variable，一併修掉潛在 IDOR）與 `getChatRoomsByHotel()`（`/api/chatrooms/hotel/{hotelId}`，新增），兩者皆改為委派給 `ChatRoomService`（新增 `getChatRoomsForHotel()`），回傳 `ChatRoomDto` 而非 entity。`ChatController` 的 `ChatRoomRepository`/`ChatRoom`/`User` 注入與 import 已全部移除；`ChatRoomController` 也順手移除了多餘的 `chatRoomRepository.save()` 重複存檔呼叫（`findOrCreateChatRoom()` 內的 `ChatRoomService` 已經存過一次）與對應的 Repository 注入。**`MessageController` 已完成**：`getChatHistory()`（`receiverId`+`hotelId` 查詢，已被 `chatRoomId` 統一取代）與 `sendMessage()`（REST 測試端點，已用 `@Operation` 自承「正式用 WebSocket」）兩個確認無呼叫端的方法已整個刪除；`getMessages()` 改為 `Authentication`+`getPrincipal()` 取得 userId（同 `ReviewController.deleteReview()` 模式，移除多餘的手動 JWT 解析與防呆檢查），delegate 給 `MessageService.getMessages()`——這同時修掉一個當時還沒被套用的真實漏洞：舊版 `getMessages()` 完全沒有驗證 `chatRoomId` 是否屬於該使用者，任何登入使用者換個 `chatRoomId` 就能讀到別人的對話（跟本項下方 2026-08-05 補充記錄的隱私外洩是同一個洞，只是 `MessageController` 這邊當時還沒套用修復）。`MessageRepository`/`UserRepository`/`HotelRepository`/`ChatRoomRepository`/`ChatRoomService`/`JwtService` 六個注入已全部移除，改為只注入 `MessageService`。**至此 `ChatController`、`ChatRoomController`、`MessageController` 皆已完全不直接存取 Repository，本項全部完成。** |
| **位置** | `AuthController.java`（注入 `UserRepository`）、`UserController.java`（注入 `UserRepository`） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
`AuthController` 的 `checkEmail()` 方法直接呼叫 `userRepository.existsByEmail()`，`UserController.getProfile()` 直接呼叫 `userRepository.findByEmail()`。Controller 不應知道 Repository 的存在，所有資料存取都應透過 Service 層，這樣才能統一套用業務規則、快取、異常處理邏輯。

**建議方向**：  
移除 Controller 中的 Repository 注入，改為在對應的 Service 中提供方法。

> **補充（2026-07-30 新增，處理 §3.2 時發現）**：本項「位置」清單原本只列 `AuthController`、`UserController`，但複查後發現 `ChatController`、`MessageController`、`LocationController` 也都直接注入 Repository（`ChatRoomRepository`、`UserRepository`、`HotelRepository`、`MessageRepository`、`CityRepository`、`DistrictRepository`）。這次只把它們改成建構子注入（§3.2 範圍），Repository 依賴本身尚未移除，待本項排入時一併處理。

> **補充（2026-08-05 新增，Learn-it-myself，處理 `ChatController`/`MessageController` 時）**：新增 `MessageService`（`sendMessage()`、`getMessages()`），並在設計過程中額外發現、修復兩個原本不在稽核清單中的授權漏洞：
> - `sendMessage()` 原本沒有驗證發送者是否真的屬於目標 `chatRoomId`，任何登入使用者只要猜得到別人的 chatRoomId，就能插入訊息偽造對話（IDOR）。
> - `getMessages()` 原本只驗證「有登入」，沒驗證「這個聊天室是不是你的」，等於任何登入使用者都能讀出別人的完整對話內容，是比寫入更嚴重的隱私外洩。
>
> 解法是讓 `ChatRoom` 既有的 `user1`/`user2` 參與者關聯本身成為授權依據（不符合就丟新增的 `ChatRoomAccessDeniedException`，對應 403；聊天室不存在則丟 `ChatRoomNotFoundException`，對應 404，兩者已在 `GlobalExceptionHandler` 註冊），同時讓 `receiver` 直接從 `chatRoom` 反推，不再信任呼叫端傳入的 `receiverId`。另外，`MessageController.sendMessage()`（REST 測試端點，已用註解自承「保留測試用，正式用 WebSocket」）與 `getChatHistory()`（用 receiverId+hotelId 查詢，功能與 `getMessages()` 用 chatRoomId 查詢完全重複）已確認在本 repo 內無任何呼叫端，決定直接刪除，不遷移。

> **補充（2026-08-19 新增，Learn-it-myself，處理 `ChatController` 時發現並修復）**：`WebSocketConfig` 原本完全沒有任何身份驗證機制——STOMP 連線在 CONNECT 階段不驗證任何東西，任何人都能建立連線、送任意訊息，`ChatController` 過去只能靠每個 `@MessageMapping` 方法自己在方法內手動重新解析 JWT，無法拒絕無效連線，也無法把錯誤回饋送給特定使用者（`@SendToUser` 需要 session 上綁定身份才能定位收件人）。修法是新增 `ChannelInterceptor`（`configureClientInboundChannel`），在 CONNECT frame 驗證 `Authorization` native header 帶的 JWT：header 缺失/格式錯、token 解析失敗、查無使用者，一律丟 `MessagingException` 直接拒絕連線（不允許匿名連線）；驗證成功則 `accessor.setUser(...)` 綁定身份。同時發現並修復另一個相關小缺口：`SecurityConfig` 從未明確設定 `SessionCreationPolicy`，代表這個名義上無狀態的 JWT API，實際上 Spring Security 預設的 session-based `SecurityContext` 還原機制仍啟用著，已補上 `SessionCreationPolicy.STATELESS`。
>
> **後續（2026-08-21，同一輪）**：`ChatController` 補上 `@MessageExceptionHandler`（對應 `ChatRoomNotFoundException`/`ChatRoomAccessDeniedException`）+ `@SendToUser("/queue/errors")`，讓 `sendMessage()` 的授權例外能實際送回發送者本人；`sendMessage()` 本身也已改為用 `Principal`（上述 CONNECT 階段綁定的身份）取代手動解析 JWT。
>
> **後續（2026-08-23，同一輪，尚未 commit）**：`ChatController` 剩下的 `getChatRoomsByUser()`/`getChatRoomsByHotel()` 已搬到 `ChatRoomController`，改為委派給 `ChatRoomService`、回傳 DTO；`MessageController` 也已完成（刪除兩個無呼叫端的方法、`getMessages()` 改用 `Authentication`+delegate 給 `MessageService`），詳見上方狀態欄。**本項（§2.3）全部完成。**

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

### 2.5 聊天室資料模型的擴充性與效能限制（2026-08-05 新發現，處理 §2.3 `MessageService` 時）

| 項目 | 內容 |
|------|------|
| **狀態** | ⬜ 未開始（先記錄，暫緩處理，優先完成 §2.3 的 Repository 移除） |
| **位置** | `ChatRoom.java`、`Message.java`、`MessageService.resolveDisplayName()` |
| **嚴重度** | 🟢 Low（目前資料量下不影響功能，是可擴充性/效能債，非 bug） |

**問題說明**：

**① `ChatRoom` 寫死 `user1`/`user2` 兩個固定欄位，只能支援雙人對話。** 業界常見做法是另外用一張「參與者關聯表」（例如 `chat_room_participants`，存 `chatRoomId` + `userId` 的多對多關係），才能不改資料庫結構就擴充成群組聊天。目前的設計是合理的 MVP 簡化，但代表未來若要支援群組對話（例如「飯店 + 房客 + 客服」三方），會是一次結構性重寫。

**② `Message.isRead` 存在每一則訊息上，`MessageService.getMessages()` 用迴圈逐筆呼叫 `messageRepository.save()` 標記已讀。** 聊天室訊息量一多，打開聊天室這個動作就會觸發等量的 UPDATE。更常見的做法是在「聊天室-使用者」這個組合上存一個單一的 `lastReadAt`（或 `lastReadMessageId`），使用者打開聊天室只需要一次 UPDATE，判斷「這則訊息讀了沒」改用 `message.sentAt > lastReadAt` 比較即可。跟 [§2.2](#22-搜尋邏輯載入全部資料到記憶體致命效能問題)、[§5.1](#51-hotel-搜尋-n1-問題最嚴重的效能問題) 是同一類「逐筆處理造成 N 次資料庫操作」的思維。

**③ `MessageService.resolveDisplayName()` 對每一則訊息都重新判斷一次「對話對象是誰」。** 但「對話對象是誰」其實是**聊天室**的屬性，同一個 `chatRoomId` 底下所有訊息的答案都一樣（`chatRoom.getUser1()`/`getUser2()` 兩者之一，不會因訊息而變）。目前寫法在 `getMessages()` 回傳的訊息列表裡，對每一筆訊息都重算一次，可以改成在方法一開始只算一次「對方是誰」，重複使用同一個結果。

**建議方向**：  
① 若確定未來會做群組聊天，改用參與者關聯表；若确定不會，維持現狀即可，但應在文件中明確記錄這個設計取捨。② 改用 `lastReadAt` 欄位，一次 UPDATE 取代逐筆 UPDATE。③ 把「對方是誰」的判斷從 `resolveDisplayName()` 內部移到 `getMessages()` 開頭算一次，重複利用。

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
| **狀態** | ✅ 已完成（commit `378597c`，2026-07-09 解除註解啟用；2026-07-31 補完重複樣板重構） — 已解除註解啟用，隨 §2.1 陸續新增例外類別後共 9 個 `@ExceptionHandler`。2026-07-31：9 個 handler 原本重複的 `HashMap` 組裝邏輯已抽成 private helper `buildErrorResponse(Exception ex, HttpStatus status, String errorType)`，每個 handler 現在只剩一行 `return buildErrorResponse(...)`，行為與重構前一致。 |
| **位置** | `exception/GlobalExceptionHandler.java`（整個檔案被 `//` 註解） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
原本設計了集中錯誤處理機制，但目前整個 `@ControllerAdvice` 類別都被註解掉。沒有全域例外處理器意味著：未預期的 RuntimeException 會被 Spring 包成 500 錯誤，且沒有統一的錯誤 JSON 結構，前端無法穩定解析錯誤訊息。

**建議方向**：  
啟用 `GlobalExceptionHandler`，補上常用的例外映射（`IllegalArgumentException → 400`、`RuntimeException → 500`），並定義統一的錯誤回應 DTO（`ErrorResponse { status, message, timestamp }`）。

> **✅ 部分已修復（2026-07-09，隨 §2.1 Auth 重構一併處理）**：`GlobalExceptionHandler` 已解除註解啟用，並新增 `InvalidCredentialsException`（401）、`EmailNotVerifiedException`（403）的映射，與既有的 `HotelNotFoundException`、`RoomTypeNotFoundException`（皆 404）並列。

> **✅ 已修復（2026-07-31）**：原本待辦的重複樣板已收斂——新增 private helper `buildErrorResponse(Exception ex, HttpStatus status, String errorType)`，統一組裝 `timestamp`/`status`/`error`/`message` 四個 key，9 個 `@ExceptionHandler` 都改為呼叫這個 helper。之後若要調整錯誤回應格式（例如加欄位），只需要改一個地方。

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
| **狀態** | ⬜ 未開始（全專案命名/目錄整理仍未進行；下方 4.5.1 是隨手發現的獨立子問題，範圍較小已先修復） |
| **位置** | `dto/` 根目錄（平面放置）vs. `dto/request/`、`dto/response/` 子目錄 |
| **嚴重度** | 🟢 Low |

**問題說明**：  
部分 DTO 放在 `dto/` 根目錄（如 `LoginRequest.java`、`RegisterRequest.java`、`FavoriteDto.java`），部分放在 `dto/request/`、`dto/response/` 子目錄（如 `CreateReviewRequestDto.java`、`ReviewResponseDto.java`）。命名也不統一：有的加 `Dto` 後綴，有的加 `DTO`，有的不加。

**建議方向**：  
統一用 `dto/request/` 和 `dto/response/` 分類，命名一律用 `XxxRequest` / `XxxResponse` 不加後綴 DTO/Dto。

---

### 4.5.1 Controller 直接回傳 User entity，洩漏 password hash（2026-08-01 新發現並已修復）

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（2026-08-01，處理 §2.3 `UserController` 時順帶發現） — 新增 `dto/response/UserResponse.java`（record，含 `static factory` `from(User)`，只挑選 `id`/`firstName`/`lastName`/`email`/`tel`），`UserController.getProfile()`、`updateProfile()` 皆已改為回傳 `UserResponse` 而非 `User`。 |
| **位置** | `UserController.java`（`getProfile()`、`updateProfile()`） |
| **嚴重度** | 🟡 Med（敏感欄位外洩，但需先竊取有效 JWT 才能觸發，故非 🔴） |

**問題說明**：  
`User` entity 的 `password` 欄位（bcrypt hash）沒有 `@JsonIgnore` 或其他遮蔽機制，`getProfile()`、`updateProfile()` 原本直接把整個 `User` entity 序列化回傳，導致密碼 hash 一併外洩給前端（可在瀏覽器開發者工具的 Network 分頁看到）。這是「Controller 直接回傳 JPA entity 當 API 回應」的通例——entity 的欄位是資料庫層的關注點，不該跟 API contract 綁在一起，屬於 §4.5 DTO 分層問題的具體案例。

**建議方向（已執行）**：  
新增 Response DTO，只挑選 API 需要的欄位，用 static factory method 集中轉換邏輯，避免每個 Controller 各自手動組欄位。

---

### 4.6 ReviewService 的日期過濾 bug

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（2026-08-23） — `endDate` 已改傳 `endOfDay`（`LocalDateTime`）。同時發現並修復 `startDate` 同一類型別不一致問題：原本直接拿 `LocalDate` 跟 `createdAt`（`LocalDateTime`）比較，已改用 `startDate.atStartOfDay()`，與 `endOfDay` 對稱。 |
| **位置** | `ReviewService.java`（`searchReviewsForHost` 方法） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
程式碼計算了 `endOfDay`（`endDate.atTime(LocalTime.MAX)`），但最後傳給 `cb.lessThanOrEqualTo` 的是原始的 `endDate`（`LocalDate`），而非 `endOfDay`（`LocalDateTime`）。這個 bug 會導致在查詢「結束日期當天」建立的評論時，因為時間比對不正確而遺漏資料。

**建議方向（已執行）**：  
將 `endDate` 改為 `endOfDay`（`LocalDateTime`）傳入 Specification；`startDate` 比照改用 `atStartOfDay()`。

---

### 4.7 javax.annotation vs jakarta.annotation

| 項目 | 內容 |
|------|------|
| **狀態** | ✅ 已完成（2026-08-24） — 已改為 `jakarta.annotation.PostConstruct`。註：`FirebaseConfig` 本身按 [§6](#6-未完成的第三方整合firebase--google-maps) 已定案未來會整檔移除，這裡先修是為了目前 classpath 正確性；若之後確定移除 Firebase，這個檔案會一併刪除，屆時本項修改自動失效不影響。 |
| **位置** | `FirebaseConfig.java`（`import jakarta.annotation.PostConstruct`） |
| **嚴重度** | 🟡 Med |

**問題說明**：  
本專案使用 Spring Boot 3.5.0（Java EE → Jakarta EE 命名空間遷移後）。`javax.annotation.PostConstruct` 在 Spring Boot 3.x 中已被棄用，應改為 `jakarta.annotation.PostConstruct`。雖然部分情境仍可運作（若 classpath 中有相關依賴），但這是潛在的問題。

**建議方向（已執行）**：  
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
| ✅ | 啟用 `GlobalExceptionHandler`，統一錯誤回應格式 | 🎓 Learn-it-myself | 高 | [§3.3](#33-globalexceptionhandler-完全被註解) |
| ✅ | 移除 Controller 中的 Repository 直接存取 | 🎓 Learn-it-myself | 高 | [§2.3](#23-controller-直接存取-repository) |
| ✅ | 修復 `UserController` 洩漏 `User` entity（含 password hash） | 🎓 Learn-it-myself | 高（DTO 分層、record 語言特性） | [§4.5.1](#451-controller-直接回傳-user-entity洩漏-password-hash2026-08-01-新發現並已修復) |
| ✅ | 修復 `FirebaseConfig` 的 `javax` → `jakarta` import | ⚡ Fix-it-faster | 低 | [§4.7](#47-javaxannotation-vs-jakartaannotation) |
| ✅ | 修復 `ReviewService` 日期過濾 bug | ⚡ Fix-it-faster | 低 | [§4.6](#46-reviewservice-的日期過濾-bug) |
| ✅ | 統一 CORS 設定，移除分散的 `@CrossOrigin` | ⚡ Fix-it-faster | 低 | [§1.5](#15-cors-設定不一致websocket-完全開放) |
| 🚧 | 刪除死碼（`OrderService.java`、大量 comment blocks） | ⚡ Fix-it-faster | 低 | [§4.3](#43-大量被註解的死碼) |
| ⬜ | 替換 `System.out.println` 為 SLF4J Logger | ⚡ Fix-it-faster | 低（但面試表現重要） | [§4.4](#44-systemoutprintln-滿佈-production-程式碼) |
| ⬜ | 整合 Arvin 系列到對應 class，刪除命名類別 | ⚡ Fix-it-faster | 低 | [§4.2](#42-隊友名字出現在類別名稱中) |
| ⬜ | 重命名 package 為 `com.staytion.backend` | ⚡ Fix-it-faster | 低 | [§4.1](#41-package-名稱仍是-spring-initializr-預設值) |
| ⬜ | Review/Message 使用者輸入加輸出端防護（Stored XSS） | 🎓 Learn-it-myself | 高（User Center 模組 + 安全性核心概念） | [§1.8](#18-reviewmessage-使用者輸入缺乏輸出端防護stored-xss-風險2026-08-25-新發現聊天討論-corscsrfxss-差異時) |
| ⬜ | `MailService` 改用範本引擎，移除字串拼接組 HTML 的地雷寫法 | ⚡ Fix-it-faster | 低（現況安全，屬預防性技術債） | [§1.9](#19-mailservice-用字串拼接組-html-信件潛在地雷寫法2026-08-25-新發現同上) |

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
| ⬜ | 聊天室改用參與者關聯表 / `lastReadAt` 取代逐筆已讀 / `displayName` 只算一次 | 🎓 Learn-it-myself | 中（資料模型擴充性） | [§2.5](#25-聊天室資料模型的擴充性與效能限制2026-08-05-新發現處理-23-messageservice-時) |
| ⬜ | 撰寫 README，說明架構、啟動方式、技術選型理由 | ⚡ Fix-it-faster | 低（但 Portfolio 必需） | — |

---

> **下一步行動建議**：Stage 1 全部完成後，才開始 Stage 2。Stage 1 的安全問題（特別是 1.3 JWT 驗證被停用）會影響你驗證其他功能是否正確運作的能力。建議從「恢復 JwtAuthenticationFilter」開始你的第一個 Learn-it-myself 任務。
