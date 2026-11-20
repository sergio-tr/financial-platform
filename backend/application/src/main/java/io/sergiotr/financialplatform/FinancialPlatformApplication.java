package io.sergiotr.financialplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@Modulith
@SpringBootApplication
public class FinancialPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialPlatformApplication.class, args);
    }
}