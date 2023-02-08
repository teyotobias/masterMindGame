package gameFiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


//THIS IS WHERE BULK TO CODE IS -> GAME IMPLEMENTATION
//CLIENTHANDLER HANDLES WHAT GAME.JAVA HANDLED IN PROJECT 2
//INTROGAME FIRST, THEN USUAL METHODS FOR GAME
//EVAL RESULTS SETS FLAG TO TRUE WHICH GETS CLIENT OUT OF LOOP AND OUT OF GAME
//EVERYTHING MUST BE CLOSED AT THE END, DONE BY CLOSE CLIENT FUNCTION

public class ClientHandler implements Runnable{
    volatile private static boolean flag = false;
    public String[] cols = GameConfiguration.colors;
    public boolean play = false;
    private String nameTag;
    public String[] guessHist; // array for guesses (history)
    public String[] resultHist; // array for results
    public String holdGuess; //holds guess
    public String holdCode; //holds code
    public int numTries = 0; //number of guesses so far
    public int numGuesses = GameConfiguration.guessNumber; //guess limit
    public static final int guesses = GameConfiguration.guessNumber; //guess limit
    public int pegNum = GameConfiguration.pegNumber; //num of letters for guess
    int B = 0; //black pegs
    int W = 0; //white pegs
    public String guess; //holds user guess
    private Socket client; //socket for specific client
    private BufferedReader in; //for input
    private PrintWriter out; //for output
    private ArrayList<ClientHandler> clients;
    public String code; //randomly generated code to guess
    Scanner input; // scanner for input


    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients, String code, Scanner in2) throws IOException {
        try {
            this.client = clientSocket;
            this.clients = clients;
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
            this.code = code;
            input = in2;
            guessHist = new String[guesses];
            resultHist = new String[guesses];
            guess = new String();
            holdGuess = new String();
            holdCode = new String();
            for (int i = 0; i < guesses; i++) {
                guessHist[i] = new String();
                resultHist[i] = new String();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }


    }

    //public void listen(){
     //   new Thread(new Runnable() {
      //      public void run() {
      //          while (client.isConnected()) {
       //             try {
          //              String serverResponse = in.readLine();
           //             if (serverResponse == null) break;
            //            System.out.println(serverResponse);//System.out.println("Server says " + serverResponse);
             //       } catch (IOException e) {
              //          closeAll(client, in, out);
               //     }

                    //try {
                    //   while (client.isConnected()) {
                    //      String serverResponse = in.readLine();
                    //     if(serverResponse == null) break;
                    //     System.out.println(serverResponse);//System.out.println("Server says " + serverResponse);
                    // }
                    //} catch (IOException e) {
                    //    e.printStackTrace();
                    //   closeAll(client, in, out);
               // }
         //   }
      //  }).start();
   // }

    @Override
    public void run() {
        //out.println(code); //TAKE OUT - FOR TESTING
        introGame(out);
        try {
            play = playGame();
        } catch (IOException e) {
            e.printStackTrace();
        }
            // try {
           //     play = playGame();
           // } catch (IOException e) {
           //     e.printStackTrace();
          //  }
        if(play == false){
            out.println("Goodbye!");
            closeClient(client, in, out); //NEED IMPLEMENTATION TO TAKE THREAD OUT
        }
        out.println("What is your name?");
        try {
            nameTag = in.readLine();
        }
        catch(IOException e){
            e.printStackTrace();
        }


        while(!flag) {
            B = 0;
            W = 0;
            out.println("You have " + numGuesses + " guesses left.\nWhat is your next guess?\n" +
                    "Type in the characters for your guess and press enter.\nEnter guess: ");
            try {
                guess = in.readLine();
                if(flag == true){
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                checkInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getResults();
            evalResults();
        }
        closeClient(client, in, out);






                /*
                String request = in.readLine();
                if (request.contains("name")) {
                    out.println(MyServer.getRandomName());
                } else if(request.startsWith("say")){
                    int firstSpace = request.indexOf(" ");
                    if(firstSpace != -1){
                        outToAll(request.substring(firstSpace + 1)); //this one is important
                    }
                }
                else {
                    out.println("Type 'tell me a name' to get a random name");
                }
                 */


    }

    private void outToAll(String msg){
        for( ClientHandler aClient : clients){
            aClient.out.println(msg);
        }
    }
    public static void introGame(PrintWriter out){
        out.println("Welcome to Mastermind. Here are the rules.\n");
        out.println("This is a text version of the classic board game Mastermind.\n");
        out.print("The computer will think of a secret code. The code consists of 4\n" +
                "colored pegs. The pegs MUST be one of 6 colors: blue, green, orange, purple, red, or yellow.");
        out.println(" A color may appear more than once in\nthe code. You try to " +
                "guess what colored pegs are in the code and\nwhat order they are in. After you make" +
                " a valid guess the result\n(feedback) will be displayed.\n");
        out.println("The result consists of a black peg for each peg you have guessed\n" +
                "exactly correct (color and position) in your guess. For each peg in\nthe" +
                " guess that is the correct color, but is out of position, you get\na white peg." +
                " For each peg, which is fully incorrect, you get no\nfeedback.\n");
        out.println("Only the first letter of the color is displayed. B for Blue, R for\n" +
                "Red, and so forth. When entering guesses you only need to enter the\nfirst character " +
                "of each color as a capital letter.\n");
    }
    public boolean playGame() throws IOException {
        String decis = "N";
        out.println("You have 12 guesses to figure out the secret code or you lose the game. " +
                "Are you ready to play?");
        decis = in.readLine();
        if(decis.equals("Y") || decis.equals("y")){
            return true;
        }

        return false;
    }
    public void printHistory(){
        for(int i = 0; i < numTries;i++){
            out.println(guessHist[i] + "\t\t" + resultHist[i]);
        }
        out.println("");

    }
    public void checkInput() throws IOException {
        int flag = 0;
        if(guess.equals("HISTORY")){
            printHistory();
        }
        holdGuess = guess;
        holdCode = code;
        while (flag == 0) {
            if(contains() == false) {
                if (!guess.equals("HISTORY")) {
                    out.println(guess + " -> INVALID GUESS\n");
                } else {
                    out.println("You have " + numGuesses + " guesses left. \n");
                }
                out.println("What is your next guess?\n" +
                        "Type in the characters for your guess and press enter. \nEnter guess: ");
                guess = in.readLine();
                out.println("");
                if (guess.equals("HISTORY")) {
                    printHistory();
                }
            }
            else {
                holdGuess = guess;
                flag = 1;





          //  for (int i = 0; i < pegNum; i++) {
           //     if (contains() == false) {
            //        if(!guess.equals("HISTORY")){
              //          out.println(guess + " -> INVALID GUESS\n");
               //     }
               //     else {
                //        out.print("You have " + numGuesses +  " guesses left.\n");
                //    }
                 //   out.println("What is your next guess?\n" +
                 //           "Type in the characters for your guess and press enter.\nEnter guess: ");
                 //   guess = in.readLine();
                    /*
                    if(in.hasNextLine()){
                        guess = in.nextLine();
                    }
                    else {
                        guess = "";
                    }
                    System.out.println("");
                     */
                  //  out.println("");
                  //  if(guess.equals("HISTORY")){
                   //     printHistory();
                   // }
                   // holdGuess = guess;
                   // break;
              //  else if(i == pegNum-1){
              //      flag = 1;
            }

        }
        numGuesses--;
    } //checks if input is valid and runs until valid input is given
    public boolean contains(){
        int flag = 0;
        if(guess.equals("")){
            return false;
        }
        if(guess.length() != pegNum){
            return false;
        }
        for(int i = 0; i < cols.length; i++) {
            if (guess.contains(cols[i])) {
                for (int j = 0; j < pegNum; j++) {
                    if (guess.charAt(j) == cols[i].charAt(0)) {
                        flag += 1;
                    }
                }
            }
        }
        if(flag != 4){
            return false;
        }
        return true;

    }
    public void getResults(){
        for(int j = 0; j < pegNum; j++){
            if (holdGuess.charAt(j) == holdCode.charAt(j)) {
                B++;
                String newGuess = holdGuess.replaceFirst(holdGuess.charAt(j)+"", "-");
                String newCode = holdCode.replaceFirst(holdCode.charAt(j)+"", "-");
                holdGuess = newGuess;
                holdCode = newCode;
            }
        }
        for(int i = 0; i < pegNum; i++){
            for(int j = 0; j < pegNum; j++){
                if (holdGuess.charAt(i) == holdCode.charAt(j) && holdGuess.charAt(i) != '-') {
                    W++;
                    String newGuess = holdGuess.replaceFirst(holdGuess.charAt(j)+"", "-");
                    String newCode = holdCode.replaceFirst(holdCode.charAt(j)+"", "-");
                    holdGuess = newGuess;
                    holdCode = newCode;
                }
            }

        }
    }
    public void evalResults(){
        guessHist[numTries] = guess;
        resultHist[numTries] = B + "B_" + W + "W";
        numTries++;
        if(B == pegNum){
            out.println(guess + " -> Result: " + resultHist[numTries-1] + " - You win !!\n");
            outToAll(nameTag + " is the winner! Goodbye!");
            flag = true;

            closeClient(client, in, out);
        }
        else if(numTries == guesses){
            out.println("Sorry, you are out of guesses. You lose, boo-hoo.\n");
            closeClient(client, in, out);



        }
        else {
            out.println(guess + " -> Result: " + resultHist[numTries - 1] + "\n");
        }


    }//stores guess and results and prints them out to screen

    public void goodbyeClient(){
        clients.remove(this);
    }
    public void closeClient(Socket sock, BufferedReader reader, PrintWriter writer){
        goodbyeClient();
        try {
            if(reader != null){
                reader.close();
            }
            if(writer != null){
                writer.close();
            }
            if(sock != null){
                sock.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        //try {
         //   if(reader != null)
          //  {
          //      reader.close();
          //  }
          //  if(writer != null){
          //      writer.close();
          //  }
          //  if(sock != null){
           //     sock.close();
          //  }
      //  } catch (IOException e){
       //     e.printStackTrace();

    }


}
