package ru.aksndr.web;

import com.sun.net.httpserver.HttpServer;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private final ITaskManager manager;
    private HttpServer server;

    public HttpTaskServer(ITaskManager taskManager) {
        manager = taskManager;
    }

    public void start() throws IOException {

        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PriorityTaskHandler(manager));
        server.start();
        System.out.println("HTTP сервер запущен на " + PORT + " порту.");
    }

    public void stop() {
        server.stop(0);
    }

}