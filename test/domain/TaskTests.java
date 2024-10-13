package domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.*;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTests {

    private static ITaskManager taskManager;

    @BeforeAll
    public static void init() {
        taskManager = Managers.getDefaultTaskManager();
    }

    // проверка, что экземпляры класса Task равны друг другу, если равен их id
    @Test
    public void tasksShouldBeEqualIfIdsEqual() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание 2"));

        task2.setId(task1.getId());

        assertEquals(task1, task2, "Задачи не совпадают");
    }

    // проверка, что наследники класса Task равны друг другу, если равен их id
    @Test
    public void tasksAndItsExtendersShouldBeEqualIfIdsEqual() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId()));

        subTask1.setId(task1.getId());

        assertEquals(task1, subTask1, "Задачи не совпадают");
    }

}