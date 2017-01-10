
/**
 * Licenses:
 * Icon (eye dropper and more):
 * <div>Icons made by <a href="http://www.freepik.com"
 * title="Freepik">Freepik</a> from <a href="http://www.flaticon.com"
 * title="Flaticon">www.flaticon.com</a> is licensed by
 * <a href="http://creativecommons.org/licenses/by/3.0/"
 * title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
 *
 */
package quickdraw;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The toolbar for the drawing program
 */
public class DrawingToolBar{
    // The list of buttons we need to add to the program
    private final List<ToggleButton> buttonList;

    // The slider that will determine how large the brush/eraser size is
    private final Slider sizeSlider;
    private int currentSize;

    // The current size displayed
    final Label sizeValue;

    // The current tool in use
    private String currentTool;
    ToggleGroup group;


    ToggleGroup eraseOptionGroup;
    private String currentEraserSetting;

    private Color currentColor;
    private final ColorPicker colorPicker;
    public DrawingToolBar(){
        buttonList = new ArrayList<>();
        createButtons();
        currentColor = Color.BLACK;

        colorPicker = createColorPicker();

        sizeSlider = createSizeSlider();

        sizeValue = new Label(
        Integer.toString((int) sizeSlider.getValue()));
        sizeValue.setTextFill(Color.WHITE);



    }

    /**
     * Creates a slider that lets for the paint brush grow in size.
     * @return A slider with variables from 1 to 100
     */
    private Slider createSizeSlider(){
        Slider slider = new Slider();
        slider.setMin(1);
        slider.setMax(100);
        slider.setValue(1);
        currentSize = 1;
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(1);


        slider.valueProperty().addListener((ObservableValue<? extends Number>
                ov, Number old_val, Number new_val) -> {
                setSize(new_val.intValue());
                int value = new_val.intValue();

                sizeValue.setText(Integer.toString(value));

        });
        return slider;
    }

    /**
     * Sets the size of the paint brush
     *
     * @param size The size of the paint brush
     */
    private void setSize(int size){
        currentSize = size;
    }

    /**
     * Gets the size of the paint brush
     * @return The size of the pait brush (obtained from the slider)
     */
    public int getSize(){
        return currentSize;
    }

    /**
     * Gets the current toool of the paint brush
     * @return The name of the current tool
     */
    public String getTool(){
        return currentTool;
    }

    /**
     * Sets the tool of the paint brush
     * @param currentTool The tool that is currently being used
     */
    public void setTool(String currentTool){
        System.out.println(currentTool);
        this.currentTool = currentTool;
    }

    /**
     * Creates the toolbar by placing all the buttons first,
     * then places the size slider, and then places the color picker
     * @return A toolbar containing all the tools needed for the canvas
     */
    public VBox createToolbar(){
        VBox v = new VBox();

        for (ToggleButton t : buttonList) {
            v.getChildren().add(t);
        }
        Label size = new Label("Size");
        size.setTextFill(Color.WHITE);
        v.getChildren().add(size);
        v.getChildren().add(sizeSlider);
        v.getChildren().add(sizeValue);
        v.getChildren().add(colorPicker);
        v.getStyleClass().add("toolbar");
        return v;
    }

    /**
     * Creates the color picker
     * @return A color picker with an event handler when it's used
     */
    private ColorPicker createColorPicker(){
        final ColorPicker createPicker = new ColorPicker(currentColor);
        createPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(Event t) {
                setColor(createPicker.getValue());
            }
        });

        return createPicker;
    }
    /**
     * Sets the color of the paintbrush and the colorPicker
     * @param color The color needed to be set
     */
    public void setColor(Color color){
        colorPicker.setValue(color);
        currentColor = color;
    }

    /**
     * Gets the color that the paint brush is currently using
     * @return The current color of the paint brush
     */
    public Color getColor(){
        return currentColor;
    }

    /**
     * Creates the buttons for the toolbar
     * It first creates toggle buttons, then gets the icon location, then creates
     * their ids, and then sets them to a toggle group.
     */
    private void createButtons(){
        ToggleButton paint = new ToggleButton();
        ToggleButton pencil = new ToggleButton();
        //ToggleButton fill = new ToggleButton();
        ToggleButton erase = new ToggleButton();
        ToggleButton circle = new ToggleButton();
        ToggleButton square = new ToggleButton();
        ToggleButton eyeDrop = new ToggleButton();

        buttonList.add(paint);
        buttonList.add(pencil);
        buttonList.add(erase);
        buttonList.add(circle);
        buttonList.add(square);
        buttonList.add(eyeDrop);

        List<String> imagePaths = new ArrayList<>();
        imagePaths.add("icons/paintIcon.png");
        imagePaths.add("icons/pencilIcon.png");
        imagePaths.add("icons/eraserIcon.png");
        imagePaths.add("icons/circle.png");
        imagePaths.add("icons/square.png");
        imagePaths.add("icons/eyeDropIcon.png");

        List<String> buttonIDs = new ArrayList<>();
        buttonIDs.add("paintButton");
        buttonIDs.add("pencilButton");
        buttonIDs.add("eraseButton");
        buttonIDs.add("circleButton");
        buttonIDs.add("squareButton");
        buttonIDs.add("eyeDropButton");

        group = new ToggleGroup();

        Iterator<ToggleButton> it1 = buttonList.iterator();
        Iterator<String> it2 = imagePaths.iterator();
        Iterator<String> it3 = buttonIDs.iterator();

        while (it1.hasNext() && it2.hasNext() && it3.hasNext()) {
            ToggleButton t = it1.next();
            String s  = it2.next();
            String id = it3.next();

            t.setPrefSize(30, 30);
            Image image = new Image(getClass().getResourceAsStream(s));
            ImageView iView = new ImageView(image);
            iView.setFitWidth(20);
            iView.setFitHeight(20);
            t.setGraphic(iView);
            t.setId(id);
            t.setToggleGroup(group);
        }

        group.selectedToggleProperty().addListener((ObservableValue<?
                extends Toggle> ov, Toggle toggle, Toggle new_toggle) -> {
            if (new_toggle != null){
                ToggleButton b = (ToggleButton) group.getSelectedToggle();
                String bText = b.getId();
                setTool(bText);
            } else {
                setTool(null);
            }
        });
    }

    /**
     * Disables the selected button
     */
    public void disableButton(){
        group.getSelectedToggle().setSelected(false);
    }
}
