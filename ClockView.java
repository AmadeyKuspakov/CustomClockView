package study.amadey.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Amadey on 10/18/2018.
 */

public class ClockView extends View {
    private static final int NUMBER_OF_MINUTES = 60;
    private static final int NUMBER_OF_HOURS = 12;
    private static final int DEGREES_IN_CIRCLE = 360;
    private static final int TEXT_SIZE = 7;
    private static final int EVERY_NTH_BIG_MINUTE = 5;
    private boolean isZoomNeeded = false;
    private boolean isLineDrawn = false;
    private float alpha;
    private RectF rect = new RectF();
    private RectF innerRect = new RectF();
    private Paint arcPaint = new Paint();
    private Paint circlePaint = new Paint();
    private Paint paintOfMinute = new Paint();
    private Paint textPaint = new Paint();
    private Point canvasCenter = new Point();
    private Point zoomInPoint = new Point();
    private Point centerOfCurrentlyDrawnClockElement = new Point();
    private Point pointOfTouch = new Point();
    private Radius radius = new Radius();
    private ClockElements currentlyChosenClockElement = ClockElements.HOUR;
    private String zoomInText;

    public ClockView(Context context) {
        super(context);
    }

    public ClockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public ClockView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
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
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        rect = null;
        innerRect = null;
        arcPaint = null;
        circlePaint = null;
        paintOfMinute = null;
        textPaint = null;
        canvasCenter = null;
        zoomInPoint = null;
        centerOfCurrentlyDrawnClockElement = null;
        pointOfTouch = null;
        radius.destroy();
        radius = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getCircleDiameter(int left, int top, int right, int bottom) {
        int differenceHeight = bottom - top;
        int differenceWidth = right - left;
        return differenceHeight > differenceWidth ? differenceWidth : differenceHeight;
    }

    private int getAppropriateOffset(int start, int end, int bestFitLength) {
        return (end - start - bestFitLength) / 2;
    }

    private void setAppropriateRectForCircle(int left, int top, int right, int bottom) {
        int circleDiameter = getCircleDiameter(left, top, right, bottom);
        int newLeft = left + getAppropriateOffset(left, right, circleDiameter);
        int newTop = top + getAppropriateOffset(top, bottom, circleDiameter);
        int newRight = newLeft + circleDiameter;
        int newBottom = newTop + circleDiameter;
        rect.set(newLeft, newTop, newRight, newBottom);
    }

    private void setAppropriateRectForInnerCircle(Point center, int radius){
        int newLeft = center.getX() - radius;
        int newTop = center.getY() - radius;
        int newRight = center.getX() + radius;
        int newBottom = center.getY() + radius;
        innerRect.set(newLeft, newTop, newRight, newBottom);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        canvasCenter.setX(getWidth() / 2);
        canvasCenter.setY(getHeight() / 2);
        setAppropriateRectForCircle(0, 0, getWidth(), getHeight());
        radius.setRect(rect);
    }

    private int getAmountOfClockElements(ClockElements clockElement){
        switch (clockElement){
            case HOUR:
                return NUMBER_OF_HOURS;
            case MINUTE:
                return NUMBER_OF_MINUTES;
            default:
                return 0;
        }
    }

    /*
    Draws a circle for a main clock view and a special arc that shows currently chosen hour/minute element.
     */
    private void drawCircleForClock(Canvas canvas, boolean isLineDrawn, ClockElements clockElement, Radius radius) {
        canvas.drawArc(rect, -90, 360, true, circlePaint);
        if (!isLineDrawn) {
            setAppropriateRectForInnerCircle(canvasCenter, radius.getRadiusOfInnerCircleForClockElement(clockElement));
            canvas.drawArc(innerRect, -90, alpha, true, arcPaint);
        } else {
            canvas.drawLine(canvasCenter.x, canvasCenter.y,
                    getCoordinateAroundClockCenter(Coordinates.X, alpha, currentlyChosenClockElement, radius),
                    getCoordinateAroundClockCenter(Coordinates.Y, alpha, currentlyChosenClockElement, radius), arcPaint);
        }
    }

    private void setCenterPointOfCurrentlyDrawnClockElement(ClockElements clockElement, Radius radius, Point point){
        int x = canvasCenter.getX();
        int y = canvasCenter.getY() - radius.getRadiusOfInnerCircleForClockElement(clockElement);
        point.setXY(x, y);
    }

    private boolean isBigMinuteCircleNeeded(int i){
        return i % EVERY_NTH_BIG_MINUTE == 0;
    }

