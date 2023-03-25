import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.util.Date;
import java.util.Scanner;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public Client() {}
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

        try {
            while (true) {
            System.out.println(in.readLine());
            inMessage = scanner.nextLine();
            out.println(inMessage);
            if (inMessage.equals("quit"))
                break;
                while(true){
                    System.out.println(in.readLine());
                    String date = scanner.nextLine();
                    out.println(date);
                    String info = in.readLine();
                    System.out.println(info);
                    if(info.equals("Poprawna data!"))
                        break;
                }
            }
            stopClient();
        }
        catch (IOException e) {
            System.out.println("Nie udało się połączyć z hostem!");
            System.exit(1);
        }
        }


    public void stopClient() {
        try {
            in.close();
            out.close();
            if(!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
