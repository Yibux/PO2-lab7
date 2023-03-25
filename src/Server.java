import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.LinkedHashMap;
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
                Socket newClient = server.accept(); //dodanie uzytkownika do serwera
                System.out.println("Dolączył: " + newClient.getInetAddress());
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
        LinkedList<Notification> notifications;
        private final Socket newClient;
        private PrintWriter out;
        private BufferedReader in;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        public clientHandler(Socket newClient){
            this.newClient = newClient;
        }
        @Override
        public void run(){
            try{
                out = new PrintWriter(new OutputStreamWriter(newClient.getOutputStream()), true); //dodanie możliwości wysyłania wiadomości do klienta
                in = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
                notifications = new LinkedList<>();
                out.println("Witaj. Podążaj zgodnie z poleceniami lub wpisz quit jeśli chcesz opuścić czat. ");
                while (true){
                    out.println("Podaj wiadomość: ");
                    String message = in.readLine();
                    if(message.equals("quit"))
                        break;
                    while(true){
                        LocalTime currentTime = LocalTime.now().withNano(0);
                        out.println("Podaj godzinę notyfikacji późniejszą niż "+ currentTime +" w formacie hh:mm:ss : ");
                        String formattedTime = in.readLine();
                        LocalTime time;
                        try{
                            time = LocalTime.parse(formattedTime);
                            Duration duration = Duration.between(currentTime,time);
                            long msDiff = duration.toSeconds();
                            if(msDiff<0){
                                throw new UjemnyCzasException();
                            }
                            out.println("Poprawana data!");
                            System.out.println(message+" "+time);
                            System.out.println("Różnica czasów: "+ msDiff);
                            break;
                        } catch (DateTimeException e){
                            out.println("Podano niepoprawny format godziny!");
                        } catch (UjemnyCzasException e){
                            out.println(e.getMessage());
                        }
                    }
                }

            } catch (IOException e){
                stopClient();
            }
        }

        public void sendMessage(String message){
            out.println(message);
        }


        public void stopClient() {

            if(!newClient.isClosed()){
                try {
                    out.close();
                    in.close();
                    newClient.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    class Notification{
        public Notification(String message, long delay){

        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

}
