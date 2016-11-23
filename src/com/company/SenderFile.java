package com.company;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Класс реализует метод для отправки изображения клиенту
 */
public class SenderFile extends Thread {

    /**
     * Диспетчер приемникка
     */
    private ServerDispatcher serverDispatcher;

    /**
     * Клиент сендера
     */
    private Client client;
    /**
     * Изображение для отправки
     */
    private File imageFile;

    /**
     * Время работы алгоритма
     */
    long time;

    /**
     * Потоки для обработки и отправки изображения
     */
    private ObjectOutputStream oos;
    private BufferedInputStream bis;


    /**
     * Конструктор класса
     *
     * @param client - клиент
     * @param file   - файл с обработанным изображением
     * @param Time   - время работы алгоритма в миллисекундах
     */
    SenderFile(Client client, File file, long Time, ServerDispatcher serverDispatcher) throws IOException {
        this.client = client;
        this.imageFile = file;
        this.time = Time;
        this.serverDispatcher = serverDispatcher;
        oos = new ObjectOutputStream(client.getCharSocket().getOutputStream());
        bis = new BufferedInputStream(new FileInputStream(imageFile));
    }


    /**
     * Метод-поток сендера
     */
    @Override
    public void run() {

        /*Переводим миллисекунды в секунды*/
        timeToSecond();
        System.out.println("Sender File: Try sent image file to client");
        byte[] bytes = new byte[(int) imageFile.length()];

        try {
            bis.read(bytes, 0, bytes.length);

            /*Отправляем файл клиенту*/
            oos.writeObject(bytes);
            oos.flush();

            if (imageFile.delete())
                System.out.println("Sender File: Image was delete");

            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException ex) {
                System.out.println("Sender File: Error");
            }

            System.out.println("Sender File: Time: " + String.valueOf(time) + " second(s)");


            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                System.out.println("Sender File: Errror");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод переводит время из миллисекунд в секунды
     */
    private void timeToSecond() {
        time /= 1000;
    }
}