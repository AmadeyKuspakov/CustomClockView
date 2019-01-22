package study.amadey.customview;

import android.graphics.RectF;

import java.time.Clock;

final class Radius {

    private RectF clockFrame;

    Radius() {}

    void setClockFrame(RectF clockFrame){
        this.clockFrame = clockFrame;
    }

    /**
     * Determines total radius of clock face which will contain spinning clock hand and time labels.
     *
     * @return
     *          returns total radius of clock face
     */
    int getTotalClockRadius() {
        return (int) clockFrame.width() / 2;
    }

    /**
     *  Determines radius of zooming in for a time label when clock hand is pointing at it.
     *
     * @return
     *          returns radius of zooming in
     */
    int getZoomingRadius() {
        return (int) (clockFrame.width() / 25);
    }

    /**
     * Determines radius of drawing for time label.
     *
     * @param clockElement
     *              determines type of drawing clock element
     * @param drawBigMinuteRadius
     *              determines whether radius of minute time label must be big or small
     * @return
     *          returns time label radius
     */
    int getTimeLabelRadius(ClockElement clockElement, boolean drawBigMinuteRadius) {
        switch (clockElement) {
            case HOUR:
                return getRadiusOfHourCircle();
            case MINUTE:
                return drawBigMinuteRadius ? getBigRadiusOfMinuteCircle() : getSmallRadiusOfMinuteCircle();
            default:
                return 0;
        }
    }
    private int getRadiusOfHourCircle() {
        return (int) clockFrame.width() / 50;
    }
    private int getBigRadiusOfMinuteCircle() {
        return (int) clockFrame.width() / 70;
    }
    private int getSmallRadiusOfMinuteCircle() {
        return (int) clockFrame.width() / 150;
    }

    /**
     * Determines radius of spinning of a clock hand depending on
     * {@code clockElement} type.
     *
     * @param clockElement
     *              determines clock hand type
     *
     * @return
     *          radius of spinning of a clock hand
     */
    int getRadiusOfSpinning(ClockElement clockElement) {
        switch (clockElement) {
            case HOUR:
                return getRadiusOfSpinningOfHourHand();
            case MINUTE:
                return getRadiusOfSpinningOfMinuteHand();
            default:
                return 0;
        }
    }
    private int getRadiusOfSpinningOfHourHand() {
        return getTotalClockRadius() - ((getTotalClockRadius() / 10) * 3);
    }
    private int getRadiusOfSpinningOfMinuteHand() {
        return getTotalClockRadius() - getTotalClockRadius() / 10;
    }
}
