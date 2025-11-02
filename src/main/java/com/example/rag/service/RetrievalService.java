package com.example.rag.service;

import com.example.rag.model.DocumentEntity;
import com.example.rag.repo.DocumentRepository;
import com.example.rag.util.VectorUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RetrievalService {
    private final DocumentRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public RetrievalService(DocumentRepository repo) { this.repo = repo; }

    public static class ScoreDoc {
        public final DocumentEntity doc;
        public final double score;
        public ScoreDoc(DocumentEntity doc, double score) { this.doc = doc; this.score = score; }
    }

    public List<ScoreDoc> findNearest(double[] queryVec, int k) {
        List<DocumentEntity> all = repo.findAll();
        List<ScoreDoc> scored = new ArrayList<>();
        for (DocumentEntity d : all) {
            try {
                double[] vec = mapper.readValue(d.getEmbeddingJson(), double[].class);
                double sim = VectorUtils.cosineSimilarity(queryVec, vec);
                scored.add(new ScoreDoc(d, sim));
            } catch (Exception e) {
                // skip
            }
        }
        scored.sort((a,b) -> Double.compare(b.score, a.score));
        if (scored.size() > k) return scored.subList(0, k);
        return scored;
    }
}
