package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointIntegrationTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    @Test
    public void 포인트조회() {
        // given
        long id = 1;
        long point = 500;
        UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
        pointRepository.insertOrUpdate(userPoint.id(), userPoint.point());

        // when
        userPoint = pointService.getPoint(id);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(id);
        assertThat(userPoint.point()).isEqualTo(point);

    }

    @Test
    public void 포인트사용성공() {
        // given
        long id = 2;
        long point = 500;
        long amount = 50;

        UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
        pointRepository.insertOrUpdate(userPoint.id(), userPoint.point());

        // when
        userPoint = pointService.use(id, amount);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(id);
        assertThat(userPoint.point()).isEqualTo(point - amount);

        PointHistory pointHistory = pointRepository.selectAllByUserId(userPoint.id()).get(0);

        System.out.println(pointHistory);
        assertThat(pointHistory).isNotNull();
        assertThat(pointHistory.userId()).isEqualTo(id);
        assertThat(pointHistory.amount()).isEqualTo(amount);
    }

    @Test
    public void 포인트충전성공() {
        // given
        long id = 3;
        long point = 500;
        long amount = 50;

        UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
        pointRepository.insertOrUpdate(userPoint.id(), userPoint.point());

        // when
        userPoint = pointService.charge(id, amount);

        // then
        assertThat(userPoint).isNotNull();
        assertThat(userPoint.id()).isEqualTo(id);
        assertThat(userPoint.point()).isEqualTo(point + amount);

        PointHistory pointHistory = pointRepository.selectAllByUserId(userPoint.id()).get(0);

        assertThat(pointHistory).isNotNull();
        assertThat(pointHistory.userId()).isEqualTo(id);
        assertThat(pointHistory.amount()).isEqualTo(amount);
    }

    @Test
    public void 포인트충전사용내역조회() {
        // given
        long userId = 4;
        List<PointHistory> history = List.of(
                new PointHistory(1, userId, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, userId, 500L, TransactionType.USE, System.currentTimeMillis())
        );

        for (PointHistory info : history) {
            pointRepository.insert(info.userId(), info.amount(), info.type(), info.updateMillis());
        }

        // when
        history = pointService.getPointHistory(userId);

        // then
        assertThat(history.size()).isEqualTo(2);

        assertThat(history.get(0).userId()).isEqualTo(userId);
        assertThat(history.get(0).amount()).isEqualTo(1000);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.CHARGE);

        assertThat(history.get(1).userId()).isEqualTo(userId);
        assertThat(history.get(1).amount()).isEqualTo(500);
        assertThat(history.get(1).type()).isEqualTo(TransactionType.USE);

    }

}
