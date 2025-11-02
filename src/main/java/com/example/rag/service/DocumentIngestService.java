package com.example.rag.service;

import com.example.rag.client.OllamaClient;
import com.example.rag.model.DocumentEntity;
import com.example.rag.repo.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Service
public class DocumentIngestService {
    private final DocumentRepository repo;
    private final OllamaClient ollama;
    private final ObjectMapper mapper = new ObjectMapper();

    public DocumentIngestService(DocumentRepository repo, OllamaClient ollama) {
        this.repo = repo; this.ollama = ollama;
    }

    public void ingestTxtFile(Path path) throws IOException {
        String text = Files.readString(path);
        double[] emb = ollama.embedText(text);
        DocumentEntity d = new DocumentEntity();
        d.setText(text);
        d.setTitle(path.getFileName().toString());
        d.setSource(path.toString());
        d.setEmbeddingJson(mapper.writeValueAsString(emb));
        repo.save(d);
    }

    public void ingestPdfFile(Path path) throws IOException {
        try (PDDocument doc = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            double[] emb = ollama.embedText(text);
            DocumentEntity d = new DocumentEntity();
            d.setText(text);
            d.setTitle(path.getFileName().toString());
            d.setSource(path.toString());
            d.setEmbeddingJson(mapper.writeValueAsString(emb));
            repo.save(d);
        }
    }

    public void ingestAllInFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) return;
        try (Stream<Path> s = Files.list(folder)) {
            s.forEach(p -> {
                try {
                    String fname = p.getFileName().toString().toLowerCase();
                    if (fname.endsWith(".txt") || fname.endsWith(".md")) ingestTxtFile(p);
                    else if (fname.endsWith(".pdf")) ingestPdfFile(p);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
