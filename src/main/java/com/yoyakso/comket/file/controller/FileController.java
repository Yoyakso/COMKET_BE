package com.yoyakso.comket.file.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yoyakso.comket.file.dto.FileDownloadResponse;
import com.yoyakso.comket.file.dto.FileUploadResponse;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.enums.FileCategory;
import com.yoyakso.comket.file.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {
	private final FileService fileService;

	@PostMapping("/upload/profile")
	public ResponseEntity<FileUploadResponse> upload(MultipartFile multipartFile, FileCategory fileCategory) {
		File file = fileService.uploadProfile(multipartFile, fileCategory);
		String fileUrl = fileService.getFileUrlByPath(file.getFilePath());

		return ResponseEntity.ok(FileUploadResponse.builder()
			.fileId(file.getId())
			.fileUrl(fileUrl)
			.fileName(file.getFileName())
			.fileType(file.getFileType())
			.fileSize(file.getFileSize())
			.build());
	}

	@PostMapping("/upload")
	public ResponseEntity<FileUploadResponse> upload(MultipartFile multipartFile, String filePath) {
		File file = fileService.upload(multipartFile, filePath, null);
		String fileUrl = fileService.getFileUrlByPath(file.getFilePath());

		return ResponseEntity.ok(FileUploadResponse.builder()
			.fileId(file.getId())
			.fileUrl(fileUrl)
			.fileName(file.getFileName())
			.fileType(file.getFileType())
			.fileSize(file.getFileSize())
			.build());
	}

	@GetMapping("/download")
	public ResponseEntity<FileDownloadResponse> download(Long fileId) {
		File file = fileService.getFileById(fileId);
		String fileUrl = fileService.getFileUrlByPath(file.getFilePath());

		return ResponseEntity.ok(FileDownloadResponse.builder()
			.fileId(file.getId())
			.fileUrl(fileUrl)
			.fileName(file.getFileName())
			.fileType(file.getFileType())
			.fileSize(file.getFileSize())
			.build()
		);
	}
}
