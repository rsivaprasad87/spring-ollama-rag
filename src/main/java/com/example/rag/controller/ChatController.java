package com.example.rag.controller;

import com.example.rag.client.OllamaClient;
import com.example.rag.service.RetrievalService;
import com.example.rag.service.RetrievalService.ScoreDoc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final OllamaClient ollama;
    private final RetrievalService retrieval;
    private final double threshold;

    public ChatController(OllamaClient ollama, RetrievalService retrieval,
                          @Value("${rag.similarity.threshold:0.75}") double threshold) {
        this.ollama = ollama; this.retrieval = retrieval; this.threshold = threshold;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String,String> body) throws Exception {
        String userMessage = body.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message required"));
        }

        double[] qEmb = ollama.embedText(userMessage);
        List<ScoreDoc> top = retrieval.findNearest(qEmb, 5);
        if (top.isEmpty() || top.get(0).score < threshold) {
            return ResponseEntity.ok(Map.of(
                "answer", "I'm sorry — I don't have information in the documents about that.",
                "source", "none",
                "topScore", top.isEmpty() ? 0.0 : top.get(0).score,
                    "treshold", threshold
            ));
        }

        StringBuilder sys = new StringBuilder();
        sys.append("You are an assistant that answers ONLY based on the provided documents. ");
        sys.append("If the question cannot be answered using the documents, reply: 'I don't know based on the documents.'\n\n");
        sys.append("Context:\n");
        for (ScoreDoc sd : top) {
            sys.append("-----\n");
            sys.append("Title: ").append(sd.doc.getTitle()).append("\n");
            String snippet = sd.doc.getText();
            if (snippet.length() > 800) snippet = snippet.substring(0, 800) + "...";
            sys.append(snippet).append("\n");
        }

        String answer = ollama.chat(sys.toString(), userMessage);
        return ResponseEntity.ok(Map.of("answer", answer, "topScore", top.get(0).score));
    }
}
