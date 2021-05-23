import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {
    private Stage stage;
    private int port = 9090;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("menu.fxml"));
        stage = primaryStage;
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> System.exit(1));

        runChat();
    }

    private void runChat() {
        TextField userNameField = (TextField) stage.getScene().lookup("#userNameField");
        TextField ipAddressField = (TextField) stage.getScene().lookup("#ipAddressField");

        Button beHostButton = (Button) stage.getScene().lookup("#beHostButton");
        beHostButton.setOnAction(event -> {
            Server server = new Server(userNameField.getText(), port);
            createServerWindow(server);
        });

        Button joinToChatButton = (Button) stage.getScene().lookup("#joinToChatButton");
        joinToChatButton.setOnAction(event -> {
            Client client = new Client(ipAddressField.getText(), port, userNameField.getText());
            createClientWindow(client);
        });

    }


    private void createClientWindow(Client client) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
            stage.setScene(new Scene(root));
            stage.show();

            client.setTextFlow((TextFlow) stage.getScene().lookup("#textFlow"));

            TextField message = (TextField) root.lookup("#message");
            Button sendMessage = (Button) root.lookup("#sendMessage");
            sendMessage.setOnAction(event -> {
                client.sendMessage("msg=" + message.getText());
                client.clientInput("Me: " + message.getText());
                message.setText("");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createServerWindow(Server server) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("server.fxml"));
            stage.setScene(new Scene(root));
            stage.show();

            server.setTextFlow((TextFlow) stage.getScene().lookup("#textFlow"));

            ((Label) root.lookup("#ipAddress")).setText("IP Address: " + server.getIPAddress());
            ((Label) root.lookup("#port")).setText("Port: " + port);

            TextField message = (TextField) root.lookup("#message");
            Button sendMessage = (Button) root.lookup("#sendMessage");
            sendMessage.setOnAction(event -> {
                server.sendMessageToClients("msg=" + message.getText());
                server.serverInput("Me: " + message.getText());
                message.setText("");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void draw(){
        TextFlow textFlow = new TextFlow();
        textFlow.setPadding(new Insets(5,10,10,10));

        BorderPane pane = new BorderPane(textFlow);
        HBox hBox = new HBox();
        pane.setBottom(hBox);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
