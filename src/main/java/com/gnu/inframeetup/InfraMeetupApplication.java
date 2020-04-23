package com.gnu.inframeetup;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InfraMeetupApplication {

    public static void main(String[] args) {
        SpringApplication.run(InfraMeetupApplication.class, args);
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricConfig(){
        return registry -> registry.config()
                .commonTags("zone", "infra-1");
    }

}
