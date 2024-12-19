package ru.aksndr.web.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.aksndr.domain.SubTask;
import ru.aksndr.exceptions.IdNotANumberException;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.handlers.BaseHandler;
import ru.aksndr.web.service.IWorkItemHandlerService;

import java.io.IOException;

public class SubTaskHandlerService extends BaseHandler implements IWorkItemHandlerService {

    public SubTaskHandlerService(ITaskManager taskManager) {
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

            SubTask subTask = gson.fromJson(jsonElement, SubTask.class);
            if (subTask.getId() > 0) {
                subTask = taskManager.updateSubTask(subTask);
            } else {
                subTask = taskManager.createSubTask(subTask);
            }

            String json = gson.toJson(subTask);
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
                message = gson.toJson(taskManager.getAllSubTasks());
            } else {
                SubTask subTask = taskManager.getSubTask(id);
                if (subTask == null) {
                    sendNotFoundMessage(exchange);
                    return;
                }
                message = gson.toJson(subTask);
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
                taskManager.deleteAllSubTasks();
            } else {
                taskManager.deleteSubTask(id);
            }
            sendOkMessage(exchange);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (IdNotANumberException e) {
            sendErrorMessage(exchange);
        }
    }
}