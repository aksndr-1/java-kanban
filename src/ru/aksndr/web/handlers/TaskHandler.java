package ru.aksndr.web.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.domain.Task;
import ru.aksndr.enums.Endpoint;
import ru.aksndr.enums.WorkItemType;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(ITaskManager taskManager) {
        super.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        switch (endpoint) {
            case GET_TASKS: {
                handleGetTasks(exchange);
                break;
            }
            case GET_TASK: {
                handleGetTaskById(exchange);
                break;
            }
            case CREATE_TASK: {
                handleCreateTask(exchange);
                break;
            }
            case DELETE_TASK: {
                handleRemoveTask(exchange);
                break;
            }
            case DELETE_TASKS: {
                handleRemoveTasks(exchange);
                break;
            }
        }
    }

    private void handleRemoveTasks(HttpExchange exchange) throws IOException {
        removeAllTasks(exchange, WorkItemType.TASK);
    }

    private void handleRemoveTask(HttpExchange exchange) throws IOException {
        removeTask(exchange, WorkItemType.TASK);
    }


    private void handleCreateTask(HttpExchange exchange) throws IOException {
        String body = readRequestFromBody(exchange);
        if (body.isEmpty()) {
            return;
        }
        try {
            JsonElement jsonElement = JsonParser.parseString(body);
            if (!jsonElement.isJsonObject()) {
                sendBadRequestMessage(exchange);
                return;
            }

            Task taskOfElement = gson.fromJson(jsonElement, Task.class);
            Task task = new Task(taskOfElement.getTitle(),
                    taskOfElement.getDescription(),
                    taskOfElement.getStatus(),
                    taskOfElement.getStartTime(),
                    taskOfElement.getDuration());

            if (taskOfElement.getId() > 0) {
                task.setId(taskOfElement.getId());
                Task updTask = taskManager.updateTask(task);
                String json = gson.toJson(updTask);
                sendMessage(exchange, json);
            } else if (task.getId() == 0) {
                Task newTask = taskManager.createTask(task);
                String json = gson.toJson(newTask);
                sendMessage(exchange, json);
            } else {
                sendNotFoundMessage(exchange);
            }
        } catch (TasksIntersectsException e) {
            sendIntersectionFindMessage(exchange);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        getTask(exchange, WorkItemType.TASK);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        getTaskById(exchange, WorkItemType.TASK);
    }
}