package com.exercises.hotelbooking;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public Module customizeObjectMapper() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new DateSerializer(false, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
        return module;
    }
}
