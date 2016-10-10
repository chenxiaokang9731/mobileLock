package com.example.train912.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.train912.R;

import java.util.ArrayList;

/**
 * Created by 小康 on 2016/9/13.
 */
public class LockPatternView extends View {

    private PointView[][] mPointView = new PointView[3][3];

    private ArrayList<PointView> mPointList;     //存放划过的点

    private float moveX, moveY;

    private boolean isInit = false;      //是否初始化过

    private boolean isSelect = false;            //是否选中

    private boolean isFinish = false;

    private boolean isMove = true;

    private Bitmap mNormalBtp, mPressBtp, mErrorBtp, mLineBtp, mLineErrBtp;

    private OnPatternChangeListener mListener;

    private final static int DEFAULT_SIZE = 3;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化九个点坐标
     */
    private void initView(){

        mPointList = new ArrayList<PointView>();

        int height = getHeight();
        int width = getWidth();
        int offsetY = 0;
        int offsetX = 0;

        if(height >= width){
            offsetY = (height - width)/2;   //计算y轴偏移量
            offsetX = width / 4;            //计算x轴偏移量
        }else{
            offsetY = (width - height)/2;
            offsetX = width / 4;
        }

        //初始化图标
        mNormalBtp = BitmapFactory.decodeResource(getResources(), R.drawable.btn_circle_normal);
        mPressBtp = BitmapFactory.decodeResource(getResources(), R.drawable.btn_circle_pressed);
        mErrorBtp = BitmapFactory.decodeResource(getResources(), R.drawable.btn_circle_selected);
        mLineBtp = BitmapFactory.decodeResource(getResources(), R.drawable.ddd);
        mLineErrBtp = BitmapFactory.decodeResource(getResources(), R.drawable.qqq);

        //初始化9个点坐标
        mPointView[0][0] = new PointView(offsetX, offsetY + offsetX);
        mPointView[0][1] = new PointView(offsetX * 2, offsetY + offsetX);
        mPointView[0][2] = new PointView(offsetX * 3, offsetY + offsetX);

        mPointView[1][0] = new PointView(offsetX, offsetY + offsetX * 2);
        mPointView[1][1] = new PointView(offsetX * 2, offsetY + offsetX * 2);
        mPointView[1][2] = new PointView(offsetX * 3, offsetY + offsetX * 2);

        mPointView[2][0] = new PointView(offsetX, offsetY + offsetX * 3);
        mPointView[2][1] = new PointView(offsetX * 2, offsetY + offsetX * 3);
        mPointView[2][2] = new PointView(offsetX * 3, offsetY + offsetX * 3);

        int index = 1;
        for(int raw = 0; raw < mPointView.length; raw++){
            for(int col = 0; col < mPointView[0].length; col++){
                mPointView[raw][col].index = index;
                index++;
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(!isInit){
            initView();
            isInit = true;
        }

        draw2Canvas(canvas);   //绘制图案

        //绘制线
        int size = mPointList.size();
        if(size > 0){
            PointView start = mPointList.get(0);
            for(int i = 0; i < size; i++){
                PointView end = mPointList.get(i);
                line2Canvas(canvas, start, end);
                start = end;
            }

            if(isMove){
                line2Canvas(canvas, start, new PointView(moveX, moveY));
            }
        }

        super.onDraw(canvas);
    }

    //添加画线
    private void line2Canvas(Canvas canvas, PointView a, PointView b){

        float lineLength = (float) PointView.getDistance(a, b);
//        System.out.println(lineLength);
        float degree = getDegrees(a, b);
        canvas.rotate(degree, a.x, a.y);

        Matrix matrix = new Matrix();

        if( a.state == PointView.POINT_PRESS ) {
            matrix.setScale(lineLength / mLineBtp.getWidth(), 1);
            matrix.postTranslate(a.x - mLineBtp.getWidth() / 2, a.y - mLineBtp.getWidth() / 2);
            canvas.drawBitmap(mLineBtp, matrix, mPaint);
        }else if( a.state == PointView.POINT_ERROR ){
            matrix.setScale(lineLength / mLineBtp.getWidth(), 1);
            matrix.postTranslate(a.x - mLineBtp.getWidth() / 2, a.y - mLineBtp.getWidth() / 2);
            canvas.drawBitmap(mLineErrBtp, matrix, mPaint);
        }

        canvas.rotate(-degree, a.x, a.y);
    }

    // 获取角度
    public float getDegrees(PointView pointA, PointView pointB) {
        return (float) Math.toDegrees(Math.atan2(pointB.y - pointA.y, pointB.x
                - pointA.x));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        isFinish = false;
        isMove = false;

        moveX = event.getX();
        moveY = event.getY();

        PointView pointView = new PointView();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

                 if( mListener != null ){
                    mListener.patternStartListener();
                 }

                 resetPoint();   //恢复点的初始化状态

                 pointView = checkInPattern(moveX, moveY);
                 if(pointView != null){
                    isSelect = true;
                 }
                 break;
            case MotionEvent.ACTION_MOVE:
                 if(isSelect){
                     pointView = checkInPattern(moveX, moveY);
                     if(pointView == null){
                         isMove = true;
                     }
                 }
                 break;
            case MotionEvent.ACTION_UP:
                 isFinish = true;
                 isSelect = false;
                 break;
        }

        //绘制还没有完成
        if( !isFinish && isSelect && pointView != null){

            if(containPoint(pointView)) {
                isMove = true;
            }else {
                pointView.state = PointView.POINT_PRESS;   //更新状态
                mPointList.add(pointView);
            }
        }

        //绘制完成
        if( isFinish ){

            if( mPointList.size() == 1 ){
                errorPoint();
            }else if( mPointList.size() > 1 && mPointList.size() < DEFAULT_SIZE){
                errorPoint();    //错误的点
                if(mListener != null) {
                    mListener.patternChangeListener(null);
                }
            }else{
                if(mListener != null) {
                    String pass = "";
                    int size = mPointList.size();
                    for (int i = 0; i < size; i++) {
                        pass += mPointList.get(i).index;
                    }
                    if( !TextUtils.isEmpty(pass) ) {
                        mListener.patternChangeListener(pass);
                    }
                }
            }
        }

        postInvalidate();    //发通知，调用OnDraw方法
        return true;
    }

    public boolean containPoint(PointView pv){

        if(mPointList.contains(pv)){
            return true;
        }

        return false;
    }

    /**
     * 将坐标点重新清空
     */
    private void resetPoint(){
        for(PointView pv : mPointList){
            pv.state = PointView.POINT_NORMAL;
        }
        mPointList.clear();
    }

    /**
     * 绘制错误
     */
    public void errorPoint(){
        for(PointView pv : mPointList){
            pv.state = PointView.POINT_ERROR;
        }
    }

    /**
     * 检测按下的点是否在圆内
     * @param x
     * @param y
     * @return
     */
    private PointView checkInPattern(float x, float y) {

        for(int raw = 0; raw < mPointView.length; raw++){
            for(int col = 0; col < mPointView[0].length; col++){
                PointView pointView = mPointView[raw][col];
                if(pointView.isIn(pointView.x, pointView.y, mNormalBtp.getWidth() / 2, x, y)){
                    return pointView;
                }
            }
        }

        return null;
    }

    private void draw2Canvas(Canvas canvas) {

        for(int raw = 0; raw < mPointView.length; raw++){
            for(int col = 0; col < mPointView[0].length; col++){
                if(mPointView[raw][col].state == PointView.POINT_NORMAL){
                    canvas.drawBitmap(mNormalBtp,
                                      mPointView[raw][col].x - mNormalBtp.getWidth()/2,
                                      mPointView[raw][col].y - mNormalBtp.getHeight()/2,
                                      mPaint);
                }else if (mPointView[raw][col].state == PointView.POINT_PRESS){
                    canvas.drawBitmap(mPressBtp,
                                      mPointView[raw][col].x - mNormalBtp.getWidth()/2,
                                      mPointView[raw][col].y - mNormalBtp.getHeight()/2,
                                      mPaint);
                }else if (mPointView[raw][col].state == PointView.POINT_ERROR){
                    canvas.drawBitmap(mErrorBtp,
                                      mPointView[raw][col].x - mNormalBtp.getWidth()/2,
                                      mPointView[raw][col].y - mNormalBtp.getHeight()/2,
                                      mPaint);
                }
            }
        }

    }

    private static class PointView{

        public int state = 0;  // 初始化点的状态
        public int index = 0;

        //图案的状态
        public static final int POINT_NORMAL = 0;
        public static final int POINT_PRESS = 1;
        public static final int POINT_ERROR = 2;

        float x;
        float y;

        public PointView(){}

        public PointView(float x, float y){
            this.x = x;
            this.y = y;
        }

        public static double getDistance(PointView a, PointView b){

            return Math.sqrt(Math.abs(a.x - b.x) * Math.abs(a.x - b.x) + Math.abs(a.y - b.y) * Math.abs(a.y - b.y));
        }

        /**
         * 是否在圆的范围内
         * @return
         */
        public static boolean isIn(float pointX, float pointY, float r, float moveX, float moveY){

            return Math.sqrt((pointX - moveX)*(pointX - moveX) + (pointY - moveY)*(pointY - moveY)) < r;
        }
    }

    public interface OnPatternChangeListener{
        public void patternChangeListener(String pass);
        public void patternStartListener();
    }

    public void setOnPatternChangeListener(OnPatternChangeListener listener){
        if(listener != null){
            mListener = listener;
        }
    }
}
