
import javafx.application.Application;
import javafx.stage.Stage;
import model.PageLoader;

import java.io.File;

public class Main extends Application {

    @Override
    public void init() {
        System.out.println("running");
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        PageLoader.initStage(primaryStage);
        new PageLoader().load("/view/SignIn.fxml");
    }

    @Override
    public void stop(){
        File DB = new File("/currentUser");
        DB.delete();
        DB.mkdir();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
