package com.nimbusfreight;

import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.config.LlmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AlertPolicy.class, LlmProperties.class})
public class NimbusFreightApplication {

    public static void main(String[] args) {
        SpringApplication.run(NimbusFreightApplication.class, args);
    }
}
