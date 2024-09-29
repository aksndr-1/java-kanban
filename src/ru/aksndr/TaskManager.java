package ru.aksndr;

import ru.aksndr.domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

public class TaskManager {

    private int taskId = 0;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>();

    // Операции с задачами
    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public Collection<Task> getAllTasks() {
        return tasks.values();
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    // Операции с подзадачами
    public SubTask createSubTask(SubTask subtask) {
        subtask.setId(getNextId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
        }
        return subtask;
    }

    public SubTask getSubTask(int id) {
        return subtasks.get(id);
    }

    public void updateSubTask(SubTask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
            epic.updateStatus();
        }
    }

    public void deleteSubTask(int id) {
        SubTask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.deleteSubtask(subtask.getId());
            }
        }
    }

    public Collection<SubTask> getAllSubTasks() {
        return subtasks.values();
    }

    public void deleteAllSubTasks() {
        ArrayList<Integer> cleanedEpicsIds = new ArrayList<>();

        for (SubTask subtask : subtasks.values()) {
            int epicId = subtask.getEpicId();
            if (cleanedEpicsIds.contains(epicId)) {
                continue;
            }

            Epic epic = epics.get(epicId);
            if (epic != null) {
                epic.deleteAllSubtasks();
                cleanedEpicsIds.add(epicId);
            }
        }
        subtasks.clear();
    }

    // Операции с эпиками
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) return;

        Epic epic = epics.get(id);
        if (epic != null) {
            epic.deleteAllSubtasks();
        }
        epics.remove(id);
    }

    public Collection<SubTask> getEpicSubtasks(int id) {
        if (!epics.containsKey(id)) return null;

        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }
        return epic.getSubtasks();
    }

    public Collection<Epic> getAllEpics() {
        return epics.values();
    }

    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtasks();
        }
        epics.clear();
    }

    public int getNextId() {
        return ++taskId;
    }

}