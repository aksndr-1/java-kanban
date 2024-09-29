package ru.aksndr.domain;

import ru.aksndr.enums.TaskStatus;

public class SubTask extends Task {

    private int epicId;

    public SubTask(int id, String title, String description, TaskStatus taskStatus, int epicId) {
        super(id, title, description, taskStatus);
        this.epicId = epicId;
    }

    public SubTask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "SubTask {" +
                "id = " + getId() +
                ", title = '" + getTitle() + '\'' +
                ", description = '" + getDescription() + '\'' +
                ", status = " + getStatus() +
                ", parentId = " + getEpicId() +
                '}';
    }
}
