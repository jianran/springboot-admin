package io.github.adminconsole.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.arthas.agent.attach.ArthasAgent;
import io.github.adminconsole.config.AdminConsoleProperties;
import io.github.adminconsole.config.AdminConsoleAnnotationSettings;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.HashMap;

@Component
public class ArthasService {
    private final AdminConsoleProperties properties;
    private final ObjectMapper mapper;
    private final AdminConsoleAnnotationSettings annotationSettings;
    private final HttpClient client;

    public ArthasService(AdminConsoleProperties properties, ObjectMapper mapper,
                         AdminConsoleAnnotationSettings annotationSettings) {
        this.properties = properties;
        this.mapper = mapper;
        this.annotationSettings = annotationSettings;
        this.client = HttpClient.newBuilder().connectTimeout(properties.getArthas().getTimeout()).build();
    }

    public JsonNode execute(String command) {
        var config = properties.getArthas();
        assertEnabled();
        String normalized = command == null ? "" : command.strip();
        String root = normalized.split("\\s+", 2)[0];
        if (normalized.isBlank() || !config.getAllowedCommands().contains(root))
            throw new IllegalArgumentException("Arthas command is not allowed: " + root);
        try {
            String body = mapper.writeValueAsString(Map.of("action", "exec", "command", normalized,
                "execTimeout", config.getTimeout().toMillis()));
            HttpRequest request = HttpRequest.newBuilder(config.getEndpoint()).timeout(config.getTimeout())
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new IllegalStateException("Arthas returned HTTP " + response.statusCode());
            return mapper.readTree(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Arthas request interrupted", e);
        } catch (Exception e) {
            throw new IllegalStateException("Arthas request failed", e);
        }
    }

    public synchronized Map<String, Object> attach() {
        var config = properties.getArthas();
        assertEnabled();
        String host = config.getEndpoint().getHost();
        if (!("127.0.0.1".equals(host) || "localhost".equalsIgnoreCase(host) || "::1".equals(host)))
            throw new IllegalStateException("Self-attach requires a loopback Arthas endpoint");
        if (isAttached()) return Map.of("status", "already-attached", "endpoint", config.getEndpoint().toString());
        int port = config.getEndpoint().getPort() > 0 ? config.getEndpoint().getPort() : 8563;
        try {
            HashMap<String, String> values = new HashMap<>();
            values.put("arthas.ip", host);
            values.put("arthas.httpPort", Integer.toString(port));
            values.put("arthas.telnetPort", "-1");
            ArthasAgent.attach(values);
            if (!isAttached()) throw new IllegalStateException("Arthas attached but its HTTP API is not responding");
            return Map.of("status", "attached", "endpoint", config.getEndpoint().toString());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to attach Arthas to the current JVM", exception);
        }
    }

    public Map<String, Object> status() {
        boolean enabled = properties.getArthas().isEnabled() || annotationSettings.arthasEnabled();
        return Map.of("enabled", enabled, "attached", enabled && isAttached(),
            "endpoint", properties.getArthas().getEndpoint().toString());
    }

    private boolean isAttached() {
        try { execute("version"); return true; } catch (RuntimeException ignored) { return false; }
    }

    private void assertEnabled() {
        if (!properties.getArthas().isEnabled() && !annotationSettings.arthasEnabled())
            throw new IllegalStateException("Arthas integration is disabled");
    }
}
