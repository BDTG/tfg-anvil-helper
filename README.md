# TFG Anvil Helper — bản dùng riêng

Mod **client-only**, dành cho đúng cấu hình đang cài trên máy:

| Thành phần | Phiên bản |
|---|---|
| TerraFirmaGreg Modern | 0.13.15 |
| Minecraft | 1.20.1 |
| Forge | 47.4.13 |
| TerraFirmaGreg Core | 0.9.29 |
| TerraFirmaCraft tích hợp trong Core | 3.2.25 |
| Java | 17 |

Repository GitHub dùng riêng: [BDTG/tfg-anvil-helper](https://github.com/BDTG/tfg-anvil-helper) (**private**). Không có telemetry hoặc dịch vụ mạng.

**Tải bản đã build:** [Releases](https://github.com/BDTG/tfg-anvil-helper/releases/latest). Không dùng lại bản 1.0.0 vì thiếu resource-pack metadata.

## Cách dùng

1. Tắt Minecraft nếu đang chạy.
2. Tải `tfg-anvil-helper-1.0.1-mc1.20.1.jar` từ [Releases](https://github.com/BDTG/tfg-anvil-helper/releases/latest), hoặc lấy từ `dist/` sau khi build; chép vào thư mục `minecraft/mods/` của instance TerraFirmaGreg-Modern trong PrismLauncher.
3. Mở game, mở anvil, đặt nguyên liệu và chọn công thức như bình thường.
4. Bảng trợ lý hiện chuỗi thao tác **từ trái sang phải, trên xuống dưới**. Bước tiếp theo có viền vàng; nút tương ứng trên anvil cũng được đánh dấu.
5. **Bạn tự bấm** nút rèn. Mod đọc trạng thái được TFC đồng bộ và tính lại khi vị trí, mục tiêu hoặc lịch sử thay đổi. Bấm nhầm vẫn được tính lại từ trạng thái mới.
6. Di chuột lên biểu tượng để xem tên bước và mức dịch chuyển. Nhấn **H** trong GUI anvil để ẩn/hiện trợ lý (chỉ lưu trong phiên game).

Ngôn ngữ theo game: có `en_us` và `vi_vn`, mặc định tiếng Anh nếu ngôn ngữ khác.

**Vẫn cần búa, anvil đúng tier, nguyên liệu đủ nóng và công thức hợp lệ.** Mod không sửa nhiệt độ, vật phẩm, quyền server hay hiệu ứng rèn.

## Thuật toán và giới hạn

- Ý tưởng sử dụng lấy từ https://iagocq.github.io/tfc-anvil/ (iagocq/tfc-anvil).
- Bộ giải Java mới dùng BFS trên trạng thái `(work, ba loại thao tác cuối)`; mỗi cạnh là một lần bấm, mọi vị trí trung gian phải thuộc `[0, 150]`.
- Đích phải **chính xác** và khớp mọi luật `LAST`, `SECOND_LAST`, `THIRD_LAST`, `NOT_LAST`, `ANY`. Ba mức Hit được phân biệt về dịch chuyển nhưng cùng đáp ứng luật Hit của TFC.
- Tối thiểu số bước **còn lại từ trạng thái hiện tại**, không chỉ một chuỗi gần tối ưu. Không chạy lại bộ giải ở mỗi frame nếu dữ liệu không đổi.
- Không cam kết Perfectly Forged cho phôi đã bị rèn thừa trước đó: bộ đếm tổng số lần rèn của TFC vẫn giữ nguyên. Độ trễ server cũng có thể khiến thao tác nhanh đọc trạng thái cũ; hãy đợi marker cập nhật.
- GUI chỉ đọc, không gửi packet rèn, không gọi bấm nút hay sửa inventory. Không cần cài helper lên server; vẫn tôn trọng quy định server về mod trợ giúp.
- Bản này nhắm phiên bản cụ thể trên; không phải mod tương thích mọi phiên bản TFG/TFC. Metadata khóa TFC 3.2.25.
- Lỗi tích hợp được ghi trong `minecraft/logs/latest.log`; trợ lý tự ngừng thay vì sửa dữ liệu game.

## Build offline trên Linux với PrismLauncher

Cần **Python 3**, **JDK 17** và instance **TerraFirmaGreg-Modern 0.13.15** đã cài đủ dependency. Repository không phân phối các JAR Minecraft/Forge/TFC.

```bash
gh repo clone BDTG/tfg-anvil-helper
cd tfg-anvil-helper
python3 build.py
```

Script dùng JDK 17 có sẵn trong `PrismLauncher/java/java-runtime-gamma`, các dependency trong metadata của **Minecraft 1.20.1 + Forge 47.4.13** và Core 0.9.29 đang cài. Không cần Gradle, tải SDK hay quyền root. Nguồn dùng tên runtime SRG của 1.20.1 và biên dịch trực tiếp sang JAR production, không phải development JAR cần remap.

Có thể đổi đường dẫn bằng `PRISM_ROOT`, `TFG_INSTANCE`, `JAVA_HOME`. Không phân phối kèm JAR Minecraft/TFC/Forge.

## Kiểm chứng

`python3 build.py` biên dịch Java 17, chạy toàn bộ test, chỉ đóng gói khi test thành công:

- Metadata Forge, subscriber chỉ chạy CLIENT; kiểm tra bytecode không có lời gọi packet/forge tự động.
- Bộ nhớ đệm snapshot và tính lại khi trạng thái thay đổi.
- Đường ngắn nhất, luật Hit-last.
- Đối chiếu **343 histories × 30 luật** với `ForgeRule.matches()` thật trong Core cài sẵn.
- Kiểm tra cả ba mức Hit trên 729 tổ hợp, mọi cặp luật, mọi bộ luật từ 278 recipe anvil trong JAR.
- Oracle độc lập kiểm tra đường ngắn nhất và replay cho toàn bộ 151 target trên bốn recipe mẫu.
- Biên 0/150, đường vòng, lịch sử cũ, luật mâu thuẫn, tên luật sai và input bất biến.

Log trong `verification/build-1.0.1.log`. Đây là test thuật toán/API/metadata trên JAR thật, **không thay thế việc rèn trực tiếp trong GUI game**. Chưa xác nhận một ca rèn hoàn chỉnh trong Minecraft hoặc trên server của bạn.

### Bản vá 1.0.1 — resource-pack metadata

Bản 1.0.0 thiếu `pack.mcmeta`, khiến Forge báo `failed to load a valid ResourcePackInfo` và không nạp được assets của helper. Không dùng lại JAR 1.0.0.

- Thêm `pack.mcmeta` với `pack_format=15` (Minecraft 1.20.1).
- Regression `ResourcePackTest` kiểm tra bằng parser Minecraft thật.
- `ResourcePackProbe` mở **JAR thành phẩm** bằng `Pack.readPackInfo`/`FilePackResources`, yêu cầu metadata hợp lệ và đọc được cả hai file ngôn ngữ. Probe được chạy tự động sau mỗi lần đóng gói.
- Đã tái hiện native probe thất bại trên JAR cũ, thành công trên JAR vá; xem `verification/probe-old.log` và `probe-fixed.log`.
- Đã mở lại instance thật, xác nhận tới menu chính không có màn lỗi nạp helper, và trang Mods hiện **version 1.0.1, State: done**. Ảnh trong `verification/main-menu-1.0.1.png` và `mod-loaded-1.0.1.png`.
- Log vẫn có lỗi audit `dev/kosmx/playerAnim/api/layered/IAnimation` từ ModernFix/nhánh animation; lỗi đó không ngăn lần kiểm tra tới menu chính. Không thay đổi các mod animation.
- Chưa kiểm chứng rèn hoàn chỉnh trong world.

## Gỡ

Xóa đúng `tfg-anvil-helper-1.0.1-mc1.20.1.jar` khỏi `minecraft/mods/`. Helper không tạo dữ liệu world hoặc config riêng.

## License

AGPL-3.0-only, xem `LICENSE` và `NOTICE`. “Private” nghĩa là không công khai lên mạng; không có cơ chế DRM hoặc ẩn mod khỏi danh sách mod. Giữ các thông báo bản quyền và tuân thủ license nếu chia sẻ/phân phối lại.
