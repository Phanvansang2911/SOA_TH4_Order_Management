# SOA - Bài thực hành 4: Order Management

Ba Spring Boot services độc lập, ba cổng và ba database MySQL riêng.

| Service | Port | Database | Vai trò |
|---|---:|---|---|
| auth-service | 3000 | auth_db | Register, login, kiểm tra Bearer token |
| product-service | 3001 | product_db | CRUD sản phẩm (giữ code bài trước) |
| order-service | 3002 | order_db | CRUD đơn hàng và chi tiết, kiểm tra tồn kho qua REST |

## Chuẩn bị
- Java 17+ (đã thiết kế với Spring Boot 4.1.1), Maven, MySQL 8, Postman.
- Trong MySQL Workbench chạy `database/create_databases.sql`.
- Mở PowerShell mới và đặt thông tin DB (cần đặt cho từng terminal chạy service):
  ```powershell
  $env:DB_USERNAME = "root"
  $env:DB_PASSWORD = Read-Host "MySQL password"
  ```
  Hoặc cấu hình biến môi trường của Windows vĩnh viễn (không ghi mật khẩu vào mã nguồn).

## Khởi chạy: mở 3 terminal riêng
Trong mỗi terminal đặt `DB_USERNAME` và `DB_PASSWORD` như trên, sau đó:

```powershell
cd auth-service
mvn spring-boot:run
```

```powershell
cd product-service
mvn spring-boot:run
```

```powershell
cd order-service
mvn spring-boot:run
```

Các lệnh `cd` tính từ thư mục gốc; mở mỗi terminal tại thư mục gốc.

## Kiểm thử Postman
1. `POST http://localhost:3000/register`, JSON: `{"email":"student@example.com","password":"Password123!"}` -> 201.
2. `POST http://localhost:3000/login`, cùng JSON -> 200 `access_token`.
3. Các yêu cầu Product/Order đều có header `Authorization: Bearer <access_token>`.
4. `POST http://localhost:3001/products`, JSON: `{"name":"Ban phim","description":"Co","price":250000,"quantity":10}` -> 201. Ghi lại id sản phẩm.
5. `POST http://localhost:3002/orders`, JSON:
   ```json
   {"customerName":"Nguyen Van A","customerEmail":"a@example.com","items":[{"productId":1,"quantity":2}]}
   ```
   Đổi `productId` theo bước 4. -> 201, tự chốt giá tại thời điểm đặt, tổng 500000 nếu giá giữ nguyên.
6. `GET http://localhost:3002/orders` -> 200. `GET /orders/{id}` -> 200.
7. `PUT http://localhost:3002/orders/1` body `{"status":"completed"}` -> 200.
8. `GET /order_items`, `GET /order_items/{id}`, `POST /order_items`, `PUT /order_items/{id}`, `DELETE /order_items/{id}`.
9. Tạo đơn với `quantity` > `Product.quantity` -> 409; token sai -> 401; tắt Product Service -> 503.

## Toàn bộ endpoint
- Auth: `POST /register`, `POST /login`, `GET /auth`.
- Products: `GET /products`, `GET /products/{id}`, `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}`.
- Orders: `GET /orders`, `GET /orders/{id}`, `POST /orders`, `PUT /orders/{id}`, `DELETE /orders/{id}`.
- Items: `GET /order_items`, `GET /order_items/{id}`, `POST /order_items`, `PUT /order_items/{id}`, `DELETE /order_items/{id}`.

Example for `POST /order_items`: `{"orderId":1,"productId":1,"quantity":1}`.
Example for `PUT /order_items/1`: `{"productId":1,"quantity":3}`.

## SOA và giới hạn bản thực hành
- Mỗi service lưu bảng riêng. `order_items.product_id` chỉ tham chiếu bằng ID, không có foreign key xuyên database.
- Order Service gọi `GET /products/{id}` qua HTTP và chuyển tiếp token đến Product Service.
- Order Service kiểm tra tồn kho nhưng **không trừ/giữ tồn kho** vì đề chỉ yêu cầu kiểm tra. Với môi trường thực, cần xử lý reservation/trừ kho nguyên tử, cạnh tranh đồng thời, và nhất quán liên dịch vụ.
- Mặt hàng lưu tên và giá sản phẩm thời điểm tạo/sửa; sửa giá ở Product Service sau đó không tự đổi giá đơn đã đặt.
- `order_items` là tên bảng dùng thống nhất với endpoint; phụ lục đề có chỗ ghi `other_items`.
- `ddl-auto=update` phù hợp demo lớp học, hệ thống thật nên thay bằng migration Flyway.
- Token là opaque random token băm SHA-256 trong Auth DB, hiệu lực 8 giờ, không phải JWT. Password BCrypt. Cần bổ sung phân quyền, logout, rate-limit, HTTPS cho môi trường thật.
- Không upload file `.env`, mật khẩu, IDE cache, `target/` lên GitHub.

## GitHub
Tạo repository trống trên GitHub (ví dụ `soa-th4-order-management`). Tại thư mục gốc:
```powershell
git init
git add .
git commit -m "feat: implement SOA TH4 auth product and order services"
git branch -M main
git remote add origin https://github.com/<YOUR_USERNAME>/soa-th4-order-management.git
git push -u origin main
```
Thay `<YOUR_USERNAME>` bằng tài khoản GitHub của mày; không dùng repository Rental Property của nhóm.
