package com.company;

import org.opencv.core.Core;

import java.io.*;

public class Main {

    /**
     * Подключаем OpenCV
     */
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        try {
            /**
             * Инициализируем и запускаем сервер
             */
            System.out.println("Main: Start program");
            InitServer initServer = new InitServer();
            initServer.start();
        } catch (IOException ex) {
            /**
             * В случае ошибки или прерывания - останавливаем сервер
             */
            System.out.println("Main: Stop program");
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}

