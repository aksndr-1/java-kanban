package ru.aksndr.web.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.aksndr.domain.Task;
import ru.aksndr.exceptions.IdNotANumberException;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.handlers.BaseHandler;
import ru.aksndr.web.service.IWorkItemHandlerService;

import java.io.IOException;

public class TaskHandlerService extends BaseHandler implements IWorkItemHandlerService {

    public TaskHandlerService(ITaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void createOrUpdate(HttpExchange exchange) throws IOException {
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

            Task task = gson.fromJson(jsonElement, Task.class);
            if (task.getId() > 0) {
                task = taskManager.updateTask(task);
            } else {
                task = taskManager.createTask(task);
            }

            String json = gson.toJson(task);
            sendMessage(exchange, json);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (TasksIntersectsException e) {
            sendIntersectionFindMessage(exchange);
        } catch (Exception e) {
            sendErrorMessage(exchange);
        }
    }

    @Override
    public void read(HttpExchange exchange) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            String message;
            if (id == 0) {
                message = gson.toJson(taskManager.getAllTasks());
            } else {
                Task task = taskManager.getTask(id);
                if (task == null) {
                    sendNotFoundMessage(exchange);
                    return;
                }
                message = gson.toJson(task);
            }
            sendMessage(exchange, message);
        } catch (IdNotANumberException e) {
            sendErrorMessage(exchange);
        }
    }

    @Override
    public void delete(HttpExchange exchange) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            if (id == 0) {
                taskManager.deleteAllTasks();
            } else {
                taskManager.deleteTask(id);
            }
            sendOkMessage(exchange);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (IdNotANumberException e) {
            sendErrorMessage(exchange);
        }
    }
}