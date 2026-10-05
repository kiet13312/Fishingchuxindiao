package com.kiet13312.fishingchuxindiao;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;

final class Fx {
    static final int[] BODY = {0xFFC2452D, 0xFF3B6EA5, 0xFF8A4A8A, 0xFF3F8F4A, 0xFFD9822B, 0xFF2E8B8B, 0xFFB5338A, 0xFF5B6B7A, 0xFF444444, 0xFF9C6B30, 0xFFEFEFEF, 0xFF7A1F1F};
    static final Path path = new Path();
    static final int[][] BG = {{0xFF6EC6F0, 0xFFE8F6D8}, {0xFFEDEDED, 0xFF9A9AA8}, {0xFF4A2A6A, 0xFFE8903A}, {0xFF2A9AB0, 0xFFBFEFF0},
            {0xFF0A5A66, 0xFF29E0E8}, {0xFF0B1030, 0xFF304880}, {0xFF300808, 0xFFFF7A18}, {0xFF081018, 0xFF2A5A78}};
    static final int[] MAIN = {0xFFFFD27A, 0xFFFFFFFF, 0xFFFFE04A, 0xFF9FF0FF, 0xFF7FF8FF, 0xFFB8D8FF, 0xFFFF9A2A, 0xFF7FE8FF};

    static float rnd(int n) { float x = (float) Math.sin(n * 12.9898) * 43758.547f; return x - (float) Math.floor(x); }
    static float seg(float k, float a, float b) { return Math.max(0f, Math.min(1f, (k - a) / (b - a))); }
    static int al(int col, float a) { return (col & 0xFFFFFF) | ((int) (255 * Math.max(0f, Math.min(1f, a))) << 24); }

    static void text(Canvas c, Paint p, String s, float x, float y, float sz, int col) {
        p.setTextSize(sz);
        float xx = x - p.measureText(s) / 2;
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(sz * .16f); p.setColor(0xFF000000); c.drawText(s, xx, y, p);
        p.setStyle(Paint.Style.FILL); p.setColor(col); c.drawText(s, xx, y, p);
    }

    static void person(Canvas c, Paint p, float x, float y, float s, int i) { pose(c, p, x, y, s, i, 56.4f, 136f, 0, 0, 0, 0, false); }

