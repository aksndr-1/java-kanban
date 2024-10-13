package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Task;
import ru.aksndr.service.IHistoryManager;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class InMemoryHistoryManagerTest {

    private static ITaskManager taskManager;
    private static IHistoryManager historyManager;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefaultTaskManager();
        historyManager = taskManager.getHistoryManager();
    }

    // проверяем, что длина истории не превышает ограничения
    @Test
    void historyLengthMatchesLimit() {
        for (int i = 1; i <= 50; i++) {
            Task task = taskManager.createTask(new Task("Задача " + i, "Описание " + i));
            taskManager.getTask(task.getId());
        }
        List<Task> history = historyManager.getHistory();
        assertEquals(historyManager.getLimit(), history.size(), "Длина списка истории не равна ограничению");
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

}
