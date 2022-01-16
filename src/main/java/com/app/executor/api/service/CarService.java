package com.app.executor.api.service;

import com.app.executor.api.entity.CarState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class CarService {
    private static final double EPSILON = 0.000001d;
    private static final int STEP_IN_MINUTES = 1;
    private static final Logger logger = LoggerFactory.getLogger(CarService.class);

    @Async
    public CompletableFuture<Double> getAmountOfRemainingFuel(CarState carState) {
        logger.info("We find out the amount of remaining fuel in the thread " + Thread.currentThread().getName());
        return CompletableFuture.completedFuture(carState.getAmountOfFuelLeft());
    }

    @Async
    public CompletableFuture<Boolean> isSuccessful(CarState carState) {
        logger.info("We check whether the car has finished its path in the thread " + Thread.currentThread().getName());
        return CompletableFuture.completedFuture(Math.abs(carState.getDistanceLeft()) < EPSILON);
    }

    public static class CarTask implements Runnable {
        private static final double NUM_OF_MINUTES_IN_AN_HOUR = 60.0;

        private final CarState carState;
        private CountDownLatch readyThreadCounter;
        private CountDownLatch callingThreadBlocker;

        public CarTask(CarState carState, CountDownLatch readyThreadCounter, CountDownLatch callingThreadBlocker) {
            this.carState = carState;
            this.readyThreadCounter = readyThreadCounter;
            this.callingThreadBlocker = callingThreadBlocker;
        }

        @Override
        public void run() {
            readyThreadCounter.countDown();
            try {
                callingThreadBlocker.await();
                boolean speedWasUpdated = false;

                while (true) {
                    logger.info("We update the state of car in the thread " + Thread.currentThread().getName()
                    + " (carState: " + carState + ")");
                    // We will assume that one second is equal to one minute in order to speed up the computation process
                    TimeUnit.SECONDS.sleep(STEP_IN_MINUTES);
                    synchronized (carState) {
                        double newDistanceLeft = Math.max(
                                carState.getDistanceLeft() - carState.getSpeed() / NUM_OF_MINUTES_IN_AN_HOUR, 0);
                        double newAmountOfFuelLeft = Math.max(
                                carState.getAmountOfFuelLeft() - carState.getFuelConsumption() / NUM_OF_MINUTES_IN_AN_HOUR, 0);
                        if (newDistanceLeft < EPSILON) {
                            newAmountOfFuelLeft = Math.max(
                                    carState.getAmountOfFuelLeft() - carState.getFuelConsumption()
                                            * carState.getDistanceLeft() / carState.getSpeed(), 0);
                        } else if (newAmountOfFuelLeft < EPSILON) {
                            newDistanceLeft = Math.max(
                                    carState.getDistanceLeft() - carState.getSpeed()
                                            * carState.getAmountOfFuelLeft() / carState.getFuelConsumption(), 0);
                        }
                        carState.setDistanceLeft(newDistanceLeft);
                        carState.setAmountOfFuelLeft(newAmountOfFuelLeft);
                        if (!speedWasUpdated && carState.getDistanceLeft() <= carState.getDistance() / 2) {
                            // На середине пути скорость автомобиля увеличивается в два раза,
                            // тем самым топливо начинает тратиться в два раза быстрее.
                            carState.setSpeed(carState.getSpeed() * 2);
                            carState.setFuelConsumption(carState.getFuelConsumption() * 2);
                            speedWasUpdated = true;
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class DistanceTask implements Runnable {
        private CarState carState;
        private CountDownLatch readyThreadCounter;
        private CountDownLatch callingThreadBlocker;
        private CountDownLatch stopDownLatch;

        public DistanceTask(CarState carState, CountDownLatch readyThreadCounter,
                            CountDownLatch callingThreadBlocker, CountDownLatch stopDownLatch) {
            this.carState = carState;
            this.readyThreadCounter = readyThreadCounter;
            this.callingThreadBlocker = callingThreadBlocker;
            this.stopDownLatch = stopDownLatch;
        }

        private boolean isFinished() {
            return carState.getDistanceLeft() < EPSILON;
        }

        @Override
        public void run() {
            readyThreadCounter.countDown();
            try {
                callingThreadBlocker.await();

                while (!isFinished()) {
                    TimeUnit.MILLISECONDS.sleep(STEP_IN_MINUTES * 1000 / 2);
                    logger.info("We check whether we need to stop DistanceTask in the thread "
                            + Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                stopDownLatch.countDown();
            }
        }
    }

    public static class FuelTask implements Runnable {
        private CarState carState;
        private CountDownLatch readyThreadCounter;
        private CountDownLatch callingThreadBlocker;
        private CountDownLatch stopDownLatch;

        public FuelTask(CarState carState, CountDownLatch readyThreadCounter,
                            CountDownLatch callingThreadBlocker, CountDownLatch stopDownLatch) {
            this.carState = carState;
            this.readyThreadCounter = readyThreadCounter;
            this.callingThreadBlocker = callingThreadBlocker;
            this.stopDownLatch = stopDownLatch;
        }

        private boolean isFinished() {
            return carState.getAmountOfFuelLeft() < EPSILON;
        }

        @Override
        public void run() {
            readyThreadCounter.countDown();
            try {
                callingThreadBlocker.await();

                while (!isFinished()) {
                    TimeUnit.MILLISECONDS.sleep(STEP_IN_MINUTES * 1000 / 2);
                    logger.info("We check whether we need to stop FuelTask in the thread "
                            + Thread.currentThread().getName());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                stopDownLatch.countDown();
            }
        }
    }
}
