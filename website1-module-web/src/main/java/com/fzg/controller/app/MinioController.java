package com.fzg.controller.app;

import com.fzg.service.MinioService;
import io.minio.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;


@Slf4j
@RestController
@RequestMapping("/minio")
@RequiredArgsConstructor
@Schema(name = "MinioController", description = "Minio文件管理")
public class MinioController {

    private final MinioClient minioClient;

    private final MinioService minioService;

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
        log.info("MinioController.uploadFile开始");

        return minioService.upload(file, bucketName, minioClient);

    }



    /**
     * 文件上传
     * @return 文件访问路径
     * @throws  Exception 异常
     */
    @PostMapping("/uploadByUrl")
//    @SaCheckRole("admin")
    public String uploadFileByUrl(@RequestParam String url){


        return minioService.uploadByUrl(url, bucketName, minioClient);


    }



    /**
     * 文件下载
     * @param objectName 对象名称
     * @return 文件输入流
     * @throws  Exception 异常
     */
    @GetMapping("/download/{objectName}")
//    @SaCheckRole("admin")
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
//    @SaCheckRole("admin")
    public void deleteFile(@PathVariable String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
    }


}