package ru.aksndr.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.service.IWorkItemHandlerService;
import ru.aksndr.web.service.factory.WorkItemHandlerServiceFactory;

import java.io.IOException;

public class WorkItemHandler implements HttpHandler {

    private final ITaskManager manager;

    public WorkItemHandler(ITaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        IWorkItemHandlerService service = WorkItemHandlerServiceFactory.getService(exchange, manager);

        switch (exchange.getRequestMethod()) {
            case "POST" -> service.createOrUpdate(exchange);
            case "GET" -> service.read(exchange);
            case "DELETE" -> service.delete(exchange);
        }
    }

}