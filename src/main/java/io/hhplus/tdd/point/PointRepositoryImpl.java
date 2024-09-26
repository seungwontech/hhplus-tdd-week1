package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class PointRepositoryImpl implements PointRepository {

    private final PointHistoryTable pointHistoryTable;

    private final UserPointTable userPointTable;

    @Override
    public UserPoint selectById(long userId) {
        return userPointTable.selectById(userId);
    }

    @Override
    public UserPoint insertOrUpdate(long userId, long amount) {
        return userPointTable.insertOrUpdate(userId, amount);
    }

    @Override
    public PointHistory insert(long userId, long amount, TransactionType type, long timestamp) {
        return pointHistoryTable.insert(userId, amount, type, timestamp);
    }

    @Override
    public List<PointHistory> selectAllByUserId(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

}
