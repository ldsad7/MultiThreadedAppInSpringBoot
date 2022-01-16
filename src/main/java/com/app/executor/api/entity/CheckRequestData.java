package com.app.executor.api.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
public class CheckRequestData {
    @DecimalMin("0.0")
    private Double speed; // начальная скорость автомобиля в км/ч
    @DecimalMin("0.0")
    private Double fuelConsumption; // расход топлива при начальной скорости
    @DecimalMin("0.0")
    private Double amountOfFuel; // количество топлива в литрах
    @DecimalMin("0.0")
    private Double distance; // расстояние в километрах, которое необходимо преодолеть
}
