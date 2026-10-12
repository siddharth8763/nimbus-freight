package com.nimbusfreight;

import com.nimbusfreight.config.AlertPolicy;
import com.nimbusfreight.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({AlertPolicy.class, OpenAiProperties.class})
public class NimbusFreightApplication {

    public static void main(String[] args) {
        SpringApplication.run(NimbusFreightApplication.class, args);
    }
}
