package ru.sharphurt.musicserver.library.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sharphurt.musicserver.library.service.WebhookHandler;
import ru.sharphurt.musicserver.soulseek.dto.rest.SlskDownloadCompleteDto;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookHandler handler;

    @PostMapping("/download/complete")
    public ResponseEntity<Void> handleSlskDownloadCompleteEvent(@RequestBody SlskDownloadCompleteDto payload) {
        log.info("Получено событие {}", payload);
        if ("DownloadFileComplete".equals(payload.getType())) {
            handler.onDownloadComplete(payload);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/download/failed")
    public ResponseEntity<Void> handleSlskDownloadFailed(@RequestBody Map<String, Object> payload) {

//        if ("DownloadFileComplete".equals(payload.getType())) {
//            handler.onDownloadComplete(payload);
//        }

        return ResponseEntity.ok().build();
    }

}
