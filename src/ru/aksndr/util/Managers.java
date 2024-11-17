package ru.aksndr.util;

import ru.aksndr.exceptions.BackupFileException;
import ru.aksndr.service.IHistoryManager;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.service.impl.FileBackedTaskManager;
import ru.aksndr.service.impl.InMemoryHistoryManager;
import ru.aksndr.service.impl.InMemoryTaskManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Managers {

    public static ITaskManager getDefaultTaskManager() {
        return new InMemoryTaskManager();
    }

    public static ITaskManager getFileBackedTaskManager() {
        Path path = Paths.get("./resources/storage.csv");
        File file = new File(path.toUri());
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new BackupFileException("Ошибка загрузки файла бекапа данных таск треккера");
            }
        }
        return new FileBackedTaskManager(file);
    }

    public static IHistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager();
    }
}