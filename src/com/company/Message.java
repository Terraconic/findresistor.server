package com.company;

/**
 * Класс описывает структуру пары сообщения-клиента
 */
public class Message {

    private Client client;

    private String message;

    public Message(Client client, String message) {
        setClient(client);
        setMessage(message);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
