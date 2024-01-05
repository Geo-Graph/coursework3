package coursework03;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Подключение к серверу установлено.");

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            Thread messageReceiverThread = new Thread(() -> {
                try {
                    while (true) {
                        
                        Message message = (Message) inputStream.readObject();
                        System.out.println(message.getContent());
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                
                String input = scanner.nextLine();

                outputStream.writeObject(new Message(input));
                outputStream.flush();

                
                if (input.startsWith("UPLOAD_FILE:")) {
                    String filePath = input.substring(12);
                    File file = new File(filePath);
                    if (file.exists()) {
                        outputStream.writeObject(new Message("FILE_DESCRIPTION:" + file.getName()));
                        outputStream.flush();

                        
                        byte[] fileContent = new byte[(int) file.length()];
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            fileInputStream.read(fileContent);
                        }
                        outputStream.writeObject(new Message(new String(fileContent)));
                        outputStream.flush();
                    } else {
                        System.out.println("Файл не найден.");
                    }
                } else if (input.startsWith("DOWNLOAD_FILE:")) {
                    // Отправка запроса на получение файла с сервера
                    String fileName = input.substring(14);
                    outputStream.writeObject(new Message("REQUEST_FILE:" + fileName));
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
