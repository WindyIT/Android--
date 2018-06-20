package com.example.windy.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class OrientationView extends View {
    private Context ctx;
    private Bitmap bgOrientation;
    private BallPainter ball;
    private Paint paint = new Paint();

    public OrientationView(Context context) {
        this(context, null);
    }

    public OrientationView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OrientationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ctx = context;
        bgOrientation = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.bg_orientation);
        Bitmap ballOrientation = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.ball_orientation);
        ball = new BallPainter(ballOrientation, bgOrientation.getWidth()/2 - ballOrientation.getWidth()/2, bgOrientation.getHeight()/2 - ballOrientation.getHeight()/2, bgOrientation.getWidth() / 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }else {
            width = getPaddingLeft() + bgOrientation.getWidth() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }else {
            height = getPaddingBottom() + bgOrientation.getHeight() + getPaddingTop();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bgOrientation, 0,0, paint);
        canvas.drawBitmap(ball.getBitmap(), ball.getX(), ball.getY(), paint);
    }

    /**
     * 手机水平左转/右转，小球向右/向左浮动
     * 手机水平上转/下转，小球向下/向上浮动
     */
    private float prevRoll = 0;
    private float prevPitch = 0;
    public void rollBall(float currPitch, float currRoll){
        float pitchAngle = currPitch - prevPitch;
        float rollAngle = currRoll - prevRoll;

        Log.e("Angle", pitchAngle + " || " + rollAngle);
        //转动幅度过小 不作处理
        if (Math.abs(pitchAngle) < 0.08 && Math.abs(rollAngle) < 0.08){
            return;
        }
        //回归水平状态 不作计算 小球直接居中
        if ((int)currPitch == 0 && (int)currRoll == 0){
            ball.setOrigin();
            return;
        }
        ball.mapY(pitchAngle);
        ball.mapX(rollAngle);
        postInvalidate();
        prevPitch = currPitch;
        prevRoll = currRoll;

        //  if (currPitch >= 0){ //手机顶部向下，小球y值增加
      //  } else {

      //  }

        if (currRoll >= 0){ //手机右转，小球x值减少

        }else {

        }

        if (Math.abs(currPitch - prevPitch) >= 0.1 || Math.abs(currRoll - prevRoll) >= 0.1){

        }
    }

    private boolean isOutOfBounds(float c){
        return c > 1.0 || c < -1.0;
    }

    private class BallPainter{
        private float xParent, yParent; //外圆的圆心坐标
        private float radiusParent;

        private float x, y; //小球的圆心坐标

        private Bitmap bitmap; //小球图片

        public BallPainter(@NonNull Bitmap bitmap, float xParent, float yParent, float radiusParent) {
            this.bitmap = bitmap;
            this.xParent = xParent;
            this.yParent = yParent;
            this.radiusParent = radiusParent;
            this.x = xParent;
            this.y = yParent;
        }

        public void setOrigin(){
            x = xParent;
            y = yParent;
        }

        /**
         * 根据半径计算出小球的XY偏移值
         * @param c 范围 -1.0~1.0
         */
        public void mapX(float c){
            float tmpX = (radiusParent * c) + x;
            if (xParent - radiusParent <= tmpX && tmpX <= xParent + radiusParent){ //坐标不越界
                x = tmpX;
            }
            Log.e("BALL", "X: " + x + " Ra: " + radiusParent * c);
        }

        public void mapY(float c){
            float tmpY = (radiusParent * c) + y;
            if (yParent - radiusParent <= tmpY && tmpY <= yParent + radiusParent){ //坐标不越界
                y = tmpY;
            }
            Log.e("BALL", "Y: " + y + " Ra: " + radiusParent * c);
        }

        public float getRadiusParent() {
            return radiusParent;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setxParent(int xParent) {
            this.xParent = xParent;
        }

        public void setyParent(int yParent) {
            this.yParent = yParent;
        }

        public void setRadiusParent(float radiusParent) {
            this.radiusParent = radiusParent;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}
