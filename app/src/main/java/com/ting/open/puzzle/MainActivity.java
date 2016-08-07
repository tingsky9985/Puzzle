package com.ting.open.puzzle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by lt on 2016/8/1.
 */
public class MainActivity extends Activity {

    private final String TAG = "MainActivity";

    //利用二维数组创建若干游戏小方块，3行5列
    private ImageView[][] gameArr = new ImageView[3][5];
    //游戏主界面
    private GridLayout mainLayout;
    //当前空方块实例的保存
    private ImageView mNoneImage;
    //判断游戏是否开始
    private boolean isGameStart = false;
    //当前动画是否是移动动画
    private boolean isAnimaRun = false;

    //当前手势
    private GestureDetector mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG,"onDown.");
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.d(TAG,"onShowPress.");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG,"onSingleTapUp.");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG,"onScroll.");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG,"onLongPress.");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int getDirByGes = getDirByGes(e1.getX(), e1.getY(), e2.getX(),e2.getY());
                Log.d(TAG,"onFling." + getDirByGes);
                changeByDir(getDirByGes);
                Toast.makeText(MainActivity.this," 移动方向："+getDirByGes,Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        setContentView(R.layout.activity_main);

        //初始化游戏的若干小方块
        Bitmap bBm = ((BitmapDrawable) getResources().getDrawable(R.drawable.lie)).getBitmap();
        //每个游戏方块的宽高
        int bBmWidth = bBm.getWidth() / 5;
        //小方块的宽高是屏幕的1/5
        int bvWidth = getWindowManager().getDefaultDisplay().getWidth()/5;
        for (int i = 0; i < gameArr.length; i++) {
            for (int j = 0; j < gameArr[0].length; j++) {
                Bitmap bm = Bitmap.createBitmap(bBm, j * bBmWidth, i * bBmWidth, bBmWidth, bBmWidth);
                gameArr[i][j] = new ImageView(this);
                //设置每个游戏方块图案
                gameArr[i][j].setImageBitmap(bm);
                //设置小方块的宽高
                gameArr[i][j].setLayoutParams(new RelativeLayout.LayoutParams(bvWidth,bvWidth));
                //设置方块间距
                gameArr[i][j].setPadding(2, 2, 2, 2);
                //绑定自定义的数据
                gameArr[i][j].setTag(new GameData(i, j, bm));
                //设置监听
                gameArr[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean flag = isHasByNoneImageView((ImageView) v);
                        Toast.makeText(MainActivity.this, "位置关系是否存在：" + flag, Toast.LENGTH_SHORT).show();
                        if (flag) {
                            changeDataByIdImageView((ImageView) v);
                        }
                    }
                });
            }
        }
        //初始化游戏主界面，添加若干的小方块
        mainLayout = (GridLayout) findViewById(R.id.main_game);
        for (int i = 0; i < gameArr.length; i++) {
            for (int j = 0; j < gameArr[0].length; j++) {
                mainLayout.addView(gameArr[i][j]);
            }
        }

        //设置最后一个方块为空
        setNoneImageView(gameArr[2][4]);
        //随机打乱顺序方法
        randomeMove();
        isGameStart = true;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"onTouchEvent");
        return mDetector.onTouchEvent(event);
    }

    /**
     * 处理上半部分无法滑动的问题
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public void changeByDir(int type){
        changeByDir(type,false);
    }

    /**
     * 根据手势的方向获取空方块相邻的位置，若存在方块，那么进行数据交换
     * @param type 1:上 2：下 3：左 4：右
     * @param isAnima true 动画， false 无动画
     */

    public void changeByDir(int type, boolean isAnima){
        //获取当前空方块的位置
        GameData mNoneGameData = (GameData) mNoneImage.getTag();
        //根据方向，设置相应的相邻位置坐标
        int new_x = mNoneGameData.x;
        int new_y = mNoneGameData.y;
        if(type == 1){//要移动的坐标在空方块的下方
            new_x ++;//x:i,y:j???
        }else if(type == 2){
            new_x --;
        }else if(type == 3){
            new_y ++;
        }else if(type == 4){
            new_y --;
        }

        //判断这个坐标是否存在
        if(new_x>=0 && new_x<gameArr.length && new_y>=0 && new_y<gameArr[0].length){
            //存在的话，开始移动
            if(isAnima) {
                changeDataByIdImageView(gameArr[new_x][new_y]);
            }else{
                changeDataByIdImageViewNoAnima(gameArr[new_x][new_y], false);
            }
        }else{
            //什么也不做
        }
        //存在的话，开始移动
    }

    /**
     *判断手势滑动方向
     * @param start_x 手势开始x
     * @param start_y 手势开始y
     * @param end_x   手势终止x
     * @param end_y   手势终止y
     * @return 1:上 2：下 3：左 4：右
     */
    private int getDirByGes(float start_x, float start_y, float end_x, float end_y) {
        Log.d(TAG,"getDirByGes: start_x: " + start_x + " start_y: " + start_y + " end_x: " + end_x + " end_y：" + end_y);
        boolean isLeftOrRgiht = (Math.abs(start_x - end_x) > Math.abs(start_y - end_y)) ? true : false;
        if (isLeftOrRgiht) {
            boolean isLeft = start_x - end_x > 0 ? true : false;
            if (isLeft) {
                return 3;
            } else {
                return 4;
            }

        } else {
            boolean isUp = start_y - end_y > 0 ? true : false;
            if (isUp) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    /**
     * 判断游戏结束的方法
     */
    public void isGameOver(){
        boolean isGameOver = true;
        //遍历到每个游戏方块
        for(int i = 0; i < gameArr.length; i++){
            for(int j = 0;j < gameArr[0].length; j++){
                //为空的方块不判断跳过
                if(mNoneImage == gameArr[i][j]){
                    continue;
                }
                GameData mGameData = (GameData) gameArr[i][j].getTag();
                if(!mGameData.isTrue()){
                    Log.d(TAG,"Game is over.");
                    isGameOver = false;
                    break;
                }
            }
        }

        //根据开关变量判断游戏是否结束
        if(isGameOver){
            Log.d(TAG,"游戏结束");
            Toast.makeText(MainActivity.this,"游戏结束！",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 随机打乱顺序
     */
    public void randomeMove(){
        for(int i = 0; i < 10; i++){
            //手势交换，无动画
            int type = (int) ((Math.random() * 4) + 1);
            changeByDir(type, false);
        }
    }

    /**
     * 不执行动画，交换两个方块的数据
     *
     * @param imageView 点击的方块
     * @param isAnima true 有动画 false 无动画
     */
    public void changeDataByIdImageViewNoAnima(final ImageView imageView,boolean isAnima){
        GameData mGameData = (GameData) imageView.getTag();
        mNoneImage.setImageBitmap(mGameData.bitmap);
        GameData mNoneGameData = (GameData) mNoneImage.getTag();
        mNoneGameData.bitmap = mGameData.bitmap;
        mNoneGameData.px = mGameData.px;
        mNoneGameData.py = mGameData.py;
        //设置当前点击的方块为空方块
        setNoneImageView(imageView);
        if(isGameStart) {
            //成功时会弹出提示
            isGameOver();
        }
    }
    /**
     * 利用动画结束之后，交换两个方块的数据
     *
     * @param imageView 点击的方块
     */
    public void changeDataByIdImageView(final ImageView imageView) {
        if(isAnimaRun){
            return;
        }
        //创建一个动画，设置好方向，移动的距离
        TranslateAnimation translateAnimation = null;
        if (imageView.getX() > mNoneImage.getX()) {//当前点击的方块在空方块下方
            //向上移动
            translateAnimation = new TranslateAnimation(0.1f, -imageView.getWidth(), 0.1f, 0.1f);
        } else if (imageView.getX() < mNoneImage.getX()) {//当前点击的方块在空方块上方
            //向下移动
            translateAnimation = new TranslateAnimation(0.1f, imageView.getWidth(), 0.1f, 0.1f);
        } else if (imageView.getY() > mNoneImage.getY()) {//当前点击的方块在空方块右方
            //向左移动
            translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, -imageView.getWidth());
        } else if (imageView.getY() < mNoneImage.getY()) {//当前点击的方块在空方块左方
            //向右移动
            translateAnimation = new TranslateAnimation(0.1f, 0.1f, 0.1f, imageView.getWidth());
        }

        //设置动画的时长
        translateAnimation.setDuration(1000);

        //设置动画结束后是否停留
        translateAnimation.setFillAfter(true);

        //设置动画结束后要真正的把数据交换
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnimaRun = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.clearAnimation();
                GameData mGameData = (GameData) imageView.getTag();
                mNoneImage.setImageBitmap(mGameData.bitmap);
                GameData mNoneGameData = (GameData) mNoneImage.getTag();
                mNoneGameData.bitmap = mGameData.bitmap;
                mNoneGameData.px = mGameData.px;
                mNoneGameData.py = mGameData.py;
                //设置当前点击的方块为空方块
                setNoneImageView(imageView);
                if(isGameStart) {
                    //成功时会弹出提示
                    isGameOver();
                }
                isAnimaRun = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //执行动画
        imageView.startAnimation(translateAnimation);
    }

    /*
    * 设置某个方块为空
    * @param ImageView
    */
    public void setNoneImageView(ImageView imageView) {
        imageView.setImageBitmap(null);
        mNoneImage = imageView;
    }

    /*
    *当前点击的方块，是否与空方块是相邻关系
    *@param imageView 所点击的方块
    *@return true 相邻，false 不相邻
     */
    public boolean isHasByNoneImageView(ImageView imageView) {

        //分别获取当前空方块的位置和点击方块的位置，通过两边都差一的方式判断
        GameData mNoneGataData = (GameData) mNoneImage.getTag();
        GameData mGataData = (GameData) imageView.getTag();

        if (mNoneGataData.y == mGataData.y && mGataData.x + 1 == mNoneGataData.x) {//当前点击的方块是在空方块的上边
            return true;
        } else if (mNoneGataData.y == mGataData.y && mGataData.x - 1 == mNoneGataData.x) {//当前点击的方块是在空方块的下边
            return true;
        } else if (mNoneGataData.y == mGataData.y + 1 && mGataData.x == mNoneGataData.x) {//当前点击的方块是在空方块的左边
            return true;
        } else if (mNoneGataData.y == mGataData.y - 1 && mGataData.x == mNoneGataData.x) {//当前点击的方块是在空方块的右边
            return true;
        }
        return false;
    }


    /*
  *每个小方块绑定的数据
   */
    class GameData {
        //每个小方块的实际位置x
        public int x = 0;
        //每个小方块的实际位置y
        public int y = 0;
        //每个小方块的图片
        public Bitmap bitmap;
        //每个小方块的图片位置x
        public int px = 0;
        //每个小方块的图片位置y
        public int py = 0;

        public GameData(int x, int y, Bitmap bitmap) {
            super();
            this.x = x;
            this.y = y;
            this.bitmap = bitmap;
            this.px = x;
            this.py = y;
        }

        /**
         * 每个小方块的位置是否正确
         * @return true 正确 false 不正确
         */
        public boolean isTrue() {
            if(x == px && y == py){
                return true;
            }
            return false;
        }
    }


}
