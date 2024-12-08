package ru.aksndr.domain;

import ru.aksndr.enums.TaskStatus;
import ru.aksndr.enums.WorkItemType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {

    @Override
    public WorkItemType getItemType() {
        return WorkItemType.EPIC;
    }

    private final List<SubTask> subTasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description);
        this.endTime = null;
    }

    public Epic(int id, String title, String description, TaskStatus taskStatus) {
        super(id, title, description, taskStatus);
    }

    public Epic(int id, String title, String description, TaskStatus taskStatus, LocalDateTime startTime, Duration duration) {
        super(id, title, description, taskStatus, startTime, duration);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<SubTask> getSubTasks() {
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
        updateEpic();
    }

    public void deleteSubtask(SubTask subTask) {
        subTasks.remove(subTask);
        updateEpic();
    }

    public void deleteAllSubtasks() {
        subTasks.clear();
        updateEpic();
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
     * Обновление составных свойств Эпика по подзадачам
     */
    public void updateEpic() {
        updateStatus();
        updateStartTime();
        updateDuration();
        updateEndTime();
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

    /**
     * Обновление длительности эпика
     */
    private void updateDuration() {
        Duration duration = Duration.ZERO;
        for (SubTask subtask : subTasks) {
            if (subtask.getDuration() != null && !subtask.getDuration().isZero()) {
                Duration subtask1Duration = subtask.getDuration();
                duration = duration.plus(subtask1Duration);
            }
        }
        setDuration(duration);
    }

    /**
     * Обновление времени начала эпика
     */
    private void updateStartTime() {
        Optional<SubTask> epicStartTime = subTasks.stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .min(Comparator.comparing(Task::getStartTime));
        epicStartTime.ifPresent(subtask -> setStartTime(subtask.getStartTime()));
    }

    /**
     * Обновление времени окончания эпика
     */
    private void updateEndTime() {
        Optional<SubTask> epicEndTime = subTasks.stream()
                .filter(subtask -> subtask.getEndTime() != null)
                .max(Comparator.comparing(Task::getEndTime));
        epicEndTime.ifPresent(subtask -> setEndTime(subtask.getEndTime()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTasks, epic.subTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasks);
    }

}