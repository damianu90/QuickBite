# QuickBite Backend - Szkielet

Backend aplikacji QuickBite zgodny z zalozeniami z `AGENTS.md`.

## Co zawiera szkielet

- endpoint `POST /api/recipes/suggest`
- endpoint `GET /api/health`
- reczny rekord modelu (`record`): `RecipeRequest`
- reczna walidacja requestu w kontrolerze
- globalna obsluga bledow przez `@RestControllerAdvice`
- konfiguracja CORS oparta o `FRONTEND_ORIGIN`
- integracja z OpenAI przez Spring AI (`ChatClient`) i odpowiedz tekstowa

## Wymagania

- Java 21+
- zmienna srodowiskowa `OPENAI_API_KEY`

## Uruchomienie

```powershell
$env:OPENAI_API_KEY="sk-..."
.\mvnw.cmd spring-boot:run
```

## Przykladowe wywolania

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/health"
```

```powershell
$body = @{ ingredients = @("jajka", "ser", "pomidory", "cebula") } | ConvertTo-Json
Invoke-WebRequest -Method Post -Uri "http://localhost:8080/api/recipes/suggest" -ContentType "application/json" -Body $body | Select-Object -ExpandProperty Content
```

