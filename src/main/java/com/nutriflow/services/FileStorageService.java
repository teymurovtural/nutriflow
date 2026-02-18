package com.nutriflow.services;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    String saveFile(MultipartFile file) throws IOException;
    void deleteFile(String filePath) throws IOException; // Added in case it's needed
}