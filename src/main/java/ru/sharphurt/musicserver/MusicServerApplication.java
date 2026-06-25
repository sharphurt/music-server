package ru.sharphurt.musicserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "ru.sharphurt.musicserver")
@EnableCaching
@EnableScheduling
public class MusicServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicServerApplication.class, args);
    }
}
