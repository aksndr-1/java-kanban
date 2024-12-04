package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.enums.TaskStatus;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryTaskManagerTests {

    private static ITaskManager taskManager;

    private Task task1;
    private Epic epic1;
    private SubTask subTask1;
    private SubTask subTask2;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefaultTaskManager();
        task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId()));
        subTask2 = taskManager.createSubTask(new SubTask("Подзадача 2 Эпика 1", "Описание подзадачи 2 Эпика 1", epic1.getId()));
    }

    @AfterEach
    public void clean() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();
    }

    @DisplayName("проверить, что свойства добавленной и полученной из менеджера задачи совпадают")
    @Test
    void taskFeaturesEqualityTest() {
        Task task2 = taskManager.getTask(task1.getId());
        assertEquals(task1.getDescription(), task2.getDescription(), "Описание задачи не соответствует полученной");
        assertEquals(task1.getTitle(), task2.getTitle(), "Наименованеи задачи не соответствует полученной");
        assertEquals(task1.getStatus(), task2.getStatus(), "Статус задачи не соответствует полученной");
    }

    @DisplayName("проверить, что добавление задачи приводит к заполнению списка задач")
    @Test
    void manageTaskTests() {
        Collection<Task> tasks = taskManager.getAllTasks();
        assertNotEquals(0, tasks.size(), "Список задач пустой после добавления задачи");
        assertNotNull(taskManager.getTask(task1.getId()), "Не удалось получить задачу по ID после добавления");
    }

    @DisplayName("Проверка срока исполнения задач")
    @Test
    void tasksEndTimeTests() {
        task1.setStartTime(LocalDateTime.of(2024, 12, 1, 12, 10));
        task1.setDuration(Duration.ofMinutes(10));

        assertEquals(LocalDateTime.of(2024, 12, 1, 12, 20), task1.getEndTime(), "Время исполнения задачи не соответствуте времени старта плюс длительность задачи");
    }

    @DisplayName("Проверка невозможности создания пересекающихся задач")
    @Test
    void tasksIntersectionTests() {
        task1.setStartTime(LocalDateTime.of(2024, 12, 1, 12, 10));
        task1.setDuration(Duration.ofMinutes(10));

        Task task2 = new Task("Задача 2", "Описание 2", LocalDateTime.of(2024, 12, 1, 12, 0), Duration.ofMinutes(15));
        assertNull(taskManager.createTask(task2), "Пересекающиеся по времени задачи не могут быть созданы");
    }

    @DisplayName("Проверка методов управления задачами")
    @Test
    void manageSubTaskTests() {
        task1.setStartTime(LocalDateTime.of(2024, 12, 1, 12, 10));
        task1.setDuration(Duration.ofMinutes(10));
        task1.setTitle("Задача 1 - updated");
        task1.setDescription("Описание 1 - updated");
        Task task = taskManager.updateTask(task1);

        assertEquals(LocalDateTime.of(2024, 12, 1, 12, 20), task.getEndTime(), "Время исполнения задачи не соответствуте времени старта плюс длительность задачи");
        assertEquals("Задача 1 - updated", task.getTitle(), "Наименованеи задачи не соответствует полученной");
        assertEquals("Описание 1 - updated", task.getDescription(), "Описание задачи не соответствует полученной");

        List<Epic> epics = taskManager.getAllEpics();
        assertNotEquals(0, epics.size(), "Список эпиков пустой после добавления");
        assertNotNull(taskManager.getEpic(epic1.getId()), "Не удалось получить эпик по id после добавления");

        List<SubTask> subTasks = taskManager.getEpicSubtasks(epic1.getId());
        assertEquals(subTasks.toArray()[0], subTask1, "Добавленный SubTask не соответствет полученному из ITaskManager");

        subTask1.setStatus(TaskStatus.DONE);
        subTask1 = taskManager.updateSubTask(subTask1);

        subTask2.setStatus(TaskStatus.DONE);
        subTask2 = taskManager.updateSubTask(subTask2);

        // проверка изменения статуса подзадачи и связанного эпика
        assertEquals(TaskStatus.DONE, taskManager.getEpicSubtasks(epic1.getId()).getFirst().getStatus(), "Статус подзадачи не соответствует измененному");
        assertEquals(TaskStatus.DONE, taskManager.getEpic(subTask1.getEpicId()).getStatus(), "Статус эпика не соответствует измененному");
    }

    @DisplayName("тесты управления свойствами эпиков")
    @Test
    void manageEpicsTests() {
        assertEquals(TaskStatus.NEW, epic1.getStatus(), "Новый эпик должен быть в статусе NEW");
        assertEquals(TaskStatus.NEW, epic1.getStatus(), "Эпик с новыми подзадачами должен быть в статусе NEW");

        subTask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus(), "Эпик с однной выполненной задачей должен быть в статусе IN_PROGRESS");

        subTask2.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask2);
        assertEquals(TaskStatus.DONE, epic1.getStatus(), "Эпик со всеми выполненными задачами должен быть в статусе DONE");

        taskManager.deleteSubTask(subTask1.getId());
        assertEquals(1, epic1.getSubTasks().size(), "Удаление подзадачи должно удалить её из эпика");
    }

    @DisplayName("Проверяем, что при удалении подзадачи она удаляется из эпика")
    @Test
    void deleteSubTaskTest() {
        taskManager.deleteSubTask(subTask1.getId());
        assertFalse(epic1.getSubTasks().contains(subTask1), "Удаление подзадачи должно удалить её из эпика");
    }

    @DisplayName("Проверяем, что при удалении эпика он удаляетс из менеджера и его подзадачи тоже удаляются из менеджера")
    @Test
    void deleteEpicTest() {
        taskManager.deleteEpic(epic1.getId());
        int subTaskId= subTask1.getId();
        assertNull(taskManager.getEpic(epic1.getId()), "Удаление эпика должно удалить её из менеджера");
        assertNull(taskManager.getSubTask(subTaskId), "Удаление эпика должно удалить его подзадачу");
    }

    @DisplayName("Проверка прироритетности задач")
    @Test
    void priorityTaskTest() {
        assertEquals("Задача 1", taskManager.getPrioritizedTasks().getLast().getTitle());
    }

}