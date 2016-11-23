package com.company;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * Класс реализует метод отправки сообщений для клиента
 */
public class Sender extends Thread {

    /**
     * Очередь сообщений
     */
    private Vector<String> mMessageQueue = new Vector<>();

    private ServerDispatcher serverDispatcher;

    private Client client;

    private PrintWriter printWriter;

    public Sender(Client client, ServerDispatcher serverDispatcher) throws IOException {
        this.client = client;
        this.serverDispatcher = serverDispatcher;
        printWriter = new PrintWriter(new OutputStreamWriter(client.getCharSocket().getOutputStream()));
    }

    /**
     * Метод добавляет сообщение в очередь сообщений
     */
    public synchronized void sendMessage(String message) {
        mMessageQueue.add(message);
        notify();
    }

    /**
     * Метод возвращает сообщение из очереди
     */
    private synchronized String getNextMessageFromQueue() throws InterruptedException {
        while (mMessageQueue.size() == 0)
            wait();
        String message = mMessageQueue.get(0);
        mMessageQueue.removeElementAt(0);
        return message;
    }

    /**
     * Метод отправляет сообщение клиенту
     */
    public void sendMessageToClient(String aMessage) {
        printWriter.println(aMessage);
        printWriter.flush();
    }

    /**
     * В потоке сообщения отправляются своим клиентам
     */
    public void run() {
        try {
            while (!isInterrupted()) {
                String message = getNextMessageFromQueue();
                sendMessageToClient(message);
            }
        } catch (Exception e) {
            client.getReceiver().interrupt();
            e.printStackTrace();
        }
    }
}
