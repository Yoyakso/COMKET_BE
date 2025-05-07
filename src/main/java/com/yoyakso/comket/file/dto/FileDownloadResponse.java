package com.yoyakso.comket.file.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FileDownloadResponse {
	private Long fileId;
	private String fileUrl;
	private String fileName;
	private String fileType;
	private Long fileSize;
}
