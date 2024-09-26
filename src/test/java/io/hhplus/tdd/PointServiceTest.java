package io.hhplus.tdd;

import io.hhplus.tdd.point.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @Test
    public void 포인트조회_성공() {
        // given
        long id = 1;
        doReturn(UserPoint.empty(id)).when(pointRepository).selectById(id);

        // when
        UserPoint result = pointService.getPoint(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.id());
        assertThat(result.point()).isEqualTo(0);
    }

    @Test
    public void 포인트조회_조회값이Null인경우_실패() {
        // given
        long id = 1;
        doReturn(null).when(pointRepository).selectById(id);

        // when
        PointException result = assertThrows(PointException.class, () -> pointService.getPoint(id));

        // then
        assertThat(result.getErrorResult()).isEqualTo(PointErrorResult.POINT_NOT_FOUND);
    }

    @Test
    public void 포인트충전_성공() {
        // given
        long id = 1;
        long amount = 100;
        UserPoint userPoint = UserPoint.empty(id);
        doReturn(userPoint).when(pointRepository).selectById(id);
        doReturn(userPoint.charge(amount)).when(pointRepository).insertOrUpdate(id, amount);

        // when
        userPoint = pointService.charge(id, amount);

        // then
        assertThat(userPoint.id()).isEqualTo(id);
        assertThat(userPoint.point()).isEqualTo(amount);
    }

    @Test
    public void 포인트충전_최대포인트로인해충전이불가능한경우_실패() {
        // given
        long id = 1;
        long maxPoint = Long.MAX_VALUE;
        long amount = 1;

        UserPoint userPoint = new UserPoint(id, maxPoint, System.currentTimeMillis());
        doReturn(userPoint).when(pointRepository).selectById(id);

        // when
        PointException result = assertThrows(PointException.class, () -> pointService.charge(id, amount));

        // then
        assertThat(result.getErrorResult()).isEqualTo(PointErrorResult.POINT_CHARGE_LIMIT);
    }

    @Test
    public void 포인트충전_충전금액이0인경우_실패() {
        // given
        long id = 1;
        long amount = 0;

        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        doReturn(userPoint).when(pointRepository).selectById(id);

        // when
        PointException result = assertThrows(PointException.class, () -> pointService.charge(id, amount));

        // then
        assertThat(result.getErrorResult()).isEqualTo(PointErrorResult.POINT_CHARGE_LIMIT);
    }

    @Test
    public void 포인트충전_충전금액이음수인경우_실패() {
        // given
        long id = 1;
        long amount = -1;

        UserPoint userPoint = new UserPoint(id, 100, System.currentTimeMillis());
        doReturn(userPoint).when(pointRepository).selectById(id);

        // when
        PointException result = assertThrows(PointException.class, () -> pointService.charge(id, amount));

        // then
        assertThat(result.getErrorResult()).isEqualTo(PointErrorResult.POINT_CHARGE_LIMIT);
    }

    @Test
    public void 포인트사용_성공() {
        // given
        long id = 1;
        long useAmount = 100;
        UserPoint userPoint = new UserPoint(id, 100, System.currentTimeMillis());
        doReturn(userPoint).when(pointRepository).selectById(id);
        doReturn(userPoint.use(useAmount)).when(pointRepository).insertOrUpdate(id, userPoint.point() - useAmount);

        // when
        userPoint = pointService.use(id, useAmount);

        // then
        assertThat(userPoint.id()).isEqualTo(id);
        assertThat(userPoint.point()).isEqualTo(0);

    }

    @Test
    public void 포인트사용_사용금액이현재보유금액보다많은경우_실패() {
        // given
        long id = 1;
        long useAmount = 101;
        UserPoint userPoint = new UserPoint(id, 100, System.currentTimeMillis());
        doReturn(userPoint).when(pointRepository).selectById(id);

        // when
        PointException result = assertThrows(PointException.class, () -> pointService.use(id, useAmount));

        // then
        assertThat(result.getErrorResult()).isEqualTo(PointErrorResult.POINT_USE_LIMIT);
    }

    @Test
    public void 포인트사용_사용금액이음수인경우_실패() {
        // given
        long id = 1;
        long useAmount = -1;
        UserPoint userPoint = new UserPoint(id, 100, System.currentTimeMillis());
        doReturn(userPoint).when(pointRepository).selectById(id);

        // when
        PointException result = assertThrows(PointException.class, () -> pointService.use(id, useAmount));

        // then
        assertThat(result.getErrorResult()).isEqualTo(PointErrorResult.POINT_USE_LIMIT);
    }

    @Test
    public void 포인트충전사용내역조회_성공() {
        // given
        long userId = 1;
        List<PointHistory> history = List.of(
                new PointHistory(1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, userId, 500, TransactionType.USE, System.currentTimeMillis())
        );
        doReturn(history).when(pointRepository).selectAllByUserId(userId);

        // when
        List<PointHistory> result = pointService.getPointHistory(userId);

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void 포인트충전사용내역등록_성공() {
        long currentTimeMillis = System.currentTimeMillis();
        // given
        PointHistory pointHistory = new PointHistory(1, 1, 1000, TransactionType.CHARGE, currentTimeMillis);

        doReturn(pointHistory).when(pointRepository).insert(1, 1000, TransactionType.CHARGE, currentTimeMillis);

        // when
        PointHistory result = pointService.insert(1, 1000, TransactionType.CHARGE, currentTimeMillis);

        // then
        assertThat(result.id()).isEqualTo(1);
        assertThat(result.type()).isEqualTo(TransactionType.CHARGE);
        assertThat(result.updateMillis()).isEqualTo(currentTimeMillis);
    }

}
