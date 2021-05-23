import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class Client {
    private final String userName;
    Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private TextFlow textFlow;

    Client(String ipAddress, int port, String userName){
        this.userName = userName;
        try {
            socket = new Socket(ipAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            waitMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitMessages() {
        Thread messageWait = new Thread(() -> {
            clientInput(String.format("Chat started at %tc", new Date()));
            sendMessage("clientName=" + userName);
            while (!socket.isClosed()){
               try {
                   if (in.ready()){
                       String message = in.readLine();
                       if(message.split("=")[0].equalsIgnoreCase("msg")){
                           String msg = message.split("=")[1];
                           clientInput(msg);
                           if(msg.trim().equalsIgnoreCase("exit")) {
                               closeConnection();
                           }
                       }else {
                           clientInput(message);
                       }
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        });
        messageWait.start();
    }

    private void closeConnection() {
        try {
            socket.close();
            in.close();
            out.close();
            System.out.println("Client closed");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void clientInput(String message) {
        Text text = new Text(message + "\n");
        text.setFont(Font.font(16));
        Platform.runLater(() -> textFlow.getChildren().add(text));
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
        if (message.split("=")[1].trim().equalsIgnoreCase("exit")){
            closeConnection();
        }
    }

    public void setTextFlow(TextFlow textFlow) {
        this.textFlow = textFlow;
    }
}
