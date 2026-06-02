package org.gepron1x.mixxed.form;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
public class MixUploadForm {
    private String title;
    private String description;
    private String genre;
    private MultipartFile audioFile;
    private MultipartFile coverFile;
    private List<TrackEntry> tracks = new ArrayList<>();

    @Data
    public static class TrackEntry {
        private String startTime; // mm:ss
        private String artist;
        private String title;
    }
}
