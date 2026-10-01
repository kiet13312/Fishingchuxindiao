package com.kiet13312.fishingchuxindiao;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);setContentView(new FishingView(this));}
 static class FishingView extends View {
  Paint p=new Paint(3); Random r=new Random(); int character=0,money=1000,weight=0,energy=100,rod=0,rodPower=100;
  boolean fishing=false,hooked=false,skillFlash=false,tractorSkill=false,skillReady=true; boolean skillBoosted=false; long skillCooldownEnd=0; long tractorEnd=0; final long SKILL_COOLDOWN=8000; final long TRACTOR_DURATION=3000;
  String caughtFish="",message="Sẵn sàng câu cá"; ArrayList<Fish> bag=new ArrayList<>();
  String[] names={"Sở Tâm","Bá Thường","Lão Ngô"}; int[] power={70,95,80};
  String[] rodNames={"Cần Tre","Cần Sắt","Cần Thép","Cần Vàng","Cần Thần"}; int[] rodPrices={0,2000,8000,30000,100000}; int[] rodPowerList={100,300,600,1200,2500};
  int[] fishWeights={10,20,40,60,80,100,120,160,200,300,500,800,1000};
  String[] fishNames={"Cá rô","Cá chép","Cá trắm","Cá lóc","Cá mè","Cá nheo","Cá hồng","Cá basa","Cá kiếm","Cá khổng lồ","Cá vạn cân","Cá thần","Cá cực hiếm"};
  FishingView(Context c){super(c);p.setTypeface(Typeface.DEFAULT_BOLD);}
  boolean skillReady(){return System.currentTimeMillis()>=skillCooldownEnd;}
  long cooldownLeft(){return Math.max(0,skillCooldownEnd-System.currentTimeMillis());}
  protected void onDraw(Canvas c){
   int w=getWidth(),h=getHeight(); p.setStyle(Paint.Style.FILL);
   p.setColor(Color.rgb(105,180,220));c.drawRect(0,0,w,h,p);p.setColor(Color.rgb(55,145,190));c.drawRect(0,h/2,w,h,p);p.setColor(Color.rgb(45,125,75));c.drawRect(0,(int)(h*.78),w,h,p);
   if(skillFlash){p.setColor(Color.argb(115,255,245,120));c.drawRect(0,0,w,h,p);p.setColor(Color.argb(190,255,215,60));c.drawCircle(w*.52f,h*.75f,90,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);c.drawCircle(w*.52f,h*.75f,125,p);p.setStyle(Paint.Style.FILL);}
   if(tractorSkill){ drawTractorSkill(c,w,h); postInvalidateDelayed(40); }
   p.setColor(Color.WHITE);p.setTextSize(27);c.drawText("CÂU CÁ VẠN CÂN",20,36,p);p.setTextSize(18);c.drawText("Tiền: "+money+"   Năng lượng: "+energy,20,67,p);p.setTextSize(16);c.drawText(rodNames[rod]+" | Sức chịu: "+rodPower+" kg",20,88,p);
   for(int i=0;i<3;i++){float x=20+i*190;p.setColor(i==character?Color.rgb(255,210,70):Color.rgb(45,55,65));c.drawRoundRect(x,100,x+175,160,14,14,p);p.setColor(Color.WHITE);p.setTextSize(17);c.drawText(names[i],x+12,126,p);p.setTextSize(12);c.drawText("Lực câu "+power[i],x+12,148,p);}
   float fx=w*.18f,fy=h*.57f;p.setColor(Color.DKGRAY);c.drawCircle(fx,fy-50,23,p);p.setStrokeWidth(8);p.setColor(Color.rgb(100,60,30));c.drawLine(fx,fy-25,fx+70,fy+15,p);p.setStrokeWidth(3);p.setColor(Color.WHITE);float hx=fishing?w*.52f:fx+70;c.drawLine(fx+70,fy+15,hx,h*.75f,p);p.setColor(Color.RED);c.drawCircle(hx,h*.75f,8,p);if(fishing){p.setColor(Color.rgb(235,140,45));c.drawOval(hx-45,h*.75f-17,hx+35,h*.75f+17,p);}
   p.setColor(Color.argb(215,0,0,0));c.drawRoundRect(20,h-150,w-20,h-12,18,18,p);p.setColor(Color.WHITE);p.setTextSize(17);c.drawText(message,35,h-120,p);if(!caughtFish.equals("")){p.setTextSize(15);c.drawText("Vừa bắt: "+caughtFish+" - "+weight+" kg",35,h-94,p);}p.setTextSize(15);c.drawText("Kho cá: "+bag.size()+" con",35,h-68,p);
   p.setColor(Color.rgb(255,193,7));c.drawRoundRect(w-190,h-140,w-35,h-94,14,14,p);p.setColor(Color.BLACK);p.setTextSize(14);c.drawText(fishing?"KÉO CÁ":"QUĂNG CẦN",w-175,h-112,p);
   p.setColor(Color.rgb(100,200,100));c.drawRoundRect(w-190,h-87,w-35,h-43,14,14,p);p.setColor(Color.BLACK);c.drawText("BÁN CÁ",w-165,h-60,p);
   p.setColor(Color.rgb(80,150,240));c.drawRoundRect(w-380,h-87,w-205,h-43,14,14,p);p.setColor(Color.WHITE);p.setTextSize(13);c.drawText(rod<rodNames.length-1?"MUA CẦN":"CẦN TỐI ĐA",w-365,h-60,p);
   if(character==0||character==1){boolean ready=skillReady();p.setColor(ready?(character==0?Color.rgb(70,150,80):Color.rgb(175,70,220)):Color.GRAY);c.drawRoundRect(w-405,h-140,w-205,h-94,14,14,p);p.setColor(Color.WHITE);p.setTextSize(12);c.drawText(ready?(character==0?"XE KÉO":"PHI THIÊN VÔ CỰC"):"HỒI "+((cooldownLeft()+999)/1000)+"s",w-390,h-112,p);postInvalidateDelayed(100);}
  }
  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight();
   if(y>=100&&y<=170){int i=(int)((x-20)/190);if(i>=0&&i<3){character=i;message="Đã chọn "+names[i];invalidate();return true;}}
   if((character==0||character==1)&&x>=w-420&&x<=w-195&&y>=h-150&&y<=h-88){useSkill();return true;}
   if(x>=w-200&&y>=h-150&&y<=h-90){if(!fishing)startFishing();else if(hooked)catchFish();else message="Cá chưa cắn!";invalidate();return true;}
   if(x>=w-200&&y>=h-90&&y<=h-38){sellFish();invalidate();return true;}if(x>=w-390&&x<=w-195&&y>=h-90&&y<=h-38){buyRod();invalidate();return true;}return true;
  }
  void useSkill(){
   if(!skillReady()){message=(character==0?"Xe Kéo":"Phi Thiên Vô Cực")+" đang hồi: "+((cooldownLeft()+999)/1000)+" giây";invalidate();return;}
   skillCooldownEnd=System.currentTimeMillis()+SKILL_COOLDOWN;
   if(character==0){
    tractorSkill=true; tractorEnd=System.currentTimeMillis()+TRACTOR_DURATION; skillBoosted=false;
    message="🚜 XE KÉO! Sở Tâm lên máy cày!";
    if(fishing){weight+=50;hooked=true;skillBoosted=true;}
    invalidate();
    new Handler().postDelayed(()->{tractorSkill=false;message="Xe Kéo kết thúc - tiếp tục câu cá";invalidate();},TRACTOR_DURATION);
   }else{
    skillFlash=true;message="⚡ PHI THIÊN VÔ CỰC!";
    if(fishing){weight+=50;hooked=true;}
    invalidate();new Handler().postDelayed(()->{skillFlash=false;invalidate();},650);
   }
  }
  void drawTractorSkill(Canvas c,int w,int h){
   long now=System.currentTimeMillis(); float t=1f-(float)Math.max(0,tractorEnd-now)/TRACTOR_DURATION; float tx=w*(0.18f+0.38f*t), ty=h*.57f;
   p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(45,100,45));c.drawRect(tx-85,ty-55,tx+70,ty+5,p);
   p.setColor(Color.rgb(70,130,55));c.drawRect(tx-20,ty-95,tx+55,ty-55,p);p.setColor(Color.rgb(175,220,235));c.drawRect(tx+5,ty-88,tx+45,ty-60,p);
   p.setColor(Color.DKGRAY);c.drawCircle(tx-55,ty+18,28,p);c.drawCircle(tx+55,ty+12,20,p);
   p.setColor(Color.rgb(35,35,35));c.drawCircle(tx-55,ty+18,15,p);c.drawCircle(tx+55,ty+12,11,p);
   p.setColor(Color.rgb(120,75,35));c.drawRect(tx-95,ty-35,tx-15,ty-5,p);
   p.setColor(Color.rgb(245,200,120));c.drawCircle(tx+18,ty-115,16,p);p.setColor(Color.rgb(30,90,160));c.drawRect(tx+2,ty-100,tx+34,ty-68,p);
   // Cần câu nằm phía sau thùng xe, kéo về phía sau
   p.setColor(Color.rgb(100,60,30));p.setStrokeWidth(7);c.drawLine(tx-85,ty-25,tx-165,ty-75,p);
   p.setColor(Color.WHITE);p.setStrokeWidth(3);c.drawLine(tx-165,ty-75,tx-260, h*.75f,p);
   p.setColor(Color.RED);c.drawCircle(tx-260,h*.75f,9,p);
   p.setColor(Color.argb(80,255,255,255));c.drawCircle(tx-260,h*.75f,28,p);
   p.setColor(Color.WHITE);p.setTextSize(22);c.drawText("XE KÉO",tx-65,ty+70,p);
  }
  void startFishing(){if(energy<5){message="Hết năng lượng!";return;}fishing=true;hooked=false;weight=0;caughtFish="";skillBoosted=false;energy-=5;message="Đang chờ cá cắn...";long delay=1000+r.nextInt(3000);new Handler().postDelayed(()->{if(!fishing)return;int i=randomFish();caughtFish=fishNames[i];weight=fishWeights[i];if(skillBoosted)weight+=50;hooked=true;message="CÁ CẮN! "+caughtFish+" - "+weight+" kg";invalidate();},delay);}
  int randomFish(){int roll=r.nextInt(1000);if(roll<450)return r.nextInt(5);if(roll<800)return 5+r.nextInt(3);if(roll<950)return 8+r.nextInt(3);return 11+r.nextInt(2);}
  void catchFish(){if(!hooked)return;int max=rodPower+power[character]*2+(skillBoosted?100:0);if(weight>max){message="Cá quá nặng! Cần câu không chịu nổi và cá trốn mất!";fishing=false;hooked=false;weight=0;skillBoosted=false;return;}bag.add(new Fish(caughtFish,weight));message="Bắt được "+caughtFish+" nặng "+weight+" kg!";fishing=false;hooked=false;energy=Math.min(100,energy+10);}
  void buyRod(){if(rod>=rodNames.length-1){message="Bạn đã có Cần Thần!";return;}int price=rodPrices[rod+1];if(money<price){message="Không đủ tiền! Cần "+price+" tiền.";return;}money-=price;rod++;rodPower=rodPowerList[rod];message="Đã mua "+rodNames[rod]+"! Sức chịu "+rodPower+" kg.";}
  void sellFish(){if(bag.isEmpty()){message="Kho cá đang trống!";return;}int total=0;for(Fish f:bag)total+=f.weight*3;money+=total;bag.clear();message="Đã bán toàn bộ cá: +"+total+" tiền";}
  static class Fish{String name;int weight;Fish(String n,int w){name=n;weight=w;}}
 }
}