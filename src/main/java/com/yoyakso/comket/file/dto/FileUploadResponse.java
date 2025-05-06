package com.yoyakso.comket.file.dto;

import com.yoyakso.comket.file.enums.FileCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileUploadResponse {
	private Long fileId;
	private String fileUrl;
	private String fileName;
	private String fileType;
	private FileCategory category;
	private Long fileSize;
}
