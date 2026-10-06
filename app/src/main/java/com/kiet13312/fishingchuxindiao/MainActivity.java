package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Random;

// Toàn bộ game nằm trong file này (GameView, Fx hiệu ứng chiêu, Snd âm thanh). Các file GameView.java, Fx.java, Snd.java cũ có thể xóa.
public class MainActivity extends Activity {
    GameView g;

    static String trace(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final SharedPreferences sp0 = getSharedPreferences("fish5", 0);
        final Thread.UncaughtExceptionHandler old = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread t, Throwable e) {
                try { sp0.edit().putString("crash", trace(e)).commit(); } catch (Throwable x) { }
                if (old != null) old.uncaughtException(t, e);
            }
        });
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        g = new GameView(this);
        setContentView(g);
    }

    @Override protected void onPause() { super.onPause(); if (g != null) g.save(); }



    static class GameView extends View {
        static final int LOBBY = 0, CHAR = 1, MAPS = 2, TALK = 3, UPG = 4, FISH = 5;
        static final String[] CH = {"Trương Tinh", "Trần Bạch Cương", "Em họ", "Em trai", "Thương Không", "Ngất ngọt", "Công cô câu", "Bắc Mộng", "Thiên quốc", "Ông Cương", "Nam Khổng", "Hào Đảo Đế"};
        static final int[] CLV = {1, 15, 60, 1, 16, 60, 1, 1, 60, 1, 0, 0};
        static final String[] MP = {"Bản đập Pá Đất", "Nước thải ô nhiễm", "Hắc Hổ", "Địa Đồ Lễ Hội", "Ngũ Hồ Sơn Lợi", "Thôn Quái", "Quán sau Nam Cương", "Bờ Biển", "Trường Bạch Sơn"};
        static final String[] MF = {"Ngựa lưng chừng", "Shark biển đỏ", "Cá chép tai bạc", "Cá lễ hội vàng", "Cá rồng hình rồng", "Ây ngư âm", "Kún", "Thủy quái bờ biển", "Cá Chép Râu Bạc"};
        static final int[] MW = {30, 200, 2000, 5000, 12000, 50000, 120000, 500000, 1500000}, MLV = {1, 10, 20, 30, 40, 55, 70, 85, 95};
        static final int[] MC = {0xFF2FA3B3, 0xFF4E9A5E, 0xFF2A6FA0, 0xFF7A4FB0, 0xFF3A7FD0, 0xFF4A5C7A, 0xFF1B4F72, 0xFF0F6FA8, 0xFF8FB8C8};
        static final String[] ROD = {"Cần Tre", "Cần Sắt", "Cần Thép", "Cần Thép Gân", "Cần Vàng", "Cần Thần", "Cần Hải Thần", "Đao Long Ấn"};
        static final int[] RP = {80, 160, 300, 520, 900, 1500, 2600, 4500}, RC = {0, 1500, 6000, 18000, 50000, 150000, 500000, 1500000};
        static final String[] SKN = {
                "Ổn Như Lão Cẩu Chi Điếu", "Mã Đạt Điếu Pháp", "Các Bà Cô Thất Bại",
                "Chàng Trai Xuống Núi", "Quay Đầu Móc", "Lão Hán Đạp Đơn Xa",
                "Phi Thiên Vô Cực", "Song Long Xuất Thải", "Tề Thiên Đại Điếu Pháp",
                "Ga Chống Đè Trứng", "Hoành Tảo Thiên Quân", "Câu Long Quyển Hổ",
                "Khỉ Chui Trời", "Thiên Đề Đánh Một Gậy", "Thí Thần Điếu",
                "Nhất Điếu Khai Thiên Môn", "Phá Phủ Trầm Chu", "Sở Tân Câu"};
        static final int[] SC = {40,45,55,60,70,80,90,100,110,120,130,140,150,160,170,180,190,200};
        static final String TALK_TXT = "Thằng cá độ đáng ghét, dám bắt cá của tôi ở bên kia, hôm nay dù anh là ai thì cũng không dễ anh chạy trốn được.";

        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path path = new Path();
        final Random rnd = new Random();
        final SharedPreferences sp;
        final ArrayList<float[]> hit = new ArrayList<float[]>();
        int scr, map, sel, tab, rod, baits = 20, invN, phase, kg, ptr = -1, joyPtr = -1, fxWho = -1, unl = 0x3FF, eqN; // phase: 0 rảnh, 1 chờ cá, 2 đang kéo
        int[] sk = {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, team = {0, 1, 2}, eq = {0, 1, -1};
        long money = 500, xp, inv, bite, msgT, fxT, talkT, hitT, lastMs = System.currentTimeMillis();
        float hp = 1, hpMax = 1, dist, maxLine = 40, ten, st = 150, u = 1, t, jx, jy, fxDmg, hitD, zoom = 1, lx = .34f, ly = .88f;
        float[] cd = new float[18], px = {.34f, .26f, .18f}, py = {.88f, .88f, .88f};
        boolean reel, spot, gift, fxSnd;
        String msg = "", err;

        GameView(Context c) {
            super(c);
            sp = c.getSharedPreferences("fish5", 0);
            Fx.initTT();
            money = sp.getLong("m", 500); xp = sp.getLong("x", 0); rod = sp.getInt("r", 0); baits = sp.getInt("b", 20);
            inv = sp.getLong("i", 0); invN = sp.getInt("n", 0); unl = sp.getInt("u", 0x3FF); gift = sp.getBoolean("g", false);
            for (int i = 0; i < 18; i++) sk[i] = sp.getInt("s" + i, i < 2 ? 1 : 0);
            for (int i = 0; i < 3; i++) { team[i] = sp.getInt("t" + i, i); eq[i] = sp.getInt("e" + i, i < 2 ? i : -1); }
            err = sp.getString("crash", null);
            if (err != null) sp.edit().remove("crash").apply();
        }

        void save() {
            SharedPreferences.Editor e = sp.edit();
            e.putLong("m", money).putLong("x", xp).putInt("r", rod).putInt("b", baits).putLong("i", inv).putInt("n", invN).putInt("u", unl).putBoolean("g", gift);
            for (int i = 0; i < 18; i++) e.putInt("s" + i, sk[i]);
            for (int i = 0; i < 3; i++) { e.putInt("t" + i, team[i]); e.putInt("e" + i, eq[i]); }
            e.apply();
        }

        long cum(int n) { return (long) (n - 1) * (100 + 20 * n); }
        int lv() { int l = 1; while (l < 100 && cum(l + 1) <= xp) l++; return l; }
        long need() { return 100 + 40L * lv(); }
        int maxSt() { return 150 + lv() * 5; }
        float pw() { float s = 3f * (RP[rod] + lv() * 6f) + (rod == 7 ? 1500 : 0); for (int i = 0; i < 18; i++) s += sk[i]; for (int i = 0; i < 3; i++) s += CLV[team[i]] * 2; return s; }
        float sm(int i) { return 1.15f + .16f * i + .04f * sk[i]; }
        long uc(int i) { return sk[i] == 0 ? 1500L * (i + 1) * (i + 1) : 120L * sk[i] * (10 + sk[i]) / 10 * (i + 1); }
        boolean inTeam(int c) { return team[0] == c || team[1] == c || team[2] == c; }
        void say(String s) { msg = s; msgT = System.currentTimeMillis() + 2500; }

        void win() {
            phase = 0; reel = false;
            long v = (long) kg * (2 + map);
            inv += v; invN++; xp += 15 + kg / 3; Snd.play(Snd.WIN);
            say("Bắt được " + MF[map] + " " + kg + " lạng (+$" + v + ")");
        }

        void skill(int slot) {
            int i = eq[slot];
            if (i < 0) return;
            if (phase != 2 || cd[i] > 0 || st < SC[i]) { say("Chưa dùng được chiêu"); return; }
            st -= SC[i]; cd[i] = 8;
            float d = pw() * sm(i);
            if (i % 3 == 0) dist = Math.max(0, dist - 8);
            if (i % 3 == 2) ten = Math.max(5, ten - 30);
            hp -= d;
            fxWho = i; fxT = System.currentTimeMillis(); fxDmg = d; fxSnd = false; Snd.play(Snd.WHOOSH);
            if (hp <= 0) win();
        }

        void act(int id, int pt) {
            long now = System.currentTimeMillis();
            if (id == 160) err = null;
            else if (id == 1) {
                if (phase == 0) {
                    if (baits <= 0) { say("Hết mồi, mua ở Cần câu & mồi"); return; }
                    baits--;
                    kg = (int) (MW[map] * (.85f + rnd.nextFloat() * .3f));
                    hpMax = hp = 112f * (float) Math.pow(kg, .55);
                    maxLine = 40 + rod * 15 + map * 6; dist = maxLine * .7f; ten = 20;
                    phase = 1; spot = true; bite = now + 1200 + rnd.nextInt(2500);
                    say("Đã thả lưới..."); Snd.play(Snd.CAST);
                } else if (phase == 2) { reel = true; ptr = pt; }
            } else if (id == 3) { if (phase == 0) scr = LOBBY; }
            else if (id >= 10 && id < 13) skill(id - 10);
            else if (id >= 300 && id < 308) {
                int i = id - 300;
                if (i <= rod) rod = i; else if (money >= RC[i]) { money -= RC[i]; rod = i; } else say("Không đủ tiền");
            } else if (id >= 20 && id < 38) {
                int i = id - 20;
                long cost = uc(i);
                if (sk[i] >= 100) say("Đã mãn cấp"); else if (money >= cost) { money -= cost; sk[i]++; } else say("Không đủ tiền");
            } else if (id == 40) { if (money >= 100) { money -= 100; baits += 10; } else say("Không đủ tiền"); }
            else if (id == 41 || id == 63) { if (invN == 0) say("Kho đang trống"); else { money += inv; say("Đã bán cá +$" + inv); inv = 0; invN = 0; } }
            else if (id == 150) scr = LOBBY;
            else if (id == 60) scr = CHAR;
            else if (id == 61) { scr = UPG; tab = 0; }
            else if (id == 62) { scr = UPG; tab = 1; }
            else if (id == 70) { if (!gift) { gift = true; money += 800000; say("Nhận phúc lợi +800000"); } else say("Đã nhận rồi"); }
            else if (id == 71) scr = MAPS;
            else if (id == 80) {
                if (inTeam(sel)) {
                    for (int c = 0; c < 12; c++) if ((unl >> c & 1) == 1 && !inTeam(c)) { for (int j = 0; j < 3; j++) if (team[j] == sel) team[j] = c; break; }
                } else team[eqN++ % 3] = sel;
            } else if (id == 81) { if (money >= 50000) { money -= 50000; unl |= 1 << sel; } else say("Cần $50000 để mở khóa"); }
            else if (id >= 100 && id < 112) sel = id - 100;
            else if (id >= 200 && id < 209) {
                if (lv() >= MLV[id - 200]) { map = id - 200; scr = TALK; talkT = now; } else say("Cần đạt Lv " + MLV[id - 200] + " mới vào được");
            } else if (id == 120) { scr = FISH; phase = 0; spot = false; }
            else if (id == 130 || id == 131) tab = id - 130;
            else if (id >= 140 && id < 158) {
                int i = id - 140, slot = -1;
                for (int j = 0; j < 3; j++) if (eq[j] == i) slot = j;
                if (sk[i] == 0) say("Chưa mở khóa");
                else if (slot >= 0) eq[slot] = -1;
                else { int f = -1; for (int j = 0; j < 3; j++) if (eq[j] < 0) { f = j; break; } if (f < 0) f = eqN++ % 3; eq[f] = i; }
            }
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
                if (scr == FISH && Math.hypot(x - 95 * u, y - (getHeight() - 95 * u)) < 80 * u) { joyPtr = id; joy(x, y); return true; }
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
            if (phase == 0 && (Math.abs(jx) > .1f || Math.abs(jy) > .1f)) spot = false;
            lx = Math.max(.14f, Math.min(.46f, lx + jx * .25f * dt));
            ly = Math.max(.68f, Math.min(.92f, ly + jy * .10f * dt));
            float gap = 75 * u / Math.max(1, getWidth());
            for (int i = 0; i < 3; i++) {
                float tx = spot ? .50f - i * 1.5f * gap : lx - i * gap, ty = spot ? .82f : ly;
                px[i] += (tx - px[i]) * Math.min(1f, 5 * dt);
                py[i] += (ty - py[i]) * Math.min(1f, 5 * dt);
            }
            zoom += ((phase == 2 ? 1.22f : 1f) - zoom) * Math.min(1f, 3 * dt);
            for (int i = 0; i < 18; i++) cd[i] = Math.max(0, cd[i] - dt);
            st = Math.min(maxSt(), st + (phase == 2 ? 4 : 8) * dt);
            if (phase == 1 && now >= bite) { phase = 2; dist = maxLine * .7f; ten = 30; say("CÁ CẮN! Giữ CO LẠI ĐÂY"); Snd.play(Snd.BITE); }
            if (phase != 2) return;
            float s = (.8f + Math.min(3f, kg / 60000f)) * (1 + .2f * (float) Math.sin(t * 6));
            if (reel) {
                dist = Math.max(0, dist - (1.5f + pw() / 500) * dt); ten += (10 + s * 6) * dt; hp -= pw() * dt;
                if (now - hitT > 300) { hitT = now; hitD = pw() * .3f; Snd.play(Snd.TICK); }
            } else { dist += s * 1.4f * dt; ten -= 14 * dt; hp = Math.min(hpMax, hp + hpMax * .01f * dt); }
            ten = Math.max(5 + 60 * dist / maxLine, Math.min(100, ten));
            if (dist > maxLine) { phase = 0; reel = false; say("Cá thoát mất!"); Snd.play(Snd.LOSE); }
            else if (ten >= 99.5f) { phase = 0; reel = false; say("Dây đứt rồi!"); Snd.play(Snd.LOSE); }
            else if (hp <= 0 || dist < .3f) win();
        }

        // ---------------- vẽ ----------------
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
            tx(c, s, (l + r) / 2, (tp + b) / 2 + 5 * u, 13, on ? 0xFF1D2B33 : 0xFFFFFFFF, true);
        }

        @Override protected void onDraw(Canvas c) {
            try {
                if (err != null) errScreen(c); else frame(c);
            } catch (Throwable e) { err = MainActivity.trace(e); scr = LOBBY; phase = 0; reel = false; fxWho = -1; }
            postInvalidateOnAnimation();
        }

        void errScreen(Canvas c) {
            int w = getWidth(), h = getHeight();
            u = h / 540f; hit.clear();
            p.setStyle(Paint.Style.FILL); p.setColor(0xFF200808); c.drawRect(0, 0, w, h, p);
            tx(c, "Game gặp lỗi - chụp màn hình này gửi cho Claude", 20 * u, 36 * u, 16, 0xFFFFD27A, false);
            String[] ln = err.split("\n");
            for (int i = 0; i < ln.length && i < 14; i++) tx(c, ln[i].length() > 110 ? ln[i].substring(0, 110) : ln[i], 20 * u, 64 * u + i * 22 * u, 11, 0xFFFFFFFF, false);
            btn(c, 160, "Tiếp tục", w - 220 * u, h - 70 * u, w - 20 * u, h - 20 * u, true);
        }

        void frame(Canvas c) {
            long now = System.currentTimeMillis();
            float dt = Math.min(.05f, (now - lastMs) / 1000f);
            lastMs = now;
            int w = getWidth(), h = getHeight();
            u = h / 540f; t += dt; hit.clear();
            p.setStyle(Paint.Style.FILL); p.setColor(0xFF0E1A22); c.drawRect(0, 0, w, h, p);
            if (scr == FISH) { update(dt, now); fishing(c, w, h, now); }
            else if (scr == LOBBY) lobby(c, w, h);
            else if (scr == CHAR) chars(c, w, h);
            else if (scr == MAPS) maps(c, w, h);
            else if (scr == TALK) talk(c, w, h, now);
            else upg(c, w, h);
            if (now < msgT) {
                box(c, w * .2f, h * .22f + 60 * u, w * .8f, h * .22f + 100 * u, 0xD911181D);
                tx(c, msg, w / 2f, h * .22f + 86 * u, 15, 0xFFFFFFFF, true);
            }
            postInvalidateOnAnimation();
        }

        void hud(Canvas c, int w, int h) {
            box(c, 10 * u, 10 * u, 262 * u, 122 * u, 0xBF0C141C);
            tx(c, CH[team[0]], 22 * u, 32 * u, 14, 0xFFFFFFFF, false);
            tx(c, "Lv " + lv(), 22 * u, 52 * u, 12, 0xFFFFFFFF, false);
            long cur = xp - cum(lv()), nd = need();
            bar(c, 70 * u, 42 * u, 250 * u, 54 * u, (float) cur / nd, 0xFF5AAAFF);
            tx(c, cur + "/" + nd, 74 * u, 52 * u, 9, 0xFFFFFFFF, false);
            tx(c, "Thể lực", 22 * u, 72 * u, 12, 0xFFFFFFFF, false);
            bar(c, 70 * u, 62 * u, 250 * u, 74 * u, st / maxSt(), 0xFFE6463C);
            tx(c, (int) st + "/" + maxSt(), 74 * u, 72 * u, 9, 0xFFFFFFFF, false);
            tx(c, "Tổng trọng cá câu được: " + inv + " lạng", 22 * u, 96 * u, 10, 0xFFF2B931, false);
            tx(c, "Đồng cấp VIP  $" + money, w / 2f, 28 * u, 15, 0xFFF2B931, true);
            String[] mn = {"Người bạn câu cá", "Cách đánh cá", "Cây cần", "Quán cá"};
            for (int i = 0; i < 4; i++) btn(c, 60 + i, mn[i], 10 * u, 135 * u + i * 52 * u, 170 * u, 179 * u + i * 52 * u, phase == 0);
        }

        void lobby(Canvas c, int w, int h) {
            p.setColor(0xFF22262E); c.drawRect(0, 0, w, h, p);
            p.setColor(0xFF3A3D45); c.drawRect(0, h * .55f, w, h, p);
            p.setColor(0xFFEDEDED);
            for (int i = 0; i < 24; i++) c.drawRect(w * i / 24f + 4 * u, h * .30f, w * i / 24f + 10 * u, h * .50f, p);
            c.drawRect(0, h * .30f, w, h * .33f, p);
            float cx = w * .45f, cy = h * .76f;
            p.setColor(0xFF7A5230); c.drawRect(cx - 130 * u, cy - 70 * u, cx + 30 * u, cy - 10 * u, p);
            p.setColor(0xFF2E8B8B); c.drawRoundRect(cx + 30 * u, cy - 70 * u, cx + 150 * u, cy - 30 * u, 10 * u, 10 * u, p);
            p.setColor(0xFF111111); c.drawCircle(cx - 90 * u, cy, 36 * u, p); c.drawCircle(cx + 20 * u, cy, 30 * u, p);
            Fx.person(c, p, cx - 100 * u, cy - 40 * u, u * .8f, team[2]);
            Fx.person(c, p, cx - 50 * u, cy - 40 * u, u * .8f, team[1]);
            Fx.person(c, p, cx + 70 * u, cy - 40 * u, u * .8f, team[0]);
            hud(c, w, h);
            btn(c, 0, "Bảng xếp hạng", w - 190 * u, 70 * u, w - 10 * u, 112 * u, false);
            btn(c, 0, "Chợ giao dịch", w - 190 * u, 122 * u, w - 10 * u, 164 * u, false);
            btn(c, 0, "Phòng chat", w - 190 * u, 174 * u, w - 10 * u, 216 * u, false);
            btn(c, 70, gift ? "Phúc lợi: đã nhận" : "Phúc lợi chơi game 800,000", 10 * u, h - 70 * u, 230 * u, h - 14 * u, !gift);
            p.setColor(0xFFD04A5A); c.drawOval(w - 330 * u, h - 100 * u, w - 200 * u, h - 50 * u, p);
            btn(c, 71, "Đi câu cá ›", w - 220 * u, h - 100 * u, w - 10 * u, h - 40 * u, true);
        }

        void chars(Canvas c, int w, int h) {
            btn(c, 150, "‹ Quay lại trang chủ", 10 * u, 10 * u, 220 * u, 52 * u, false);
            float cw = 120 * u;
            for (int i = 0; i < 12; i++) {
                float l = 20 * u + i % 3 * (cw + 10 * u), tp = 70 * u + i / 3 * 112 * u;
                hit.add(new float[]{l, tp, l + cw, tp + 104 * u, 100 + i});
                box(c, l, tp, l + cw, tp + 104 * u, i == sel ? 0xFF3A4F5E : 0xFF18242C);
                Fx.person(c, p, l + cw / 2, tp + 66 * u, u * .4f, i);
                tx(c, CH[i], l + cw / 2, tp + 82 * u, 10, 0xFFFFFFFF, true);
                tx(c, (inTeam(i) ? "Đã chiến đấu  " : "") + "Lv " + CLV[i], l + cw / 2, tp + 98 * u, 9, inTeam(i) ? 0xFFF2B931 : 0xFFCCCCCC, true);
            }
            float rl = w * .52f;
            box(c, rl, 70 * u, w - 20 * u, h - 30 * u, 0xFF18242C);
            Fx.person(c, p, rl + 110 * u, 330 * u, u * 1.5f, sel);
            tx(c, CH[sel] + "   Lv " + CLV[sel], rl + 230 * u, 110 * u, 18, 0xFFF2B931, false);
            tx(c, "Đua xe đua tốc độ: " + (30 + CLV[sel] * 3), rl + 230 * u, 150 * u, 13, 0xFFFFFFFF, false);
            tx(c, "Giảm phụ cá: " + (160 + CLV[sel] * 8) + "%", rl + 230 * u, 176 * u, 13, 0xFFFFFFFF, false);
            tx(c, "Lực kéo cộng thêm: +" + CLV[sel] * 2, rl + 230 * u, 202 * u, 13, 0xFFFFFFFF, false);
            if ((unl >> sel & 1) == 1) btn(c, 80, inTeam(sel) ? "Hủy tham gia chiến đấu" : "Tham gia chiến đấu", rl + 230 * u, h - 110 * u, w - 40 * u, h - 56 * u, !inTeam(sel));
            else {
                tx(c, "Phải đánh bại " + CH[11], rl + 230 * u, h - 130 * u, 13, 0xFFFF4040, false);
                btn(c, 81, "Mở khóa $50000", rl + 230 * u, h - 110 * u, w - 40 * u, h - 56 * u, money >= 50000);
            }
        }

        void maps(Canvas c, int w, int h) {
            btn(c, 150, "‹ Quay lại trang chủ", 10 * u, 10 * u, 220 * u, 52 * u, false);
            float cw = (w - 80 * u) / 3f;
            for (int i = 0; i < 9; i++) {
                float l = 20 * u + i % 3 * (cw + 20 * u), tp = 60 * u + i / 3 * 155 * u;
                hit.add(new float[]{l, tp, l + cw, tp + 145 * u, 200 + i});
                box(c, l, tp, l + cw, tp + 145 * u, 0xFF18242C);
                p.setColor(0xFF6FBF8A); c.drawRect(l + 8 * u, tp + 30 * u, l + cw - 8 * u, tp + 70 * u, p);
                p.setColor(MC[i]); c.drawRect(l + 8 * u, tp + 70 * u, l + cw - 8 * u, tp + 100 * u, p);
                tx(c, MP[i], l + cw / 2, tp + 22 * u, 13, 0xFFFFFFFF, true);
                tx(c, MF[i] + " " + MW[i] * 8 / 10 + "-" + MW[i] * 12 / 10, l + 8 * u, tp + 116 * u, 10, 0xFFF2B931, false);
                tx(c, lv() >= MLV[i] ? "Chạm để vào" : "Cần đạt Lv " + MLV[i], l + 8 * u, tp + 136 * u, 10, lv() >= MLV[i] ? 0xFF45D687 : 0xFFFF4040, false);
            }
        }

        void talk(Canvas c, int w, int h, long now) {
            lake(c, w, h);
            Fx.person(c, p, w * .22f, h * .70f, u * 1.6f, 10);
            hit.add(new float[]{0, 0, w, h, 120});
            box(c, w * .1f, h * .72f, w * .9f, h * .94f, 0xFFFFFFFF);
            tx(c, "Nam Khổng:", w * .12f, h * .78f, 14, 0xFFD9822B, false);
            tx(c, TALK_TXT.substring(0, (int) Math.min(TALK_TXT.length(), (now - talkT) / 35)), w * .12f, h * .84f, 14, 0xFF1D2B33, false);
            tx(c, "Tap to continue", w * .88f, h * .92f, 11, 0xFF999999, true);
            tx(c, "Hào Đảo Đế canh giữ " + MP[map], w / 2f, 30 * u, 14, 0xFFFFFFFF, true);
        }

        void upg(Canvas c, int w, int h) {
            btn(c, 150, "‹ Quay lại trang chủ", 10 * u, 10 * u, 170 * u, 52 * u, false);
            btn(c, 130, "Phương pháp câu cá", 10 * u, 70 * u, 170 * u, 116 * u, tab == 0);
            btn(c, 131, "Cần câu & mồi", 10 * u, 126 * u, 170 * u, 172 * u, tab == 1);
            tx(c, "$" + money, 190 * u, 40 * u, 16, 0xFFF2B931, false);
            if (tab == 0) {
                float cw = (w - 205 * u) / 6f;
                for (int i = 0; i < 18; i++) {
                    float l = 185 * u + i % 6 * (cw + 6 * u), tp = 52 * u + i / 6 * 158 * u;
                    box(c, l, tp, l + cw, tp + 148 * u, 0xFF18242C);
                    tx(c, SKN[i].length() > 18 ? SKN[i].substring(0, 18) + "…" : SKN[i], l + cw / 2, tp + 19 * u, 9, 0xFFFFFFFF, true);
                    tx(c, sk[i] == 0 ? "Chưa mở khóa" : sk[i] >= 100 ? "Lv 100" : "Lv " + sk[i], l + cw / 2, tp + 38 * u, 10, 0xFFF2B931, true);
                    tx(c, "Thể lực: " + SC[i], l + 6 * u, tp + 57 * u, 8, 0xFFCCCCCC, false);
                    tx(c, "Lực: " + (int) (pw() * sm(i)), l + 6 * u, tp + 73 * u, 8, 0xFFCCCCCC, false);
                    btn(c, 20 + i, sk[i] >= 100 ? "Mãn cấp" : (sk[i] == 0 ? "Mở khóa $" : "Nâng cấp $") + uc(i), l + 5 * u, tp + 82 * u, l + cw - 5 * u, tp + 112 * u, sk[i] < 100 && money >= uc(i));
                    boolean on = eq[0] == i || eq[1] == i || eq[2] == i;
                    btn(c, 140 + i, on ? "Hủy" : "Cấu hình", l + 5 * u, tp + 118 * u, l + cw - 5 * u, tp + 145 * u, sk[i] > 0 && !on);
                }
            } else {
                float cw = (w - 230 * u) / 4f;
                for (int i = 0; i < 8; i++) {
                    float l = 185 * u + i % 4 * (cw + 6 * u), tp = 70 * u + i / 4 * 90 * u;
                    btn(c, 300 + i, ROD[i] + " " + (i == rod ? "(đang dùng)" : i < rod ? "(dùng)" : "$" + RC[i]), l, tp, l + cw, tp + 76 * u, i <= rod || money >= RC[i]);
                }
                btn(c, 40, "Mua 10 mồi $100 (có " + baits + ")", 185 * u, 280 * u, 185 * u + 2 * cw, 340 * u, money >= 100);
                btn(c, 41, "Bán " + invN + " cá $" + inv, 197 * u + 2 * cw, 280 * u, 191 * u + 4 * cw, 340 * u, invN > 0);
            }
        }

        void lake(Canvas c, int w, int h) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(0xFF2E6B3E); c.drawRect(-w, -h, 2 * w, h * .28f, p);
            for (int i = 0; i < 24; i++) {
                p.setColor(i % 3 == 0 ? 0xFFD9822B : i % 3 == 1 ? 0xFF3F8F4A : 0xFFE0C23A);
                c.drawCircle(w * (i + .5f) / 24f, h * .22f + i % 2 * 12 * u, (26 + i % 4 * 6) * u, p);
            }
            p.setColor(MC[map]); c.drawRect(-w, h * .28f, 2 * w, 2 * h, p);
            p.setColor(0xFFEBDDB8); path.reset(); path.moveTo(-w, h * .60f); path.lineTo(w * .52f, h * .80f); path.lineTo(w * .52f, 2 * h); path.lineTo(-w, 2 * h); path.close(); c.drawPath(path, p);
            p.setColor(0xFF8C8C86);
            for (int i = 0; i < 8; i++) c.drawRoundRect(w * (.30f + .045f * i), h * (.76f + .01f * i), w * (.30f + .045f * i) + 40 * u, h * (.76f + .01f * i) + 16 * u, 6 * u, 6 * u, p);
        }

        void chain(Canvas c, float x0, float y0, float x1, float y1) {
            p.setStyle(Paint.Style.FILL);
            for (int i = 0; i <= 16; i++) {
                float q = i / 16f;
                p.setColor(i % 2 == 0 ? 0xFF202428 : 0xFFE8E8E8);
                c.drawCircle(x0 + (x1 - x0) * q, y0 + (y1 - y0) * q + (float) Math.sin(q * 3.14f) * (8 + ten * .2f) * u, 2.6f * u, p);
            }
        }

        void fishing(Canvas c, int w, int h, long now) {
            float k = fxWho >= 0 ? (now - fxT) / 1800f : 0f;
            c.save();
            if (k > .45f && k < .7f) c.translate((rnd.nextFloat() - .5f) * 18 * u, (rnd.nextFloat() - .5f) * 18 * u);
            c.scale(zoom, zoom, w * .45f, h * .8f);
            lake(c, w, h);
            float fx = w * (.56f + .36f * (phase == 2 ? dist / maxLine : .55f)), fy = h * (.55f + .03f * (float) Math.sin(t * 1.7f));
            Fx.ttMode = phase == 2
                    ? (reel ? 3 : 2)
                    : ((Math.abs(jx) > .1f || Math.abs(jy) > .1f) ? 1 : 0);
            for (int i = 2; i >= 0; i--) {
                float x = w * px[i], y = h * py[i];
                float bend = (phase == 2 ? Math.min(1f, ten / 100f) : 0f);
                c.save();
                c.rotate(-bend * 13f, x, y);
                Fx.person(c, p, x, y, u * .8f, team[i]);
                c.restore();
                tx(c, CH[team[i]], x, y - 104 * u, 10, 0xFFFFFFFF, true);
                if (phase > 0) {
                    float pull = phase == 2 ? Math.min(1f, ten / 100f) : 0f;
                    float sx = x + 68 * u, sy = y - 132 * u;
                    float mx = (sx + fx) * .5f;
                    float my = (sy + fy + i * 6 * u) * .5f - pull * 55 * u;
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth((1.5f + pull * 2.5f) * u);
                    p.setColor(0xFFDCE6EA);
                    path.reset(); path.moveTo(sx, sy);
                    path.quadTo(mx, my, fx, fy + i * 6 * u);
                    c.drawPath(path, p);
                    p.setStyle(Paint.Style.FILL);
                }
            }
            if (phase == 1) { p.setColor(0xFFE5413A); c.drawCircle(fx, fy, 7 * u, p); }
            if (phase == 2) {
                float s = u * (.6f + .9f * (float) Math.sqrt(Math.min(1f, kg / 120000f)));
                p.setColor(0xFF6843A5);
                c.drawOval(fx - 40 * s, fy - 17 * s, fx + 36 * s, fy + 17 * s, p);
                c.drawCircle(fx + 50 * s, fy, 14 * s, p);
                if (now - hitT < 500) Fx.text(c, p, "-" + Fx.fmt((long) hitD), fx + 40 * u, fy - 50 * u - (now - hitT) * .08f * u, 20 * u, 0xFFFF5577);
            }
            if (fxWho >= 0) {
                if (k >= 1f) fxWho = -1;
                else {
                    if (k > .45f && !fxSnd) { fxSnd = true; Snd.play(Snd.BOOM); }
                    Fx.draw(c, p, w, h, u, k, fxWho, SKN[fxWho], fx, fy, fxDmg, team[0]);
                }
            }
            c.restore();
            hud(c, w, h);
            if (phase == 2) {
                tx(c, MF[map] + " • " + kg + " lạng", w / 2f, 60 * u, 13, 0xFFFFFFFF, true);
                bar(c, w / 2f - 190 * u, 68 * u, w / 2f + 190 * u, 86 * u, hp / hpMax, 0xFFD9303A);
                tx(c, Fx.fmt((long) Math.max(0, hp)) + " / " + Fx.fmt((long) hpMax), w / 2f, 82 * u, 11, 0xFFFFFFFF, true);
                for (int i = 0; i < 24; i++) {
                    float l = w / 2f - 190 * u + i * 16 * u;
                    p.setStyle(Paint.Style.FILL); p.setColor(i < ten * .24f ? (i < 12 ? 0xFF45D687 : i < 19 ? 0xFFF2D230 : 0xFFE6463C) : 0xFF2D3840);
                    c.drawRect(l, 90 * u, l + 14 * u, 100 * u, p);
                }
                if (!reel) tx(c, "KÉO >>", w * .5f, h * .5f, 24, 0x99FFFFFF, true);
                if (hp < hpMax * .35f) tx(c, "Dùng chiêu để kết thúc mẻ cá!", w / 2f, 124 * u, 14, 0xFFFFD02A, true);
            }
            for (int i = 0; i < 3; i++) {
                float l = w / 2f - 215 * u + i * 145 * u;
                int e = eq[i];
                btn(c, 10 + i, e < 0 ? "(trống)" : SKN[e].length() > 14 ? SKN[e].substring(0, 14) + "." : SKN[e], l, h - 95 * u, l + 135 * u, h - 35 * u, e >= 0 && phase == 2 && cd[e] <= 0 && st >= SC[e]);
                if (e >= 0) tx(c, cd[e] > 0 ? (int) Math.ceil(cd[e]) + "s" : "-" + SC[e], l + 67 * u, h - 40 * u, 10, 0xFF1D2B33, true);
            }
            if (phase == 0) btn(c, 3, "Về sảnh", w - 190 * u, 10 * u, w - 10 * u, 52 * u, false);
            box(c, w - 70 * u, h * .26f - 26 * u, w - 10 * u, h * .26f, 0xFF32495A);
            tx(c, "Trang bị", w - 40 * u, h * .26f - 8 * u, 10, 0xFFFFFFFF, true);
            float gx = w - 40 * u, gt = h * .30f, gb = h * .66f, f = phase == 2 ? Math.min(1f, dist / maxLine) : 0f;
            box(c, gx - 8 * u, gt, gx + 8 * u, gb, 0x99000000);
            box(c, gx - 6 * u, gb - (gb - gt) * f, gx + 6 * u, gb, f > .85f ? 0xFFFF4040 : 0xFF45D687);
            tx(c, "Chiều dài cáp câu " + (phase == 2 ? (int) dist : 0) + "m", w - 100 * u, gb + 22 * u, 9, 0xFFFFFFFF, false);
            float jcx = 95 * u, jcy = h - 95 * u;
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3 * u); p.setColor(0xAAFFFFFF); c.drawCircle(jcx, jcy, 60 * u, p);
            p.setStyle(Paint.Style.FILL); p.setColor(0xCCFFFFFF); c.drawCircle(jcx + jx * 40 * u, jcy + jy * 40 * u, 22 * u, p);
            float rx = w - 110 * u, ry = h - 120 * u;
            hit.add(new float[]{rx - 62 * u, ry - 62 * u, rx + 62 * u, ry + 62 * u, 1});
            p.setColor(reel ? 0xFFF2B931 : 0xFF2B2B2B); c.drawCircle(rx, ry, 62 * u, p);
            tx(c, phase == 0 ? "Thả lưới" : phase == 1 ? "Chờ cá" : "Co lại đây", rx, ry + 5 * u, 15, reel ? 0xFF1D2B33 : 0xFFFFFFFF, true);
        }
    }



    static final class Fx {
        static final int[] BODY = {0xFFC2452D, 0xFF3B6EA5, 0xFF8A4A8A, 0xFF3F8F4A, 0xFFD9822B, 0xFF2E8B8B, 0xFFB5338A, 0xFF5B6B7A, 0xFF444444, 0xFF9C6B30, 0xFFEFEFEF, 0xFF7A1F1F};
        static final Path path = new Path();
        static Bitmap TT_BITMAP;
        static int ttMode = 0;

        static void initTT() {
            if (TT_BITMAP == null) TT_BITMAP = TruongTinhAsset.load();
        }

        static void drawTT(Canvas c, Paint p, float x, float y, float s, int frame) {
            if (TT_BITMAP == null) return;
            frame = Math.max(0, Math.min(4, frame));
            Rect src = new Rect(frame * 60, 0, frame * 60 + 60, 100);
            float dh = 160f * s;
            float dw = 96f * s;
            p.setFilterBitmap(true);
            c.drawBitmap(TT_BITMAP, src,
                    new RectF(x - dw / 2f, y - dh, x + dw / 2f, y), p);
            p.setFilterBitmap(false);
        }
        static final int[][] BG = {
                {0xFF6EC6F0,0xFFE8F6D8},{0xFFEDEDED,0xFF9A9AA8},{0xFF4A2A6A,0xFFE8903A},{0xFF2A9AB0,0xFFBFEFF0},
                {0xFF0A5A66,0xFF29E0E8},{0xFF0B1030,0xFF304880},{0xFF300808,0xFFFF7A18},{0xFF081018,0xFF2A5A78},
                {0xFF173B72,0xFF7AE7FF},{0xFF6B3B18,0xFFFFD15A},{0xFF143D2B,0xFF66F0A0},{0xFF18244F,0xFF7C9CFF},
                {0xFF2A173A,0xFFFF76C8},{0xFF4A3310,0xFFFFE48A},{0xFF160F38,0xFFB58CFF},{0xFF173E5C,0xFF8CF7FF},
                {0xFF321A12,0xFFFF8A5C},{0xFF0F3540,0xFF65E9FF}};
        static final int[] MAIN = {0xFFFFD27A,0xFFFFFFFF,0xFFFFE04A,0xFF9FF0FF,0xFF7FF8FF,0xFFB8D8FF,0xFFFF9A2A,0xFF7FE8FF,
                0xFF73D8FF,0xFFFFD15A,0xFF66F0A0,0xFF8FA8FF,0xFFFF76C8,0xFFFFE48A,0xFFB58CFF,0xFF8CF7FF,0xFFFF8A5C,0xFF65E9FF};

        static float rnd(int n) { float x = (float) Math.sin(n * 12.9898) * 43758.547f; return x - (float) Math.floor(x); }
        static float seg(float k, float a, float b) { return Math.max(0f, Math.min(1f, (k - a) / (b - a))); }
        static int al(int col, float a) { return (col & 0xFFFFFF) | ((int) (255 * Math.max(0f, Math.min(1f, a))) << 24); }

        static String fmt(long n) { return String.format(java.util.Locale.US, "%,d", n); }

        static void text(Canvas c, Paint p, String s, float x, float y, float sz, int col) {
            p.setTextSize(sz);
            float xx = x - p.measureText(s) / 2;
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(sz * .16f); p.setColor(0xFF000000); c.drawText(s, xx, y, p);
            p.setStyle(Paint.Style.FILL); p.setColor(col); c.drawText(s, xx, y, p);
        }

        static void person(Canvas c, Paint p, float x, float y, float s, int i) {
            if (i == 0 && TT_BITMAP != null) {
                int frame = ttMode == 1
                        ? 1 + ((int) (System.currentTimeMillis() / 180L) & 1)
                        : (ttMode == 2 ? 3 : (ttMode == 3 ? 4 : 0));
                drawTT(c, p, x, y, s, frame);
                return;
            }
            pose(c, p, x, y, s, i, 56.4f, 136f, 0, 0, 0, 0, false);
        }

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
            float v = Math.min(1f, im * 4f) * Math.max(0f, 1f - im * 1.1f);
            p.setStyle(Paint.Style.FILL); p.setColor(al(0x0A1450, .75f * v)); c.drawOval(fx - 190 * u * v, fy - 110 * u * v, fx + 190 * u * v, fy + 110 * u * v, p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5 * u);
            for (int i = 0; i < 6; i++) {
                float r = (50 + 28 * i) * u * v;
                p.setColor(al(i % 2 == 0 ? 0x3AB8FF : 0x1A4ADC, .85f * v));
                c.drawArc(fx - r * 1.7f, fy - r, fx + r * 1.7f, fy + r, im * 1100 + i * 60, 110, false, p);
            }
            p.setColor(al(0x9FF0FF, .9f * v)); c.drawOval(fx - 210 * u, fy - 120 * u, fx + 210 * u, fy + 120 * u, p);
            p.setStyle(Paint.Style.FILL); p.setColor(al(0x5AB8FF, .9f * v));
            path.reset(); path.moveTo(fx, fy - 70 * u); path.lineTo(fx - 26 * u, fy - 30 * u); path.lineTo(fx + 26 * u, fy - 30 * u); path.close(); c.drawPath(path, p);
            c.drawRect(fx - 9 * u, fy - 30 * u, fx + 9 * u, fy + 30 * u, p);
            float rise = im * 120 * u;
            text(c, p, "-" + fmt((long) dmg), fx, fy - 60 * u - rise, 36 * u, 0xFF9FE0FF);
            text(c, p, "-" + fmt((long) (dmg * .37f)), fx - 90 * u, fy - 20 * u - rise * .7f, 20 * u, 0xFFFF6A8A);
            text(c, p, "-" + fmt((long) (dmg * .21f)), fx + 90 * u, fy - 10 * u - rise * .5f, 18 * u, 0xFFFF6A8A);
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
                case 8: { pose(c,p,cx,cy,S,who,70,155,0,.05f,1,0,true); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(12*u); p.setColor(al(MAIN[sk],.85f)); float r=(100+260*seg(k,.1f,.65f))*u; c.drawOval(cx-r,cy-r*.45f,cx+r,cy+r*.45f,p); break; }
                case 9: { float q=seg(k,0,.45f); pose(c,p,cx,cy,S,who,-30,145,0,.35f*q,1-q,0,true); cracks(c,p,u,cx,cy,seg(k,.25f,.55f)); break; }
                case 10: { pose(c,p,cx,cy,S,who,15,165,0,0,1,0,true); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(18*u); p.setColor(al(MAIN[sk],.8f)); c.drawArc(cx-250*u,cy-180*u,cx+250*u,cy+180*u,-55,110,false,p); if(im>0)c.drawLine(cx+20*S,cy-110*S,fx,fy,p); break; }
                case 11: { pose(c,p,cx,cy,S,who,50,150,0,0,1,0,true); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(8*u); p.setColor(al(MAIN[sk],.9f)); for(int i=0;i<4;i++){float r=(40+i*55+seg(k,0,.8f)*120)*u;c.drawOval(fx-r,fy-r*.55f,fx+r,fy+r*.55f,p);} break; }
                case 12: { float q=seg(k,0,.55f); pose(c,p,cx,cy-q*h*.28f,S,who,-70,150,.1f,0,1,0,true); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(7*u); p.setColor(al(MAIN[sk],.7f)); c.drawLine(cx,cy,cx+q*(fx-cx),cy+q*(fy-cy),p); break; }
                case 13: { pose(c,p,cx,cy,S,who,85,190,0,0,1,0,true); if(k>.3f){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(14*u);p.setColor(al(MAIN[sk],1-seg(k,.7f,1)));c.drawLine(cx+20*S,cy-160*S,fx,fy,p);} break; }
                case 14: { pose(c,p,cx,cy,S,who,35,160,0,0,1,1,true); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5*u);p.setColor(al(MAIN[sk],.9f)); for(int i=0;i<6;i++){float r=(55+i*28+80*k)*u;c.drawCircle(fx,fy,r,p);} break; }
                case 15: { pose(c,p,cx,cy,S,who,-10,175,0,0,1,0,true); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(24*u);p.setColor(al(MAIN[sk],.65f));c.drawLine(fx,0,fx,fy,p);p.setStrokeWidth(5*u);p.setColor(0xFFFFFFFF);c.drawLine(fx,0,fx,fy,p); break; }
                case 16: { pose(c,p,cx,cy,S,who,25,180,-.2f,0,1,1,true); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(22*u);p.setColor(al(MAIN[sk],.65f));c.drawLine(cx+120*S,cy-90*S,fx,fy,p); cracks(c,p,u,fx,fy,seg(k,.35f,.65f)); break; }
                case 17: { pose(c,p,cx,cy,S,who,45,170,0,0,1,1,true); p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(9*u);p.setColor(al(MAIN[sk],.85f)); for(int i=0;i<9;i++){float r=(45+i*24+120*k)*u;c.drawOval(fx-r,fy-r*.5f,fx+r,fy+r*.5f,p);} if(k>.35f)c.drawLine(cx+80*S,cy-100*S,fx,fy,p); break; }
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



    // Âm thanh tự tổng hợp bằng code, không cần file âm thanh
    static final class Snd {
        static final int CAST = 0, BITE = 1, WHOOSH = 2, BOOM = 3, WIN = 4, LOSE = 5, TICK = 6;
        static final int R = 22050;
        static final Random rnd = new Random();
        static final short[][] CACHE = new short[7][];
        static final long[] LAST = new long[7];
        static boolean on = true;
        // mỗi nốt: tần số đầu, tần số cuối, thời gian (ms), độ nhiễu, âm lượng
        static final float[][][] SEQ = {
                {{900, 200, 200, .6f, .5f}},
                {{1200, 1200, 90, 0, .5f}, {1800, 1800, 130, 0, .5f}},
                {{200, 1400, 500, .7f, .5f}},
                {{90, 30, 450, .5f, .9f}},
                {{523, 523, 110, 0, .5f}, {659, 659, 110, 0, .5f}, {784, 784, 110, 0, .5f}, {1047, 1047, 260, 0, .5f}},
                {{400, 120, 450, 0, .5f}},
                {{1500, 1500, 25, .3f, .3f}}};

        static short[] gen(int t) {
            int total = 0;
            for (float[] n : SEQ[t]) total += (int) (R * n[2] / 1000f);
            short[] out = new short[total];
            int pos = 0;
            for (float[] n : SEQ[t]) {
                int len = (int) (R * n[2] / 1000f);
                double ph = 0;
                for (int i = 0; i < len; i++) {
                    float q = i / (float) len, f = n[0] + (n[1] - n[0]) * q;
                    ph += 2 * Math.PI * f / R;
                    float v = (float) Math.sin(ph) * (1 - n[3]) + (rnd.nextFloat() * 2 - 1) * n[3];
                    float env = Math.min(1f, i / 80f) * (1 - q);
                    out[pos++] = (short) (v * env * n[4] * 32000);
                }
            }
            return out;
        }

        static void play(int t) {
            long now = System.currentTimeMillis();
            if (!on || now - LAST[t] < 80) return;
            LAST[t] = now;
            try {
                if (CACHE[t] == null) CACHE[t] = gen(t);
                final short[] d = CACHE[t];
                final AudioTrack a = new AudioTrack(AudioManager.STREAM_MUSIC, R, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, d.length * 2, AudioTrack.MODE_STATIC);
                a.write(d, 0, d.length);
                a.play();
                new Thread(new Runnable() {
                    @Override public void run() {
                        try { Thread.sleep(d.length * 1000L / R + 300); } catch (Exception e) { }
                        a.release();
                    }
                }).start();
            } catch (Exception e) { }
        }
    }
}