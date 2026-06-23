package ru.sharphurt.musicserver.library.controller;

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

    @PostMapping("/downloadcomplete")
    public ResponseEntity<Void> handleSlskdEvent(@RequestBody SlskDownloadCompleteDto payload) {

        if ("DownloadFileComplete".equals(payload.getType())) {
            handler.onDownloadComplete(payload);
        }

        return ResponseEntity.ok().build();
    }

}
