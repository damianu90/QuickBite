package com.duzieblo.quickbite.backend.model;

import java.util.List;

public record Recipe(
        String name,
        String time,
        String difficulty,
        List<String> ingredients,
        List<String> steps
) {
}

