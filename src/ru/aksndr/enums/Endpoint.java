package ru.aksndr.enums;

public enum Endpoint {
    UNKNOWN,
    // Task
    GET_TASK,
    CREATE_TASK,
    DELETE_TASK,
    GET_TASKS,
    DELETE_TASKS,
    // Subtask
    GET_SUBTASK,
    CREATE_SUBTASK,
    DELETE_SUBTASK,
    GET_SUBTASKS,
    DELETE_SUBTASKS,
    // Epic
    GET_EPIC,
    GET_EPIC_SUBTASKS,
    CREATE_EPIC,
    DELETE_EPIC,
    GET_EPICS,
    DELETE_EPICS,
    // History
    GET_HISTORY,
    // Priority
    GET_PRIORITY_TASKS
}