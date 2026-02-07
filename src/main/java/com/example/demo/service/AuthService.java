package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;

    // Константы Green API (в реальном проекте вынесите их в application.properties)
    private final String INSTANCE_ID = "7103507490";
    private final String API_TOKEN = "b3618a1229d940c3a5d101bf300a7133a20d53264194453086";
    private final String GREEN_API_BASE_URL = "https://api.green-api.com";

    /**
     * Генерация OTP и запуск процесса отправки
     */
    public void sendOtp(String phone) {
        // Генерируем 4-значный код
        String code = String.valueOf(1000 + new Random().nextInt(9000));

        // Сохраняем в Redis на 2 минуты
        redisTemplate.opsForValue()
                .set("otp:" + phone, code, 2, TimeUnit.MINUTES);

        // Отправляем код в WhatsApp асинхронно
        this.sendWhatsAppMessage(phone, "Ваш код подтверждения: " + code);

        log.info("OTP generated for {}. Simulation for dev: {}", phone, code);
    }

    /**
     * Асинхронный метод отправки сообщения через Green API
     */
    @Async
    protected void sendWhatsAppMessage(String phone, String message) {
        try {
            // 1. Форматируем номер: только цифры + суффикс @c.us
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            String chatId = cleanPhone + "@c.us";

            // 2. Формируем URL
            String url = String.format("%s/waInstance%s/sendMessage/%s",
                    GREEN_API_BASE_URL, INSTANCE_ID, API_TOKEN);

            // 3. Создаем тело запроса
            Map<String, String> payload = new HashMap<>();
            payload.put("chatId", chatId);
            payload.put("message", message);

            // 4. Выполняем POST запрос
            restTemplate.postForEntity(url, payload, String.class);

            log.info("WhatsApp message sent successfully to {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", phone, e.getMessage());
        }
    }

    /**
     * Проверка кода и выдача JWT токенов
     */
    @Transactional
    public AuthResponse verifyOtp(String phone, String code) {
        String savedCode = redisTemplate.opsForValue().get("otp:" + phone);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new RuntimeException("Неверный код или срок действия истек");
        }

        // Если код верный — удаляем его из Redis
        redisTemplate.delete("otp:" + phone);

        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> {
                    User u = new User();
                    u.setPhone(phone);
                    u.setLastSeen(Instant.now());
                    User saved = userRepository.save(u);
                    System.out.println("DEBUG: Сохранен новый пользователь с ID: " + saved.getId());
                    return saved;
                });

        // Обновляем время последнего входа
        user.setLastSeen(Instant.now());
        userRepository.save(user);

        String access = jwtProvider.generateAccessToken(user.getId(), phone);
        String refresh = jwtProvider.generateRefreshToken(user.getId(), phone);

        return new AuthResponse(
                access,
                refresh,
                user.getDisplayName() == null // true, если нужно заполнить профиль
        );
    }
}