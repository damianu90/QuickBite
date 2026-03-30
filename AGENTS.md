# AGENTS.md

Plik instruktażowy dla agentów AI pracujących z projektem **QuickBite** – aplikacją generującą przepisy kulinarne na podstawie składników podanych przez użytkownika.

---

## Stack technologiczny

### Backend
- **Java 21**
- **Spring Boot 3.x**
- **Maven** – zarządzanie zależnościami
- **Spring AI 1.x** – integracja z OpenAI API

### Frontend
- **Angular 17+**
- **TypeScript**
- **Angular HttpClient** – komunikacja z backendem
- **Angular CLI** – budowanie i uruchamianie projektu

---

## Architektura aplikacji

Aplikacja składa się z dwóch niezależnych modułów:

```
[Użytkownik]
     │
     ▼
[Frontend – Angular :4200]
     │  POST /api/recipes/suggest
     ▼
[Backend – Spring Boot :8080]
     │  POST https://api.openai.com/v1/chat/completions
     ▼
[OpenAI API]
```

- Frontend komunikuje się z backendem przez REST API.
- Backend odpowiada za budowanie promptu i wywołanie OpenAI API.
- OpenAI API zwraca 3 propozycje przepisów w formacie JSON.
- Frontend wyświetla przepisy w postaci kart.

---

## Struktura projektu

```
quickbite/
├── backend/                        # Moduł Spring Boot
│   ├── src/main/java/com/quickbite/
│   │   ├── QuickBiteApplication.java
│   │   ├── controller/
│   │   │   └── RecipeController.java
│   │   ├── service/
│   │   │   └── OpenAiService.java
│   │   └── model/
│   │       ├── RecipeRequest.java
│   │       ├── RecipeResponse.java
│   │       └── Recipe.java
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
└── frontend/                       # Moduł Angular
    ├── src/app/
    │   ├── app.component.ts
    │   ├── app.component.html
    │   ├── app.component.scss
    │   ├── models/
    │   │   └── recipe.model.ts
    │   └── services/
    │       └── recipe.service.ts
    ├── angular.json
    └── package.json
```

---

## Modele danych

### Request – Frontend → Backend

```json
POST /api/recipes/suggest
Content-Type: application/json

{
  "ingredients": ["jajka", "ser", "pomidory", "cebula"]
}
```

### Response – Backend → Frontend

```json
{
  "recipes": [
    {
      "name": "Omlet z pomidorami i serem",
      "time": "15 min",
      "difficulty": "łatwy",
      "ingredients": [
        "3 jajka",
        "1 pomidor",
        "50g sera żółtego",
        "sól, pieprz"
      ],
      "steps": [
        "Rozbij jajka do miski i roztrzep widelcem.",
        "Pokrój pomidora w kostkę, zetrzyj ser.",
        "Rozgrzej patelnię, wlej jajka.",
        "Dodaj pomidory i ser, złóż omlet na pół."
      ]
    },
    { ... },
    { ... }
  ]
}
```

### Typescript model (frontend)

```typescript
export interface Recipe {
  name: string;
  time: string;
  difficulty: string;
  ingredients: string[];
  steps: string[];
}

export interface RecipeResponse {
  recipes: Recipe[];
}
```

---

## Endpointy API

| Metoda | Endpoint                  | Opis                                              |
|--------|---------------------------|---------------------------------------------------|
| POST   | `/api/recipes/suggest`    | Przyjmuje składniki, zwraca 3 propozycje przepisów |
| GET    | `/api/health`             | Health check backendu                             |

### Szczegóły endpointu `/api/recipes/suggest`

- **Request body**: `{ "ingredients": ["string"] }`
- **Response**: `{ "recipes": [Recipe] }` – zawsze dokładnie 3 przepisy
- **Kody odpowiedzi**:
  - `200 OK` – przepisy wygenerowane poprawnie
  - `400 Bad Request` – brak składników lub pusta lista
  - `502 Bad Gateway` – błąd komunikacji z OpenAI API

---

## Zmienne środowiskowe

### Backend (`application.properties` lub zmienna środowiskowa)

| Zmienna                  | Opis                              | Przykład                          |
|--------------------------|-----------------------------------|-----------------------------------|
| `OPENAI_API_KEY`         | Klucz API do OpenAI               | `sk-...`                          |
| `OPENAI_MODEL`           | Model OpenAI do użycia            | `gpt-4o`                          |
| `SERVER_PORT`            | Port backendu (domyślnie 8080)    | `8080`                            |
| `FRONTEND_ORIGIN`        | Dozwolony origin CORS             | `http://localhost:4200`           |

> ⚠️ Nigdy nie commituj `OPENAI_API_KEY` do repozytorium. Użyj `.env` lub zmiennych środowiskowych systemu.

### Frontend (`environment.ts`)

| Zmienna              | Opis                         | Przykład                       |
|----------------------|------------------------------|--------------------------------|
| `apiUrl`             | Bazowy URL backendu          | `http://localhost:8080`        |

---

## Uruchomienie aplikacji

### Wymagania wstępne
- Java 21+
- Node.js 20+
- Angular CLI (`npm install -g @angular/cli`)
- Klucz API OpenAI (https://platform.openai.com/api-keys)

### Backend

```bash
cd backend
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
# Backend dostępny na http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
ng serve
# Frontend dostępny na http://localhost:4200
```

---

## Instrukcje dla agenta

### Co agent może robić samodzielnie
- Generować i modyfikować kod w obrębie istniejących plików i modułów.
- Dodawać nowe metody do istniejących serwisów i kontrolerów.
- Modyfikować style i szablony HTML w Angular.
- Aktualizować zależności Maven/npm, jeśli nie zmienia to kontraktu API.
- Pisać i uruchamiać testy jednostkowe.

### Co agent musi skonsultować z użytkownikiem
- Zmianę kontraktu API (kształtu JSON requestu lub response) – wymaga aktualizacji obu warstw jednocześnie.
- Zmianę modelu OpenAI lub parametrów wywołania API.
- Dodanie nowych endpointów lub usunięcie istniejących.
- Zmiany w konfiguracji CORS lub zmiennych środowiskowych.

### Zasady ogólne
- Po każdej zmianie kontraktu API zaktualizuj sekcję **Modele danych** i **Endpointy API** w tym pliku.
- Zawsze przestrzegaj struktury katalogów opisanej w sekcji **Struktura projektu**.
- Backend musi zwracać dokładnie 3 przepisy – prompt do OpenAI powinien to wymuszać.
- Nie przechowuj klucza API w kodzie źródłowym.
