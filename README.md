# taxi

## Docker Compose

Требуется Docker Desktop. Из каталога `TAXI_microservices`:

```bash
docker compose up --build -d
```

Порты: 8081 user, 8082 trip, 8083 notification, 8084 vehicle, 8085 rating, 8086 favorites, 6379 Redis.

Остановка: `docker compose down`

## JWT

Общий секрет: **`JWT_SECRET`** (не короче 32 байт в UTF-8). Локально подойдёт значение по умолчанию из `application.yml`; в Compose задан одинаковый секрет для всех сервисов, которые проверяют JWT.

1. Регистрация: в JSON для `POST /passengers` и `POST /drivers` добавлено поле **`password`** (6–72 символа), в ответах не отдаётся.
2. Вход: `POST http://localhost:8081/auth/login` с телом  
   `{"email":"...","password":"...","role":"PASSENGER"}` или `"DRIVER"`.  
   В ответе: `accessToken`, `tokenType`, `userId`, `role`.
3. Защищённые вызовы: заголовок **`Authorization: Bearer <accessToken>`**.
4. Без токена (межсервисно): `exists`, списки доступных водителей, `PATCH /drivers/{id}/status`, `GET /trips/{id}`, `GET /vehicles/price-estimate`, `GET /vehicles/driver/{id}/today` без Bearer.
5. Удаление избранного: `DELETE /favorites/{id}?userRole=...&userId=...` плюс Bearer того же пользователя.
