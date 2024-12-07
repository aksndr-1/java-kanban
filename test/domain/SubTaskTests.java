package domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

import static org.junit.jupiter.api.Assertions.assertTrue;


class SubTaskTests {

    private static ITaskManager taskManager;

    @BeforeAll
    public static void init() {
        taskManager = Managers.getDefaultTaskManager();
    }

    // проверить, что объект Subtask нельзя сделать своим же эпиком
    @Test
    public void subTaskCouldNotBeSelfEpic() throws TasksIntersectsException {

        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic.getId()));

        subTask.setEpicId(subTask.getId());
        taskManager.updateSubTask(subTask);

        assertTrue(taskManager.getEpicSubtasks(epic.getId()).contains(subTask),"Подзадача не может быть сделана своим эпиком");
    }

}