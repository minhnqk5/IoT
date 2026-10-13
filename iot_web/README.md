# IoT Monitoring (bản dễ đọc hơn)

## Công nghệ
- Java 17, Spring Boot, Spring Data JPA
- MySQL lưu dữ liệu
- MQTT giao tiếp với ESP8266
- HTML, CSS, JavaScript và Chart.js cho giao diện

## Chạy project
1. Mở thư mục chứa `pom.xml` bằng IntelliJ IDEA.
2. Kiểm tra thông tin MySQL và MQTT trong `src/main/resources/application.properties`.
3. Chạy `IotMonitoringApplication.java`.
4. Mở `http://localhost:8080`.

## Thứ tự đọc code đề xuất
1. `src/main/resources/static/index.html`: cấu trúc giao diện.
2. `src/main/resources/static/style.css`: cách trình bày giao diện.
3. `src/main/resources/static/app.js`: đăng nhập, chuyển trang, gọi API, biểu đồ, điều khiển LED, bảng dữ liệu và lịch sử.
4. `controller/`: nhận yêu cầu HTTP từ giao diện.
5. `service/MqttService.java`: kết nối MQTT, nhận dữ liệu cảm biến và phản hồi thiết bị.
6. `repository/`: truy vấn MySQL.
7. `entity/`: ánh xạ bảng dữ liệu.
8. `dto/`: cấu trúc dữ liệu trả về hoặc nhận từ API.

## Các thay đổi để dễ học và sửa

### Frontend: `static/app.js`
- `renderTable(bodyId, rows, columns)`: dùng chung để hiển thị bảng cảm biến và lịch sử.
- `renderPagination(prefix, result)`: dùng chung để cập nhật số trang và trạng thái nút trước/sau.
- `esc(value)`: escape ký tự HTML trước khi hiển thị giá trị lấy từ API.
- Giữ nguyên tên ID HTML, endpoint API, bộ lọc, phân trang, biểu đồ, đăng nhập và điều khiển LED.

### Backend
- Thêm `util/RequestFilters.java` để gom các logic dùng chung:
  - `blank(...)`: chuyển chuỗi trống thành `null`.
  - `pageable(...)`: chuẩn hóa số trang, kích thước trang và thứ tự sắp xếp.
  - `timeRange(...)`: phân tích bộ lọc thời gian.
- `DataSensorController` và `DeviceController` dùng chung các hàm trên, tránh duy trì hai bản logic giống nhau.

## Lưu ý
- Không thay đổi tên API hoặc cấu trúc JSON trả về trong bản chỉnh sửa này.
- Cần chạy thử với MySQL và MQTT thực tế sau khi giải nén. JavaScript đã được kiểm tra cú pháp; môi trường tạo bản này không có Maven nên chưa xác nhận được build backend.
