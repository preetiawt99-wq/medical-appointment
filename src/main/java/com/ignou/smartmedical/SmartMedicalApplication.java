package com.ignou.smartmedical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartMedicalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMedicalApplication.class, args);
    }
}