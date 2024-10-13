package ru.aksndr.service;

import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;

import java.util.Collection;
import java.util.List;

public interface ITaskManager {
    // Операции с задачами
    Task createTask(Task task);

    Task getTask(int id);

    void updateTask(Task task);

    void deleteTask(int id);

    Collection<Task> getAllTasks();

    void deleteAllTasks();

    // Операции с подзадачами
    SubTask createSubTask(SubTask subtask);

    SubTask getSubTask(int id);

    void updateSubTask(SubTask subtask);

    void deleteSubTask(int id);

    Collection<SubTask> getAllSubTasks();

    void deleteAllSubTasks();

    // Операции с эпиками
    Epic createEpic(Epic epic);

    Epic getEpic(int id);

    void updateEpic(Epic epic);

    void deleteEpic(int id);

    List<SubTask> getEpicSubtasks(int id);

    List<Epic> getAllEpics();

    void deleteAllEpics();

    List<Task> getHistory();

    void addSubTaskToEpic(Epic epic, Task subTask);

    IHistoryManager getHistoryManager();
}
