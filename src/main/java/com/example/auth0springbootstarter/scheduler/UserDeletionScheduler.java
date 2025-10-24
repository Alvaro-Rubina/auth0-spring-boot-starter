package com.example.auth0springbootstarter.scheduler;

import com.auth0.exception.Auth0Exception;
import com.example.auth0springbootstarter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDeletionScheduler {

    private final UserService userService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void processScheduledDeletions() {
        log.info("=== Iniciando proceso de eliminación de usuarios programados ===");
        try {
            userService.executeScheduledDeletions();
            log.info("=== Proceso de eliminación completado exitosamente ===");
        } catch (Exception e) {
            log.error("Error crítico procesando eliminaciones programadas", e);
            // Opcional: enviar alerta a administradores
        }
    }
}