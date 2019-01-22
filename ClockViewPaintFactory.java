package study.amadey.customview;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Size;

final class ClockViewPaintFactory {

    private static final String timeLabelColor = "#ffedb8";
    private static final String hourHandColor = "#d06383";
    private static final String minuteHandColor = "#fff4e2";
    private static final String watchFaceColor = "#63367b";
    private static final int TEXT_SIZE = 7;

    private ClockViewPaintFactory(){}

    static Paint produceTimeLabelPaint(){
        Paint timeLabelPaint = new Paint();
        timeLabelPaint.setColor(Color.parseColor(timeLabelColor));
        return timeLabelPaint;
    }

    static Paint produceHourHandPaint(){
        Paint hourHandPaint = new Paint();
        hourHandPaint.setColor(Color.parseColor(hourHandColor));
        return hourHandPaint;
    }

    static Paint produceMinuteHandPaint(){
        Paint minuteHandPaint = new Paint();
        minuteHandPaint.setColor(Color.parseColor(minuteHandColor));
        return minuteHandPaint;
    }

    static Paint produceWatchFacePaint(){
        Paint watchFacePaint = new Paint();
        watchFacePaint.setColor(Color.parseColor(watchFaceColor));
        return watchFacePaint;
    }

    static Paint produceTextPaintForZoom(float pixelDensity){
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(pixelDensity * TEXT_SIZE);
        return textPaint;
    }

    static int getHeightOfTextPaint(Paint textPaint, String text){
        return calculateHeightOfPaintWithinItsRect(textPaint, text);
    }

    private static Rect getTextPaintBounds(Paint textPaint, String text) {
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private static int calculateHeightOfPaintWithinItsRect(Paint textPaint, String text) {
        Rect rect = getTextPaintBounds(textPaint, text);
        return Math.abs(rect.top - rect.bottom);
    }

}
