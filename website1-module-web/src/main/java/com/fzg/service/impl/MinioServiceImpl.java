package com.fzg.service.impl;

import com.fzg.service.MinioService;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@Slf4j
public class MinioServiceImpl implements MinioService {

    /**
     * 通过文件直接上传
     * @param file
     * @param bucketName
     * @param minioClient
     * @return
     */
    @Override
    public String upload(MultipartFile file, String bucketName, MinioClient minioClient) {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueObjectName = UUID.randomUUID().toString()  + fileExtension;


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


            String signatureUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs
                            .builder()
                            .method(Method.GET)
                            .bucket(
                                    bucketName).object(uniqueObjectName).build()
            );

            return extractEffectiveUrl(signatureUrl);
        } catch (Exception e) {
            e.printStackTrace(); // 或使用日志记录工具
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 通过图片url上传
     * @param
     * @param bucketName
     * @param minioClient
     * @return
     */
    @Override
    public String uploadByUrl(String imageUrl, String bucketName, MinioClient minioClient) {
        try {
            URL url = new URL(imageUrl);
            String originalFilename = getFileNameFromUrl(imageUrl); // 获取文件名（如 "image.jpg"）
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueObjectName = UUID.randomUUID().toString()  + fileExtension;

            // 2. 使用 InputStream 直接上传（避免本地存储）
            try (InputStream inputStream = url.openStream()) {
                // 3. 上传到 MinIO
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(uniqueObjectName) // 存储的文件名
                                .stream(inputStream, -1, 10485760) // -1 表示自动计算大小，10MB 分块
                                //.contentType(getContentType(fileExtension)) // 设置 Content-Type（如 "image/jpeg"）
                                .contentType("image/jpeg")
                                .build()
                );
            }

            // 4. 返回 MinIO 访问 URL
            String signatureURL = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs
                            .builder()
                            .method(Method.GET)
                            .bucket(
                                    bucketName).object(uniqueObjectName).build()
            );

            return extractEffectiveUrl(signatureURL);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("上传失败: " + e.getMessage(), e);
        }
    }


    /**
     * 从 URL 中提取文件名（如 "https://example.com/image.jpg" → "image.jpg"）
     */
    public String getFileNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return url;
        }

        String fileNameWithQuery = url.substring(lastSlashIndex + 1);

        int queryIndex = fileNameWithQuery.indexOf('?');
        if (queryIndex != -1) {
            fileNameWithQuery = fileNameWithQuery.substring(0, queryIndex);
        }

        return fileNameWithQuery;
    }



    //文件删除
    public void removeFile(String objectName, String bucketName, MinioClient minioClient) {
        try {
            String fileName = getFileNameFromUrl(objectName);
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(fileName).build());
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage());
        }
    }


    /**
     * 根据文件扩展名获取 Content-Type
     */
    public String getContentType(String fileExtension) {
        fileExtension = fileExtension.toLowerCase(); // 确保扩展名为小写
        if (fileExtension.equals(".jpg") || fileExtension.equals(".jpeg")) {
            return "image/jpeg";
        } else if (fileExtension.equals(".png")) {
            return "image/png";
        } else if (fileExtension.equals(".gif")) {
            return "image/gif";
        }else if(fileExtension.equals(".img")) {
            return "image/img";
        }else {
            return "application/octet-stream"; // 默认二进制流
        }
    }

    /**
     * 截取有效URL，获得实际的url
     * @param url
     * @return
     */
    public String extractEffectiveUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }


        // 1. 定位最后一个 / 的位置
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return url;
        }

        // 2. 从最后一个 / 之后的部分查找扩展名
        String pathAfterSlash = url.substring(lastSlashIndex + 1);
        int dotIndex = pathAfterSlash.lastIndexOf('.');
        if (dotIndex == -1) {
            return url;
        }

        // 3. 查找扩展名后的第一个非字母数字字符
        int invalidCharIndex = -1;
        for (int i = dotIndex + 1; i < pathAfterSlash.length(); i++) {
            char c = pathAfterSlash.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                invalidCharIndex = i;
                break;
            }
        }

        // 4. 截取有效部分
        if (invalidCharIndex != -1) {
            url = url.substring(0, lastSlashIndex + 1 + dotIndex + invalidCharIndex);
        }

        int queryParamStart = url.indexOf('?');
        if (queryParamStart > 0) {
            url = url.substring(0, queryParamStart);
        }
        // 2. 处理 %20 等 URL 编码字符（可选，按需解码）
        int i = url.indexOf('%');
        if (i > 0) {
            url = url.substring(0, i);
        }

        // 3. 去除 # 后的片段标识符（罕见但可能存在于某些代理场景）
        int fragmentStart = url.indexOf('#');
        if (fragmentStart > 0) {
            url = url.substring(0, fragmentStart);
        }

        return url;
    }

}
