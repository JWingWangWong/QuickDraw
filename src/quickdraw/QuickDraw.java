package quickdraw;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author cooldued59
 */
public class QuickDraw extends Application {

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        DrawingToolBar t = new DrawingToolBar();
        StackPane holder = new StackPane();

        holder.setMaxSize(650, 650);
        VBox v = new VBox();
        DrawingCanvas canvas = new DrawingCanvas(600,600, t);
        DrawingMenuToolbar dmt = new DrawingMenuToolbar(canvas);




        holder.getChildren().add(canvas.getView());
        root.setTop(v);
        root.setCenter(holder);
        root.setLeft(t.createToolbar());
        root.getStylesheets().add(getClass().getResource("mainStyle.css").toString());

        Scene scene = new Scene(root, 1280, 720);

        holder.getStyleClass().add("holder");
        root.getStyleClass().add("background");
        primaryStage.setTitle("QuickDraw");
        DrawingMenuBar menuBar = new DrawingMenuBar(canvas, primaryStage);
        v.getChildren().add(menuBar);
        v.getChildren().add(dmt.createMenuToolbar());
        v.getStyleClass().add("menubar");

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
