package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Task;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class InMemoryHistoryManagerTests {

    private static ITaskManager taskManager;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefaultTaskManager();
    }

    // проверяем, что задача добавляется в историю при её получении из менеджера задач
    @Test
    void addToHistoryTest() {
        Task task = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        taskManager.getTask(task.getId());

        List<Task> history = taskManager.getHistory();
        assertNotEquals(0, history.size(), "Список истории не заполняется");
    }

    // проверяем, что история заполняется согласно порядку получения задач
    @Test
    void checkHistoryRelevance() {
        Task task = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        taskManager.getTask(task.getId());

        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание 2"));
        taskManager.getTask(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(task, history.getFirst(), "Последовательность заполнения истории некорректна");
    }

    // проверяем, что задача при удалении из менеджера задач удаляется из истории
    @Test
    void checkHistoryRelevanceAfterDelete() {
        Task task = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        taskManager.getTask(task.getId());
        List<Task> history = taskManager.getHistory();
        assertEquals(task, history.getFirst(), "Последовательность заполнения истории некорректна");

        taskManager.deleteTask(task.getId());

        history = taskManager.getHistory();
        assertEquals(0, history.size(), "Задача не удаляется из истории при удалении");
    }

    // проверяем, что задача перемещается в конец истории при её повторном получении из менеджера задач
    @Test
    void checkHistoryRelevanceAfterRepeat() {
        Task task = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        taskManager.getTask(task.getId());

        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание 2"));
        taskManager.getTask(task2.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(task, history.getFirst(), "Последовательность заполнения истории некорректна");

        taskManager.getTask(task.getId());

        history = taskManager.getHistory();
        assertEquals(task, history.getLast(), "Последовательность заполнения истории некорректна");
    }

}