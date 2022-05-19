import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader read;
    private PrintWriter write;
    private  boolean done = false;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1",9999);
            write = new PrintWriter(client.getOutputStream(),true);
            read = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String message;
            while ((message = read.readLine()) != null){
                System.out.println(message);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() throws IOException {
        done = true;
        try {
            read.close();
            write.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch (IOException e){
        }

    }
    class InputHandler implements Runnable{

        @Override
        public void run() {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            while (!done){
                try {
                    String message = inputReader.readLine();
                    if(message.equals("/quit")){
                        inputReader.close();
                        shutdown();
                    }else {
                        write.println(message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
