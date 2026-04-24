## 緣起

於待業期間，想要弄個簡單作品集，所以有這個範例的誕生。本專案改寫自前單位的新人訓練教材，是一個基於 六角形架構 (Hexagonal Architecture) 的火車與車票相關微服務範例。

**主要演進：**

>* **技術棧升級**：從 SpringBoot 2.X 升級至 3.3.3，並將 Rocket MQ 替換為 Rabbit MQ（練習在 SpringBoot 3 環境下對 RabbitMQ 的實作與優化）。
>* **功能擴充**：原範例僅有核心車務，本專案自行發想並擴充了會員系統、帳戶儲值、自動化退票流程等功能。
>* **架構強化**：引入了 Saga Pattern 與 Outbox Pattern，解決分散式架構下的資料最終一致性問題。


## 框架及外部依賴

>* Java
>* SpringBoot 3.3.3
>* JDK 17
>* MySQL
>* Rabbit MQ
>* MinIO
>* Lombok & ModelMapper (簡化代碼與物件轉換)
>* Mailhug：外部開發依賴，用於攔截並模擬 SMTP 郵件發送，方便開發者即時預覽郵件渲染效果。
>* FreeMarker：作為範本引擎，支援 HTML 動態內容渲染。

## 核心技術亮點：

### 1. 分散式事務 (Saga Pattern)
為了保證跨微服務動作的資料最終一致性，本專案實作了兩段基於 事件驅動 (Choreography-based) 的 Saga 流程：

**A. 會員開戶與初始儲值 Saga**
處理使用者註冊後，必須確保帳戶資金與帳戶狀態同步變更的流程。

**成功路徑 (Happy Path)：**
> 1. Account Created：建立帳戶實體（狀態為 PENDING）。
> 2. Initial Deposit：執行初始資金存入。
> 3. Account Activated：資金到位，將帳戶轉為啟用狀態。
> 4. Saga Completed：觸發 NotificationService 發送 開戶成功歡迎信。

**補償路徑 (Compensation Path)：**
> 若初始儲值失敗：觸發 Freeze Account (凍結帳戶) → 標記註冊異常 → 發送 開戶失敗警告信，引導使用者聯繫客服。

**B. 訂位購票與扣款 Saga**
處理從發起訂票、鎖定座位到帳戶扣款的複雜交易鏈。

**成功路徑 (Happy Path)：**

> 1. Booking Created：建立訂票紀錄。
> 2. Seat Reserved：透過 SeatService 完成劃位（含 MySQL 業務冪等鎖定）。
> 3. Fare Charged：從會員帳戶扣除票價。
> 4. Booking Completed：更新訂單狀態為已完成。
> 5. Saga Completed：觸發通知中心發送 訂票成功確認信。

**補償路徑 (Compensation Path)：**

> 若扣款失敗：觸發 Release Seat (釋放座位) → Rollback Booking (標記訂票失敗) → 發送 訂票失敗通知信。

**手動退票路徑 (Manual Cancellation)：**
> 使用者發起退票 → Booking Cancelled → Release Seat → Refund to Account (執行非同步退款流程)。


### 2. 動態化通知中心 (Notification Center)
本專案實作了一套高度解耦的通知機制：

>* Port-Adapter 模式：定義了 MailSenderPort 與 TemplateEnginePort，將業務邏輯與具體的發信技術、渲染技術隔離。
>* DB-Driven Templates：郵件主旨與 HTML 內容均存於 MySQL 的 EMAIL_TEMPLATE 表中，支援不重啟程式即可動態修改郵件措辭。
>* 範本後台維護 (CRUD)：提供 API 支援前端頁面維護 EMAIL_TEMPLATE 表。支援動態調整郵件主旨與 HTML 內容，無需重新發布程式即可生效。
>* 個人化渲染：利用 FreeMarker 引擎，將領域物件（如 username, bookingUuid）動態注入範本。

### 3. 業務冪等執行器 (Business Idempotency)
針對高併發下的「搶號/劃位」情境，實作了 BusinessIdempotentExecutorPort：

