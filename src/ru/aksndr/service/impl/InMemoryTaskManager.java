package ru.aksndr.service.impl;

import ru.aksndr.domain.*;
import ru.aksndr.enums.TaskStatus;
import ru.aksndr.enums.WorkItemType;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.*;
import ru.aksndr.util.Managers;

import java.util.*;

/**
 * Класс реализует управление задачами в памяти
 */
public class InMemoryTaskManager implements ITaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, SubTask> subTasks = new HashMap<>();
    protected final IHistoryManager historyManager = Managers.getDefaultHistoryManager();
    protected final Set<Task> prioritizedTasks;
    protected int counter = 0;

    public InMemoryTaskManager() {
        prioritizedTasks = new TreeSet<>(tasksStartTimeComparator);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Операции с задачами
    @Override
    public Task createTask(Task task) throws TasksIntersectsException {
        if (isIntersected(task))
            throw new TasksIntersectsException("Task intersect by start and end time with another task ");
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        rePrioritizeTasks(task);
        return task;
    }

    @Override
    public Task getTask(int id) {
        if (!tasks.containsKey(id))
            throw new TaskNotFoundException(WorkItemType.TASK, id);
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Task updateTask(Task task) throws TasksIntersectsException {
        if (isIntersected(task))
            throw new TasksIntersectsException("Task intersect by start and end time with another task ");
        tasks.put(task.getId(), task);
        rePrioritizeTasks(task);
        return task;
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
    }

    @Override
    public Collection<Task> getAllTasks() {
        Collection<Task> tasks = this.tasks.values();
        historyManager.add(tasks);
        return tasks;
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(task -> {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        });
        tasks.clear();
    }

    // Операции с подзадачами
    @Override
    public SubTask createSubTask(SubTask subtask) throws TasksIntersectsException {
        if (isIntersected(subtask))
            throw new TasksIntersectsException("Task intersect by start and end time with another task ");
        subtask.setId(getNextId());
        subTasks.put(subtask.getId(), subtask);
        if (!epics.containsKey(subtask.getEpicId()))
            throw new TaskNotFoundException(WorkItemType.EPIC, subtask.getEpicId());
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
        }
        rePrioritizeTasks(subtask);
        return subtask;
    }

    @Override
    public SubTask getSubTask(int id) {
        if (!subTasks.containsKey(id))
            throw new TaskNotFoundException(WorkItemType.SUBTASK, id);
        SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) throws TasksIntersectsException {
        if (isIntersected(subTask))
            throw new TasksIntersectsException("Task intersect by start and end time with another task ");

        // Удаляем подзадачу из старого эпика, если она была перемещена в другой эпик
        if (!subTasks.containsKey(subTask.getId()))
            throw new TaskNotFoundException(WorkItemType.SUBTASK, subTask.getId());
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
        if (!epics.containsKey(subTask.getEpicId()))
            throw new TaskNotFoundException(WorkItemType.EPIC, subTask.getEpicId());
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subTask);
            rePrioritizeTasks(subTask);
        }
        return subTask;
    }

    @Override
    public void deleteSubTask(int id) {
        SubTask subtask = subTasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.deleteSubtask(subtask);
            }
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
        }
    }

    @Override
    public void addSubTaskToEpic(Epic epic, Task subTask) {
        if (!(subTask instanceof SubTask)) {
            throw new IllegalArgumentException("SubTask must be instance of SubTask");
        }
        epic.addSubtask((SubTask) subTask);
    }

    @Override
    public Collection<SubTask> getAllSubTasks() {
        Collection<SubTask> subTasks = this.subTasks.values();
        historyManager.add(subTasks);
        return subTasks;
    }

    @Override
    public void deleteAllSubTasks() {
        subTasks.clear();
        epics.values().forEach(epic -> {
            epic.setStatus(TaskStatus.NEW);
            epic.getSubTasks().forEach(subtask -> {
                historyManager.remove(subtask.getId());
                subTasks.remove(subtask.getId());
                prioritizedTasks.remove(subtask);
            });
            epic.deleteAllSubtasks();
        });
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
        if (!epics.containsKey(id)) throw new TaskNotFoundException(WorkItemType.EPIC, id);
        return epics.get(id);
    }

    @Override
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id))
            throw new TaskNotFoundException(WorkItemType.EPIC, id);

        Epic epic = epics.get(id);
        for (SubTask subtask : epic.getSubTasks()) {
            historyManager.remove(subtask.getId());
            subTasks.remove(subtask.getId());
            epic.deleteSubtask(subtask);
        }
        historyManager.remove(epic.getId());
        epics.remove(id);
    }

    @Override
    public List<SubTask> getEpicSubtasks(int id) {
        if (!epics.containsKey(id))
            throw new TaskNotFoundException(WorkItemType.EPIC, id);

        Epic epic = epics.get(id);
        if (epic == null) {
            return null;
        }

        List<SubTask> subTasks = epic.getSubTasks();
        historyManager.add(subTasks);
        return subTasks;
    }

    @Override
    public List<Epic> getAllEpics() {
        List<Epic> epics = this.epics.values().stream().toList();
        historyManager.add(epics);
        return epics;
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (SubTask subtask : epic.getSubTasks()) {
                historyManager.remove(subtask.getId());
                epic.deleteSubtask(subtask);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
    }

    private boolean isIntersected(Task task) {
        if (task == null || task.getStartTime() == null) {
            return false;
        }

        Optional<Task> intersection = getPrioritizedTasks().stream()
                .filter(Objects::nonNull)
                .filter(t -> !t.equals(task))
                .filter(t -> t.getStartTime() != null)
                .filter(t -> task.getStartTime().isBefore(t.getEndTime())
                && task.getEndTime().isAfter(t.getStartTime()))
                .findFirst();

        return intersection.isPresent();
    }

    private void rePrioritizeTasks(Task task) {
        prioritizedTasks.remove(task);
        prioritizedTasks.add(task);
    }

    Comparator<Task> tasksStartTimeComparator = (task1, task2) -> {
        int result = 0;
        if (task1 == null) {
            result = 1;
        } else if (task1.getStartTime() == null && task2.getStartTime() == null) {
            result = task2.getId() - task1.getId();
        } else if (task1.getStartTime() != null && task2.getStartTime() == null) {
            result = -1;
        } else if (task1.getStartTime() == null && task2.getStartTime() != null) {
            result = 1;
        } else if (task1.getStartTime() != null && task2.getStartTime() != null) {
            if (task1.getStartTime().isAfter(task2.getStartTime())) {
                result = 1;
            } else if (task1.getStartTime().isBefore(task2.getStartTime())) {
                result = -1;
            }
        }
        return result;
    };

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected int getNextId() {
        return ++counter;
    }

}