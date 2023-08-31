package com.adsoa.adsoa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import org.kordamp.bootstrapfx.BootstrapFX;

public class ADSOAMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ADSOAMain.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        stage.setTitle("ADSOA!");
        stage.setScene(scene);
        APICalc.main(null);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
//        Run the api

    }
}