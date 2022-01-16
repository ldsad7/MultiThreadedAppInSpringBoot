package com.app.executor.api.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.DecimalMin;

@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class CheckResponseData {
    private Boolean successful; // можно ли с заданным набором данных доехать до конца пути
    @DecimalMin("0.0")
    private Double amountOfFuelLeft; // сколько топлива будет в остатке
    private String message; // сообщение в случае ошибочных переданных параметров
}
