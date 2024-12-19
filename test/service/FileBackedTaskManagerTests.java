package service;

import org.junit.jupiter.api.*;
import ru.aksndr.service.impl.FileBackedTaskManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTests extends TaskManagerTest{

    static File file;

    @BeforeAll
    public static void initManger() throws IOException {
        file = File.createTempFile("storage", ".csv");
        taskManager = FileBackedTaskManager.load(file);
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
        assertEquals(linesList.get(1), "6,TASK,Задача 1,NEW,Описание 1,,2024-12-01T10:20,PT15M\n");
        assertEquals(linesList.get(2), "7,EPIC,Эпик 1,NEW,Описание эпика 1,,2024-12-02T10:50,PT30M\n");
        assertEquals(linesList.get(3), "8,SUBTASK,Подзадача 1 Эпика 1,NEW,Описание подзадачи 1 Эпика 1,7,2024-12-05T10:20,PT15M\n");
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