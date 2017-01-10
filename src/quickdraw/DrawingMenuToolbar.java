package quickdraw;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static javafx.beans.binding.Bindings.isEmpty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * The menu toolbar that has things like undo and redo
 * @author cooldued59
 */
public class DrawingMenuToolbar {
    private final List<Button> buttonList;
    public DrawingMenuToolbar(DrawingCanvas c){

        buttonList = new ArrayList<>();
        createButtons(c);
    }

    /**
     * Creates the undo and redo button
     * @param c - The DrawingCanvas c that is used to get methods for
     */
    private void createButtons(DrawingCanvas c){
        // Create redo and undo buttons that have properties where
        // if the undo or redo list in drawingcanvas is empty they are disabled.
        Button undo = new Button();
        Button redo = new Button();
        undo.disableProperty().bind(isEmpty(c.getObUndoList()));
        undo.setOnAction(e -> c.undoCanvas());

        redo.disableProperty().bind(isEmpty(c.getObRedoList()));
        redo.setOnAction(e -> c.redoCanvas());

        buttonList.add(undo);
        buttonList.add(redo);

        // Add image icons
        List<String> imagePaths = new ArrayList<>();
        imagePaths.add("icons/undoIcon.png");
        imagePaths.add("icons/redoIcon.png");

        // Add button ids to buttons
        List<String> buttonIDs = new ArrayList<>();
        buttonIDs.add("undoButton");
        buttonIDs.add("redoButton");


        Iterator<Button> it1 = buttonList.iterator();
        Iterator<String> it2 = imagePaths.iterator();
        Iterator<String> it3 = buttonIDs.iterator();

        // Add properties to buttons
        while (it1.hasNext() && it2.hasNext() && it3.hasNext()) {
            Button t = it1.next();
            String s  = it2.next();
            String id = it3.next();

            t.setPrefSize(30, 30);
            Image image = new Image(getClass().getResourceAsStream(s));
            ImageView iView = new ImageView(image);
            iView.setFitWidth(20);
            iView.setFitHeight(20);
            t.setGraphic(iView);
            t.setId(id);


        }


    }

    /**
     * Create the menu toolbar
     * @return An hbox containing the menu buttons
     */
    public HBox createMenuToolbar(){
        HBox hb = new HBox();
        hb.setPadding(new Insets(7, 6, 7, 6));
        hb.setSpacing(3);
        for (Button t : buttonList) {
            hb.getChildren().add(t);
        }
        return hb;
    }

}
