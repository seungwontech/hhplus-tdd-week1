package io.hhplus.tdd;

import io.hhplus.tdd.point.PointRepository;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointConcurrencyIntegrationTest {

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    @Test
    public void 포인트충전테스트() throws InterruptedException {
        // given
        long id = 1;
        long point = 1000;
        long chargeAmount = 500;
        int threadCount = 10;

        UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
        pointRepository.insertOrUpdate(userPoint.id(), userPoint.point());

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(id, chargeAmount);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        // then
        UserPoint updatedUserPoint = pointRepository.selectById(id);
        long expectedPoint = point + (chargeAmount * threadCount);
        assertThat(updatedUserPoint.point()).isEqualTo(expectedPoint);
    }

    @Test
    public void 포인트사용테스트() throws InterruptedException {
        // given
        long id = 1;
        long point = 5000;
        long useAmount = 500;
        int threadCount = 10;

        UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());
        pointRepository.insertOrUpdate(userPoint.id(), userPoint.point());

        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.use(id, useAmount);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        // then
        UserPoint updatedUserPoint = pointRepository.selectById(id);
        long expectedPoint = point - (useAmount * threadCount);
        assertThat(updatedUserPoint.point()).isEqualTo(expectedPoint);
    }

}
