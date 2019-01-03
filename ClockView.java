package study.amadey.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Amadey on 10/18/2018.
 */

public class ClockView extends View {
    private static final String tag = "Point_1";
    private static final int NUMBER_OF_MINUTES = 60;
    private static final int NUMBER_OF_HOURS = 12;
    private static final int DEGREES_IN_CIRCLE = 360;
    private RectF rect;
    private Paint circlePaint;
    private Paint arcPaint;
    private float alpha;
    private Paint paintOfMinute;
    private Paint textPaint;
    private static final int TEXT_SIZE = 7;
    private Paint minuteRect;
    private static Point canvasCenter;
    private static boolean isHourCurrentlySet = false;
    private static boolean isZoomNeeded = false;
    private static Point zoomInPoint;
    private static String zoomInText;
    private boolean isLineDrawn = false;

    public ClockView(Context context) {
        super(context);
        arcPaint = new Paint();
        rect = new RectF();
        circlePaint = new Paint();
        paintOfMinute = new Paint();
        textPaint = new Paint();
        minuteRect = new Paint();
        canvasCenter = new Point();
    }

    public ClockView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        arcPaint = new Paint();
        rect = new RectF();
        circlePaint = new Paint();
        paintOfMinute = new Paint();
        textPaint = new Paint();
        minuteRect = new Paint();
        canvasCenter = new Point();
    }

    public ClockView(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet,defStyle);
        arcPaint = new Paint();
        rect = new RectF();
        circlePaint = new Paint();
        paintOfMinute = new Paint();
        textPaint = new Paint();
        minuteRect = new Paint();
        canvasCenter = new Point();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        arcPaint.setColor(Color.parseColor("#d06383"));
        circlePaint.setColor(Color.parseColor("#63367b"));
        paintOfMinute.setColor(Color.parseColor("#ffedb8"));
        textPaint.setColor(Color.BLACK);
        float pixelDensity = getContext().getResources().getDisplayMetrics().density;
        textPaint.setTextSize(pixelDensity * TEXT_SIZE);
        minuteRect.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getCircleDiameter(int left, int top, int right, int bottom){
        int differenceHeight = bottom - top;
        int differenceWidth = right - left;
        return differenceHeight > differenceWidth ? differenceWidth : differenceHeight;
    }

    private int getAppropriateOffset(int start, int end, int bestFitLength){
        return (end - start- bestFitLength) / 2;
    }

    private void setAppropriateRectForCircle(int left, int top, int right, int bottom){
        int circleDiameter = getCircleDiameter(left, top, right, bottom);
        int newLeft = left + getAppropriateOffset(left, right, circleDiameter);
        int newTop = top + getAppropriateOffset(top, bottom, circleDiameter);
        int newRight = newLeft + circleDiameter;
        int newBottom = newTop + circleDiameter;
        rect.set(newLeft, newTop, newRight, newBottom);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        canvasCenter.setX(getWidth()/2);
        canvasCenter.setY(getHeight()/2);
        setAppropriateRectForCircle(0, 0, getWidth(), getHeight());
    }

    /*
    Returns radius of circle of clock view
     */
    private int getRadius(){
        return (int)rect.width()/2;
    }

    /*
    Returns a radius of inner circle along which minute, hour elements are drawn.
     */
    private int getRadiusOfInnerCircle(){
        return getRadius() - getRadius()/10;
    }

    /*
    Returns radius of a circle of a minute element that should be displayed for minutes divisible by 5 with remainder 0: 0, 5, 10, 15...
     */
    private int getMinuteBigRadius(){
        return (int) rect.width() / 70;
    }

    /*
    Returns radius of a circle of a minute element that should be displayes for minutes other than those divisble by 5 with remainder 0. Minutes between 0 and 5 for example.
     */
    private int getMinuteSmallRadius(){
        return (int) rect.width() / 150;
    }

    /*
    Returns radius of a circle of an hour element.
     */
    private int getHourRadius(){
        return (int) rect.width() / 50;
    }

    /*
    Returns radius of a circle of a minute element depending on currently drawing minute.
     */
    private int getCurrentMinuteRadius(int valueOfCurrentMinute){
        return valueOfCurrentMinute % 5 == 0 ? getMinuteBigRadius() : getMinuteSmallRadius();
    }

    private int getZoomingCircleRadius(){
        return (int) (rect.width() / 25);
    }

    private RectF getInnerCircleBoundingRectF(Point center, int radius){
        return new RectF(center.getX() - radius, center.getY() - radius, center.getX() + radius, center.getY() + radius);
    }

    /*
    Draws a circle for a main clock view and a special arc that shows currently chosen hour/minute element.
     */
    private void drawCircleForClock(Canvas canvas){
        canvas.drawArc(rect, -90, 360, true, circlePaint);
        if(!isLineDrawn) {
            RectF innerCircleBoundingRectF = getInnerCircleBoundingRectF(canvasCenter, getRadiusOfInnerCircle());
            canvas.drawArc(innerCircleBoundingRectF, -90, alpha, true, arcPaint);
        }else {
            canvas.drawLine(canvasCenter.x, canvasCenter.y, getXAroundClockCenter(alpha), getYAroundClockCenter(alpha), arcPaint);
        }
    }

    /*
    Returns a point of new drawing hour/minute element before its rotation along with rotation of a canvas.
     */
    private Point getPointOfCenterOfNewlyDrawnMinute(){
        int x = canvasCenter.getX();
        int y = canvasCenter.getY() - getRadiusOfInnerCircle();
        return new Point(x, y);
    }

    /*
    Draws circles on rotated @canvas depending on @isHourCircle chooses how many and of what size draw circles.
     */
    private void rotateCircles(Canvas canvas, boolean isHourCircle) {
        int amountOfElements = isHourCircle ? NUMBER_OF_HOURS : NUMBER_OF_MINUTES;
        float angle = DEGREES_IN_CIRCLE/amountOfElements;
        Point centerOfNewlyDrawnCircle = getPointOfCenterOfNewlyDrawnMinute();
        canvas.save();
        for(int i = 1; i<=60; i++){
            canvas.rotate(angle, canvasCenter.getX(), canvasCenter.getY());
            if(!isHourCircle) {
                canvas.drawCircle(centerOfNewlyDrawnCircle.getX(), centerOfNewlyDrawnCircle.getY(), getCurrentMinuteRadius(i), paintOfMinute);
            }else{
                canvas.drawCircle(centerOfNewlyDrawnCircle.getX(), centerOfNewlyDrawnCircle.getY(), getHourRadius(), paintOfMinute);
            }
        }
        canvas.restore();
    }

    private Rect getTextBounds(String text){
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private int getWidthOfText(String text){
        Rect rect = getTextBounds(text);
        return Math.abs(rect.right - rect.left);
    }

    private int getHeightOfText(String text){
        Rect rect = getTextBounds(text);
        return Math.abs(rect.top - rect.bottom);
    }

    private void zoomInOnElement(Canvas canvas){
        canvas.drawCircle(zoomInPoint.getX(), zoomInPoint.getY(), getZoomingCircleRadius(), paintOfMinute);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int x = zoomInPoint.getX();
        int y = zoomInPoint.getY() + (getHeightOfText(zoomInText) / 2);
        canvas.drawText(zoomInText, x, y, textPaint);
    }

    public void setLineOrArc(boolean isLineDrawn){
        this.isLineDrawn = isLineDrawn;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircleForClock(canvas);
        rotateCircles(canvas, isHourCurrentlySet);
        if(isZoomNeeded){
            zoomInOnElement(canvas);
        }
    }

    private float getAngleAroundClockCenter(int x, int y){
        double lengthAlongX = x - canvasCenter.getX();
        double heightAlongY = y - canvasCenter.getY();
        double tangent = 0.0;
        float angleOffset = 0.0f;

        if(lengthAlongX >= 0 && heightAlongY < 0){
            tangent = lengthAlongX / heightAlongY;
        }else if(lengthAlongX >= 0 && heightAlongY >= 0){
            tangent =  heightAlongY / lengthAlongX;
            angleOffset = 90.0f;
        }else if(lengthAlongX < 0 && heightAlongY < 0){
            tangent = heightAlongY / lengthAlongX;
            angleOffset = 270.0f;
        }else if(lengthAlongX < 0 && heightAlongY >= 0){
            tangent = lengthAlongX / heightAlongY;
            angleOffset = 180.0f;
        }
        return Math.abs((float) Math.toDegrees(Math.atan(tangent))) + angleOffset;
    }

    private float roundAngleToNearestHourMinuteElement(float currentAngle, boolean isHourCircle){
        int amountOfElements = isHourCircle ? NUMBER_OF_HOURS : NUMBER_OF_MINUTES;
        float angleBetweenContiguousElements = DEGREES_IN_CIRCLE/amountOfElements;
        float quotient = currentAngle / angleBetweenContiguousElements;
        int nearestValue = Math.round(quotient);
        return nearestValue * angleBetweenContiguousElements;
    }

    private int getXAroundClockCenter(float angle){
        double angleInRads = Math.toRadians(angle);
        double sinOfAngle = Math.sin(angleInRads);
        int x = (int)(sinOfAngle * getRadiusOfInnerCircle());
        return canvasCenter.getX() + x;
    }

    private int getYAroundClockCenter(float angle){
        double angleInRads = Math.toRadians(angle);
        double cosOfAngle = Math.cos(angleInRads);
        int y = (int)(cosOfAngle * getRadiusOfInnerCircle());
        return canvasCenter.getY() - y;
    }

    private int getCurrentHourMinuteByAngle(float angle){
        int amountOfElements = isHourCurrentlySet ? NUMBER_OF_HOURS : NUMBER_OF_MINUTES;
        float degreesBetweenTwoHourMinuteElements = DEGREES_IN_CIRCLE / amountOfElements;
        int hourMinute = (int)(angle/degreesBetweenTwoHourMinuteElements);
        if(isHourCurrentlySet){
            return hourMinute == 12 ? 0 : hourMinute;
        }else {
            return hourMinute == 60 ? 0 : hourMinute;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentAngle = getAngleAroundClockCenter((int)event.getX(), (int)event.getY());
        alpha = roundAngleToNearestHourMinuteElement(currentAngle, isHourCurrentlySet);
        Log.e("Point_1", "This is new alpha: " + alpha + " and current angle is " + currentAngle);
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                Log.e("Point_1", "Action is Action MOVE");
                zoomInPoint = new Point(getXAroundClockCenter(alpha), getYAroundClockCenter(alpha));
                zoomInText = String.valueOf(getCurrentHourMinuteByAngle(alpha));
                break;
            case MotionEvent.ACTION_DOWN:
                isZoomNeeded = true;
                zoomInPoint = new Point(getXAroundClockCenter(alpha), getYAroundClockCenter(alpha));
                zoomInText = String.valueOf(getCurrentHourMinuteByAngle(alpha));
                break;
            case MotionEvent.ACTION_UP:
                isZoomNeeded = false;
                isHourCurrentlySet = !isHourCurrentlySet;
                alpha = 0.0f;
                break;
        }
        invalidate();
        return true;
    }

    private class Point{

        private int x;
        private int y;

        Point(){}

        Point(int x, int y){
            this.x = x;
            this.y = y;
        }

        void setXY(int x, int y) {
            setX(x);
            setY(y);
        }

        void setX(int x){
            this.x = x;
        }

        void setY(int y){
            this.y = y;
        }

        int getX(){
            return x;
        }

        int getY(){
            return y;
        }

        @Override
        public String toString() {
            return "x:" + x + ",y:" + y;
        }
    }

}
