package bms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {



    @Override
    public void start(Stage primaryStage) throws Exception{
        //System.out.println(getClass().getResource("/fxml/bms.fxml"));
        AnchorPane root = FXMLLoader.load(getClass().getResource("/fxml/sample.fxml"));
        primaryStage.setTitle("BMS");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        // Would otherwise focus the width textfield
        root.requestFocus();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
