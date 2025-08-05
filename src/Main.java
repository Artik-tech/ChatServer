import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

class ChatServer {
    private static final int PORT = 12345; // порт сервера
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Чат-сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для отправки сообщения всем клиентам, кроме отправителя
    public static void broadcast(String message, ClientHandler excludeClient) {
        for (ClientHandler client : clients) {
            if (client != excludeClient) {
                client.sendMessage(message);
            }
        }
    }

    // Внутренний класс для обработки клиента
    static class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Метод для отправки сообщения клиенту
        public void sendMessage(String message) {
            out.println(message);
        }

        @Override
        public void run() {
            String name = "Клиент " + socket.getRemoteSocketAddress();
            sendMessage("Добро пожаловать! Введите сообщение. Для выхода напишите 'exit'.");
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit")) {
                        sendMessage("Вы вышли из чата. До свидания!");
                        break; // выходим из цикла при команде 'exit'
                    }
                    String formattedMessage = name + ": " + message;
                    System.out.println(formattedMessage); // вывод в консоль сервера
                    broadcast(formattedMessage, this);
                }
            } catch (IOException e) {
                System.out.println("Клиент отключился: " + socket.getRemoteSocketAddress());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) { /* игнорируем */ }
                clients.remove(this);
            }
        }
    }
}