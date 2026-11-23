
package org.example.escapehubbackend;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class BackendServer {

    private static final Path FRONTEND_FOLDER = Path.of(
            "C:/Users/jarof/IdeaProjects/escapehub/frontend"
    );

    private static final Path PROJECT_FOLDER = Path.of(
            "C:/Users/jarof/IdeaProjects/escapehub"
    );

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080), 0
        );

        // Frontend files
        server.createContext("/", exchange -> {

            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")) {
                path = "/index.html";
            }

            Path file = FRONTEND_FOLDER.resolve(
                    path.substring(1)
            ).normalize();

            if (!file.startsWith(FRONTEND_FOLDER)
                    || !Files.exists(file)
                    || Files.isDirectory(file)) {

                sendResponse(exchange, 404, "File not found",
                        "text/plain");
                return;
            }

            String type = "text/plain";

            if (path.endsWith(".html")) {
                type = "text/html";
            } else if (path.endsWith(".css")) {
                type = "text/css";
            } else if (path.endsWith(".js")) {
                type = "text/javascript";
            }

            byte[] content = Files.readAllBytes(file);

            exchange.getResponseHeaders().set(
                    "Content-Type", type + "; charset=UTF-8"
            );

            exchange.sendResponseHeaders(200, content.length);

            try (OutputStream output = exchange.getResponseBody()) {
                output.write(content);
            }
        });

        // Backend test
        server.createContext("/api/test", exchange -> {

            if (!handleCors(exchange)) {
                return;
            }

            String response =
                    "{\"status\":\"success\",\"message\":\"Backend connected!\"}";

            sendResponse(exchange, 200, response,
                    "application/json");
        });

        // Backend search
        server.createContext("/api/search", exchange -> {

            if (!handleCors(exchange)) {
                return;
            }

            String query = "";
            String request = exchange.getRequestURI().getRawQuery();

            if (request != null && request.startsWith("query=")) {
                query = URLDecoder.decode(
                        request.substring(6),
                        StandardCharsets.UTF_8
                );
            }

            String response =
                    "{\"message\":\"Escape Hub search is ready.\"}";

            sendResponse(exchange, 200, response,
                    "application/json");
        });

        // Git information
        server.createContext("/actuator/info", exchange -> {

            if (!handleCors(exchange)) {
                return;
            }

            String version = getGitInfo(
                    "rev-parse", "--short", "HEAD"
            );

            String branch = getGitInfo(
                    "branch", "--show-current"
            );

            String message = getGitInfo(
                    "log", "-1", "--pretty=%s"
            );

            String date = getGitInfo(
                    "log", "-1", "--date=short",
                    "--pretty=%cd"
            );

            String response =
                    "{\"app\":{" +
                            "\"version\":\"" + escapeJson(version) + "\"," +
                            "\"commit\":\"" + escapeJson(branch) + "\"," +
                            "\"message\":\"" + escapeJson(message) + "\"," +
                            "\"date\":\"" + escapeJson(date) + "\"" +
                            "}}";

            sendResponse(exchange, 200, response,
                    "application/json");
        });

        server.start();

        System.out.println("Escape Hub is running!");
        System.out.println("Frontend: http://localhost:8080");
        System.out.println("Backend: http://localhost:8080/api/test");
        System.out.println("Git information: http://localhost:8080/actuator/info");
    }

    // Gets information from Git
    private static String getGitInfo(String... command) {

        try {
            String[] fullCommand = new String[command.length + 3];

            fullCommand[0] = "git";
            fullCommand[1] = "-C";
            fullCommand[2] = PROJECT_FOLDER.toString();

            System.arraycopy(command, 0, fullCommand, 3,
                    command.length);

            Process process = new ProcessBuilder(fullCommand)
                    .redirectErrorStream(true)
                    .start();

            boolean finished = process.waitFor(
                    5, TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();
                return "unknown";
            }

            String result = new String(
                    process.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            ).trim();

            if (process.exitValue() != 0 || result.isEmpty()) {
                return "unknown";
            }

            return result;

        } catch (Exception e) {
            return "unknown";
        }
    }

    // Makes text safe for JSON
    private static String escapeJson(String text) {

        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // Allows frontend and backend communication
    private static boolean handleCors(HttpExchange exchange)
            throws IOException {

        String origin = exchange.getRequestHeaders()
                .getFirst("Origin");

        if ("http://localhost:8080".equals(origin)
                || "http://127.0.0.1:8080".equals(origin)) {

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Origin", origin);
        }

        exchange.getResponseHeaders()
                .set("Access-Control-Allow-Methods", "GET, OPTIONS");

        exchange.getResponseHeaders()
                .set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return false;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405,
                    "{\"error\":\"Method not allowed\"}",
                    "application/json");
            return false;
        }

        return true;
    }

    // Sends responses
    private static void sendResponse(
            HttpExchange exchange,
            int status,
            String response,
            String contentType) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type", contentType + "; charset=UTF-8"
        );

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(status, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}
