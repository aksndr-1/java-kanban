package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.service.impl.FileBackedTaskManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTests {

    private ITaskManager taskManager;
    private File file;

    @BeforeEach
    public void beforeEach() throws IOException {
        file = File.createTempFile("storage", ".csv");
        taskManager = FileBackedTaskManager.load(file);
        taskManager.createTask(new Task("Задача 1", "Описание 1", LocalDateTime.of(2024, 12, 1, 10, 20), Duration.ofMinutes(15)));
        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId(), LocalDateTime.of(2024, 12, 2, 10, 20), Duration.ofMinutes(15)));
    }

    @AfterEach
    public void afterEach() {
        file.deleteOnExit();
    }

    @DisplayName("Проверить, что данные загруженные из файла соответствуют содержимому менеджера")
    @Test
    void loadedBackupDataIsEqualsInMemoryaluesTest() {
        FileBackedTaskManager btm = FileBackedTaskManager.load(file);
        assertEquals(btm.getAllEpics().stream().findFirst(), taskManager.getAllEpics().stream().findFirst());
        assertEquals(btm.getAllSubTasks().stream().findFirst(), taskManager.getAllSubTasks().stream().findFirst());
        assertEquals(btm.getAllTasks().stream().findFirst(), taskManager.getAllTasks().stream().findFirst());
    }

    @DisplayName("Проверка структуры файла бекапа данных")
    @Test
    void backupFileStructureTest() throws IOException {

        List<String> linesList= new ArrayList<>();

        Reader reader = new FileReader(file, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
            while (br.ready()) {
                String line = br.readLine();
                linesList.add(line + "\n");
            }
        assertEquals(linesList.get(0), "id,type,name,status,description,epic,startTime,duration\n");
        assertEquals(linesList.get(1), "2,TASK,Задача 1,NEW,Описание 1,,2024-12-01T10:20,PT15M\n");
        assertEquals(linesList.get(2), "3,EPIC,Эпик 1,NEW,Описание эпика 1,,2024-12-02T10:20,PT15M\n");
        assertEquals(linesList.get(3), "4,SUBTASK,Подзадача 1 Эпика 1,NEW,Описание подзадачи 1 Эпика 1,3,2024-12-02T10:20,PT15M\n");
    }

    @DisplayName("Очистка задач и проверка пустоты хранилища")
    @Test
    void deleteAllWorkItemsTest() throws IOException { // после удаления задач всех типов в истории должна остаться только одна строка - оглавление
        taskManager.deleteAllTasks();
        taskManager.deleteAllSubTasks();
        taskManager.deleteAllEpics();
        List<String> linesList = new ArrayList<>();
        Reader reader = new FileReader(file, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
            while (br.ready()) {
                String line = br.readLine();
                linesList.add(line + "\n");
            }

        assertEquals(linesList.getFirst(), "id,type,name,status,description,epic,startTime,duration\n");
        assertEquals(1, linesList.size());
    }

}