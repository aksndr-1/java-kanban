package domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.exceptions.TaskNotFoundException;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SubTaskTests {

    private static ITaskManager taskManager;

    @BeforeAll
    public static void init() {
        taskManager = Managers.getDefaultTaskManager();
    }

    @DisplayName("Проверка, что для подзадачи нельзя сделать эпиком несуществующий рабочий элемент")
    @Test
    public void subTaskCouldNotBeSelfEpic() throws TasksIntersectsException {

        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic.getId()));

        subTask.setEpicId(888);
        assertThrows(TaskNotFoundException.class,
                () -> taskManager.updateSubTask(subTask), "Для подзадачи в качестве эпика указан несуществующий рабочий элемент");
    }
}