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
  int character=0,money=1000,weight=0,energy=100;
  boolean fishing=false,hooked=false,skillReady=true;
  String caughtFish="",message="Sẵn sàng câu cá";
  ArrayList<Fish> bag=new ArrayList<>();
  String[] names={"Sở Tâm","Bá Thường","Lão Ngô"};
  int[] power={70,95,80};
  int[] fishWeights={10,20,40,60,80,100,120,160,200,300,500,800,1000};
  String[] fishNames={"Cá rô","Cá chép","Cá trắm","Cá lóc","Cá mè","Cá nheo","Cá hồng","Cá basa","Cá kiếm","Cá khổng lồ","Cá vạn cân","Cá thần","Cá cực hiếm"};

  FishingView(Context c){super(c);p.setTypeface(Typeface.DEFAULT_BOLD);}

  protected void onDraw(Canvas c){
   int w=getWidth(),h=getHeight();
   p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(105,180,220));c.drawRect(0,0,w,h,p);
   p.setColor(Color.rgb(55,145,190));c.drawRect(0,h/2,w,h,p);
   p.setColor(Color.rgb(45,125,75));c.drawRect(0,(int)(h*.78),w,h,p);
   p.setColor(Color.WHITE);p.setTextSize(28);c.drawText("CÂU CÁ VẠN CÂN",25,38,p);
   p.setTextSize(19);c.drawText("Tiền: "+money+"   Năng lượng: "+energy,25,70,p);
   for(int i=0;i<3;i++){float x=20+i*190;p.setColor(i==character?Color.rgb(255,210,70):Color.rgb(45,55,65));c.drawRoundRect(x,90,x+175,155,14,14,p);p.setColor(Color.WHITE);p.setTextSize(18);c.drawText(names[i],x+12,118,p);p.setTextSize(13);c.drawText("Lực câu "+power[i],x+12,143,p);}
   float fx=w*.18f,fy=h*.57f;p.setColor(Color.DKGRAY);c.drawCircle(fx,fy-50,23,p);p.setStrokeWidth(8);p.setColor(Color.rgb(100,60,30));c.drawLine(fx,fy-25,fx+70,fy+15,p);
   p.setStrokeWidth(3);p.setColor(Color.WHITE);float hx=fishing?w*.52f:fx+70;c.drawLine(fx+70,fy+15,hx,h*.75f,p);p.setColor(Color.RED);c.drawCircle(hx,h*.75f,8,p);
   if(fishing){p.setColor(Color.rgb(235,140,45));c.drawOval(hx-45,h*.75f-17,hx+35,h*.75f+17,p);}
   p.setColor(Color.argb(215,0,0,0));c.drawRoundRect(20,h-145,w-20,h-15,18,18,p);
   p.setColor(Color.WHITE);p.setTextSize(18);c.drawText(message,40,h-112,p);
   if(!caughtFish.equals("")){p.setTextSize(16);c.drawText("Vừa bắt: "+caughtFish+" - "+weight+" kg",40,h-85,p);}
   p.setTextSize(16);c.drawText("Kho cá: "+bag.size()+" con",40,h-58,p);
   p.setColor(Color.rgb(255,193,7));c.drawRoundRect(w-190,h-125,w-35,h-70,16,16,p);p.setColor(Color.BLACK);p.setTextSize(15);c.drawText(fishing?"KÉO CÁ":"QUĂNG CẦN",w-175,h-91,p);
   p.setColor(Color.rgb(100,200,100));c.drawRoundRect(w-190,h-62,w-35,h-17,14,14,p);p.setColor(Color.BLACK);c.drawText("BÁN CÁ",w-165,h-34,p);
   if(character==1){p.setColor(skillReady?Color.rgb(175,70,220):Color.GRAY);c.drawRoundRect(w-405,h-125,w-205,h-70,16,16,p);p.setColor(Color.WHITE);p.setTextSize(14);c.drawText("PHI THIÊN VÔ CỰC",w-390,h-91,p);}
  }

  public boolean onTouchEvent(MotionEvent e){
   if(e.getAction()!=MotionEvent.ACTION_UP)return true;
   float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight();
   if(y>=90&&y<=165){int i=(int)((x-20)/190);if(i>=0&&i<3){character=i;skillReady=true;message="Đã chọn "+names[i];invalidate();return true;}}
   if(y>=h-140&&y<=h-65&&x>=w-420&&x<=w-195&&character==1&&skillReady){
    skillReady=false;
    if(fishing){weight+=50;hooked=true;message="Phi Thiên Vô Cực! Lực kéo tăng mạnh!";}
    else message="Phi Thiên Vô Cực sẵn sàng cho lần câu tiếp theo!";
    invalidate();
    new Handler().postDelayed(()->{skillReady=true;invalidate();},8000);
    return true;
   }
   if(x>=w-200&&y>=h-140&&y<=h-65){
    if(!fishing){startFishing();}
    else if(hooked){catchFish();}
    else message="Cá chưa cắn!";
    invalidate();return true;
   }
   if(x>=w-200&&y>=h-65){sellFish();invalidate();return true;}
   return true;
  }

  void startFishing(){
   if(energy<5){message="Hết năng lượng!";return;}
   fishing=true;hooked=false;weight=0;caughtFish="";energy-=5;message="Đang chờ cá cắn...";
   long delay=1000+r.nextInt(3000);
   new Handler().postDelayed(()->{
    if(!fishing)return;
    int i=randomFish();
    caughtFish=fishNames[i];weight=fishWeights[i];hooked=true;
    message="CÁ CẮN! "+caughtFish+" - "+weight+" kg";
    invalidate();
   },delay);
  }

  int randomFish(){
   int roll=r.nextInt(1000);
   if(roll<450)return r.nextInt(5);
   if(roll<800)return 5+r.nextInt(3);
   if(roll<950)return 8+r.nextInt(3);
   return 11+r.nextInt(2);
  }

  void catchFish(){
   if(!hooked)return;
   int bonus=(character==1&&!skillReady)?50:0;
   weight+=bonus;
   bag.add(new Fish(caughtFish,weight));
   message="Bắt được "+caughtFish+" nặng "+weight+" kg!";
   fishing=false;hooked=false;energy=Math.min(100,energy+10);
   if(character==1&&!skillReady)message+=" Phi Thiên Vô Cực đã cộng lực!";
  }

  void sellFish(){
   if(bag.isEmpty()){message="Kho cá đang trống!";return;}
   int total=0;
   for(Fish f:bag)total+=f.weight*3;
   money+=total;bag.clear();message="Đã bán toàn bộ cá: +"+total+" tiền";
  }

  static class Fish{
   String name;int weight;
   Fish(String n,int w){name=n;weight=w;}
  }
 }
}