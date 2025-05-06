package com.yoyakso.comket.file.entity;

import org.hibernate.annotations.CreationTimestamp;

import com.yoyakso.comket.file.enums.FileCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String memberId;

	@Column(nullable = false, length = 100)
	private String fileName;

	@Column(nullable = false, length = 100)
	private String filePath;

	@Column(nullable = false, length = 100)
	private String fileType;

	@Column(nullable = false, length = 100)
	private Long fileSize;

	@Column(nullable = true, length = 100)
	private FileCategory fileCategory;

	@CreationTimestamp
	private String uploadDate;

}
