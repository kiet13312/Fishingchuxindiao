package com.kiet13312.fishingchuxindiao;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
  setContentView(new FishingView(this));
 }

 static class FishingView extends View {
  Paint p=new Paint(3); Random r=new Random();
  int character=0,money=1000,weight=0,energy=100; boolean fishing=false,hooked=false,skillReady=true,victory=false;
  String[] names={"Sở Tâm","Bá Thường","Lão Ngô"};
  int[] power={70,95,80};
  FishingView(Context c){super(c);p.setTypeface(Typeface.DEFAULT_BOLD);}

  protected void onDraw(Canvas c){
   int w=getWidth(),h=getHeight();
   p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(105,180,220)); c.drawRect(0,0,w,h,p);
   p.setColor(Color.rgb(55,145,190)); c.drawRect(0,h/2,w,h,p);
   p.setColor(Color.rgb(45,125,75)); c.drawRect(0,(int)(h*.78),w,h,p);
   p.setColor(Color.WHITE);p.setTextSize(28);c.drawText("CÂU CÁ VẠN CÂN",25,38,p);
   p.setTextSize(20);c.drawText("Tiền: "+money+"   Năng lượng: "+energy,25,70,p);
   for(int i=0;i<3;i++){float x=20+i*190;p.setColor(i==character?Color.rgb(255,210,70):Color.rgb(45,55,65));c.drawRoundRect(x,90,x+175,155,14,14,p);p.setColor(Color.WHITE);p.setTextSize(19);c.drawText(names[i],x+12,118,p);p.setTextSize(14);c.drawText("Lực câu "+power[i],x+12,143,p);}
   float fx=w*.18f,fy=h*.57f;p.setColor(Color.DKGRAY);c.drawCircle(fx,fy-50,23,p);p.setStrokeWidth(8);p.setColor(Color.rgb(100,60,30));c.drawLine(fx,fy-25,fx+70,fy+15,p);
   p.setStrokeWidth(3);p.setColor(Color.WHITE);float hx=fishing?w*.52f:fx+70;c.drawLine(fx+70,fy+15,hx,h*.75f,p);
   p.setColor(Color.RED);c.drawCircle(hx,h*.75f,8,p);
   if(fishing){p.setColor(Color.rgb(235,140,45));c.drawOval(hx-45,h*.75f-17,hx+35,h*.75f+17,p);}
   if(victory){p.setColor(Color.argb(230,0,0,0));c.drawRect(0,0,w,h,p);p.setColor(Color.YELLOW);p.setTextSize(40);c.drawText("CÁ KHỔNG LỒ!",w/2-140,h/2,p);p.setColor(Color.WHITE);p.setTextSize(21);c.drawText("Kéo được "+weight+" kg",w/2-70,h/2+40,p);return;}
   p.setColor(Color.argb(215,0,0,0));c.drawRoundRect(20,h-105,w-20,h-15,18,18,p);p.setColor(Color.WHITE);p.setTextSize(19);c.drawText(fishing?(hooked?"Cá đã cắn! Nhấn KÉO CÁ":"Đang chờ cá cắn..."):"Nhấn QUĂNG CẦN để câu",40,h-65,p);
   p.setColor(Color.rgb(255,193,7));c.drawRoundRect(w-190,h-92,w-35,h-30,18,18,p);p.setColor(Color.BLACK);p.setTextSize(17);c.drawText(fishing?"KÉO CÁ":"QUĂNG CẦN",w-175,h-54,p);
   if(character==1){p.setColor(skillReady?Color.rgb(175,70,220):Color.GRAY);c.drawRoundRect(w-405,h-92,w-205,h-30,18,18,p);p.setColor(Color.WHITE);p.setTextSize(15);c.drawText("PHI THIÊN VÔ CỰC",w-390,h-54,p);}
  }

  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight();
   if(y>=90&&y<=165){int i=(int)((x-20)/190);if(i>=0&&i<3){character=i;skillReady=true;invalidate();return true;}}
   if(character==1&&x>=w-410&&x<=w-195&&y>=h-110&&y<=h-15&&skillReady){skillReady=false;if(fishing){weight+=50;hooked=true;}energy=Math.min(100,energy+25);invalidate();return true;}
   if(x>=w-200&&y>=h-120){if(!fishing){if(energy>=5){fishing=true;hooked=false;weight=0;energy-=5;new Handler().postDelayed(()->{if(fishing){hooked=true;weight=30+r.nextInt(170);invalidate();}},1800+r.nextInt(1800));}}else if(hooked){catchFish();}else{hooked=true;weight=30+r.nextInt(170);}}invalidate();return true;
  }
  void catchFish(){int bonus=character==1?40:0;weight=Math.max(weight,30+r.nextInt(220)+bonus);money+=weight*3;energy=Math.min(100,energy+10);fishing=false;if(weight>=500)victory=true;if(character==1&&!skillReady)new Handler().postDelayed(()->{skillReady=true;invalidate();},8000);invalidate();}
 }
}