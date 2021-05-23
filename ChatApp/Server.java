import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private final String hostName;
    private final List<ClientConnection> connectionList;
    private TextFlow textFlow;

    public Server(String hostName, int port) {
        this.hostName = hostName;
        connectionList = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port, 0, InetAddress.getLocalHost());
        } catch (IOException e) {
            e.printStackTrace();
        }
        runServer();
    }

    public String getHostName() {
        return hostName;
    }

    void serverInput(String message){
        Text text = new Text(message + "\n");
        text.setFont(Font.font(16));
        Platform.runLater(() -> textFlow.getChildren().add(text));
    }

    public String getIPAddress(){
        return serverSocket.getInetAddress().getHostAddress();
    }

    public void sendMessageToClients(String message){
        connectionList.forEach(clientConnection -> clientConnection.sendMessage(message));
        if (message.split("=").length >=2 && message.split("=")[1].trim().equalsIgnoreCase("exit")){
            closeServer();
        }
    }

    private void runServer() {
        Thread connectionWait = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    ClientConnection client = new ClientConnection(clientSocket);
                    connectionList.add(client);
                    client.start();
                } catch (IOException e) {
                    System.out.println("ServerSocket closed");
                }
            }
        });
        connectionWait.start();
    }

    private void closeServer(){
        try {
            serverSocket.close();
            synchronized (connectionList) {
                connectionList.forEach(ClientConnection::closeConnection);
            }
            System.out.println("Server successfully closed");
            System.exit(1);
        }catch (Exception e){
            System.out.println("Error on server closing");
        }
    }

    public void setTextFlow(TextFlow textFlow) {
        this.textFlow = textFlow;
        serverInput(String.format("Chat started at %tc", new Date()));
    }

    private class ClientConnection extends Thread{
        private String clientName;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        ClientConnection(Socket socket){
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                closeConnection();
            }
        }

        @Override
        public void run() {
            while (!socket.isClosed()){
                try {
                    if (in.ready()){
                        String message = in.readLine();
                        if(message.split("=")[0].equalsIgnoreCase("clientName")){
                            clientName = message.split("=")[1];

                            sendMessageToClients(clientName + " joined to chat");
                            serverInput(clientName + " joined to chat");

                            final String[] listOfUsers = {"\n" + hostName};
                            connectionList.forEach(client -> listOfUsers[0] += "\n" + client.getClientName());

                            sendMessage("Hello " + clientName + "\nList of online clients:" + listOfUsers[0]);
                            serverInput("List of online clients:" + listOfUsers[0]);
                        }else if(message.split("=")[0].equalsIgnoreCase("msg")){
                            String str = clientName + ": " + message.split("=")[1];
                            connectionList.forEach(clientConnection -> {
                                if(!clientConnection.getClientName().equals(clientName)) {
                                    clientConnection.sendMessage("msg=" + str);
                                }
                            });
                            serverInput(str);
                            if(message.split("=")[1].trim().equalsIgnoreCase("exit")){
                                sendMessageToClients(clientName + " exited from chat");
                                serverInput(clientName + " exited from chat");
                                closeConnection();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void closeConnection() {
            try {
                in.close();
                out.close();
                socket.close();

                connectionList.remove(this);
                if (connectionList.size() == 0){
                    closeServer();
                }
            } catch (IOException e) {
                System.out.println("Error on clientConnection closing");
            }
        }

        private void sendMessage(String message){
            out.println(message);
            out.flush();
        }

        public String getClientName() {
            return clientName;
        }
    }
}
