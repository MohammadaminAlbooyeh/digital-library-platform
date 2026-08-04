package com.dlp.repository;

import com.dlp.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) = LOWER(:name)")
    List<Book> findByAuthorName(@Param("name") String name);

    @Query("SELECT b FROM Book b JOIN b.categories c WHERE LOWER(c.name) = LOWER(:category)")
    Page<Book> findByCategoryName(@Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors a LEFT JOIN b.categories c " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "   OR LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Book> search(@Param("q") String q, Pageable pageable);

    List<Book> findByPublisherId(Long publisherId);
}

