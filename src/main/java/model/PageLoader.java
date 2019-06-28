package model;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class PageLoader {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static Stage mainStage;

    public static void initStage(Stage stage) {
        mainStage = stage;
        mainStage.setTitle("Google Mail");
        mainStage.setResizable(true);
        mainStage.initStyle(StageStyle.DECORATED);
        mainStage.getIcons().add(new Image(Paths.get("src/main/resources/images/icon.png").toUri().toString()));
    }

    public void load(String url) throws IOException {
        URL u = getClass().getResource(url);
        Parent root = FXMLLoader.load(u);
        mainStage.setScene(new Scene(root, WIDTH, HEIGHT));
        mainStage.show();
    }

    public void load(String url, Object controller) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(url));
        fxmlLoader.setController(controller);
        fxmlLoader.load();
    }
}