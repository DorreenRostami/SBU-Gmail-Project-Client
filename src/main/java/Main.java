
import javafx.application.Application;
import javafx.stage.Stage;
import model.PageLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main extends Application {

    @Override
    public void init() throws IOException {
        /*File file = new File("resources/images/a text.txt");
        byte[] bytes = Files.readAllBytes(file.toPath());
        System.out.println(file.getPath());*/
        /*File newFile = new File("resources/downloads/my.txt");
        OutputStream os = new FileOutputStream(newFile);
        os.write(bytes);*/
        System.out.println("running");
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        PageLoader.initStage(primaryStage);
        new PageLoader().load("/SignIn.fxml");
    }

    @Override
    public void stop(){
        System.out.println("done");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
