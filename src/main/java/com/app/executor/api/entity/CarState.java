package com.app.executor.api.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class CarState {
    @DecimalMin("0.0")
    private double speed; // начальная скорость автомобиля в км/ч
    @DecimalMin("0.0")
    private double fuelConsumption; // расход топлива при начальной скорости в л/ч
    @DecimalMin("0.0")
    private double amountOfFuelLeft; // оставшееся количество топлива в литрах
    @DecimalMin("0.0")
    private double distance; // расстояние в километрах, которое нужно было изначально преодолеть
    @DecimalMin("0.0")
    private double distanceLeft; // расстояние в километрах, которое осталось преодолеть
}
