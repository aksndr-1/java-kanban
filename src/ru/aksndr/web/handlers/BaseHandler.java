package ru.aksndr.web.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.aksndr.exceptions.IdNotANumberException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.adapters.DurationAdapter;
import ru.aksndr.web.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHandler {

    protected final ITaskManager taskManager;

    public BaseHandler(ITaskManager taskManager) {
        this.taskManager = taskManager;
    }

    protected Gson gson = new Gson().newBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();

    protected void sendOkMessage(HttpExchange exchange) throws IOException {
        int statusCode = 204;
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    protected int getWorkItemId(HttpExchange exchange) throws IdNotANumberException {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length < 3) {
            return 0;
        }
        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            throw new IdNotANumberException("Некоректный ID задачи");
        }
    }

    protected void sendMessage(HttpExchange exchange, String message) throws IOException {
        int statusCode = 200;
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendErrorMessage(HttpExchange exchange) throws IOException {
        int statusCode = 500;
        exchange.sendResponseHeaders(statusCode, 0);
        exchange.close();
    }

    protected void sendBadRequestMessage(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, 0);
        exchange.close();
    }

    protected void sendNotFoundMessage(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
        exchange.close();
    }

    protected void sendIntersectionFindMessage(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(406, 0);
        exchange.close();
    }

    protected String readRequestFromBody(HttpExchange exchange) throws IOException {
        String body;
        try (InputStream inputStream = exchange.getRequestBody()) {
            body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (body.isBlank()) {
            sendBadRequestMessage(exchange);
            return "";
        }
        return body;
    }

}
