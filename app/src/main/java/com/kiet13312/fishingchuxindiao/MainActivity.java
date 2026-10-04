package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    FishingGame game;
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        game = new FishingGame(this);
        setContentView(game);
    }
    @Override protected void onPause() { super.onPause(); if (game != null) game.save(); }
    @Override protected void onStop() { super.onStop(); if (game != null) game.save(); }

    static class FishingGame extends View {
        static final int LOBBY=0, MAP=1, CHAR=2, GEAR=3, INV=4, QUEST=5, FISH=6, RESULT=7;
        static final int READY=0, WAIT_BITE=1, FIGHT=2;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path path=new Path();
        final Random rnd=new Random();
        final SharedPreferences prefs;
        ToneGenerator tone;

        final String[] chars={"Sở Tâm","Bá Thường","Lão Ngô"};
        final String[] skills={"Chàng trai xuống núi","Đại ma bại trận","Ông lão đạp xe đạp"};
        final String[] skillShort={"XUỐNG NÚI","ĐẠI MA","XE ĐẠP"};
        final int[] body={Color.rgb(67,156,95),Color.rgb(155,83,178),Color.rgb(70,126,181)};
        final int[] charLv={1,1,1};
        final int[] skillLv={1,1,1};
        final int[] upgradeCost={500,700,900};
        final int[] skillCost={350,500,450};
        final int[] cooldown={0,0,0};
        final float[] charX={.76f,.84f,.92f}, charY={.74f,.74f,.74f};
        final float[] lineDistance={0,0,0}, lineTension={0,0,0};
        final float[] rodBend={0,0,0};
        final boolean[] facingLeft={true,true,true};
        float stamina=100f;
        float fishNX=.25f, fishNY=.56f, fishVX=.055f, fishVY=.018f;
        float damagePopup=0f, damageX=0f, damageY=0f;
        long damageUntil=0, skillFxUntil=0;
        long lastDamageTick=0;

        final String[] rods={"Cần Tre","Cần Sắt","Cần Thép","Cần Vàng","Cần Thần","Cần Hải Thần"};
        final int[] rodCost={0,1800,6500,18000,50000,150000};
        final float[] rodPower={90,170,300,520,900,1500};
        final float[] rodLine={35,55,80,115,160,230};
        final String[] baits={"Mồi thường","Mồi thơm","Mồi hiếm","Mồi huyền thoại"};
        final int[] baitCost={0,120,450,1600};
        final float[] baitRare={0.00f,0.08f,0.22f,0.50f};

        final String[] mapNames={"Bến Sông","Hồ Ngọc Bích","Bãi Đá","Rừng Mây","Biển Đêm","Đảo Vạn Cân"};
        final int[] mapLv={1,3,6,10,15,20};
        final int[] mapNeedMoney={0,3500,10000,24000,55000,120000};
        final int[] mapWater={Color.rgb(53,163,188),Color.rgb(38,142,174),Color.rgb(35,117,164),Color.rgb(28,103,151),Color.rgb(16,73,118),Color.rgb(10,59,103)};
        final String[][] fishNames={
                {"Cá rô","Cá chép","Cá lóc","Cá trê"},
                {"Cá trắm","Cá mè","Cá kiếm","Cá đuối"},
                {"Cá ngừ","Cá mập","Cá voi con","Kình ngư"},
                {"Cá rồng","Long ngư","Kình thiên","Thủy quái"},
                {"Cá đêm","Ma ngư","Cá vực","Hắc long"},
                {"Cá vạn cân","Tề Thiên","Hải thần","Vua đại dương"}};
        final int[][] fishKg={
                {35,80,160,300},{450,700,1200,1800},{2400,4200,6500,9000},
                {10500,15000,22000,32000},{38000,52000,75000,100000},{120000,180000,260000,400000}};
        final int[][] fishCoinPerKg={
                {2,2,3,4},{3,4,5,6},{4,5,6,7},{6,7,8,9},{7,8,10,12},{9,12,14,18}};

        int screen=LOBBY,selected=0,map=0,rod=0,bait=0,baitCount=30,storage=30;
        int money=2500,xp=0,level=1;
        int[] caught=new int[24];
        int questFish=0,questKg=0,questLegend=0;
        boolean quest1Claim=false,quest2Claim=false,quest3Claim=false;

        int phase=READY,fishId=0,catchWeight=80,fishValue=1600;
        float fishHp=1f,tension=22f,distance=0f,maxLine=35f;
        float fishHpMax=1f,fishPower=1f,dmgAccum=0f,joyDX=0f,joyDY=0f;int joyId=-1,reelId=-1;
        long biteAt=0,toastUntil=0;
        boolean reelHeld=false,skillFx=false,resultCaught=false;
        int activeSkill=-1;
        float fishWave=0f;
        String toast="Sẵn sàng ra bến câu";
        boolean joyTouch=false,joyActive=false;
        float joyX,joyY,joyKnobX,joyKnobY;
        final float JOY_R=58f;
        long lastFrame=System.currentTimeMillis();

        FishingGame(Context c){
            super(c); prefs=c.getSharedPreferences("fishing_save",Context.MODE_PRIVATE);
            try{tone=new ToneGenerator(AudioManager.STREAM_MUSIC,55);}catch(Exception ignored){}
            setFocusable(true); load();
        }

        void save(){
            SharedPreferences.Editor e=prefs.edit();
            e.putInt("money",money).putInt("xp",xp).putInt("level",level).putInt("selected",selected)
             .putInt("map",map).putInt("rod",rod).putInt("bait",bait).putInt("baitCount",baitCount)
             .putInt("storage",storage).putInt("questFish",questFish).putInt("questKg",questKg).putInt("questLegend",questLegend)
             .putBoolean("q1",quest1Claim).putBoolean("q2",quest2Claim).putBoolean("q3",quest3Claim);
            for(int i=0;i<3;i++){e.putInt("char"+i,charLv[i]);e.putInt("skill"+i,skillLv[i]);}
            for(int i=0;i<caught.length;i++)e.putInt("caught"+i,caught[i]);
            e.apply();
        }

        void load(){
            money=prefs.getInt("money",money);xp=prefs.getInt("xp",xp);level=prefs.getInt("level",level);
            selected=Math.max(0,Math.min(2,prefs.getInt("selected",selected)));
            map=Math.max(0,Math.min(5,prefs.getInt("map",map)));rod=Math.max(0,Math.min(rods.length-1,prefs.getInt("rod",rod)));
            bait=Math.max(0,Math.min(baits.length-1,prefs.getInt("bait",bait)));baitCount=prefs.getInt("baitCount",baitCount);
            storage=Math.max(10,prefs.getInt("storage",storage));questFish=prefs.getInt("questFish",0);questKg=prefs.getInt("questKg",0);questLegend=prefs.getInt("questLegend",0);
            quest1Claim=prefs.getBoolean("q1",false);quest2Claim=prefs.getBoolean("q2",false);quest3Claim=prefs.getBoolean("q3",false);
            for(int i=0;i<3;i++){charLv[i]=prefs.getInt("char"+i,charLv[i]);skillLv[i]=prefs.getInt("skill"+i,skillLv[i]);}
            for(int i=0;i<caught.length;i++)caught[i]=prefs.getInt("caught"+i,0);
            for(int i=0;i<3;i++){upgradeCost[i]=new int[]{500,700,900}[i]+(charLv[i]-1)*350;skillCost[i]=new int[]{350,500,450}[i]+(skillLv[i]-1)*300;}
            if(level<mapLv[map])map=0; resetJoystick();
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c); long now=System.currentTimeMillis(); float dt=Math.min(.06f,(now-lastFrame)/1000f);lastFrame=now;
            if(screen==LOBBY)drawLobby(c);else if(screen==MAP)drawMap(c);else if(screen==CHAR)drawChars(c);else if(screen==GEAR)drawGear(c);
            else if(screen==INV)drawInventory(c);else if(screen==QUEST)drawQuests(c);else if(screen==FISH){updateFishing(dt,now);drawFishing(c,now);}else drawResult(c);
            postInvalidateDelayed(33);
        }

        void bg(Canvas c,int w,int h,int water){
            p.setShader(new LinearGradient(0,0,0,h*.47f,Color.rgb(98,150,115),Color.rgb(207,218,189),Shader.TileMode.CLAMP));c.drawRect(0,0,w,h*.50f,p);p.setShader(null);
            p.setColor(Color.rgb(54,110,62));for(int i=0;i<24;i++)c.drawCircle((i*83)%w,h*.18f+(i%4)*22,38+(i%3)*12,p);
            p.setShader(new LinearGradient(0,h*.35f,0,h*.84f,water,Color.rgb(Math.max(0,Color.red(water)-28),Math.max(40,Color.green(water)-20),Math.max(70,Color.blue(water)-12)),Shader.TileMode.CLAMP));c.drawRect(0,h*.35f,w,h*.84f,p);p.setShader(null);
            p.setColor(Color.argb(95,255,255,255));for(int i=0;i<20;i++){float y=h*.41f+i*h*.019f;c.drawRoundRect((i*73)%Math.max(1,w),y,Math.min(w,(i*73)%Math.max(1,w)+55+(i%5)*15),y+3,3,3,p);}
            p.setColor(Color.rgb(224,212,180));c.drawRect(0,h*.80f,w,h,p);p.setColor(Color.rgb(175,155,124));for(int i=0;i<120;i++)c.drawCircle((i*97)%Math.max(1,w),h*.82f+(i*19)%Math.max(1,(int)(h*.14f)),1+(i%3),p);
        }
        void dark(Canvas c,int w,int h){p.setColor(Color.rgb(8,11,15));c.drawRect(0,0,w,h,p);}
        void panel(Canvas c,float l,float t,float r,float b,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,16,16,p);}
        void text(Canvas c,String s,float x,float y,float size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);c.drawText(s,x,y,p);}
        void center(Canvas c,String s,float x,float y,float size,int color){p.setTextSize(size);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawText(s,x-p.measureText(s)/2f,y,p);}
        void outline(Canvas c,float l,float t,float r,float b,int color,float sw){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);p.setColor(color);c.drawRoundRect(l,t,r,b,15,15,p);p.setStyle(Paint.Style.FILL);}
        void button(Canvas c,float l,float t,float r,float b,String s,boolean active){p.setColor(Color.argb(100,0,0,0));c.drawRoundRect(l+3,t+4,r+3,b+4,14,14,p);p.setColor(active?Color.rgb(242,185,49):Color.rgb(50,73,89));c.drawRoundRect(l,t,r,b,14,14,p);outline(c,l,t,r,b,active?Color.rgb(255,234,128):Color.rgb(113,145,160),2);center(c,s,(l+r)/2,(t+b)/2+6,Math.min(17,(r-l)/7f),Color.WHITE);}
        void top(Canvas c,String title){int w=getWidth();p.setColor(Color.argb(230,9,12,16));c.drawRect(0,0,w,62,p);text(c,"☰",20,40,27,Color.WHITE);text(c,title,60,38,22,Color.WHITE);text(c,"$ "+money,w-120,38,18,Color.YELLOW);}

        void drawLobby(Canvas c){int w=getWidth(),h=getHeight();bg(c,w,h,mapWater[map]);p.setColor(Color.argb(95,0,0,0));c.drawRect(0,0,w,h,p);
            center(c,"CÂU CÁ VẠN CÂN",w*.50f,h*.18f,44,Color.WHITE);center(c,"FISHING ADVENTURE",w*.50f,h*.225f,14,Color.rgb(228,238,226));
            panel(c,w*.28f,h*.29f,w*.72f,h*.57f,Color.argb(225,13,18,23));center(c,mapNames[map],w*.50f,h*.37f,27,Color.YELLOW);center(c,"3 câu thủ sẵn sàng phối hợp",w*.50f,h*.42f,18,Color.WHITE);center(c,"Câu cá • bán cá • nâng đồ • mở vùng nước",w*.50f,h*.465f,13,Color.LTGRAY);
            button(c,w*.36f,h*.53f,w*.64f,h*.63f,"ĐI CÂU",true);
            float y=h*.70f,g=14,bw=(w-28-g*5)/5f;String[] b={"MÀN","NHÂN VẬT","TRANG BỊ","KHO","NHIỆM VỤ"};for(int i=0;i<5;i++)button(c,14+i*(bw+g),y,14+i*(bw+g)+bw,y+58,b[i],false);
            panel(c,18,h-54,400,h-15,Color.argb(170,10,14,18));text(c,"Lv "+level+"  •  XP "+xp+"  •  "+rods[rod]+"  •  Mồi x"+baitCount,32,h-29,13,Color.WHITE);
        }

        boolean isMapUnlocked(int i){return level>=mapLv[i]&&money>=mapNeedMoney[i];}
        void drawMap(Canvas c){int w=getWidth(),h=getHeight();dark(c,w,h);top(c,"CHỌN VÙNG CÂU");text(c,"Mở khóa bằng cấp độ và tiền tích lũy",30,91,15,Color.LTGRAY);
            for(int i=0;i<6;i++){int col=i%3,row=i/3;float l=30+col*w*.32f,t=112+row*190,r=l+w*.28f,b=t+155;panel(c,l,t,r,b,Color.rgb(24,31,37));outline(c,l,t,r,b,i==map?Color.YELLOW:Color.rgb(65,91,103),i==map?4:2);p.setColor(mapWater[i]);c.drawRoundRect(l+8,t+8,r-8,t+75,11,11,p);p.setColor(Color.argb(80,255,255,255));for(int k=0;k<4;k++)c.drawRoundRect(l+18+k*38,t+26,l+40+k*38,t+29,3,3,p);text(c,mapNames[i],l+15,t+103,17,Color.WHITE);text(c,"Lv "+mapLv[i]+" • $"+mapNeedMoney[i],l+15,t+126,12,Color.YELLOW);button(c,r-95,t+98,r-15,t+138,isMapUnlocked(i)?"VÀO":"KHÓA",isMapUnlocked(i));}
            button(c,25,h-62,165,h-18,"VỀ SẢNH",false);
        }

        void drawChars(Canvas c){int w=getWidth(),h=getHeight();dark(c,w,h);top(c,"NHÂN VẬT");text(c,"Cả 3 người luôn cùng tham gia trong một lần câu.",30,91,15,Color.LTGRAY);
            for(int i=0;i<3;i++){float l=38+i*w*.31f,r=l+w*.25f;panel(c,l,112,r,h-88,i==selected?Color.rgb(64,53,31):Color.rgb(24,31,37));outline(c,l,112,r,h-88,i==selected?Color.YELLOW:Color.rgb(62,85,97),i==selected?4:2);drawPerson(c,(l+r)/2,235,i,1.42f);center(c,chars[i],(l+r)/2,352,21,Color.WHITE);center(c,"Lv "+charLv[i],(l+r)/2,378,14,Color.YELLOW);center(c,"Lực "+(int)(50+charLv[i]*12),(l+r)/2,401,13,Color.LTGRAY);center(c,skills[i]+" Lv "+skillLv[i],(l+r)/2,428,13,Color.rgb(242,207,102));button(c,l+18,h-162,r-18,h-128,charLv[i]>=100?"ĐÃ ĐẦY CẤP":"NÂNG NGƯỜI $"+upgradeCost[i],true);button(c,l+18,h-123,r-18,h-91,skillLv[i]>=100?"ĐÃ ĐẦY CẤP":"SKILL $"+skillCost[i],true);}
            button(c,25,h-57,165,h-18,"VỀ SẢNH",false);
        }

        void drawGear(Canvas c){int w=getWidth(),h=getHeight();dark(c,w,h);top(c,"TRANG BỊ & CỬA HÀNG");text(c,"Cần câu",28,92,18,Color.WHITE);text(c,"Mồi",w*.56f,92,18,Color.WHITE);
            for(int i=0;i<rods.length;i++){int col=i%3,row=i/3;float l=24+col*w*.30f,t=108+row*122,r=l+w*.26f,b=t+100;panel(c,l,t,r,b,Color.rgb(24,31,37));text(c,rods[i],l+12,t+24,15,Color.WHITE);text(c,"Lực "+(int)rodPower[i]+" • Dây "+(int)rodLine[i]+"m",l+12,t+47,11,Color.LTGRAY);text(c,i==rod?"ĐANG DÙNG":i<rod?"ĐÃ MUA":"$"+rodCost[i],l+12,t+69,12,i<=rod?Color.rgb(100,235,155):Color.YELLOW);button(c,l+10,t+76,r-10,t+98,i<=rod?"DÙNG":"MUA",i<=rod);}
            for(int i=0;i<4;i++){float l=w*.56f,r=w*.93f,t=112+i*86,b=t+70;panel(c,l,t,r,b,Color.rgb(24,31,37));text(c,baits[i]+" x"+(i==bait?baitCount:0),l+14,t+27,14,Color.WHITE);text(c,i==bait?"ĐANG CHỌN":"Giá "+baitCost[i],l+14,t+51,12,i==bait?Color.YELLOW:Color.LTGRAY);button(c,r-105,t+18,r-15,t+58,i==bait?"DÙNG":"MUA",true);}
            button(c,w*.56f,h-110,w*.93f,h-72,"MUA 5 MỒI • $"+(baitCost[bait]*5),true);button(c,25,h-57,165,h-18,"VỀ SẢNH",false);
        }

        void drawInventory(Canvas c){int w=getWidth(),h=getHeight();dark(c,w,h);top(c,"KHO CÁ");text(c,"Đang giữ "+sumCaught()+" / "+storage+" cá",28,91,16,Color.WHITE);text(c,"Bán toàn bộ: $"+inventoryValue(),w*.66f,91,15,Color.YELLOW);button(c,w*.66f,108,w*.93f,151,"BÁN HẾT",true);
            for(int i=0;i<24;i++){if(caught[i]==0)continue;int row=i/6,col=i%6;float l=24+col*(w-48)/6f,t=170+row*100,r=l+(w-48)/6f-8,b=t+82;panel(c,l,t,r,b,Color.rgb(23,30,36));center(c,getFishName(i),(l+r)/2,t+27,13,Color.WHITE);center(c,"x"+caught[i],(l+r)/2,t+49,13,Color.YELLOW);}
            button(c,25,h-57,165,h-18,"VỀ SẢNH",false);
        }

        void drawQuests(Canvas c){int w=getWidth(),h=getHeight();dark(c,w,h);top(c,"NHIỆM VỤ");text(c,"Hoàn thành để lấy tiền và XP",28,91,16,Color.LTGRAY);
            questCard(c,28,116,w*.47f,225,"Ngư dân mới","Bắt 10 con cá",Math.min(10,questFish),10,250,0);
            questCard(c,28,242,w*.47f,351,"Kẻ săn cá lớn","Bắt tổng 5.000 kg",Math.min(5000,questKg),5000,800,1);
            questCard(c,28,368,w*.47f,477,"Vạn cân","Bắt 1 cá từ 100.000 kg",Math.min(1,questLegend),1,2500,2);
            panel(c,w*.55f,125,w*.93f,460,Color.rgb(25,33,40));center(c,"THƯỞNG",w*.74f,162,22,Color.YELLOW);center(c,"Tiền + XP",w*.74f,194,15,Color.WHITE);text(c,"Nhiệm vụ được lưu tự động.",w*.60f,245,13,Color.LTGRAY);text(c,"Bạn có thể tiếp tục sau khi",w*.60f,270,13,Color.LTGRAY);text(c,"thoát game.",w*.60f,291,13,Color.LTGRAY);center(c,"Bắt cá → bán cá → nâng cấp →",w*.74f,352,14,Color.WHITE);center(c,"mở vùng mới → săn cá khủng",w*.74f,377,14,Color.WHITE);
            button(c,25,h-57,165,h-18,"VỀ SẢNH",false);
        }
        void questCard(Canvas c,float l,float t,float r,float b,String title,String desc,int val,int max,int reward,int which){panel(c,l,t,r,b,Color.rgb(24,31,37));text(c,title,l+14,t+29,17,Color.WHITE);text(c,desc,l+14,t+52,12,Color.LTGRAY);text(c,val+" / "+max,l+14,t+78,12,Color.YELLOW);p.setColor(Color.rgb(39,52,59));c.drawRoundRect(l+93,t+68,r-82,t+79,5,5,p);p.setColor(Color.rgb(73,190,117));c.drawRoundRect(l+93,t+68,l+93+(r-82-l-93)*Math.min(1f,val/(float)max),t+79,5,5,p);boolean claimed=which==0?quest1Claim:which==1?quest2Claim:quest3Claim;button(c,r-76,t+60,r-13,t+89,claimed?"ĐÃ NHẬN":"+"+reward,val>=max&&!claimed);}

        void drawFishing(Canvas c,long now){int w=getWidth(),h=getHeight();p.setShader(new LinearGradient(0,0,w*.67f,0,mapWater[map],Color.rgb(72,178,193),Shader.TileMode.CLAMP));c.drawRect(0,0,w*.69f,h,p);p.setShader(null);
            p.setColor(Color.rgb(239,234,214));c.drawRect(w*.65f,0,w,h,p);p.setColor(Color.rgb(86,150,69));c.drawRect(w*.65f,0,w,h*.13f,p);p.setColor(Color.rgb(58,112,55));c.drawRect(w*.65f,h*.13f,w,h*.16f,p);p.setColor(Color.argb(115,255,255,255));
            for(int i=0;i<24;i++){float yy=h*.38f+(i*31)%Math.max(1,(int)(h*.36f));float xx=(i*71)%Math.max(1,(int)(w*.62f));c.drawRoundRect(xx,yy,Math.min(w*.62f,xx+35+(i%4)*20),yy+3,3,3,p);}
            drawHud(c);text(c,"$ "+money,w-105,35,19,Color.YELLOW);
            drawSideMenu(c,w,h);float fx=w*(.08f+fishNX*.54f),fy=h*(.36f+fishNY*.36f);
            for(int i=0;i<3;i++){float px=w*charX[i],base=h*(charY[i]);float lean=(float)Math.sin(now/150.0+i)*2; if(fishHooked())lean-=rodBend[i]*7f;drawHero(c,px,base,i,.92f,lean);drawRodLine(c,px,base,i,fx,fy,rodBend[i]);}
            drawFish(c,fx,fy,fishHooked()?1.02f:0.72f,fishHooked());
            if(phase==WAIT_BITE){center(c,"...",fx,fy-60,30,Color.WHITE);center(c,"Cá đang dò mồi",w*.29f,h*.35f,17,Color.WHITE);}
            else if(phase==FIGHT){center(c,"CÁ CẮN!",w*.30f,h*.34f,26,Color.WHITE);drawFishHud(c,fx,fy,w);}
            if(damageUntil>now){center(c,"-"+(int)damagePopup,damageX,damageY,16,Color.rgb(255,235,120));}
            panel(c,14,h-105,270,h-18,Color.argb(170,0,0,0));
            text(c,"Dây 1: "+String.format(Locale.US,"%.1f",lineDistance[0])+"m  "+(int)lineTension[0]+"%",28,h-77,11,Color.WHITE);
            text(c,"Dây 2: "+String.format(Locale.US,"%.1f",lineDistance[1])+"m  "+(int)lineTension[1]+"%",28,h-59,11,Color.WHITE);
            text(c,"Dây 3: "+String.format(Locale.US,"%.1f",lineDistance[2])+"m  "+(int)lineTension[2]+"%",28,h-41,11,Color.WHITE);
            text(c,phase==READY?"Nhấn THẢ LƯỚI":phase==WAIT_BITE?"Chờ cá cắn...":reelHeld?"ĐANG KÉO 3 CẦN!":"Thả để cá kéo",28,h-23,11,Color.YELLOW);
            joyX=94;joyY=h-150;p.setColor(Color.argb(78,0,0,0));c.drawCircle(joyX,joyY,62,p);outline(c,joyX-JOY_R,joyY-JOY_R,joyX+JOY_R,joyY+JOY_R,Color.WHITE,3);float kx=joyActive?joyKnobX:joyX,ky=joyActive?joyKnobY:joyY;p.setColor(Color.argb(230,244,193,56));c.drawCircle(kx,ky,25,p);
            for(int i=0;i<3;i++){float l=w*.47f+i*w*.10f,t=h-70,r=l+w*.09f,b=h-16;drawSkill(c,l,t,r,b,i);}float bx=w*.90f,by=h*.68f;p.setColor(Color.argb(85,0,0,0));c.drawCircle(bx+4,by+5,59,p);p.setColor(Color.rgb(38,74,83));c.drawCircle(bx,by,55,p);outline(c,bx-46,by-46,bx+46,by+46,Color.WHITE,4);center(c,phase==READY?"THẢ LƯỚI":phase==WAIT_BITE?"ĐANG CHỜ":"CO LẠI ĐÂY",bx,by+4,12,Color.WHITE);
            drawLineGauge(c,w,h);p.setColor(Color.argb(145,0,0,0));c.drawRoundRect(w*.30f,h*.90f,w*.63f,h*.945f,8,8,p);
            float avgT=(lineTension[0]+lineTension[1]+lineTension[2])/3f;
            p.setColor(avgT>84?Color.RED:avgT>64?Color.YELLOW:Color.rgb(69,214,135));c.drawRoundRect(w*.30f,h*.90f,w*.30f+w*.33f*avgT/100f,h*.945f,8,8,p);
            center(c,"CĂNG DÂY "+(int)avgT+"%  •  THỂ LỰC "+(int)stamina+"%",w*.465f,h*.931f,10,Color.WHITE);
            if(toastUntil>now)drawToast(c,w,h,toast);if(skillFx&&skillFxUntil>now)drawSkillFx(c,w,h,now);
        }

        void drawHud(Canvas c){panel(c,10,10,236,113,Color.argb(190,12,20,28));
            text(c,"Lv "+level+"  •  "+mapNames[map],22,30,13,Color.WHITE);
            int xpNow=xp%500;p.setColor(Color.rgb(45,56,64));c.drawRoundRect(22,38,224,48,5,5,p);p.setColor(Color.rgb(90,170,255));c.drawRoundRect(22,38,22+202f*xpNow/500f,48,5,5,p);text(c,"XP "+xpNow+"/500",24,47,9,Color.WHITE);
            p.setColor(Color.rgb(45,56,64));c.drawRoundRect(22,55,224,65,5,5,p);p.setColor(Color.rgb(230,70,60));c.drawRoundRect(22,55,22+202f*Math.max(0f,stamina)/100f,65,5,5,p);text(c,"Thể lực "+(int)stamina+"/100",24,64,9,Color.WHITE);
            text(c,"Tổng cá câu được: "+questKg+" kg",22,86,11,Color.YELLOW);text(c,"Đang câu: "+(fishHooked()?catchWeight:0)+" kg",22,104,11,Color.WHITE);}
        void drawLineGauge(Canvas c,int w,int h){float gx=w*.965f,top=h*.25f,bot=h*.62f;p.setColor(Color.argb(150,0,0,0));c.drawRoundRect(gx-8,top,gx+8,bot,8,8,p);
            float f=phase==FIGHT?Math.min(1f,distance/Math.max(1f,maxLine)):0f;p.setColor(f>.85f?Color.RED:Color.rgb(69,214,135));c.drawRoundRect(gx-6,bot-(bot-top)*f,gx+6,bot,6,6,p);
            center(c,"Dây "+(int)(phase==FIGHT?distance:0)+"m",gx,bot+20,10,Color.WHITE);}
        void drawSideMenu(Canvas c,int w,int h){String[] m={"Người câu","Cách đánh cá","Cây câu","Quán cá"};for(int i=0;i<4;i++){float l=7,t=126+i*61,r=158,b=t+49;panel(c,l,t,r,b,Color.argb(220,247,242,226));outline(c,l,t,r,b,Color.rgb(82,74,66),2);p.setColor(Color.rgb(82,157,196));c.drawRoundRect(l+5,t+5,l+40,b-5,6,6,p);center(c,"•",l+22,t+34,21,Color.WHITE);text(c,m[i],l+47,t+31,13,Color.rgb(60,53,49));}}
        void drawHero(Canvas c,float x,float y,int who,float s,float lean){
            p.setColor(Color.argb(75,0,0,0));c.drawOval(x-31,y+45,x+34,y+60,p);
            p.setColor(Color.rgb(57,49,48));c.drawRoundRect(x-15,y+5,x-3,y+45,4,4,p);c.drawRoundRect(x+3,y+5,x+15,y+45,4,4,p);
            p.setColor(body[who]);c.drawRoundRect(x-24+lean,y-49,x+24+lean,y+8,10,10,p);
            p.setColor(Color.rgb(245,208,165));c.drawCircle(x+lean,y-70,18,p);
            p.setColor(who==2?Color.rgb(155,145,137):Color.rgb(38,33,42));c.drawOval(x-19+lean,y-88,x+19+lean,y-67,p);if(who==2)c.drawOval(x-18+lean,y-88,x+17+lean,y-54,p);
            p.setColor(Color.DKGRAY);c.drawCircle(x-6+lean,y-70,2,p);c.drawCircle(x+6+lean,y-70,2,p);
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(7);p.setColor(Color.rgb(245,208,165));
            float handX=x+(facingLeft[who]?-11:15)+lean,handY=y-30;
            c.drawLine(x-14+lean,y-18,handX,handY,p);c.drawLine(x+14+lean,y-18,handX+2,handY+3,p);
            p.setStrokeWidth(5);p.setColor(Color.rgb(90,57,35));c.drawLine(handX,handY,handX+(facingLeft[who]?-54:54),handY-28,p);
        }
        void drawRodLine(Canvas c,float x,float y,int who,float fx,float fy,float bend){
            float handX=x+(facingLeft[who]?-11:15),handY=y-30;
            float tipX=handX+(facingLeft[who]?-54:54),tipY=handY-28;
            float bendX=(tipX+fx)/2f+(facingLeft[who]?-18:18)*bend;
            float bendY=(tipY+fy)/2f+35f*bend;
            p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(1.5f+2.5f*bend);p.setColor(Color.rgb(238,238,235));
            path.reset();path.moveTo(tipX,tipY);path.quadTo(bendX,bendY,fx,fy);c.drawPath(path,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(242,197,54));c.drawCircle(fx,fy,4,p);
        }
        void drawFish(Canvas c,float x,float y,float sc,boolean hooked){p.setColor(hooked?Color.rgb(104,67,165):Color.rgb(48,107,165));c.drawOval(x-78*sc,y-34*sc,x+72*sc,y+34*sc,p);path.reset();path.moveTo(x+60*sc,y);path.lineTo(x+116*sc,y-45*sc);path.lineTo(x+105*sc,y);path.lineTo(x+116*sc,y+45*sc);path.close();c.drawPath(path,p);p.setColor(Color.WHITE);c.drawCircle(x-46*sc,y-7*sc,10*sc,p);p.setColor(Color.BLACK);c.drawCircle(x-46*sc,y-7*sc,4*sc,p);p.setColor(Color.rgb(175,86,113));path.reset();path.moveTo(x-5*sc,y-23*sc);path.lineTo(x+28*sc,y-55*sc);path.lineTo(x+34*sc,y-16*sc);path.close();c.drawPath(path,p);}
        void drawSkill(Canvas c,float l,float t,float r,float b,int who){panel(c,l,t,r,b,Color.argb(190,11,23,31));outline(c,l,t,r,b,body[who],2);center(c,skillShort[who],(l+r)/2,t+21,10,Color.WHITE);center(c,"Lv "+skillLv[who],(l+r)/2,t+42,10,Color.YELLOW);if(cooldown[who]>0)center(c,(cooldown[who]/1000)+"s",(l+r)/2,t+61,9,Color.LTGRAY);}
        void drawToast(Canvas c,int w,int h,String s){float y=h*.75f;panel(c,w*.30f,y,w*.70f,y+43,Color.argb(215,17,24,29));center(c,s,w*.50f,y+27,13,Color.WHITE);}
        void drawSkillFx(Canvas c,int w,int h,long now){float a=Math.max(.15f,Math.min(1f,(skillFxUntil-now)/850f));float cx=w*(.08f+fishNX*.54f),cy=h*(.36f+fishNY*.36f);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.argb((int)(190*a),255,224,94));c.drawCircle(cx,cy,22+28*a,p);
            for(int i=0;i<6;i++){double ang=i*Math.PI/3;c.drawLine(cx+(float)Math.cos(ang)*25,cy+(float)Math.sin(ang)*25,cx+(float)Math.cos(ang)*55,cy+(float)Math.sin(ang)*55,p);}
            p.setStyle(Paint.Style.FILL);center(c,skills[activeSkill],cx,cy-42,14,Color.WHITE);
        }
        void drawFishHud(Canvas c,float fx,float fy,int w){float left=Math.max(12,Math.min(w*.58f,fx-70)),top=Math.max(78,fy-88),right=left+145;
            panel(c,left,top,right,top+43,Color.argb(190,8,15,22));text(c,getFishNameNow(),left+8,top+17,12,Color.WHITE);
            p.setColor(Color.rgb(55,66,72));c.drawRoundRect(left+8,top+25,right-8,top+33,4,4,p);p.setColor(Color.rgb(239,86,76));c.drawRoundRect(left+8,top+25,left+8+(right-left-16)*Math.max(0,fishHp),top+33,4,4,p);
            text(c,(int)Math.ceil(fishHp*fishHpMax)+" / "+(int)fishHpMax,left+8,top+40,9,Color.LTGRAY);
        }
        boolean fishHooked(){return phase==FIGHT;}

        void updateFishing(float dt,long now){
            if(joyActive){charX[selected]=Math.max(.69f,Math.min(.97f,charX[selected]+joyDX*.22f*dt));charY[selected]=Math.max(.64f,Math.min(.80f,charY[selected]+joyDY*.10f*dt));}
            for(int i=0;i<3;i++)if(cooldown[i]>0)cooldown[i]=Math.max(0,cooldown[i]-(int)(dt*1000));
            stamina=Math.min(100f,stamina+dt);
            if(skillFx&&now>=skillFxUntil)skillFx=false;
            if(phase==WAIT_BITE&&now>=biteAt){
                phase=FIGHT;
                for(int i=0;i<3;i++){lineDistance[i]=maxLine*(.72f+i*.035f);lineTension[i]=32+i*4;rodBend[i]=.18f;}
                toast("CÁ CẮN! Giữ CO DÂY để kéo từng nhịp",2300);tone(1);
            }
            if(phase!=FIGHT)return;
            float struggle=(.70f+catchWeight/160000f*1.20f)*(1f+.18f*(float)Math.sin(now/170.0));
            fishNX+=fishVX*dt*(reelHeld?-1.0f:1.0f);
            fishNY+=fishVY*dt*(reelHeld?-0.7f:1.0f);
            if(fishNX<.08f||fishNX>.86f)fishVX=-fishVX;
            if(fishNY<.38f||fishNY>.90f)fishVY=-fishVY;
            float totalDamage=0,totalPull=0;
            for(int i=0;i<3;i++){
                float power=rodPower[rod]*(.85f+i*.05f)+charLv[i]*9f+skillLv[i]*6f;
                float pull=power*.0045f;
                if(reelHeld){
                    lineDistance[i]=Math.max(0,lineDistance[i]-(1.0f+pull*2.8f)*dt);
                    lineTension[i]+= (7.5f+struggle*5.5f-pull*.9f)*dt;
                    totalDamage+=power*dt;
                    totalPull+=pull;
                }else{
                    lineDistance[i]+=struggle*(1.35f+i*.12f)*dt;
                    lineTension[i]-=12f*dt;
                }
                float targetT=8f+lineDistance[i]/Math.max(1,maxLine)*82f;
                lineTension[i]=Math.max(4f,Math.min(100f,Math.max(lineTension[i],targetT)));
                rodBend[i]=Math.min(1f,lineTension[i]/100f);
                if(lineDistance[i]>maxLine){endFishing(false,"Dây "+(i+1)+" quá dài! Cá thoát.");return;}
                if(lineTension[i]>=99.8f){endFishing(false,"Dây "+(i+1)+" quá căng!");return;}
            }
            if(reelHeld&&now-lastDamageTick>220){
                lastDamageTick=now;
                damagePopup=Math.max(1,dmgAccum);dmgAccum=0f;
                damageX=getWidth()*(.08f+fishNX*.54f);
                damageY=getHeight()*(.36f+fishNY*.36f)-45;
                damageUntil=now+650;
            }
            if(reelHeld){fishHp-=totalDamage/fishHpMax;dmgAccum+=totalDamage;fishNX=Math.max(.08f,fishNX-totalPull*.006f*dt);}
            else{fishHp+=struggle*.010f*dt;fishHp=Math.min(1f,fishHp);}
            distance=(lineDistance[0]+lineDistance[1]+lineDistance[2])/3f;
            tension=(lineTension[0]+lineTension[1]+lineTension[2])/3f;
            if(fishHp<=0f||distance<=0.5f){finishCatch();return;}
        }

        void startCast(){if(sumCaught()>=storage){toast("Kho đầy! Hãy bán cá trước.",2200);return;}if(baitCount<=0){toast("Hết mồi! Mua thêm trong TRANG BỊ.",2200);return;}baitCount--;fishId=pickFish();catchWeight=fishKg(map,fishId);fishValue=catchWeight*fishCoin(map,fishId);fishHp=1f;fishPower=8f*(float)Math.pow(catchWeight,.55);fishHpMax=fishPower*14f;dmgAccum=0f;maxLine=rodLine[rod]+map*12;distance=maxLine*.70f;tension=20;for(int i=0;i<3;i++){lineDistance[i]=maxLine*.70f;lineTension[i]=18+i*2;rodBend[i]=.12f;}fishNX=.25f;fishNY=.56f;fishVX=.055f;fishVY=.018f;phase=WAIT_BITE;reelHeld=false;stamina=100f;biteAt=System.currentTimeMillis()+1200+rnd.nextInt(2200);toast("Đã thả mồi • 3 cần cùng chờ cá",1800);tone(0);save();}
        int pickFish(){float rare=baitRare[bait],roll=rnd.nextFloat();float rareStart=.70f; if(roll<rareStart){return roll<.45f?0:1;} if(roll<rareStart+rare*.75f)return 2; if(roll<rareStart+rare)return 3; return 1;}
        int fishKg(int m,int id){return this.fishKg[Math.max(0,Math.min(5,m))][Math.max(0,Math.min(3,id))];}
        int fishCoin(int m,int id){return fishCoinPerKg[Math.max(0,Math.min(5,m))][Math.max(0,Math.min(3,id))];}
        String getFishName(int global){int m=global/4,id=global%4;return fishNames[m][id];}
        String getFishNameNow(){return fishNames[map][fishId];}
        int sumCaught(){int s=0;for(int v:caught)s+=v;return s;}
        int inventoryValue(){int v=0;for(int m=0;m<6;m++)for(int id=0;id<4;id++){int idx=m*4+id;v+=caught[idx]*fishKg[m][id]*fishCoinPerKg[m][id];}return v;}

        void reelDown(){if(phase==READY){startCast();return;}if(phase==FIGHT)reelHeld=true;}
        void reelUp(){reelHeld=false;}

        float totalPower(){float t=0;for(int i=0;i<3;i++)t+=rodPower[rod]*(.85f+i*.05f)+charLv[i]*9f+skillLv[i]*6f;return t;}
        void activateSkill(int who){if(phase!=FIGHT||cooldown[who]>0)return;
            float cost=12f+skillLv[who]*2f;if(stamina<cost){toast("Không đủ thể lực",1200);return;}
            stamina-=cost;activeSkill=who;skillFx=true;skillFxUntil=System.currentTimeMillis()+850;cooldown[who]=9000;
            float tp=totalPower();int dmg;
            if(who==0){dmg=(int)(tp*(1.4f+.12f*skillLv[who]));lineDistance[who]=Math.max(0,lineDistance[who]-10-skillLv[who]*1.5f);lineTension[who]=Math.max(5,lineTension[who]-22);fishNX=Math.max(.08f,fishNX-.035f);toast("CHÀNG TRAI XUỐNG NÚI • kéo cá lại",1400);}
            else if(who==1){dmg=(int)(tp*(2.4f+.20f*skillLv[who]));lineDistance[who]=Math.max(0,lineDistance[who]-7);lineTension[who]=Math.min(96,lineTension[who]+8);toast("ĐẠI MA BẠI TRẬN • đánh mạnh",1400);}
            else{dmg=(int)(tp*(.6f+.08f*skillLv[who]));for(int i=0;i<3;i++)lineTension[i]=Math.max(5,lineTension[i]-25);lineDistance[who]=Math.max(0,lineDistance[who]-4);toast("ÔNG LÃO ĐẠP XE ĐẠP • ổn định 3 dây",1400);}
            fishHp-=dmg/fishHpMax;
            damagePopup=dmg;damageX=getWidth()*(.08f+fishNX*.54f);damageY=getHeight()*(.36f+fishNY*.36f)-55;damageUntil=System.currentTimeMillis()+850;
            skillFxUntil=System.currentTimeMillis()+850;tone(2);if(fishHp<=0||distance<=0)finishCatch();
        }

        void finishCatch(){phase=READY;reelHeld=false;resultCaught=true;int idx=map*4+fishId;if(sumCaught()<storage)caught[idx]++;money+=fishValue;xp+=Math.max(5,catchWeight/25);level=Math.max(1,1+xp/500);questFish++;questKg+=catchWeight;if(catchWeight>=100000)questLegend++;toast("Bắt được "+getFishNameNow()+" • +$"+fishValue,2500);tone(3);save();screen=RESULT;}
        void endFishing(boolean ok,String msg){phase=READY;reelHeld=false;resultCaught=false;toast=msg;toastUntil=System.currentTimeMillis()+2200;tone(4);save();screen=RESULT;}

        void drawResult(Canvas c){int w=getWidth(),h=getHeight();bg(c,w,h,mapWater[map]);p.setColor(Color.argb(125,0,0,0));c.drawRect(0,0,w,h,p);panel(c,w*.28f,h*.19f,w*.72f,h*.74f,Color.argb(238,12,16,21));center(c,resultCaught?"CÂU THÀNH CÔNG":"CÁ THOÁT",w*.50f,h*.30f,32,resultCaught?Color.rgb(120,240,165):Color.rgb(255,150,120));center(c,resultCaught?getFishNameNow():"Đừng để căng dây quá lâu",w*.50f,h*.39f,20,Color.WHITE);if(resultCaught){center(c,catchWeight+" kg",w*.50f,h*.45f,26,Color.YELLOW);center(c,"+$"+fishValue+" • XP +"+Math.max(5,catchWeight/25),w*.50f,h*.50f,15,Color.rgb(180,230,190));}else center(c,"Luyện nhịp kéo và thả dây để cá tự mỏi",w*.50f,h*.45f,14,Color.LTGRAY);button(c,w*.37f,h*.56f,w*.63f,h*.64f,"CÂU TIẾP",true);button(c,w*.37f,h*.66f,w*.63f,h*.74f,"VỀ SẢNH",false);}

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight(),a=e.getActionMasked();
            if(screen==FISH){
                int idx=e.getActionIndex(),id=e.getPointerId(idx);
                if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_POINTER_DOWN)fishDown(id,e.getX(idx),e.getY(idx),w,h);
                else if(a==MotionEvent.ACTION_MOVE){for(int i=0;i<e.getPointerCount();i++)if(e.getPointerId(i)==joyId)moveJoy(e.getX(i),e.getY(i));}
                else if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP)fishUp(id);
                else if(a==MotionEvent.ACTION_CANCEL){joyId=-1;reelId=-1;joyTouch=false;joyActive=false;reelHeld=false;resetJoystick();}
                return true;
            }
            if(a!=MotionEvent.ACTION_UP)return true;
            if(screen==LOBBY){if(y>h*.51f&&y<h*.66f){startFishing();return true;}float yy=h*.70f,g=14,bw=(w-28-g*5)/5f;for(int i=0;i<5;i++)if(x>=14+i*(bw+g)&&x<=14+i*(bw+g)+bw&&y>yy-8&&y<yy+70){screen=i==0?MAP:i==1?CHAR:i==2?GEAR:i==3?INV:QUEST;invalidate();return true;}}
            else if(screen==MAP){for(int i=0;i<6;i++){int col=i%3,row=i/3;float l=30+col*w*.32f,t=112+row*190,r=l+w*.28f,b=t+155;if(x>=l&&x<=r&&y>=t&&y<=b){if(isMapUnlocked(i)){map=i;toast="Đã chọn "+mapNames[i];}else toast="Cần Lv "+mapLv[i]+" và $"+mapNeedMoney[i];toastUntil=System.currentTimeMillis()+1700;invalidate();return true;}}if(y>h-70){screen=LOBBY;invalidate();return true;}}
            else if(screen==CHAR){for(int i=0;i<3;i++){float l=38+i*w*.31f,r=l+w*.25f;if(x>=l&&x<=r&&y>112&&y<h-88){if(y>=h-162&&y<h-127){if(charLv[i]>=100)toast=chars[i]+" đã đầy cấp";else if(money>=upgradeCost[i]){money-=upgradeCost[i];charLv[i]++;upgradeCost[i]+=350;toast=chars[i]+" lên Lv "+charLv[i];tone(0);save();}else toast="Không đủ tiền nâng "+chars[i];}else if(y>=h-124&&y<h-89){if(skillLv[i]>=100)toast=skills[i]+" đã đầy cấp";else if(money>=skillCost[i]){money-=skillCost[i];skillLv[i]++;skillCost[i]+=300;toast=skills[i]+" lên Lv "+skillLv[i];tone(2);save();}else toast="Không đủ tiền nâng kỹ năng";}else if(y>=112&&y<h-89){selected=i;toast="Đã chọn "+chars[i];save();}invalidate();return true;}}if(y>h-70){screen=LOBBY;invalidate();return true;}}
            else if(screen==GEAR){handleGearTap(x,y,w,h);}
            else if(screen==INV){if(x>w*.64f&&y>100&&y<165){int v=inventoryValue();money+=v;for(int i=0;i<caught.length;i++)caught[i]=0;toast="Đã bán cá +$"+v;save();invalidate();return true;}if(y>h-70){screen=LOBBY;invalidate();return true;}}
            else if(screen==QUEST){handleQuestTap(x,y,w,h);}
            else if(screen==RESULT){if(y>h*.55f&&y<h*.66f){startFishing();return true;}if(y>h*.64f){screen=LOBBY;invalidate();return true;}}
            return true;
        }

        void fishDown(int id,float x,float y,int w,int h){
            joyX=94;joyY=h-150;float dx=x-joyX,dy=y-joyY;
            if(dx*dx+dy*dy<=(JOY_R+28)*(JOY_R+28)){joyId=id;joyTouch=true;joyActive=true;moveJoy(x,y);return;}
            if(Math.hypot(x-w*.90f,y-h*.68f)<80){reelId=id;reelDown();return;}
            for(int i=0;i<3;i++){float l=w*.47f+i*w*.10f;if(x>=l&&x<=l+w*.09f&&y>=h-75){activateSkill(i);return;}}
            if(x<165&&y>=126&&y<126+4*61){
                if(phase!=READY){toast("Đang câu, hãy kéo cá xong đã",1500);return;}
                int k=(int)((y-126)/61);screen=k==2?GEAR:k==3?INV:CHAR;invalidate();
            }
        }
        void fishUp(int id){
            if(id==joyId){joyId=-1;joyTouch=false;joyActive=false;resetJoystick();}
            if(id==reelId){reelId=-1;reelUp();}
        }
        void moveJoy(float x,float y){float dx=x-joyX,dy=y-joyY,len=(float)Math.hypot(dx,dy);if(len>JOY_R){dx*=JOY_R/len;dy*=JOY_R/len;}joyKnobX=joyX+dx;joyKnobY=joyY+dy;
            joyDX=dx/JOY_R;joyDY=dy/JOY_R;
            if(Math.abs(dx)>JOY_R*.08f)facingLeft[selected]=dx<0;
            if(Math.abs(dx)+Math.abs(dy)>8){toast=chars[selected]+" di chuyển";toastUntil=System.currentTimeMillis()+700;}
        }
        void resetJoystick(){joyDX=0;joyDY=0;joyX=94;joyY=Math.max(100,getHeight()-150);joyKnobX=joyX;joyKnobY=joyY;}

        void handleGearTap(float x,float y,int w,int h){
            for(int i=0;i<rods.length;i++){int col=i%3,row=i/3;float l=24+col*w*.30f,t=108+row*122,r=l+w*.26f,b=t+100;if(x>=l&&x<=r&&y>=t&&y<=b){if(i<=rod){rod=i;toast="Đang dùng "+rods[i];}else if(money>=rodCost[i]){money-=rodCost[i];rod=i;toast="Đã mua "+rods[i];}else toast="Không đủ tiền";save();invalidate();return;}}
            for(int i=0;i<4;i++){float l=w*.56f,r=w*.93f,t=112+i*86,b=t+70;if(x>=l&&x<=r&&y>=t&&y<=b){if(i<=bait){bait=i;toast="Đã chọn "+baits[i];}else if(money>=baitCost[i]){money-=baitCost[i];bait=i;toast="Đã mua "+baits[i];}else toast="Không đủ tiền";save();invalidate();return;}}
            if(x>w*.54f&&y>h-125){int cost=baitCost[bait]*5;if(cost==0)cost=25;if(money>=cost){money-=cost;baitCount+=5;toast="Đã mua 5 mồi";save();}else toast="Không đủ tiền";invalidate();return;}if(y>h-70){screen=LOBBY;invalidate();}
        }

        void handleQuestTap(float x,float y,int w,int h){
            float r=w*.47f;int[] ys={116,242,368};int[] max={10,5000,1};int[] reward={250,800,2500};int[] vals={questFish,questKg,questLegend};boolean[] cl={quest1Claim,quest2Claim,quest3Claim};
            for(int i=0;i<3;i++)if(x>=r-85&&x<=r-8&&y>=ys[i]+50&&y<=ys[i]+105){if(vals[i]>=max[i]&&!cl[i]){money+=reward[i];xp+=reward[i]/2;if(i==0)quest1Claim=true;else if(i==1)quest2Claim=true;else quest3Claim=true;level=Math.max(1,1+xp/500);toast="Nhận thưởng +$"+reward[i];tone(0);save();}else toast="Chưa hoàn thành hoặc đã nhận";invalidate();return;}if(y>h-70){screen=LOBBY;invalidate();}
        }

        void startFishing(){joyId=-1;reelId=-1;joyTouch=false;joyActive=false;resetJoystick();charX[0]=.76f;charX[1]=.84f;charX[2]=.92f;charY[0]=.74f;charY[1]=.74f;charY[2]=.74f;for(int i=0;i<3;i++)facingLeft[i]=true;phase=READY;reelHeld=false;skillFx=false;screen=FISH;toast="3 câu thủ vào vị trí • mỗi người một cần";toastUntil=System.currentTimeMillis()+2200;invalidate();}
        void toast(String s,long ms){toast=s;toastUntil=System.currentTimeMillis()+ms;}
        void tone(int kind){if(tone==null)return;try{tone.startTone(kind==0?ToneGenerator.TONE_PROP_BEEP:kind==1?ToneGenerator.TONE_PROP_ACK:kind==2?ToneGenerator.TONE_PROP_BEEP2:kind==3?ToneGenerator.TONE_PROP_PROMPT:ToneGenerator.TONE_PROP_NACK,120);}catch(Exception ignored){}}
        void drawPerson(Canvas c,float x,float y,int who,float s){p.setColor(Color.rgb(48,39,36));c.drawCircle(x,y-67*s,18*s,p);p.setColor(Color.rgb(244,211,172));c.drawCircle(x,y-64*s,16*s,p);p.setColor(body[who]);c.drawRoundRect(x-23*s,y-44*s,x+23*s,y+10*s,9*s,9*s,p);p.setColor(Color.rgb(54,51,54));c.drawRect(x-14*s,y+10*s,x-3*s,y+50*s,p);c.drawRect(x+3*s,y+10*s,x+14*s,y+50*s,p);p.setColor(Color.rgb(104,61,35));p.setStrokeWidth(5*s);c.drawLine(x+13*s,y-12*s,x+72*s,y-51*s,p);}
    }
            }
                                                                                                                                                                                                                                                                                                                                                                  
