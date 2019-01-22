package study.amadey.customview;

final class Point {

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

    int getCoordinate(Coordinate coordinate) {
        switch (coordinate) {
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
