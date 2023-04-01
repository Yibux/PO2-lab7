import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
//import java.time.LocalTime;
import java.util.Date;
import java.util.Scanner;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    boolean isRunning;
    public int a;
    public String processMessage="";

    public Client() {
        isRunning = true;
        a = 4;
    }
    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 1234);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
            System.out.println(in.readLine());
        }
        catch (IOException e) {
            System.out.println("Nie udało się połączyć z hostem!");
            System.exit(1);
        }
        Scanner scanner = new Scanner(System.in);
        String inMessage ="";
        Thread getter = new Thread(new Sender(scanner, out));
        getter.start();

        try {
            while (isRunning) {
                inMessage = in.readLine();
                if(inMessage == null)
                    break;
                System.out.println(inMessage);
            }
            System.out.println("koniec pętli");
        }
            catch (IOException e) {
//                System.out.println("Nie udało się połączyć z hostem!");
//                System.exit(1);
            }
        }


    public void stopClient() {
        isRunning = false;
        try {
            in.close();
            out.close();
            if(!client.isClosed()) {
                client.close();
                System.out.println("koniec klienta");
            }
        } catch (IOException e) {
            //ignore
        }
    }

    class Sender implements Runnable{
        Scanner scanner;
        PrintWriter out;
        public Sender(Scanner scanner, PrintWriter out){
            this.scanner = scanner;
            this.out = out;
        }
        @Override
        public void run() {
            while(isRunning){
                processMessage = scanner.nextLine();
                out.println(processMessage);
                if(processMessage.equals("quit")) {
                    stopClient();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
