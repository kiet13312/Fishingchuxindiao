package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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
}
