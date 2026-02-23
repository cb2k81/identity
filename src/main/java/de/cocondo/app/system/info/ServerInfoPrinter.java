package de.cocondo.app.system.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ServerInfoPrinter implements ApplicationListener<WebServerInitializedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoPrinter.class);

    private final Environment environment;

    public ServerInfoPrinter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        String protocol = "http"; // Standard-Protokoll
        String host = "localhost"; // Standard-Host

        String serverPort = environment.getProperty("server.port");
        if (serverPort != null && !serverPort.isEmpty()) {
            port = Integer.parseInt(serverPort);
        }

        String serverProtocol = environment.getProperty("server.protocol");
        if (serverProtocol != null && !serverProtocol.isEmpty()) {
            protocol = serverProtocol;
        }

        String serverAddress = environment.getProperty("server.address");
        if (serverAddress != null && !serverAddress.isEmpty()) {
            host = serverAddress;
        }

        logger.info("Server reachable at: {}://{}:{}", protocol, host, port);
    }
}
