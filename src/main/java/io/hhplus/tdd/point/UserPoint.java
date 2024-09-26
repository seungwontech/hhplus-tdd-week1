package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public UserPoint charge(long amount) {
        if (point == Long.MAX_VALUE) {
            throw new PointException(PointErrorResult.POINT_CHARGE_LIMIT);
        }

        if (amount <= 0) {
            throw new PointException(PointErrorResult.POINT_CHARGE_LIMIT, "충전금액이 0이하면 충전할수 없습니다.");
        }

        return new UserPoint(id, point + amount, System.currentTimeMillis());
    }

    public UserPoint use(long amount) {
        if (point - amount < 0) {
            throw new PointException(PointErrorResult.POINT_USE_LIMIT);
        }

        if (amount <= 0) {
            throw new PointException(PointErrorResult.POINT_USE_LIMIT, "사용금액이 0이하면 사용할수 없습니다.");
        }

        return new UserPoint(id, point - amount, System.currentTimeMillis());
    }
}
