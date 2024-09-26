package io.hhplus.tdd.point;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final ReentrantLock lock = new ReentrantLock(true);

    private final PointRepository pointRepository;

    /**
     * 포인트를 조회
     *
     * @param id
     * @return
     */
    public UserPoint getPoint(long id) {
        UserPoint userPoint = pointRepository.selectById(id);
        if (userPoint == null) {
            throw new PointException(PointErrorResult.POINT_NOT_FOUND);
        }
        return userPoint;
    }

    /**
     * 포인트를 충전
     *
     * @param id
     * @param amount
     * @return
     */
    public UserPoint charge(long id, long amount) {
        lock.lock();
        try {

            UserPoint userPoint = pointRepository.selectById(id);
            if (userPoint == null) {
                throw new PointException(PointErrorResult.POINT_NOT_FOUND);
            }

            userPoint = userPoint.charge(amount);

            pointRepository.insertOrUpdate(id, userPoint.point());

            insert(userPoint.id(), amount, TransactionType.CHARGE, userPoint.updateMillis());

            return userPoint;

        } finally {
            lock.unlock();
        }

    }

    /**
     * 포인트 사용
     *
     * @param id
     * @param amount
     * @return
     */
    public UserPoint use(long id, long amount) {
        lock.lock();
        try {

            UserPoint userPoint = pointRepository.selectById(id);
            if (userPoint == null) {
                throw new PointException(PointErrorResult.POINT_NOT_FOUND);
            }

            userPoint = userPoint.use(amount);

            pointRepository.insertOrUpdate(id, userPoint.point());

            insert(userPoint.id(), amount, TransactionType.USE, userPoint.updateMillis());

            return userPoint;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 포인트 충전/사용 내역 조회
     *
     * @param userId
     * @return
     */
    public List<PointHistory> getPointHistory(long userId) {
        return pointRepository.selectAllByUserId(userId);
    }

    /**
     * 포인트 충전/사용 내역 등록
     *
     * @param userId
     * @param amount
     * @param type
     * @param currentTimeMillis
     * @return
     */
    public PointHistory insert(long userId, long amount, TransactionType type, long currentTimeMillis) {
        return pointRepository.insert(userId, amount, type, currentTimeMillis);
    }

}
