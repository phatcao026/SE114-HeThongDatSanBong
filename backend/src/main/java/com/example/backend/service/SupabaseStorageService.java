package com.example.backend.service;

import com.example.backend.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.bucket-name:images}")
    private String bucketName;

    private final RestClient restClient;

    public SupabaseStorageService() {
        this.restClient = RestClient.create();
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(400, "File is empty or invalid");
        }

        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(serviceRoleKey)) {
            throw new AppException(500, "Supabase Storage is not configured on this server");
        }

        String originalFilename = file.getOriginalFilename();
        String cleanOriginalName = originalFilename != null 
                ? originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_") 
                : "upload";
        String uniqueFilename = UUID.randomUUID() + "_" + cleanOriginalName;

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + uniqueFilename;
        String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uniqueFilename;

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException(400, "Failed to read file contents: " + e.getMessage());
        }

        try {
            MediaType mediaType = MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");

            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .contentType(mediaType)
                    .body(fileBytes)
                    .retrieve()
                    .toBodilessEntity();
            
            return publicUrl;
        } catch (Exception e) {
            throw new AppException(500, "Failed to upload file to Supabase Storage: " + e.getMessage());
        }
    }
}
