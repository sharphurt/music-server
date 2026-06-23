package ru.sharphurt.musicserver.library.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sharphurt.musicserver.library.dto.rest.AddToLibraryRequestDto;
import ru.sharphurt.musicserver.library.enitiy.AddToLibraryResult;
import ru.sharphurt.musicserver.library.service.LibraryManagementService;
import ru.sharphurt.musicserver.user.repository.UserRepository;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryManagementService libraryManagementService;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addToLibrary(@RequestBody AddToLibraryRequestDto dto) {
        // TODO: auth
        AddToLibraryResult result = libraryManagementService.addToLibrary(dto.getDownloadUuid(),
            userRepository.getReferenceById(1L));
        return switch (result) {
            case ALREADY_IN_LIBRARY -> ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            case MOVED -> ResponseEntity.ok().build();
            case REQUEUED -> ResponseEntity.accepted().build();
            case NOT_FOUND -> ResponseEntity.notFound().build();
        };
    }
}
