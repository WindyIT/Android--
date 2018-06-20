package com.example.windy.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 水平仪控件
 * 通过设置{@link #setAngle(double, double)}
 * Learn From @author chen.canney@gmail.com
 */
public class LevelView extends View {
    /**
     * 最大圈半径
     */
    private float mLimitRadius = 0;

    /**
     * 气泡半径
     */
    private float mBubbleRadius;

    private Bitmap limitCircle;
    private Bitmap bubbleBall;
    private Paint paint = new Paint();

    /**
     * 中心点坐标
     */
    private PointF centerPnt = new PointF();

    /**
     * 计算后的气泡点
     */
    private PointF bubblePoint;
    private double pitchAngle = -90;
    private double rollAngle = -90;

    public LevelView(Context context) {
        this(context, null);
    }

    public LevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        limitCircle = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_orientation);
        bubbleBall = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ball_orientation);
        mLimitRadius = limitCircle.getWidth()/2;
        mBubbleRadius = bubbleBall.getWidth()/2;
        centerPnt.set(mLimitRadius - mBubbleRadius, mLimitRadius - mBubbleRadius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;
        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize;
        }else {
            width = getPaddingLeft() + limitCircle.getWidth() + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        }else {
            height = getPaddingBottom() + limitCircle.getHeight() + getPaddingTop();
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(limitCircle, 0,0, paint);
        drawBubble(canvas);
    }

    private boolean isCenter(PointF bubblePoint){
        if(bubblePoint == null){
            return false;
        }
        float threshold = 3.0f;
        return Math.abs(bubblePoint.x - centerPnt.x) < threshold && Math.abs(bubblePoint.y - centerPnt.y) < threshold;
    }

    private void drawBubble(Canvas canvas) {
        if(bubblePoint != null){
            canvas.save();
            canvas.translate(bubblePoint.x, bubblePoint.y);
            canvas.drawBitmap(bubbleBall, 0, 0, paint);
            canvas.restore();
        }
    }

    /**
     * Convert angle to screen coordinate point.
     * @param rollAngle 横滚角(弧度)
     * @param pitchAngle 俯仰角(弧度)
     * @return
     */
    private PointF convertCoordinate(double rollAngle, double pitchAngle, double radius){
        double scale = radius / Math.toRadians(90);

        //以圆心为原点，使用弧度表示坐标
        double x0 = -(rollAngle * scale);
        double y0 = -(pitchAngle * scale);

        //使用屏幕坐标表示气泡点
        double x = centerPnt.x - x0;
        double y = centerPnt.y - y0;

        return new PointF((float)x, (float)y);
    }

    /**
     * @param pitchAngle （弧度）
     * @param rollAngle (弧度)
     */
    public void setAngle(double rollAngle, double pitchAngle) {
        this.pitchAngle = pitchAngle;
        this.rollAngle = rollAngle;

        //考虑气泡边界不超出限制圆，此处减去气泡的显示半径，做为最终的限制圆半径
        float limitRadius = mLimitRadius - mBubbleRadius;

        bubblePoint = convertCoordinate(rollAngle, pitchAngle, mLimitRadius);

        //坐标超出最大圆，取法向圆上的点
        if(outLimit(bubblePoint, limitRadius)){
            onCirclePoint(bubblePoint, limitRadius);
        }

        //水平时调用
        if(isCenter(bubblePoint)){
            setVisibility(INVISIBLE);
        } else {
            setVisibility(VISIBLE);
        }

        invalidate();
    }

    /**
     * 验证气泡点是否超过限制{@link #mLimitRadius}
     * @param bubblePnt
     * @return
     */
    private boolean outLimit(PointF bubblePnt, float limitRadius){

        float cSqrt = (bubblePnt.x - centerPnt.x) * (bubblePnt.x - centerPnt.x)
                        + (centerPnt.y - bubblePnt.y) * (centerPnt.y - bubblePnt.y);

        if(cSqrt - limitRadius * limitRadius > 0){
            return true;
        }
        return false;
    }

    /**
     * 计算圆心到 bubblePnt点在圆上的交点坐标
     * 即超出圆后的最大圆上坐标
     * @param bubblePnt 气泡点
     * @param limitRadius 限制圆的半径
     * @return
     */
    private PointF onCirclePoint(PointF bubblePnt, double limitRadius) {
        double azimuth = Math.atan2((bubblePnt.y - centerPnt.y), (bubblePnt.x - centerPnt.x));
        azimuth = azimuth < 0 ? 2 * Math.PI + azimuth : azimuth;

        //圆心+半径+角度 求圆上的坐标
        double x1 = centerPnt.x + limitRadius * Math.cos(azimuth);
        double y1 = centerPnt.y + limitRadius * Math.sin(azimuth);

        bubblePnt.set((float) x1, (float) y1);

        return bubblePnt;
    }
}
