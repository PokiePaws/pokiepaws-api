# PokiePaws API

REST API dla systemu PokiePaws, obslugujacego gabinety weterynaryjne, wizyty, wlascicieli zwierzat, weterynarzy, magazyn i powiadomienia.

## Wymagania

- Java 21
- Docker Desktop
- Maven Wrapper z repozytorium (`mvnw` / `mvnw.cmd`)

## Konfiguracja

Skopiuj plik przykladowy:

```powershell
Copy-Item .env.example .env
```

Przed uruchomieniem ustaw w `.env` klucz aplikacji:

```text
JWT_SECRET=replace-with-64-character-minimum-random-secret-value
```

Najwazniejsze zmienne w `.env`:

- `APP_PORT=9090`
- `SERVER_PORT=9090`
- `POSTGRES_PORT=5432`
- `MAILPIT_DASHBOARD_PORT=8025`
- `JWT_SECRET` - klucz aplikacji uzywany do podpisywania tokenow JWT
- `BASE_URL=http://localhost:9090`
- `FRONTEND_URL=http://localhost:3000`

## Uruchamianie

Najprosciej uruchomic caly stack przez Docker Compose:

```powershell
docker compose up --build
```

API bedzie dostepne pod:

```text
http://localhost:9090
```

Health check:

```text
http://localhost:9090/actuator/health
```

## Porty

| Usluga | Port |
| --- | ---: |
| API | `9090` |
| PostgreSQL | `5432` |
| Mailpit SMTP | `1025` |
| Mailpit UI | `8025` |
| Frontend | `3000` |

## Dokumentacja API

Dokumentacja API jest dostepna pod:

```text
https://docs.pokiepaws.pl
```

Lokalnie:

```text
http://localhost:9090/swagger-ui.html
http://localhost:9090/api-docs
http://localhost:9090/redoc.html
```

## Testy

Wszystkie testy:

```powershell
.\mvnw.cmd test
```

Wybrana klasa testowa:

```powershell
.\mvnw.cmd "-Dtest=AuthServiceTest" test
```

Testy integracyjne z Testcontainers:

```powershell
.\mvnw.cmd "-Dtest=AuthControllerInitTests" test
```

Testy integracyjne uruchamiaja kontenery PostgreSQL i Mailpit, wiec Docker Desktop musi byc wlaczony.

## Jakosc Kodu

```powershell
.\mvnw.cmd spotless:check
.\mvnw.cmd spotless:apply
.\mvnw.cmd verify
```