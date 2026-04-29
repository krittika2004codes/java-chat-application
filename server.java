import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Server started... waiting for clients.");
        ServerSocket serverSocket = new ServerSocket(1234);

        // Thread to allow server to send messages too
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String msg = sc.nextLine();
                synchronized (clientWriters) {
                    for (PrintWriter writer : clientWriters) {
                        writer.println("Server: " + msg);
                    }
                }
            }
        }).start();

        // Accept clients
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected.");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            synchronized (clientWriters) {
                clientWriters.add(out);
            }
            new Thread(new ClientHandler(socket, out)).start();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg;
                while ((msg = in.readLine()) != null) {
                    System.out.println("Received: " + msg);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(msg);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}