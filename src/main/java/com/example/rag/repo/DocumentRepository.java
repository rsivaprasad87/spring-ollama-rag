package com.example.rag.repo;

import com.example.rag.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> { }
