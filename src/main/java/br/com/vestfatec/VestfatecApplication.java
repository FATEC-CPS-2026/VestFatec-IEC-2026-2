package br.com.vestfatec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VestfatecApplication {

    public static void main(String[] args) {
        SpringApplication.run(VestfatecApplication.class, args);
    }
}
