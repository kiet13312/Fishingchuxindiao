# Fishingchuxindiao

Một game câu cá Android Java chơi đơn, lấy cảm hứng từ vòng lặp gameplay được mô tả công khai của Fish It / Câu Cá Vạn Cân trên Builda.

## Có trong bản 2.0
- Ba câu thủ cùng tham gia một lần câu: Sở Tâm, Bá Thường, Lão Ngô.
- Vòng lặp thật: THẢ LƯỚI -> chờ cá cắn -> giữ CO DÂY -> nhả để dây hồi.
- Quản lý độ căng và độ dài dây; căng quá hoặc cá kéo quá xa sẽ thất bại.
- Ba kỹ năng: Xe Kéo, Phi Thiên Vô Cực, Hộ Lực; có hồi chiêu và hiệu ứng.
- Sáu vùng câu, nhiều loài cá theo vùng, cân nặng và giá trị khác nhau.
- Cần câu, mồi câu, nâng cấp nhân vật, nâng cấp kỹ năng.
- Kho cá, bán toàn bộ, giới hạn sức chứa.
- Nhiệm vụ, XP, level, phần thưởng.
- Lưu tiến trình bằng SharedPreferences, tự lưu khi thay đổi và khi Activity tạm dừng.
- Vẽ bằng Android Canvas, không dùng thư viện runtime bên thứ ba, phù hợp mục tiêu máy Android cấu hình thấp.
- Đồ họa được tự vẽ bằng code, không đóng gói mã nguồn hay asset trò chơi tham khảo.

## Build
GitHub Actions dùng JDK 17 và lệnh gradle assembleDebug. APK debug được upload thành artifact sau mỗi lần push vào main.
