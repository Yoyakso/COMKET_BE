package com.yoyakso.comket.file.service;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.enums.FileCategory;
import com.yoyakso.comket.file.repository.fileRepository;

import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

	private final S3Client s3Client;
	private final fileRepository fileRepository;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${spring.cloud.aws.s3.file-url}")
	private String fileUrl;

	public File upload(MultipartFile multipartFile, String dirName, String fileName) {
		if (multipartFile.isEmpty()) {
			throw new CustomException("INVALID_FILE", "파일이 비어있습니다.");
		}
		fileName = Objects.requireNonNullElse(fileName, UUID.randomUUID().toString());
		String filePath = dirName + "/" + fileName;

		putS3(filePath, multipartFile);

		File file = File.builder()
			.fileName(fileName)
			.filePath(filePath)
			.fileType(multipartFile.getContentType())
			.fileSize(multipartFile.getSize())
			.build();

		//DB에 저장
		fileRepository.save(file); // DB에 저장

		return file; // 업로드된 파일 주소 리턴
	}

	private void putS3(String filePath, MultipartFile multipartFile) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(filePath)
			.contentType(multipartFile.getContentType())
			.contentLength(multipartFile.getSize())
			.build();
		try {
			s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));
		} catch (IOException e) {
			throw new CustomException("FILE_UPLOAD_FAIL", "파일 업로드 중 문제가 발생했습니다.");
		}
	}

	public File uploadProfile(MultipartFile multipartFile, FileCategory fileCategory, Long id) {
		File uploadFile = upload(multipartFile, fileCategory.getDirectoryName(), id.toString()); // s3에 업로드
		return fileRepository.save(uploadFile); // DB에 저장
	}

	public String getFileUrlByPath(String filePath) {
		return fileUrl + "/" + filePath;
	}

	public File getFileById(String fileId) {
		return fileRepository.findById(Long.valueOf(fileId))
			.orElseThrow(() -> new CustomException("FILE_NOT_FOUND", "파일을 찾을 수 없습니다."));
	}

}
