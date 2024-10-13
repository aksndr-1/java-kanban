package ru.aksndr.service.impl;

import ru.aksndr.service.IHistoryManager;
import ru.aksndr.domain.Task;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InMemoryHistoryManager implements IHistoryManager {

    private final List<Task> history;
    private static final int LIMIT = 10;

    public InMemoryHistoryManager() {
        this.history = new ArrayList<>();
    }

    @Override
    public void addHistory(Task task) {
        if (history.size() == LIMIT) history.removeFirst();
        history.add(task);
    }

    @Override
    public void addHistory(Collection<? extends Task> tasks) {
        for (Task task : tasks) {
            addHistory(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

    @Override
    public short getLimit() {
        return LIMIT;
    }

}