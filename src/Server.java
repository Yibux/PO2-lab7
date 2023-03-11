import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private LinkedList<clientHandler> clients;
    private ServerSocket server;
    private boolean isRunning;
    private ExecutorService threadPool;
    private int hostSocket;
    public Server(){
        clients = new LinkedList<>();
        isRunning = true;
        hostSocket = 1234;
    }
    @Override
    public void run(){
        try{
            server = new ServerSocket(hostSocket); //tworzenie serwera
            System.out.println("Serwer dziala na socketcie: " + hostSocket);
            threadPool = Executors.newCachedThreadPool();
            while (isRunning) {
                Socket newClient = server.accept(); //dodanie uzytkownika do serwera?
                System.out.println("ktoś się dostał");
                clientHandler threadHandler = new clientHandler(newClient);
                clients.add(threadHandler);
                threadPool.execute(threadHandler);
            }

        } catch (IOException e){
            stopServer();
        }
    }
    public void broadcast(String message) {
        for (clientHandler ch : clients){
            if(ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void stopServer() {
        isRunning = false;
        if(!server.isClosed()){
            try{
                server.close();
                for(clientHandler e : clients) {
                    e.stopClient();
                }
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }
    }

    class clientHandler implements Runnable{
        private Socket newClient;
        private BufferedReader reader;
        private PrintWriter writer;
        public clientHandler(Socket newClient){
            this.newClient = newClient;
        }
        @Override
        public void run(){
            try{
                writer = new PrintWriter(newClient.getOutputStream(), true); //dodanie możliwości wysyłania czegoś do klienta
                reader = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
                String message = "Podaj wiadomość: ";
                while (true){
                    writer.println(message);
                    message = reader.readLine();
                    System.out.println(message);
                    if(message.startsWith("quit")){
                        stopClient();
                        break;
                    }
                    broadcast("HEJ!!!");
                }

            } catch (IOException e){
                stopClient();
            }
        }

        public void sendMessage(String message){
            writer.println(message);
        }

        public void stopClient() {

            if(!newClient.isClosed()){
                try {
                    reader.close();
                    writer.close();
                    newClient.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

}
