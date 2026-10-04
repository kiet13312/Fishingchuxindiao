package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    GameView g;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        g = new GameView(this);
        setContentView(g);
    }

    @Override protected void onPause() { super.onPause(); g.save(); }

    static class GameView extends View {
        static final String[] FN = {"Cá rô", "Cá chép", "Cá lóc", "Cá mập", "Cá voi", "Cá vạn cân"};
        static final int[] FKG = {30, 80, 200, 2000, 12000, 120000};
        static final String[] ROD = {"Cần Tre", "Cần Sắt", "Cần Thép", "Cần Vàng", "Cần Thần", "Cần Hải Thần"};
        static final int[] RP = {80, 160, 300, 520, 900, 1500}, RC = {0, 1500, 6000, 18000, 50000, 150000};
        static final String[] SK = {"Xuống núi", "Đại ma", "Xe đạp"};
        static final String[] SKN = {"Chàng trai xuống núi", "Đại ma bại trận", "Ông lão đạp xe đạp"};
        static final int[] SC = {40, 60, 80};

        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rnd = new Random();
        final SharedPreferences sp;
        final ArrayList<float[]> hit = new ArrayList<float[]>();
        int scr, rod, baits = 20, invN, phase, fk, kg, ptr = -1, joyPtr = -1, fxWho = -1; // phase: 0 rảnh, 1 chờ cá, 2 đang kéo
        long money = 500, xp, inv, bite, msgT, fxT, lastMs = System.currentTimeMillis();
        int[] sk = {1, 1, 1};
        float hp = 1, hpMax = 1, dist, maxLine = 40, ten, st = 100, u = 1, t, jx, jy, fxDmg, lx = .34f, ly = .90f;
        float[] cd = new float[3], px = {.34f, .26f, .18f}, py = {.90f, .90f, .90f};
        boolean reel;
        String msg = "";

        GameView(Context c) {
            super(c);
            sp = c.getSharedPreferences("fish4", 0);
            money = sp.getLong("m", 500); xp = sp.getLong("x", 0); rod = sp.getInt("r", 0); baits = sp.getInt("b", 20);
            inv = sp.getLong("i", 0); invN = sp.getInt("n", 0);
            for (int i = 0; i < 3; i++) sk[i] = sp.getInt("s" + i, 1);
        }

        void save() {
            sp.edit().putLong("m", money).putLong("x", xp).putInt("r", rod).putInt("b", baits).putLong("i", inv)
                    .putInt("n", invN).putInt("s0", sk[0]).putInt("s1", sk[1]).putInt("s2", sk[2]).apply();
        }

        int lv() { return (int) Math.min(100, 1 + xp / 400); }
        float pw() { return 3f * (RP[rod] + lv() * 6f) + sk[0] + sk[1] + sk[2]; }
        void say(String s) { msg = s; msgT = System.currentTimeMillis() + 2500; }

        void win() {
            phase = 0; reel = false;
            long v = (long) kg * (2 + fk);
            inv += v; invN++; xp += Math.max(5, kg / 10);
            say("Bắt được " + FN[fk] + " " + kg + " kg (+$" + v + ")");
        }

        void skill(int i) {
            if (phase != 2 || cd[i] > 0 || st < SC[i]) { say("Chưa dùng được chiêu"); return; }
            st -= SC[i]; cd[i] = 8;
            float d = pw() * ((i == 0 ? 1.2f : i == 1 ? 2f : .5f) + .05f * sk[i]);
            if (i == 0) dist = Math.max(0, dist - 8);
            if (i == 2) ten = Math.max(5, ten - 30);
            hp -= d;
            fxWho = i; fxT = System.currentTimeMillis(); fxDmg = d;
            if (hp <= 0) win();
        }

        void act(int id, int pt) {
            long now = System.currentTimeMillis();
            if (id == 1) {
                if (phase == 0) {
                    if (baits <= 0) { say("Hết mồi, mua ở Cửa hàng"); return; }
                    baits--;
                    fk = Math.min(5, rnd.nextInt(3) + rod);
                    kg = (int) (FKG[fk] * (.85f + rnd.nextFloat() * .3f));
                    hpMax = hp = 112f * (float) Math.pow(kg, .55);
                    maxLine = 40 + rod * 15; dist = maxLine * .7f; ten = 20;
                    phase = 1; bite = now + 1200 + rnd.nextInt(2500);
                    say("Đã thả lưới...");
                } else if (phase == 2) { reel = true; ptr = pt; }
            } else if (id == 2 && phase == 0) scr = 1;
            else if (id == 3) scr = 0;
            else if (id >= 10 && id < 13) skill(id - 10);
            else if (id >= 20 && id < 23) {
                int i = id - 20;
                long c = 120L * sk[i] * (10 + sk[i]) / 10;
                if (sk[i] < 100 && money >= c) { money -= c; sk[i]++; } else say("Không nâng được");
            } else if (id >= 30 && id < 36) {
                int i = id - 30;
                if (i <= rod) rod = i; else if (money >= RC[i]) { money -= RC[i]; rod = i; } else say("Không đủ tiền");
            } else if (id == 40 && money >= 100) { money -= 100; baits += 10; }
            else if (id == 41) { money += inv; inv = 0; invN = 0; }
            save();
        }

        void joy(float x, float y) {
            float dx = (x - 95 * u) / (60 * u), dy = (y - (getHeight() - 95 * u)) / (60 * u), l = (float) Math.hypot(dx, dy);
            if (l > 1) { dx /= l; dy /= l; }
            jx = dx; jy = dy;
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            int a = e.getActionMasked(), i = e.getActionIndex(), id = e.getPointerId(i);
            if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_POINTER_DOWN) {
                float x = e.getX(i), y = e.getY(i);
                if (scr == 0 && Math.hypot(x - 95 * u, y - (getHeight() - 95 * u)) < 80 * u) { joyPtr = id; joy(x, y); return true; }
                for (int k = hit.size() - 1; k >= 0; k--) {
                    float[] r = hit.get(k);
                    if (x >= r[0] && x <= r[2] && y >= r[1] && y <= r[3]) { act((int) r[4], id); break; }
                }
            } else if (a == MotionEvent.ACTION_MOVE) {
                for (int k = 0; k < e.getPointerCount(); k++) if (e.getPointerId(k) == joyPtr) joy(e.getX(k), e.getY(k));
            } else if (a == MotionEvent.ACTION_CANCEL || a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_POINTER_UP) {
                if (a == MotionEvent.ACTION_CANCEL || id == joyPtr) { joyPtr = -1; jx = 0; jy = 0; }
                if (a == MotionEvent.ACTION_CANCEL || id == ptr) { reel = false; ptr = -1; }
            }
            return true;
        }

        void update(float dt, long now) {
            // nhân vật dẫn đầu di chuyển, hai người còn lại đi theo thành hàng ngang
            lx = Math.max(.22f, Math.min(.40f, lx + jx * .25f * dt));
            ly = Math.max(.86f, Math.min(.95f, ly + jy * .10f * dt));
            float gap = 75 * u / Math.max(1, getWidth());
            for (int i = 0; i < 3; i++) {
                px[i] += (lx - i * gap - px[i]) * Math.min(1f, 6 * dt);
                py[i] += (ly - py[i]) * Math.min(1f, 6 * dt);
            }
            for (int i = 0; i < 3; i++) cd[i] = Math.max(0, cd[i] - dt);
            st = Math.min(150 + lv() * 5, st + (phase == 2 ? 4 : 8) * dt);
            if (phase == 1 && now >= bite) { phase = 2; dist = maxLine * .7f; ten = 30; say("CÁ CẮN! Giữ CO LẠI ĐÂY"); }
            if (phase != 2) return;
            float s = (.8f + kg / 60000f) * (1 + .2f * (float) Math.sin(t * 6));
            if (reel) { dist = Math.max(0, dist - (1.5f + pw() / 500) * dt); ten += (10 + s * 6) * dt; hp -= pw() * dt; }
            else { dist += s * 1.4f * dt; ten -= 14 * dt; hp = Math.min(hpMax, hp + hpMax * .01f * dt); }
            ten = Math.max(5 + 60 * dist / maxLine, Math.min(100, ten));
            if (dist > maxLine) { phase = 0; reel = false; say("Cá thoát mất!"); }
            else if (ten >= 99.5f) { phase = 0; reel = false; say("Dây đứt rồi!"); }
            else if (hp <= 0 || dist < .3f) win();
        }

        void tx(Canvas c, String s, float x, float y, float sz, int col, boolean mid) {
            p.setStyle(Paint.Style.FILL); p.setTextSize(sz * u); p.setColor(col);
            c.drawText(s, mid ? x - p.measureText(s) / 2 : x, y, p);
        }

        void box(Canvas c, float l, float tp, float r, float b, int col) {
            p.setStyle(Paint.Style.FILL); p.setColor(col); c.drawRoundRect(l, tp, r, b, 12 * u, 12 * u, p);
        }

        void bar(Canvas c, float l, float tp, float r, float b, float f, int col) {
            box(c, l, tp, r, b, 0xFF2D3840);
            box(c, l, tp, l + (r - l) * Math.max(0f, Math.min(1f, f)), b, col);
        }

        void btn(Canvas c, int id, String s, float l, float tp, float r, float b, boolean on) {
            hit.add(new float[]{l, tp, r, b, id});
            box(c, l, tp, r, b, on ? 0xFFF2B931 : 0xFF32495A);
            tx(c, s, (l + r) / 2, (tp + b) / 2 + 5 * u, 14, on ? 0xFF1D2B33 : 0xFFFFFFFF, true);
        }

        @Override protected void onDraw(Canvas c) {
            long now = System.currentTimeMillis();
            float dt = Math.min(.05f, (now - lastMs) / 1000f);
            lastMs = now;
            int w = getWidth(), h = getHeight();
            u = h / 540f; t += dt; hit.clear();
            if (scr == 0) { update(dt, now); fishing(c, w, h, now); } else shop(c, w, h);
            if (now < msgT) {
                box(c, w * .2f, h * .22f + 60 * u, w * .8f, h * .22f + 100 * u, 0xD911181D);
                tx(c, msg, w / 2f, h * .22f + 86 * u, 15, 0xFFFFFFFF, true);
            }
            postInvalidateOnAnimation();
        }

        void fishing(Canvas c, int w, int h, long now) {
            float k = fxWho >= 0 ? (now - fxT) / 1800f : 0f;
            c.save();
            if (k > .45f && k < .7f) c.translate((rnd.nextFloat() - .5f) * 18 * u, (rnd.nextFloat() - .5f) * 18 * u); // rung màn hình
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFFBFE6F5); c.drawRect(0, 0, w, h * .45f, p);
            p.setColor(0xFF2A8BB0); c.drawRect(0, h * .45f, w, h, p);
            p.setColor(0xFFCBB899); c.drawRect(0, h * .82f, w * .46f, h, p);
            float fx = w * (.46f + .44f * (phase == 2 ? dist / maxLine : .55f)), fy = h * (.6f + .04f * (float) Math.sin(t * 1.7f));
            for (int i = 2; i >= 0; i--) { // hai người theo vẽ trước, người dẫn đầu vẽ sau cùng
                float x = w * px[i], y = h * py[i];
                Fx.person(c, p, x, y, u, i);
                if (phase > 0) { p.setStrokeWidth(1.5f * u); p.setColor(0xDDFFFFFF); c.drawLine(x + 85 * u, y - 165 * u, fx, fy + i * 6 * u, p); }
                tx(c, Fx.NAME[i] + (i == 0 ? " (dẫn đầu)" : ""), x, y - 122 * u, 11, 0xFF1D2B33, true);
            }
            if (phase == 1) { p.setColor(0xFFE5413A); c.drawCircle(fx, fy, 7 * u, p); }
            if (phase == 2) {
                float s = u * (.6f + .9f * (float) Math.sqrt(kg / 120000f));
                p.setColor(0xFF6843A5);
                c.drawOval(fx - 40 * s, fy - 17 * s, fx + 36 * s, fy + 17 * s, p);
                c.drawCircle(fx + 50 * s, fy, 14 * s, p);
                tx(c, FN[fk] + " " + kg + " kg", fx, fy - 70 * u, 13, 0xFFFFFFFF, true);
                bar(c, fx - 90 * u, fy - 62 * u, fx + 90 * u, fy - 50 * u, hp / hpMax, 0xFFEF564C);
                tx(c, (int) Math.max(0, hp) + " / " + (int) hpMax, fx, fy - 38 * u, 11, 0xFFFFFFFF, true);
            }
            if (fxWho >= 0) {
                if (k >= 1f) fxWho = -1; else Fx.draw(c, p, w, h, u, k, fxWho, SKN[fxWho], fx, fy, fxDmg);
            }
            c.restore();
            tx(c, "Lv " + lv() + "  $" + money + "  Mồi " + baits + "  Kho " + invN + " cá", 16 * u, 30 * u, 15, 0xFF1D2B33, false);
            bar(c, 16 * u, 42 * u, 216 * u, 54 * u, st / (150 + lv() * 5), 0xFFE6463C);
            tx(c, "Thể lực " + (int) st, 20 * u, 52 * u, 10, 0xFFFFFFFF, false);
            bar(c, w / 2f - 150 * u, h - 125 * u, w / 2f + 150 * u, h - 111 * u, ten / 100f, ten > 84 ? 0xFFFF4040 : 0xFF45D687);
            tx(c, "CĂNG DÂY " + (int) ten + "%", w / 2f, h - 130 * u, 11, 0xFF1D2B33, true);
            for (int i = 0; i < 3; i++) {
                float l = w / 2f - 215 * u + i * 145 * u;
                btn(c, 10 + i, SK[i] + (cd[i] > 0 ? " " + (int) Math.ceil(cd[i]) + "s" : " -" + SC[i]), l, h - 95 * u, l + 135 * u, h - 35 * u, phase == 2 && cd[i] <= 0 && st >= SC[i]);
            }
            if (phase == 0) btn(c, 2, "CỬA HÀNG", w - 190 * u, 10 * u, w - 10 * u, 56 * u, false);
            float jcx = 95 * u, jcy = h - 95 * u;
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3 * u); p.setColor(0xAAFFFFFF); c.drawCircle(jcx, jcy, 60 * u, p);
            p.setStyle(Paint.Style.FILL); p.setColor(0xCCF2B931); c.drawCircle(jcx + jx * 40 * u, jcy + jy * 40 * u, 22 * u, p);
            float rx = w - 110 * u, ry = h - 140 * u;
            hit.add(new float[]{rx - 68 * u, ry - 68 * u, rx + 68 * u, ry + 68 * u, 1});
            p.setColor(reel ? 0xFFF2B931 : 0xFF264A53); c.drawCircle(rx, ry, 68 * u, p);
            tx(c, phase == 0 ? "THẢ LƯỚI" : phase == 1 ? "ĐANG CHỜ" : "CO LẠI ĐÂY", rx, ry + 5 * u, 15, reel ? 0xFF1D2B33 : 0xFFFFFFFF, true);
        }

        void shop(Canvas c, int w, int h) {
            p.setStyle(Paint.Style.FILL); p.setColor(0xFF0E1A22); c.drawRect(0, 0, w, h, p);
            tx(c, "CỬA HÀNG   $" + money, 24 * u, 44 * u, 22, 0xFFF2B931, false);
            float cw = (w - 60 * u) / 3f;
            for (int i = 0; i < 6; i++) {
                float l = 20 * u + i % 3 * (cw + 10 * u), tp = 70 * u + i / 3 * 80 * u;
                btn(c, 30 + i, ROD[i] + " " + (i == rod ? "(đang dùng)" : i < rod ? "(dùng)" : "$" + RC[i]), l, tp, l + cw, tp + 66 * u, i <= rod || money >= RC[i]);
            }
            for (int i = 0; i < 3; i++) {
                float l = 20 * u + i * (cw + 10 * u);
                long cost = 120L * sk[i] * (10 + sk[i]) / 10;
                btn(c, 20 + i, SKN[i] + " Lv" + sk[i] + (sk[i] >= 100 ? " ĐẦY CẤP" : " nâng $" + cost), l, 250 * u, l + cw, 330 * u, sk[i] < 100 && money >= cost);
            }
            btn(c, 40, "Mua 10 mồi $100 (có " + baits + ")", 20 * u, 360 * u, 20 * u + cw, 420 * u, money >= 100);
            btn(c, 41, "Bán " + invN + " cá $" + inv, 30 * u + cw, 360 * u, 30 * u + 2 * cw, 420 * u, invN > 0);
            btn(c, 3, "VỀ", 40 * u + 2 * cw, 360 * u, 40 * u + 3 * cw, 420 * u, false);
        }
    }
}
