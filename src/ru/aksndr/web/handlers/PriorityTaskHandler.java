package ru.aksndr.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.service.ITaskManager;

import java.io.IOException;

public class PriorityTaskHandler extends BaseHandler implements HttpHandler {

    public PriorityTaskHandler(ITaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestURI().getPath().contains("prioritized")) {
            sendMessage(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
        } else {
            sendNotFoundMessage(exchange);
        }
    }
}