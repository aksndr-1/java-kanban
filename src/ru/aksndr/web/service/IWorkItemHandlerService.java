package ru.aksndr.web.service;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface IWorkItemHandlerService {

    void createOrUpdate(HttpExchange exchange) throws IOException;

    void read(HttpExchange exchange) throws IOException;

    void delete(HttpExchange exchange) throws IOException;
}