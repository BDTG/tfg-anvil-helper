# TFG Anvil Helper

Mod **client-only, mã nguồn mở** hiển thị chuỗi rèn ngắn nhất còn lại trong GUI anvil của TerraFirmaGreg. Người chơi tự bấm; không tự động rèn, sửa vật phẩm, nhiệt độ hay gửi packet thao tác.

Mã nguồn được công khai tại [BDTG/tfg-anvil-helper](https://github.com/BDTG/tfg-anvil-helper) theo giấy phép **AGPL-3.0-only**. Bạn có thể tải bản build từ Releases hoặc tự build theo hướng dẫn bên dưới. Không có telemetry hoặc dịch vụ mạng.

## Phiên bản tương thích

| Helper | TerraFirmaGreg Modern | Core | TFC tích hợp | Minecraft / Forge |
|---|---|---|---|---|
| **1.0.2 — hiện tại** | **0.13.16** | **0.9.30** | **3.2.28** | 1.20.1 / 47.4.13 |
| [1.0.1](https://github.com/BDTG/tfg-anvil-helper/releases/tag/v1.0.1) | 0.13.15 | 0.9.29 | 3.2.25 | 1.20.1 / 47.4.13 |

**Tải bản hiện tại:** [Releases](https://github.com/BDTG/tfg-anvil-helper/releases/latest).

Mỗi bản helper khóa đúng phiên bản TFC đã được kiểm tra. Không dùng helper 1.0.1 trên TFG 0.13.16; không tự nới range để bỏ qua lỗi dependency. Không dùng lại helper 1.0.0 vì thiếu resource-pack metadata.

## Cài và dùng

1. Tắt Minecraft; xóa JAR helper cũ nếu có để tránh trùng mod ID.
2. Chép `tfg-anvil-helper-1.0.2-mc1.20.1.jar` từ Releases vào `minecraft/mods/` của instance **TerraFirmaGreg-Modern 0.13.16** trong PrismLauncher.
3. Mở game, mở anvil, đặt phôi và chọn công thức.
4. Đọc chuỗi thao tác **trái sang phải, trên xuống dưới**. Bước tiếp theo có viền vàng; nút tương ứng trên anvil cũng được đánh dấu.
5. **Tự bấm** nút rèn. Mod đọc lại trạng thái được TFC đồng bộ và tính lại khi work, target, luật hoặc lịch sử thay đổi. Bấm nhầm vẫn được tính lại từ trạng thái mới.
6. Di chuột lên biểu tượng để xem tên bước và mức dịch chuyển. Nhấn **H** trong GUI anvil để ẩn/hiện (chỉ lưu trong phiên game).

Có `en_us` và `vi_vn`, theo ngôn ngữ game; ngôn ngữ khác dùng fallback tiếng Anh. Vẫn cần **búa, anvil đúng tier, phôi đủ nóng và công thức hợp lệ**. Không cần helper trên server; hãy tuân thủ quy định server về mod trợ giúp.

Sau khi rèn xong, helper có thể trở về thông báo chọn công thức: sản phẩm không còn mang công thức đang rèn. Đây không phải lỗi.

## Thuật toán và giới hạn

- Ý tưởng sử dụng tham khảo [iagocq/tfc-anvil](https://iagocq.github.io/tfc-anvil/).
- Bộ giải Java dùng BFS trên `(work, ba loại thao tác cuối)`; mỗi cạnh là một lần bấm. Mọi vị trí trung gian phải trong `[0, 150]`.
- Đích phải **chính xác**, đồng thời khớp mọi luật `LAST`, `SECOND_LAST`, `THIRD_LAST`, `NOT_LAST`, `ANY`. Ba mức Hit khác nhau về dịch chuyển nhưng cùng đáp ứng luật Hit.
- Tối thiểu số bước **còn lại từ trạng thái hiện tại**. Cache snapshot tránh chạy lại bộ giải trên mọi frame.
- Không cam kết Perfectly Forged cho phôi đã bị rèn thừa: bộ đếm tổng bước của TFC không bị sửa. Khi server có độ trễ, hãy đợi marker cập nhật trước khi bấm tiếp.
- Adapter hiện nhắm rèn tay thông thường. Chưa hỗ trợ đầy đủ history có null ở giữa do automation/remote tùy biến.
- Lỗi tích hợp được ghi trong `minecraft/logs/latest.log`; trợ lý ngừng hoạt động an toàn thay vì sửa dữ liệu game.

## Build offline trên Linux với PrismLauncher

Cần **Python 3**, **JDK 17** và instance **TerraFirmaGreg-Modern 0.13.16** đã cài đủ dependency. Không phân phối kèm JAR Minecraft/Forge/TFC.

```bash
git clone https://github.com/BDTG/tfg-anvil-helper.git
cd tfg-anvil-helper
python3 build.py
```

Script dùng JDK 17 trong `PrismLauncher/java/java-runtime-gamma`, dependency theo metadata **Minecraft 1.20.1 + Forge 47.4.13**, và `TerraFirmaGreg-Core-Modern-0.9.30.jar` đã cài. Có thể đổi vị trí bằng `PRISM_ROOT`, `TFG_INSTANCE`, `JAVA_HOME`. Build không cần Gradle, tải SDK hoặc quyền root.

Nguồn dùng tên runtime **SRG 1.20.1**, biên dịch trực tiếp ra production JAR, không phải development JAR còn cần remap. Muốn build bản cũ, checkout tag tương ứng và dùng đúng instance cũ theo bảng tương thích.

## Kiểm chứng

`python3 build.py` biên dịch Java 17 và chạy:

- Metadata Forge, subscriber chỉ chạy CLIENT; bytecode không gọi packet rèn hoặc ghi gameplay.
- **ModCompatibilityTest:** đọc TOML thật, dùng Maven VersionRange kiểm tra dependency nhận đúng **TFC 3.2.28** đang được nạp, từ chối phiên bản chưa xác minh.
- Cache snapshot, tính lại khi trạng thái thay đổi; đường ngắn nhất và luật Hit-last.
- **449.391 assertion** trong SolverValidationTest: đối chiếu `ForgeRule.matches()` từ **Core 0.9.30 / TFC 3.2.28** thật; 343 histories × 30 luật, các mức Hit, cặp luật, toàn bộ bộ luật của 278 recipe anvil trong JAR, biên, lịch sử, input bất biến.
- Oracle độc lập kiểm tra shortest/replay cho mọi target 0–150 của bốn recipe mẫu (905 solve calls trong toàn bộ suite validation).
- Parser Minecraft đọc `pack.mcmeta`. Sau khi đóng gói, native ResourcePackInfo probe mở **JAR thành phẩm**, kiểm tra format 15 và cả hai asset ngôn ngữ.

Kết quả hiện tại trong [`verification/build-1.0.2.log`](verification/build-1.0.2.log); phạm vi tương thích và kiểm chứng runtime trong [`verification/compatibility-1.0.2.md`](verification/compatibility-1.0.2.md). Đây là kết quả chạy local, **không phải GitHub Actions/CI**.

### Lịch sử kiểm chứng — không trộn phiên bản

- **Helper 1.0.1 / TFG 0.13.15:** đã nạp tới menu chính, trang Mods hiển thị `State: done`; ảnh trong `verification/`. Video người dùng cung cấp sau đó cho thấy ba sản phẩm — Steel Screwdriver Tip, Steel Axe Head, Steel Prospector's Pick Head — đều có tooltip **Perfectly Forged!**. Video gốc không được đưa lên repo. Video không xác nhận phím H hay hành vi khi server lag.
- **Helper 1.0.2 / TFG 0.13.16:** đã build và chạy lại bộ test với JAR mới. **Chưa xác nhận một ca rèn hoàn chỉnh trong world của bản modpack mới**; kết quả gameplay trên bản cũ không được dùng thay cho kiểm chứng bản mới.

### Changelog

- **1.0.2:** cập nhật target sang TFG 0.13.16 / Core 0.9.30 / TFC 3.2.28; thêm regression dependency native. Không thay thuật toán hay cách bấm rèn.
- **1.0.1:** bổ sung `pack.mcmeta` còn thiếu ở 1.0.0. Native probe tái hiện lỗi `failed to load a valid ResourcePackInfo` trên JAR cũ và chấp nhận JAR vá; xem `verification/probe-old.log` và `probe-fixed.log`.

## Gỡ

Xóa đúng JAR `tfg-anvil-helper-*.jar` khỏi `minecraft/mods/`. Helper không tạo dữ liệu world hoặc config riêng. Không xóa các mod khác.

## License

Dự án được phát hành theo **GNU Affero General Public License v3.0 (AGPL-3.0-only)**; xem [LICENSE](LICENSE) và [NOTICE](NOTICE) để biết giấy phép và ghi nhận nguồn tham khảo. Khi sửa đổi hoặc phân phối lại, hãy giữ các thông báo bản quyền và tuân thủ điều kiện giấy phép. Minecraft, Forge, TerraFirmaCraft và TerraFirmaGreg không được phân phối kèm.

## Báo lỗi và đóng góp

Bạn có thể [tạo issue](https://github.com/BDTG/tfg-anvil-helper/issues) hoặc gửi pull request. Khi báo lỗi, ghi rõ phiên bản helper, modpack, Minecraft/Forge, các bước tái hiện và phần log liên quan. Hãy loại bỏ token, địa chỉ server riêng tư và thông tin tài khoản trước khi chia sẻ log.
