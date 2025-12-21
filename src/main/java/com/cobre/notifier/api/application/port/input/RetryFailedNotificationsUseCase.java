package com.cobre.notifier.api.application.port.input;

/**
 * Puerto de entrada (Input Port) para reintentar notificaciones fallidas.
 * Define el caso de uso para procesar notificaciones que fallaron y pueden ser reintentadas.
 */
public interface RetryFailedNotificationsUseCase {

    /**
     * Reintenta notificaciones fallidas que aún pueden ser procesadas.
     * Busca notificaciones pendientes y las reintenta según la estrategia de retry.
     * 
     * @return Número de notificaciones procesadas
     */
    int retryFailedNotifications();
}

