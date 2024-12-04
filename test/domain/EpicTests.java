package domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Epic;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;


class EpicTests {

    private static ITaskManager taskManager;

    @BeforeAll
    public static void init() {
        taskManager = Managers.getDefaultTaskManager();
    }

    @DisplayName("Проверить, что объект Epic нельзя добавить в самого себя в виде подзадачи")
    @Test
    public void epicCouldNotBeSelfSubTask() {

        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> taskManager.addSubTaskToEpic(epic, epic), "Эпик не может быть подзадачей");
    }

}