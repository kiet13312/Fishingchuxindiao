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
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new FishingGame(this));
    }

    static class FishingGame extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Path path = new Path();
        final Random random = new Random();
        final Handler handler = new Handler();

        final String[] names = {"Sở Tâm","Bá Thường","Lão Ngô"};
        final String[] skills = {"XE KÉO","PHI THIÊN VÔ CỰC","HỘ LỰC"};
        final int[] basePower = {70,95,80};
        final int[] shirt = {Color.rgb(74,147,87),Color.rgb(136,71,173),Color.rgb(60,102,165)};

        final String[] rodNames = {"Cần Tre","Cần Sắt","Cần Thép","Cần Vàng","Cần Thần"};
        final int[] rodPrice = {0,2000,8000,30000,100000};
        final int[] rodPower = {120,350,700,1400,3000};

        final int[] weights = {50,100,200,400,800,1200,2000,5000,10000};
        final String[] fishNames = {"Cá rô","Cá chép","Cá trắm","Cá lóc","Cá mè","Cá kiếm","Cá thần","Cá vạn cân","Cá Tề Thiên"};

        int selected=0,money=1000,energy=100,rod=0,state=0;
        int fishWeight=0,fishHp=0,fishMaxHp=0,tension=20;
        float fishX=.74f,fishY=.53f,fishSpeed=.0028f;
        long biteAt=0,lastTick=0,skillEnd=0,skillCooldownEnd=0;
        int skillHits=0;
        boolean tractor=false,flash=false,equipment=false;
        String message="Sẵn sàng câu cá";
        final ArrayList<Fish> bag=new ArrayList<Fish>();

        FishingGame(Context c){super(c);p.setTypeface(Typeface.DEFAULT_BOLD);setFocusable(true);}

        @Override protected void onDraw(Canvas c){
            int w=getWidth(),h=getHeight(); long now=System.currentTimeMillis();
            drawBackground(c,w,h,now);
            if(state==1 && now>=biteAt) beginBattle();
            if(state==2) updateBattle(now);
            drawTop(c,w,h);
            drawPlayers(c,w,h);
            if(state==2) drawFish(c,w,h,now);
            drawBottom(c,w,h,now);
            if(tractor) drawTractor(c,w,h,now);
            if(flash) drawFlash(c,w,h);
            if(equipment) drawEquipment(c,w,h);
            postInvalidateDelayed(40);
        }

        void drawBackground(Canvas c,int w,int h,long now){
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(102,169,188)); c.drawRect(0,0,w,h,p);
            p.setColor(Color.rgb(29,61,44));
            for(int i=0;i<18;i++) c.drawCircle(i*w/17f,h*.34f-(i%4)*10,62+(i%3)*12,p);
            p.setColor(Color.rgb(52,95,57));
            for(int i=0;i<22;i++) c.drawCircle(i*w/21f+20,h*.41f-(i%5)*7,36+(i%2)*8,p);
            p.setColor(Color.rgb(63,151,174)); c.drawRect(0,h*.38f,w,h*.78f,p);
            for(int i=0;i<14;i++){
                float y=h*.42f+i*h*.025f;
                p.setColor(Color.argb(78,235,255,255)); c.drawLine(0,y,w,y+(i%2==0?5:-3),p);
            }
            p.setColor(Color.rgb(229,214,178)); c.drawRect(0,h*.77f,w,h,p);
            p.setColor(Color.rgb(196,178,143));
            for(int i=0;i<70;i++) c.drawCircle((i*73)%w,h*.79f+(i*31)%Math.max(1,(int)(h*.18f)),2+i%3,p);
            p.setColor(Color.rgb(88,64,42)); c.drawRoundRect(22,h*.68f,138,h*.78f,12,12,p);
            p.setColor(Color.DKGRAY); c.drawCircle(49,h*.80f,14,p); c.drawCircle(112,h*.80f,14,p);
            p.setColor(Color.rgb(177,139,84)); c.drawRect(35,h*.64f,122,h*.69f,p);
        }

        void drawTop(Canvas c,int w,int h){
            p.setColor(Color.WHITE); p.setTextSize(17); c.drawText("‹  Quay lại trang chủ",24,30,p);
            p.setTextSize(19); c.drawText("Câu cá vạn cân",32,70,p);
            p.setTextSize(12); c.drawText("Lv "+level(selected),34,92,p);

            if(state==2){
                p.setColor(Color.argb(185,12,15,18)); c.drawRoundRect(w*.33f,18,w*.73f,92,16,16,p);
                p.setColor(Color.WHITE); p.setTextSize(16); c.drawText("Lv 100   "+currentFishName(),w*.36f,42,p);
                p.setColor(Color.DKGRAY); c.drawRoundRect(w*.36f,53,w*.70f,69,8,8,p);
                float ratio=fishMaxHp==0?0:(float)fishHp/fishMaxHp;
                p.setColor(Color.rgb(226,56,68)); c.drawRoundRect(w*.36f,53,w*.36f+(w*.34f)*ratio,69,8,8,p);
                p.setColor(Color.WHITE); p.setTextSize(10); c.drawText(fishHp+"/"+fishMaxHp,w*.505f,65,p);
            }

            p.setColor(Color.argb(190,18,20,23)); c.drawRoundRect(w-372,14,w-18,94,18,18,p);
            p.setColor(Color.rgb(255,223,42)); p.setTextSize(15); c.drawText("Đồng cấp VIP",w-354,39,p);
            p.setColor(Color.WHITE); p.setTextSize(16); c.drawText("$ "+money,w-246,39,p);
            p.setTextSize(13); c.drawText("ENERGY "+energy,w-354,69,p); c.drawText(rodNames[rod]+" • "+rodPower[rod]+" kg",w-255,69,p);
            p.setTextSize(18); c.drawText("☻  •  …  ×",w-101,42,p);
        }

        void drawPlayers(Canvas c,int w,int h){
            float[] xs={w*.25f,w*.38f,w*.51f}; float base=h*.77f;
            for(int i=0;i<3;i++){
                float x=xs[i];
                if(i==selected){
                    p.setColor(Color.argb(90,255,220,50)); c.drawCircle(x,base-48,58,p);
                }
                p.setColor(Color.rgb(48,39,34)); c.drawCircle(x-5,base-108,22,p);
                p.setColor(Color.rgb(242,205,164)); c.drawCircle(x,base-94,19,p);
                p.setColor(shirt[i]); c.drawRoundRect(x-24,base-74,x+24,base-18,10,10,p);
                p.setColor(Color.rgb(43,46,50)); c.drawRect(x-15,base-18,x-3,base+24,p); c.drawRect(x+3,base-18,x+15,base+24,p);
                p.setColor(Color.rgb(94,57,30)); p.setStrokeWidth(i==selected?7:5); c.drawLine(x+15,base-40,x+105,base-100,p);
                p.setColor(Color.WHITE); p.setTextSize(11); c.drawText(names[i],x-22,base+43,p); p.setTextSize(9); c.drawText("Lv "+level(i),x-12,base+57,p);
            }
            // line of selected character emphasized
            float sx=xs[selected]+105,sy=base-100;
            float ex=state==2?w*fishX:w*.70f,ey=state==2?h*fishY:h*.69f;
            path.reset(); path.moveTo(sx,sy); path.quadTo((sx+ex)/2f,sy-45,ex,ey);
            p.setColor(Color.WHITE); p.setStrokeWidth(3.5f); p.setStyle(Paint.Style.STROKE); c.drawPath(path,p); p.setStyle(Paint.Style.FILL);
        }

        void drawFish(Canvas c,int w,int h,long now){
            float x=w*fishX,y=h*fishY,s=1f+Math.min(.75f,fishWeight/10000f);
            p.setColor(Color.rgb(91,59,96)); c.drawOval(x-86*s,y-38*s,x+86*s,y+38*s,p);
            path.reset(); path.moveTo(x+64*s,y); path.lineTo(x+132*s,y-62*s); path.lineTo(x+118*s,y); path.lineTo(x+132*s,y+62*s); path.close();
            p.setColor(Color.rgb(136,72,87)); c.drawPath(path,p);
            p.setColor(Color.WHITE); c.drawCircle(x-48*s,y-10*s,10*s,p); p.setColor(Color.BLACK); c.drawCircle(x-48*s,y-10*s,4*s,p);
            p.setColor(Color.rgb(175,91,109)); c.drawOval(x-10*s,y-61*s,x+48*s,y-24*s,p);
            p.setColor(Color.argb(150,235,252,255));
            for(int i=0;i<8;i++) c.drawCircle(x-70+i*22,y+52+(float)Math.sin(now/170.0+i)*8,3+i%2,p);
        }

        void drawBottom(Canvas c,int w,int h,long now){
            p.setColor(Color.argb(212,16,19,23)); c.drawRoundRect(16,h-108,525,h-18,18,18,p);
            for(int i=0;i<3;i++){
                float l=28+i*156; boolean active=i==selected;
                p.setColor(active?Color.rgb(255,213,52):Color.rgb(59,65,73)); c.drawRoundRect(l,h-98,l+145,h-29,12,12,p);
                p.setColor(shirt[i]); c.drawCircle(l+28,h-63,20,p);
                p.setColor(Color.WHITE); p.setTextSize(11); c.drawText(names[i],l+54,h-67,p); p.setTextSize(9); c.drawText("Lực "+(basePower[i]+rod*20),l+54,h-50,p);
            }

            p.setColor(Color.argb(205,14,17,20)); c.drawRoundRect(20,h-157,w-320,h-118,12,12,p);
            p.setColor(Color.WHITE); p.setTextSize(13); c.drawText(message,34,h-132,p);

            smallButton(c,550,h-94,670,h-39,"KHO "+bag.size(),Color.rgb(70,112,155));
            smallButton(c,680,h-94,815,h-39,"TRANG BỊ",Color.rgb(103,88,150));

            if(state==0) smallButton(c,825,h-94,970,h-39,"THẢ LƯỠI",Color.rgb(59,132,181));
            if(state==2){
                smallButton(c,w-225,h-153,w-112,h-110,"GIẢM CÁP",Color.rgb(56,101,135));
                float cx=w-91,cy=h-76;
                p.setColor(Color.argb(80,0,0,0)); c.drawCircle(cx+4,cy+5,62,p);
                p.setColor(Color.rgb(28,31,34)); c.drawCircle(cx,cy,60,p);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(Color.WHITE); c.drawCircle(cx,cy,55,p); p.setStyle(Paint.Style.FILL);
                p.setColor(Color.WHITE); p.setTextSize(15); center(c,"Co lại dây",cx,cy+5);

                boolean ready=now>=skillCooldownEnd;
                String s=ready?skills[selected]:"HỒI "+((skillCooldownEnd-now+999)/1000)+"s";
                int col=ready?(selected==0?Color.rgb(63,146,81):selected==1?Color.rgb(151,69,185):Color.rgb(70,108,163)):Color.GRAY;
                smallButton(c,825,h-153,1020,h-110,s,col);
            }
        }

        void smallButton(Canvas c,float l,float t,float rr,float b,String text,int col){
            p.setColor(Color.argb(70,0,0,0)); c.drawRoundRect(l+3,t+4,rr+3,b+4,12,12,p);
            p.setColor(col); c.drawRoundRect(l,t,rr,b,12,12,p);
            p.setColor(Color.WHITE); p.setTextSize(text.length()>14?10:12); center(c,text,(l+rr)/2f,t+(b-t)/2f+4);
        }

        void drawFlash(Canvas c,int w,int h){
            p.setColor(Color.argb(100,255,235,65)); c.drawRect(0,0,w,h,p);
            p.setColor(Color.argb(170,255,220,50)); c.drawCircle(w*.53f,h*.60f,80,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(8); c.drawCircle(w*.53f,h*.60f,110,p); p.setStyle(Paint.Style.FILL);
        }

        void drawTractor(Canvas c,int w,int h,long now){
            float t=1-Math.max(0,tractorEnd-now)/3000f; float x=-110+(w*.65f+110)*Math.min(1,Math.max(0,t)),y=h*.60f;
            p.setColor(Color.rgb(55,132,58)); c.drawRoundRect(x-78,y-34,x+75,y+18,12,12,p);
            p.setColor(Color.rgb(76,157,69)); c.drawRoundRect(x-3,y-77,x+55,y-31,9,9,p);
            p.setColor(Color.rgb(190,221,226)); c.drawRect(x+8,y-68,x+45,y-42,p);
            p.setColor(Color.DKGRAY); c.drawCircle(x-44,y+23,28,p); c.drawCircle(x+55,y+20,19,p);
            p.setColor(Color.BLACK); c.drawCircle(x-44,y+23,13,p); c.drawCircle(x+55,y+20,9,p);
            p.setColor(Color.rgb(242,205,164)); c.drawCircle(x+18,y-95,16,p); p.setColor(shirt[0]); c.drawRect(x+3,y-80,x+35,y-48,p);
            p.setColor(Color.rgb(94,57,30)); p.setStrokeWidth(7); c.drawLine(x-62,y-20,x-148,y-62,p);
            p.setColor(Color.WHITE); p.setStrokeWidth(3); c.drawLine(x-148,y-62,x-230,h*.71f,p);
            p.setColor(Color.RED); c.drawCircle(x-230,h*.71f,8,p);
            p.setColor(Color.WHITE); p.setTextSize(20); c.drawText("XE KÉO",x-48,y+66,p);
        }

        void drawEquipment(Canvas c,int w,int h){
            p.setColor(Color.argb(238,7,9,12)); c.drawRect(0,0,w,h,p);
            p.setColor(Color.WHITE); p.setTextSize(21); c.drawText("Trang bị cần câu",34,46,p);
            p.setTextSize(13); c.drawText("Chọn cần để tăng lực kéo",35,70,p);
            for(int i=0;i<rodNames.length;i++){
                float y=92+i*75;
                p.setColor(i==rod?Color.rgb(255,216,46):Color.rgb(51,56,63)); c.drawRoundRect(28,y,w-28,y+59,12,12,p);
                p.setColor(i==rod?Color.BLACK:Color.WHITE); p.setTextSize(15); c.drawText(rodNames[i],47,y+25,p); p.setTextSize(11); c.drawText("Lực kéo "+rodPower[i]+" kg",47,y+45,p);
                if(i>rod) c.drawText("Giá "+rodPrice[i]+"$",w-165,y+34,p); else if(i==rod) c.drawText("Đang dùng",w-160,y+34,p);
            }
            smallButton(c,32,h-62,185,h-23,"ĐÓNG",Color.rgb(55,112,155));
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(),y=e.getY(); int w=getWidth(),h=getHeight();

            if(equipment){
                if(y>=h-90){equipment=false;invalidate();}
                return true;
            }

            if(y>=h-108&&y<=h-18&&x>=16&&x<=525){
                int i=(int)((x-28)/156f); if(i>=0&&i<3){selected=i;message="Đã chọn "+names[i];invalidate();return true;}
            }

            if(x>=680&&x<=815&&y>=h-105){equipment=true;invalidate();return true;}
            if(x>=550&&x<=670&&y>=h-105){sellFish();return true;}

            if(state==0&&x>=825&&x<=970&&y>=h-110){castLine();return true;}

            if(state==2){
                float cx=w-91,cy=h-76;
                if(Math.hypot(x-cx,y-cy)<=70){reel();return true;}
                if(x>=w-225&&x<=w-112&&y>=h-170&&y<=h-104){loosen();return true;}
                if(x>=825&&x<=1020&&y>=h-170&&y<=h-104){useSkill();return true;}
            }
            return true;
        }

        void castLine(){
            if(energy<5){message="Hết năng lượng!";invalidate();return;}
            energy-=5;state=1;biteAt=System.currentTimeMillis()+1400+random.nextInt(2600);
            message="Thả lưỡi... chờ cá cắn";invalidate();
        }

        void beginBattle(){
            state=2; fishWeight=weights[random.nextInt(weights.length)];
            fishMaxHp=4500+fishWeight*250; fishHp=fishMaxHp; tension=24;
            fishX=.78f; fishY=.52f; fishSpeed=random.nextBoolean()?.0028f:-.0028f;
            lastTick=System.currentTimeMillis(); skillHits=0;
            message="Cá cắn! Co lại dây, tránh để đứt dây.";
        }

        void updateBattle(long now){
            fishX+=fishSpeed; if(fishX<.65f||fishX>.88f) fishSpeed=-fishSpeed;
            fishY=.52f+(float)Math.sin(now/240.0)*.025f;
            if(now-lastTick>700){tension+=5+random.nextInt(5);lastTick=now;}
            tension--; if(tension<7)tension=7;
            if(tension>=100){state=0;fishHp=fishMaxHp=0;tension=18;message="Đứt dây! Cá chạy mất.";return;}
            if(fishHp<=0) landFish();
        }

        void reel(){
            if(state!=2)return;
            int damage=rodPower[rod]/3+basePower[selected];
            if(skillHits>0){damage+=130;skillHits--;}
            fishHp-=damage;tension+=10+random.nextInt(8);
            if(fishHp<0)fishHp=0; message="Co lại dây  -"+damage+" HP";invalidate();
        }

        void loosen(){
            if(state!=2)return;
            tension-=29;if(tension<5)tension=5;
            message="Giảm cáp  •  Hạ độ căng dây";invalidate();
        }

        void useSkill(){
            if(state!=2){message="Chỉ dùng kỹ năng khi cá đã cắn.";invalidate();return;}
            long now=System.currentTimeMillis();
            if(now<skillCooldownEnd){message="Kỹ năng đang hồi "+((skillCooldownEnd-now+999)/1000)+"s";invalidate();return;}
            skillCooldownEnd=now+8000;
            if(selected==0){
                tractor=true;tractorEnd=now+3000;skillHits=8;message="XE KÉO! Sở Tâm tăng lực kéo.";
                handler.postDelayed(new Runnable(){@Override public void run(){tractor=false;invalidate();}},3000);
            }else if(selected==1){
                flash=true;skillHits=10;tension-=30;if(tension<5)tension=5;message="PHI THIÊN VÔ CỰC!";
                handler.postDelayed(new Runnable(){@Override public void run(){flash=false;invalidate();}},750);
            }else{
                flash=true;skillHits=6;tension-=18;if(tension<5)tension=5;message="HỘ LỰC! Lão Ngô tăng lực kéo.";
                handler.postDelayed(new Runnable(){@Override public void run(){flash=false;invalidate();}},550);
            }
            invalidate();
        }

        void landFish(){
            int reward=fishWeight*3; money+=reward; energy=Math.min(100,energy+10);
            bag.add(new Fish(currentFishName(),fishWeight)); state=0;tension=18;fishHp=fishMaxHp=0;
            message="Bắt được "+currentFishName()+" • "+fishWeight+" kg • +"+reward+"$";
        }

        String currentFishName(){
            if(fishWeight<=50)return fishNames[0];
            if(fishWeight<=100)return fishNames[1];
            if(fishWeight<=200)return fishNames[2];
            if(fishWeight<=400)return fishNames[3];
            if(fishWeight<=800)return fishNames[4];
            if(fishWeight<=1200)return fishNames[5];
            if(fishWeight<=2000)return fishNames[6];
            if(fishWeight<=5000)return fishNames[7];
            return fishNames[8];
        }

        int level(int i){return i==0?80:i==1?100:44;}

        void sellFish(){
            if(bag.isEmpty())message="Kho cá đang trống.";
            else{int total=0;for(Fish f:bag)total+=f.weight*3;money+=total;bag.clear();message="Đã bán toàn bộ cá +"+total+"$";}
            invalidate();
        }

        void center(Canvas c,String s,float x,float y){c.drawText(s,x-p.measureText(s)/2f,y,p);}

        static class Fish{
            String name; int weight;
            Fish(String n,int w){name=n;weight=w;}
        }
    }
}
