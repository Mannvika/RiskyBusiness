package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameService gameService;
    private final LobbyService lobbyService;

    public WebSocketConfig(LobbyService lobbyService, GameService gameService) {
        this.gameService = gameService;
        this.lobbyService = lobbyService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new GameWebSocketHandler(lobbyService, gameService), "/game")
                .setAllowedOrigins("*");
    }
}
