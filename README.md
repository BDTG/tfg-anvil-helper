# TFG Anvil Helper

Mod **client-only, mã nguồn mở** hiển thị chuỗi rèn ngắn nhất còn lại trong GUI anvil của TerraFirmaGreg. Mặc định người chơi tự bấm. Từ **1.1.0**, có thể nhấn **F8** để tự rèn **một phôi**, xong thì dừng; nhấn lại để hủy. Không sửa trực tiếp vật phẩm hoặc nhiệt độ; chế độ tự động gọi nút rèn gốc của TFC và vẫn chịu kiểm tra của server.

Mã nguồn được công khai tại [BDTG/tfg-anvil-helper](https://github.com/BDTG/tfg-anvil-helper) theo giấy phép **AGPL-3.0-only**. Bạn có thể tải bản build từ Releases hoặc tự build theo hướng dẫn bên dưới. Không có telemetry hoặc dịch vụ mạng bên ngoài.

## Phiên bản tương thích

| Helper | TerraFirmaGreg Modern | Core | TFC tích hợp | Minecraft / Forge |
|---|---|---|---|---|
| **1.1.3 — stable, bản hiện tại** | **0.13.17** | **0.9.31** | **3.2.28** | 1.20.1 / 47.4.13 |
| [1.1.0–1.1.2](https://github.com/BDTG/tfg-anvil-helper/releases/tag/v1.1.2) | 0.13.16 | 0.9.30 | 3.2.28 | 1.20.1 / 47.4.13 |
| **1.0.2 — ổn định, rèn tay** | **0.13.16** | **0.9.30** | **3.2.28** | 1.20.1 / 47.4.13 |
| [1.0.1](https://github.com/BDTG/tfg-anvil-helper/releases/tag/v1.0.1) | 0.13.15 | 0.9.29 | 3.2.25 | 1.20.1 / 47.4.13 |

**Bản mới nhất (stable):** [v1.1.3](https://github.com/BDTG/tfg-anvil-helper/releases/tag/v1.1.3). **Cho pack cũ 0.13.16:** [v1.1.2](https://github.com/BDTG/tfg-anvil-helper/releases/tag/v1.1.2).

Mỗi bản helper khóa đúng phiên bản TFC đã được kiểm tra. Không dùng helper 1.0.1 trên TFG 0.13.16; không tự nới range để bỏ qua lỗi dependency. Không dùng lại helper 1.0.0 vì thiếu resource-pack metadata.

## Cài và dùng

1. Tắt Minecraft; xóa JAR helper cũ nếu có để tránh trùng mod ID.
2. Chép `tfg-anvil-helper-1.1.3-mc1.20.1.jar` từ Releases vào `minecraft/mods/` của instance **TerraFirmaGreg-Modern 0.13.17** trong PrismLauncher.
3. Mở game, mở anvil, đặt phôi và chọn công thức.
4. Đọc chuỗi thao tác **trái sang phải, trên xuống dưới**. Bước tiếp theo có **viền kép hồng tím đậm / đen**; nút tương ứng trên anvil cũng được đánh dấu. Viền không che biểu tượng hoặc tràn sang nút bên cạnh. Dòng **F8: tự rèn 1 phôi / hủy | H: ẩn / hiện** luôn nằm trên đầu bảng khi overlay hiện, kể cả sau khi rèn xong.
5. **Tự bấm** nút rèn. Mod đọc lại trạng thái được TFC đồng bộ và tính lại khi work, target, luật hoặc lịch sử thay đổi. Bấm nhầm vẫn được tính lại từ trạng thái mới.
6. **Tùy chọn F8:** bắt đầu tự rèn phôi hiện tại theo công thức đã chọn, hoặc hủy lượt đang chạy. Khi đang tự rèn, nút đỏ **DỪNG AUTO / STOP AUTO** hiện bên phải dòng hướng dẫn; bấm chuột trái để dừng. Khi hoàn thành hoặc đã hủy, auto đã tắt và nút biến mất. Không tự chọn công thức, lấy sản phẩm hay bắt đầu phôi kế tiếp.
7. Di chuột lên biểu tượng để xem tên bước và mức dịch chuyển. Nhấn **H** trong GUI anvil để ẩn/hiện (chỉ lưu trong phiên game); đồng thời dừng tự rèn nếu đang chạy.

Có `en_us` và `vi_vn`, theo ngôn ngữ game; ngôn ngữ khác dùng fallback tiếng Anh. Vẫn cần **búa, anvil đúng tier, phôi đủ nóng và công thức hợp lệ**. Không cần helper trên server; hãy tuân thủ quy định server về mod trợ giúp.

Sau khi rèn xong, helper có thể trở về thông báo chọn công thức: sản phẩm không còn mang công thức đang rèn. Đây không phải lỗi.

### An toàn của tự rèn

- Tắt mặc định; mỗi phôi cần một lần nhấn F8 mới. Giữ phím không tạo nhiều lượt.
- Chỉ gửi một thao tác đang chờ xác nhận; đối chiếu work, tổng bước và lịch sử trước khi gửi bước tiếp theo. Khoảng cách tối thiểu 4 client tick, timeout 60 client tick; không tự gửi lại thao tác.
- Nút DỪNG AUTO chỉ hủy, không khởi động lại hay xóa khóa bước chưa xác nhận. Click được chặn trước GUI native để không bấm xuyên xuống inventory.
- Hủy khi nhấn F8/H, bấm chuột hoặc Enter/Space kích hoạt nút native, đóng GUI, đổi world/phôi/công thức, thiếu điều kiện rèn hoặc trạng thái không khớp.
- Sau khi hủy/timeout mà còn bước chưa được xác nhận, **đóng rồi mở lại anvil** trước khi bắt đầu lượt mới. Nhấn F8 liên tiếp không bỏ qua khóa này; mở menu mới từ server hoặc đổi kết nối mới giải phóng khóa.
- Hủy chỉ ngừng gửi bước mới: thao tác đã gửi có thể vẫn được server xử lý. Không dùng chung một anvil với người khác đang rèn đồng thời.
- F8 hiện là phím cố định trong GUI anvil. Chỉ bật tự rèn trên server cho phép tính năng này.

## Thuật toán và giới hạn

- Ý tưởng sử dụng tham khảo [iagocq/tfc-anvil](https://iagocq.github.io/tfc-anvil/).
- Bộ giải Java dùng BFS trên `(work, ba loại thao tác cuối)`; mỗi cạnh là một lần bấm. Mọi vị trí trung gian phải trong `[0, 150]`.
- Đích phải **chính xác**, đồng thời khớp mọi luật `LAST`, `SECOND_LAST`, `THIRD_LAST`, `NOT_LAST`, `ANY`. Ba mức Hit khác nhau về dịch chuyển nhưng cùng đáp ứng luật Hit.
- Tối thiểu số bước **còn lại từ trạng thái hiện tại**. Cache snapshot tránh chạy lại bộ giải trên mọi frame.
- Không cam kết Perfectly Forged cho phôi đã bị rèn thừa: bộ đếm tổng bước của TFC không bị sửa. Khi server có độ trễ, hãy đợi marker cập nhật trước khi bấm tiếp.
- Tự rèn từ chối history có null ở giữa do automation/remote tùy biến; không đoán lại vị trí lịch sử.
- Lỗi tích hợp được ghi trong `minecraft/logs/latest.log`; trợ lý ngừng hoạt động an toàn thay vì sửa dữ liệu game.

## Build offline trên Linux với PrismLauncher

Cần **Python 3**, **JDK 17** và instance **TerraFirmaGreg-Modern 0.13.17** đã cài đủ dependency. Không phân phối kèm JAR Minecraft/Forge/TFC.

```bash
git clone https://github.com/BDTG/tfg-anvil-helper.git
cd tfg-anvil-helper
python3 build.py
```

Script dùng JDK 17 trong `PrismLauncher/java/java-runtime-gamma`, dependency theo metadata **Minecraft 1.20.1 + Forge 47.4.13**, và `TerraFirmaGreg-Core-Modern-0.9.31.jar` đã cài. Có thể đổi vị trí bằng `PRISM_ROOT`, `TFG_INSTANCE`, `JAVA_HOME`. Build không cần Gradle, tải SDK hoặc quyền root.

Nguồn dùng tên runtime **SRG 1.20.1**, biên dịch trực tiếp ra production JAR, không phải development JAR còn cần remap. Muốn build bản cũ, checkout tag tương ứng và dùng đúng instance cũ theo bảng tương thích.

## Kiểm chứng

`python3 build.py` biên dịch Java 17 và chạy:

- Metadata Forge, subscriber chỉ chạy CLIENT. Advisor không ghi gameplay; tự rèn chỉ gọi callback của `AnvilStepButton` gốc, không tự tạo packet hoặc ghi inventory.
- **AutoForgeControllerTest:** một phôi/lượt, một thao tác chờ ack, giới hạn tốc độ, hủy, thay phôi, mất điều kiện, desync, timeout và ack đến muộn.
- **AutoInputIntegrationTest:** Enter/keypad Enter/Space hủy bằng predicate native; hủy/timeout giữ khóa bước chưa xác nhận, chỉ tái kích hoạt sau menu/kết nối mới; giữ giới hạn tốc độ qua các lượt.
- **AutoForgeReplayTest:** 278 recipe cài thực tế / 31 bộ luật; **366 lượt mô phỏng, 3.384 yêu cầu bước**, với xác nhận trễ 1/7/29 tick. Đây là replay trong bộ test, **không phải 366 lượt rèn trong game**.
- Hook tích hợp và 10.000 idle tick: không tự kích hoạt và không truy cập Minecraft singleton khi không chạy.
- **ModCompatibilityTest:** đọc TOML thật, dùng Maven VersionRange kiểm tra dependency nhận đúng **TFC 3.2.28** đang được nạp, từ chối phiên bản chưa xác minh.
- Cache snapshot, tính lại khi trạng thái thay đổi; đường ngắn nhất và luật Hit-last.
- **StopControlTest:** hitbox đúng màn hình/khung đã vẽ, click trái gọi hủy, không click xuyên/khởi động lại, giữ quarantine; event chuột ưu tiên HIGHEST và nhãn Anh/Việt.
- **OverlayPresentationTest:** hướng dẫn F8/H có trong cả nhánh đang rèn và bảng không có recipe/đã xong; raster kiểm tra viền đen + hồng tím trên toàn bộ khung, vùng icon 16×16 nguyên vẹn, không vượt khung 20×20.
- **449.391 assertion** trong SolverValidationTest: đối chiếu `ForgeRule.matches()` từ **Core 0.9.31 / TFC 3.2.28** thật; 343 histories × 30 luật, các mức Hit, cặp luật, toàn bộ bộ luật của 278 recipe anvil trong JAR, biên, lịch sử, input bất biến.
- Oracle độc lập kiểm tra shortest/replay cho mọi target 0–150 của bốn recipe mẫu (905 solve calls trong toàn bộ suite validation).
- Parser Minecraft đọc `pack.mcmeta`. Sau khi đóng gói, native ResourcePackInfo probe mở **JAR thành phẩm**, kiểm tra format 15 và cả hai asset ngôn ngữ.

Kết quả 1.1.3 trong [`verification/build-1.1.3.log`](verification/build-1.1.3.log) và [`verification/compatibility-1.1.3.md`](verification/compatibility-1.1.3.md). Kết quả các bản trước được giữ riêng. Kết quả 1.0.2 vẫn được giữ riêng trong `verification/`. Đây là kết quả chạy local, **không phải GitHub Actions/CI**.

### Lịch sử kiểm chứng — không trộn phiên bản

- **Helper 1.0.1 / TFG 0.13.15:** đã nạp tới menu chính, trang Mods hiển thị `State: done`; ảnh trong `verification/`. Video người dùng cung cấp sau đó cho thấy ba sản phẩm — Steel Screwdriver Tip, Steel Axe Head, Steel Prospector's Pick Head — đều có tooltip **Perfectly Forged!**. Video gốc không được đưa lên repo. Video không xác nhận phím H hay hành vi khi server lag.
- **Helper 1.0.2 / TFG 0.13.16:** đã build và chạy lại bộ test với JAR mới. **Chưa xác nhận một ca rèn hoàn chỉnh trong world của bản modpack mới**; kết quả gameplay trên bản cũ không được dùng thay cho kiểm chứng bản mới.

### Changelog

- **1.1.3:** cập nhật target sang TFG 0.13.17 / Core 0.9.31; TFC vẫn 3.2.28, MC/Forge không đổi. Giữ nguyên bộ giải, F8 tự rèn, nút DỪNG AUTO và viền hồng tím.
- **1.1.2:** thêm nút đỏ DỪNG AUTO / STOP AUTO có thể click khi đang tự rèn; không còn chỉ là dòng hướng dẫn phím. Hủy qua cùng bộ điều khiển, giữ khóa bước đang chờ và chặn click xuyên. Không thay solver, adapter rèn hay cơ chế F8.
- **1.1.1:** hướng dẫn F8/H cố định trên đầu mọi bảng overlay thông thường; trạng thái không lặp lại phím tắt; đổi viền bước kế tiếp và nút native sang khung kép hồng tím `#FF2DA6` / đen, giữ nguyên vùng icon. Không thay solver hoặc bộ điều khiển tự rèn.
- **1.1.0:** thêm F8 tự rèn một phôi/lượt, xác nhận từng bước, hủy an toàn và trạng thái trên overlay; mặc định vẫn rèn tay. Đã chạy test và nạp mod trong game; chưa xác minh độc lập toàn bộ thao tác F8 → tooltip chất lượng thành phẩm.
- **1.0.2:** cập nhật target sang TFG 0.13.16 / Core 0.9.30 / TFC 3.2.28; thêm regression dependency native. Không thay thuật toán hay cách bấm rèn.
- **1.0.1:** bổ sung `pack.mcmeta` còn thiếu ở 1.0.0. Native probe tái hiện lỗi `failed to load a valid ResourcePackInfo` trên JAR cũ và chấp nhận JAR vá; xem `verification/probe-old.log` và `probe-fixed.log`.

## Gỡ

Xóa đúng JAR `tfg-anvil-helper-*.jar` khỏi `minecraft/mods/`. Helper không tạo dữ liệu world hoặc config riêng. Không xóa các mod khác.

## License

Dự án được phát hành theo **GNU Affero General Public License v3.0 (AGPL-3.0-only)**; xem [LICENSE](LICENSE) và [NOTICE](NOTICE) để biết giấy phép và ghi nhận nguồn tham khảo. Khi sửa đổi hoặc phân phối lại, hãy giữ các thông báo bản quyền và tuân thủ điều kiện giấy phép. Minecraft, Forge, TerraFirmaCraft và TerraFirmaGreg không được phân phối kèm.

## Báo lỗi và đóng góp

Bạn có thể [tạo issue](https://github.com/BDTG/tfg-anvil-helper/issues) hoặc gửi pull request. Khi báo lỗi, ghi rõ phiên bản helper, modpack, Minecraft/Forge, các bước tái hiện và phần log liên quan. Hãy loại bỏ token, địa chỉ server riêng tư và thông tin tài khoản trước khi chia sẻ log.
