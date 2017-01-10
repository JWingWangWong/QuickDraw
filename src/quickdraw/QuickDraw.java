package quickdraw;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The mainframe for the application. This is where all the classes are created
 * are linked together to make the application work.
 * @author cooldued59
 */
public class QuickDraw extends Application {

    /**
     * Initializes the stage and the panes within it.
     * @param primaryStage - The stage to initialize
     * TODO: Remove magic numbers
     */
    @Override
    public void start(Stage primaryStage) {

        // Create panes that will contain our application funcitons
        BorderPane root = new BorderPane();
        DrawingToolBar t = new DrawingToolBar();
        StackPane holder = new StackPane();

        // The size of the canvas
        holder.setMaxSize(650, 650);
        VBox v = new VBox();
        DrawingCanvas canvas = new DrawingCanvas(600,600, t);
        DrawingMenuToolbar dmt = new DrawingMenuToolbar(canvas);

        // Set the position of the panes and their features
        holder.getChildren().add(canvas.getView());
        root.setTop(v);
        root.setCenter(holder);
        root.setLeft(t.createToolbar());

        // Use mainStyle.css to style the window
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
