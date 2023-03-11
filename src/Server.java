import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server implements Runnable{
    private LinkedList<clientHandler> clients;
    private ServerSocket server;
    private boolean isRunning;
    public Server(){
        clients = new LinkedList<>();
        isRunning = true;
    }
    @Override
    public void run(){
        try{
            server = new ServerSocket(1234); //tworzenie serwera
            while (isRunning) {
                Socket newClient = server.accept(); //dodanie uzytkownika do serwera?
                clients.add(new clientHandler(newClient));
            }

        } catch (IOException e){
            //e.printStackTrace();
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
                writer.println("Podaj wiadomość: ");
                System.out.println(reader.readLine());
                broadcast("HEJ!!!");
            } catch (IOException e){
                //e.printStackTrace();
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
}
