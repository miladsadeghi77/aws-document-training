package com.miladsadeghi.awsdocument.infrastracture.persistance.repository;

import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {


}