    // (x, y) = chân. ang/len = góc và độ dài cần. lean nghiêng người, crouch ngồi xổm, armUp giơ tay, flex bắp tay, yell hét
    static void pose(Canvas c, Paint p, float x, float y, float s, int i, float ang, float len, float lean, float crouch, float armUp, float flex, boolean yell) {
        float L = 26 * (1 - .5f * crouch), bt = L + 50, hd = bt + 16, sh = bt - 8;
        float hx = x + (10 - 8 * armUp) * s, hy = y - (sh - 16 + armUp * 45) * s;
        c.save();
        c.rotate(lean * 30f, x, y);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x44000000); c.drawOval(x - 30 * s, y - 4 * s, x + 30 * s, y + 8 * s, p);
        p.setColor(0xFF333333); c.drawRect(x - 11 * s, y - L * s, x - 2 * s, y, p); c.drawRect(x + 2 * s, y - L * s, x + 11 * s, y, p);
        p.setColor(BODY[i % BODY.length]); c.drawRoundRect(x - 15 * s, y - bt * s, x + 15 * s, y - (L - 2) * s, 9 * s, 9 * s, p);
        p.setStrokeWidth(7 * s); p.setColor(0xFFF2C9A0);
        c.drawLine(x + 12 * s, y - sh * s, hx, hy, p); c.drawLine(x - 12 * s, y - sh * s, hx - 4 * s, hy + 6 * s, p);
        if (flex > 0) { c.drawCircle(x + 22 * s, y - (sh - 8) * s, 11 * s * flex, p); c.drawCircle(x - 22 * s, y - (sh - 8) * s, 11 * s * flex, p); }
        c.drawCircle(x, y - hd * s, 15 * s, p);
        p.setColor(i % 3 == 2 ? 0xFFD9D9D9 : 0xFF2A2230); c.drawCircle(x, y - (hd + 8) * s, 10 * s, p);
        p.setColor(0xFF222222); c.drawCircle(x - 5 * s, y - hd * s, 1.8f * s, p); c.drawCircle(x + 5 * s, y - hd * s, 1.8f * s, p);
        if (yell) {
            c.drawRect(x - 5 * s, y - (hd - 7) * s, x + 5 * s, y - (hd - 12) * s, p);
            p.setStrokeWidth(2 * s);
            c.drawLine(x - 9 * s, y - (hd + 7) * s, x - 2 * s, y - (hd + 3) * s, p); c.drawLine(x + 9 * s, y - (hd + 7) * s, x + 2 * s, y - (hd + 3) * s, p);
        }
        float a = (float) Math.toRadians(ang);
        p.setStrokeWidth(3 * s); p.setColor(0xFF4A3A2A);
        c.drawLine(hx, hy, hx + (float) Math.cos(a) * len * s, hy - (float) Math.sin(a) * len * s, p);
        c.restore();
    }

    static void cracks(Canvas c, Paint p, float u, float cx, float cy, float g) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3 * u); p.setColor(0xFF2A1D14);
        for (int i = 0; i < 12; i++) {
            float ang = i * .5236f + rnd(i) * .3f, px = cx, py = cy;
            for (int sg = 0; sg < 4; sg++) {
                float len = (40 + 50 * rnd(i * 4 + sg)) * u * g, na = ang + (rnd(i * 7 + sg) - .5f) * .8f;
                float nx = px + (float) Math.cos(na) * len * 1.6f, ny = py + (float) Math.sin(na) * len * .45f;
                c.drawLine(px, py, nx, ny, p); px = nx; py = ny;
            }
        }
        p.setStyle(Paint.Style.FILL);
    }

    static void debris(Canvas c, Paint p, float u, float cx, float cy, float kk) {
        p.setStyle(Paint.Style.FILL);
        kk = Math.max(0f, kk) * 1.6f;
        for (int i = 0; i < 12; i++) {
            float vx = (rnd(i + 200) - .5f) * 380 * u, vy = (200 + 260 * rnd(i + 300)) * u, sz = (6 + 8 * rnd(i + 400)) * u;
            float dx = cx + vx * kk, dy = cy - vy * kk + 900 * u * kk * kk;
            p.setColor(i % 2 == 0 ? 0xFF8A6A43 : 0xFFB89A6A);
            c.drawRect(dx - sz, dy - sz, dx + sz, dy + sz, p);
        }
    }

    static void beads(Canvas c, Paint p, float u, float x0, float y0, float x1, float y1, float g, float k, int col) {
        p.setStyle(Paint.Style.FILL); p.setColor(col);
        for (int i = 0; i < 24; i++) {
            float q = i / 23f * g;
            c.drawCircle(x0 + (x1 - x0) * q, y0 + (y1 - y0) * q + (float) Math.sin(q * 9 + k * 30) * 6 * u, 4.5f * u, p);
        }
    }

    static void bolt(Canvas c, Paint p, float u, float x0, float y0, float x1, float y1, int sd) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(4 * u); p.setColor(0xFFE8F4FF);
        float px = x0, py = y0;
        for (int i = 1; i <= 8; i++) {
            float q = i / 8f, nx = x0 + (x1 - x0) * q + (i < 8 ? (rnd(sd * 9 + i) - .5f) * 60 * u : 0), ny = y0 + (y1 - y0) * q;
            c.drawLine(px, py, nx, ny, p); px = nx; py = ny;
        }
    }

    // cú đánh trúng cá: lóe sáng, sóng, nước bắn, số sát thương tím
    static void impact(Canvas c, Paint p, float u, float fx, float fy, float im, int col, float dmg) {
        if (im <= 0) return;
        float f = Math.min(1f, im * 6f), fade = Math.max(0f, 1f - im * 2.2f);
        p.setStyle(Paint.Style.FILL); p.setColor(al(0xFFFFFF, fade)); c.drawCircle(fx, fy, 70 * u * f, p);
        for (int i = 0; i < 14; i++)
            c.drawCircle(fx + (rnd(i + 600) - .5f) * 200 * u, fy - (60 + 220 * rnd(i + 700)) * Math.min(1f, im * 3f) * u + 600 * u * im * im, (4 + 6 * rnd(i + 800)) * u, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(6 * u); p.setColor(al(col, fade));
        for (int i = 0; i < 3; i++) { float r = (40 + im * (420 + 140 * i)) * u; c.drawOval(fx - r, fy - r * .4f, fx + r, fy + r * .4f, p); }
        float rise = im * 120 * u;
        text(c, p, "-" + (int) dmg, fx, fy - 60 * u - rise, 34 * u, 0xFFC04DFF);
        text(c, p, "-" + (int) (dmg * .37f), fx - 90 * u, fy - 20 * u - rise * .7f, 20 * u, 0xFFE08CFF);
        text(c, p, "-" + (int) (dmg * .21f), fx + 90 * u, fy - 10 * u - rise * .5f, 18 * u, 0xFFE08CFF);
    }

    // sk: 0..7 chiêu. Mỗi chiêu là một cảnh riêng: nhân vật đổi tư thế rồi tung đòn tới con cá
    static void draw(Canvas c, Paint p, int w, int h, float u, float k, int sk, String name, float fx, float fy, float dmg, int who) {
        float a = Math.min(Math.min(1f, k * 6f), Math.min(1f, (1f - k) * 6f));
        float cx = w * .36f, cy = h * .80f, S = u * 1.7f, im = k - .45f;
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, 0, 0, h, BG[sk][0], BG[sk][1], Shader.TileMode.CLAMP));
        p.setAlpha((int) (225 * a)); c.drawRect(0, 0, w, h, p);
        p.setShader(null); p.setAlpha(255);
        switch (sk) {
            case 0: { // Chàng trai xuống núi: nhảy từ trên cao, đáp xuống nện cần
                float fall = 1 - seg(k, 0, .28f); fall *= fall;
                float cr = seg(k, .28f, .34f) * (1 - seg(k, .42f, .7f));
                pose(c, p, cx, cy - fall * h * .8f, S, who, -80f, 150f, 0, cr, k < .28f ? 1f : .5f, 0, true);
                if (k > .28f) {
                    cracks(c, p, u, cx, cy, seg(k, .28f, .5f));
                    debris(c, p, u, cx, cy, k - .28f);
                    float r = 260 * u * seg(k, .28f, .6f);
                    p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5 * u); p.setColor(al(0xFFFFFF, 1 - seg(k, .28f, .6f)));
                    c.drawOval(cx - r, cy - r * .3f, cx + r, cy + r * .3f, p);
                }
                if (im > 0) {
                    p.setStyle(Paint.Style.FILL); p.setColor(al(0xFFFFFF, 1 - im * 1.6f));
                    for (int i = 0; i < 7; i++) c.drawRect(fx + (i - 3) * 22 * u - 8 * u, fy - (60 + 90 * rnd(i)) * u * Math.min(1f, im * 4f), fx + (i - 3) * 22 * u + 8 * u, fy, p);
                }
                break;
            }
            case 1: { // Đại ma bại trận: quỳ cắm cần, mực đen bốc lên
                pose(c, p, cx, cy, S, who, 90f, 170f, .15f, .9f * seg(k, 0, .25f), .3f, 0, false);
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 20; i++) {
                    float t1 = (k * 2 + i * .11f) % 1f;
                    p.setColor(al(0x140A28, (1 - t1) * .8f));
                    c.drawCircle(cx + (rnd(i) - .5f) * 160 * u * (.4f + t1), cy - 30 * u - t1 * h * .6f, (18 + 36 * t1) * u, p);
                }
                float g = seg(k, .4f, .6f);
                if (g > 0) {
                    p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(10 * u); p.setColor(0xFF140A28);
                    float x0 = cx + 10 * S, y0 = cy - 230 * S;
                    for (int i = 0; i < 14; i++) {
                        float q0 = i / 14f * g, q1 = (i + 1) / 14f * g;
                        c.drawLine(x0 + (fx - x0) * q0, y0 + (fy - y0) * q0 + (float) Math.sin(q0 * 9 + k * 20) * 14 * u,
                                x0 + (fx - x0) * q1, y0 + (fy - y0) * q1 + (float) Math.sin(q1 * 9 + k * 20) * 14 * u, p);
                    }
                }
                break;
            }
            case 2: { // Gà trống đại chiến: bùng hào quang vàng, giơ tay hét
                float pulse = (float) Math.sin(k * 50);
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 16; i++) {
                    float bx = cx + (i - 7.5f) * 14 * S, hh = (50 + 90 * rnd(i)) * S * (.8f + .2f * pulse);
                    path.reset(); path.moveTo(bx - 9 * S, cy); path.lineTo(bx, cy - hh); path.lineTo(bx + 9 * S, cy); path.close();
                    p.setColor(al(0xFFE04A, .8f)); c.drawPath(path, p);
                }
                pose(c, p, cx, cy, S, who, 80f, 150f, 0, 0, 1f, 0, true);
                p.setColor(0xFF8A6A43);
                for (int i = 0; i < 6; i++) {
                    float rx = cx + (rnd(i + 9) - .5f) * 320 * u, ry = cy - 80 * u - (k * 200 + i * 60) % 260 * u;
                    c.drawRect(rx, ry, rx + 16 * u, ry + 16 * u, p);
                }
                if (k > .45f) {
                    float f = 1 - seg(k, .7f, .95f);
                    p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(26 * u); p.setColor(al(0xFFE04A, .7f * f));
                    c.drawLine(cx + 10 * S, cy - 150 * S, fx, fy, p);
                    p.setStrokeWidth(10 * u); p.setColor(al(0xFFFFFF, .9f * f));
                    c.drawLine(cx + 10 * S, cy - 150 * S, fx, fy, p);
                }
                break;
            }
            case 3: { // Ngược dòng: nhảy lên, hai cần xoay chéo như chong chóng
                float oy = cy - (float) Math.sin(Math.min(1f, k * 1.6f) * Math.PI) * h * .12f;
                pose(c, p, cx, oy, S, who, 56f, 60f, 0, .2f, .8f, 0, true);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(9 * u); p.setColor(0xFFDDE6EE);
                for (int j = 0; j < 2; j++) {
                    double r = Math.toRadians(k * 1080 + j * 90);
                    c.drawLine(cx - (float) Math.cos(r) * 230 * u, oy - 110 * S - (float) Math.sin(r) * 80 * u, cx + (float) Math.cos(r) * 230 * u, oy - 110 * S + (float) Math.sin(r) * 80 * u, p);
                }
                p.setStrokeWidth(4 * u); p.setColor(al(0x9FF0FF, .8f));
                for (int i = 0; i < 3; i++) { float r2 = (60 + 50 * i + k * 120) * u; c.drawOval(cx - r2, cy - r2 * .3f, cx + r2, cy + r2 * .3f, p); }
                if (im > 0) {
                    p.setStrokeWidth(10 * u); p.setColor(al(0xFFFFFF, 1 - im * 1.8f));
                    float q = Math.min(1f, im * 2.5f), my = cy - 110 * S + (fy - cy + 110 * S) * q, mx = cx + (fx - cx) * q;
                    for (int i = 0; i < 2; i++) c.drawArc(mx - 60 * u, my - 60 * u - i * 30 * u, mx + 60 * u, my + 60 * u - i * 30 * u, -60, 120, false, p);
                }
                break;
            }
            case 4: { // Ông lão đạp xe đạp: khoe cơ bắp, xích hạt xanh
                p.setStyle(Paint.Style.FILL);
                for (int i = 0; i < 18; i++) {
                    double r = i * Math.PI / 9 + k * 2;
                    path.reset(); path.moveTo(cx, cy - 100 * S);
                    path.lineTo(cx + (float) Math.cos(r) * w, cy - 100 * S + (float) Math.sin(r) * w);
                    path.lineTo(cx + (float) Math.cos(r + .1) * w, cy - 100 * S + (float) Math.sin(r + .1) * w);
                    path.close(); p.setColor(al(0xFFFFFF, i % 2 == 0 ? .15f : .05f)); c.drawPath(path, p);
                }
                pose(c, p, cx, cy, S, who, 0f, 150f, 0, .25f, .2f, 1.2f + .15f * (float) Math.sin(k * 40), true);
                beads(c, p, u, cx + 8 * S, cy - 58 * S, cx + 158 * S, cy - 58 * S, 1f, k, 0xFF7FF8FF);
                if (im > 0) beads(c, p, u, cx + 158 * S, cy - 58 * S, fx, fy, Math.min(1f, im * 4f), k, 0xFF7FF8FF);
                break;
            }
            case 5: { // Tay bóng phản chiếu: bay lên giữa bão sấm, quăng dây vòng tròn
                float up = h * .12f * seg(k, 0, .2f);
                pose(c, p, cx, cy - up, S, who, 40f, 120f, -.15f, 0, .9f, 0, true);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3 * u); p.setColor(al(0xFFFFFF, .9f));
                c.drawArc(cx - 170 * u, cy - up - 330 * u, cx + 170 * u, cy - up - 30 * u, k * 900, 220, false, p);
                int fl = (int) (k * 24);
                if (fl % 2 == 0) bolt(c, p, u, w * rnd(fl), 0, w * rnd(fl + 5), h * .6f, fl);
                if (im > 0) bolt(c, p, u, fx, 0, fx, fy, 77);
                break;
            }
            case 6: { // Câu cá bằng động cơ: xoáy lửa, phóng quả cầu lửa
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(18 * u);
                for (int i = 0; i < 5; i++) {
                    p.setColor(al(i % 2 == 0 ? 0xFFD02A : 0xFF5A10, .6f));
                    float r = (90 + 60 * i) * u;
                    c.drawArc(w / 2f - r * 1.6f, h / 2f - r, w / 2f + r * 1.6f, h / 2f + r, k * 500 + i * 70, 120, false, p);
                }
                pose(c, p, cx, cy, S, who, 15f, 150f, -.25f, .1f, .2f, 0, true);
                float g = seg(k, .35f, .62f), x0 = cx + 150 * S, y0 = cy - 70 * S;
                if (g > 0 && g < 1) {
                    p.setStyle(Paint.Style.FILL);
                    for (int i = 6; i >= 0; i--) {
                        float q = Math.max(0f, g - i * .03f);
                        p.setColor(al(0xFF5A10, .5f - i * .06f)); c.drawCircle(x0 + (fx - x0) * q, y0 + (fy - y0) * q, (26 - i * 2) * u, p);
                    }
                    p.setColor(0xFFFFD02A); c.drawCircle(x0 + (fx - x0) * g, y0 + (fy - y0) * g, 18 * u, p);
                }
                break;
            }
            default: { // Phá ông chìm thuyền: giơ cần gọi rồng, rồng phun tia
                pose(c, p, cx, cy, S, who, 45f, 160f, 0, 0, 1f, 0, true);
                float dh = seg(k, .15f, .45f), hx = w * 1.1f + (fx + 120 * u - w * 1.1f) * dh, hy = fy - 30 * u;
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(46 * u); p.setColor(0xFF1E3A4A);
                path.reset(); path.moveTo(w * 1.2f, h * 1.1f); path.quadTo(w * .9f, h * .9f + (1 - dh) * h * .3f, hx, hy); c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL); p.setColor(0xFFE8F0F8);
                path.reset(); path.moveTo(hx - 100 * u, hy); path.lineTo(hx - 20 * u, hy - 42 * u); path.lineTo(hx + 60 * u, hy - 30 * u);
                path.lineTo(hx + 60 * u, hy + 32 * u); path.lineTo(hx - 20 * u, hy + 26 * u); path.close(); c.drawPath(path, p);
                p.setColor(0xFF29E0E8); c.drawCircle(hx, hy - 16 * u, 7 * u, p);
                p.setColor(0xFFFFFFFF);
                for (int i = 0; i < 5; i++) {
                    path.reset(); path.moveTo(hx - 90 * u + i * 16 * u, hy + 4 * u); path.lineTo(hx - 82 * u + i * 16 * u, hy + 20 * u); path.lineTo(hx - 74 * u + i * 16 * u, hy + 4 * u); path.close(); c.drawPath(path, p);
                }
                if (k > .45f) {
                    p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(26 * u); p.setColor(al(0x29E0E8, .7f * (1 - seg(k, .7f, .95f))));
                    c.drawLine(hx - 100 * u, hy, fx - 140 * u, fy + 60 * u, p);
                }
                break;
            }
        }
        impact(c, p, u, fx, fy, im, MAIN[sk], dmg);
        p.setStyle(Paint.Style.FILL); p.setColor(al(0x000000, a));
        c.drawRect(0, 0, w, h * .1f, p); c.drawRect(0, h * .9f, w, h, p);
        text(c, p, name, w / 2f, h * .96f, 20 * u, 0xFFFFFFFF);
    }
}
