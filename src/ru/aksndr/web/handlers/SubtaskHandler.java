package ru.aksndr.web.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.domain.SubTask;
import ru.aksndr.enums.Endpoint;
import ru.aksndr.enums.WorkItemType;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    public SubtaskHandler(ITaskManager taskManager) {
        super.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET_SUBTASKS: {
                handleGetSubtask(exchange);
                break;
            }
            case GET_SUBTASK: {
                handleSubtaskID(exchange);
                break;
            }
            case CREATE_SUBTASK: {
                handleCreateSubtask(exchange);
                break;
            }
            case DELETE_SUBTASK: {
                handleDeleteSubtask(exchange);
                break;
            }
            case DELETE_SUBTASKS: {
                handleDeleteSubtasks(exchange);
                break;
            }
            default:
                sendNotFoundMessage(exchange);
        }
    }

    private void handleDeleteSubtasks(HttpExchange exchange) throws IOException {
        removeAllTasks(exchange, WorkItemType.SUBTASK);
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        removeTask(exchange, WorkItemType.SUBTASK);
    }

    private void handleCreateSubtask(HttpExchange exchange) throws IOException {
        String body = readRequestFromBody(exchange);
        if (body.isEmpty()) {
            sendBadRequestMessage(exchange);
            return;
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(body);
            if (!jsonElement.isJsonObject()) {
                sendBadRequestMessage(exchange);
                return;
            }
            SubTask subtaskOfRequest = gson.fromJson(jsonElement, SubTask.class);
            SubTask subtask = new SubTask(subtaskOfRequest.getTitle(),
                    subtaskOfRequest.getDescription(),
                    subtaskOfRequest.getStatus(),
                    subtaskOfRequest.getEpicId(),
                    subtaskOfRequest.getStartTime(),
                    subtaskOfRequest.getDuration());
            if (subtaskOfRequest.getId() > 0) {
                subtask.setId(subtaskOfRequest.getId());
                SubTask updSubtask = taskManager.updateSubTask(subtask);
                String json = gson.toJson(updSubtask);
                sendMessage(exchange, json);
            } else if (subtaskOfRequest.getId() == 0) {
                SubTask newSubtask = taskManager.createSubTask(subtask);
                String json = gson.toJson(newSubtask);
                sendMessage(exchange, json);
            } else {
                sendNotFoundMessage(exchange);
            }
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (TasksIntersectsException e) {
            sendIntersectionFindMessage(exchange);
        }
    }

    private void handleSubtaskID(HttpExchange exchange) throws IOException {
        getTaskById(exchange, WorkItemType.SUBTASK);
    }

    private void handleGetSubtask(HttpExchange exchange) throws IOException {
        getTask(exchange, WorkItemType.SUBTASK);
    }
}