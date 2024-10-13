package ru.aksndr.util;

import ru.aksndr.service.IHistoryManager;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.service.impl.InMemoryHistoryManager;
import ru.aksndr.service.impl.InMemoryTaskManager;

public class Managers {

    public static ITaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static IHistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}