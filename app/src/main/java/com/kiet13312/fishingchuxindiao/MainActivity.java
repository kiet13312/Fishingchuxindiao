package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new FishingView(this));
    }

    static class FishingView extends View {
        Paint p = new Paint(3);
        Random r = new Random();
        Handler h = new Handler();

        String[] chars = {"Sở Tâm","Bá Thường","Lão Ngô"};
        int[] charPower = {70,95,80};
        String[] rods = {"Cần Tre","Cần Sắt","Cần Thép","Cần Vàng","Cần Thần"};
        int[] rodPrice = {0,2000,8000,30000,100000};
        int[] rodPower = {100,300,600,1200,2500};
        int[] fishW = {50,100,200,400,800,1200,2000,5000,10000};
        String[] fishN = {"Cá rô","Cá chép","Cá trắm","Cá lóc","Cá mè","Cá kiếm","Cá thần","Cá vạn cân","Cá Tề Thiên"};

        int character=0, money=1000, energy=100, rod=0;
        int state=0; // 0 ready, 1 waiting, 2 fighting
        int fishWeight=0, fishHP=0, fishMax=0;
        int tension=18;
        float fishX=.78f, fishY=.52f, fishDX=-.004f;
        long biteAt, lastTick, skillEnd, skillCooldown;
        boolean tractor=false, flash=false;
        int skillHits=0;
        String message="Sẵn sàng câu cá";
        ArrayList<Fish> bag=new ArrayList<Fish>();

        FishingView(Context c){super(c);p.setTypeface(Typeface.DEFAULT_BOLD);}

        protected void onDraw(Canvas c){
            int w=getWidth(), hh=getHeight();
            drawWorld(c,w,hh);
            long now=System.currentTimeMillis();

            if(state==1 && now>=biteAt) beginFight();
            if(state==2) updateFight(now);

            drawPlayer(c,w,hh);
            if(state==2) drawFish(c,w,hh,now);
            drawHUD(c,w,hh);
            drawControls(c,w,hh);

            if(tractor) drawTractor(c,w,hh,now);
            if(flash){
                p.setColor(Color.argb(100,255,230,60)); c.drawRect(0,0,w,hh,p);
            }
            postInvalidateDelayed(40);
        }

        void drawWorld(Canvas c,int w,int hh){
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(145,205,224)); c.drawRect(0,0,w,hh*.40f,p);
            p.setColor(Color.rgb(55,145,175)); c.drawRect(0,hh*.37f,w,hh*.82f,p);
            for(int i=0;i<13;i++){
                p.setColor(Color.argb(75,230,255,255));
                float y=hh*.42f+i*hh*.027f;
                c.drawLine(0,y,w,y+7,p);
            }
            for(int i=0;i<12;i++){
                float x=i*w/11f;
                p.setColor(Color.rgb(54,105,62)); c.drawCircle(x,hh*.34f-(i%3)*8,42,p);
                p.setColor(Color.rgb(76,130,69)); c.drawCircle(x+22,hh*.37f,31,p);
            }
            p.setColor(Color.rgb(226,205,160)); c.drawRect(0,hh*.80f,w,hh,p);
            p.setColor(Color.rgb(178,155,110));
            for(int i=0;i<45;i++) c.drawCircle((i*83)%w,hh*.83f+(i*29)%(Math.max(1,(int)(hh*.14f))),2+i%3,p);
            p.setColor(Color.rgb(104,70,40)); c.drawRoundRect(20,hh*.72f,140,hh*.79f,12,12,p);
            p.setColor(Color.DKGRAY); c.drawCircle(48,hh*.80f,13,p); c.drawCircle(112,hh*.80f,13,p);
        }

        void drawPlayer(Canvas c,int w,int hh){
            float[] xs={w*.28f,w*.45f,w*.62f}, base=hh*.76f;
            for(int i=0;i<3;i++){
                float x=xs[i];
                if(i==character){
                    p.setColor(Color.argb(90,255,220,50)); c.drawCircle(x,base-55,55,p);
                }
                p.setColor(Color.rgb(40,43,48)); c.drawRect(x-17,base-5,x-4,base+30,p); c.drawRect(x+4,base-5,x+17,base+30,p);
                p.setColor(i==0?Color.rgb(66,150,85):i==1?Color.rgb(145,72,180):Color.rgb(62,105,165));
                c.drawRoundRect(x-27,base-65,x+27,base-5,12,12,p);
                p.setColor(Color.rgb(242,205,165)); c.drawCircle(x,base-88,22,p);
                p.setColor(Color.rgb(45,35,30)); c.drawCircle(x-7,base-99,12,p);
                p.setColor(Color.rgb(95,58,30)); p.setStrokeWidth(i==character?7:5);
                c.drawLine(x+18,base-38,x+92,base-94,p);
                p.setColor(Color.WHITE); p.setTextSize(12);
                c.drawText(chars[i],x-25,base+50,p);
            }
        }

        void drawFish(Canvas c,int w,int hh,long now){
            float x=w*fishX, y=hh*fishY;
            float scale=1f+Math.min(.65f,fishWeight/10000f);
            p.setColor(Color.rgb(88,66,98));
            c.drawOval(x-72*scale,y-34*scale,x+72*scale,y+34*scale,p);
            Path q=new Path();
            q.moveTo(x+55*scale,y);
            q.lineTo(x+115*scale,y-55*scale);
            q.lineTo(x+105*scale,y);
            q.lineTo(x+115*scale,y+55*scale); q.close();
            p.setColor(Color.rgb(124,70,88)); c.drawPath(q,p);
            p.setColor(Color.WHITE); c.drawCircle(x-42*scale,y-9*scale,9,p);
            p.setColor(Color.BLACK); c.drawCircle(x-42*scale,y-9*scale,4,p);
            p.setColor(Color.argb(100,255,120,130)); c.drawOval(x-85*scale,y-48*scale,x+95*scale,y+48*scale,p);
            p.setColor(Color.argb(160,240,255,255));
            for(int i=0;i<6;i++) c.drawCircle(x-50+i*22,y+48+(float)Math.sin(now/170.0+i)*8,4,p);
        }

        void drawHUD(Canvas c,int w,int hh){
            p.setColor(Color.argb(215,18,23,28)); c.drawRoundRect(18,14,430,91,16,16,p);
            p.setColor(Color.WHITE); p.setTextSize(20); c.drawText("CÂU CÁ VẠN CÂN",32,41,p);
            p.setTextSize(14); c.drawText(chars[character]+"   $" + money + "   ENERGY "+energy,32,66,p);
            p.setTextSize(13); c.drawText(rods[rod]+" • "+rodPower[rod]+" kg",32,84,p);

            if(state==2){
                p.setColor(Color.argb(225,18,23,28)); c.drawRoundRect(w*.23f,102,w*.78f,164,16,16,p);
                p.setColor(Color.WHITE); p.setTextSize(16); center(c,fishName(),w*.505f,124);
                p.setColor(Color.DKGRAY); c.drawRoundRect(w*.27f,136,w*.74f,151,8,8,p);
                p.setColor(Color.rgb(224,58,70)); float hp=fishMax==0?0:(float)fishHP/fishMax;
                c.drawRoundRect(w*.27f,w*.0f+136,w*.27f+(w*.47f)*hp,151,8,8,p);
                p.setColor(Color.WHITE); p.setTextSize(11); center(c,fishHP+" / "+fishMax,w*.505f,148);

                p.setTextSize(13); c.drawText("ĐỘ CĂNG DÂY",25,hh*.63f,p);
                p.setColor(Color.argb(150,0,0,0)); c.drawRoundRect(25,hh*.645f,220,hh*.675f,8,8,p);
                p.setColor(tension>80?Color.RED:Color.rgb(255,190,50));
                c.drawRoundRect(25,hh*.645f,25+190*tension/100f,hh*.675f,8,8,p);
            }

            p.setColor(Color.argb(210,20,24,28)); c.drawRoundRect(20,hh-132,510,hh-94,12,12,p);
            p.setColor(Color.WHITE); p.setTextSize(14); c.drawText(message,34,hh-108,p);
        }

        void drawControls(Canvas c,int w,int hh){
            if(state==2){
                button(c,w-300,hh-92,w-145,hh-37,"CO DÂY",Color.rgb(205,72,65));
                button(c,w-455,hh-92,w-310,hh-37,"GIẢM CÁP",Color.rgb(65,120,170));
                if(character<2){
                    String s=System.currentTimeMillis()>=skillCooldown
                            ?(character==0?"XE KÉO":"PHI THIÊN VÔ CỰC")
                            :"HỒI "+((skillCooldown-System.currentTimeMillis()+999)/1000)+"s";
                    button(c,18,hh-92,210,hh-37,s,
                            System.currentTimeMillis()>=skillCooldown?(character==0?Color.rgb(60,145,80):Color.rgb(155,70,190)):Color.GRAY);
                }
            }else{
                button(c,w-300,hh-92,w-145,hh-37,state==0?"THẢ LƯỠI":"...",Color.rgb(65,120,175));
            }
            button(c,220,hh-92,365,hh-37,"BÁN CÁ",Color.rgb(75,150,85));
            if(rod<4) button(c,375,hh-92,570,hh-37,"MUA CẦN $"+rodPrice[rod+1],Color.rgb(190,145,50));
        }

        void button(Canvas c,float l,float t,float rr,float b,String s,int col){
            p.setColor(Color.argb(70,0,0,0)); c.drawRoundRect(l+3,t+4,rr+3,b+4,14,14,p);
            p.setColor(col); c.drawRoundRect(l,t,rr,b,14,14,p);
            p.setColor(Color.WHITE); p.setTextSize(s.length()>16?10:13); center(c,s,(l+rr)/2,t+(b-t)/2+5);
        }

        public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(), y=e.getY(); int w=getWidth(),hh=getHeight();

            if(y>95 && y<170){
                int i=(int)((x-15)/(w*.18f));
                if(i>=0&&i<3){character=i;message="Đã chọn "+chars[i];invalidate();return true;}
            }

            if(state==2){
                if(x>=w-310&&x<=w-135&&y>=hh-105){reel();return true;}
                if(x>=w-465&&x<=w-300&&y>=hh-105){loosen();return true;}
                if(character<2&&x>=10&&x<=220&&y>=hh-105){skill();return true;}
            }else if(x>=w-310&&x<=w-135&&y>=hh-105){cast();return true;}

            if(x>=210&&x<=375&&y>=hh-105){sell();return true;}
            if(x>=370&&x<=590&&y>=hh-105){buyRod();return true;}
            return true;
        }

        void cast(){
            if(energy<5){message="Hết năng lượng!";return;}
            energy-=5; state=1; message="Thả lưỡi... cá đang tìm mồi!";
            biteAt=System.currentTimeMillis()+1300+r.nextInt(2200); invalidate();
        }

        void beginFight(){
            state=2;
            fishWeight=fishW[r.nextInt(fishW.length)];
            fishMax=3000+fishWeight*220; fishHP=fishMax;
            tension=25; fishX=.80f; fishY=.53f; fishDX=-.004f;
            lastTick=System.currentTimeMillis();
            message="CÁ CẮN! Co dây đúng lúc, đừng để đứt!";
        }

        void updateFight(long now){
            fishX+=fishDX;
            if(fishX<.67f||fishX>.88f) fishDX=-fishDX;
            fishY=.52f+(float)Math.sin(now/230.0)*.018f;

            if(now-lastTick>850){
                tension+=4+r.nextInt(5); lastTick=now;
            }
            tension-=1;
            if(tension<8)tension=8;

            if(tension>=100){
                state=0; message="ĐỨT DÂY! Cá đã thoát."; tension=18; return;
            }
            if(fishHP<=0) landFish();
        }

        void reel(){
            if(state!=2)return;
            int damage=rodPower[rod]/3+charPower[character];
            if(skillHits>0){damage+=120;skillHits--;}
            fishHP-=damage; tension+=11+r.nextInt(6);
            message="CO DÂY! -"+damage+" HP";
            if(fishHP<0)fishHP=0; invalidate();
        }

        void loosen(){
            if(state!=2)return;
            tension-=27;if(tension<5)tension=5;
            message="GIẢM CÁP — dây đang hạ căng";invalidate();
        }

        void skill(){
            if(state!=2){message="Hãy đợi cá cắn rồi dùng kỹ năng.";return;}
            long now=System.currentTimeMillis();
            if(now<skillCooldown){message="Kỹ năng còn hồi "+((skillCooldown-now+999)/1000)+"s";return;}
            skillCooldown=now+8000; skillHits=character==0?7:9;
            if(character==0){
                tractor=true;skillEnd=now+3000;message="XE KÉO! Sở Tâm tăng lực kéo!";
                h.postDelayed(new Runnable(){public void run(){tractor=false;invalidate();}},3000);
            }else{
                flash=true;tension=Math.max(5,tension-28);message="PHI THIÊN VÔ CỰC!";
                h.postDelayed(new Runnable(){public void run(){flash=false;invalidate();}},700);
            }
            invalidate();
        }

        void drawTractor(Canvas c,int w,int hh,long now){
            float t=1-Math.max(0,skillEnd-now)/3000f;
            float x=-100+(w*.62f+100)*Math.min(1,Math.max(0,t)), y=hh*.61f;
            p.setColor(Color.rgb(55,130,58));c.drawRoundRect(x-78,y-34,x+75,y+17,12,12,p);
            p.setColor(Color.rgb(75,155,70));c.drawRoundRect(x,y-73,x+55,y-30,8,8,p);
            p.setColor(Color.rgb(190,220,225));c.drawRect(x+10,y-65,x+43,y-40,p);
            p.setColor(Color.DKGRAY);c.drawCircle(x-45,y+23,28,p);c.drawCircle(x+55,y+20,19,p);
            p.setColor(Color.BLACK);c.drawCircle(x-45,y+23,13,p);c.drawCircle(x+55,y+20,9,p);
            p.setColor(Color.rgb(242,205,165));c.drawCircle(x+20,y-92,15,p);
            p.setColor(Color.rgb(60,145,85));c.drawRect(x+5,y-78,x+35,y-48,p);
            p.setColor(Color.rgb(95,58,30));p.setStrokeWidth(6);c.drawLine(x-62,y-20,x-145,y-62,p);
            p.setColor(Color.WHITE);p.setStrokeWidth(3);c.drawLine(x-145,y-62,x-225,hh*.72f,p);
            p.setColor(Color.RED);c.drawCircle(x-225,hh*.72f,8,p);
            p.setColor(Color.WHITE);p.setTextSize(20);c.drawText("XE KÉO",x-48,y+62,p);
        }

        void landFish(){
            int reward=fishWeight*3; money+=reward; energy=Math.min(100,energy+10);
            bag.add(new Fish(fishName(),fishWeight));
            message="BẮT ĐƯỢC "+fishName()+" • "+fishWeight+" kg • +"+reward+"$";
            state=0;tension=18;fishHP=fishMax=0;
        }

        String fishName(){
            int i=0;
            if(fishWeight<=100)i=0; else if(fishWeight<=200)i=1; else if(fishWeight<=400)i=2;
            else if(fishWeight<=800)i=3; else if(fishWeight<=1200)i=4; else if(fishWeight<=2000)i=5;
            else if(fishWeight<=5000)i=6; else if(fishWeight<=10000)i=7; else i=8;
            return fishN[i];
        }

        void buyRod(){
            if(rod>=4){message="Đã có Cần Thần!";return;}
            int price=rodPrice[rod+1];
            if(money<price){message="Không đủ tiền! Cần "+price+"$";return;}
            money-=price;rod++;message="Đã mua "+rods[rod]+"!";invalidate();
        }

        void sell(){
            if(bag.size()==0){message="Kho cá trống!";return;}
            int total=0;for(Fish f:bag)total+=f.weight*3;
            money+=total;bag.clear();message="Đã bán cá +"+total+"$";invalidate();
        }

        void center(Canvas c,String s,float x,float y){
            c.drawText(s,x-p.measureText(s)/2,y,p);
        }

        static class Fish{String n;int w;Fish(String n,int w){this.n=n;this.w=w;}}
    }
}
