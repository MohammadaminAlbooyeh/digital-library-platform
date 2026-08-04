package com.dlp.repository;

import com.dlp.model.entity.Audiobook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudiobookRepository extends JpaRepository<Audiobook, Long> {

    List<Audiobook> findByBookId(Long bookId);
}

