package com.duzieblo.quickbite.backend.service;

import com.duzieblo.quickbite.backend.model.RecipeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OpenAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAiService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public RecipeResponse suggestRecipes(List<String> ingredients) {
        String response = chatClient.prompt(buildPrompt(ingredients)).call().content();

        if (response == null || response.isBlank()) {
            throw new RuntimeException("OpenAI zwrocilo pusta odpowiedz");
        }

        return parseResponse(response);
    }

    private String buildPrompt(List<String> ingredients) {
        return """
                Jestes asystentem kulinarnym. Na podstawie skladnikow wygeneruj dokladnie 3 przepisy. \
                Odpowiedz wylacznie czystym JSON bez markdown i bez dodatkowego tekstu. \
                Uzyj dokladnie tego formatu: \
                {"recipes":[{"name":"","time":"","difficulty":"","ingredients":[""],"steps":[""]}]} \
                Skladniki uzytkownika: \
                """ + String.join(", ", ingredients);
    }

    private RecipeResponse parseResponse(String response) {
        try {
            RecipeResponse result = objectMapper.readValue(response, RecipeResponse.class);
            if (result.recipes() == null || result.recipes().size() != 3) {
                throw new RuntimeException("OpenAI nie zwrocilo dokladnie 3 przepisow");
            }
            return result;
        } catch (IOException ex) {
            throw new RuntimeException("Nie udalo sie sparsowac odpowiedzi OpenAI", ex);
        }
    }
}
