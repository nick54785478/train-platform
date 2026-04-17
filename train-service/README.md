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

## 核心技術亮點：分散式事務 (Saga Pattern)
為了保證「訂票 -> 劃位 -> 扣款」這一連串跨服務動作的資料一致性，本專案實作了 基於事件驅動 (Choreography-based) 的 Saga 模式：

### 1. 交易流程設計

>* **成功路徑 (Happy Path)：** Booking Created → Seat Booked → Money Deposited & Account Activated → Saga Completed (發送歡迎通知)。
>* **補償路徑 (Compensation Path)：** 若初始儲值失敗，系統會觸發自動補償機制：Freeze Account (凍結帳戶) → Release Seat (釋放座位) → Mark Booking Failed
>* **手動退票路徑 (Manual Cancellation)：** 使用者發起退票 → Booking Cancelled → Release Seat → Refund to Account (非同步退款)。

### 2. 資料一致性與可靠性 (Outbox Pattern)
為了確保「資料庫更新」與「訊息發送」的原子性，引入了 Outbox Pattern：

>* EventLog 機制：所有領域事件先隨業務交易存入 EVENT_LOG 表。
>* 定時排程 (Scheduler)：獨立執行緒掃描 Outbox 表，確保訊息 At-least-once (至少一次) 送達 RabbitMQ。
>* 冪等性檢查 (Idempotency)：所有 Consumer 均實作 checkEventIdempotency，防止網路波動造成的重複消費。

### 3. 全線追蹤 (Correlation ID)
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





