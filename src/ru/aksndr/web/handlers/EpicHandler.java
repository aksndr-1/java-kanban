package ru.aksndr.web.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.enums.Endpoint;
import ru.aksndr.enums.WorkItemType;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.service.ITaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {

    public EpicHandler(ITaskManager taskManager) {
        super.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPIC: {
                handleGetEpic(exchange);
                break;
            }
            case CREATE_EPIC: {
                handlePostEpic(exchange);
                break;
            }
            case DELETE_EPIC: {
                handleDeleteEpic(exchange);
                break;
            }
            case GET_EPICS: {
                handleGetEpics(exchange);
                break;
            }
            case GET_EPIC_SUBTASKS: {
                handleGetEpicSubTasks(exchange);
                break;
            }
            case DELETE_EPICS: {
                handleDeleteEpics(exchange);
                break;
            }
            default:
                sendNotFoundMessage(exchange);
        }
    }

    private void handleDeleteEpics(HttpExchange exchange) throws IOException {
        removeAllTasks(exchange, WorkItemType.EPIC);
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        removeTask(exchange, WorkItemType.EPIC);
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String body = readRequestFromBody(exchange);
        if (body.isEmpty()) return;

        try {
            JsonElement jsonElement = JsonParser.parseString(body);
            if (!jsonElement.isJsonObject()) {
                sendBadRequestMessage(exchange);
                return;
            }
            Epic epic = gson.fromJson(jsonElement, Epic.class);
            if (epic.getId() > 0) {
                epic = taskManager.updateEpic(epic);
            } else {
                epic = taskManager.createEpic(epic);
            }
            String json = gson.toJson(epic);
            sendMessage(exchange, json);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    private void handleGetEpicSubTasks(HttpExchange exchange) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            if (taskManager.getEpic(id) == null) {
                sendNotFoundMessage(exchange);
                return;
            }
            List<SubTask> subtasks = taskManager.getEpicSubtasks(id);
            String json = gson.toJson(subtasks);
            sendMessage(exchange, json);
        } catch (TaskNotFoundException e) {
            sendErrorMessage(exchange);
        }
    }

    private void handleGetEpic(HttpExchange exchange) throws IOException {
        getTaskById(exchange, WorkItemType.EPIC);
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        getTask(exchange, WorkItemType.EPIC);
    }
}