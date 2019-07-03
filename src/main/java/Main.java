import javafx.application.Application;
import javafx.stage.Stage;
import model.PageLoader;

public class Main extends Application {

    @Override
    public void init() {
        System.out.println("running");
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        PageLoader.initStage(primaryStage);
        new PageLoader().load("/SignIn.fxml");
    }

    @Override
    public void stop() {
        System.out.println("done");
    }

    public static void main(String[] args) {
        launch(args);
    }
}