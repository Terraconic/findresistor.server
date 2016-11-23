package com.company;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMatchTemplate;

/**
 * Класс описывает приемник файла от клиента
 * с обработкой сообщения
 */
public class ReceiverFile extends Thread {

    /**
     * Клиент приемника фалйа
     */
    private Client client;

    /**
     * Диспетчер приемникка
     */
    private ServerDispatcher serverDispatcher;

    /**
     * Файл и поток для сохранения изображения
     */
    private File file;
    private ObjectInputStream objectInputStream;


    /**
     * Таймер работы алгоритма
     */
    private long timer;


    /**
     * Конструктор класса
     *
     * @param client - клиент приемника
     */
    public ReceiverFile(Client client, ServerDispatcher serverDispatcher) throws IOException {
        this.client = client;
        this.serverDispatcher = serverDispatcher;
        this.objectInputStream = new ObjectInputStream(client.getCharSocket().getInputStream());
    }

    /**
     * Основной метод класса - получение файла
     */
    @Override
    public void run() {
        try {
            /*Задаем имя файла, полученного от клиента*/
            String imageName = "image" + client.getUuid().toString() + ".jpg";

            file = new File(imageName);

            byte[] bytes;
            FileOutputStream fos;

            try {
                bytes = (byte[]) objectInputStream.readObject();
                fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.close();

                findResistor(imageName);

                File schemeImage = new File(imageName);
                if (schemeImage.exists()) {
                    /*Отправляет результат клиенту*/
                    SenderFile senderFile = new SenderFile(client, schemeImage, timer, serverDispatcher);
                    client.setSenderFile(senderFile);
                    client.getSenderFile().start();
                    client.flag = true;

                } else {
                    System.out.println("Receiver File: Error send image file");
                    client.getSender().sendMessageToClient("ERROR");
                }


                if (file.delete())
                    System.out.println("Receiver File: File from client was delete");
                client.flag = true;


            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


    }

    /** Функция обрабатывает полученное от клиента изображение, находит на нем резисторы
     *
     * @param imageName Имя изображения, полученного от клиента
     */
    public void findResistor(String imageName) {
        for (int j = 1; j <= 2; j++) {
            String Resistor = "Resistors/" + Integer.toString(j) + ".jpg";
            for (int i = 0; i < 30; i++) {
                IplImage src = cvLoadImage(imageName);
                IplImage tmp = cvLoadImage(Resistor);
                IplImage result = cvCreateImage(
                        cvSize(src.width() - tmp.width() + 1,
                                src.height() - tmp.height() + 1), IPL_DEPTH_32F, 1);
                // Match Template Function from OpenCV
                cvMatchTemplate(src, tmp, result, 5);
                double[] min_val = new double[2];
                double[] max_val = new double[2];
                CvPoint minLoc = new CvPoint();
                CvPoint maxLoc = new CvPoint();
                cvMinMaxLoc(result, min_val, max_val, minLoc, maxLoc, null);
                cvNormalize(result, result, 1, 0, CV_MINMAX, null);
                CvPoint point = new CvPoint();
                point.x(maxLoc.x() + tmp.width());
                point.y(maxLoc.y() + tmp.height());
                cvRectangle(src, maxLoc, point, CvScalar.RED, 4, 8, 0);
                cvSaveImage(imageName, src);
                cvWaitKey(0);
                // освобождаем ресурсы
                cvReleaseImage(src);
                cvReleaseImage(tmp);
                cvReleaseImage(result);
                cvDestroyAllWindows();
            }
        }
    }
}