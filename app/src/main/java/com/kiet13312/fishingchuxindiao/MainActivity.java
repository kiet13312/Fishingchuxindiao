package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(new FishingGame(this));
    }

    static class FishingGame extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path path = new Path();
        final Random random = new Random();

        static final int HOME = 0;
        static final int LEVELS = 1;
        static final int CHARACTERS = 2;
        static final int SHOP = 3;
        static final int SKILLS = 4;
        static final int GAME = 5;

        int page = HOME;
        int selectedLevel = 1;
        int selectedCharacter = 0;
        int equippedRod = 0;

        long money = 12540000L;
        int energy = 100;

        final String[] characterNames = {"Sở Tâm", "Bá Thường", "Lão Ngô"};
        final String[] skillNames = {"XE KÉO", "PHI THIÊN VÔ CỰC", "HỘ NGƯ"};
        final int[] characterPower = {180000, 220000, 195000};
        final int[] characterColor = {
                Color.rgb(72, 146, 88),
                Color.rgb(151, 68, 177),
                Color.rgb(70, 108, 166)
        };

        final String[] rodNames = {
                "Cần Tre", "Cần Sắt", "Cần Thép", "Cần Vàng", "Cần Thần"
        };
        final int[] rodPrice = {0, 200000, 800000, 3000000, 10000000};
        final int[] rodPower = {1, 2, 4, 7, 12};

        final String[] mapNames = {
                "Hồ Chứa", "Bến Câu", "Rừng Xanh",
                "Đầm Độc", "Biển Biến Đổi", "Hắc Hồ",
                "Ngũ Hồ Sơn Lôi", "Thôn Quái", "Quán Sau Nam Cường"
        };

        int fishLevel;
        String fishName;
        long fishHp;
        long fishMaxHp;
        int lineTension = 18;
        float fishX = 0.76f;
        float fishY = 0.51f;
        float fishVX = 0.0021f;
        float lineLength = 2.5f;

        long nextBite = 0;
        long lastPhysics = 0;
        long lastReel = 0;

        boolean fishing = false;
        boolean fishBiting = false;
        boolean finished = false;
        boolean reelPressed = false;

        long[] skillReadyAt = {0, 0, 0};
        int activeSkill = -1;
        long activeSkillEnd = 0;
        int skillTick = 0;

        String notice = "Sẵn sàng câu cá";

        FishingGame(Context c) {
            super(c);
            p.setTypeface(Typeface.DEFAULT_BOLD);
            setFocusable(true);
        }

        @Override
        protected void onDraw(Canvas c) {
            int w = getWidth();
            int h = getHeight();
            long now = System.currentTimeMillis();

            if (page == HOME) {
                drawLobby(c, w, h);
            } else if (page == LEVELS) {
                drawLobby(c, w, h);
                drawPanel(c, w, h, "CHỌN MÀN");
                drawLevels(c, w, h);
            } else if (page == CHARACTERS) {
                drawLobby(c, w, h);
                drawPanel(c, w, h, "CHỌN NHÂN VẬT");
                drawCharacters(c, w, h);
            } else if (page == SHOP) {
                drawLobby(c, w, h);
                drawPanel(c, w, h, "SHOP - CẦN CÂU");
                drawShop(c, w, h);
            } else if (page == SKILLS) {
                drawLobby(c, w, h);
                drawPanel(c, w, h, "MENU KỸ NĂNG");
                drawSkills(c, w, h);
            } else if (page == GAME) {
                drawFishingScene(c, w, h, now);
                updateGame(now);
            }

            postInvalidateDelayed(40);
        }

        void drawLobby(Canvas c, int w, int h) {
            p.setStyle(Paint.Style.FILL);

            // Sky / forest.
            p.setColor(Color.rgb(44, 88, 72));
            c.drawRect(0, 0, w, h * 0.38f, p);

            for (int i = 0; i < 22; i++) {
                p.setColor(i % 2 == 0
                        ? Color.rgb(65, 119, 73)
                        : Color.rgb(92, 142, 82));
                float x = (i * 67) % w;
                float y = h * 0.25f + (i % 5) * 13;
                c.drawCircle(x, y, 62 + (i % 3) * 14, p);
            }

            // Water.
            p.setColor(Color.rgb(68, 150, 173));
            c.drawRect(0, h * 0.36f, w, h * 0.78f, p);
            p.setColor(Color.argb(75, 240, 255, 255));
            for (int i = 0; i < 11; i++) {
                float y = h * 0.41f + i * 22;
                c.drawLine(0, y, w, y + (i % 2 == 0 ? 3 : -2), p);
            }

            // Beach.
            p.setColor(Color.rgb(232, 217, 183));
            c.drawRect(0, h * 0.76f, w, h, p);
            p.setColor(Color.rgb(192, 173, 138));
            for (int i = 0; i < 80; i++) {
                float x = (i * 83) % w;
                float y = h * 0.79f + (i * 31) % Math.max(1, (int) (h * 0.18f));
                c.drawCircle(x, y, 2 + i % 3, p);
            }

            // Top bar.
            p.setColor(Color.argb(215, 11, 15, 18));
            c.drawRoundRect(18, 14, w - 18, 78, 18, 18, p);
            p.setColor(Color.WHITE);
            p.setTextSize(18);
            c.drawText("CÂU CÁ VẠN CÂN", 35, 45, p);

            p.setColor(Color.rgb(255, 220, 40));
            p.setTextSize(16);
            c.drawText("Tiền: " + moneyText(), w * 0.53f, 42, p);

            p.setColor(Color.WHITE);
            p.setTextSize(13);
            c.drawText("Năng lượng: " + energy, w * 0.53f, 64, p);
            c.drawText("Cần: " + rodNames[equippedRod], w * 0.72f, 64, p);

            // Characters always together in the lobby.
            drawCharacter(c, w * .28f, h * .70f, 0, 1.0f, false);
            drawCharacter(c, w * .43f, h * .70f, 1, 1.0f, false);
            drawCharacter(c, w * .58f, h * .70f, 2, 1.0f, false);

            // Main lobby menu.
            float left = 22;
            float bw = 215;
            float bh = 55;
            button(c, left, h * .15f, left + bw, h * .15f + bh,
                    "BẮT ĐẦU", Color.rgb(48, 140, 79), 18);
            button(c, left, h * .15f + 67, left + bw, h * .15f + 67 + bh,
                    "CHỌN MÀN", Color.rgb(62, 109, 157), 15);
            button(c, left, h * .15f + 134, left + bw, h * .15f + 134 + bh,
                    "NHÂN VẬT", characterColor[0], 15);
            button(c, left, h * .15f + 201, left + bw, h * .15f + 201 + bh,
                    "SHOP / CẦN CÂU", Color.rgb(151, 112, 44), 14);
            button(c, left, h * .15f + 268, left + bw, h * .15f + 268 + bh,
                    "KỸ NĂNG", characterColor[1], 15);

            p.setColor(Color.argb(180, 10, 12, 15));
            c.drawRoundRect(w - 285, h * .16f, w - 24, h * .16f + 170, 18, 18, p);
            p.setColor(Color.WHITE);
            p.setTextSize(14);
            c.drawText("THÔNG TIN", w - 258, h * .16f + 30, p);
            p.setTextSize(12);
            c.drawText("Màn hiện tại: " + selectedLevel, w - 258, h * .16f + 58, p);
            c.drawText("Nhân vật chính: " + characterNames[selectedCharacter],
                    w - 258, h * .16f + 82, p);
            c.drawText("Kỹ năng: " + skillNames[selectedCharacter],
                    w - 258, h * .16f + 106, p);
            c.drawText("Cả 3 nhân vật sẽ thả câu",
                    w - 258, h * .16f + 132, p);
            c.drawText("đồng thời khi vào trận.",
                    w - 258, h * .16f + 150, p);
        }

        void drawPanel(Canvas c, int w, int h, String title) {
            p.setColor(Color.argb(244, 5, 8, 11));
            c.drawRect(0, 0, w, h, p);

            p.setColor(Color.WHITE);
            p.setTextSize(24);
            c.drawText("‹", 24, 45, p);
            p.setTextSize(20);
            c.drawText(title, 60, 45, p);

            p.setColor(Color.rgb(255, 220, 45));
            p.setTextSize(15);
            c.drawText("Tiền: " + moneyText(), w - 245, 42, p);
        }

        void drawLevels(Canvas c, int w, int h) {
            int cols = 3;
            float startX = 70;
            float startY = 85;
            float cardW = (w - 190) / 3f;
            float cardH = 115;

            for (int i = 0; i < 9; i++) {
                int row = i / cols;
                int col = i % cols;
                float x = startX + col * (cardW + 25);
                float y = startY + row * 130;
                boolean unlocked = i < 4;

                p.setColor(unlocked
                        ? Color.rgb(44, 105, 73)
                        : Color.rgb(48, 52, 58));
                c.drawRoundRect(x, y, x + cardW, y + cardH, 16, 16, p);

                p.setColor(Color.WHITE);
                p.setTextSize(16);
                c.drawText("MÀN " + (i + 1), x + 16, y + 27, p);

                p.setTextSize(13);
                c.drawText(mapNames[i], x + 16, y + 50, p);
                c.drawText(unlocked ? "Đã mở" : "Khóa", x + 16, y + 75, p);

                if (unlocked) {
                    p.setColor(Color.rgb(255, 218, 47));
                    p.setTextSize(11);
                    c.drawText("Cá đặc biệt • Lv " + (i + 1) * 10, x + 16, y + 98, p);
                } else {
                    p.setColor(Color.LTGRAY);
                    p.setTextSize(22);
                    c.drawText("LOCK", x + cardW - 68, y + 58, p);
                }
            }
        }

        void drawCharacters(Canvas c, int w, int h) {
            float cardW = (w - 90) / 3f;
            for (int i = 0; i < 3; i++) {
                float x = 20 + i * (cardW + 15);
                float y = 85;

                p.setColor(i == selectedCharacter
                        ? Color.rgb(255, 216, 48)
                        : Color.rgb(55, 60, 68));
                c.drawRoundRect(x, y, x + cardW, h - 36, 18, 18, p);

                drawCharacter(c, x + cardW / 2f, y + 105, i, 1.25f, false);

                p.setColor(i == selectedCharacter ? Color.BLACK : Color.WHITE);
                p.setTextSize(19);
                center(c, characterNames[i], x + cardW / 2f, y + 178);

                p.setTextSize(12);
                center(c, "Lực câu: " + formatNumber(characterPower[i]),
                        x + cardW / 2f, y + 203);

                center(c, "Kỹ năng", x + cardW / 2f, y + 228);
                p.setTextSize(14);
                center(c, skillNames[i], x + cardW / 2f, y + 251);

                p.setTextSize(11);
                center(c, i == 0
                                ? "Kéo cá về nhanh, giảm độ căng."
                                : i == 1
                                ? "Đánh liên kích, gây nhiều sát thương."
                                : "Ổn định dây và bảo vệ đồng đội.",
                        x + cardW / 2f, y + 278);
            }
        }

        void drawShop(Canvas c, int w, int h) {
            float x = 55;
            for (int i = 0; i < rodNames.length; i++) {
                float y = 80 + i * 73;
                p.setColor(i == equippedRod
                        ? Color.rgb(254, 217, 49)
                        : Color.rgb(43, 49, 57));
                c.drawRoundRect(x, y, w - 55, y + 58, 14, 14, p);

                p.setColor(i == equippedRod ? Color.BLACK : Color.WHITE);
                p.setTextSize(15);
                c.drawText(rodNames[i], x + 22, y + 25, p);

                p.setTextSize(11);
                c.drawText("Sức mạnh x" + rodPower[i], x + 22, y + 46, p);

                if (i == equippedRod) {
                    c.drawText("ĐANG DÙNG", w - 150, y + 34, p);
                } else {
                    c.drawText("Giá: " + rodPrice[i], w - 170, y + 34, p);
                }
            }
        }

        void drawSkills(Canvas c, int w, int h) {
            float cardW = (w - 90) / 3f;

            for (int i = 0; i < 3; i++) {
                float x = 20 + i * (cardW + 15);
                float y = 95;

                p.setColor(characterColor[i]);
                c.drawRoundRect(x, y, x + cardW, h - 55, 18, 18, p);

                p.setColor(Color.argb(55, 0, 0, 0));
                c.drawCircle(x + cardW / 2f, y + 75, 45, p);

                p.setColor(Color.WHITE);
                p.setTextSize(17);
                center(c, characterNames[i], x + cardW / 2f, y + 145);

                p.setTextSize(13);
                center(c, skillNames[i], x + cardW / 2f, y + 175);

                p.setTextSize(11);
                String desc = i == 0
                        ? "Xe kéo xuất hiện, kéo cá về và hạ căng dây."
                        : i == 1
                        ? "Đánh liên kích màu tím, gây sát thương rất lớn."
                        : "Ổn định dây trong thời gian ngắn, giảm rủi ro đứt dây.";
                center(c, desc, x + cardW / 2f, y + 207);
                center(c, "Hồi chiêu: " + (i == 1 ? "12s" : "8s"),
                        x + cardW / 2f, y + 232);
            }
        }

        void drawFishingScene(Canvas c, int w, int h, long now) {
            // Background close to the reference: lake + bank.
            p.setColor(Color.rgb(42, 82, 68));
            c.drawRect(0, 0, w, h * .34f, p);
            for (int i = 0; i < 24; i++) {
                p.setColor(i % 2 == 0
                        ? Color.rgb(66, 116, 76)
                        : Color.rgb(98, 145, 87));
                c.drawCircle((i * 61) % w, h * .24f - (i % 4) * 8,
                        55 + (i % 4) * 8, p);
            }

            p.setColor(Color.rgb(64, 154, 177));
            c.drawRect(0, h * .31f, w, h * .80f, p);

            p.setColor(Color.argb(65, 240, 255, 255));
            for (int i = 0; i < 18; i++) {
                float y = h * .36f + i * 15;
                c.drawLine(0, y, w, y + (float)Math.sin(i) * 5, p);
            }

            p.setColor(Color.rgb(232, 217, 183));
            c.drawRect(0, h * .72f, w, h, p);

            // Top player/fish bars.
            p.setColor(Color.argb(215, 10, 14, 17));
            c.drawRoundRect(18, 14, 320, 95, 14, 14, p);

            p.setColor(Color.WHITE);
            p.setTextSize(16);
            c.drawText("con ng", 36, 35, p);
            p.setTextSize(12);
            c.drawText("Lv " + (70 + selectedLevel * 3), 36, 56, p);
            c.drawText("Thể lực: " + energy + "/100", 36, 73, p);
            c.drawText("Màn " + selectedLevel + " - " + mapNames[selectedLevel - 1], 36, 89, p);

            // Money bar.
            p.setColor(Color.rgb(255, 220, 45));
            p.setTextSize(15);
            c.drawText("Tiền: " + moneyText(), w - 330, 30, p);
            p.setColor(Color.WHITE);
            p.setTextSize(11);
            c.drawText(rodNames[equippedRod] + "  •  x" + rodPower[equippedRod],
                    w - 330, 49, p);

            if (fishing || fishBiting) {
                p.setColor(Color.argb(220, 12, 15, 18));
                c.drawRoundRect(w * .36f, 15, w * .72f, 86, 15, 15, p);
                p.setColor(Color.RED);
                p.setTextSize(17);
                c.drawText(fishName == null ? "ĐANG CẮN..." : fishName,
                        w * .39f, 39, p);

                p.setColor(Color.DKGRAY);
                c.drawRoundRect(w * .39f, 53, w * .69f, 69, 8, 8, p);
                float hpRatio = fishMaxHp <= 0 ? 0 : Math.max(0, Math.min(1,
                        (float) fishHp / (float) fishMaxHp));
                p.setColor(Color.rgb(224, 54, 61));
                c.drawRoundRect(w * .39f, 53,
                        w * .39f + w * .30f * hpRatio, 69, 8, 8, p);

                p.setColor(Color.WHITE);
                p.setTextSize(10);
                c.drawText(formatNumber(fishHp) + "/" + formatNumber(fishMaxHp),
                        w * .50f, 65, p);
            }

            // Three characters. All three cast together.
            float[] xs = {w * .25f, w * .38f, w * .51f};
            for (int i = 0; i < 3; i++) {
                drawCharacter(c, xs[i], h * .71f, i, 1.05f, true);

                float sx = xs[i] + 52;
                float sy = h * .64f;
                float ex = w * (.66f + i * .035f);
                float ey = h * (.47f + i * .045f);

                path.reset();
                path.moveTo(sx, sy);
                path.cubicTo(
                        sx + 90, sy - 70 - i * 8,
                        ex - 180, ey - 30,
                        ex, ey
                );
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(i == selectedCharacter ? 4 : 3);
                p.setColor(Color.WHITE);
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);

                p.setColor(Color.WHITE);
                c.drawCircle(ex, ey, 5, p);
            }

            if (fishBiting) {
                drawFish(c, w * fishX, h * fishY, now);
            }

            // Right tension meter.
            float meterX = w - 245;
            p.setColor(Color.argb(175, 11, 14, 16));
            c.drawRoundRect(meterX - 38, h * .42f,
                    meterX + 42, h * .82f, 12, 12, p);

            p.setColor(Color.DKGRAY);
            c.drawRoundRect(meterX - 7, h * .45f,
                    meterX + 7, h * .73f, 7, 7, p);

            p.setColor(lineTension >= 80
                    ? Color.RED
                    : lineTension >= 55
                    ? Color.rgb(255, 150, 0)
                    : Color.rgb(127, 226, 43));
            float barTop = h * .73f - (h * .27f * lineTension / 100f);
            c.drawRoundRect(meterX - 7, barTop,
                    meterX + 7, h * .73f, 7, 7, p);

            p.setColor(Color.WHITE);
            p.setTextSize(11);
            c.drawText("Trang bị", meterX - 31, h * .39f, p);
            c.drawText("Chiều dài cáp câu", meterX - 83, h * .79f, p);
            c.drawText(String.format(java.util.Locale.US, "%.1fm", lineLength),
                    meterX - 20, h * .82f, p);

            // Bottom skill menu.
            drawGameSkills(c, w, h, now);

            // Reel button.
            p.setColor(Color.argb(210, 12, 15, 18));
            c.drawCircle(w - 91, h - 76, 62, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.WHITE);
            c.drawCircle(w - 91, h - 76, 55, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            p.setTextSize(15);
            center(c, "CO LẠI DÂY", w - 91, h - 71);

            button(c, w - 235, h - 130, w - 115, h - 86,
                    "GIẢM CÁP", Color.rgb(61, 102, 139), 12);

            if (!fishing && !fishBiting && !finished) {
                button(c, 32, h - 92, 220, h - 37,
                        "THẢ LƯỚI", Color.rgb(49, 137, 91), 15);
            }

            p.setColor(Color.argb(195, 10, 13, 16));
            c.drawRoundRect(32, h - 137, 535, h - 99, 12, 12, p);
            p.setColor(Color.WHITE);
            p.setTextSize(11);
            c.drawText(notice, 47, h - 113, p);

            if (finished) {
                p.setColor(Color.argb(230, 4, 7, 9));
                c.drawRect(0, 0, w, h, p);
                p.setColor(Color.rgb(255, 218, 45));
                p.setTextSize(31);
                center(c, "BẮT ĐƯỢC CÁ!", w / 2f, h * .37f);
                p.setColor(Color.WHITE);
                p.setTextSize(18);
                center(c, fishName + "  •  + " + rewardText() + "$",
                        w / 2f, h * .44f);
                button(c, w * .38f, h * .52f, w * .62f, h * .52f + 58,
                        "CÂU TIẾP", Color.rgb(51, 136, 88), 16);
                button(c, w * .38f, h * .52f + 72, w * .62f, h * .52f + 130,
                        "VỀ SẢNH", Color.rgb(62, 105, 150), 16);
            }
        }

        void drawGameSkills(Canvas c, int w, int h, long now) {
            for (int i = 0; i < 3; i++) {
                float x = 24 + i * 165;
                float y = h - 92;
                p.setColor(characterColor[i]);
                c.drawRoundRect(x, y, x + 148, y + 55, 12, 12, p);

                p.setColor(Color.WHITE);
                p.setTextSize(10);
                c.drawText(characterNames[i], x + 10, y + 17, p);

                String label = skillNames[i];
                p.setTextSize(label.length() > 13 ? 9 : 11);
                c.drawText(label, x + 10, y + 35, p);

                long left = Math.max(0, skillReadyAt[i] - now);
                p.setTextSize(9);
                c.drawText(left == 0 ? "SẴN SÀNG" : ("Hồi " + ((left + 999) / 1000) + "s"),
                        x + 10, y + 49, p);
            }

            if (activeSkill >= 0 && now < activeSkillEnd) {
                if (activeSkill == 0) drawTowTruck(c, w, h, now);
                if (activeSkill == 1) drawSkyBurst(c, w, h, now);
                if (activeSkill == 2) drawShield(c, w, h, now);
            }
        }

        void drawFish(Canvas c, float x, float y, long now) {
            float s = 1.0f;
            if (fishLevel >= 7) s = 1.25f;
            if (fishLevel >= 9) s = 1.45f;

            p.setColor(Color.rgb(93, 61, 96));
            c.drawOval(x - 76 * s, y - 34 * s,
                    x + 76 * s, y + 34 * s, p);

            path.reset();
            path.moveTo(x + 58 * s, y);
            path.lineTo(x + 120 * s, y - 52 * s);
            path.lineTo(x + 109 * s, y);
            path.lineTo(x + 120 * s, y + 52 * s);
            path.close();
            p.setColor(Color.rgb(142, 73, 89));
            c.drawPath(path, p);

            p.setColor(Color.WHITE);
            c.drawCircle(x - 46 * s, y - 9 * s, 10 * s, p);
            p.setColor(Color.BLACK);
            c.drawCircle(x - 46 * s, y - 9 * s, 4 * s, p);

            p.setColor(Color.rgb(173, 91, 108));
            c.drawOval(x - 4 * s, y - 51 * s,
                    x + 42 * s, y - 22 * s, p);

            p.setColor(Color.argb(150, 240, 250, 255));
            for (int i = 0; i < 8; i++) {
                c.drawCircle(
                        x - 66 + i * 20,
                        y + 42 + (float)Math.sin(now / 180.0 + i) * 7,
                        3 + i % 2, p
                );
            }
        }

        void drawCharacter(Canvas c, float x, float y, int id, float scale, boolean rodVisible) {
            // Head.
            p.setColor(Color.rgb(242, 205, 164));
            c.drawCircle(x, y - 70 * scale, 19 * scale, p);

            // Hair.
            p.setColor(id == 1 ? Color.BLACK : Color.rgb(64, 58, 52));
            c.drawCircle(x, y - 80 * scale, 21 * scale, p);

            // Body.
            p.setColor(characterColor[id]);
            c.drawRoundRect(
                    x - 24 * scale, y - 52 * scale,
                    x + 24 * scale, y + 12 * scale,
                    10 * scale, 10 * scale, p
            );

            // Legs.
            p.setColor(Color.rgb(43, 46, 50));
            c.drawRect(x - 15 * scale, y + 10 * scale,
                    x - 3 * scale, y + 40 * scale, p);
            c.drawRect(x + 3 * scale, y + 10 * scale,
                    x + 15 * scale, y + 40 * scale, p);

            // Simple arms / pose.
            p.setColor(Color.rgb(242, 205, 164));
            p.setStrokeWidth(7 * scale);
            c.drawLine(x - 17 * scale, y - 38 * scale,
                    x - 31 * scale, y - 10 * scale, p);
            c.drawLine(x + 17 * scale, y - 38 * scale,
                    x + 31 * scale, y - 10 * scale, p);

            p.setColor(Color.WHITE);
            p.setTextSize(10 * scale);
            center(c, characterNames[id], x, y + 58 * scale);

            if (rodVisible) {
                p.setColor(Color.rgb(88, 55, 29));
                p.setStrokeWidth(5 * scale);
                c.drawLine(x + 24 * scale, y - 25 * scale,
                        x + 78 * scale, y - 82 * scale, p);
            }
        }

        void drawTowTruck(Canvas c, int w, int h, long now) {
            float progress = 1f - Math.max(0,
                    activeSkillEnd - now) / 2500f;
            float x = -160 + (w * .46f + 160) * progress;
            float y = h * .61f;

            p.setColor(Color.rgb(60, 132, 64));
            c.drawRoundRect(x - 75, y - 28, x + 78, y + 22, 13, 13, p);

            p.setColor(Color.rgb(205, 219, 224));
            c.drawRect(x + 12, y - 20, x + 60, y + 8, p);

            p.setColor(Color.DKGRAY);
            c.drawCircle(x - 48, y + 24, 23, p);
            c.drawCircle(x + 50, y + 22, 17, p);

            p.setColor(Color.BLACK);
            c.drawCircle(x - 48, y + 24, 11, p);
            c.drawCircle(x + 50, y + 22, 8, p);

            p.setColor(Color.WHITE);
            p.setTextSize(22);
            c.drawText("XE KÉO!", x - 58, y + 66, p);
        }

        void drawSkyBurst(Canvas c, int w, int h, long now) {
            float pulse = 1f + (float)Math.sin(now / 90f) * .12f;
            float cx = w * .66f;
            float cy = h * .50f;

            p.setColor(Color.argb(75, 185, 65, 255));
            c.drawCircle(cx, cy, 120 * pulse, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(12);
            p.setColor(Color.rgb(193, 48, 240));
            for (int i = 0; i < 8; i++) {
                float a = (float)(i * Math.PI / 4.0 + now / 700.0);
                c.drawLine(
                        cx, cy,
                        cx + (float)Math.cos(a) * 170,
                        cy + (float)Math.sin(a) * 120,
                        p
                );
            }
            p.setStyle(Paint.Style.FILL);

            p.setColor(Color.MAGENTA);
            p.setTextSize(23);
            center(c, "-" + formatNumber(600000), cx, cy - 20);
            center(c, "-" + formatNumber(600000), cx + 35, cy + 12);
            center(c, "-" + formatNumber(600000), cx - 35, cy + 45);
        }

        void drawShield(Canvas c, int w, int h, long now) {
            float pulse = 1f + (float)Math.sin(now / 130f) * .08f;
            float cx = w * .45f;
            float cy = h * .58f;

            p.setColor(Color.argb(65, 90, 190, 255));
            c.drawCircle(cx, cy, 85 * pulse, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(10);
            p.setColor(Color.rgb(112, 203, 255));
            c.drawCircle(cx, cy, 65 * pulse, p);
            p.setStyle(Paint.Style.FILL);

            p.setColor(Color.WHITE);
            p.setTextSize(20);
            center(c, "HỘ NGƯ", cx, cy + 7);
        }

        void updateGame(long now) {
            if (page != GAME) return;

            if (fishing && !fishBiting && now >= nextBite) {
                startFishFight(now);
            }

            if (!fishBiting || finished) return;

            if (lastPhysics == 0) lastPhysics = now;
            if (now - lastPhysics >= 600) {
                lastPhysics = now;

                lineTension += 4 + random.nextInt(6);
                lineLength += .15f + random.nextFloat() * .18f;

                if (reelPressed) {
                    lineTension += 3;
                }

                if (lineTension > 100) lineTension = 100;
                if (lineLength > 10f) lineLength = 10f;
            }

            reelPressed = false;

            fishX += fishVX;
            if (fishX < .58f || fishX > .90f) fishVX = -fishVX;
            fishY = .50f + (float)Math.sin(now / 230.0) * .035f;

            if (activeSkill >= 0 && now < activeSkillEnd) {
                if (activeSkill == 1 && now - skillTick >= 300) {
                    skillTick = (int)now;
                    long damage = 600000L;
                    fishHp -= damage;
                }
            }

            if (fishHp <= 0) {
                fishHp = 0;
                finishCatch();
                return;
            }

            if (lineTension >= 100) {
                fishBiting = false;
                fishing = false;
                lineTension = 20;
                lineLength = 2.5f;
                notice = "Đứt cáp! Hãy giảm căng dây và thử lại.";
                activeSkill = -1;
                return;
            }
        }

        void startFishFight(long now) {
            fishBiting = true;
            fishing = true;
            fishLevel = selectedLevel;
            fishName = fishLevel >= 8 ? "Hạo Đạo Đế" : "Cá Vạn Cân";
            fishMaxHp = 9999999L + fishLevel * 1000000L;
            fishHp = fishMaxHp;
            fishX = .78f;
            fishY = .50f;
            fishVX = .0021f + fishLevel * .0001f;
            lineTension = 14;
            lineLength = .7f;
            lastPhysics = now;
            notice = "Cá đã cắn! Cả 3 nhân vật đang câu cùng lúc.";
        }

        void castAll() {
            if (energy < 5) {
                notice = "Hết năng lượng.";
                return;
            }

            energy -= 5;
            fishing = true;
            fishBiting = false;
            finished = false;
            activeSkill = -1;
            lineTension = 12;
            lineLength = .7f;

            long now = System.currentTimeMillis();
            nextBite = now + 1200;
            lastPhysics = 0;

            notice = "Cả 3 nhân vật cùng thả câu...";
        }

        void reel() {
            if (!fishBiting || finished) return;

            long now = System.currentTimeMillis();
            if (now - lastReel < 160) return;
            lastReel = now;
            reelPressed = true;

            long damage = 330000L
                    + (long)rodPower[equippedRod] * 90000L
                    + (long)selectedLevel * 10000L;

            fishHp -= damage;
            lineTension += 7;
            lineLength = Math.max(.5f, lineLength - .35f);

            if (fishHp < 0) fishHp = 0;
            notice = "Co lại dây! -" + formatNumber(damage) + " HP";
        }

        void loosen() {
            if (!fishBiting || finished) return;

            lineTension -= 30;
            if (lineTension < 5) lineTension = 5;
            lineLength = Math.max(.6f, lineLength - .6f);
            notice = "Đã giảm cáp, hạ độ căng dây.";
        }

        void useSkill(int who) {
            if (!fishBiting || finished) {
                notice = "Kỹ năng chỉ dùng khi cá đã cắn.";
                return;
            }

            long now = System.currentTimeMillis();
            if (now < skillReadyAt[who]) {
                notice = characterNames[who] + ": kỹ năng đang hồi.";
                return;
            }

            skillReadyAt[who] = now + (who == 1 ? 12000 : 8000);
            activeSkill = who;
            activeSkillEnd = now + (who == 1 ? 2100 : 2500);
            skillTick = (int)now;

            if (who == 0) {
                lineTension -= 48;
                if (lineTension < 4) lineTension = 4;
                lineLength = Math.max(.5f, lineLength - 3.2f);
                fishHp -= 800000L;
                notice = "SỞ TÂM dùng XE KÉO! Kéo cá về gần bờ.";
            } else if (who == 1) {
                fishHp -= 600000L;
                lineTension -= 20;
                if (lineTension < 5) lineTension = 5;
                notice = "BÁ THƯỜNG dùng PHI THIÊN VÔ CỰC!";
            } else {
                lineTension -= 45;
                if (lineTension < 3) lineTension = 3;
                notice = "LÃO NGÔ dùng HỘ NGƯ! Ổn định dây cho cả đội.";
            }

            if (fishHp < 0) fishHp = 0;
        }

        void finishCatch() {
            finished = true;
            fishBiting = false;
            fishing = false;
            long reward = 500000L + (long)selectedLevel * 150000L;
            money += reward;
            notice = "Bắt được " + fishName + "!";
        }

        String rewardText() {
            return "" + (500000L + (long)selectedLevel * 150000L);
        }

        void drawRewardPanel(Canvas c, int w, int h) {
            // Reserved for future expansion.
        }

        boolean hit(float x, float y, float l, float t, float r, float b) {
            return x >= l && x <= r && y >= t && y <= b;
        }

        void button(Canvas c, float l, float t, float r, float b,
                    String text, int color, float size) {
            p.setColor(Color.argb(85, 0, 0, 0));
            c.drawRoundRect(l + 3, t + 4, r + 3, b + 4, 12, 12, p);
            p.setColor(color);
            c.drawRoundRect(l, t, r, b, 12, 12, p);
            p.setColor(Color.WHITE);
            p.setTextSize(size);
            center(c, text, (l + r) / 2f, (t + b) / 2f + 5);
        }

        void center(Canvas c, String text, float x, float y) {
            c.drawText(text, x - p.measureText(text) / 2f, y, p);
        }

        String moneyText() {
            if (money >= 1000000) {
                return String.format(java.util.Locale.US, "%.2fTr", money / 1000000.0);
            }
            if (money >= 1000) {
                return String.format(java.util.Locale.US, "%.1fK", money / 1000.0);
            }
            return String.valueOf(money);
        }

        String formatNumber(long n) {
            if (Math.abs(n) >= 1000000L) {
                return String.format(java.util.Locale.US, "%.2fM", n / 1000000.0);
            }
            if (Math.abs(n) >= 1000L) {
                return String.format(java.util.Locale.US, "%.1fK", n / 1000.0);
            }
            return String.valueOf(n);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() != MotionEvent.ACTION_UP) return true;

            float x = e.getX();
            float y = e.getY();
            int w = getWidth();
            int h = getHeight();

            if (page == HOME) {
                float top = h * .15f;
                if (hit(x, y, 22, top, 237, top + 55)) {
                    page = LEVELS;
                } else if (hit(x, y, 22, top + 67, 237, top + 122)) {
                    page = LEVELS;
                } else if (hit(x, y, 22, top + 134, 237, top + 189)) {
                    page = CHARACTERS;
                } else if (hit(x, y, 22, top + 201, 237, top + 256)) {
                    page = SHOP;
                } else if (hit(x, y, 22, top + 268, 237, top + 323)) {
                    page = SKILLS;
                }
                invalidate();
                return true;
            }

            if (page == LEVELS) {
                if (hit(x, y, 18, 10, 55, 70)) {
                    page = HOME;
                } else {
                    int cols = 3;
                    float startX = 70;
                    float startY = 85;
                    float cardW = (w - 190) / 3f;
                    for (int i = 0; i < 9; i++) {
                        int row = i / cols;
                        int col = i % cols;
                        float bx = startX + col * (cardW + 25);
                        float by = startY + row * 130;
                        if (i < 4 && hit(x, y, bx, by, bx + cardW, by + 115)) {
                            selectedLevel = i + 1;
                            page = GAME;
                            castAll();
                            break;
                        }
                    }
                }
                invalidate();
                return true;
            }

            if (page == CHARACTERS) {
                if (hit(x, y, 18, 10, 55, 70)) {
                    page = HOME;
                } else {
                    float cardW = (w - 90) / 3f;
                    for (int i = 0; i < 3; i++) {
                        float bx = 20 + i * (cardW + 15);
                        if (hit(x, y, bx, 85, bx + cardW, h - 36)) {
                            selectedCharacter = i;
                            notice = "Đã chọn " + characterNames[i] + ".";
                            break;
                        }
                    }
                }
                invalidate();
                return true;
            }

            if (page == SHOP) {
                if (hit(x, y, 18, 10, 55, 70)) {
                    page = HOME;
                } else {
                    for (int i = 0; i < rodNames.length; i++) {
                        float by = 80 + i * 73;
                        if (hit(x, y, 55, by, w - 55, by + 58)) {
                            if (i == equippedRod) {
                                notice = rodNames[i] + " đang được dùng.";
                            } else if (money >= rodPrice[i]) {
                                money -= rodPrice[i];
                                equippedRod = i;
                                notice = "Đã mua và trang bị " + rodNames[i] + ".";
                            } else {
                                notice = "Không đủ tiền mua " + rodNames[i] + ".";
                            }
                            break;
                        }
                    }
                }
                invalidate();
                return true;
            }

            if (page == SKILLS) {
                if (hit(x, y, 18, 10, 55, 70)) {
                    page = HOME;
                    invalidate();
                    return true;
                }
                return true;
            }

            if (page == GAME) {
                if (finished) {
                    if (hit(x, y, w * .38f, h * .52f,
                            w * .62f, h * .52f + 58)) {
                        finished = false;
                        selectedLevel = Math.min(9, selectedLevel + 1);
                        castAll();
                    } else if (hit(x, y, w * .38f, h * .52f + 72,
                            w * .62f, h * .52f + 130)) {
                        page = HOME;
                    }
                    invalidate();
                    return true;
                }

                if (!fishing && !fishBiting &&
                        hit(x, y, 32, h - 92, 220, h - 37)) {
                    castAll();
                    invalidate();
                    return true;
                }

                if (hit(x, y, w - 165, h - 145, w - 35, h - 10)) {
                    reel();
                    invalidate();
                    return true;
                }

                if (hit(x, y, w - 245, h - 145, w - 115, h - 80)) {
                    loosen();
                    invalidate();
                    return true;
                }

                for (int i = 0; i < 3; i++) {
                    float sx = 24 + i * 165;
                    if (hit(x, y, sx, h - 92, sx + 148, h - 37)) {
                        useSkill(i);
                        invalidate();
                        return true;
                    }
                }

                // Top-left back area.
                if (hit(x, y, 16, 10, 180, 52)) {
                    page = HOME;
                    fishing = false;
                    fishBiting = false;
                }

                invalidate();
                return true;
            }

            return true;
        }
    }
}
