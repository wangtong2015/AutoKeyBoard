接口函数：

- public  boolean tryLayout(char ch,float x,float y)
- public char getKeyByPosition(float x, float y, int mode)
  - mode==0 init_layout
  - mode==1 current_layout
- public void resetLayout()
  - 只是将Layout的内的坐标值复原了，还没有画出来
  - 之后需要接setLayout
- public void setLayout()
  - 将键盘画出来
- void setTopThreshold(float newTopThreshold)
  - 设置上界，默认为0
- void setBottomThreshold(float newBottomThreshold)
  - 设置下界，默认为955
- void setMinWidth(float newMinWidth)
  - 设置最小宽度，默认为72
- void setMinHetight(float newMinHeight)
  - 设置最小高度，默认为95
- void setKeyboardHeight(float newkeyBoardHeight)
  - 设置键盘高度，默认为570
- void setKeyboardWidth(float newkeyBoardWidth)
  - 设置键盘宽度，默认为1438



界面初始化

```Java
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
        autoKeyboard=new AutoKeyboard(keyboard);// 必须将作为画布的图片作为参数传入
        
    }
```



默认使用的作为画布的图片为1438*955的蓝色图片，如下所示

![keyboard](keyboard.png)

键盘默认大小为：1438*570

键盘默认位置为(左上角:0,190)，如上图所示



