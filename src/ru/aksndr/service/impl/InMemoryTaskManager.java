package ru.aksndr.service.impl;

import ru.aksndr.domain.*;
import ru.aksndr.enums.TaskStatus;
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
    protected final TreeSet<Task> prioritizedTasks;
    protected int counter = 0;

    public InMemoryTaskManager() {
        prioritizedTasks = new TreeSet<>(TasksStartTimeComparator);
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Операции с задачами
    @Override
    public Task createTask(Task task) {
        if (isIntersected(task)) {
            return null;
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        rePrioritizeTasks(task);
        return task;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (isIntersected(task)) {
            return null;
        }
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
    public SubTask createSubTask(SubTask subtask) {
        if (isIntersected(subtask)) {
            return null;
        }
        subtask.setId(getNextId());
        subTasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
        }
        rePrioritizeTasks(subtask);
        return subtask;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = subTasks.get(id);
        historyManager.add(subTask);
        return subTask;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        if (isIntersected(subTask)) {
            return null;
        }
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
        for (SubTask subtask : epic.getSubTasks()){
            historyManager.remove(subtask.getId());
            subTasks.remove(subtask.getId());
            epic.deleteSubtask(subtask);
        }
        historyManager.remove(epic.getId());
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
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubTasks().forEach(subtask -> {
                historyManager.remove(subtask.getId());
                epic.deleteSubtask(subtask);
            });
        });
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

    Comparator<Task> TasksStartTimeComparator = (task1, task2) -> {
        int result = 0;
        if (task1 == null) {
            result = 1;
        } else if(task1.getStartTime() == null && task2.getStartTime() == null){
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