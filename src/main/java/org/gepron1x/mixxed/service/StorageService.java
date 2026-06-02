package org.gepron1x.mixxed.service;


import lombok.SneakyThrows;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

@Service
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;

    public StorageService(S3Client s3Client,
                          S3Presigner s3Presigner,
                          String minioBucket) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucket = minioBucket;
    }

    public String uploadAudio(MultipartFile file, String key) {
        try {
            ensureBucket();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType() != null ? file.getContentType() : "audio/mpeg")
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload audio: " + e.getMessage(), e);
        }
    }

    public String uploadImage(MultipartFile file, String key) {
        try {
            ensureBucket();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType() != null ? file.getContentType() : "image/jpeg")
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            return key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public String getPresignedUrl(String key) {
        try {
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofHours(1))
                            .getObjectRequest(GetObjectRequest.builder()
                                    .bucket(bucket)
                                    .key(key)
                                    .build())
                            .build()
            );
            return presigned.url().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<ResponseEntity<InputStreamResource>> getFile(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        ResponseInputStream<GetObjectResponse> s3Object;
        try {
             s3Object = s3Client.getObject(getRequest);
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        }

        String contentType = s3Object.response().contentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return Optional.of(ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(key.substring(key.lastIndexOf('/') + 1))
                                .build().toString())
                .body(new InputStreamResource(s3Object)));

    }

    public ResponseEntity<StreamingResponseBody> streamAudio(String key, String rangeHeader) {
        try {
            GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key);

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                requestBuilder.range(rangeHeader);
            }

            GetObjectRequest request = requestBuilder.build();
            var response = s3Client.getObject(request);
            var responseDetails = response.response();

            long contentLength = responseDetails.contentLength() != null ? responseDetails.contentLength() : 0;
            String contentType = responseDetails.contentType() != null ? responseDetails.contentType() : "audio/mpeg";
            String contentRange = responseDetails.contentRange();

            StreamingResponseBody body = outputStream -> {
                try (InputStream in = response) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    // client disconnected
                }
            };

            HttpStatus status = (rangeHeader != null) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            if (contentLength > 0) headers.setContentLength(contentLength);
            if (contentRange != null) headers.set(HttpHeaders.CONTENT_RANGE, contentRange);

            return new ResponseEntity<>(body, headers, status);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private void ensureBucket() {
        try {
            s3Client.headBucket(b -> b.bucket(bucket));
        } catch (Exception e) {
            try {
                s3Client.createBucket(b -> b.bucket(bucket));
            } catch (Exception ex) {
                // bucket may already exist
            }
        }
    }
}
