package com.fzg.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Autowired
    private MinioProperties minioProperties;

    @Value("${minio.temp-bucket-name}")
    private String tempBucket;

    @Value("${minio.bucket-name}")
    private String newsBucket;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
    public CommandLineRunner initBuckets(MinioClient minioClient) {
        return args -> {
            // 确保桶存在
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(tempBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(tempBucket).build());
            }
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(newsBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(newsBucket).build());
            }
        };
    }


}