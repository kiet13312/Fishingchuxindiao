package com.kiet13312.fishingchuxindiao;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {
    static final int LOBBY = 0, CHAR = 1, MAPS = 2, TALK = 3, UPG = 4, FISH = 5;
    static final String[] CH = {"Trương Tiểu", "Trần Bạch Cương", "Em họ", "Em trai", "Thương Không", "Ngất ngọt", "Công cô câu", "Bắc Mộng", "Thiên quốc", "Ông Cương", "Nam Khổng", "Hào Đảo Đế"};
    static final int[] CLV = {1, 15, 60, 1, 16, 60, 1, 1, 60, 1, 0, 0};
    static final String[] MP = {"Bản đập Pá Đất", "Nước thải ô nhiễm", "Hắc Hổ", "Địa Đồ Lễ Hội", "Ngũ Hồ Sơn Lợi", "Thôn Quái", "Quán sau Nam Cương", "Bờ Biển", "Trường Bạch Sơn"};
    static final String[] MF = {"Ngựa lưng chừng", "Shark biển đỏ", "Cá chép tai bạc", "Cá lễ hội vàng", "Cá rồng hình rồng", "Ây ngư âm", "Kún", "Thủy quái bờ biển", "Cá Chép Râu Bạc"};
    static final int[] MW = {30, 200, 2000, 5000, 12000, 50000, 120000, 500000, 1500000}, MLV = {1, 10, 20, 30, 40, 55, 70, 85, 95};
    static final int[] MC = {0xFF2FA3B3, 0xFF4E9A5E, 0xFF2A6FA0, 0xFF7A4FB0, 0xFF3A7FD0, 0xFF4A5C7A, 0xFF1B4F72, 0xFF0F6FA8, 0xFF8FB8C8};
    static final String[] ROD = {"Cần Tre", "Cần Sắt", "Cần Thép", "Cần Thép Gân", "Cần Vàng", "Cần Thần", "Cần Hải Thần", "Đao Long Ấn"};
    static final int[] RP = {80, 160, 300, 520, 900, 1500, 2600, 4500}, RC = {0, 1500, 6000, 18000, 50000, 150000, 500000, 1500000};
    static final String[] SKN = {"Chàng trai xuống núi", "Đại ma bại trận", "Gà trống đại chiến", "Ngược dòng", "Ông lão đạp xe đạp", "Tay bóng phản chiếu", "Câu cá bằng động cơ", "Phá ông chìm thuyền"};
    static final int[] SC = {40, 60, 80, 120, 180, 250, 80, 300};
    static final String TALK_TXT = "Thằng cá độ đáng ghét, dám bắt cá của tôi ở bên kia, hôm nay dù anh là ai thì cũng không dễ anh chạy trốn được.";

    final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    final Path path = new Path();
    final Random rnd = new Random();
    final SharedPreferences sp;
    final ArrayList<float[]> hit = new ArrayList<float[]>();
    int scr, map, sel, tab, rod, baits = 20, invN, phase, kg, ptr = -1, joyPtr = -1, fxWho = -1, unl = 0x3FF, eqN; // phase: 0 rảnh, 1 chờ cá, 2 đang kéo
    int[] sk = {1, 1, 0, 0, 0, 0, 0, 0}, team = {0, 1, 2}, eq = {0, 1, -1};
    long money = 500, xp, inv, bite, msgT, fxT, talkT, hitT, lastMs = System.currentTimeMillis();
    float hp = 1, hpMax = 1, dist, maxLine = 40, ten, st = 150, u = 1, t, jx, jy, fxDmg, hitD, zoom = 1, lx = .34f, ly = .88f;
    float[] cd = new float[8], px = {.34f, .26f, .18f}, py = {.88f, .88f, .88f};
    boolean reel, spot, gift, fxSnd;
    String msg = "";

    GameView(Context c) {
        super(c);
        sp = c.getSharedPreferences("fish5", 0);
        money = sp.getLong("m", 500); xp = sp.getLong("x", 0); rod = sp.getInt("r", 0); baits = sp.getInt("b", 20);
        inv = sp.getLong("i", 0); invN = sp.getInt("n", 0); unl = sp.getInt("u", 0x3FF); gift = sp.getBoolean("g", false);
        for (int i = 0; i < 8; i++) sk[i] = sp.getInt("s" + i, i < 2 ? 1 : 0);
        for (int i = 0; i < 3; i++) { team[i] = sp.getInt("t" + i, i); eq[i] = sp.getInt("e" + i, i < 2 ? i : -1); }
    }

    void save() {
        SharedPreferences.Editor e = sp.edit();
        e.putLong("m", money).putLong("x", xp).putInt("r", rod).putInt("b", baits).putLong("i", inv).putInt("n", invN).putInt("u", unl).putBoolean("g", gift);
        for (int i = 0; i < 8; i++) e.putInt("s" + i, sk[i]);
        for (int i = 0; i < 3; i++) { e.putInt("t" + i, team[i]); e.putInt("e" + i, eq[i]); }
        e.apply();
    }

    long cum(int n) { return (long) (n - 1) * (100 + 20 * n); }
    int lv() { int l = 1; while (l < 100 && cum(l + 1) <= xp) l++; return l; }
    long need() { return 100 + 40L * lv(); }
    int maxSt() { return 150 + lv() * 5; }
    float pw() { float s = 3f * (RP[rod] + lv() * 6f) + (rod == 7 ? 1500 : 0); for (int i = 0; i < 8; i++) s += sk[i]; for (int i = 0; i < 3; i++) s += CLV[team[i]] * 2; return s; }
    float sm(int i) { return 1.2f + .35f * i + .05f * sk[i]; }
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
        if (id == 1) {
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
        else if (id >= 20 && id < 28) {
            int i = id - 20;
            long cost = uc(i);
            if (sk[i] >= 100) say("Đã mãn cấp"); else if (money >= cost) { money -= cost; sk[i]++; } else say("Không đủ tiền");
        } else if (id >= 30 && id < 38) {
            int i = id - 30;
            if (i <= rod) rod = i; else if (money >= RC[i]) { money -= RC[i]; rod = i; } else say("Không đủ tiền");
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
        else if (id >= 140 && id < 148) {
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
        for (int i = 0; i < 8; i++) cd[i] = Math.max(0, cd[i] - dt);
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
            float cw = (w - 200 * u) / 4f;
            for (int i = 0; i < 8; i++) {
                float l = 185 * u + i % 4 * (cw + 6 * u), tp = 60 * u + i / 4 * 235 * u;
                box(c, l, tp, l + cw, tp + 225 * u, 0xFF18242C);
                tx(c, SKN[i], l + cw / 2, tp + 22 * u, 11, 0xFFFFFFFF, true);
                tx(c, sk[i] == 0 ? "Chưa mở khóa" : sk[i] >= 100 ? "Lv 100 (mãn cấp)" : "Lv " + sk[i], l + cw / 2, tp + 44 * u, 12, 0xFFF2B931, true);
                tx(c, "Chi tiêu thể lực: " + SC[i], l + 8 * u, tp + 70 * u, 10, 0xFFCCCCCC, false);
                tx(c, "Lực: " + (int) (pw() * sm(i)), l + 8 * u, tp + 90 * u, 10, 0xFFCCCCCC, false);
                btn(c, 20 + i, sk[i] >= 100 ? "Đã mãn cấp" : (sk[i] == 0 ? "Mở khóa $" : "Nâng cấp $") + uc(i), l + 6 * u, tp + 105 * u, l + cw - 6 * u, tp + 150 * u, sk[i] < 100 && money >= uc(i));
                boolean on = eq[0] == i || eq[1] == i || eq[2] == i;
                btn(c, 140 + i, on ? "Hủy cấu hình" : "Cấu hình", l + 6 * u, tp + 160 * u, l + cw - 6 * u, tp + 205 * u, sk[i] > 0 && !on);
            }
        } else {
            float cw = (w - 230 * u) / 4f;
            for (int i = 0; i < 8; i++) {
                float l = 185 * u + i % 4 * (cw + 6 * u), tp = 70 * u + i / 4 * 90 * u;
                btn(c, 30 + i, ROD[i] + " " + (i == rod ? "(đang dùng)" : i < rod ? "(dùng)" : "$" + RC[i]), l, tp, l + cw, tp + 76 * u, i <= rod || money >= RC[i]);
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
        for (int i = 2; i >= 0; i--) {
            float x = w * px[i], y = h * py[i];
            Fx.person(c, p, x, y, u * .8f, team[i]);
            tx(c, CH[team[i]], x, y - 104 * u, 10, 0xFFFFFFFF, true);
            if (phase > 0) chain(c, x + 68 * u, y - 132 * u, fx, fy + i * 6 * u);
        }
        if (phase == 1) { p.setColor(0xFFE5413A); c.drawCircle(fx, fy, 7 * u, p); }
        if (phase == 2) {
            float s = u * (.6f + .9f * (float) Math.sqrt(Math.min(1f, kg / 120000f)));
            p.setColor(0xFF6843A5);
            c.drawOval(fx - 40 * s, fy - 17 * s, fx + 36 * s, fy + 17 * s, p);
            c.drawCircle(fx + 50 * s, fy, 14 * s, p);
            if (now - hitT < 500) Fx.text(c, p, "-" + (int) hitD, fx + 40 * u, fy - 50 * u - (now - hitT) * .08f * u, 20 * u, 0xFFC04DFF);
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
            tx(c, (int) Math.max(0, hp) + "/" + (int) hpMax, w / 2f, 82 * u, 11, 0xFFFFFFFF, true);
            bar(c, w / 2f - 190 * u, 90 * u, w / 2f + 190 * u, 100 * u, ten / 100f, ten > 84 ? 0xFFFF4040 : 0xFF8ADB3A);
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
