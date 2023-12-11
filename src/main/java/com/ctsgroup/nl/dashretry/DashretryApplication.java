package com.ctsgroup.nl.dashretry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DashretryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashretryApplication.class, args);
    }

}
