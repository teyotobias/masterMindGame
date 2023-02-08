package gameFiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//has a startServer function that outputs if a new client is requested
//adds client to an arrayList of clients
//starts a thread for clientHandlers HERE
//why different sockets for each thread? -> objects cannot run simultaneously, need thread
public class MyServer {
    public static String code = SecretCodeGenerator.getInstance().getNewSecretCode();
    private static final int PORT = 6666;
    private ServerSocket serverSocket;
    Scanner input = new Scanner(System.in);
    private Thread threadUno;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    //private static ExecutorService pool = Executors.newFixedThreadPool(4); //GET RID OF, FIND REPLACEMENT

    public MyServer(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try {
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected");
                ClientHandler clientHandler = new ClientHandler(socket, clients, code, input);
                clients.add(clientHandler);

                threadUno = new Thread(clientHandler);;
                threadUno.start();
            }
        }
        catch(IOException e){

        }
    }
    public void closeServerSocket(){
        try {
            if(serverSocket != null){
                serverSocket.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(PORT);
        while (true) {
            MyServer server = new MyServer(listener);
            server.startServer();
           // System.out.println("Waiting for client connection");
            //Socket client = listener.accept();
           // System.out.println("Connected to client!");
           // Scanner input = new Scanner(System.in);
            //ClientHandler clientThread = new ClientHandler(client, clients, code, input);
            //pool.execute(clientThread);

        }


    }
}



