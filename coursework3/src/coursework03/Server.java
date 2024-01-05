package coursework03;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int SERVER_PORT = 12345;
    private static final int MAX_FILE_DESCRIPTION_LENGTH = 100;
    private static final int MAX_FILE_CONTENT_SIZE = 10 * 1024 * 1024;

    private List<Connection> connections = new ArrayList<>();
    private static List<FileData> files = new ArrayList<>();

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Сервер запущен и ожидает подключений.");

            while (true) {

                Socket socket = serverSocket.accept();
                System.out.println("Подключился новый клиент.");


                Connection connection = new Connection(socket);
                connections.add(connection);


                Thread connectionThread = new Thread(connection);
                connectionThread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(Message message) {
        for (Connection connection : connections) {
            connection.sendMessage(message);
        }
    }

    private void removeConnection(Connection connection) {
        connections.remove(connection);
    }


    private class Connection implements Runnable {
        private final Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public Connection(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());

                while (true) {

                    Message message = (Message) inputStream.readObject();

                    if (message.getContent().startsWith("FILE_DESCRIPTION:")) {
                        String fileDescription = message.getContent().substring(17);
                        if (fileDescription.length() <= MAX_FILE_DESCRIPTION_LENGTH) {

                            message = (Message) inputStream.readObject();
                            byte[] fileContent = message.getContent().getBytes();

                            if (fileContent.length <= MAX_FILE_CONTENT_SIZE) {

                                String fileName = generateFileName();

                                File file = new File(fileName);
                                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                                    fileOutputStream.write(fileContent);
                                }

                                files.add(new FileData(fileName, fileDescription));

                                broadcastMessage(new Message("Новый файл: " + fileDescription));
                            } else {
                                System.out.println("Превышен максимальный размер файла.");
                            }
                        } else {
                            System.out.println("Превышена максимальная длина описания файла.");
                        }
                    } else if (message.getContent().startsWith("REQUEST_FILE:")) {
                        String requestedFileName = message.getContent().substring(13);
                        for (FileData fileData : files) {
                            if (fileData.getFileName().equals(requestedFileName)) {
                                sendMessage(new Message("FILE_DESCRIPTION:" + fileData.getDescription()));


                                File file = new File(fileData.getFileName());
                                byte[] fileContent = new byte[(int) file.length()];
                                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                                    fileInputStream.read(fileContent);
                                }
                                sendMessage(new Message(new String(fileContent)));
                                break;
                            }
                        }
                    } else {

                        broadcastMessage(message);
                    }
                }
            }
            catch (IOException | ClassNotFoundException e) {
                removeConnection(this);
                System.out.println("Соединение с клиентом разорвано.");
            }
        }

        public void sendMessage(Message message) {
            try {
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static class FileData {
        private final String fileName;
        private final String description;

        public FileData(String fileName, String description) {
            this.fileName = fileName;
            this.description = description;
        }

        public String getFileName() {
            return fileName;
        }

        public String getDescription() {
            return description;
        }
    }

    private static String generateFileName() {
        return "file_" + System.currentTimeMillis();
    }
}