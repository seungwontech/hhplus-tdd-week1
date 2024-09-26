## 동시성 제어 방식에 대한 분석 및 보고서 작성

### 동시성 제어란
여러 사용자가 동시에 데이터베이스나 자원에 접근할 대 데이터의 일관성과 무결성을 유지하기 위해 사용한 기법입니다.

### 동시성 제어 방법 중 ReentrantLock 선택한 이유
ReentrantLock: 제공하는 기능들도 많고 공정성과 순서 보장을 제공하는 장점과 사용법이 간단해서 입니다.

```java
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

```
### 동시성 테스트 코드
동시성 테스트 코드는 CountDownLatch를 사용해 사용과 충전 통합테스트를 구현했습니다.

- ### 포인트 충전 테스트
- 목적: 
  - 여러 스레드가 동시에 포인트를 충전할 때, 포인트의 총합이 정확하게 계산되는지를 검증합니다.
- 테스트 내용: 
   - 10개의 스레드가 각각 500의 포인트를 충전하는 작업을 수행합니다.
    - 모든 스레드가 작업을 완료한 후, 데이터베이스에서 해당 사용자의 포인트를 조회하여 기대하는 포인트 값(기본 포인트 + 총 충전된 포인트)이 일치하는지 확인합니다.
      
- ### 포인트 사용 테스트
- 목적:
    - 여러 스레드가 동시에 포인트를 사용할 때, 사용된 포인트가 정확히 반영되는지를 검증합니다.
- 테스트 내용:
    - 10개의 스레드가 각각 500의 포인트를 사용하는 작업을 수행합니다.
    - 모든 스레드가 작업을 완료한 후, 데이터베이스에서 해당 사용자의 포인트를 조회하여 기대하는 포인트 값(기본 포인트 - 총 사용된 포인트)이 일치하는지 확인합니다.
```java

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

```
#### 개인적 의견
동시성 제어의 중요성을 다시 한 번 느꼈습니다.
특히 ReentrantLock을 사용하면서 공정성과 순서 보장이 주는 이점을 경험할 수 있었습니다.
스레드 간의 자원 경합을 효과적으로 관리함으로써 데이터의 일관성을 유지할 수 있었습니다.
또한 CountDownLatch를 활용한 동시성 테스트는 매우 유용했습니다. 
여러 스레드가 동시에 작업을 수행하는 환경을 테스트해 보며 충전 및 사용 메서드의 신뢰성을 검증할 수 있었습니다.


