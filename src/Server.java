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
                System.out.println("Dolączył: " + newClient.getInetAddress().getHostName());
                clientHandler threadHandler = new clientHandler(newClient);
                clients.add(threadHandler);
                threadPool.execute(threadHandler);
            }

        } catch (IOException e){
            stopServer();
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
        private final ExecutorService threadPool;

        public clientHandler(Socket newClient){
            threadPool = Executors.newCachedThreadPool();
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
                    if(message == null || message.equals("quit"))
                        break;
                    long msDiff = 0;
                    LocalTime currentTime = LocalTime.now().withNano(0);
                    out.println("Podaj godzinę notyfikacji późniejszą niż "+ currentTime +" w formacie hh:mm:ss : ");
                    String formattedTime = in.readLine();
                    LocalTime time;
                    try{
                        time = LocalTime.parse(formattedTime);
                        Duration duration = Duration.between(currentTime,time);
                        msDiff = duration.toSeconds();
                        if(msDiff<0){
                            throw new UjemnyCzasException();
                        }
                        out.println("Poprawana data!");
                        System.out.println(message+" "+time);
                        System.out.println("Różnica czasów: "+ msDiff);

                    } catch (DateTimeException e){
                        out.println("Podano niepoprawny format godziny!");
                        continue;
                    } catch (UjemnyCzasException e){
                        out.println(e.getMessage());
                        continue;
                    }
                    threadPool.execute(new Notification(message, out, time));
                }

            } catch (IOException e){
                stopClient();
            }
        }

        public void stopClient() {

            if(!newClient.isClosed()){
                try {
                    out.close();
                    in.close();
                    newClient.close();
                    threadPool.close();
                    System.out.println("Uczestnik opuścił czat");
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    class Notification implements Runnable{
        String message;
        LocalTime time;
        PrintWriter out;

        public Notification(String message, PrintWriter out, LocalTime time){
            this.out = out;
            this.message = message;
            this.time = time;
        }

        @Override
        public void run(){
            while(true) {
                Duration duration = Duration.between(LocalTime.now().withNano(0),time);
                double msDiff = duration.toSeconds();
                if(msDiff <= 0)
                    break;
            }
            out.println("Przypomnienie o czasie " + time + " z wiadomością: " + message);
            System.out.println("Przypomnienie z wiadomością: " + message);
            System.out.println("Wysłano o czasie: " + LocalTime.now().withNano(0));
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

}
