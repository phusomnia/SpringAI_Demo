package com.example.springai_seminar.Mistral.repos;

import com.example.springai_seminar.entities.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends CrudRepository<Book, String> {
}
