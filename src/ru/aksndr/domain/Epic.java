package ru.aksndr.domain;

import ru.aksndr.enums.TaskStatus;

import java.util.HashMap;
import java.util.Collection;

public class Epic extends Task{

    // я не согласен, что здесь лучше будет использовать List, потому,
    // что в методе deleteSubtask мне придётся ходить циклом, чтобы удалить подзадачу по идентификатору
    // тогда как использование Map будет быстрее, т.к. сложность будет O(1)
    // а в случае List будет O(n)
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>();

    public Epic(int id, String title, String description, TaskStatus taskStatus) {
        super(id, title, description, taskStatus);
    }

    public Epic(String title, String description) {
        super(title, description);
    }

    public Collection<SubTask> getSubtasks() {
        return subtasks.values();
    }

    public void addSubtask(SubTask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public String toString() {
        return "Epic {" +
                "id = " + getId() +
                ", title = '" + getTitle() + '\'' +
                ", description = '" + getDescription() + '\'' +
                ", status = " + getStatus() +
                ", subtasks count = " + getSubtasks().size() +
                '}';
    }

    public void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }
        boolean allDone = true, allNew = true;

        for (SubTask subtask : subtasks.values()) {
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            setStatus(TaskStatus.NEW);
        } else if (allDone) {
            setStatus(TaskStatus.DONE);
        } else {
            setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            subtasks.remove(id);
            updateStatus();
        }
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        updateStatus();
    }
}