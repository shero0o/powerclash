package at.fhv.spiel_service.config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "socketio")
public class SocketIOConfig {

    private static final Logger log = LoggerFactory.getLogger(SocketIOConfig.class);

    @NotBlank
    private String hostname;
    @Min(1)
    @Max(65535)
    private int port;
    @NotBlank
    private String origin;


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


