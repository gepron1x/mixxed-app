package org.gepron1x.mixxed.controller;

import lombok.RequiredArgsConstructor;
import org.gepron1x.mixxed.entity.Mix;
import org.gepron1x.mixxed.repository.MixRepository;
import org.gepron1x.mixxed.service.StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Controller
@RequiredArgsConstructor
public class StreamController {

    private final MixRepository mixRepository;
    private final StorageService storageService;

    @GetMapping("/api/mixes/{slug}/stream")
    public ResponseEntity<StreamingResponseBody> stream(
        @PathVariable String slug,
        @RequestHeader(value = "Range", required = false) String rangeHeader
    ) {
        Mix mix = mixRepository.findBySlug(slug).orElse(null);
        if (mix == null || mix.getAudioUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        return storageService.streamAudio(mix.getAudioUrl(), rangeHeader);
    }
}