package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.enums.TaskStatus;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryTaskManagerTests {

    private static ITaskManager taskManager;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefaultTaskManager();
    }

    // проверить, что свойства добавленной и полученной из менеджера задачи совпадают
    @Test
    void taskFeaturesEqualityTest() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Task task2 = taskManager.getTask(task1.getId());
        assertEquals(task1.getDescription(), task2.getDescription(), "Описание задачи не соответствует полученной");
        assertEquals(task1.getTitle(), task2.getTitle(), "Наименованеи задачи не соответствует полученной");
        assertEquals(task1.getStatus(), task2.getStatus(), "Статус задачи не соответствует полученной");
    }

    // проверить, что добавление задачи приводит к заполнению списка задач
    @Test
    void manageTaskTests() {
        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Collection<Task> tasks = taskManager.getAllTasks();
        assertNotEquals(0, tasks.size(), "Список задач пустой после добавления задачи");
        assertNotNull(taskManager.getTask(task1.getId()), "Не удалось получить задачу по ID после добавления");
    }

    // проверка методов управления задачами
    @Test
    void manageSubTaskTests() {
        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId()));

        List<Epic> epics = taskManager.getAllEpics();
        assertNotEquals(0, epics.size(), "Список эпиков пустой после добавления");
        assertNotNull(taskManager.getEpic(epic1.getId()), "Не удалось получить эпик по id после добавления");

        List<SubTask> subTasks = taskManager.getEpicSubtasks(epic1.getId());
        assertEquals(subTasks.toArray()[0], subTask1, "Добавленный SubTask не соответствет полученному из ITaskManager");

        subTask1.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subTask1);

        // проверка изменения статуса подзадачи и связанного эпика
        assertEquals(TaskStatus.DONE, taskManager.getEpicSubtasks(epic1.getId()).getFirst().getStatus(), "Статус подзадачи не соответствует измененному");
        assertEquals(TaskStatus.DONE, taskManager.getEpic(subTask1.getEpicId()).getStatus(), "Статус эпика не соответствует измененному");
    }

    // тесты управления свойствами эпиков
    @Test
    void manageEpicsTests() {
        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));

        assertEquals(TaskStatus.NEW, epic1.getStatus(), "Новый эпик должен быть в статусе NEW");

        SubTask subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId()));
        SubTask subTask2 = taskManager.createSubTask(new SubTask("Подзадача 2 Эпика 1", "Описание подзадачи 2 Эпика 1", epic1.getId()));

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

}