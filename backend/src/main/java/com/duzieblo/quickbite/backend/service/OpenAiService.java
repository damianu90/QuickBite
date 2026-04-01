package com.duzieblo.quickbite.backend.service;

import com.duzieblo.quickbite.backend.model.Recipe;
import com.duzieblo.quickbite.backend.model.RecipeResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        String prompt = buildPrompt(ingredients);
        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            throw new RuntimeException("OpenAI zwrocilo pusta odpowiedz");
        }

        return parseRecipes(response);
    }

    private String buildPrompt(List<String> ingredients) {
        return "Jestes asystentem kulinarnym. Na podstawie podanych skladnikow wygeneruj DOKLADNIE 3 przepisy. "
                + "Odpowiedz w czystym JSON bez zadnego dodatkowego tekstu. "
                + "Uzyj dokladnie tego formatu: "
                + "{\"recipes\":[{\"name\":\"...\",\"time\":\"...\",\"difficulty\":\"...\",\"ingredients\":[\"...\"],\"steps\":[\"...\"]}]} "
                + "Kazdy przepis ma miec pola: name, time, difficulty, ingredients, steps. "
                + "Skladniki uzytkownika: " + String.join(", ", ingredients);
    }

    private RecipeResponse parseRecipes(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode recipesNode = root.path("recipes");

            if (!recipesNode.isArray() || recipesNode.size() != 3) {
                throw new RuntimeException("OpenAI zwrocilo niepoprawna liczbe przepisow");
            }

            List<Recipe> recipes = new ArrayList<>();
            for (JsonNode recipeNode : recipesNode) {
                recipes.add(objectMapper.treeToValue(recipeNode, Recipe.class));
            }
            return new RecipeResponse(recipes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Nie udalo sie sparsowac odpowiedzi OpenAI", e);
        }
    }
}

