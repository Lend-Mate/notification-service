# Notification Service

Sipariş olaylarını dinleyerek müşterilere ve ürün sahiplerine e-posta bildirimi gönderen, bildirim kayıtlarını yöneten HemenKirala servisi.

---

## İçindekiler
- [Genel Bakış](#genel-bakış)
- [Mimari](#mimari)
- [Teknolojiler](#teknolojiler)
- [Veritabanı](#veritabanı)
- [API Endpoints](#api-endpoints)
- [Servisler Arası İletişim](#servisler-arası-i̇letişim)
- [Kurulum](#kurulum)
- [Ortam Değişkenleri](#ortam-değişkenleri)
- [Testler](#testler)

---

## Genel Bakış

Notification Service, LendMate uygulamasındaki bildirimlerin oluşturulmasından ve e-posta olarak gönderilmesinden sorumludur.

- `order-topic` Kafka topic'inden sipariş olaylarını tüketir.
- Siparişteki müşteri, ürün ve ürün sahibi bilgilerini `user-service` ve `product-service` üzerinden alır.
- Thymeleaf şablonlarıyla müşteriye sipariş onayı, ürün sahibine sipariş bilgisi e-postası hazırlar.
- Gönderim sonucunu PostgreSQL'deki `notification` tablosuna kaydeder.
- REST API üzerinden bildirimleri listeleme, görüntüleme, oluşturma ve silme işlemlerini sağlar.
- Plain text veya HTML e-posta gönderimini doğrudan tetikleyen endpoint'ler sunar.

---

## Mimari

### Katmanlar

- **Controller:** `/notifications` REST endpoint'lerini dışarı açar.
- **Service:** Bildirim kayıtlarını yöneten `NotificationService` ve e-posta gönderen `MailService` iş mantığını barındırır.
- **Kafka Consumer:** `OrderConsumer`, sipariş olaylarını işler ve bildirim akışını başlatır.
- **Feign Clients:** `user-service` ve `product-service` ile senkron HTTP iletişimi kurar.
- **Repository:** Spring Data JPA üzerinden `Notification` kayıtlarına erişir.
- **Persistence:** Flyway migration'ı ile PostgreSQL şeması oluşturulur.
- **Template:** Thymeleaf, müşteri ve ürün sahibi e-posta içeriklerini üretir.

### Klasör Yapısı

```text
src/main/java/com/lendmate/notificationservice/
├── controller/       # REST API
├── dto/               # İstek ve yanıt modelleri
├── feignClient/       # User ve product servis istemcileri
├── kafka/             # OrderEvent ve Kafka consumer
├── mapper/            # DTO <-> entity dönüşümleri
├── model/             # JPA entity'leri
├── repository/        # Veritabanı erişimi
├── service/           # Servis arayüzleri ve implementasyonları
└── utility/           # Sabitler ve yardımcı yapılar

src/main/resources/
├── db/migration/      # Flyway SQL migration'ları
└── templates/         # E-posta HTML şablonları
```

---

## Teknolojiler

| Teknoloji | Versiyon | Kullanım Amacı |
|---|---|---|
| Java | 21 | Uygulama çalışma ortamı |
| Spring Boot | 3.5.15 | Uygulama çatısı |
| Spring Web | Spring Boot ile yönetilir | REST API |
| Spring Data JPA / Hibernate | Spring Boot ile yönetilir | ORM ve kalıcı veri erişimi |
| PostgreSQL | Runtime bağımlılığı | Bildirim veritabanı |
| Spring Kafka | Spring Cloud BOM ile yönetilir | Sipariş olaylarının tüketilmesi |
| Spring Cloud OpenFeign | 2025.0.0 BOM | Servisler arası HTTP çağrıları |
| Spring Boot Mail | Spring Boot ile yönetilir | E-posta gönderimi |
| Thymeleaf | Spring Boot ile yönetilir | HTML e-posta şablonları |
| Flyway | Spring Boot ile yönetilir | Veritabanı migration'ları |
| MapStruct | 1.6.3 | DTO/entity dönüşümleri |
| Springdoc OpenAPI | 2.8.16 | API dokümantasyonu |
| OpenTelemetry | 2.6.0 | İzlenebilirlik ve Feign trace aktarımı |

---

## Veritabanı

### Tablolar

#### `notification`

| Kolon | Tip | Açıklama |
|---|---|---|
| `id` | `BIGSERIAL` | Birincil anahtar |
| `user_id` | `BIGINT` | Bildirimin hedef kullanıcısı |
| `type` | `VARCHAR(50)` | Bildirim türü |
| `channel` | `VARCHAR(20)` | Kanal; ör. `EMAIL` |
| `title` | `VARCHAR(255)` | Bildirim/e-posta başlığı |
| `message` | `TEXT` | Bildirim içeriği |
| `status` | `VARCHAR(20)` | Gönderim durumu; ör. `COMPLETED`, `FAILED` |
| `read_at` | `TIMESTAMP` | Okunma zamanı |
| `sent_at` | `TIMESTAMP` | Gönderilme zamanı |
| `created_at` | `TIMESTAMP` | Oluşturulma zamanı |


---

## API Endpoints

### Notifications
| Method | Endpoint | Açıklama | Auth |
|---|---|---|---|
| `GET` | `/notifications/health` | Servisin çalışır durumda olduğunu kontrol eder. | Uygulama içinde auth kontrolü görünmüyor |
| `POST` | `/notifications` | Yeni bildirim kaydı oluşturur. | Uygulama içinde auth kontrolü görünmüyor |
| `GET` | `/notifications/{id}` | ID ile bir bildirim getirir. | Uygulama içinde auth kontrolü görünmüyor |
| `GET` | `/notifications` | Tüm bildirim kayıtlarını getirir. | Uygulama içinde auth kontrolü görünmüyor |
| `DELETE` | `/notifications/{id}` | ID ile bildirim siler. | Uygulama içinde auth kontrolü görünmüyor |
| `POST` | `/notifications/sendPlainText` | Plain text e-posta gönderir. | Uygulama içinde auth kontrolü görünmüyor |
| `POST` | `/notifications/sendHtml` | HTML e-posta gönderir. | Uygulama içinde auth kontrolü görünmüyor |

Bildirim oluşturma isteği için `NotificationRequest` alanları: `userId`, `type`, `channel`, `title`, `message`, `status`.
E-posta endpoint'leri `MailRequest` gövdesi bekler: `to`, `subject`, `body`.

OpenAPI arayüzü varsayılan olarak `/swagger-ui/index.html`, JSON tanımı ise `/v3/api-docs` adresindedir.

---

## Servisler Arası İletişim

### Feign Client (Senkron)

| İstemci | Uzak endpoint | Kullanım |
|---|---|---|
| `UserServiceClient` | `GET /user/internal/{id}` | Müşteri ve ürün sahibi bilgileri |
| `UserServiceClient` | `GET /user/internal/{id}/email` | Kullanıcı e-postası |
| `ProductServiceClient` | `GET /products/{id}` | Ürün detayları, fiyatı ve görselleri |

### Kafka Events (Asenkron)

- **Topic:** `order-topic`
- **Consumer group:** `notification-service`
- **Mesaj modeli:** `OrderEvent` (`orderId`, `status`, `userId`, `orderNumber`, `items`)
- **Akış:** Sipariş olayı alınır, kullanıcı/ürün detayları çekilir, ilgili e-postalar gönderilir ve her gönderim için bildirim kaydı oluşturulur.
- E-posta içeriği `order-confirmation.html` ve `info-to-owners.html` şablonlarından üretilir.
- Hata durumları loglanır; mevcut kodda DLT stratejisi bulunmamaktadır.

---

## Kurulum

### Gereksinimler

- JDK 21
- Maven 3.9+ veya projeyle birlikte gelen Maven Wrapper (`./mvnw`)
- PostgreSQL
- Kafka
- `user-service`, `product-service` ve yapılandırma sunucusu
- Yerel Docker çalıştırması için Docker ve `lendmate-net` ağı

### Çalıştırma

Maven Wrapper ile:

```bash
./mvnw spring-boot:run
```

Paket oluşturma:

```bash
./mvnw clean package
```

Docker Compose ile (dış servislerin `lendmate-net` ağı üzerinde çalıştığı varsayılır):

```bash
docker network create lendmate-net
docker compose -f docker-compose-local.yml up --build
```

Servis varsayılan olarak `http://localhost:8083` adresinde çalışır.

---

## Ortam Değişkenleri

| Değişken | Açıklama | Örnek |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Aktif Spring profili | `dev`, `stage`, `prod`, `test` |
| `CONFIG_SERVER_URL` | Spring Config Server adresi | `http://config-server:8888` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC bağlantı adresi | `jdbc:postgresql://postgres:5432/notification_service_db` |
| `SPRING_DATASOURCE_USERNAME` | Veritabanı kullanıcı adı | `lendmate` |
| `SPRING_DATASOURCE_PASSWORD` | Veritabanı parolası | `***` |
| `MAIL_USERNAME` | SMTP kullanıcı adı | `***` |
| `MAIL_PASSWORD` | SMTP parolası | `***` |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry collector adresi | `http://otel-collector:4318` |
| `OTEL_SERVICE_NAME` | Telemetri servis adı | `notification-service` |
| `JAVA_OPTS` | JVM başlatma seçenekleri | `-XX:MaxRAMPercentage=60.0` |

---

## Testler

Mevcut test, Spring uygulama context'inin `test` profiliyle yüklenebildiğini doğrular:

```bash
./mvnw test
```
