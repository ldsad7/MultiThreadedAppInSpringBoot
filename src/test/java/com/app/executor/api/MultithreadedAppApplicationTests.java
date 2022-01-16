package com.app.executor.api;

import com.app.executor.api.controller.CarController;
import com.app.executor.api.entity.CheckRequestData;
import com.app.executor.api.entity.CheckResponseData;
import com.app.executor.api.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class MultithreadedAppApplicationTests {
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new CarController(new CarService())).build();
    }

    @Test
    public void givenCheckRequestData_whenMockMVC_thenVerifyCheckResponseData() throws Exception {
        Map<CheckRequestData, CheckResponseData> tests = new HashMap<>();
        Double[] speeds = new Double[]{0.0, 13.43, 60.92, 60.0};
        Double[] amountsOfFuel = new Double[]{0.0, 100.2, 100.2, 100.0};
        Double[] distances = new Double[]{0.0, 15.0, 15.0, 15.0};
        Double[] fuelConsumptions = new Double[]{0.0, 204.23, 200.0, 400.0};

        Boolean[] successful = new Boolean[]{true, false, true, true};
        Double[] amountsOfFuelLeft = new Double[]{0.0, 0.0, 50.95508864084051, 0.0};

        for (int i = 0; i < speeds.length; i++) {
            tests.put(
                    new CheckRequestData()
                            .setSpeed(speeds[i])
                            .setAmountOfFuel(amountsOfFuel[i])
                            .setDistance(distances[i])
                            .setFuelConsumption(fuelConsumptions[i]),
                    new CheckResponseData()
                            .setSuccessful(successful[i])
                            .setAmountOfFuelLeft(amountsOfFuelLeft[i])
                            .setMessage(null)
            );
        }

        for (Map.Entry<CheckRequestData, CheckResponseData> test : tests.entrySet()) {
            CheckRequestData testData = test.getKey();
            CheckResponseData result = test.getValue();

            this.mockMvc
                    .perform(MockMvcRequestBuilders.get(
                            String.format("/check?speed=%f&fuelConsumption=%f&amountOfFuel=%f&distance=%f",
                                    testData.getSpeed(), testData.getFuelConsumption(),
                                    testData.getAmountOfFuel(), testData.getDistance()
                            )))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.successful").value(result.getSuccessful()))
                    .andExpect(jsonPath("$.amountOfFuelLeft").value(result.getAmountOfFuelLeft()))
            ;
        }
    }
}
