package ru.aksndr.service.impl;

import ru.aksndr.domain.*;
import ru.aksndr.enums.TaskStatus;
import ru.aksndr.service.*;
import ru.aksndr.util.Managers;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements ITaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subTasks = new HashMap<>();
    private final IHistoryManager historyManager = Managers.getDefaultHistoryManager();
    private int counter = 0;

    public InMemoryTaskManager() {}

    @Override
    public IHistoryManager getHistoryManager() {
        return historyManager;
    }

    // Операции с задачами
    @Override
    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.addHistory(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public Collection<Task> getAllTasks() {
        Collection<Task> tasks = this.tasks.values();
        historyManager.addHistory(tasks);
        return tasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    // Операции с подзадачами
    @Override
    public SubTask createSubTask(SubTask subtask) {
        subtask.setId(getNextId());
        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
        }
        return subtask;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        historyManager.addHistory(subTask);
        return subTask;
    }

    @Override
    public void updateSubTask(SubTask subTask) {

        // Удаляем подзадачу из старого эпика, если она была перемещена в другой эпик
        SubTask oldVersionSubTask = subTasks.get(subTask.getId());
        int oldEpicId = oldVersionSubTask.getEpicId();
        if (oldEpicId != subTask.getEpicId()) {
            Epic oldEpic = epics.get(oldEpicId);
            if (oldEpic != null) {
                oldEpic.deleteSubtask(oldVersionSubTask);
            }
        }

        // Добавляем подзадачу в новый эпик
        subTasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subTask);
            epic.updateStatus();
        }
    }

    @Override
    public void deleteSubTask(int id) {
        SubTask subtask = subTasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.deleteSubtask(subtask);
            }
        }
    }

    @Override
    public void addSubTaskToEpic(Epic epic, Task subTask){
        if (!(subTask instanceof SubTask)) {
            throw new IllegalArgumentException("SubTask must be instance of SubTask");
        }
        epic.addSubtask((SubTask) subTask);
        epic.updateStatus();
    }

    @Override
    public Collection<SubTask> getAllSubTasks() {
        Collection<SubTask> subTasks = this.subTasks.values();
        historyManager.addHistory(subTasks);
        return subTasks;
    }

    @Override
    public void deleteAllSubTasks() {
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.setStatus(TaskStatus.NEW);
            epic.deleteAllSubtasks();
        }
    }

    // Операции с эпиками
    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) return;

        Epic epic = epics.get(id);
        for (SubTask subtask : epic.getSubTasks()) {
            subTasks.remove(subtask.getId());
        }
        epics.remove(id);
    }

    @Override
    public List<SubTask> getEpicSubtasks(int id) {
        if (!epics.containsKey(id)) return null;

        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }

        List<SubTask> subTasks = epic.getSubTasks();
        historyManager.addHistory(subTasks);
        return subTasks;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> epics = this.epics.values().stream().toList();
        historyManager.addHistory(epics);
        return epics;
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            epic.deleteAllSubtasks();
        }
        epics.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private int getNextId() {
        return ++counter;
    }

}