package ru.aksndr.domain;

import ru.aksndr.enums.TaskStatus;
import java.util.ArrayList;

public class Epic extends Task{

    private final ArrayList<SubTask> subTasks = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public void addSubtask(SubTask subtask) {
        if (subTasks.contains(subtask)) {
            int index = subTasks.indexOf(subtask);
            subTasks.remove(index);
            subTasks.add(index, subtask);
        } else {
            subTasks.add(subtask);
        }
    }

    public void deleteSubtask(SubTask subTask) {
        subTasks.remove(subTask);
    }

    public void deleteAllSubtasks() {
        subTasks.clear();
    }

    @Override
    public String toString() {
        return "Epic {" +
                "id = " + getId() +
                ", title = '" + getTitle() + '\'' +
                ", description = '" + getDescription() + '\'' +
                ", status = " + getStatus() +
                ", subtasks count = " + subTasks.size() +
                '}';
    }

    /**
     * Обновление статуса Эпика по всем задачам в нем
     */
    public void updateStatus() {
        if (subTasks.isEmpty()) {
            setStatus(TaskStatus.NEW);
            return;
        }
        boolean allDone = true, allNew = true;

        for (SubTask subtask : subTasks) {
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

}