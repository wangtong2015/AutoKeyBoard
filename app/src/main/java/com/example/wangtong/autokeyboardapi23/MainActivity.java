package com.example.wangtong.autokeyboardapi23;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {
    ImageView keyboard;// keyboard上面长了190，以此为准
    char KEY_NOT_FOUND=0;
    AutoKeyboard autoKeyboard;
    int[] location;
    float width,height;
    public class AutoKeyboard{
        ImageView keyboard;
        Canvas canvas;
        Bitmap baseBitmap;
        Paint backgroundPaint;
        Paint textPaint;

        float screen_width_ratio = 1F;
        float screen_height_ratio = 1F;
        //Fuzzy Input Test Var
        float keyboardHeight=570;
        float keyboardWidth=1438;
        float deltaY=190;
        float topThreshold=0;// 上界
        float bottomThreshold=955;// 下界
        float minWidth=72;// 最小键宽
        float minHetight=95;//最小键长
        int keyPos[];
        int[] location;
        class KEY{
            char ch;
            float init_x;
            float init_y;
            float curr_x;
            float curr_y;
            float test_x;
            float test_y;
            float init_width;
            float init_height;
            float curr_width;
            float curr_height;
            float test_width;
            float test_height;
            float getDist(float x,float y,int mode){
                // mode==0 init_layout
                // mode==1 current_layout
                if(mode==0){
                    return (init_x-x)*(init_x-x)+(init_y-y)*(init_y-y);
                }
                else{
                    return (curr_x-x)*(curr_x-x)+(curr_y-y)*(curr_y-y);
                }
            }
            KEY(){
                ch='A';
                init_x=0;
                init_y=0;
                init_height=0;
                init_width=0;
                curr_height=0;
                curr_width=0;
                curr_x=0;
                curr_y=0;
                test_height=0;
                test_width=0;
                test_x=0;
                test_y=0;
            }
        }
        KEY keys[];

        void setTopThreshold(float newTopThreshold){
            this.topThreshold=newTopThreshold;
        }
        void setBottomThreshold(float newBottomThreshold){
            this.bottomThreshold=newBottomThreshold;
        }
        void setMinWidth(float newMinWidth){
            this.minWidth=newMinWidth;
        }
        void setMinHetight(float newMinHeight){
            this.minHetight=newMinHeight;
        }
        void getScreenSizeRatio(){
            DisplayMetrics metrics =new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            this.screen_width_ratio = metrics.widthPixels/1440F;
            this.screen_height_ratio = metrics.heightPixels/2560F;
        }

        public char getKeyByPosition(float x, float y, int mode){
            // mode==0 init_layout
            // mode==1 current_layout
            char key = KEY_NOT_FOUND;
            if(y<topThreshold || y>bottomThreshold)
                return key;
            float min_dist = Float.MAX_VALUE;
            for (int i = 0; i < 26; ++i){
                float dist_temp=keys[i].getDist(x,y,mode);
                if (dist_temp<min_dist){
                    key=(char)('a'+i);
                    min_dist=dist_temp;
                }
            }
            return key;
        };
        public  boolean tryLayout(char ch,float x,float y){
            ch=Character.toUpperCase(ch);
            int pos = this.keyPos[ch-'A'];
            float dX=x-this.keys[pos].init_x;
            float dY=y-this.keys[pos].init_y;
            if(dY>=0){// 向下平移
                if(this.keys[19].init_y+this.keys[19].init_height/2+dY>this.bottomThreshold){
                    if(pos>18)
                        return false;
                    else if(pos>9){//第二排向下压缩
                        for (int i=0;i<19;i++){
                            this.keys[i].test_y=this.keys[i].init_y+dY;
                            this.keys[i].test_height=this.keys[i].init_height;
                        }
                        float bottomHeight=this.bottomThreshold-this.keys[15].test_y-this.keys[15].test_height/2;
                        for (int i=19;i<26;i++){
                            this.keys[i].test_y=this.bottomThreshold-bottomHeight/2;
                            this.keys[i].test_height=bottomHeight;
                        }
                    }else{// 第一排向下压缩
                        for (int i=0;i<9;i++){
                            this.keys[i].test_y=this.keys[i].init_y+dY;
                            this.keys[i].test_height=this.keys[i].init_height;
                        }
                        float bottomHeight=this.bottomThreshold-this.keys[0].test_y-this.keys[0].test_height/2;
                        for (int i=10;i<19;i++){
                            this.keys[i].test_y=this.bottomThreshold-bottomHeight*3/4;
                            this.keys[i].test_height=bottomHeight/2;
                        }
                        for (int i=19;i<26;i++){
                            this.keys[i].test_y=this.bottomThreshold-bottomHeight/4;
                            this.keys[i].test_height=bottomHeight/2;
                        }
                    }
                }
                else{
                    for (int i=0;i<26;i++){
                        this.keys[i].test_y=this.keys[i].init_y+dY;
                        this.keys[i].test_height=this.keys[i].init_height;
                    }
                }
            }else{// 向上平移
                if(this.keys[0].init_y-this.keys[0].init_height/2+dY<this.topThreshold){
                    if(pos<10)
                        return false;
                    else if(pos<19){//第二排向上压
                        for (int i=10;i<26;i++){
                            this.keys[i].test_y=this.keys[i].init_y+dY;
                            this.keys[i].test_height=this.keys[i].init_height;
                        }
                        float topHeight=this.keys[15].test_y-this.keys[15].test_height/2;
                        for (int i=0;i<10;i++){
                            this.keys[i].test_y=topHeight/2;
                            this.keys[i].test_height=topHeight;
                        }
                    }else{//第三排向上压
                        for (int i=19;i<26;i++){
                            this.keys[i].test_y=this.keys[i].init_y+dY;
                            this.keys[i].test_height=this.keys[i].init_height;
                        }
                        float topHeight=this.keys[19].test_y-this.keys[19].test_height/2;
                        for (int i=0;i<10;i++){
                            this.keys[i].test_y=topHeight/4;
                            this.keys[i].test_height=topHeight/2;
                        }
                        for (int i=10;i<19;i++){
                            this.keys[i].test_y=topHeight*3/4;
                            this.keys[i].test_height=topHeight/2;
                        }
                    }
                }else{
                    for (int i=0;i<26;i++){
                        this.keys[i].test_y=this.keys[i].init_y+dY;
                        this.keys[i].test_height=this.keys[i].init_height;
                    }
                }
            }
            for (int i=0;i<26;i++){
                if(this.keys[i].test_height<this.minHetight){
                    return false;
                }
            }
            if(dX>=0) {// 向右平移
                if (pos == 9 || pos == 18 || pos == 25)
                    return false;
            }else {// 向左平移
                if (pos == 0 || pos == 10 || pos == 19)
                    return false;
            }

            if(pos<10 ){// 第一排
                this.keys[pos].test_x=x;
                this.keys[pos].test_width=this.keys[pos].init_width;

                float rightRatio=(this.keyboardWidth-this.keys[pos].test_x-this.keys[pos].test_width/2)/(this.keyboardWidth-this.keys[pos].init_x-this.keys[pos].init_width/2);
                float leftRatio=(this.keys[pos].test_x-this.keys[pos].test_width)/(this.keys[pos].init_x-this.keys[pos].init_width);

                for(int i=0;i<pos;i++){
                    this.keys[i].test_x=this.keys[i].init_x*leftRatio;
                    this.keys[i].test_width=this.keys[i].init_width*leftRatio;
                }
                for (int i=pos+1;i<10;i++){
                    this.keys[i].test_x=keyboardWidth-(keyboardWidth-this.keys[i].init_x)*rightRatio;
                    this.keys[i].test_width=this.keys[i].init_width*rightRatio;
                }

                for (int i=10;i<19;i++){
                    this.keys[i].test_x=(this.keys[i-10].test_x+this.keys[i-9].test_x)/2;
                    this.keys[i].test_width=this.keys[i-9].test_x-this.keys[i-10].test_x;
                }
                for (int i=19;i<26;i++){
                    this.keys[i].test_x=this.keys[i-8].test_x;
                    this.keys[i].test_width=this.keys[i-8].test_width;
                }

            }else if(pos<19){// 第二排
                this.keys[pos].test_x=x;
                this.keys[pos].test_width=this.keys[pos].init_width;

                float rightRatio=(this.keyboardWidth-this.keys[pos].test_x-this.keys[pos].test_width/2)/(this.keyboardWidth-this.keys[pos].init_x-this.keys[pos].init_width/2);
                float leftRatio=(this.keys[pos].test_x-this.keys[pos].test_width)/(this.keys[pos].init_x-this.keys[pos].init_width);

                for(int i=10;i<pos;i++){
                    this.keys[i].test_x=this.keys[i].init_x*leftRatio;
                    this.keys[i].test_width=this.keys[i].init_width*leftRatio;
                }
                for (int i=pos+1;i<19;i++){
                    this.keys[i].test_x=this.keyboardWidth-(this.keyboardWidth-this.keys[i].init_x)*rightRatio;
                    this.keys[i].test_width=this.keys[i].init_width*rightRatio;
                }
                this.keys[0].test_x=this.keys[10].test_x/2;
                this.keys[0].test_width=this.keys[10].test_x;
                this.keys[9].test_width=this.keyboardWidth-this.keys[18].test_x;
                this.keys[9].test_x=this.keyboardWidth-this.keys[9].test_width/2;
                for (int i=1;i<9;i++){
                    this.keys[i].test_x=(this.keys[i+9].test_x+this.keys[i+10].test_x)/2;
                    this.keys[i].test_width=this.keys[i+10].test_x-this.keys[i+9].test_x;
                }
                for (int i=19;i<26;i++){
                    this.keys[i].test_x=this.keys[i-8].test_x;
                    this.keys[i].test_width=this.keys[i-8].test_width;
                }
            }else{// 第三排
                this.keys[pos].test_x=x;
                this.keys[pos].test_width=this.keys[pos].init_width;

                float rightRatio=(this.keyboardWidth-this.keys[pos].test_x-this.keys[pos].test_width/2)/(this.keyboardWidth-this.keys[pos].init_x-this.keys[pos].init_width/2);
                float leftRatio=(this.keys[pos].test_x-this.keys[pos].test_width)/(this.keys[pos].init_x-this.keys[pos].init_width);

                for(int i=19;i<pos;i++){
                    this.keys[i].test_x=this.keys[i].init_x*leftRatio;
                    this.keys[i].test_width=this.keys[i].init_width*leftRatio;
                }
                for (int i=pos+1;i<26;i++){
                    this.keys[i].test_x=this.keyboardWidth-(this.keyboardWidth-this.keys[i].init_x)*rightRatio;
                    this.keys[i].test_width=this.keys[i].init_width*rightRatio;
                }

                for (int i=11;i<18;i++){
                    this.keys[i].test_x=this.keys[i+8].test_x;
                    this.keys[i].test_width=this.keys[i+8].test_width;
                }
                this.keys[10].test_width=this.keys[11].test_x-this.keys[11].test_width/2;
                this.keys[10].test_x=this.keys[10].test_width/2;
                this.keys[18].test_width=this.keyboardWidth-this.keys[17].test_x-this.keys[17].test_width/2;
                this.keys[18].test_x=this.keyboardWidth-this.keys[18].test_width/2;

                this.keys[0].test_x=this.keys[10].test_x/2;
                this.keys[0].test_width=this.keys[10].test_x;
                this.keys[9].test_width=this.keyboardWidth-this.keys[18].test_x;
                this.keys[9].test_x=this.keyboardWidth-this.keys[9].test_width/2;
                for (int i=1;i<9;i++){
                    this.keys[i].test_x=(this.keys[i+9].test_x+this.keys[i+10].test_x)/2;
                    this.keys[i].test_width=this.keys[i+10].test_x-this.keys[i+9].test_x;
                }
            }


            for (int i=0;i<26;i++){
                if(this.keys[i].test_width<this.minWidth){
                    return false;
                }
            }
            for (int i=0;i<26;i++){
                this.keys[i].curr_x=this.keys[i].test_x;
                this.keys[i].curr_y=this.keys[i].test_y;
                this.keys[i].curr_height=this.keys[i].test_height;
                this.keys[i].curr_width=this.keys[i].test_width;
            }
            return true;
        }

        public void setLayout(){ // curr_x,curr_y
            float left=this.location[0]+this.keys[0].curr_x-this.keys[0].curr_width/2;
            float top=this.location[1]+this.keys[0].curr_y-this.keys[0].curr_height/2;
            float right=this.location[0]+this.keys[9].curr_x+this.keys[9].curr_width/2;
            float bottom=this.location[1]+this.keys[25].curr_y+this.keys[25].curr_height/2;
            this.baseBitmap = Bitmap.createBitmap(this.keyboard.getWidth(),this.keyboard.getHeight(), Bitmap.Config.ARGB_8888);
            this.canvas=new Canvas(this.baseBitmap);
            RectF rect = new RectF(left, top, right, bottom);
            this.canvas.drawRect(rect, this.backgroundPaint);
            for (int i=0;i<26;i++){
                this.canvas.drawText(String.valueOf(this.keys[i].ch),this.keys[i].curr_x+this.location[0],this.keys[i].curr_y+this.location[1],this.textPaint);
            }
            this.keyboard.setImageBitmap(this.baseBitmap);
        }

        public void resetLayout(){
            if(this.keys==null){
                this.keys=new KEY[26];
            }
            this.keys[0].ch='Q';
            this.keys[1].ch='W';
            this.keys[2].ch='E';
            this.keys[3].ch='R';
            this.keys[4].ch='T';
            this.keys[5].ch='Y';
            this.keys[6].ch='U';
            this.keys[7].ch='I';
            this.keys[8].ch='O';
            this.keys[9].ch='P';
            this.keys[10].ch='A';
            this.keys[11].ch='S';
            this.keys[12].ch='D';
            this.keys[13].ch='F';
            this.keys[14].ch='G';
            this.keys[15].ch='H';
            this.keys[16].ch='J';
            this.keys[17].ch='K';
            this.keys[18].ch='L';
            this.keys[19].ch='Z';
            this.keys[20].ch='X';
            this.keys[21].ch='C';
            this.keys[22].ch='V';
            this.keys[23].ch='B';
            this.keys[24].ch='N';
            this.keys[25].ch='M';

            for (int i=0;i<10;i++){
                this.keys[i].init_x=this.keyboardWidth*(2*i+1)/20;
                this.keys[i].init_y=this.keyboardHeight/6+this.deltaY;
            }
            for (int i=10;i<19;i++){
                this.keys[i].init_x=(this.keys[i-10].init_x+this.keys[i-9].init_x)/2;
                this.keys[i].init_y=this.keyboardHeight/2+this.deltaY;
            }
            for (int i=19;i<26;i++){
                this.keys[i].init_x=this.keys[i-8].init_x;
                this.keys[i].init_y=this.keyboardHeight*5/6+this.deltaY;
            }

            for (int i=0;i<26;i++) {
                this.keys[i].init_height=this.keyboardHeight/3;
                this.keys[i].init_width=this.keyboardWidth/10;
                this.keys[i].curr_width=this.keys[i].init_width;
                this.keys[i].curr_height=this.keys[i].init_height;
                this.keys[i].curr_x = this.keys[i].init_x;
                this.keys[i].curr_y = this.keys[i].init_y;
            }
        }
        public AutoKeyboard(ImageView keyBoard){
            this.backgroundPaint=new Paint();
            this.textPaint=new Paint();
            this.backgroundPaint.setColor(Color.rgb(230,255,255));
            this.backgroundPaint.setStrokeJoin(Paint.Join.ROUND);
            this.backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
            this.backgroundPaint.setStrokeWidth(3);

            this.textPaint.setColor(Color.BLACK);
            this.textPaint.setStrokeJoin(Paint.Join.ROUND);
            this.textPaint.setStrokeCap(Paint.Cap.ROUND);
            this.textPaint.setStrokeWidth(3);
            this.textPaint.setTextSize(Math.round(40*screen_height_ratio));
            this.keyboard=keyBoard;

            getScreenSizeRatio();
            this.keyboardHeight=this.keyboardHeight*this.screen_height_ratio;
            this.keyboardWidth=this.keyboardWidth*this.screen_width_ratio;
            this.topThreshold=this.screen_height_ratio*this.topThreshold;
            this.bottomThreshold=this.screen_width_ratio*this.bottomThreshold;
            this.minWidth=this.screen_width_ratio*this.minWidth;
            this.minHetight=this.screen_height_ratio*this.minHetight;


            this.location=new int[2];
            this.keyPos=new int[]{10,23,21,12,2,13,14,15,7,16,17,18,25,24,8,9,0,3,11,4,6,22,1,20,5,19};// A-Z 对应 Q-M
            this.keyboard.getLocationOnScreen(this.location);
            this.keys=new KEY[26];
            for (int i=0;i<26;i++){
                this.keys[i]=new KEY();
            }
            this.resetLayout();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keyboard=(ImageView)findViewById(R.id.keyboard);
        ViewTreeObserver vto2 = keyboard.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                keyboard.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                autoKeyboard.setLayout();
                keyboard.getLocationOnScreen(location);
            }
        });
        location=new int[2];
        autoKeyboard=new AutoKeyboard(keyboard);
    }

    public boolean onTouchEvent(MotionEvent event){
        float x=event.getX()-location[0];
        float y=event.getY()-location[1];
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:{
                if(autoKeyboard.tryLayout('G',x,y)){
                    autoKeyboard.setLayout();
                }
            }
        }
        return true;
    }

}
