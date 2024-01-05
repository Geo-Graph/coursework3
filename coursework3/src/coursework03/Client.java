package coursework03;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

//Этот класс представляет клиентскую часть приложения.
public class Client
{
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    //Здесь клиент устанавливает соединение с сервером, создается поток для приема сообщений от сервера и начинает отправляться сообщения на сервер.
    //Когда клиент вводит команду для загрузки файла (UPLOAD_FILE:), клиент отправляет описание файла и содержимое файла на сервер.
    //Если клиент вводит команду для скачивания файла (DOWNLOAD_FILE:), клиент отправляет запрос на получение файла на сервер.
    //Когда клиент получает сообщение от сервера, он выводит его на консоль.
    public static void main(String[] args)
    {
        try
        {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Подключение к серверу установлено.");

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            Thread messageReceiverThread = new Thread(() ->
            {
                try
                {
                    while (true)
                    {
                        // Получаем сообщение от сервера
                        Message message = (Message) inputStream.readObject();
                        System.out.println(message.getContent());
                    }
                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();

            Scanner scanner = new Scanner(System.in);
            while (true)
            {
                // Считывание введенной пользователем строки
                String input = scanner.nextLine();

                // Отправка сообщения на сервер
                outputStream.writeObject(new Message(input));
                outputStream.flush();

                // Если введена команда для загрузки файла
                if (input.startsWith("UPLOAD_FILE:"))
                {
                    String filePath = input.substring(12);
                    File file = new File(filePath);
                    if (file.exists())
                    {
                        // Отправка описания файла на сервер
                        outputStream.writeObject(new Message("FILE_DESCRIPTION:" + file.getName()));
                        outputStream.flush();

                        // Отправка содержимого файла на сервер
                        byte[] fileContent = new byte[(int) file.length()];
                        try (FileInputStream fileInputStream = new FileInputStream(file))
                        {
                            fileInputStream.read(fileContent);
                        }
                        outputStream.writeObject(new Message(new String(fileContent)));
                        outputStream.flush();
                    }
                    else
                    {
                        System.out.println("Файл не найден.");
                    }
                }
                else if (input.startsWith("DOWNLOAD_FILE:"))
                {
                    // Отправка запроса на получение файла с сервера
                    String fileName = input.substring(14);
                    outputStream.writeObject(new Message("REQUEST_FILE:" + fileName));
                    outputStream.flush();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}