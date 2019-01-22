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

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by Amadey on 10/18/2018.
 */

public class ClockView extends View {
    private static final int NUMBER_OF_MINUTES = 60;
    private static final int NUMBER_OF_HOURS = 12;
    private static final int DEGREES_IN_CIRCLE = 360;
    private static final int EVERY_NTH_BIG_MINUTE = 5;
    private static final int STARTING_ANGLE_FOR_CIRCLE_DRAWING = -90;
    private boolean isZoomNeeded = false;
    private ClockHandDrawingType chosenClockHandDrawType = ClockHandDrawingType.ARC;
    private float[] alphas = new float[2];
    private RectF rect = new RectF();
    private RectF innerRect = new RectF();
    private Paint minuteHandPaint = ClockViewPaintFactory.produceMinuteHandPaint();
    private Paint watchFacePaint = ClockViewPaintFactory.produceWatchFacePaint();
    private Paint timeLabelPaint = ClockViewPaintFactory.produceTimeLabelPaint();
    private Paint hourHandPaint = ClockViewPaintFactory.produceHourHandPaint();
    private Paint textPaint = ClockViewPaintFactory.produceTextPaintForZoom(getContext().getResources().getDisplayMetrics().density);
    private Point canvasCenter = new Point();
    private Point zoomInPoint = new Point();
    private Point startingPointOfDrawingRotation = new Point();
    private Point pointOfTouch = new Point();
    private Radius radius = new Radius();
    private ClockElement currentlyChosenClockElement = ClockElement.HOUR;
    private String zoomInText;
    private ArrayList<ClockElement> clockElements = new ArrayList<>(EnumSet.allOf(ClockElement.class));
    private ArrayList<Paint> clockHandsPaintList = new ArrayList<>(Arrays.asList(minuteHandPaint, hourHandPaint));

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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        rect = null;
        innerRect = null;
        minuteHandPaint = null;
        watchFacePaint = null;
        timeLabelPaint = null;
        hourHandPaint = null;
        textPaint = null;
        canvasCenter = null;
        zoomInPoint = null;
        startingPointOfDrawingRotation = null;
        pointOfTouch = null;
        radius = null;
        clockElements.clear();
        clockElements = null;
        clockHandsPaintList.clear();
        clockHandsPaintList = null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        canvasCenter.setX(getWidth() / 2);
        canvasCenter.setY(getHeight() / 2);
        setAppropriateRectForCircle(0, 0, getWidth(), getHeight());
        radius.setClockFrame(rect);
    }

    private void setAppropriateRectForInnerCircle(Point center, int radius) {
        int newLeft = center.getX() - radius;
        int newTop = center.getY() - radius;
        int newRight = center.getX() + radius;
        int newBottom = center.getY() + radius;
        innerRect.set(newLeft, newTop, newRight, newBottom);
    }

    private void setAppropriateRectForCircle(int left, int top, int right, int bottom) {
        int circleDiameter = getCircleDiameter(left, top, right, bottom);
        int newLeft = left + getAppropriateOffset(left, right, circleDiameter);
        int newTop = top + getAppropriateOffset(top, bottom, circleDiameter);
        int newRight = newLeft + circleDiameter;
        int newBottom = newTop + circleDiameter;
        rect.set(newLeft, newTop, newRight, newBottom);
    }

    private int getAppropriateOffset(int start, int end, int bestFitLength) {
        return (end - start - bestFitLength) / 2;
    }

    private int getCircleDiameter(int left, int top, int right, int bottom) {
        int differenceHeight = bottom - top;
        int differenceWidth = right - left;
        return differenceHeight > differenceWidth ? differenceWidth : differenceHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawClock(canvas, alphas);
        drawTimeLabelsOfWatchFace(canvas, clockElements);
        if (isZoomNeeded) {
            zoomInOnElement(canvas);
        }
    }

    private void drawTimeLabelsOfWatchFace(Canvas canvas, ArrayList<ClockElement> ClockElement) {
        for (ClockElement clockElement : ClockElement) {
            drawTimeLabel(canvas, clockElement);
        }
    }

    private void drawTimeLabel(Canvas canvas, ClockElement clockElement) {
        int amountOfElements = getAmountOfClockElement(clockElement);
        float angle = DEGREES_IN_CIRCLE / amountOfElements;
        int timeLabelRadius;
        setStartingPointOfDrawingRotation(clockElement, radius, startingPointOfDrawingRotation);
        canvas.save();
        for (int i = 1; i <= amountOfElements; i++) {
            canvas.rotate(angle, canvasCenter.getX(), canvasCenter.getY());
            timeLabelRadius = radius.getTimeLabelRadius(clockElement, isBigMinuteCircleNeeded(i));
            canvas.drawCircle(startingPointOfDrawingRotation.getX(), startingPointOfDrawingRotation.getY(), timeLabelRadius, timeLabelPaint);
        }
        canvas.restore();
    }

    private boolean isBigMinuteCircleNeeded(int i) {
        return i % EVERY_NTH_BIG_MINUTE == 0;
    }

    private void setStartingPointOfDrawingRotation(ClockElement clockElement, Radius radius, Point point) {
        int x = canvasCenter.getX();
        int y = canvasCenter.getY() - radius.getRadiusOfSpinning(clockElement);
        point.setXY(x, y);
    }

    private void drawClock(Canvas canvas, float clockHandRotationAngles[]) {
        canvas.drawArc(rect, STARTING_ANGLE_FOR_CIRCLE_DRAWING, DEGREES_IN_CIRCLE, true, watchFacePaint);
        for (int i = 0; i < clockHandRotationAngles.length; i++) {
            drawClockHand(canvas, clockHandRotationAngles[i], radius.getRadiusOfSpinning(clockElements.get(i)), clockHandsPaintList.get(i));
        }
    }

    private void drawClockHand(Canvas canvas, float clockHandRotationAngle, int clockHandLength, Paint clockHandPaint) {
        switch (chosenClockHandDrawType) {
            case ARC:
                drawArc(canvas, clockHandRotationAngle, clockHandLength, clockHandPaint);
                break;
            case LINE:
                drawLine(canvas, clockHandRotationAngle, clockHandLength, clockHandPaint);
                break;
        }
    }

    private void drawArc(Canvas canvas, float angle, int radius, Paint paint) {
        setAppropriateRectForInnerCircle(canvasCenter, radius);
        canvas.drawArc(innerRect, STARTING_ANGLE_FOR_CIRCLE_DRAWING, angle, true, paint);
    }

    private void drawLine(Canvas canvas, float angle, int length, Paint paint) {
        canvas.drawLine(canvasCenter.getX(), canvasCenter.getY(),
                calculateCoordinateOfRotation(Coordinate.X, angle, length),
                calculateCoordinateOfRotation(Coordinate.Y, angle, length), paint);
    }

    private void zoomInOnElement(Canvas canvas) {
        canvas.drawCircle(zoomInPoint.getX(), zoomInPoint.getY(), radius.getZoomingRadius(), timeLabelPaint);
        int x = zoomInPoint.getX();
        int y = zoomInPoint.getY() + (ClockViewPaintFactory.getHeightOfTextPaint(textPaint, zoomInText) / 2);
        canvas.drawText(zoomInText, x, y, textPaint);
    }

    private int getAmountOfClockElement(ClockElement clockElement) {
        switch (clockElement) {
            case HOUR:
                return NUMBER_OF_HOURS;
            case MINUTE:
                return NUMBER_OF_MINUTES;
            default:
                return 0;
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

    private float roundAngleToNearestClockElement(float currentAngle, ClockElement clockElement) {
        float angleBetweenContiguousElements = DEGREES_IN_CIRCLE / getAmountOfClockElement(clockElement);
        float quotient = currentAngle / angleBetweenContiguousElements;
        int nearestValue = Math.round(quotient);
        return nearestValue * angleBetweenContiguousElements;
    }

    private double calculateTrigonometricValueOfAngle(Coordinate coordinate, float angle) {
        double angleInRads = Math.toRadians(angle);
        switch (coordinate) {
            case X:
                return Math.sin(angleInRads);
            case Y:
                return Math.cos(angleInRads);
            default:
                return 0;
        }
    }

    private int getAngleCoordinateSign(Coordinate coordinate) {
        switch (coordinate) {
            case X:
                return 1;
            case Y:
                return -1;
            default:
                return 0;
        }
    }

    private int calculateCoordinateOfRotation(Coordinate coordinate, float angle, int radius) {
        double trigonometricValueOfAngle = calculateTrigonometricValueOfAngle(coordinate, angle);
        int coordinateAroundCenter = (int) (trigonometricValueOfAngle * radius);
        return canvasCenter.getCoordinate(coordinate) + (getAngleCoordinateSign(coordinate) * coordinateAroundCenter);
    }

    private int normalizeCurrentClockElement(ClockElement clockElement, int currentClockElement) {
        switch (clockElement) {
            case HOUR:
                return currentClockElement == NUMBER_OF_HOURS ? 0 : currentClockElement;
            case MINUTE:
                return currentClockElement == NUMBER_OF_MINUTES ? 0 : currentClockElement;
            default:
                return 0;
        }
    }

    private int getCurrentClockElementByAngle(float angle, ClockElement clockElement) {
        int amountOfElements = getAmountOfClockElement(clockElement);
        float angleBetweenTwoContiguousClockElement = DEGREES_IN_CIRCLE / amountOfElements;
        int currentClockElement = (int) (angle / angleBetweenTwoContiguousClockElement);
        return normalizeCurrentClockElement(clockElement, currentClockElement);
    }

    private ClockElement clockElementChosen(Point pointOfTouch, Radius radius) {
        int normalizedX = pointOfTouch.getX() - canvasCenter.getX();
        int normalizedY = pointOfTouch.getY() - canvasCenter.getY();
        double length = Math.sqrt(normalizedX * normalizedX + normalizedY * normalizedY);
        if (radius.getRadiusOfSpinning(ClockElement.HOUR) >= length) {
            return ClockElement.HOUR;
        } else {
            return ClockElement.MINUTE;
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
                alphas[clockElements.indexOf(currentlyChosenClockElement)] = roundAngleToNearestClockElement(getAngleAroundClockCenter(pointOfTouch.getX(), pointOfTouch.getY()), currentlyChosenClockElement);
                zoomInPoint.setXY(
                        calculateCoordinateOfRotation(Coordinate.X, alphas[clockElements.indexOf(currentlyChosenClockElement)],
                                radius.getRadiusOfSpinning(currentlyChosenClockElement)),
                        calculateCoordinateOfRotation(Coordinate.Y, alphas[clockElements.indexOf(currentlyChosenClockElement)],
                                radius.getRadiusOfSpinning(currentlyChosenClockElement)));
                zoomInText = String.valueOf(getCurrentClockElementByAngle(alphas[clockElements.indexOf(currentlyChosenClockElement)], currentlyChosenClockElement));
                break;
            case MotionEvent.ACTION_UP:
                isZoomNeeded = false;
                currentlyChosenClockElement = ClockElement.MINUTE;
                break;
        }
        invalidate();
        return true;
    }

    public void setLineOrArc(boolean isLineDrawn) {
        if (isLineDrawn) {
            chosenClockHandDrawType = ClockHandDrawingType.LINE;
        }
        invalidate();
    }

    public void setTime(int currentHour, int currentMinute) {
        alphas[clockElements.indexOf(ClockElement.HOUR)] = currentHour * (DEGREES_IN_CIRCLE / getAmountOfClockElement(ClockElement.HOUR));
        alphas[clockElements.indexOf(ClockElement.MINUTE)] = currentMinute * (DEGREES_IN_CIRCLE / getAmountOfClockElement(ClockElement.MINUTE));
    }

}
