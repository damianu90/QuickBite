package com.duzieblo.quickbite.backend.controller;

import com.duzieblo.quickbite.backend.model.RecipeRequest;
import com.duzieblo.quickbite.backend.model.RecipeResponse;
import com.duzieblo.quickbite.backend.service.OpenAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final OpenAiService openAiService;

    public RecipeController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping("/recipes/suggest")
    public RecipeResponse suggestRecipes(@RequestBody RecipeRequest request) {
        if (request == null || request.ingredients() == null || request.ingredients().isEmpty()) {
            throw new IllegalArgumentException("Lista skladnikow nie moze byc pusta");
        }
        return openAiService.suggestRecipes(request.ingredients());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK");
    }
}
