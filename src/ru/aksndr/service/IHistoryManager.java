package ru.aksndr.service;

import ru.aksndr.domain.Task;

import java.util.Collection;
import java.util.List;

public interface IHistoryManager {

    /**
    * Добавить задачу в историю просмотров
    */
    void addHistory(Task task);

    /**
     * Добавить набор задач в историю просмотров
     */
    void addHistory(Collection<? extends Task> values);

    /**
     * Получить историю просмотра задачи
     * @return  История росмотров задачи
     */
    List<Task> getHistory();

    /**
     * Получить максимальное количество просмотров
     * @return Максимальное количество просмотров
     */
    short getLimit();
}