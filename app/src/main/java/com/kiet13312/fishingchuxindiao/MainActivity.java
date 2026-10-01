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
        boolean joystickActive=false, joystickTouch=false;
        float joystickKnobX=0,joystickKnobY=0;
        final float JOY_R=58f;
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

        int mapRequiredLevel(int i){
            int[] req={1,20,40,60,80,100};
            return req[Math.max(0,Math.min(req.length-1,i))];
        }

        boolean isMapUnlocked(int i){
            return levels[selected]>=mapRequiredLevel(i);
        }

        void resetJoystick(){
            joystickActive=false;
            joystickTouch=false;
            float jx=95, jy=getHeight()-170;
            joystickKnobX=jx;
            joystickKnobY=jy;
        }

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
                button(c,r-88,t+88,r-15,t+132,"VÀO",isMapUnlocked(i));
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
            long now=System.currentTimeMillis();
            float pulse=(float)((Math.sin(now/90.0)+1)/2.0);
            float pull=(fishHooked?0.035f:0.012f)+(tension/100f)*0.025f;

            // Bright outdoor lake scene, matching the composition/style of the supplied reference
            // without copying its original frames or artwork.
            LinearGradient sky=new LinearGradient(0,0,0,h*.34f,
                    Color.rgb(78,177,239),Color.rgb(190,232,255),Shader.TileMode.CLAMP);
            p.setShader(sky); c.drawRect(0,0,w,h*.34f,p); p.setShader(null);

            // distant hills
            p.setColor(Color.rgb(74,160,87));
            Path hill=new Path();
            hill.moveTo(0,h*.30f); hill.cubicTo(w*.12f,h*.18f,w*.23f,h*.27f,w*.36f,h*.20f);
            hill.cubicTo(w*.49f,h*.13f,w*.63f,h*.27f,w*.78f,h*.17f);
            hill.cubicTo(w*.87f,h*.13f,w*.95f,h*.21f,w,h*.16f);
            hill.lineTo(w,h*.39f); hill.lineTo(0,h*.39f); hill.close(); c.drawPath(hill,p);

            // water
            LinearGradient water=new LinearGradient(0,h*.31f,0,h*.88f,
                    Color.rgb(36,170,210),Color.rgb(29,105,170),Shader.TileMode.CLAMP);
            p.setShader(water); c.drawRect(0,h*.31f,w,h*.88f,p); p.setShader(null);

            p.setColor(Color.argb(105,255,255,255));
            for(int i=0;i<17;i++){
                float yy=h*.36f+i*h*.026f;
                float sway=(float)Math.sin(now/700.0+i)*16;
                c.drawLine(w*.02f+sway,yy,w*.28f+sway+35,yy+5,p);
                c.drawLine(w*.46f-sway,yy+9,w*.82f-sway,yy+3,p);
                c.drawLine(w*.77f+sway,yy+17,w*.98f+sway,yy+11,p);
            }

            // grassy bank / rocks
            p.setColor(Color.rgb(93,181,67)); c.drawRect(0,h*.78f,w,h,p);
            p.setColor(Color.rgb(145,199,92));
            for(int i=0;i<32;i++) c.drawCircle((i*73)%Math.max(1,w),h*.77f+(i%4)*8,8+(i%4)*3,p);
            p.setColor(Color.rgb(123,105,80));
            for(int i=0;i<20;i++) c.drawOval((i*97)%Math.max(1,w),h*.80f+(i%3)*18,(i*97)%Math.max(1,w)+26,h*.80f+(i%3)*18+11,p);

            // Small top labels; keep the gameplay scene dominant.
            panel(c,18,16,242,69,Color.argb(150,8,20,29));
            txt(c,"CÂU CÁ VẠN CÂN",31,41,17,Color.WHITE);
            txt(c,"Màn "+(map+1)+"   •   "+fishNames[fishIdx],31,59,11,Color.YELLOW);

            panel(c,w-272,16,w-18,74,Color.argb(155,8,20,29));
            txt(c,"CÁ "+fishNames[fishIdx],w-250,38,14,Color.WHITE);
            p.setColor(Color.argb(120,0,0,0)); c.drawRoundRect(w-250,48,w-42,63,8,8,p);
            float hp=fishMax==0?1:(float)Math.max(0,fishHp)/fishMax;
            p.setColor(Color.rgb(242,68,68)); c.drawRoundRect(w-250,48,w-250+208*hp,63,8,8,p);
            txt(c,Math.max(0,fishHp)+" / "+fishMax,w-180,59,9,Color.WHITE);

            // Three characters physically fish together.
            float base=h*.765f;
            for(int i=0;i<3;i++){
                float px=w*playerX[i];
                float lean=(float)Math.sin(now/160.0+i*.8)*2.2f + (fishHooked?(-4-i*2):0);
                drawHero(c,px,base,i,1.20f,lean,pull);
                center(c,names[i],px,base+45,11,Color.WHITE);

                float rodTipX=w*(.72f+i*.055f);
                float rodTipY=h*(.40f+i*.035f)+(float)Math.sin(now/180.0+i)*8;
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeWidth(4.3f-i*.25f);
                p.setColor(Color.rgb(63,43,30));
                c.drawLine(px+25,base-72,rodTipX,rodTipY,p);
                p.setStrokeWidth(1.7f);
                p.setColor(Color.WHITE);
                Path line=new Path();
                line.moveTo(rodTipX,rodTipY);
                line.quadTo((rodTipX+w*.80f)/2,rodTipY+h*.05f,(w*.81f),h*.56f+i*5);
                line.quadTo(w*.84f+(float)Math.sin(now/240.0+i)*18,h*.54f,
                        w*.86f,h*.57f);
                c.drawPath(line,p);
                p.setStyle(Paint.Style.FILL);
            }

            // Large animated fish/monster under the lines.
            float fx=w*(.825f+(float)Math.sin(now/330.0)*.025f);
            float fy=h*(.575f+(float)Math.sin(now/210.0)*.045f);
            if(fishHooked) fx-=w*.018f;
            drawActionFish(c,fx,fy,1.0f+pulse*.05f,fishHooked);

            // Water splash where the fish fights.
            if(fishHooked){
                p.setColor(Color.argb(145,255,255,255));
                for(int i=0;i<12;i++){
                    double a=i*Math.PI/6.0 + now/5000.0;
                    float rr=28+((i%3)*14)+(float)(pulse*12);
                    c.drawCircle(fx+(float)Math.cos(a)*rr,fy+(float)Math.sin(a)*rr*.45f,3+(i%3),p);
                }
                center(c,"KÉO!!!",w*.80f,h*.34f,28,Color.WHITE);
            }

            // Bottom control strip.
            panel(c,18,h-92,w-358,h-14,Color.argb(150,7,18,26));
            for(int i=0;i<3;i++) drawSkillButton(c,34+i*112,h-79,134+i*112,h-25,i);

            // Virtual joystick remains available, but visually blends into the scene.
            float jx=94,jy=h-151;
            p.setColor(Color.argb(95,0,0,0)); c.drawCircle(jx,jy,62,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(Color.argb(220,255,255,255)); c.drawCircle(jx,jy,55,p);
            p.setStyle(Paint.Style.FILL);
            float kx=joystickActive?joystickKnobX:jx, ky=joystickActive?joystickKnobY:jy;
            p.setColor(Color.argb(220,255,214,62)); c.drawCircle(kx,ky,25,p);

            // Main pull button.
            float bx=w-93,by=h-61;
            p.setColor(Color.argb(105,0,0,0)); c.drawCircle(bx+3,by+5,62,p);
            p.setColor(Color.rgb(18,80,115)); c.drawCircle(bx,by,58,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(Color.WHITE); c.drawCircle(bx,by,52,p);
            p.setStyle(Paint.Style.FILL);
            center(c,fishHooked?"KÉO":"THẢ CÂU",bx,by+5,16,Color.WHITE);

            // Tension meter, deliberately high-contrast like an action HUD.
            float barL=w*.57f,barR=w*.75f,barT=16,barB=29;
            p.setColor(Color.argb(120,0,0,0)); c.drawRoundRect(barL,barT,barR,barB,7,7,p);
            p.setColor(tension>78?Color.RED:Color.rgb(249,207,58));
            c.drawRoundRect(barL,barT,barL+(barR-barL)*(tension/100f),barB,7,7,p);
            center(c,"LỰC DÂY "+((int)tension)+"%",(barL+barR)/2,barB+17,11,Color.WHITE);

            if(skillFX && now<skillFxUntil) drawSkillFX(c,w,h);
            else skillFX=false;

            // Fish animation / tension simulation.
            fishX=fx/w; fishY=fy/h;
            fishX+=fishV*.70f;
            if(fishX<.70f||fishX>.90f)fishV=-fishV;
            if(fishHooked){
                tension+=.08f;
                if(tension>100)tension=100;
            }else{
                tension=Math.max(12,tension-.025f);
            }
        }

        void drawHero(Canvas c,float x,float y,int who,float sc,float lean,float pull){
            // Distinct cartoon silhouettes for the three fishers.
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(80,0,0,0)); c.drawOval(x-35*sc,y+45*sc,x+38*sc,y+60*sc,p);

            // legs
            p.setColor(Color.rgb(57,50,52));
            c.drawRoundRect(x-15*sc,y+3*sc,x-3*sc,y+43*sc,5*sc,5*sc,p);
            c.drawRoundRect(x+3*sc,y+3*sc,x+15*sc,y+43*sc,5*sc,5*sc,p);

            // torso with a leaning pose
            p.setColor(clothes[who]);
            c.drawRoundRect(x-24*sc+lean,y-47*sc,x+25*sc+lean,y+8*sc,12*sc,12*sc,p);

            // head / hair
            p.setColor(Color.rgb(248,211,165)); c.drawCircle(x+lean,y-70*sc,18*sc,p);
            p.setColor(who==0?Color.rgb(35,32,38):who==1?Color.rgb(20,20,26):Color.rgb(146,137,128));
            c.drawOval(x-20*sc+lean,y-87*sc,x+20*sc+lean,y-67*sc,p);
            if(who==2) c.drawOval(x-19*sc+lean,y-88*sc,x+18*sc+lean,y-54*sc,p);

            // face
            p.setColor(Color.rgb(40,30,30)); c.drawCircle(x-6*sc+lean,y-70*sc,2*sc,p); c.drawCircle(x+6*sc+lean,y-70*sc,2*sc,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2*sc); p.setColor(Color.rgb(80,45,40));
            c.drawLine(x-4*sc+lean,y-62*sc,x+6*sc+lean,y-62*sc,p);

            // pulling arms
            p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeWidth(8*sc); p.setColor(Color.rgb(248,211,165));
            float armY=y-25*sc-pull*380;
            c.drawLine(x-15*sc+lean,y-18*sc,x+7*sc+lean,armY,p);
            c.drawLine(x+15*sc+lean,y-18*sc,x+12*sc+lean,armY+5*sc,p);
            p.setStyle(Paint.Style.FILL);
            c.drawCircle(x+8*sc+lean,armY,6*sc,p); c.drawCircle(x+13*sc+lean,armY+4*sc,6*sc,p);
        }

        void drawActionFish(Canvas c,float x,float y,float sc,boolean hooked){
            p.setColor(hooked?Color.rgb(91,66,160):Color.rgb(53,102,163));
            c.drawOval(x-86*sc,y-38*sc,x+76*sc,y+38*sc,p);

            // head / eye
            p.setColor(Color.WHITE); c.drawCircle(x-50*sc,y-8*sc,11*sc,p);
            p.setColor(Color.BLACK); c.drawCircle(x-50*sc,y-8*sc,4*sc,p);

            // tail
            Path tail=new Path();
            tail.moveTo(x+62*sc,y); tail.lineTo(x+125*sc,y-52*sc); tail.lineTo(x+111*sc,y);
            tail.lineTo(x+125*sc,y+52*sc); tail.close(); c.drawPath(tail,p);

            // fins
            p.setColor(Color.rgb(166,83,116));
            Path fin=new Path(); fin.moveTo(x-5*sc,y-25*sc); fin.lineTo(x+30*sc,y-62*sc); fin.lineTo(x+38*sc,y-18*sc); fin.close(); c.drawPath(fin,p);
            Path fin2=new Path(); fin2.moveTo(x+5*sc,y+23*sc); fin2.lineTo(x+35*sc,y+56*sc); fin2.lineTo(x+42*sc,y+18*sc); fin2.close(); c.drawPath(fin2,p);

            if(hooked){
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(Color.WHITE);
                c.drawArc(x-40*sc,y-12*sc,x+40*sc,y+20*sc,15,125,false,p);
                p.setStyle(Paint.Style.FILL);
            }
        }

        void drawSkillButton(Canvas c,float l,float t,float r,float b,int who){
            p.setColor(Color.argb(180,10,25,34)); c.drawRoundRect(l,t,r,b,14,14,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(clothes[who]); c.drawRoundRect(l,t,r,b,14,14,p);
            center(c,who==0?"XE KÉO":who==1?"PHI THIÊN":"HỘ LỰC",(l+r)/2,t+20,10,Color.WHITE);
            center(c,"Lv "+skillLevels[who],(l+r)/2,t+39,10,Color.YELLOW);
        }

        void drawSkillFX(Canvas c,int w,int h){
            long now=System.currentTimeMillis();
            float remain=Math.max(0,skillFxUntil-now);
            float alpha=Math.min(1f,remain/700f);
            float cx=w*.70f, cy=h*.49f;

            if(selected==0){
                // cart-pull effect: multiple bright speed lines converging on the fish
                p.setColor(Color.argb((int)(210*alpha),246,205,61));
                for(int i=0;i<7;i++){
                    float yy=cy-105+i*35;
                    c.drawRoundRect(w*.08f,yy,w*.63f,yy+7,5,5,p);
                }
                p.setColor(Color.rgb(69,145,70));
                c.drawRoundRect(w*.13f,h*.51f,w*.38f,h*.62f,18,18,p);
                p.setColor(Color.rgb(61,61,61)); c.drawCircle(w*.18f,h*.64f,22,p); c.drawCircle(w*.34f,h*.64f,22,p);
                center(c,"XE KÉO!",w*.55f,h*.28f,32,Color.WHITE);
            }else if(selected==1){
                // explosive sky/energy effect
                p.setColor(Color.argb((int)(145*alpha),255,230,85)); c.drawCircle(cx,cy,135,p);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(12); p.setColor(Color.rgb(255,247,159));
                for(int i=0;i<6;i++) c.drawCircle(cx,cy,45+i*24,p);
                p.setStrokeWidth(6); p.setColor(Color.WHITE);
                for(int i=0;i<12;i++){
                    double a=i*Math.PI/6.0;
                    c.drawLine(cx+(float)Math.cos(a)*52,cy+(float)Math.sin(a)*52,
                               cx+(float)Math.cos(a)*180,cy+(float)Math.sin(a)*180,p);
                }
                p.setStyle(Paint.Style.FILL);
                center(c,"PHI THIÊN VÔ CỰC!",w*.57f,h*.20f,27,Color.WHITE);
            }else{
                // protective force field
                p.setColor(Color.argb((int)(120*alpha),80,200,255)); c.drawCircle(cx,cy,145,p);
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(10); p.setColor(Color.rgb(170,240,255));
                c.drawCircle(cx,cy,90,p); c.drawCircle(cx,cy,120,p); c.drawCircle(cx,cy,148,p);
                p.setStyle(Paint.Style.FILL);
                center(c,"HỘ LỰC!",w*.58f,h*.21f,30,Color.WHITE);
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
            float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight();
            if(screen==FISHING){
                float jx=95, jy=h-170;
                int action=e.getActionMasked();

                if(action==MotionEvent.ACTION_CANCEL){
                    resetJoystick();
                    invalidate();
                    return true;
                }

                if(action==MotionEvent.ACTION_UP && joystickTouch){
                    resetJoystick();
                    invalidate();
                    return true;
                }

                if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_MOVE){
                    float dx=x-jx, dy=y-jy;
                    if(joystickTouch || (dx*dx+dy*dy)<=((JOY_R+28)*(JOY_R+28))){
                        joystickTouch=true;
                        joystickActive=true;
                        float len=(float)Math.hypot(dx,dy);
                        if(len>JOY_R){dx=dx*JOY_R/len;dy=dy*JOY_R/len;}
                        joystickKnobX=jx+dx; joystickKnobY=jy+dy;
                        float dir=dx/JOY_R;
                        if(Math.abs(dir)>.08f){
                            playerX[selected]=Math.max(.12f,Math.min(.62f,playerX[selected]+dir*.0065f));
                            toast=names[selected]+" di chuyển "+(dir<0?"sang trái":"sang phải");
                        }
                        invalidate();
                        return true;
                    }
                }
            }
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;

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
                    if(x>=l&&x<=r&&y>=t&&y<=b){
                        if(isMapUnlocked(i)){map=i;toast="Màn "+(i+1)+" đã chọn";}
                        else toast="Cần Lv "+mapRequiredLevel(i)+" để mở màn "+(i+1);
                        invalidate();return true;
                    }
                }
                if(y>h-70){screen=LOBBY;invalidate();return true;}
            }else if(screen==CHAR){
                for(int i=0;i<3;i++){
                    float l=45+i*w*.31f,r=l+w*.25f;
                    if(x>=l&&x<=r&&y>=120&&y<=h-105){
                        if(y>=h-215&&y<h-175){
                            if(money>=charUpgradeCost[i]){money-=charUpgradeCost[i];levels[i]++;charUpgradeCost[i]+=400;toast=names[i]+" lên Lv."+levels[i];}
                            else toast="Không đủ tiền nâng "+names[i];
                        }else {selected=i;if(map>0 && !isMapUnlocked(map)) map=0;toast="Đã chọn "+names[i];}
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
            resetJoystick();
            toast="Cả Sở Tâm, Bá Thường, Lão Ngô cùng thả câu!";
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
