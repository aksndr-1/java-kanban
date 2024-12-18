package ru.aksndr.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.service.ITaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHandler implements HttpHandler {

    public HistoryHandler(ITaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().contains("history")) {
            sendMessage(exchange, gson.toJson(taskManager.getHistory()));
        } else {
            sendNotFoundMessage(exchange);
        }
    }
}