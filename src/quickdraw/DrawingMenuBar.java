
package quickdraw;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
/**
 *
 * @author cooldued59
 */
public class DrawingMenuBar extends MenuBar{
    public DrawingMenuBar(DrawingCanvas c, Stage primaryStage){
        super();
        Menu file = new Menu("File");
        MenuItem fileSave = new MenuItem("Save");
        MenuItem fileOpen = new MenuItem("Open");
        file.getItems().addAll(fileSave,fileOpen);

        fileSave.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(primaryStage);

                if(file != null){
                    try {
                        WritableImage writableImage = new WritableImage(600, 600);
                        c.getView().snapshot(null, writableImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        Logger.getLogger(QuickDraw.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        });

        fileOpen.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("png files (*.png)", "*.png");

                File file = fileChooser.showOpenDialog(primaryStage);
                if(file != null){
                    try {
                        BufferedImage bufferedImage = ImageIO.read(file);
                        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                        c.loadCanvas(image);

                    } catch (IOException ex) {
                        Logger.getLogger(QuickDraw.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });


        this.getMenus().addAll(file);
    }
}
