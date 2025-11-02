package com.example.rag.startup;

import com.example.rag.service.DocumentIngestService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class StartupIngestRunner implements CommandLineRunner {
    private final DocumentIngestService ingestService;

    public StartupIngestRunner(DocumentIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @Override
    public void run(String... args) throws Exception {
        // ingest docs from /docs relative to the jar working directory
        Path p = Path.of("docs");
        ingestService.ingestAllInFolder(p);
        System.out.println("Document ingestion completed (if docs were present).");
    }
}
