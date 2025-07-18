package com.fzg.controller.app;

import io.minio.*;
import io.minio.http.Method;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;


@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
@Schema(name = "MinioController", description = "Minio文件管理")
public class MinioController {

    private final MinioClient minioClient;


    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 文件上传
     * @param file 上传的文件
     * @return 文件访问路径
     * @throws  Exception 异常
     */
    @PostMapping("/upload")
    public String uploadFile(@RequestParam MultipartFile file){

        //唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueObjectName = UUID.randomUUID().toString()  + fileExtension;


        // 确保桶存在
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 使用 try-with-resources 自动管理 InputStream 资源 直接input 网站，不用分片异步
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(uniqueObjectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build());
            }

            // 返回文件的访问 URL
            //String.format("%s/%s%s",minioClient.getEndpoint, bucketName, objectName);
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs
                            .builder()
                            .method(Method.GET)
                            .bucket(
                    bucketName).object(uniqueObjectName).build()
            );
        } catch (Exception e) {
            // 处理异常，例如记录日志或返回错误信息
            e.printStackTrace(); // 或使用日志记录工具
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

       /* if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());

        inputStream.close();

        return minioClient.getObjectUrl(bucketName, objectName);*/
    }

    /**
     * 文件下载
     * @param objectName 对象名称
     * @return 文件输入流
     * @throws  Exception 异常
     */
    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectName) throws Exception {

        InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());

        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // 或其他适当的类型
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                .body(inputStreamResource);
    }

    /**
     * 文件删除
     * @param objectName 对象名称
     * @throws  Exception 异常
     */
    @DeleteMapping("/delete/{objectName}")
    public void deleteFile(@PathVariable String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }


}