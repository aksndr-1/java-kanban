package ru.aksndr.domain;

import ru.aksndr.enums.TaskStatus;
import ru.aksndr.enums.WorkItemType;

import java.util.Objects;

public class SubTask extends Task {

    private int epicId;

    @Override
    public WorkItemType getItemType() {
        return WorkItemType.SUBTASK;
    }

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

    public void setEpicId(int epicId) {
        this.epicId = epicId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

}