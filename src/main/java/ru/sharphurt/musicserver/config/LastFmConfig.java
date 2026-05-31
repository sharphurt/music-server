package ru.sharphurt.musicserver.config;

import de.umass.lastfm.Caller;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LastFmConfig {

    @PostConstruct
    public void initializeLastFmClient() {
        Caller.getInstance().setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36");
        Caller.getInstance().setDebugMode(true);
    }
}