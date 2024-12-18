package ru.aksndr.web.service.factory;

import com.sun.net.httpserver.HttpExchange;
import ru.aksndr.enums.HttpResource;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.web.service.IWorkItemHandlerService;
import ru.aksndr.web.service.impl.EpicHandlerService;
import ru.aksndr.web.service.impl.SubTaskHandlerService;
import ru.aksndr.web.service.impl.TaskHandlerService;

public class WorkItemHandlerServiceFactory {

    public static IWorkItemHandlerService getService(HttpExchange exchange, ITaskManager manager) {
        HttpResource resource = getResourceType(exchange.getRequestURI().getPath());
        return switch (resource) {
            case TASKS -> new TaskHandlerService(manager);
            case EPICS -> new EpicHandlerService(manager);
            case SUBTASKS -> new SubTaskHandlerService(manager);
        };
    }

    private static HttpResource getResourceType(String path) {
        String[] pathParts = path.split("/");
        return HttpResource.valueOf(pathParts[1].toUpperCase());
    }

}
