package com.kiet13312.fishingchuxindiao;

import android.graphics.Canvas;
import android.graphics.Paint;

final class Fx {
    static final int[] BODY = {0xFFC2452D, 0xFF3B6EA5, 0xFF8A4A8A, 0xFF3F8F4A, 0xFFD9822B, 0xFF2E8B8B, 0xFFB5338A, 0xFF5B6B7A, 0xFF444444, 0xFF9C6B30, 0xFFEFEFEF, 0xFF7A1F1F};
    static final String[] NAME = {"Sở Tâm", "Bá Thường", "Lão Ngô"};

    static float rnd(int n) { float x = (float) Math.sin(n * 12.9898) * 43758.547f; return x - (float) Math.floor(x); }

    static void text(Canvas c, Paint p, String s, float x, float y, float sz, int col) {
        p.setTextSize(sz);
        float xx = x - p.measureText(s) / 2;
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(sz * .16f); p.setColor(0xFF000000); c.drawText(s, xx, y, p);
        p.setStyle(Paint.Style.FILL); p.setColor(col); c.drawText(s, xx, y, p);
    }

    // (x, y) là chân nhân vật, s là độ phóng to. Đầu cần nằm ở (x+85s, y-165s)
    static void person(Canvas c, Paint p, float x, float y, float s, int i) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x44000000); c.drawOval(x - 30 * s, y - 4 * s, x + 30 * s, y + 8 * s, p);
        p.setColor(0xFF333333); c.drawRect(x - 11 * s, y - 26 * s, x - 2 * s, y, p); c.drawRect(x + 2 * s, y - 26 * s, x + 11 * s, y, p);
        p.setColor(BODY[i % BODY.length]); c.drawRoundRect(x - 15 * s, y - 74 * s, x + 15 * s, y - 24 * s, 9 * s, 9 * s, p);
        p.setColor(0xFFF2C9A0); c.drawCircle(x, y - 90 * s, 15 * s, p);
        p.setColor(i % 3 == 2 ? 0xFFD9D9D9 : 0xFF2A2230); c.drawCircle(x, y - 98 * s, 10 * s, p);
        p.setColor(0xFF222222); c.drawCircle(x - 5 * s, y - 90 * s, 1.8f * s, p); c.drawCircle(x + 5 * s, y - 90 * s, 1.8f * s, p);
        p.setStrokeWidth(3 * s); p.setColor(0xFF4A3A2A); c.drawLine(x + 10 * s, y - 52 * s, x + 85 * s, y - 165 * s, p);
    }

    // k: tiến độ chiêu 0..1, who: 0 Xuống núi (đất nứt + khiên nước), 1 Đại ma (khói mực đen), 2 Xe đạp (xích + gió xoáy)
    static void draw(Canvas c, Paint p, int w, int h, float u, float k, int who, String name, float fx, float fy, float dmg) {
        float cx = w * .30f, cy = h * .78f;
        int main = who == 0 ? 0xFFFFD27A : who == 1 ? 0xFFFF4DD2 : 0xFF7FE8FF;
        float a = Math.min(Math.min(1f, k * 4f), Math.min(1f, (1f - k) * 4f));

        // 1. tối màn hình
        p.setStyle(Paint.Style.FILL); p.setColor((int) (150 * a) << 24); c.drawRect(0, 0, w, h, p);

        // 2. vạch tốc độ từ giữa màn hình
        if (k < .75f) {
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2 * u); p.setColor((main & 0xFFFFFF) | ((int) (200 * a) << 24));
            for (int i = 0; i < 28; i++) {
                float ang = i * .2244f + rnd(i) * .1f, r0 = h * (.35f + .2f * rnd(i + 50)), r1 = r0 + h * (.3f + .5f * rnd(i + 90));
                c.drawLine(w / 2f + (float) Math.cos(ang) * r0 * 1.6f, h / 2f + (float) Math.sin(ang) * r0,
                        w / 2f + (float) Math.cos(ang) * r1 * 1.6f, h / 2f + (float) Math.sin(ang) * r1, p);
            }
        }

        // 3. mặt đất nứt toác quanh chân nhân vật
        float cr = Math.min(1f, k * 3.5f);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3 * u); p.setColor(0xFF2A1D14);
        for (int i = 0; i < 12; i++) {
            float ang = i * .5236f + rnd(i) * .3f, px = cx, py = cy;
            for (int sg = 0; sg < 4; sg++) {
                float len = (40 + 50 * rnd(i * 4 + sg)) * u * cr, na = ang + (rnd(i * 7 + sg) - .5f) * .8f;
                float nx = px + (float) Math.cos(na) * len * 1.6f, ny = py + (float) Math.sin(na) * len * .45f;
                c.drawLine(px, py, nx, ny, p); px = nx; py = ny;
            }
        }

        // 4. đá văng tung tóe
        p.setStyle(Paint.Style.FILL);
        float kk = Math.max(0f, k - .1f) * 1.6f;
        for (int i = 0; i < 12; i++) {
            float vx = (rnd(i + 200) - .5f) * 380 * u, vy = (200 + 260 * rnd(i + 300)) * u, sz = (6 + 8 * rnd(i + 400)) * u;
            float dx = cx + vx * kk, dy = cy - vy * kk + 900 * u * kk * kk;
            p.setColor(i % 2 == 0 ? 0xFF8A6A43 : 0xFFB89A6A);
            c.drawRect(dx - sz, dy - sz, dx + sz, dy + sz, p);
        }

        // 5. khiên nước (Xuống núi) hoặc khói mực đen (Đại ma)
        float dome = 150 * u * Math.min(1f, k * 3f);
        if (who == 0) {
            p.setColor(0x4D78C8FF); c.drawCircle(cx, cy - 90 * u, dome, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4 * u); p.setColor(0xCCFFFFFF); c.drawCircle(cx, cy - 90 * u, dome, p);
            p.setStyle(Paint.Style.FILL);
        } else if (who == 1) {
            for (int i = 0; i < 16; i++) {
                float t0 = (k * 2.5f + i * .13f) % 1f;
                p.setColor(((int) (170 * (1 - t0) * a) << 24) | 0x1E0A32);
                c.drawCircle(cx + (rnd(i) - .5f) * 120 * u, cy - 20 * u - t0 * 330 * u, (22 + 30 * t0) * u, p);
            }
        }

        // 6. nhân vật phóng to ở giữa, cần câu khổng lồ
        float sc = u * (1.2f + 1.2f * Math.min(1f, k * 5f));
        person(c, p, cx, cy, sc, who);

        // 7. xích hạt từ đầu cần tới con cá (Đại ma, Xe đạp)
        if (who != 0 && k > .25f) {
            float x0 = cx + 85 * sc, y0 = cy - 165 * sc;
            p.setStyle(Paint.Style.FILL); p.setColor(who == 2 ? 0xFF7FE8FF : 0xFFFF4DD2);
            for (int i = 0; i < 24; i++) {
                float q = i / 23f;
                c.drawCircle(x0 + (fx - x0) * q, y0 + (fy - y0) * q + (float) Math.sin(q * 9 + k * 30) * 6 * u, 4.5f * u, p);
            }
        }

        // 8. cú đánh trúng cá: lóe sáng, sóng xung kích, nước bắn tung, gió xoáy
        float im = k - .45f;
        if (im > 0) {
            float f = Math.min(1f, im * 6f), fade = Math.max(0f, 1f - im * 2.2f);
            p.setStyle(Paint.Style.FILL); p.setColor(((int) (230 * fade) << 24) | 0xFFFFFF);
            c.drawCircle(fx, fy, 70 * u * f, p);
            for (int i = 0; i < 14; i++)
                c.drawCircle(fx + (rnd(i + 600) - .5f) * 200 * u, fy - (60 + 220 * rnd(i + 700)) * Math.min(1f, im * 3f) * u + 600 * u * im * im, (4 + 6 * rnd(i + 800)) * u, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(6 * u);
            for (int i = 0; i < 3; i++) {
                float r = (40 + im * (420 + 140 * i)) * u;
                p.setColor(((int) (200 * fade) << 24) | (main & 0xFFFFFF));
                c.drawOval(fx - r, fy - r * .4f, fx + r, fy + r * .4f, p);
            }
            p.setStrokeWidth(5 * u);
            for (int i = 0; i < 3; i++) {
                float r = (60 + 45 * i + 260 * im) * u;
                p.setColor(((int) (210 * fade) << 24) | (who == 1 ? 0xFF66E0 : 0xFFFFFF));
                c.drawArc(fx - r, fy - r * .6f, fx + r, fy + r * .6f, k * 720 + i * 120, 90, false, p);
            }
            // số sát thương màu tím như trong game
            float rise = im * 120 * u;
            text(c, p, "-" + (int) dmg, fx, fy - 60 * u - rise, 34 * u, 0xFFC04DFF);
            text(c, p, "-" + (int) (dmg * .37f), fx - 90 * u, fy - 20 * u - rise * .7f, 20 * u, 0xFFE08CFF);
            text(c, p, "-" + (int) (dmg * .21f), fx + 90 * u, fy - 10 * u - rise * .5f, 18 * u, 0xFFE08CFF);
        }

        // 9. tên chiêu phóng to như phụ đề
        text(c, p, name, w / 2f, h * .2f, (34 + 12 * Math.min(1f, k * 5f)) * u, main);
    }
}
