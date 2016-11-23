package com.company;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Класс хранит список клиентов
 */
public class ServerDispatcher extends Thread {

    /**
     * Очередь пар клиент-сообщение
     */
    private ArrayList<Message> messageQueue = new ArrayList<>();


    /**
     * ArrayList Client - список клиентов на сервере
     */
    private static ArrayList<Client> clientList = new ArrayList<>();

    /**
     * Метод добавляет клиента в список клиентов
     *
     * @param client - клиент
     */
    public synchronized void addClient(Client client) {

        /*Проверка на дурака :)*/
        if (client != null) {

            /*Добавляем клиента и возвращаем результат*/
            int index = clientList.size();
            clientList.add(index, client);

            System.out.println("Server Dispatcher: Client " + client.getCharSocket() + " add to clients list ");
            System.out.println("Server Dispatcher: Client UUID: " + clientList.get(index).getUuid().toString());
        }
    }

    /**
     * Метод добавляет пару клиент-сообщение в очередь для отправки
     */
    public synchronized void dispatchMessage(Client client, String aMessage) {
        Message message = new Message(client, aMessage);
        messageQueue.add(message);
        notify();
    }

    /**
     * Метод запускает приемник файла от клиента
     *
     * @param client - клиент, который передает файл
     */
    public synchronized void getFile(Client client) {

        /**
         * Для избежания взаимной блокировки потоков используется синхронизация
         */
        synchronized (Client.monitor) {
            /*Запускаем приемник файла*/
            client.getReceiverFile().start();

            /*Ожидаем завершения приема и передачи файла клиента*/
            while (true) {

                try {
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("Server Dispatcher: Monitor not notify");
                } catch (InterruptedException ex) {
                    System.out.println("Server Dispatcher: Error");
                }

                if (client.flag) {
                    System.out.println("Server Dispatcher: Monitor notify");
                    Client.monitor.notify();
                    break;
                }
            }
        }
    }


    /**
     * Метод достает пару клиент-сообщение из очереди
     */
    private synchronized Message getNextMessageFromQueue() throws InterruptedException {
        while (messageQueue.size() == 0)
            wait();
        Message message = messageQueue.get(0);
        messageQueue.remove(0);
        return message;
    }

    /**
     * Метод отправляет сообщение клиенту
     */
    private synchronized void sendMessageToClient(Message message) {
        message.getClient().getSender().sendMessage(message.getMessage());
    }

    /**
     * Метод в вечном цикле управляет передачей сообщений клиентам
     */
    public void run() {

        while (true) {
            try {
                Message message = getNextMessageFromQueue();
                sendMessageToClient(message);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                break;
            }
        }
    }

    /**
     * Метод проверяет существование клиента в списке клиентов
     *
     * @param client - клиент
     * @return true - если клиент существует, иначе - false
     */

    public synchronized Client clientIsExist(Client client) {

        /*Если список клиентов пустой*/
        if (clientList.size() == 0)
            return null;

        /*Поиск клиента*/
        for (Client clients : clientList) {
            if (clients.getCharSocket().getInetAddress().equals(client.getClientAddress())) {
                //System.out.println("ServerDispatcher: Client " + socket + " is exist in list of clients");
                return clients;
            }
        }

        return null;
    }

    /**
     * Метод удаляет клиента из списка клиентов
     *
     * @param client -  клиента
     * @return true - если клиент удален, иначе - false
     */
    public synchronized boolean deleteClient(Client client) {

        int index = clientList.indexOf(client);

        if (index != -1) {
            /*Прерываем работу приемника клиента*/
            client.getReceiver().interrupt();
            clientList.remove(client);
            System.out.println("ServerDispatcher: Client " + client.getCharSocket() + " delete from list of clients");
            return true;
        } else {
            System.out.println("ServerDispatcher: Error delete from list of clients");
            return false;
        }
    }

    /**
     * Метод удаляет всех клиентов с сервера
     */
    public synchronized void deleteClients() {
        clientList.clear();
        System.out.println("ServerDispatcher: All clients delete from server");
    }
}
