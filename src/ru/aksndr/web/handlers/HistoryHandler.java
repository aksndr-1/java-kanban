package ru.aksndr.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.enums.Endpoint;
import ru.aksndr.service.ITaskManager;

import java.io.IOException;
import java.util.Objects;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(ITaskManager taskManager) {
        super.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        if (Objects.requireNonNull(endpoint) == Endpoint.GET_HISTORY) {
            handleGethisory(exchange);
        } else {
            sendNotFoundMessage(exchange);
        }
    }

    private void handleGethisory(HttpExchange exchange) throws IOException {
        String json = gson.toJson(taskManager.getHistory());
        sendMessage(exchange, json);
    }
}