package com.ssafynity.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일이 없습니다"));
        }
        String original = file.getOriginalFilename();
        if (original == null || (!original.endsWith(".jpg") && !original.endsWith(".jpeg")
                && !original.endsWith(".png") && !original.endsWith(".gif") && !original.endsWith(".webp"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "허용되지 않는 파일 형식입니다"));
        }
        String ext = original.substring(original.lastIndexOf('.'));
        String filename = UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
    }
}
