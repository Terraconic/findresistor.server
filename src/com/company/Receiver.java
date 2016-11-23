package com.company;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Класс - приемник сообщений от клиента
 */
public class Receiver extends Thread {

    /**
     * Строка-ключ для управлением взаимодействием между клиентом и сервером
     */
    private final String KEY_AUTHORIZATION = "KEY_AUTHORIZATION";
    private final String KEY_REGISTRATION = "KEY_REGISTRATION";
    private final String KEY_OFFLINE = "KEY_OFFLINE";
    private final String KEY_FILE = "KEY_FILE";
    private final String SUCCESS_A = "SUCCESS_AUTHORIZATION";
    private final String SUCCESS_R = "SUCCESS_REGISTRATION";
    private final String ERROR_AR = "WRONG_LOGIN_OR_PASSWORD";

    /**
     * Поток считывания символов от клиента
     */
    private BufferedReader bufferedReader;

    /**
     * Клиент приемника
     */
    private Client client;

    /**
     * Диспетчер приемнкиа
     */
    private ServerDispatcher serverDispatcher;

    /**
     * Количество циклов приёма сообщения
     */
    private int timer = 0;

    /**
     * Конструктор класса
     *
     * @param client - Объект класса Client
     */
    public Receiver(Client client, ServerDispatcher serverDispatcher) throws IOException {
        this.client = client;
        this.serverDispatcher = serverDispatcher;
        bufferedReader = new BufferedReader(new InputStreamReader(client.getCharSocket().getInputStream()));
    }

    /**
     * Метод-поток обработки действий клиента
     */
    @Override
    public void run() {
        String message;

        try {
            /*Пока поток не прерван*/
            while (!isInterrupted()) {

                /*Если время жизни клиента позволяет*/
                if (!isAliveClient())
                    interrupt();

                /*Считываем сообщение от клиента*/
                synchronized (Client.monitor) {
                    if (bufferedReader.ready()) {
                        message = bufferedReader.readLine();

                        /*Если сообщение не пустое*/
                        if (message != null) {
                            /*Обрабатываем*/
                            processingMessage(message);
                        }
                    }
                }

            }
        } catch (IOException ex) {
            System.out.println("Receiver: Problem in message reading or client offline");
            serverDispatcher.deleteClient(client);
            ex.printStackTrace();
        }

        System.out.println("Receiver: Run stop");
    }

    /**
     * Метод отключает клиента от сервера, если он не активен 60 секунд
     *
     * @return - true - если оставшееся время больше 0, иначе - false
     */
    private boolean isAliveClient() {
        try {
            if (timer == 60) {
                /*Отпарвляем клиенту сообщение, что он не активен*/
                System.out.println("Receiver: Client offline");
                serverDispatcher.dispatchMessage(client, "OFFLINE");
                /*И удаляем сессию с сервера*/
                serverDispatcher.deleteClient(client);
                return false;
            } else {

                TimeUnit.SECONDS.sleep(1);
                /*Каждые 10 секунд выводим оставшееся время*/
                if (timer % 10 == 0) {
                    System.out.println("Receiver: Waiting message from client " + client.getCharSocket().getInetAddress());
                    System.out.println("Receiver: Time: Left " + (60 - timer) + " second");
                }

                /*Добавляем секунду*/
                timer++;
            }
        } catch (InterruptedException ie) {
            System.out.println("Receiver: Interrupted run of client");
            return false;
        }

        return true;
    }

    /**
     * Метод обновляет таймер
     */
    private void refreshTimer() {
        timer = 0;
        System.out.println("Receiver: Timer refresh ");
    }

    /**
     * Метод обрабатываем полученное сообщение от клиента
     *
     * @param message - сообщение от клиента
     */
    public void processingMessage(String message) {

        String userName;
        String password;

        switch (message) {

            /**
             * Авторизация клиента
             */
            case KEY_AUTHORIZATION: {

                try {
                    userName = bufferedReader.readLine();
                    password = bufferedReader.readLine();
                    refreshTimer();
                } catch (IOException ex) {
                    System.out.println("Receiver: Error authorization or registration");
                    serverDispatcher.deleteClient(client);
                    ex.printStackTrace();

                    break;
                }

                System.out.println("Receiver: New client. User: " + userName + " Password: " + password);

                if (Authorization.AuthorizationUser(userName, password)) {
                    System.out.println("Receiver: Authorization success");
                    serverDispatcher.dispatchMessage(client, SUCCESS_A);
                } else {
                    System.out.println("Receiver: Authorization not success");
                    serverDispatcher.dispatchMessage(client, ERROR_AR);
                }

                break;
            }

            /**
             * Регистрация клиента
             */
            case KEY_REGISTRATION: {

                try {
                    userName = bufferedReader.readLine();
                    password = bufferedReader.readLine();
                    refreshTimer();
                } catch (IOException ex) {
                    System.out.println("Receiver: Error authorization or registration");
                    serverDispatcher.deleteClient(client);
                    ex.printStackTrace();

                    break;
                }

                System.out.println("Receiver: New client. User: " + userName + " Password: " + password);

                if (Registration.RegistrationUser(userName, password)) {
                    System.out.println("Receiver: Registration complete");
                    serverDispatcher.dispatchMessage(client, SUCCESS_R);
                } else {
                    serverDispatcher.dispatchMessage(client, ERROR_AR);
                    System.out.println("Receiver: Authorization not success");
                }
                break;
            }

            /**
             * Отключение клиента от сервера
             */
            case KEY_OFFLINE: {

                System.out.println("Receiver: Close user connect");
                serverDispatcher.deleteClient(client);

                break;
            }

            /**
             * Получение файла от клиента
             */
            case KEY_FILE: {

                refreshTimer();
                System.out.println("Receiver: Get the file");

                /*Создаем приемник файла для клиента*/
                try {
                    ReceiverFile receiverFile = new ReceiverFile(client, serverDispatcher);
                    client.setReceiverFile(receiverFile);
                    client.flag = false;
                } catch (IOException ex) {
                    System.out.println("Receiver: Error create receiver file");
                }

                /*Запускаем приемник файла в другом потоке*/
                serverDispatcher.getFile(client);

                break;
            }


            default: {
                client.getSender().sendMessage(ERROR_AR);
                System.out.println("Receiver: Error authorization or registration");
                break;
            }
        }
    }


}
