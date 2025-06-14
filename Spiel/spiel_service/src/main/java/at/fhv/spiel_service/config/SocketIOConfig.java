package at.fhv.spiel_service.config;

import com.corundumstudio.socketio.SocketIOServer;
import at.fhv.spiel_service.domain.Gadget;
import at.fhv.spiel_service.domain.Player;
import at.fhv.spiel_service.domain.Position;
import at.fhv.spiel_service.dto.*;
import at.fhv.spiel_service.service.game.logic.DefaultIGameLogic;
import at.fhv.spiel_service.service.gameSession.GameSessionImpl;
import at.fhv.spiel_service.service.gameSession.IGameSession;
import at.fhv.spiel_service.service.room.IRoomService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ConfigurationProperties(prefix = "socketio")
public class SocketIOConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);

    // ========== Konfigurierbare Felder ==========
    @NotBlank
    private String hostname;
    @Min(1)
    @Max(65535)
    private int port;
    @NotBlank
    private String origin;

    // ========== Getter & Setter ==========
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    // ========== Server Bean ==========
    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        config.setOrigin(origin);

        log.info("Socket.IO Server config → Host: {}, Port: {}, Origin: {}", hostname, port, origin);
        SocketIOServer server = new SocketIOServer(config);
        server.start();
        return server;
    }
}


