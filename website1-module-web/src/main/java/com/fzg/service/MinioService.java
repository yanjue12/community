package com.fzg.service;

import io.minio.MinioClient;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    String upload(MultipartFile file, String bucketName, MinioClient minioClient);

    String uploadByUrl(String url, String bucketName, MinioClient minioClient);
}
