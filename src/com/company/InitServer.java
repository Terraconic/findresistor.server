package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Класс инициализизации сервера с установлением
 * соединений с клиентами
 */
public class InitServer {

    ServerSocket serverSocket = new ServerSocket(4444);

    /**
     * Конструктор класса. Инициализирует базу данных
     *
     * @throws IOException
     */
    public InitServer() throws IOException {

        if (!DataBase.isInitialization()) {
            DataBase.InitDB();
        }

        System.out.println("Init Server: Server started. Listening to the port 4444.");
    }

    /**
     * Метод в вечном цикле ожидает подключения клиента.
     * В случае подключения - создает нового клиента.
     */
    public void start() {

        ServerDispatcher serverDispatcher = new ServerDispatcher();
        serverDispatcher.start();

        while (true) {
            try {
                System.out.println("Init Server: Waiting for the client.");
                Socket socket = serverSocket.accept();
                Client client = new Client();
                client.setCharSocket(socket);

                Client buff = serverDispatcher.clientIsExist(client);

                /*Если на сервер уже есть пользователь с таким сокетом
                * то удаляем старую сессию*/
                if (buff != null) {
                    System.out.println("Init Server: Client is exist. Delete...");
                    if (serverDispatcher.deleteClient(buff))
                        System.out.println("Init Server: Old client is delete");
                }

                /*Создаем новую сессию */
                System.out.println("Init Server: Client is no exist");
                Receiver receiver = new Receiver(client, serverDispatcher);
                client.setReceiver(receiver);
                Sender sender = new Sender(client, serverDispatcher);
                client.setSender(sender);
                //ReceiverFile receiverFile = new ReceiverFile(client, serverDispatcher);
                //client.setReceiverFile(receiverFile);
                receiver.start();
                sender.start();
                serverDispatcher.addClient(client);


            } catch (IOException e) {
                System.out.println("Init Server error: Could not listen on port: 4444");
                e.printStackTrace();
                break;
            }
        }
    }
}
