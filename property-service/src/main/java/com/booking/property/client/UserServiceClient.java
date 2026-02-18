package com.booking.property.client;

import com.booking.property.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-client",
        url = "${services.user-service.url}"
)
public interface UserServiceClient {

    /**
     * Получение пользователя по ID.
     * @param userId ID пользователя
     * @return данные пользователя или null если не найден
     */
    @GetMapping("/api/v1/users/{userId}")
    UserDto getUserById(@PathVariable("userId") UUID userId);
}
