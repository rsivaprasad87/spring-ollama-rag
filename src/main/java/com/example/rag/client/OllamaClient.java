package com.example.rag.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OllamaClient {
    private final String base;
    private final String embedModel;
    private final String chatModel;
    private final RestTemplate rest;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaClient(@Value("${ollama.api.base}") String base,
                        @Value("${ollama.embed.model}") String embedModel,
                        @Value("${ollama.chat.model}") String chatModel) {
        this.base = base;
        this.embedModel = embedModel;
        this.chatModel = chatModel;
        this.rest = new RestTemplate();
    }

    @SuppressWarnings("unchecked")
    public double[] embedText(String text) {
        String url = base + "/api/embed";
        Map<String,Object> body = Map.of("model", embedModel, "input", text);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, h);
        ResponseEntity<Map> resp = rest.postForEntity(url, req, Map.class);
        Map map = resp.getBody();
        if (map == null) throw new RuntimeException("Empty embed response from Ollama");
        Object emb = map.get("embedding");
        if (emb == null) emb = map.get("embeddings");
        if (emb instanceof List) {
            List list = (List) emb;
            Object first = list.get(0);
            if (first instanceof List) {
                List nums = (List) first;
                return nums.stream().mapToDouble(o -> ((Number)o).doubleValue()).toArray();
            } else {
                List nums = list;
                return nums.stream().mapToDouble(o -> ((Number)o).doubleValue()).toArray();
            }
        }
        throw new RuntimeException("Unexpected embed response: " + map);
    }

    @SuppressWarnings("unchecked")
    public String chat(String systemPrompt, String userPrompt) {
        String url = base + "/api/chat";
        Map<String,Object> body = Map.of(
            "model", chatModel,
            "messages", List.of(
                Map.of("role","system","content", systemPrompt),
                Map.of("role","user","content", userPrompt)
            ),
            "stream", false
        );
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> req = new HttpEntity<>(body, h);
        ResponseEntity<Map> resp = rest.postForEntity(url, req, Map.class);
        Map map = resp.getBody();
        if (map == null) throw new RuntimeException("Empty chat response from Ollama");
        if (map.containsKey("choices")) {
            List choices = (List) map.get("choices");
            if (!choices.isEmpty()) {
                Object c0 = choices.get(0);
                if (c0 instanceof Map) {
                    Map c0m = (Map) c0;
                    if (c0m.containsKey("message")) {
                        Object m = c0m.get("message");
                        if (m instanceof Map) {
                            Object content = ((Map)m).get("content");
                            return content == null ? "" : content.toString();
                        }
                    }
                }
            }
        }
        if (map.containsKey("output")) {
            Object out = map.get("output");
            if (out instanceof List) {
                StringBuilder sb = new StringBuilder();
                for (Object o : (List) out) sb.append(o.toString()).append("\n");
                return sb.toString().trim();
            } else return out.toString();
        }
        return map.toString();
    }
}
