---
trigger: always_on
---

# Project Rules: Manual Java Web (Pure Scratch)

## 1. Core Principles (Bắt buộc)

- **NO Frameworks**: Tuyệt đối không dùng Spring, Spring Boot, Hibernate, JPA.
- **NO DI/ORM**: Không dùng thư viện Dependency Injection hay ORM.
- **NO Annotations**: Không dùng `@Service`, `@Repository`, `@Autowired`, `@Entity`, v.v. (Ngoại trừ `@Override`).
- **NO Lombok**: Viết tay Getter/Setter, Constructor, ToString.

## 2. Tech Stack

- **Java 17**: Sử dụng Record, Modern Switch, Text Blocks.
- **Server**: `com.sun.net.httpserver.HttpServer`.
- **Database**: JDBC thuần (`mysql-connector-j`) với `PreparedStatement`.
- **JSON**: Google `Gson`.
- **Auth**: `jjwt` (JWT) & `jbcrypt` (Password hashing).

## 3. Project Architecture (com.polime.\*)

- **core**: Chứa `WebServer`, `BaseHandler`, `DatabaseManager`.
- **controller**: Kế thừa từ `BaseHandler`, đăng ký route qua `get()`, `post()`.
- **service**: Xử lý logic nghiệp vụ.
- **repository**: Thao tác SQL thuần. Map `ResultSet` sang Model thủ công.
- **model**: Thực thể (Entity) POJO.
- **dto**: Đối tượng truyền tải dữ liệu (Request/Response).

## 4. Coding Standards

- **Ném Exception**: Dùng các Custom Exception (`ValidationException`, v.v.) để `BaseHandler` tự xử lý lỗi.
- **Response**: Luôn trả về thông qua `BaseResponseDto<T>` và enum `EHttpStatus`.
- **Naming**:
  - Enum bắt đầu bằng `E` (ví dụ: `EHttpStatus`).
  - Hằng số SQL dùng `TABLE_NAME`, `COLUMN_...`.
- **SQL**: Viết hoa từ khóa (`SELECT`, `INSERT`, `FROM`, `WHERE`).
- **Formatting**: Thụt lề 4 spaces (Theo `formatter.xml`).
- **Resource Management**: Luôn đóng Connection/Statement/ResultSet trong `finally` hoặc try-with-resources.

## 5. Build System

- **Maven**: Quản lý dependency qua `pom.xml`.
- **Style**: Tuân thủ Google Java Style (đã config trong Checkstyle).
