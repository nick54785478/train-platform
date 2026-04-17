package com.example.demo.infra.blob;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MinioService {

	private MinioClient client;

	@Value("${minio.bucketName}")
	private String bucketName;
	
	
	@Value("${minio.endpoint}")
	private String minioEndpoint;

	public MinioService(@Value("${minio.endpoint}") String endpoint, @Value("${minio.accessKey}") String accessKey,
			@Value("${minio.secretKey}") String secretKey) {
		log.info("MinIO 連線資訊:");
		log.info("Endpoint: {}", endpoint);
		log.info("AccessKey: {}", accessKey);
		log.info("SecretKey: {}", secretKey);
		this.client = MinioClient.builder().endpoint(endpoint) // ⚠️ 如果 endpoint 為 null，這裡會拋出異常
				.credentials(accessKey, secretKey).build();
	}

	// 檢查 bucket 是否存在，若不存在則創建
	public void createBucket() throws Exception {
		boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		if (!found) {
			client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
		}
	}

	/**
	 * 上傳檔案
	 * 
	 * @param file
	 */
	public String uploadFile(MultipartFile file) throws Exception {
		createBucket(); // 確保 bucket 存在
		String objectName = file.getOriginalFilename();
		client.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName)
				.stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());
		return objectName;
	}

	/**
	 * 上傳檔案至 MinIO
	 *
	 * @param file     上傳的檔案
	 * @param fileName 指定檔案名稱
	 * @param filePath 指定存放路徑（資料夾路徑）
	 * @return 檔案 URL
	 */
	public String uploadFile(MultipartFile file, String fileName, String filePath) throws Exception {
		this.createBucket(); // 確保 bucket 存在

		// 確保 filePath 以 '/' 結尾，避免路徑錯誤
		if (!filePath.endsWith("/")) {
			filePath += "/";
		}

		// 組合完整 objectName，例如 "uploads/images/myfile.jpg"
		String objectName = filePath + fileName;

		client.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName)
				.stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build());

		// 組合檔案的存取 URL
		return String.format("http://%s/%s/%s", minioEndpoint, bucketName, objectName);
	}

	/**
	 * 下載檔案
	 * 
	 * @param objectName Object 名稱
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws XmlParserException 
	 * @throws ServerException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidResponseException 
	 * @throws InternalException 
	 * @throws InsufficientDataException 
	 * @throws ErrorResponseException 
	 * @throws InvalidKeyException 
	 */
	public InputStream downloadFile(String objectName) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
		return client.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
	}

	/**
	 * 刪除檔案
	 * 
	 * @param objectName Object 名稱
	 */
	public void deleteFile(String objectName) throws Exception {
		client.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
	}
}
