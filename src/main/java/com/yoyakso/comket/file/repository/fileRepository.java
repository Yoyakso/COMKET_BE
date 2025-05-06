package com.yoyakso.comket.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.file.entity.File;

public interface fileRepository extends JpaRepository<File, Long> {

}
