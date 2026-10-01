package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(0);
        setContentView(new Game(this));
    }

    static class Game extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rnd = new Random();
        final Handler handler = new Handler();

        final String[] chars = {"Sở Tâm","Bá Thường","Lão Ngô"};
        final String[] skill = {"Xe Kéo","Phi Thiên Vô Cực","Hộ Lực"};
        final int[] level = {80,100,44};
        final int[] power = {70,95,80};
        final int[] shirt = {Color.rgb(245,245,245),Color.rgb(35,35,40),Color.rgb(70,70,75)};

        final String[] rods = {"Cần Tre","Cần Sắt","Cần Thép","Cần Vàng","Cần Thần"};
        final int[] rodPower = {120,350,700,1400,3000};
        final int[] rodPrice = {0,2000,8000,30000,100000};

        final String[] fishNames = {"Cá rô","Cá chép","Cá trắm","Cá lóc","Cá mè","Cá kiếm","Cá thần","Cá vạn cân","Cá Tề Thiên"};
        final int[] fishWeights = {50,100,200,400,800,1200,2000,5000,10000};

        // 0=sảnh, 1=chọn màn, 2=chọn nhân vật, 3=shop, 4=kỹ năng, 5=trận câu
        int screen=0, selected=0, selectedLevel=1, rod=0;
        int money=12540000, energy=80;
        int fishHp=0, fishMax=0, fishWeight=0;
        int[] lineLen = {0,0,0};
        boolean[] casting = {false,false,false};
        boolean[] skillReady = {true,true,true};
        long[] skillBack = {0,0,0};
        long battleStart=0, lastDamage=0;
        boolean xeKeo=false, phiThien=false, hoLuc=false;
        long effectEnd=0;
        String toast="";

        final ArrayList<Integer> bag = new ArrayList<Integer>();

        Game(Context c) {
            super(c);
            p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            setFocusable(true);
        }

        @Override protected void onDraw(Canvas c) {
            int w=getWidth(), h=getHeight();
            if(screen==0) drawLobby(c,w,h);
            else if(screen==1) drawLevels(c,w,h);
            else if(screen==2) drawCharacters(c,w,h);
            else if(screen==3) drawShop(c,w,h);
            else if(screen==4) drawSkills(c,w,h);
            else drawBattle(c,w,h);

            if(toast.length()>0) {
                p.setColor(Color.argb(205,0,0,0));
                c.drawRoundRect(w*.35f,h*.84f,w*.65f,h*.94f,14,14,p);
                p.setColor(Color.WHITE); p.setTextSize(15);
                center(c,toast,w*.5f,h*.90f);
            }
            postInvalidateDelayed(40);
        }

        void lake(Canvas c,int w,int h) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(11,38,48)); c.drawRect(0,0,w,h,p);
            p.setColor(Color.rgb(18,67,73)); c.drawRect(0,h*.34f,w,h*.78f,p);
            p.setColor(Color.rgb(23,83,78));
            for(int i=0;i<24;i++) c.drawCircle((i*83)%w,h*.18f+(i%5)*22,65+(i%4)*12,p);
            p.setColor(Color.rgb(31,103,87));
            for(int i=0;i<20;i++) c.drawCircle((i*117)%w,h*.29f+(i%4)*12,50+(i%3)*12,p);
            p.setColor(Color.rgb(230,219,185)); c.drawRect(0,h*.70f,w,h,p);
            p.setColor(Color.argb(55,255,255,255));
            for(int i=0;i<12;i++) c.drawLine(0,h*.40f+i*h*.025f,w,h*.40f+i*h*.025f,p);
            p.setColor(Color.rgb(26,53,48)); c.drawRect(0,0,w,h*.10f,p);
        }

        void dark(Canvas c,int w,int h) {
            p.setColor(Color.argb(190,0,0,0)); c.drawRect(0,0,w,h,p);
        }

        void title(Canvas c,String s,float x,float y,float size) {
            p.setColor(Color.WHITE); p.setTextSize(size); center(c,s,x,y);
        }

        void button(Canvas c,float l,float t,float r,float b,String s,int color) {
            p.setColor(Color.argb(75,0,0,0)); c.drawRoundRect(l+4,t+5,r+4,b+5,12,12,p);
            p.setColor(color); c.drawRoundRect(l,t,r,b,12,12,p);
            p.setColor(Color.WHITE); p.setTextSize(Math.min(18,(r-l)/Math.max(5,s.length()/1.7f)));
            center(c,s,(l+r)/2f,(t+b)/2f+6);
        }

        void leftMenu(Canvas c,int w,int h) {
            String[] m={"Người bạn câu cá","Cách đánh cá","Cây câu","Quán cá"};
            for(int i=0;i<4;i++) {
                float y=125+i*64;
                p.setColor(Color.argb(225,8,12,15)); c.drawRect(28,y,205,y+52,p);
                p.setColor(Color.WHITE); p.setTextSize(15); c.drawText(m[i],50,y+32,p);
            }
            p.setColor(Color.YELLOW); p.setTextSize(12);
            c.drawText("Đã tích lũy vàng:",35,408,p);
            c.drawText("28 triệu",85,427,p);
        }

        void topMoney(Canvas c,int w) {
            p.setColor(Color.argb(210,8,12,15)); c.drawRoundRect(w-350,15,w-20,67,18,18,p);
            p.setColor(Color.YELLOW); p.setTextSize(15); c.drawText("Đồng cấp VIP",w-325,37,p);
            p.setColor(Color.WHITE); p.setTextSize(17); c.drawText(format(money)+"+",w-210,39,p);
            p.setTextSize(14); c.drawText("Tiền: "+format(money),w-325,58,p);
        }

        void drawLobby(Canvas c,int w,int h) {
            lake(c,w,h);
            topMoney(c,w);
            p.setColor(Color.WHITE); p.setTextSize(18); c.drawText("con ng",82,42,p);
            p.setTextSize(14); c.drawText("Lv 80",82,68,p);
            p.setColor(Color.rgb(42,120,230)); c.drawRect(82,78,250,92,p);
            p.setColor(Color.WHITE); p.setTextSize(12); c.drawText("Thể lực: "+energy+"/60",82,111,p);

            leftMenu(c,w,h);

            // Xe kéo và ba nhân vật giống bố cục video
            drawCart(c,w*.42f,h*.56f,1.0f);
            drawPerson(c,w*.39f,h*.54f,0,1.0f);
            drawPerson(c,w*.47f,h*.54f,1,1.0f);
            drawPerson(c,w*.55f,h*.54f,2,1.0f);

            p.setColor(Color.WHITE); p.setTextSize(25);
            c.drawText("Câu Cá Vạn Cân",w*.42f,h*.25f,p);
            button(c,w*.80f,h*.75f,w*.96f,h*.88f,"ĐI CÂU CÁ",Color.rgb(239,239,239));
            p.setColor(Color.BLACK); p.setTextSize(17);
            center(c,"ĐI CÂU CÁ",w*.88f,h*.825f);
            button(c,w*.80f,h*.59f,w*.96f,h*.69f,"CỬA HÀNG",Color.rgb(58,122,182));
            button(c,w*.58f,h*.75f,w*.76f,h*.88f,"KỸ NĂNG",Color.rgb(143,78,174));
            button(c,w*.38f,h*.75f,w*.56f,h*.88f,"NHÂN VẬT",Color.rgb(70,120,90));
        }

        void drawLevels(Canvas c,int w,int h) {
            lake(c,w,h); dark(c,w,h);
            title(c,"Quay lại trang chủ",100,45,18);
            title(c,"CHỌN MÀN",w/2f,65,25);
            for(int i=0;i<9;i++) {
                int row=i/3,col=i%3;
                float l=260+col*220,t=110+row*135;
                int color=i+1==selectedLevel?Color.rgb(245,190,35):Color.rgb(55,112,175);
                button(c,l,t,l+180,t+88,"MÀN "+(i+1),color);
                p.setColor(Color.WHITE); p.setTextSize(12);
                center(c,"Cá tối đa: "+(i+1)*100+" kg",l+90,t+112);
            }
            button(c,35,h-70,190,h-22,"QUAY LẠI",Color.rgb(70,95,110));
            button(c,w-245,h-70,w-35,h-22,"CHỌN NHÂN VẬT",Color.rgb(45,140,92));
        }

        void drawCharacters(Canvas c,int w,int h) {
            lake(c,w,h); dark(c,w,h);
            title(c,"CHỌN NHÂN VẬT",w/2f,48,24);
            for(int i=0;i<3;i++) {
                float x=250+i*255;
                p.setColor(i==selected?Color.argb(100,255,215,50):Color.argb(80,255,255,255));
                c.drawRoundRect(x-105,85,x+105,430,18,18,p);
                drawPerson(c,x,180,i,1.8f);
                p.setColor(Color.YELLOW); p.setTextSize(18); center(c,chars[i],x,300);
                p.setColor(Color.WHITE); p.setTextSize(13);
                center(c,"Lv "+level[i],x,325);
                center(c,"Lực câu: "+(power[i]+rod*20),x,347);
                center(c,"Chiêu: "+skill[i],x,375);
                button(c,x-75,392,x+75,425,i==selected?Color.rgb(230,170,30):Color.rgb(50,120,180),i==selected?"ĐÃ CHỌN":"CHỌN");
            }
            button(c,30,h-65,180,h-20,"QUAY LẠI",Color.rgb(65,100,125));
            button(c,w-220,h-65,w-30,h-20,"BẮT ĐẦU",Color.rgb(45,145,85));
        }

        void drawShop(Canvas c,int w,int h) {
            lake(c,w,h); dark(c,w,h);
            title(c,"CỬA HÀNG CẦN CÂU",w/2f,48,24);
            p.setColor(Color.YELLOW); p.setTextSize(18); c.drawText("Tiền: "+format(money)+"$",35,55,p);
            for(int i=0;i<5;i++) {
                float l=230+(i%3)*250,t=95+(i/3)*190;
                p.setColor(i==rod?Color.rgb(70,110,90):Color.argb(220,20,25,28));
                c.drawRoundRect(l,t,l+210,t+150,15,15,p);
                p.setColor(Color.WHITE); p.setTextSize(17); center(c,rods[i],l+105,t+30);
                p.setTextSize(13); center(c,"Lực kéo "+rodPower[i]+" kg",l+105,t+58);
                if(i==rod) button(c,l+35,t+90,l+175,t+132,"ĐANG DÙNG",Color.rgb(60,145,85));
                else button(c,l+35,t+90,l+175,t+132,rodPrice[i]+"$",money>=rodPrice[i]?Color.rgb(52,120,180):Color.GRAY);
            }
            button(c,30,h-65,180,h-20,"QUAY LẠI",Color.rgb(65,100,125));
        }

        void drawSkills(Canvas c,int w,int h) {
            lake(c,w,h); dark(c,w,h);
            title(c,"MENU KỸ NĂNG",w/2f,48,24);
            for(int i=0;i<3;i++) {
                float x=230+i*270;
                p.setColor(Color.argb(225,14,18,22)); c.drawRoundRect(x-110,95,x+110,400,18,18,p);
                drawPerson(c,x,170,i,1.45f);
                p.setColor(Color.YELLOW); p.setTextSize(17); center(c,chars[i],x,285);
                p.setColor(Color.WHITE); p.setTextSize(14); center(c,skill[i],x,318);
                center(c,"Tiêu hao thể lực: "+(i==0?60:i==1?80:40),x,346);
                center(c,"Hồi chiêu: "+(i+1)*5+" giây",x,368);
                button(c,x-80,385,x+80,425,"XEM CHI TIẾT",Color.rgb(55,120,180));
            }
            button(c,30,h-65,180,h-20,"QUAY LẠI",Color.rgb(65,100,125));
        }

        void drawBattle(Canvas c,int w,int h) {
            lake(c,w,h);
            topMoney(c,w);
            p.setColor(Color.WHITE); p.setTextSize(15); c.drawText("con ng",70,37,p);
            c.drawText("Lv 80",70,60,p);
            c.drawText("Thể lực: "+energy+"/60",70,82,p);

            // Thanh máu cá ở giữa giống bố cục video
            p.setColor(Color.RED); p.setTextSize(20); center(c,"Hạo Đạo Đế",w*.52f,40);
            p.setColor(Color.DKGRAY); c.drawRoundRect(w*.34f,52,w*.70f,76,7,7,p);
            p.setColor(Color.RED); float hp=(float)fishHp/Math.max(1,fishMax);
            c.drawRoundRect(w*.34f,52,w*.34f+w*.36f*hp,76,7,7,p);
            p.setColor(Color.WHITE); p.setTextSize(12); center(c,fishHp+"/"+fishMax,w*.52f,69);

            // Cả 3 nhân vật cùng thả câu
            float[] xs={w*.48f,w*.58f,w*.68f};
            for(int i=0;i<3;i++) {
                drawPerson(c,xs[i],h*.63f,i,1.15f);
                float ex=w*(.43f+i*.07f), ey=h*(.49f-(i%2)*.04f);
                p.setColor(Color.WHITE); p.setStrokeWidth(2.5f);
                c.drawLine(xs[i]+35,h*.59f,ex,ey,p);
                if(casting[i]) {
                    p.setColor(Color.rgb(240,240,240));
                    c.drawCircle(ex,ey,5, p);
                }
            }

            // cá
            float fx=w*.49f, fy=h*.48f;
            p.setColor(Color.rgb(55,100,125)); c.drawOval(fx-105,fy-28,fx+105,fy+28,p);
            Path tail=new Path(); tail.moveTo(fx+85,fy); tail.lineTo(fx+145,fy-55); tail.lineTo(fx+130,fy); tail.lineTo(fx+145,fy+55); tail.close();
            p.setColor(Color.rgb(120,65,90)); c.drawPath(tail,p);
            p.setColor(Color.WHITE); c.drawCircle(fx-65,fy-6,9,p); p.setColor(Color.BLACK); c.drawCircle(fx-65,fy-6,4,p);

            // thanh kỹ năng từng nhân vật
            for(int i=0;i<3;i++) {
                float l=35+i*225;
                button(c,l,h-75,l+200,h-25,chars[i]+" • "+skill[i],skillReady[i]?Color.rgb(58,115,175):Color.GRAY);
            }
            button(c,w-230,h-150,w-110,h-105,"THẢ LƯỠI",Color.rgb(55,125,175));
            button(c,w-230,h-95,w-110,h-40,"CO LẠI DÂY",Color.rgb(30,30,35));

            if(xeKeo) drawXeKeo(c,w,h);
            if(phiThien) drawPhiThien(c,w,h);
            if(hoLuc) drawHoLuc(c,w,h);
        }

        void drawPerson(Canvas c,float x,float y,int who,float s) {
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(35,31,29)); c.drawCircle(x,y-48*s,20*s,p);
            p.setColor(Color.rgb(238,199,160)); c.drawCircle(x,y-30*s,18*s,p);
            p.setColor(shirt[who]); c.drawRoundRect(x-24*s,y-10*s,x+24*s,y+52*s,9,9,p);
            p.setColor(Color.rgb(45,45,48)); c.drawRect(x-14*s,y+52*s,x-3*s,y+86*s,p); c.drawRect(x+3*s,y+52*s,x+14*s,y+86*s,p);
            p.setColor(Color.rgb(120,75,38)); p.setStrokeWidth(6*s);
            c.drawLine(x+17*s,y+8*s,x+92*s,y-35*s,p);
        }

        void drawCart(Canvas c,float x,float y,float s) {
            p.setColor(Color.rgb(83,55,35)); c.drawRect(x-100*s,y-35*s,x+40*s,y+35*s,p);
            p.setColor(Color.rgb(40,100,130)); c.drawRoundRect(x+30*s,y-20*s,x+145*s,y+30*s,15,15,p);
            p.setColor(Color.DKGRAY); c.drawCircle(x-55*s,y+48*s,30*s,p); c.drawCircle(x+105*s,y+48*s,24*s,p);
            p.setColor(Color.LTGRAY); c.drawCircle(x-55*s,y+48*s,13*s,p); c.drawCircle(x+105*s,y+48*s,11*s,p);
        }

        void drawXeKeo(Canvas c,int w,int h) {
            float x=(float)((System.currentTimeMillis()/5)% (w+260))-130;
            float y=h*.52f;
            p.setColor(Color.rgb(40,120,55)); c.drawRoundRect(x-75,y-28,x+80,y+22,12,12,p);
            p.setColor(Color.DKGRAY); c.drawCircle(x-48,y+28,24,p); c.drawCircle(x+55,y+25,18,p);
            p.setColor(Color.WHITE); p.setTextSize(22); center(c,"XE KÉO",x,y-45);
        }

        void drawPhiThien(Canvas c,int w,int h) {
            float cx=w*.38f,cy=h*.50f,r=60+(float)(System.currentTimeMillis()%500)/5f;
            p.setColor(Color.argb(80,255,190,20)); c.drawCircle(cx,cy,r,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(15); p.setColor(Color.rgb(255,190,20));
            c.drawCircle(cx,cy,r,p); c.drawCircle(cx,cy,r*.65f,p);
            p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); p.setTextSize(25); center(c,"PHI THIÊN VÔ CỰC",cx,cy);
        }

        void drawHoLuc(Canvas c,int w,int h) {
            p.setColor(Color.argb(150,70,170,255)); c.drawCircle(w*.63f,h*.52f,80,p);
            p.setColor(Color.WHITE); p.setTextSize(22); center(c,"HỘ LỰC",w*.63f,h*.52f);
        }

        @Override public boolean onTouchEvent(android.view.MotionEvent e) {
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY(); int w=getWidth(), h=getHeight();

            if(screen==0) {
                if(y>h*.70f && x>w*.78f){screen=1;}
                else if(y>h*.70f && x>w*.56f){screen=4;}
                else if(y>h*.70f && x>w*.36f){screen=2;}
                else if(y>h*.55f && x>w*.78f){screen=3;}
            } else if(screen==1) {
                if(y>h-90 && x>w-270){screen=2;}
                else if(y>h-90 && x<210){screen=0;}
                else {
                    for(int i=0;i<9;i++){
                        int row=i/3,col=i%3;
                        float l=260+col*220,t=110+row*135;
                        if(x>=l&&x<=l+180&&y>=t&&y<=t+88) selectedLevel=i+1;
                    }
                }
            } else if(screen==2) {
                if(y>h-90 && x>w-250){screen=5;startBattle();}
                else if(y>h-90 && x<210){screen=1;}
                else {
                    for(int i=0;i<3;i++){
                        float xx=250+i*255;
                        if(x>xx-110&&x<xx+110&&y>85&&y<430) selected=i;
                    }
                }
            } else if(screen==3) {
                if(y>h-90&&x<210) screen=0;
                else {
                    for(int i=0;i<5;i++){
                        float l=230+(i%3)*250,t=95+(i/3)*190;
                        if(x>l&&x<l+210&&y>t+75&&y<t+145&&i!=rod&&money>=rodPrice[i]){
                            money-=rodPrice[i]; rod=i; toast="Đã mua "+rods[i];
                        }
                    }
                }
            } else if(screen==4) {
                if(y>h-90&&x<210) screen=0;
            } else if(screen==5) {
                for(int i=0;i<3;i++){
                    float l=35+i*225;
                    if(x>l&&x<l+200&&y>h-90) useSkill(i);
                }
                if(x>w-245&&x<w-95&&y>h-165&&y<h-100) castAll();
                if(x>w-245&&x<w-95&&y>h-105) reelAll();
            }
            invalidate();
            return true;
        }

        void startBattle() {
            screen=5; fishWeight=fishWeights[rnd.nextInt(fishWeights.length)];
            fishMax=5000000+fishWeight*1000; fishHp=fishMax;
            for(int i=0;i<3;i++){casting[i]=true;lineLen[i]=3;}
            energy=Math.max(0,energy-10);
            battleStart=System.currentTimeMillis(); lastDamage=battleStart;
            toast="Cả 3 nhân vật cùng thả câu!";
        }

        void castAll() {
            for(int i=0;i<3;i++) casting[i]=true;
            toast="Ba người cùng thả lưỡi";
        }

        void reelAll() {
            if(fishHp<=0)return;
            int damage=rodPower[rod]/2+power[0]+power[1]+power[2];
            if(xeKeo) damage*=2;
            if(phiThien) damage*=3;
            if(hoLuc) damage+=1000;
            fishHp-=damage;
            if(fishHp<0)fishHp=0;
            toast="Cả 3 cùng kéo • -"+damage;
            if(fishHp==0){
                int reward=Math.max(100,fishWeight*25);
                money+=reward; bag.add(fishWeight);
                toast="Bắt được "+fishWeight+" kg • +"+reward+"$";
            }
        }

        void useSkill(final int i) {
            long now=System.currentTimeMillis();
            if(!skillReady[i] || now<skillBack[i]){toast="Kỹ năng đang hồi";return;}
            if(energy<(i==0?60:i==1?80:40)){toast="Không đủ thể lực";return;}
            energy-=i==0?60:i==1?80:40;
            skillReady[i]=false; skillBack[i]=now+(i+1)*5000;
            if(i==0){xeKeo=true;effectEnd=now+3000;toast="SỞ TÂM • XE KÉO!";}
            else if(i==1){phiThien=true;effectEnd=now+2500;toast="BÁ THƯỜNG • PHI THIÊN VÔ CỰC!";}
            else {hoLuc=true;effectEnd=now+2500;toast="LÃO NGÔ • HỘ LỰC!";}
            handler.postDelayed(new Runnable(){public void run(){skillReady[i]=true;invalidate();}},(i+1)*5000);
            handler.postDelayed(new Runnable(){public void run(){xeKeo=false;phiThien=false;hoLuc=false;invalidate();}},i==0?3000:2500);
        }

        String format(int n){return String.format("%,d",n).replace(',','.');}
        void center(Canvas c,String s,float x,float y){c.drawText(s,x-p.measureText(s)/2f,y,p);}
    }
}
