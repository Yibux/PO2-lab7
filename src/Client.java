import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isRunning;
    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 1234);
            writer = new PrintWriter(client.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inMessage;
            while ((inMessage = reader.readLine()) != null) {
                System.out.println(inMessage);
            }
        }
        catch (IOException e) {
            stopClient();
        }
    }

    public void stopClient() {
        isRunning = false;
        try {
            reader.close();
            writer.close();
            if(!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }

    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while(isRunning) {
                    String message = inputReader.readLine();
                    writer.println(message);
                    if(message.equals("quit")) {
                        inputReader.close();
                        stopClient();
                    }
                }
            }  catch (IOException e) {
                stopClient();
            }
        }
    }
}
