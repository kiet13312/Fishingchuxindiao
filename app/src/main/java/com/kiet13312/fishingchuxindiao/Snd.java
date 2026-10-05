package com.kiet13312.fishingchuxindiao;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.util.Random;

// Âm thanh tự tổng hợp bằng code, không cần file âm thanh
final class Snd {
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
