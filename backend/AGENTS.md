# AGENTS.md

Plik instruktażowy dla agentów AI pracujących z projektem **QuickBite** – aplikacją generującą przepisy kulinarne na podstawie składników podanych przez użytkownika.

---

## Stack technologiczny

### Backend
- **Java 21**
- **Spring Boot 4.0.5**
- **Maven** – zarządzanie zależnościami
- **Spring AI 2.0.0-M4** – integracja z OpenAI API (`spring-ai-starter-model-openai`)

---

## Architektura aplikacji

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
- Backend buduje prompt i wywołuje OpenAI API przez `ChatClient` (Spring AI).
- OpenAI API zwraca 3 propozycje przepisów jako czysty tekst.
- Backend zwraca tekst bez parsowania strukturalnego JSON.
- Frontend wyświetla odpowiedź jako treść tekstową.

---

## Struktura projektu

```
backend/
└── src/main/java/com/quickbite/
    ├── QuickBiteApplication.java
    ├── config/
    │   └── WebConfig.java               # Konfiguracja CORS
    ├── controller/
    │   ├── RecipeController.java         # Endpoint /api/recipes/suggest i /api/health
    │   └── GlobalExceptionHandler.java  # Globalny handler błędów (@RestControllerAdvice)
    ├── service/
    │   └── OpenAiService.java           # Logika promptu i pobrania odpowiedzi tekstowej
    └── model/
        └── RecipeRequest.java           # Record: { List<String> ingredients }
```

---

## Decyzje architektoniczne

| Warstwa | Decyzja |
|---|---|
| Komunikacja z OpenAI | `ChatClient` (fluent API, Spring AI) |
| Format odpowiedzi AI | Czysty tekst (bez JSON) |
| Modele danych | `record` (Java 21) |
| Walidacja requestu | Ręcznie w kontrolerze (`if/throw`) |
| Obsługa błędów | `@RestControllerAdvice` – centralne miejsce |
| Konfiguracja CORS | `WebMvcConfigurer` – centralna konfiguracja |

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

```text
Przepis 1: Omlet z pomidorami i serem
Czas: 15 min
Trudnosc: latwy
Skladniki: 3 jajka, 1 pomidor, 50 g sera, sol, pieprz
Kroki:
1. Rozbij jajka i roztrzep.
2. Pokroj pomidora, zetrzyj ser.
3. Rozgrzej patelnie i usmaz omlet.

Przepis 2: ...
Przepis 3: ...
```

### Rekordy Java

```java
// RecipeRequest.java
public record RecipeRequest(List<String> ingredients) {}
```

---

## Endpointy API

| Metoda | Endpoint | Opis |
|--------|---|---|
| POST | `/api/recipes/suggest` | Przyjmuje składniki, zwraca tekst z 3 propozycjami przepisów |
| GET | `/api/health` | Health check backendu |

### Szczegóły endpointu `/api/recipes/suggest`

- **Request body**: `{ "ingredients": ["string"] }`
- **Response**: `text/plain` – opis 3 przepisow w czystym tekscie
- **Walidacja**: kontroler sprawdza ręcznie czy `ingredients` nie jest `null` ani pusta lista
- **Kody odpowiedzi**:
  - `200 OK` – przepisy wygenerowane poprawnie
  - `400 Bad Request` – brak składników lub pusta lista
  - `502 Bad Gateway` – błąd odpowiedzi z OpenAI

---

## Obsługa błędów

Globalny handler w `GlobalExceptionHandler.java` (`@RestControllerAdvice`):

| Wyjątek | HTTP Status | Kiedy |
|---|---|---|
| `IllegalArgumentException` | `400 Bad Request` | Pusta lista składników |
| `RuntimeException` | `502 Bad Gateway` | Błąd odpowiedzi OpenAI |

Format odpowiedzi błędu:
```json
{ "error": "treść komunikatu" }
```

---

## Konfiguracja CORS

Konfiguracja w `WebConfig.java` (implementuje `WebMvcConfigurer`):
- Dozwolone origins: wartość zmiennej środowiskowej `FRONTEND_ORIGIN`
- Dozwolone ścieżki: `/api/**`
- Dozwolone metody: `GET`, `POST`

---

## Zmienne środowiskowe

### Backend (`application.properties`)

```properties
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=${OPENAI_MODEL:gpt-4o}
server.port=${SERVER_PORT:8080}
app.frontend-origin=${FRONTEND_ORIGIN:http://localhost:4200}
```

| Zmienna | Opis | Domyślna wartość |
|---|---|---|
| `OPENAI_API_KEY` | Klucz API do OpenAI | brak – wymagana |
| `OPENAI_MODEL` | Model OpenAI | `gpt-4o` |
| `SERVER_PORT` | Port backendu | `8080` |
| `FRONTEND_ORIGIN` | Dozwolony origin CORS | `http://localhost:4200` |

> ⚠️ Nigdy nie commituj `OPENAI_API_KEY` do repozytorium. Użyj zmiennych środowiskowych systemu lub pliku `.env`.

---

## Uruchomienie backendu

### Wymagania wstępne
- Java 21+
- Klucz API OpenAI (https://platform.openai.com/api-keys)

### Uruchomienie

```bash
cd backend
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
# Backend dostępny na http://localhost:8080
```

---

## Instrukcje dla agenta

### Co agent może robić samodzielnie
- Generować i modyfikować kod w obrębie istniejących plików i modułów.
- Dodawać nowe metody do istniejących serwisów i kontrolerów.
- Modyfikować prompt wysyłany do OpenAI (bez zmiany kontraktu API).
- Aktualizować zależności Maven, jeśli nie zmienia to kontraktu API.

### Co agent musi skonsultować z użytkownikiem
- Zmianę kontraktu API (kształtu JSON requestu lub response) – wymaga aktualizacji obu warstw jednocześnie.
- Zmianę modelu OpenAI lub parametrów wywołania API.
- Dodanie nowych endpointów lub usunięcie istniejących.
- Zmiany w konfiguracji CORS lub zmiennych środowiskowych.
- Wprowadzenie nowych zależności Maven zmieniających sposób działania aplikacji.

### Zasady ogólne
- Po każdej zmianie kontraktu API zaktualizuj sekcje **Modele danych** i **Endpointy API** w tym pliku.
- Zawsze przestrzegaj struktury katalogów opisanej w sekcji **Struktura projektu**.
- Backend musi zwracać dokładnie 3 przepisy – prompt do OpenAI powinien to wymuszać.
- Prompt ma wymuszać odpowiedz w czystym tekscie (bez JSON i bez markdown).
- Nie przechowuj klucza API w kodzie źródłowym.
- Walidacja requestu odbywa się ręcznie w kontrolerze – nie używaj Bean Validation (`@Valid`, `@NotEmpty`).
- Modele danych to rekordy Java (`record`) – nie używaj klas z getterami/setterami ani Lombok.
