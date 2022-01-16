package com.app.executor.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MultithreadedAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultithreadedAppApplication.class, args);
    }

}
