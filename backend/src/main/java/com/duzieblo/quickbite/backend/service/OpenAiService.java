package com.duzieblo.quickbite.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenAiService {

    private final ChatClient chatClient;

    public OpenAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String suggestRecipes(List<String> ingredients) {
        String prompt = buildPrompt(ingredients);
        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            throw new RuntimeException("OpenAI zwrocilo pusta odpowiedz");
        }

        return response.trim();
    }

    private String buildPrompt(List<String> ingredients) {
        return "Jestes asystentem kulinarnym. Na podstawie podanych skladnikow wygeneruj dokladnie 3 propozycje przepisow. "
                + "Odpowiedz czystym tekstem po polsku, bez JSON i bez markdown. "
                + "Dla kazdego przepisu podaj: nazwe, czas przygotowania, poziom trudnosci, liste skladnikow i kroki. "
                + "Skladniki uzytkownika: " + String.join(", ", ingredients);
    }
}

