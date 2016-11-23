package com.company;

import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

/**
 * Класс описывает клиента сервера
 */
public class Client {

    /*Монитор для потоков*/
    public static final Object monitor = new Object();

    /*Сокет клиента для символов*/
    private Socket charSocket;

    /*Сокет клиента для файлов*/
    private Socket objectSocket;

    /*Идентификатор клиента*/
    private UUID uuid;

    /*Приемник и передатчик сообщений/файла клиента*/
    private Receiver receiver;
    private Sender sender;
    private ReceiverFile receiverFile;
    private SenderFile senderFile;

    /*Управляющий флаг*/
    public boolean flag = true;

    /**
     * Конструктор класса Client
     */
    public Client() {
        uuid = UUID.randomUUID();
    }

    /**
     * Метод возвращает сокет клиента
     *
     * @return - сокет клиента
     */
    public Socket getCharSocket() {
        return charSocket;
    }

    public void setCharSocket(Socket charSocket) {
        this.charSocket = charSocket;
    }

    public InetAddress getClientAddress() {
        return charSocket.getInetAddress();
    }

    /**
     * Метод возвращает идентификатор клиента
     *
     * @return - идентификатор клиента
     */
    public UUID getUuid() {
        return uuid;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public Sender getSender() {
        return sender;
    }

    public void setReceiverFile(ReceiverFile receiverFile) {
        this.receiverFile = receiverFile;
    }

    public void setSenderFile(SenderFile senderFile) {
        this.senderFile = senderFile;
    }

    public ReceiverFile getReceiverFile() {
        return receiverFile;
    }

    public SenderFile getSenderFile() {
        return senderFile;
    }

    public Socket getObjectSocket() {
        return objectSocket;
    }

    public void setObjectSocket(Socket objectSocket) {
        this.objectSocket = objectSocket;
    }
}
