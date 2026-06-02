package org.gepron1x.mixxed.controller;


import lombok.AllArgsConstructor;
import org.gepron1x.mixxed.service.StorageService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public final class S3Controller {

    private final StorageService storageService;

    @GetMapping("/s3/{*key}")
    public ResponseEntity<InputStreamResource> getFile(@PathVariable String key) {
        return storageService.getFile(key);
    }
}
