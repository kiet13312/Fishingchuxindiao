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
        final String[] skills={"Xe Kéo","Phi Thiên Vô Cực","Hộ Lực"};
        final String[] skillShort={"XE KÉO","PHI THIÊN","HỘ LỰC"};
        final int[] body={Color.rgb(67,156,95),Color.rgb(155,83,178),Color.rgb(70,126,181)};
        final int[] charLv={1,1,1};
        final int[] skillLv={1,1,1};
        final int[] upgradeCost={500,700,900};
        final int[] skillCost={350,500,450};
        final int[] cooldown={0,0,0};

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
            for(int i=0;i<3;i++){float l=38+i*w*.31f,r=l+w*.25f;panel(c,l,112,r,h-88,i==selected?Color.rgb(64,53,31):Color.rgb(24,31,37));outline(c,l,112,r,h-88,i==selected?Color.YELLOW:Color.rgb(62,85,97),i==selected?4:2);drawPerson(c,(l+r)/2,235,i,1.42f);center(c,chars[i],(l+r)/2,352,21,Color.WHITE);center(c,"Lv "+charLv[i],(l+r)/2,378,14,Color.YELLOW);center(c,"Lực "+(int)(50+charLv[i]*12),(l+r)/2,401,13,Color.LTGRAY);center(c,skills[i]+" Lv "+skillLv[i],(l+r)/2,428,13,Color.rgb(242,207,102));button(c,l+18,h-160,r-18,h-120,"NÂNG $"+upgradeCost[i],true);button(c,l+18,h-111,r-18,h-72,i==selected?"ĐANG CHỌN":"CHỌN",i==selected);}
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
            panel(c,10,10,218,113,Color.argb(190,12,20,28));text(c,"gameone",25,31,17,Color.WHITE);text(c,"Lv "+level+"  •  "+mapNames[map],25,51,12,Color.WHITE);text(c,"THỂ LỰC "+Math.min(100,(int)(100-tension*.55f))+"%",25,72,12,Color.WHITE);text(c,"Cá "+(fishHooked()?catchWeight:0)+" kg",25,93,12,Color.YELLOW);text(c,"$ "+money,w-105,35,19,Color.YELLOW);
            drawSideMenu(c,w,h);float[] px={w*.76f,w*.84f,w*.92f};float base=h*.74f;for(int i=0;i<3;i++){float lean=(float)Math.sin(now/160.0+i)*2;if(fishHooked())lean-=4;drawHero(c,px[i],base,i,.92f,lean);drawRodLine(c,px[i],base,i,w,h);}
            float fx=w*(.24f+(float)Math.sin(now/300.0)*.045f),fy=h*(.54f+(float)Math.sin(now/240.0)*.04f);drawFish(c,fx,fy,fishHooked()?0.95f:0.72f,fishHooked());if(phase==WAIT_BITE){center(c,"...",fx,fy-60,30,Color.WHITE);center(c,"Cá đang dò mồi",w*.29f,h*.35f,17,Color.WHITE);}else if(phase==FIGHT)center(c,"CÁ CẮN!",w*.30f,h*.34f,26,Color.WHITE);
            panel(c,14,h-92,248,h-18,Color.argb(160,0,0,0));text(c,"Dây "+String.format(Locale.US,"%.1f",distance)+" / "+(int)maxLine+" m",28,h-60,13,Color.WHITE);text(c,phase==READY?"Nhấn THẢ LƯỚI":phase==WAIT_BITE?"Chờ cá cắn...":reelHeld?"ĐANG KÉO!":"Thả để dây hồi",28,h-36,11,Color.YELLOW);
            joyX=94;joyY=h-150;p.setColor(Color.argb(78,0,0,0));c.drawCircle(joyX,joyY,62,p);outline(c,joyX-JOY_R,joyY-JOY_R,joyX+JOY_R,joyY+JOY_R,Color.WHITE,3);float kx=joyActive?joyKnobX:joyX,ky=joyActive?joyKnobY:joyY;p.setColor(Color.argb(230,244,193,56));c.drawCircle(kx,ky,25,p);
            for(int i=0;i<3;i++){float l=w*.47f+i*w*.10f,t=h-70,r=l+w*.09f,b=h-16;drawSkill(c,l,t,r,b,i);}float bx=w*.90f,by=h*.68f;p.setColor(Color.argb(85,0,0,0));c.drawCircle(bx+4,by+5,59,p);p.setColor(Color.rgb(38,74,83));c.drawCircle(bx,by,55,p);outline(c,bx-46,by-46,bx+46,by+46,Color.WHITE,4);center(c,phase==READY?"THẢ LƯỚI":phase==WAIT_BITE?"ĐANG CHỜ":"CO DÂY",bx,by+4,12,Color.WHITE);
            p.setColor(Color.argb(150,0,0,0));c.drawRoundRect(w*.30f,h*.90f,w*.63f,h*.945f,8,8,p);p.setColor(tension>84?Color.RED:tension>64?Color.YELLOW:Color.rgb(69,214,135));c.drawRoundRect(w*.30f,h*.90f,w*.30f+w*.33f*tension/100f,h*.945f,8,8,p);center(c,"CĂNG DÂY "+(int)tension+"%",w*.465f,h*.931f,11,Color.WHITE);
            if(toastUntil>now)drawToast(c,w,h,toast);if(skillFx)drawSkillFx(c,w,h,now);
        }

        void drawSideMenu(Canvas c,int w,int h){String[] m={"Người câu","Cách đánh cá","Cây câu","Quán cá"};for(int i=0;i<4;i++){float l=7,t=126+i*61,r=158,b=t+49;panel(c,l,t,r,b,Color.argb(220,247,242,226));outline(c,l,t,r,b,Color.rgb(82,74,66),2);p.setColor(Color.rgb(82,157,196));c.drawRoundRect(l+5,t+5,l+40,b-5,6,6,p);center(c,"•",l+22,t+34,21,Color.WHITE);text(c,m[i],l+47,t+31,13,Color.rgb(60,53,49));}}
        void drawHero(Canvas c,float x,float y,int who,float s,float lean){p.setColor(Color.argb(75,0,0,0));c.drawOval(x-31,y+45,x+34,y+60,p);p.setColor(Color.rgb(57,49,48));c.drawRoundRect(x-15,y+5,x-3,y+45,4,4,p);c.drawRoundRect(x+3,y+5,x+15,y+45,4,4,p);p.setColor(body[who]);c.drawRoundRect(x-24+lean,y-49,x+24+lean,y+8,10,10,p);p.setColor(Color.rgb(245,208,165));c.drawCircle(x+lean,y-70,18,p);p.setColor(who==2?Color.rgb(155,145,137):Color.rgb(38,33,42));c.drawOval(x-19+lean,y-88,x+19+lean,y-67,p);if(who==2)c.drawOval(x-18+lean,y-88,x+17+lean,y-54,p);p.setColor(Color.DKGRAY);c.drawCircle(x-6+lean,y-70,2,p);c.drawCircle(x+6+lean,y-70,2,p);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(7);p.setColor(Color.rgb(245,208,165));float ay=y-23-(phase==FIGHT?9:0);c.drawLine(x-14+lean,y-18,x+8+lean,ay,p);c.drawLine(x+14+lean,y-18,x+12+lean,ay+4,p);}
        void drawRodLine(Canvas c,float x,float y,int who,int w,int h){p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(3);p.setColor(Color.rgb(47,40,38));float tx=w*(.41f+who*.035f),ty=h*(.57f-who*.04f);c.drawLine(x-3,y-56,tx,ty,p);p.setStrokeWidth(1.2f);p.setColor(Color.WHITE);path.reset();path.moveTo(tx,ty);path.quadTo(w*.30f+who*w*.025f,h*.50f,w*.15f+who*w*.03f,h*.58f);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);}
        void drawFish(Canvas c,float x,float y,float sc,boolean hooked){p.setColor(hooked?Color.rgb(104,67,165):Color.rgb(48,107,165));c.drawOval(x-78*sc,y-34*sc,x+72*sc,y+34*sc,p);path.reset();path.moveTo(x+60*sc,y);path.lineTo(x+116*sc,y-45*sc);path.lineTo(x+105*sc,y);path.lineTo(x+116*sc,y+45*sc);path.close();c.drawPath(path,p);p.setColor(Color.WHITE);c.drawCircle(x-46*sc,y-7*sc,10*sc,p);p.setColor(Color.BLACK);c.drawCircle(x-46*sc,y-7*sc,4*sc,p);p.setColor(Color.rgb(175,86,113));path.reset();path.moveTo(x-5*sc,y-23*sc);path.lineTo(x+28*sc,y-55*sc);path.lineTo(x+34*sc,y-16*sc);path.close();c.drawPath(path,p);}
        void drawSkill(Canvas c,float l,float t,float r,float b,int who){panel(c,l,t,r,b,Color.argb(190,11,23,31));outline(c,l,t,r,b,body[who],2);center(c,skillShort[who],(l+r)/2,t+21,10,Color.WHITE);center(c,"Lv "+skillLv[who],(l+r)/2,t+42,10,Color.YELLOW);if(cooldown[who]>0)center(c,(cooldown[who]/1000)+"s",(l+r)/2,t+61,9,Color.LTGRAY);}
        void drawToast(Canvas c,int w,int h,String s){float y=h*.75f;panel(c,w*.30f,y,w*.70f,y+43,Color.argb(215,17,24,29));center(c,s,w*.50f,y+27,13,Color.WHITE);}
        void drawSkillFx(Canvas c,int w,int h,long now){float a=(float)Math.max(.15,Math.min(1,(toastUntil-now)/950.0));float cx=w*.52f,cy=h*.46f;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(7);p.setColor(Color.argb((int)(180*a),255,233,100));c.drawCircle(cx,cy,55+(now%350),p);c.drawCircle(cx,cy,95+(now%420),p);for(int i=0;i<10;i++){double ang=i*Math.PI/5;c.drawLine(cx+(float)Math.cos(ang)*60,cy+(float)Math.sin(ang)*60,cx+(float)Math.cos(ang)*145,cy+(float)Math.sin(ang)*145,p);}p.setStyle(Paint.Style.FILL);center(c,skills[activeSkill]+"!",cx,cy-115,26,Color.WHITE);}

        boolean fishHooked(){return phase==FIGHT;}

        void updateFishing(float dt,long now){
            for(int i=0;i<3;i++)if(cooldown[i]>0)cooldown[i]=Math.max(0,cooldown[i]-(int)(dt*1000));
            fishWave+=dt;if(phase==WAIT_BITE&&now>=biteAt){phase=FIGHT;distance=maxLine*.82f;tension=35;toast("CÁ CẮN! Giữ CO DÂY để kéo",2300);tone(1);}
            if(phase!=FIGHT)return;
            float fishStruggle=(.55f+catchWeight/160000f*1.25f)*(.85f+(float)Math.sin(now/180.0)*.15f);
            float teamPower=rodPower[rod]*.006f+(charLv[0]+charLv[1]+charLv[2])*.008f+(skillLv[0]+skillLv[1]+skillLv[2])*.011f;
            if(reelHeld){fishHp-=(teamPower+.75f)*dt;distance-=(1.6f+teamPower*1.7f)*dt;tension+=(11f+fishStruggle*12f)*dt;}
            else{distance+=fishStruggle*2.2f*dt;tension-=14f*dt;}
            tension=Math.max(6,Math.min(100,tension));if(tension>=99.9f){endFishing(false,"Dây quá căng! Cá thoát.");return;}if(distance>maxLine){endFishing(false,"Cá kéo quá xa! Mất dấu.");return;}if(fishHp<=0||distance<=0)finishCatch();
        }

        void startCast(){if(sumCaught()>=storage){toast("Kho đầy! Hãy bán cá trước.",2200);return;}if(baitCount<=0){toast("Hết mồi! Mua thêm trong TRANG BỊ.",2200);return;}baitCount--;fishId=pickFish();catchWeight=fishKg(map,fishId);fishValue=catchWeight*fishCoin(map,fishId);fishHp=1f;maxLine=rodLine[rod]+map*12;distance=maxLine*.70f;tension=20;phase=WAIT_BITE;reelHeld=false;biteAt=System.currentTimeMillis()+1200+rnd.nextInt(2200);toast("Đã thả mồi • chờ cá cắn",1800);tone(0);save();}
        int pickFish(){float rare=baitRare[bait],roll=rnd.nextFloat();float rareStart=.70f; if(roll<rareStart){return roll<.45f?0:1;} if(roll<rareStart+rare*.75f)return 2; if(roll<rareStart+rare)return 3; return 1;}
        int fishKg(int m,int id){return this.fishKg[Math.max(0,Math.min(5,m))][Math.max(0,Math.min(3,id))];}
        int fishCoin(int m,int id){return fishCoinPerKg[Math.max(0,Math.min(5,m))][Math.max(0,Math.min(3,id))];}
        String getFishName(int global){int m=global/4,id=global%4;return fishNames[m][id];}
        String getFishNameNow(){return fishNames[map][fishId];}
        int sumCaught(){int s=0;for(int v:caught)s+=v;return s;}
        int inventoryValue(){int v=0;for(int m=0;m<6;m++)for(int id=0;id<4;id++){int idx=m*4+id;v+=caught[idx]*fishKg[m][id]*fishCoinPerKg[m][id];}return v;}

        void reelDown(){if(phase==READY){startCast();return;}if(phase==FIGHT)reelHeld=true;}
        void reelUp(){reelHeld=false;}

        void activateSkill(int who){if(phase!=FIGHT||cooldown[who]>0)return;activeSkill=who;skillFx=true;toastUntil=System.currentTimeMillis()+950;cooldown[who]=9000;
            if(who==0){distance=Math.max(0,distance-8-skillLv[who]*2);tension=Math.max(7,tension-24);fishHp-=.16f+skillLv[who]*.025f;toast("XE KÉO • cả 3 cùng kéo",1800);}
            else if(who==1){fishHp-=.25f+skillLv[who]*.035f;tension=Math.min(99,tension+18);distance=Math.max(0,distance-10);toast("PHI THIÊN VÔ CỰC • bộc phát",1800);}
            else{tension=Math.max(5,tension-45);distance=Math.max(0,distance-4);toast("HỘ LỰC • ổn định dây",1800);}
            tone(2);if(fishHp<=0||distance<=0)finishCatch();
        }

        void finishCatch(){phase=READY;reelHeld=false;resultCaught=true;int idx=map*4+fishId;if(sumCaught()<storage)caught[idx]++;money+=fishValue;xp+=Math.max(5,catchWeight/25);level=Math.max(1,1+xp/500);questFish++;questKg+=catchWeight;if(catchWeight>=100000)questLegend++;toast("Bắt được "+getFishNameNow()+" • +$"+fishValue,2500);tone(3);save();screen=RESULT;}
        void endFishing(boolean ok,String msg){phase=READY;reelHeld=false;resultCaught=false;toast=msg;toastUntil=System.currentTimeMillis()+2200;tone(4);save();screen=RESULT;}

        void drawResult(Canvas c){int w=getWidth(),h=getHeight();bg(c,w,h,mapWater[map]);p.setColor(Color.argb(125,0,0,0));c.drawRect(0,0,w,h,p);panel(c,w*.28f,h*.19f,w*.72f,h*.74f,Color.argb(238,12,16,21));center(c,resultCaught?"CÂU THÀNH CÔNG":"CÁ THOÁT",w*.50f,h*.30f,32,resultCaught?Color.rgb(120,240,165):Color.rgb(255,150,120));center(c,resultCaught?getFishNameNow():"Đừng để căng dây quá lâu",w*.50f,h*.39f,20,Color.WHITE);if(resultCaught){center(c,catchWeight+" kg",w*.50f,h*.45f,26,Color.YELLOW);center(c,"+$"+fishValue+" • XP +"+Math.max(5,catchWeight/25),w*.50f,h*.50f,15,Color.rgb(180,230,190));}else center(c,"Luyện nhịp kéo và thả dây để cá tự mỏi",w*.50f,h*.45f,14,Color.LTGRAY);button(c,w*.37f,h*.56f,w*.63f,h*.64f,"CÂU TIẾP",true);button(c,w*.37f,h*.66f,w*.63f,h*.74f,"VỀ SẢNH",false);}

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight(),a=e.getActionMasked();
            if(screen==FISH){
                if(a==MotionEvent.ACTION_CANCEL){joyTouch=false;joyActive=false;reelHeld=false;resetJoystick();return true;}
                if(a==MotionEvent.ACTION_DOWN){joyX=94;joyY=h-150;float dx=x-joyX,dy=y-joyY;if(dx*dx+dy*dy<=(JOY_R+28)*(JOY_R+28)){joyTouch=true;joyActive=true;moveJoy(x,y);return true;}if(Math.hypot(x-w*.90f,y-h*.68f)<80){reelDown();return true;}for(int i=0;i<3;i++){float l=w*.47f+i*w*.10f;if(x>=l&&x<=l+w*.09f&&y>=h-75){activateSkill(i);return true;}}}
                if(a==MotionEvent.ACTION_MOVE&&joyTouch){moveJoy(x,y);return true;}
                if(a==MotionEvent.ACTION_UP){if(joyTouch){joyTouch=false;joyActive=false;resetJoystick();return true;}if(reelHeld){reelUp();return true;}}return true;
            }
            if(a!=MotionEvent.ACTION_UP)return true;
            if(screen==LOBBY){if(y>h*.51f&&y<h*.66f){startFishing();return true;}float yy=h*.70f,g=14,bw=(w-28-g*5)/5f;for(int i=0;i<5;i++)if(x>=14+i*(bw+g)&&x<=14+i*(bw+g)+bw&&y>yy-8&&y<yy+70){screen=i==0?MAP:i==1?CHAR:i==2?GEAR:i==3?INV:QUEST;invalidate();return true;}}
            else if(screen==MAP){for(int i=0;i<6;i++){int col=i%3,row=i/3;float l=30+col*w*.32f,t=112+row*190,r=l+w*.28f,b=t+155;if(x>=l&&x<=r&&y>=t&&y<=b){if(isMapUnlocked(i)){map=i;toast="Đã chọn "+mapNames[i];}else toast="Cần Lv "+mapLv[i]+" và $"+mapNeedMoney[i];toastUntil=System.currentTimeMillis()+1700;invalidate();return true;}}if(y>h-70){screen=LOBBY;invalidate();return true;}}
            else if(screen==CHAR){for(int i=0;i<3;i++){float l=38+i*w*.31f,r=l+w*.25f;if(x>=l&&x<=r&&y>112&&y<h-88){if(y>=h-160&&y<h-118){if(money>=upgradeCost[i]){money-=upgradeCost[i];charLv[i]++;upgradeCost[i]+=350;toast=chars[i]+" lên Lv "+charLv[i];tone(0);save();}else toast="Không đủ tiền";}else if(y>=h-118){selected=i;toast="Đã chọn "+chars[i];save();}invalidate();return true;}}if(y>h-70){screen=LOBBY;invalidate();return true;}}
            else if(screen==GEAR){handleGearTap(x,y,w,h);}
            else if(screen==INV){if(x>w*.64f&&y>100&&y<165){int v=inventoryValue();money+=v;for(int i=0;i<caught.length;i++)caught[i]=0;toast="Đã bán cá +$"+v;save();invalidate();return true;}if(y>h-70){screen=LOBBY;invalidate();return true;}}
            else if(screen==QUEST){handleQuestTap(x,y,w,h);}
            else if(screen==RESULT){if(y>h*.55f&&y<h*.66f){startFishing();return true;}if(y>h*.64f){screen=LOBBY;invalidate();return true;}}
            return true;
        }

        void moveJoy(float x,float y){float dx=x-joyX,dy=y-joyY,len=(float)Math.hypot(dx,dy);if(len>JOY_R){dx*=JOY_R/len;dy*=JOY_R/len;}joyKnobX=joyX+dx;joyKnobY=joyY+dy;if(Math.abs(dx/JOY_R)>.08f){toast=chars[selected]+" di chuyển";toastUntil=System.currentTimeMillis()+700;}}
        void resetJoystick(){joyX=94;joyY=Math.max(100,getHeight()-150);joyKnobX=joyX;joyKnobY=joyY;}

        void handleGearTap(float x,float y,int w,int h){
            for(int i=0;i<rods.length;i++){int col=i%3,row=i/3;float l=24+col*w*.30f,t=108+row*122,r=l+w*.26f,b=t+100;if(x>=l&&x<=r&&y>=t&&y<=b){if(i<=rod){rod=i;toast="Đang dùng "+rods[i];}else if(money>=rodCost[i]){money-=rodCost[i];rod=i;toast="Đã mua "+rods[i];}else toast="Không đủ tiền";save();invalidate();return;}}
            for(int i=0;i<4;i++){float l=w*.56f,r=w*.93f,t=112+i*86,b=t+70;if(x>=l&&x<=r&&y>=t&&y<=b){if(i<=bait){bait=i;toast="Đã chọn "+baits[i];}else if(money>=baitCost[i]){money-=baitCost[i];bait=i;toast="Đã mua "+baits[i];}else toast="Không đủ tiền";save();invalidate();return;}}
            if(x>w*.54f&&y>h-125){int cost=baitCost[bait]*5;if(cost==0)cost=25;if(money>=cost){money-=cost;baitCount+=5;toast="Đã mua 5 mồi";save();}else toast="Không đủ tiền";invalidate();return;}if(y>h-70){screen=LOBBY;invalidate();}
        }

        void handleQuestTap(float x,float y,int w,int h){
            float r=w*.47f;int[] ys={116,242,368};int[] max={10,5000,1};int[] reward={250,800,2500};int[] vals={questFish,questKg,questLegend};boolean[] cl={quest1Claim,quest2Claim,quest3Claim};
            for(int i=0;i<3;i++)if(x>=r-85&&x<=r-8&&y>=ys[i]+50&&y<=ys[i]+105){if(vals[i]>=max[i]&&!cl[i]){money+=reward[i];xp+=reward[i]/2;if(i==0)quest1Claim=true;else if(i==1)quest2Claim=true;else quest3Claim=true;level=Math.max(1,1+xp/500);toast="Nhận thưởng +$"+reward[i];tone(0);save();}else toast="Chưa hoàn thành hoặc đã nhận";invalidate();return;}if(y>h-70){screen=LOBBY;invalidate();}
        }

        void startFishing(){resetJoystick();phase=READY;reelHeld=false;skillFx=false;screen=FISH;toast="Cả 3 câu thủ vào vị trí. Nhấn THẢ LƯỚI.";toastUntil=System.currentTimeMillis()+2200;invalidate();}
        void toast(String s,long ms){toast=s;toastUntil=System.currentTimeMillis()+ms;}
        void tone(int kind){if(tone==null)return;try{tone.startTone(kind==0?ToneGenerator.TONE_PROP_BEEP:kind==1?ToneGenerator.TONE_PROP_ACK:kind==2?ToneGenerator.TONE_PROP_BEEP2:kind==3?ToneGenerator.TONE_PROP_PROMPT:ToneGenerator.TONE_PROP_NACK,120);}catch(Exception ignored){}}
        void drawPerson(Canvas c,float x,float y,int who,float s){p.setColor(Color.rgb(48,39,36));c.drawCircle(x,y-67*s,18*s,p);p.setColor(Color.rgb(244,211,172));c.drawCircle(x,y-64*s,16*s,p);p.setColor(body[who]);c.drawRoundRect(x-23*s,y-44*s,x+23*s,y+10*s,9*s,9*s,p);p.setColor(Color.rgb(54,51,54));c.drawRect(x-14*s,y+10*s,x-3*s,y+50*s,p);c.drawRect(x+3*s,y+10*s,x+14*s,y+50*s,p);p.setColor(Color.rgb(104,61,35));p.setStrokeWidth(5*s);c.drawLine(x+13*s,y-12*s,x+72*s,y-51*s,p);}
    }
}
