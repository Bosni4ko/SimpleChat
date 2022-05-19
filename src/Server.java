import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService thredpool;

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            thredpool = Executors.newCachedThreadPool();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                thredpool.execute(handler);
            }
        } catch (IOException e) {
            shutDown();
        }
    }

    public void broadcast(String message){
        for(ConnectionHandler ch : connections){
            if(ch!= null){
                ch.sendMessage(message);
            }
        }
    }

    public  void shutDown(){
        if(!server.isClosed()){
            try {
                done = true;
                for (ConnectionHandler ch : connections){
                    ch.shutdown();
                }
                server.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader read;
        private PrintWriter write;
        private String clientName;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run(){
            try {
                write = new PrintWriter(client.getOutputStream(), true);
                read = new BufferedReader(new InputStreamReader(client.getInputStream()));
                write.println("Enter your name: ") ;
                clientName = read.readLine(); //good to implement name validation
                System.out.println(clientName + " connected.");
                broadcast(clientName + " joined the chat.");

                String message;
                while((message = read.readLine()) != null){
                    if(message.startsWith("/changenick")){
                        String[] messageSplit = message.split(" ",2);
                        if(messageSplit.length == 2){
                            broadcast(clientName + " change name to " + messageSplit[1]);
                            System.out.println(clientName + " renamed to " + messageSplit[1]);
                            write.println("Your nickname changed to " + messageSplit[1]);
                            clientName = messageSplit[1];
                        }
                        else{
                            write.println("Incorrect nickname provided.");
                        }
                    }
                    else if(message.startsWith("/quit")){
                        broadcast("User " + clientName + " left the chat.");
                        System.out.println(clientName + " left the chat.");
                        //connections.remove(this);
                        shutdown();
                    }else broadcast(clientName + ":" + message);
                }

            } catch (IOException e){
                shutdown();
            }
        }

        public  void sendMessage(String message){
            write.println(message);
        }

        public void shutdown(){
            if(!client.isClosed()){
                try {
                    read.close();
                    write.close();
                    client.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }
}
