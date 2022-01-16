package com.app.executor.api.controller;

import com.app.executor.api.entity.CarState;
import com.app.executor.api.entity.CheckResponseData;
import com.app.executor.api.service.CarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.DecimalMin;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Validated
public class CarController {
    private final int NUM_OF_THREADS = 3;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseEntity<CheckResponseData> handleResourceNotFoundException(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new CheckResponseData().setMessage(ex.getMessage()));
    }

    /**
     * На вход REST-контроллера подаются
     *
     * @param speed           начальная скорость автомобиля в км/ч
     * @param fuelConsumption расход топлива при начальной скорости
     * @param amountOfFuel    количество топлива в литрах
     * @param distance        расстояние в километрах, которое необходимо преодолеть
     * @return ответ на запрос
     */
    @GetMapping(value = "/check", produces = "application/json")
    public ResponseEntity<CheckResponseData> checkWhetherTheCarWillGoAllTheWay(
            @RequestParam @DecimalMin("0.0") double speed,
            @RequestParam @DecimalMin("0.0") double fuelConsumption,
            @RequestParam @DecimalMin("0.0") double amountOfFuel,
            @RequestParam @DecimalMin("0.0") double distance) throws InterruptedException, ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
        CountDownLatch readyThreadCounter = new CountDownLatch(NUM_OF_THREADS);
        CountDownLatch callingThreadBlocker = new CountDownLatch(1);
        CountDownLatch stopDownLatch = new CountDownLatch(1);

        CarState carState = new CarState()
                .setDistance(distance)
                .setDistanceLeft(distance)
                .setSpeed(speed)
                .setAmountOfFuelLeft(amountOfFuel)
                .setFuelConsumption(fuelConsumption)
                ;

        threadPool.submit(new CarService.CarTask(carState, readyThreadCounter, callingThreadBlocker));
        threadPool.submit(new CarService.DistanceTask(carState, readyThreadCounter, callingThreadBlocker, stopDownLatch));
        threadPool.submit(new CarService.FuelTask(carState, readyThreadCounter, callingThreadBlocker, stopDownLatch));

        logger.info("Waiting until all threads have started");
        readyThreadCounter.await();
        logger.info("Zeroing blocking CountDownLatch to start all threads simultaneously");
        callingThreadBlocker.countDown();
        logger.info("Waiting until either of DistanceThread and FuelThread has stopped");
        stopDownLatch.await();
        logger.info("At least one watcher thread has stopped, so we shut down another thread");
        threadPool.shutdownNow();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new CheckResponseData()
                                .setSuccessful(carService.isSuccessful(carState).get())
                                .setAmountOfFuelLeft(carService.getAmountOfRemainingFuel(carState).get())
                );
    }
}
