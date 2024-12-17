package ru.aksndr.web.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.aksndr.enums.Endpoint;
import ru.aksndr.enums.WorkItemType;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.adapters.DurationAdapter;
import ru.aksndr.web.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected ITaskManager taskManager;
    protected Gson gson = new Gson().newBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();

    protected Endpoint getEndpoint(String requestPath, String requestMetod) {
        String[] pathParts = requestPath.split("/");
        String itemTypePart = pathParts[1];
        if (pathParts.length == 2) {
            switch (requestMetod) {
                case "GET": {
                    switch (itemTypePart) {
                        case "tasks" -> {
                            return Endpoint.GET_TASKS;
                        }
                        case "subtasks" -> {
                            return Endpoint.GET_SUBTASKS;
                        }
                        case "epics" -> {
                            return Endpoint.GET_EPICS;
                        }
                    }
                }
                case "POST": {
                    switch (itemTypePart) {
                        case "tasks" -> {
                            return Endpoint.CREATE_TASK;
                        }
                        case "subtasks" -> {
                            return Endpoint.CREATE_SUBTASK;
                        }
                        case "epics" -> {
                            return Endpoint.CREATE_EPIC;
                        }
                    }
                }
                case "DELETE": {
                    switch (itemTypePart) {
                        case "tasks" -> {
                            return Endpoint.DELETE_TASKS;
                        }
                        case "subtasks" -> {
                            return Endpoint.DELETE_SUBTASKS;
                        }
                        case "epics" -> {
                            return Endpoint.DELETE_EPICS;
                        }
                    }
                }
                case "prioritized": {
                    return Endpoint.GET_PRIORITY_TASKS;
                }
            }
        }

        if (pathParts.length == 3) {
            switch (requestMetod) {
                case "GET": {
                    switch (itemTypePart) {
                        case "tasks" -> {
                            return Endpoint.GET_TASK;
                        }
                        case "subtasks" -> {
                            return Endpoint.GET_SUBTASK;
                        }
                        case "epics" -> {
                            return Endpoint.GET_EPIC;
                        }
                    }
                }

                case "DELETE": {
                    switch (itemTypePart) {
                        case "tasks" -> {
                            return Endpoint.DELETE_TASK;
                        }
                        case "subtasks" -> {
                            return Endpoint.DELETE_SUBTASK;
                        }
                        case "epics" -> {
                            return Endpoint.DELETE_EPIC;
                        }
                    }
                }
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            if (requestMetod.equals("GET")) {
                return Endpoint.GET_EPIC_SUBTASKS;
            }
        }
        return Endpoint.UNKNOWN;
    }

    protected void getTask(HttpExchange exchange, WorkItemType taskType) throws IOException {
        try {
            String json = switch (taskType) {
                case EPIC -> gson.toJson(taskManager.getAllEpics());
                case TASK -> gson.toJson(taskManager.getAllTasks());
                case SUBTASK -> gson.toJson(taskManager.getAllSubTasks());
            };
            sendMessage(exchange, json);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    protected void getTaskById(HttpExchange exchange, WorkItemType taskType) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            String json = switch (taskType) {
                case EPIC -> gson.toJson(taskManager.getEpic(id));
                case SUBTASK -> gson.toJson(taskManager.getSubTask(id));
                case TASK -> gson.toJson(taskManager.getTask(id));
            };
            sendMessage(exchange, json);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    protected void removeTask(HttpExchange exchange, WorkItemType taskType) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            switch (taskType) {
                case EPIC -> taskManager.deleteEpic(id);
                case TASK -> taskManager.deleteTask(id);
                case SUBTASK -> taskManager.deleteSubTask(id);
            }
            sendOkMessage(exchange);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    protected void removeAllTasks(HttpExchange exchange, WorkItemType taskType) throws IOException {
        try {
            switch (taskType) {
                case EPIC -> taskManager.deleteAllEpics();
                case TASK -> taskManager.deleteAllTasks();
                case SUBTASK -> taskManager.deleteAllSubTasks();
            }
            sendOkMessage(exchange);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    protected void sendOkMessage(HttpExchange exchange) throws IOException {
        int statusCode = 204;
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    protected int getWorkItemId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        try {
            return Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException(LocalDateTime.now() + " Некоректный ID задачи");
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