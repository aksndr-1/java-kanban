package ru.aksndr.service.impl;

import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.enums.TaskStatus;
import ru.aksndr.enums.WorkItemType;
import ru.aksndr.exceptions.BackupFileException;
import ru.aksndr.exceptions.CastWorkItemTypeException;
import ru.aksndr.service.ITaskManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager implements ITaskManager {

    public static final String HEADER = "id,type,name,status,description,epic,startTime,duration\n";
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager load(File file) throws BackupFileException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        fileBackedTaskManager.loadFile(file);
        return fileBackedTaskManager;
    }

    // Операции с задачами
    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    // Операции с подзадачами
    @Override
    public SubTask createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
        return subTask;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
        return subTask;
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public void addSubTaskToEpic(Epic epic, Task subTask) {
        super.addSubTaskToEpic(epic, subTask);
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    // Операции с эпиками
    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteTask(id);
        save();
    }


    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    private void save() {
        try (Writer fileWriter = new FileWriter(file, StandardCharsets.UTF_8, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(HEADER);
            for (Task task : getAllTasks()) {
                bufferedWriter.write(workItemToString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                bufferedWriter.write(workItemToString(epic) + "\n");
            }

            for (SubTask subTask : getAllSubTasks()) {
                bufferedWriter.write(workItemToString(subTask) + "\n");
            }
        } catch (IOException e) {
            System.out.println("Ошибка записи в файл бекапа данных таск треккера");
        }
    }

    private void loadFile(File file) throws BackupFileException {
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.readLine();
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (line.isEmpty()) continue;
                Task task = stringToWorkItem(line);

                if (counter < task.getId()) {
                    counter = task.getId();
                }

                if (task.getItemType().equals(WorkItemType.TASK)) {
                    tasks.put(task.getId(), task);
                    prioritizedTasks.add(task);

                } else if (task.getItemType().equals(WorkItemType.EPIC)) {
                    Epic epic = (Epic) task;
                    epics.put(epic.getId(), epic);
                    prioritizedTasks.add(epic);

                } else if (task.getItemType().equals(WorkItemType.SUBTASK)) {
                    SubTask subTask = (SubTask) task;
                    subTasks.put(subTask.getId(), subTask);
                    prioritizedTasks.add(subTask);

                    Epic epic = epics.get(subTask.getEpicId());
                    epic.addSubtask(subTask);
                }
            }

            counter++;
        } catch (IOException | CastWorkItemTypeException e) {
            throw new BackupFileException("Ошибка загрузки из файла бекапа данных таск треккера");
        }
    }

    private static String workItemToString(Task task) {
        String epicId = "";
        if (task.getItemType().equals(WorkItemType.SUBTASK)) {
            epicId = String.valueOf(((SubTask) task).getEpicId());
        }
        return task.getId() + ","
                + task.getItemType() + ","
                + task.getTitle() + ","
                + task.getStatus() + ","
                + task.getDescription() + ","
                + epicId + ","
                + (task.getStartTime() == null ? "" : task.getStartTime()) + ","
                + (task.getDuration() == null ? "" : task.getDuration());
    }

    private static Task stringToWorkItem(String value) throws CastWorkItemTypeException {
        String[] split = value.split(",", -1);
        int id = Integer.parseInt(split[0]);
        WorkItemType taskType = WorkItemType.valueOf(split[1]);
        String title = split[2];
        TaskStatus status = TaskStatus.valueOf(split[3]);
        String description = split[4];
        LocalDateTime startTime = split[6].isBlank() ? null : LocalDateTime.parse(split[6]);
        Duration duration = split[7].isBlank() ? null : Duration.parse(split[7]);

        return switch (taskType) {
            case TASK -> new Task(id, title, description, status, startTime, duration);
            case SUBTASK -> {
                int epicId = Integer.parseInt(split[5]);
                yield new SubTask(id, title, description, status, epicId, startTime, duration);
            }
            case EPIC -> new Epic(id, title, description, status, startTime, duration);
        };
    }

}