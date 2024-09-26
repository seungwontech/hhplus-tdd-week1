package io.hhplus.tdd.point;

import java.util.List;

public interface PointRepository {

    /**
     * 포인트를 조회
     */
    UserPoint selectById(long userId);

    /**
     * 포인트를 업데이트 또는 저장
     */
    UserPoint insertOrUpdate(long userId, long amount);

    /**
     * 포인트 충전/사용 내역을 저장
     */
    PointHistory insert(long userId, long amount, TransactionType type, long timestamp);

    /**
     * 포인트 충전/사용 내역을 조회
     */
    List<PointHistory> selectAllByUserId(long userId);
}
