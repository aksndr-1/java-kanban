package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskManagerTest{

    static ITaskManager taskManager;

    Task task1;
    Epic epic1;
    SubTask subTask1;
    SubTask subTask2;

    @BeforeEach
    public void init() throws TasksIntersectsException {
        task1 = taskManager.createTask(new Task("Задача 1", "Описание 1", LocalDateTime.of(2024, 12, 1, 10, 20), Duration.ofMinutes(15)));
        epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId(), LocalDateTime.of(2024, 12, 2, 10, 20), Duration.ofMinutes(15)));
        subTask2 = taskManager.createSubTask(new SubTask("Подзадача 2 Эпика 1", "Описание подзадачи 2 Эпика 1", epic1.getId(), LocalDateTime.of(2024, 12, 2, 10, 50), Duration.ofMinutes(15)));
    }

    @AfterEach
    public void clean() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();
    }

}