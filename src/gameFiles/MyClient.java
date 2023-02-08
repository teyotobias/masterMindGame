package gameFiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MyClient {

    //get a socket, connect to host
    //get a buffered reader for input and keyboard input, along with a printwriter for output (autoflushed)
    //start a new server connection "thread"

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 6666;

    public static void main(String args[]) throws IOException {
        Socket s = new Socket("localhost", 6666);
        BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ServerConnection serverConn = new ServerConnection(s);
        BufferedReader keyBoard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);

        new Thread(serverConn).start();

        while(true){
            String command = keyBoard.readLine();

            if(command.equals("quit")) break; //close socket and exit system

            out.println(command);

            //dont wait for server reply here anymore, handled separately
            //String serverResponse = input.readLine();
            //System.out.println("Server says: " + serverResponse);

        }
        s.close();
        System.exit(0);
    }
}
