package ru.aksndr.web.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.aksndr.domain.Epic;
import ru.aksndr.exceptions.IdNotANumberException;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.handlers.BaseHandler;
import ru.aksndr.web.service.IWorkItemHandlerService;

import java.io.IOException;

public class EpicHandlerService extends BaseHandler implements IWorkItemHandlerService {

    public EpicHandlerService(ITaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void read(HttpExchange exchange) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            String message;
            if (id == 0) {
                message = gson.toJson(taskManager.getAllEpics());
            } else {
                Epic epic = taskManager.getEpic(id);
                if (epic == null) {
                    sendNotFoundMessage(exchange);
                    return;
                }

                if (exchange.getRequestURI().getPath().contains("/subtasks")) {
                    message = gson.toJson(epic.getSubTasks());
                } else {
                    message = gson.toJson(epic);
                }
            }
            sendMessage(exchange, message);
        } catch (IdNotANumberException e) {
            sendErrorMessage(exchange);
        }
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

    @Override
    public void delete(HttpExchange exchange) throws IOException {
        try {
            int id = getWorkItemId(exchange);
            if (id == 0) {
                taskManager.deleteAllEpics();
            } else {
                taskManager.deleteEpic(id);
            }
            sendOkMessage(exchange);
        } catch (TaskNotFoundException e) {
            sendNotFoundMessage(exchange);
        } catch (IdNotANumberException e) {
            sendErrorMessage(exchange);
        }
    }

}