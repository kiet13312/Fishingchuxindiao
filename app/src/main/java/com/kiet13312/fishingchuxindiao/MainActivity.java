package com.kiet13312.fishingchuxindiao;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.Random;

public class MainActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        setContentView(new FishingGame(this));
    }

    static class FishingGame extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rnd = new Random();

        static final int LOBBY=0, MAP=1, CHAR=2, SHOP=3, SKILL_MENU=4, FISHING=5, RESULT=6;

        final String[] names={"Sở Tâm","Bá Thường","Lão Ngô"};
        final String[] skillNames={"Xe Kéo","Phi Thiên Vô Cực","Hộ Lực"};
        final String[] skillDesc={
                "Gọi xe kéo, kéo mạnh cả 3 dây câu.",
                "Phi thiên, bộc phát lực kéo cực lớn.",
                "Hộ lực, hạ căng dây và tăng sức kéo."
        };
        final int[] levels={80,100,44};
        final int[] skillLevels={1,1,1};
        final int[] skillCosts={500,800,600};
        final int[] skillPower={1,1,1};
        final int[] charUpgradeCost={700,900,650};
        float[] playerX={.27f,.39f,.51f};
        float playerY=.77f;
        int movingWho=-1;
        float touchStartX=0,touchStartY=0;
        final int[] clothes={Color.rgb(76,155,91),Color.rgb(145,75,178),Color.rgb(61,112,167)};

        final String[] rodNames={"Cần Tre","Cần Sắt","Cần Thép","Cần Vàng","Cần Thần"};
        final int[] rodCost={0,2000,8000,30000,100000};
        final int[] rodPower={120,350,700,1400,3000};

        final String[] fishNames={"Cá rô","Cá chép","Cá trắm","Cá lóc","Cá mè","Cá kiếm","Cá thần","Cá vạn cân","Cá Tề Thiên"};
        final int[] fishWeights={50,100,200,400,800,1200,2000,5000,10000};

        int screen=LOBBY, selected=0, map=0, money=1000, rod=0;
        int fishIdx=4, fishHp=0, fishMax=0;
        float tension=28f, fishX=.77f, fishY=.51f, fishV=-.004f;
        boolean fishHooked=false, skillFX=false;
        long skillFxUntil=0;
        String toast="Chọn màn để bắt đầu";

        FishingGame(Context c){ super(c); p.setTypeface(Typeface.create("sans",Typeface.BOLD)); setFocusable(true); }

        @Override protected void onDraw(Canvas c){
            int w=getWidth(),h=getHeight();
            if(screen==LOBBY) drawLobby(c,w,h);
            else if(screen==MAP) drawMap(c,w,h);
            else if(screen==CHAR) drawChar(c,w,h);
            else if(screen==SHOP) drawShop(c,w,h);
            else if(screen==SKILL_MENU) drawSkillMenu(c,w,h);
            else if(screen==FISHING) drawFishing(c,w,h);
            else drawResult(c,w,h);
            postInvalidateDelayed(33);
        }

        void bgLake(Canvas c,int w,int h){
            LinearGradient sky=new LinearGradient(0,0,0,h*.46f,
                    Color.rgb(54,99,81),Color.rgb(183,207,177),Shader.TileMode.CLAMP);
            p.setShader(sky); c.drawRect(0,0,w,h*.46f,p); p.setShader(null);
            p.setColor(Color.rgb(42,92,62));
            for(int i=0;i<22;i++) c.drawOval(i*w/21f-70,h*.18f+(i%5)*14-30,i*w/21f+70,h*.18f+(i%5)*14+38,p);
            p.setColor(Color.rgb(80,142,97));
            for(int i=0;i<16;i++) c.drawCircle(i*w/15f,h*.33f-(i%3)*12,45+(i%4)*9,p);
            LinearGradient water=new LinearGradient(0,h*.37f,0,h*.82f,
                    Color.rgb(92,174,181),Color.rgb(72,129,149),Shader.TileMode.CLAMP);
            p.setShader(water); c.drawRect(0,h*.37f,w,h*.82f,p); p.setShader(null);
            p.setColor(Color.argb(65,255,255,255));
            for(int i=0;i<14;i++){ float y=h*.43f+i*h*.024f; c.drawLine(0,y,w,y+(i%2==0?6:-4),p); }
            p.setColor(Color.rgb(224,210,175)); c.drawRect(0,h*.80f,w,h,p);
            p.setColor(Color.rgb(179,160,128));
            for(int i=0;i<95;i++) c.drawCircle((i*97)%w,h*.82f+(i*17)%Math.max(1,(int)(h*.15f)),1+(i%3),p);
        }

        void panel(Canvas c,float l,float t,float r,float b,int color){
            p.setStyle(Paint.Style.FILL); p.setColor(color); c.drawRoundRect(l,t,r,b,18,18,p);
        }
        void txt(Canvas c,String s,float x,float y,float size,int color){
            p.setStyle(Paint.Style.FILL); p.setColor(color); p.setTextSize(size); c.drawText(s,x,y,p);
        }
        void center(Canvas c,String s,float x,float y,float size,int color){
            p.setTextSize(size); p.setColor(color); p.setStyle(Paint.Style.FILL); c.drawText(s,x-p.measureText(s)/2f,y,p);
        }
        void button(Canvas c,float l,float t,float r,float b,String s,boolean active){
            p.setColor(Color.argb(100,0,0,0)); c.drawRoundRect(l+4,t+5,r+4,b+5,16,16,p);
            p.setColor(active?Color.rgb(245,187,50):Color.rgb(54,83,102)); c.drawRoundRect(l,t,r,b,16,16,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(active?Color.rgb(255,237,147):Color.rgb(135,170,188));
            c.drawRoundRect(l,t,r,b,16,16,p); p.setStyle(Paint.Style.FILL);
            center(c,s,(l+r)/2,(t+b)/2+6,16,Color.WHITE);
        }
        void topBar(Canvas c,int w,String title){
            p.setColor(Color.argb(220,12,15,18)); c.drawRect(0,0,w,64,p);
            txt(c,"☰",22,40,26,Color.WHITE); txt(c,title,64,38,22,Color.WHITE);
            txt(c,"Tiền "+money+"$",w-205,37,17,Color.YELLOW);
            txt(c,"VIP 12,54Triệu+",w-106,37,13,Color.YELLOW);
        }

        void drawLobby(Canvas c,int w,int h){
            bgLake(c,w,h); p.setColor(Color.argb(120,0,0,0)); c.drawRect(0,0,w,h,p);
            center(c,"CÂU CÁ VẠN CÂN",w*.50f,h*.18f,46,Color.WHITE);
            center(c,"SĂN CÁ • NÂNG CẦN • MỞ KỸ NĂNG",w*.50f,h*.235f,16,Color.rgb(225,235,220));
            panel(c,w*.27f,h*.31f,w*.73f,h*.53f,Color.argb(225,9,14,18));
            center(c,"SẢNH",w*.50f,h*.375f,28,Color.YELLOW);
            center(c,"Ba câu thủ luôn ra bờ cùng nhau.",w*.50f,h*.425f,18,Color.WHITE);
            center(c,"Chọn màn, nhân vật, cần câu và kỹ năng.",w*.50f,h*.462f,14,Color.LTGRAY);
            button(c,w*.37f,h*.545f,w*.63f,h*.63f,"BẮT ĐẦU",true);
            float gap=18,bw=(w-2*gap-48)/4f,y=h*.70f;
            button(c,gap,y,gap+bw,y+58,"CHỌN MÀN",false);
            button(c,gap+bw+12,y,gap+2*bw+12,y+58,"NHÂN VẬT",false);
            button(c,gap+2*bw+24,y,gap+3*bw+24,y+58,"SHOP CẦN",false);
            button(c,gap+3*bw+36,y,gap+4*bw+36,y+58,"KỸ NĂNG",false);
            panel(c,22,h-63,410,h-20,Color.argb(175,15,18,22));
            txt(c,"💰 "+money+"$    •    "+rodNames[rod]+"    •    Lv "+levels[selected],38,h-36,14,Color.WHITE);
        }

        void drawMap(Canvas c,int w,int h){
            p.setColor(Color.rgb(7,10,14)); c.drawRect(0,0,w,h,p); topBar(c,w,"CHỌN MÀN");
            txt(c,"Chọn địa điểm câu cá",34,95,17,Color.LTGRAY);
            String[] mapNames={"Bến Sông","Hồ Ngọc Bích","Bãi Đá","Rừng Mây","Biển Đêm","Đảo Vạn Cân"};
            String[] req={"Lv 1+","Lv 20+","Lv 40+","Lv 60+","Lv 80+","Lv 100+"};
            for(int i=0;i<6;i++){
                int col=i%3,row=i/3; float l=35+col*w*.31f,t=115+row*175,r=l+w*.27f,b=t+145;
                p.setColor(Color.rgb(30,43,51)); c.drawRoundRect(l,t,r,b,14,14,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(i==map?Color.YELLOW:Color.rgb(75,108,120));c.drawRoundRect(l,t,r,b,14,14,p);p.setStyle(Paint.Style.FILL);
                p.setColor(i<4?Color.rgb(57,111,73):Color.rgb(25,49,68)); c.drawRoundRect(l+10,t+10,r-10,t+70,9,9,p);
                p.setColor(Color.rgb(67,148,169));c.drawRect(l+10,t+43,r-10,t+70,p);
                for(int k=0;k<4;k++){p.setColor(Color.rgb(87,153,91));c.drawCircle(l+25+k*48,t+30,22+(k%2)*5,p);}
                txt(c,mapNames[i],l+15,t+98,16,Color.WHITE);txt(c,req[i],l+15,t+119,12,Color.YELLOW);
                button(c,r-88,t+88,r-15,t+132,i<=map);
            }
            button(c,25,h-62,170,h-18,"VỀ SẢNH",false);
        }

        void drawChar(Canvas c,int w,int h){
            p.setColor(Color.rgb(7,10,14)); c.drawRect(0,0,w,h,p); topBar(c,w,"CHỌN NHÂN VẬT");
            txt(c,"Cả 3 người sẽ cùng xuất hiện khi vào màn.",34,94,16,Color.LTGRAY);
            for(int i=0;i<3;i++){
                float l=45+i*w*.31f,r=l+w*.25f,cx=(l+r)/2,cy=250;
                p.setColor(i==selected?Color.rgb(74,58,31):Color.rgb(26,32,38));c.drawRoundRect(l,120,r,h-105,20,20,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(i==selected?4:2);p.setColor(i==selected?Color.YELLOW:Color.rgb(75,95,105));c.drawRoundRect(l,120,r,h-105,20,20,p);p.setStyle(Paint.Style.FILL);
                drawPerson(c,cx,cy,i,1.65f);
                center(c,names[i],cx,360,22,Color.WHITE);center(c,"Lv "+levels[i],cx,386,14,Color.YELLOW);
                center(c,"Lực +"+((i==0?70:i==1?95:80)+skillLevels[i]*15),cx,412,13,Color.LTGRAY);
                center(c,skillNames[i]+" Lv."+skillLevels[i],cx,441,15,Color.rgb(239,205,112));
                button(c,l+25,h-215,r-25,h-175,"NÂNG "+charUpgradeCost[i]+"$",true);
                button(c,l+25,h-165,r-25,h-115,i==selected?"ĐANG CHỌN":"CHỌN",i==selected);
                if(i==selected) center(c,"✓",r-35,151,25,Color.YELLOW);
            }
            button(c,25,h-62,170,h-18,"VỀ SẢNH",false);button(c,w-205,h-62,w-25,h-18,"XÁC NHẬN",true);
        }

        void drawShop(Canvas c,int w,int h){
            p.setColor(Color.rgb(8,11,14));c.drawRect(0,0,w,h,p);topBar(c,w,"SHOP CẦN CÂU");
            txt(c,"Tiền hiện có: "+money+"$",34,94,16,Color.WHITE);
            for(int i=0;i<5;i++){
                float l=30+(i%3)*w*.31f,t=120+(i/3)*190,r=l+w*.27f,b=t+155;
                p.setColor(Color.rgb(27,35,42));c.drawRoundRect(l,t,r,b,16,16,p);
                p.setColor(Color.rgb(115,80,41));p.setStrokeWidth(7);c.drawLine(l+30,t+112,r-35,t+38,p);
                p.setColor(Color.rgb(205,228,232));p.setStrokeWidth(2);c.drawLine(l+30,t+112,r-35,t+38,p);
                center(c,rodNames[i],(l+r)/2,t+30,17,Color.WHITE);center(c,rodPower[i]+" kg",(l+r)/2,t+55,14,Color.YELLOW);
                center(c,i<=rod?(i==rod?"ĐANG DÙNG":"ĐÃ MUA"):"Giá "+rodCost[i]+"$",(l+r)/2,t+88,13,i<=rod?Color.rgb(120,230,160):Color.WHITE);
                button(c,l+18,t+103,r-18,t+142,i<=rod?"DÙNG":"MUA",i<=rod);
            }
            button(c,25,h-62,170,h-18,"VỀ SẢNH",false);
        }

        void drawSkillMenu(Canvas c,int w,int h){
            p.setColor(Color.rgb(8,10,14));c.drawRect(0,0,w,h,p);topBar(c,w,"MENU KỸ NĂNG");
            txt(c,"Nâng cấp kỹ năng bằng tiền • mỗi cấp tăng sức mạnh.",34,92,15,Color.LTGRAY);
            for(int i=0;i<3;i++){
                float l=35+i*w*.31f,r=l+w*.28f;
                p.setColor(Color.rgb(27,32,39));c.drawRoundRect(l,115,r,h-105,17,17,p);
                p.setColor(clothes[i]);c.drawCircle((l+r)/2,182,34,p);
                center(c,names[i],(l+r)/2,245,20,Color.WHITE);
                center(c,skillNames[i]+"  Lv."+skillLevels[i],(l+r)/2,280,17,Color.YELLOW);
                lineWrap(c,skillDesc[i],l+20,318,r-20,14,Color.LTGRAY);
                if(i==0){txt(c,"🚜  Kéo đoàn dây",l+20,388,14,Color.WHITE);txt(c,"Kéo cả 3 cần cùng lúc.",l+20,414,13,Color.LTGRAY);}
                if(i==1){txt(c,"⚡  Phi thiên bộc phát",l+20,388,14,Color.WHITE);txt(c,"Dồn lực đánh cá.",l+20,414,13,Color.LTGRAY);}
                if(i==2){txt(c,"🛡  Giảm căng dây",l+20,388,14,Color.WHITE);txt(c,"Hạ áp lực cá quẫy.",l+20,414,13,Color.LTGRAY);}
                button(c,l+20,h-175,r-20,h-125,"NÂNG CẤP "+skillCosts[i]+"$",true);
            }
            button(c,25,h-62,170,h-18,"VỀ SẢNH",false);
        }

        void lineWrap(Canvas c,String s,float l,float y,float rr,float size,int color){
            p.setTextSize(size);p.setColor(color);String[] words=s.split(" ");String cur="";
            for(String word:words){
                String n=cur.length()==0?word:cur+" "+word;
                if(p.measureText(n)>rr-l&&cur.length()>0){c.drawText(cur,l,y,p);y+=20;cur=word;}else cur=n;
            }
            if(cur.length()>0)c.drawText(cur,l,y,p);
        }

        void drawPerson(Canvas c,float x,float y,int who,float sc){
            p.setColor(Color.rgb(48,38,33));c.drawCircle(x,y-72*sc,19*sc,p);
            p.setColor(Color.rgb(244,211,172));c.drawCircle(x,y-65*sc,17*sc,p);
            p.setColor(clothes[who]);c.drawRoundRect(x-24*sc,y-45*sc,x+24*sc,y+12*sc,10*sc,10*sc,p);
            p.setColor(Color.rgb(48,49,53));c.drawRect(x-14*sc,y+12*sc,x-3*sc,y+55*sc,p);c.drawRect(x+3*sc,y+12*sc,x+14*sc,y+55*sc,p);
            p.setColor(Color.rgb(101,63,35));p.setStrokeWidth(5*sc);c.drawLine(x+14*sc,y-13*sc,x+80*sc,y-56*sc,p);
            if(who==2){p.setColor(Color.rgb(190,190,190));c.drawCircle(x,y-82*sc,18*sc,p);}
        }

        void drawFishing(Canvas c,int w,int h){
            bgLake(c,w,h);
            panel(c,18,18,315,88,Color.argb(200,16,20,23));
            txt(c,"Lv 80   con ng",32,40,15,Color.WHITE);txt(c,"3 NGƯỜI CÙNG CÂU",32,60,12,Color.WHITE);
            txt(c,"Cá đang câu: "+fishNames[fishIdx],32,78,11,Color.YELLOW);

            panel(c,w*.36f,18,w*.70f,86,Color.argb(210,13,18,22));
            txt(c,"Cá "+fishNames[fishIdx]+"   Lv 100",w*.39f,43,16,Color.WHITE);
            p.setColor(Color.DKGRAY);c.drawRoundRect(w*.39f,54,w*.67f,69,8,8,p);
            p.setColor(Color.RED);float hp=fishMax==0?1:(float)Math.max(0,fishHp)/fishMax;c.drawRoundRect(w*.39f,54,w*.39f+w*.28f*hp,69,8,8,p);
            txt(c,Math.max(0,fishHp)+"/"+fishMax,w*.505f,66,10,Color.WHITE);

            panel(c,w-240,18,w-20,88,Color.argb(200,13,18,22));
            txt(c,"💰 "+money+"$",w-220,45,16,Color.YELLOW);txt(c,rodNames[rod]+" • "+rodPower[rod]+" kg",w-220,68,13,Color.WHITE);

            float base=h*playerY;
            for(int i=0;i<3;i++){
                float px=w*playerX[i];
                drawPerson(c,px,base,i,1.35f);center(c,names[i],px,base+55,11,Color.WHITE);
                p.setStyle(Paint.Style.STROKE);p.setColor(Color.WHITE);p.setStrokeWidth(2.4f+i);
                float sx=px+105,sy=base-75,ex=w*(.67f+i*.045f),ey=h*(.57f+i*.016f);
                Path q=new Path();q.moveTo(sx,sy);q.quadTo((sx+ex)/2,sy-42,ex,ey);q.quadTo(ex+33,ey-8,ex+62,ey);c.drawPath(q,p);p.setStyle(Paint.Style.FILL);
            }

            float fx=w*fishX,fy=h*fishY;
            p.setColor(Color.rgb(90,57,100));c.drawOval(fx-72,fy-30,fx+72,fy+30,p);
            p.setColor(Color.rgb(145,75,90));Path tail=new Path();tail.moveTo(fx+58,fy);tail.lineTo(fx+112,fy-45);tail.lineTo(fx+100,fy);tail.lineTo(fx+112,fy+45);tail.close();c.drawPath(tail,p);
            p.setColor(Color.WHITE);c.drawCircle(fx-42,fy-6,8,p);p.setColor(Color.BLACK);c.drawCircle(fx-42,fy-6,3,p);

            panel(c,18,h-96,w-330,h-16,Color.argb(215,15,18,22));
            for(int i=0;i<3;i++){
                float l=28+i*120;p.setColor(clothes[i]);c.drawRoundRect(l,h-84,l+110,h-26,12,12,p);
                txt(c,skillNames[i],l+8,h-61,10,Color.WHITE);txt(c,"KÍCH HOẠT",l+8,h-39,9,Color.YELLOW);
            }

            float cx=w-95,cy=h-65;
            p.setColor(Color.argb(90,0,0,0));c.drawCircle(cx+4,cy+5,64,p);p.setColor(Color.rgb(23,27,31));c.drawCircle(cx,cy,62,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setColor(Color.WHITE);c.drawCircle(cx,cy,56,p);p.setStyle(Paint.Style.FILL);
            center(c,fishHooked?"CO LẠI DÂY":"THẢ LƯỠI",cx,cy+5,15,Color.WHITE);

            p.setColor(Color.rgb(44,24,23));c.drawRect(w-145,h*.47f,w-125,h*.77f,p);
            p.setColor(tension>75?Color.RED:Color.rgb(242,190,45));c.drawRect(w-145,h*.77f-(h*.30f)*(tension/100f),w-125,h*.77f,p);
            txt(c,"Căng dây",w-155,h*.45f,11,Color.WHITE);

            txt(c,toast,24,h-108,12,Color.WHITE);
            // movement controls
            button(c,20,h-150,100,h-100,"←",false);
            button(c,110,h-150,190,h-100,"→",false);
            if(skillFX && System.currentTimeMillis()<skillFxUntil) drawSkillFX(c,w,h);
            else skillFX=false;

            fishX+=fishV;
            if(fishX<.68f||fishX>.89f)fishV=-fishV;
            fishY=.51f+(float)Math.sin(System.currentTimeMillis()/260.0)*.03f;
            if(fishHooked){
                tension+=.11f;
                if(tension>100)tension=100;
            }
        }

        void drawSkillFX(Canvas c,int w,int h){
            int who=selected;
            float remain=Math.max(0,skillFxUntil-System.currentTimeMillis());
            float alpha=Math.min(1f,remain/900f);
            if(who==0){
                float x=-190+(w*.45f)*(1-alpha);
                p.setColor(Color.rgb(58,131,58));c.drawRoundRect(x,h*.55f,x+185,h*.65f,18,18,p);
                p.setColor(Color.rgb(78,155,65));c.drawRoundRect(x+64,h*.48f,x+124,h*.56f,8,8,p);
                p.setColor(Color.DKGRAY);c.drawCircle(x+35,h*.68f,28,p);c.drawCircle(x+150,h*.68f,19,p);
                p.setColor(Color.CYAN);p.setStrokeWidth(8);c.drawLine(x+10,h*.56f,w*.72f,h*.52f,p);
                center(c,"XE KÉO",w*.51f,h*.35f,30,Color.WHITE);
            }else if(who==1){
                p.setStyle(Paint.Style.FILL);p.setColor(Color.argb(85,70,140,255));c.drawCircle(w*.49f,h*.42f,150,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(10);p.setColor(Color.CYAN);
                for(int i=0;i<5;i++)c.drawCircle(w*.49f,h*.43f,45+i*28,p);
                p.setColor(Color.WHITE);p.setStrokeWidth(5);c.drawLine(w*.45f,h*.55f,w*.52f,h*.15f,p);
                p.setStyle(Paint.Style.FILL);center(c,"PHI THIÊN VÔ CỰC",w*.50f,h*.17f,28,Color.WHITE);
            }else{
                p.setColor(Color.argb(90,80,175,255));c.drawCircle(w*.49f,h*.44f,155,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(12);p.setColor(Color.rgb(120,225,255));c.drawCircle(w*.49f,h*.44f,85,p);c.drawCircle(w*.49f,h*.44f,120,p);
                p.setStyle(Paint.Style.FILL);center(c,"HỘ LỰC",w*.49f,h*.18f,30,Color.WHITE);
            }
            p.setStyle(Paint.Style.FILL);
        }

        void drawResult(Canvas c,int w,int h){
            bgLake(c,w,h);p.setColor(Color.argb(125,0,0,0));c.drawRect(0,0,w,h,p);
            panel(c,w*.28f,h*.22f,w*.72f,h*.70f,Color.argb(235,13,16,20));
            center(c,"VẬY LÀ XONG RỒI!",w*.50f,h*.33f,32,Color.WHITE);
            center(c,"Đã kéo được "+fishNames[fishIdx],w*.50f,h*.41f,19,Color.YELLOW);
            center(c,"+ "+(fishWeights[fishIdx]*3)+"$",w*.50f,h*.47f,20,Color.rgb(120,240,155));
            button(c,w*.37f,h*.55f,w*.63f,h*.63f,"CÂU TIẾP",true);
            button(c,w*.37f,h*.65f,w*.63f,h*.72f,"VỀ SẢNH",false);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight();

            if(screen==LOBBY){
                if(y>h*.53f&&y<h*.66f){startFishing();return true;}
                float gap=18,bw=(w-2*gap-48)/4f,yy=h*.70f;
                if(y>yy-10&&y<yy+75){
                    if(x<gap+bw+5)screen=MAP;
                    else if(x<gap+2*bw+17)screen=CHAR;
                    else if(x<gap+3*bw+29)screen=SHOP;
                    else screen=SKILL_MENU;
                    invalidate();return true;
                }
            }else if(screen==MAP){
                for(int i=0;i<6;i++){
                    int col=i%3,row=i/3;float l=35+col*w*.31f,t=115+row*175,r=l+w*.27f,b=t+145;
                    if(x>=l&&x<=r&&y>=t&&y<=b){map=i;toast="Màn "+(i+1)+" đã chọn";invalidate();return true;}
                }
                if(y>h-70){screen=LOBBY;invalidate();return true;}
            }else if(screen==CHAR){
                for(int i=0;i<3;i++){
                    float l=45+i*w*.31f,r=l+w*.25f;
                    if(x>=l&&x<=r&&y>=120&&y<=h-105){
                        if(y>=h-215&&y<h-175){
                            if(money>=charUpgradeCost[i]){money-=charUpgradeCost[i];levels[i]++;charUpgradeCost[i]+=400;toast=names[i]+" lên Lv."+levels[i];}
                            else toast="Không đủ tiền nâng "+names[i];
                        }else {selected=i;toast="Đã chọn "+names[i];}
                        invalidate();return true;
                    }
                }
                if(y>h-70&&x<190){screen=LOBBY;invalidate();return true;}
                if(y>h-70&&x>w-225){startFishing();return true;}
            }else if(screen==SHOP){
                for(int i=0;i<5;i++){
                    float l=30+(i%3)*w*.31f,t=120+(i/3)*190,r=l+w*.27f,b=t+155;
                    if(x>=l&&x<=r&&y>=t&&y<=b){
                        if(i<=rod){rod=i;toast="Đang dùng "+rodNames[i];}
                        else if(money>=rodCost[i]){money-=rodCost[i];rod=i;toast="Mua "+rodNames[i]+" thành công";}
                        else toast="Không đủ tiền";
                        invalidate();return true;
                    }
                }
                if(y>h-70){screen=LOBBY;invalidate();return true;}
            }else if(screen==SKILL_MENU){
                if(y>h-70){screen=LOBBY;invalidate();return true;}
                for(int i=0;i<3;i++){
                    float l=35+i*w*.31f,r=l+w*.28f;
                    if(x>=l&&x<=r&&y>h-190&&y<h-105){
                        if(money>=skillCosts[i]){money-=skillCosts[i];skillLevels[i]++;skillCosts[i]=skillCosts[i]+skillLevels[i]*400;toast=skillNames[i]+" lên Lv."+skillLevels[i];}
                        else toast="Không đủ tiền nâng kỹ năng";
                        invalidate();return true;
                    }
                }
            }else if(screen==FISHING){
                // Touch-drag also moves the currently selected fisherman.
                if(y>h-170 && x<210){
                    if(x<105) moveGroup(-.025f); else moveGroup(.025f);
                    invalidate(); return true;
                }
                float cx=w-95,cy=h-65;
                if(Math.hypot(x-cx,y-cy)<82){if(!fishHooked){fishHooked=true;toast="Cá cắn! 3 người cùng kéo!";}else pullTogether();invalidate();return true;}
                for(int i=0;i<3;i++){
                    float l=28+i*120;
                    if(x>=l&&x<=l+110&&y>=h-96&&y<h-10){selected=i;useSkill(i);invalidate();return true;}
                }
            }else if(screen==RESULT){
                if(y>h*.53f&&y<h*.66f){startFishing();return true;}
                if(y>h*.64f){screen=LOBBY;invalidate();return true;}
            }
            return true;
        }

        void startFishing(){
            fishIdx=rnd.nextInt(fishNames.length);fishMax=7000+fishWeights[fishIdx]*180;fishHp=fishMax;
            tension=24;fishX=.77f;fishY=.51f;fishV=-.004f;fishHooked=false;skillFX=false;screen=FISHING;
            playerX[0]=.27f;playerX[1]=.39f;playerX[2]=.51f;playerY=.77f;
            toast="Cả Sở Tâm, Bá Thường, Lão Ngô cùng thả câu!";
        }

        void moveGroup(float delta){
            playerX[selected]=Math.max(.12f,Math.min(.62f,playerX[selected]+delta));
            toast=names[selected]+" di chuyển "+(delta<0?"sang trái":"sang phải");
        }

        void pullTogether(){
            int power=rodPower[rod]/4+70+95+80+skillLevels[0]*10+skillLevels[1]*10+skillLevels[2]*10;
            fishHp-=power;tension+=7+rnd.nextInt(6);if(tension>100)tension=100;
            if(fishHp<=0){money+=fishWeights[fishIdx]*3;screen=RESULT;}
            toast="3 người cùng co dây  -"+power+" HP";
        }

        void useSkill(int who){
            selected=who;skillFX=true;skillFxUntil=System.currentTimeMillis()+1200;
            if(who==0){fishHp-=1200+skillLevels[0]*350+tension*8;tension=Math.max(5,tension-28);toast="XE KÉO Lv."+skillLevels[0]+" • Kéo cả 3 dây!";}
            else if(who==1){fishHp-=2200+skillLevels[1]*500+tension*12;tension=Math.min(100,tension+18);toast="PHI THIÊN VÔ CỰC Lv."+skillLevels[1]+" • Bộc phát lực kéo!";}
            else{fishHp-=900+skillLevels[2]*300;tension=Math.max(5,tension-42);toast="HỘ LỰC Lv."+skillLevels[2]+" • Hạ căng dây!";}
            if(fishHp<=0){money+=fishWeights[fishIdx]*3;screen=RESULT;}
        }
    }
}