    private void drawRotatedClockElements(Canvas canvas, Radius radius, ClockElements clockElement){
        int amountOfElements = getAmountOfClockElements(clockElement);
        float angle = DEGREES_IN_CIRCLE / amountOfElements;
        setCenterPointOfCurrentlyDrawnClockElement(clockElement, radius, centerOfCurrentlyDrawnClockElement);
        int radiusForClockElement;
        canvas.save();
        for(int i = 1; i <= amountOfElements; i++){
            canvas.rotate(angle, canvasCenter.getX(), canvasCenter.getY());
            radiusForClockElement = radius.getRadiusForClockElements(clockElement, isBigMinuteCircleNeeded(i));
            canvas.drawCircle(centerOfCurrentlyDrawnClockElement.getX(), centerOfCurrentlyDrawnClockElement.getY(), radiusForClockElement, paintOfMinute);
        }
        canvas.restore();
    }

    private Rect getTextBounds(String text) {
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private int getHeightOfText(String text) {
        Rect rect = getTextBounds(text);
        return Math.abs(rect.top - rect.bottom);
    }

    private void zoomInOnElement(Canvas canvas, Radius radius) {
        canvas.drawCircle(zoomInPoint.getX(), zoomInPoint.getY(), radius.getZoomingCircleRadius(), paintOfMinute);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int x = zoomInPoint.getX();
        int y = zoomInPoint.getY() + (getHeightOfText(zoomInText) / 2);
        canvas.drawText(zoomInText, x, y, textPaint);
    }

    public void setLineOrArc(boolean isLineDrawn) {
        this.isLineDrawn = isLineDrawn;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircleForClock(canvas, isLineDrawn, currentlyChosenClockElement, radius);
        drawRotatedClockElements(canvas, radius, ClockElements.MINUTE);
        drawRotatedClockElements(canvas, radius, ClockElements.HOUR);
        if (isZoomNeeded) {
            zoomInOnElement(canvas, radius);
        }
    }

    private float getAngleAroundClockCenter(int x, int y) {
        double lengthAlongX = x - canvasCenter.getX();
        double heightAlongY = y - canvasCenter.getY();
        double tangent = 0.0;
        float angleOffset = 0.0f;

        if (lengthAlongX >= 0 && heightAlongY < 0) {
            tangent = lengthAlongX / heightAlongY;
        } else if (lengthAlongX >= 0 && heightAlongY >= 0) {
            tangent = heightAlongY / lengthAlongX;
            angleOffset = 90.0f;
        } else if (lengthAlongX < 0 && heightAlongY < 0) {
            tangent = heightAlongY / lengthAlongX;
            angleOffset = 270.0f;
        } else if (lengthAlongX < 0 && heightAlongY >= 0) {
            tangent = lengthAlongX / heightAlongY;
            angleOffset = 180.0f;
        }
        return Math.abs((float) Math.toDegrees(Math.atan(tangent))) + angleOffset;
    }

    private float roundAngleToNearestClockElement(float currentAngle, ClockElements clockElement) {
        float angleBetweenContiguousElements = DEGREES_IN_CIRCLE / getAmountOfClockElements(clockElement);
        float quotient = currentAngle / angleBetweenContiguousElements;
        int nearestValue = Math.round(quotient);
        return nearestValue * angleBetweenContiguousElements;
    }

    private double calculateTrigonometricValueOfAngle(Coordinates coordinate, float angle){
        double angleInRads = Math.toRadians(angle);
        switch (coordinate){
            case X:
                return Math.sin(angleInRads);
            case Y:
                return Math.cos(angleInRads);
            default:
                return 0;
        }
    }

    private int getAngleCoordinateSign(Coordinates coordinate){
        switch (coordinate){
            case X:
                return 1;
            case Y:
                return -1;
            default:
                return 0;
        }
    }

    private int getCoordinateAroundClockCenter(Coordinates coordinate, float angle, ClockElements clockElement, Radius radius){
        double trigonometricValueOfAngle = calculateTrigonometricValueOfAngle(coordinate, angle);
        int radiusOfInnerCircleForClockElement = radius.getRadiusOfInnerCircleForClockElement(clockElement);
        int coordinateAroundCenter = (int) (trigonometricValueOfAngle * radiusOfInnerCircleForClockElement);
        return canvasCenter.getCoordinate(coordinate) + (getAngleCoordinateSign(coordinate) * coordinateAroundCenter);
    }

    private int normalizeCurrentClockElement(ClockElements clockElement, int currentClockElement){
        switch (clockElement){
            case HOUR:
                return currentClockElement == NUMBER_OF_HOURS ? 0 : currentClockElement;
            case MINUTE:
                return currentClockElement == NUMBER_OF_MINUTES ? 0 : currentClockElement;
            default:
                return 0;
        }
    }

    private int getCurrentClockElementByAngle(float angle, ClockElements clockElement) {
        int amountOfElements = getAmountOfClockElements(clockElement);
        float angleBetweenTwoContiguousClockElements = DEGREES_IN_CIRCLE / amountOfElements;
        int currentClockElement = (int) (angle / angleBetweenTwoContiguousClockElements);
        return normalizeCurrentClockElement(clockElement, currentClockElement);
    }

    private ClockElements clockElementChosen(Point pointOfTouch, Radius radius) {
        int normalizedX = pointOfTouch.getX() - canvasCenter.getX();
        int normalizedY = pointOfTouch.getY() - canvasCenter.getY();
        double length = Math.sqrt(normalizedX * normalizedX + normalizedY * normalizedY);
        if(radius.getRadiusOfInnerHourCircle() >= length){
            return ClockElements.HOUR;
        }else if(radius.getRadiusOfInnerHourCircle() < length){
            return ClockElements.MINUTE;
        }else{
            return null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointOfTouch.setXY((int) event.getX(), (int) event.getY());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isZoomNeeded = true;
                currentlyChosenClockElement = clockElementChosen(pointOfTouch, radius);
            case MotionEvent.ACTION_MOVE:
                alpha = roundAngleToNearestClockElement(getAngleAroundClockCenter(pointOfTouch.getX(), pointOfTouch.getY()), currentlyChosenClockElement);
                zoomInPoint.setXY(getCoordinateAroundClockCenter(Coordinates.X, alpha, currentlyChosenClockElement, radius),
                        getCoordinateAroundClockCenter(Coordinates.Y, alpha, currentlyChosenClockElement, radius));
                zoomInText = String.valueOf(getCurrentClockElementByAngle(alpha, currentlyChosenClockElement));
                break;
            case MotionEvent.ACTION_UP:
                isZoomNeeded = false;
                currentlyChosenClockElement = ClockElements.MINUTE;
                alpha = 0.0f;
                break;
        }
        invalidate();
        return true;
    }

    public void setTime(int currentHour, int currentMinute) {
        // to be added
    }

    private class Point {

        private int x;
        private int y;

        Point() {
        }

        void setXY(int x, int y) {
            setX(x);
            setY(y);
        }

        void setX(int x) {
            this.x = x;
        }

        void setY(int y) {
            this.y = y;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        int getCoordinate(Coordinates coordinate){
            switch (coordinate){
                case X:
                    return x;
                case Y:
                    return y;
                default:
                    return 0;
            }
        }

        @Override
        public String toString() {
            return "x:" + x + ",y:" + y;
        }
    }

    private class Radius {

        private RectF rect;

        private Radius() {
        }

        void setRect(RectF rect) {
            this.rect = rect;
        }

        /*
                Returns radius of circle of clock view
                 */
        int getRadius() {
            return (int) rect.width() / 2;
        }

        /*
        Returns a radius of inner circle along which minute, hour elements are drawn.
         */
        int getRadiusOfInnerMinuteCircle() {
            return getRadius() - getRadius() / 10;
        }

        int getRadiusOfInnerHourCircle() {
            return getRadius() - ((getRadius() / 10) * 3);
        }

        /*
        Returns radius of a circle of a minute element that should be displayed for minutes divisible by 5 with remainder 0: 0, 5, 10, 15...
         */
        int getMinuteBigRadius() {
            return (int) rect.width() / 70;
        }

        /*
        Returns radius of a circle of a minute element that should be displayes for minutes other than those divisble by 5 with remainder 0. Minutes between 0 and 5 for example.
         */
        int getMinuteSmallRadius() {
            return (int) rect.width() / 150;
        }

        /*
        Returns radius of a circle of an hour element.
         */
        int getHourRadius() {
            return (int) rect.width() / 50;
        }

        int getZoomingCircleRadius() {
            return (int) (rect.width() / 25);
        }

        int getRadiusForClockElements(ClockElements clockElement, boolean drawBigMinuteRadius){
            switch (clockElement){
                case HOUR:
                    return getHourRadius();
                case MINUTE:
                    return drawBigMinuteRadius ? getMinuteBigRadius() : getMinuteSmallRadius();
                default:
                    return 0;
            }
        }

        private int getRadiusOfInnerCircleForClockElement(ClockElements clockElement){
            switch (clockElement){
                case HOUR:
                    return getRadiusOfInnerHourCircle();
                case MINUTE:
                    return getRadiusOfInnerMinuteCircle();
                default:
                    return 0;
            }
        }

        private void destroy(){
            rect = null;
        }

    }

    private enum ClockElements{

        HOUR, MINUTE

    }

    private enum Coordinates{

        X, Y

    }

}