>* 決定性 Key 生成：根據「車次+日期+座位號」生成唯一業務 Key。
>* 物理鎖定：利用 MySQL 的 UNIQUE KEY 特性確保在分散式環境下，同一資源僅會被成功領取一次，防止重複劃位衝突。

### 4. 資料一致性與可靠性 (Outbox Pattern)
為了確保「資料庫更新」與「訊息發送」的原子性，引入了 Outbox Pattern：

>* EventLog 機制：所有領域事件先隨業務交易存入 EVENT_LOG 表。
>* 定時排程 (Scheduler)：獨立執行緒掃描 Outbox 表，確保訊息 At-least-once (至少一次) 送達 RabbitMQ。
>* 冪等性檢查 (Idempotency)：所有 Consumer 均實作 checkEventIdempotency，防止網路波動造成的重複消費。

### 5. 全線追蹤 (Correlation ID)
透過 eventTxId (Saga Transaction ID) 貫穿整個事件鏈，無論流程跳轉多少個服務，皆可透過單一 ID 在日誌中追蹤完整業務生命週期。


## 部署及啟用專案
		     
### 第一步: Clone AuthService

https://github.com/nick54785478/reactive-system-demo 	

**註.** 
>* auth-service 占用 8088 port，所以須在 application.properties 中設置 server.port=8088，再執行以下打包動作
>* MySQL 占用 3307 port

**建置步驟**
> 1. 在Maven項目或者pom.xml上右鍵 -->  Run As --> "Maven Build... " 或 Run Configuration --> "Maven Build"。
> 2. 在"Goals"输入框中输入：**clean install** 。 <br/>
> 3. 使用時在 Run As 中選中 Maven build 即可。 <br/>
> 4. console 出現 BUILD SUCCESS 即打包完成。 <br/>
> 5. 透過命令列(CMD) 進入 Dockerfile 所在目錄(我放在專案內那一層)。 <br/>
> 6. 輸入 **docker build -t {image 名稱} .**  (註. 可透過 docker images 看是否有打包成功。)。 <br/>
> 7. 解壓縮 **auth-service.zip** (放在專案內那一層)。 <br/>
> 8. 到 auth-service 資料夾內點擊 run.sh，即容器化完成。 <br/>
> 9. 在該 MySQL 中建立 auth 庫，並將 schema.sql 和 data.sql 的內容執行(新增資料表及資料)。 <br/>
> 10. 註冊一個使用者帳號以進行後續動作，因需要通過 JWToken 驗證，且相關功能可能會用使用者帳號。 <br/>

**範例:** 

**POST**  http://localhost:8088/api/v1/users/register  <br/>

Request Body :

<br/>

 ```
	 {
	    "name": "Nick",
	    "email": "nick123@example.com", // 信箱
	    "username":"nick123@example.com", // 帳號與信箱同(以利後續使用)
	    "password":"password123", // 密碼
	    "address":"台北市內湖區"	
	}
 ```

<br/> 

### 第二步: Clone train-service

透過 Docker 或 本地安裝，安裝 MYSQL 資料庫，以下為 yml 檔供參考:

```
version: "3"
services:
  db:
    image: mysql:8.0
    container_name: local-mysql
    restart: always
    environment:
      TZ: Asia/Taipei
      MYSQL_ROOT_PASSWORD: root 
    command:
      --max_connections=1000
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_unicode_ci
      --default-authentication-plugin=mysql_native_password
    ports:
      - 3306:3306
    volumes:
      - ./data:/var/lib/mysql
      - ./conf:/etc/mysql/conf.d
    networks:
        mysql:
          aliases:
            - mysql
networks:
  mysql:
    name: mysql
    driver: bridge
```
<br/>


**可執行下列指令建立 docker container**

```
        docker-compose up -d
``` 

<br/>


### 第三步: 建立表及相關外部依賴

>* 準備 MySQL (不論是 容器 或 本地端資料庫)。
>* 執行專案目錄下的 init-schema.sql 進行。
>* 可執行 data.sql (我的測試資料)。
>* 準備 RabbitMQ (不論是 容器 或 本地端)。

<br />


### 第四步: 使用 Postman 對其進行測試

train-demo.postman_collection.json
<br />

放在專案目錄下。





