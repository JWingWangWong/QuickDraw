/**
 * Code used:
 *
 * Properly making a stroke in canvas using images.
 * http://stackoverflow.com/questions/31927757/paintbrush-stroke-in-javafx
 *
 * Smooth lines.
 * https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm#Algorithm
 *
 */
package quickdraw;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;


/**
 * The canvas in which the user can draw on
 */
public final class DrawingCanvas {

    // State contains the previous image and the current image
    Pair<BufferedImage,BufferedImage> state;

    // Stack that contains the list of things to undo and the observable undo list
    // for enabling/disabling undo
    Stack<Pair<BufferedImage,BufferedImage>> undoList;
    ObservableList<Pair<BufferedImage,BufferedImage>> obUndoList;

    //
    // Stack that contains the list of things to redo and the observable redo list
    // for enabling/disabling redo
    Stack<Pair<BufferedImage,BufferedImage>> redoList;
    ObservableList<Pair<BufferedImage,BufferedImage>> obRedoList;


    // The previous state of the canvas
    private BufferedImage prevImage;

    // The canvas itseld
    private final Canvas canvas ;

    // The tool to draw things on the canvas
    private GraphicsContext gc;

    // The drawingtoolbar
    private final DrawingToolBar t;

    // The previous location of the mouse
    Point2D prevMouseLocation = new Point2D( 0, 0);

    Image brush;

    // The width and height of the brush
    double brushWidthHalf;
    double brushHeightHalf;

   // Rectangle Experiment
   double starting_point_x, starting_point_y ;
   Rectangle new_rectangle = null ;
   boolean new_rectangle_is_being_drawn = false ;

   BufferedImage saveCanvasBeforeShape;


   //  The following method adjusts coordinates so that the rectangle
   //  is shown "in a correct way" in relation to the mouse movement.

   void adjust_rectangle_properties( double starting_point_x,
                                     double starting_point_y,
                                     double ending_point_x,
                                     double ending_point_y,
                                     Rectangle given_rectangle )
   {
      given_rectangle.setX( starting_point_x ) ;
      given_rectangle.setY( starting_point_y ) ;
      given_rectangle.setWidth( ending_point_x - starting_point_x ) ;
      given_rectangle.setHeight( ending_point_y - starting_point_y ) ;

      if ( given_rectangle.getWidth() < 0 )
      {
         given_rectangle.setWidth( - given_rectangle.getWidth() ) ;
         given_rectangle.setX( given_rectangle.getX() - given_rectangle.getWidth() ) ;
      }

      if ( given_rectangle.getHeight() < 0 )
      {
         given_rectangle.setHeight( - given_rectangle.getHeight() ) ;
         given_rectangle.setY( given_rectangle.getY() - given_rectangle.getHeight() ) ;
      }
   }

   Ellipse new_circle = null;
   boolean new_circle_is_being_drawn = false ;
   void adjust_circle_properties( double starting_point_x,
                                     double starting_point_y,
                                     double ending_point_x,
                                     double ending_point_y,
                                     Ellipse given_circle )
   {
      given_circle.setCenterX( (starting_point_x-ending_point_x)/2.0 ) ;
      given_circle.setCenterY( (starting_point_y-ending_point_y)/2.0) ;
      given_circle.setRadiusX( (ending_point_x - starting_point_x)/2.0 ) ;
      given_circle.setRadiusY( (ending_point_y - starting_point_y)/2.0 ) ;

      if ( given_circle.getRadiusX() < 0 )
      {
         given_circle.setRadiusX( - given_circle.getRadiusX() ) ;
         given_circle.setCenterX( (given_circle.getCenterX() - given_circle.getRadiusX())/2.0 ) ;
      }

      if ( given_circle.getRadiusY() < 0 )
      {
         given_circle.setRadiusY( - given_circle.getRadiusY() ) ;
         given_circle.setCenterY( (given_circle.getCenterY() - given_circle.getRadiusY())/2.0 ) ;
      }
   }


    /**
     * Constructor class
     * @param width - The width of the canvas
     * @param height - The height of the canvas
     * @param t - A model for the drawing toolbar
     */
    public DrawingCanvas(int width, int height, DrawingToolBar t){

	// Initializing undoList and its observable list
	undoList = new Stack<>();
        obUndoList = FXCollections.observableList(undoList);

        // Initializing redoList and its observable list
        redoList = new Stack<>();
        obRedoList = FXCollections.observableList(redoList);

	// Create the canvas and set the graphics context to the gc
        canvas = new Canvas(width,height);
        gc = canvas.getGraphicsContext2D();
        this.t = t;

        // Create the brush and give the brush settings
        brush = createBrush(1.0, Color.BLACK);
        brushWidthHalf = brush.getWidth()/2;
        brushHeightHalf = brush.getHeight()/2;

        // Sets the undo's previous image which is blank by default
        prevImage = createImage();

        // Adds an event handler for when the mouse is pressed
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if(this.t.getTool() != null){
                // If it's the paint or eraser initialize the draw settings
                if("paintButton".equals(t.getTool()) || "eraseButton".equals(t.getTool()) ||
                        "pencilButton".equals(t.getTool()) ){

                    initDraw(gc,this.t);
                    prevMouseLocation = new Point2D( event.getX(), event.getY());

                    gc.drawImage(brush,event.getX() - brushWidthHalf,event.getY() - brushHeightHalf);

                }
                /**else if(t.getTool() == "fillButton"){
                    initDraw(gc,this.t);

                // If it's the circle/square button initialize the shape settings
                }*/ else if ("circleButton".equals(t.getTool())) {
                    ovalDraw(event.getX(),event.getY());

                } else if ("squareButton".equals(t.getTool())){
                    rectDraw(event.getX(),event.getY());
                // If it's the eye dropper pick a color on the screen
                } else if ("eyeDropButton".equals(t.getTool())){
                    try {
                        t.setColor(locationColor(event.getScreenX(),event.getScreenY()));
                        t.disableButton();
                    } catch (AWTException ex) {
                        Logger.getLogger(DrawingCanvas.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }

        });

        // Adds an event handler when the mouse moves
        // This helps make stroke lines
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent event) -> {
            if(this.t.getTool() != null){
                if("paintButton".equals(t.getTool()) || "eraseButton".equals(t.getTool()) ||
                        "pencilButton".equals(t.getTool()) ){
                    /*
                    gc.lineTo(event.getX(), event.getY());
                    gc.stroke();
                    */
                    bresenhamLine( prevMouseLocation.getX(), prevMouseLocation.getY(),
                            event.getX(), event.getY());
                } else if (t.getTool() == "circleButton") {
                    ovalDraw(event.getX(),event.getY());
                } else if (t.getTool() == "squareButton"){
                    rectDraw(event.getX(),event.getY());
                }
                prevMouseLocation = new Point2D( event.getX(), event.getY());

            }
        });

        // If the mouse is released add the state to the image array
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent event) -> {
            if("squareButton".equals(t.getTool())){
                rectReleased(event.getX(), event.getY());
            } else if ("circleButton".equals(t.getTool())) {
                ovalRelease(event.getX(),event.getY());
            }
            if(t.getTool() != null && !"eyeDropButton".equals(t.getTool())){
                System.out.println(this.t.getTool());
                BufferedImage curImg = this.createImage();
                state = new Pair<>(prevImage,curImg);
                obUndoList.add(0, state);
                prevImage = curImg;
                obRedoList.removeAll(redoList);
            }

        });


        // If the mouse moved then set the color on the eye dropper
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, (MouseEvent event) -> {
            if(this.t.getTool() == "eyeDropButton"){
                try {
                    t.setColor(locationColor(event.getScreenX(),event.getScreenY()));
                } catch (AWTException ex) {
                    Logger.getLogger(DrawingCanvas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });



    }

    private void ovalDraw(Double x, Double y){
        if ( new_circle_is_being_drawn == false )
        {
           System.out.println("Starting circle");
           starting_point_x = x;
           starting_point_y = y;
           saveCanvasBeforeShape = createImage();
           new_circle = new Ellipse() ;

           // A non-finished rectangle has always the same color.
           new_circle.setFill( Color.SNOW ) ; // almost white color
           new_circle.setStroke( Color.BLACK ) ;

           gc.drawImage(new_circle.snapshot(null, null), starting_point_x, starting_point_y);

           new_circle_is_being_drawn = true ;
        }
        if ( new_circle_is_being_drawn == true )
        {
           gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
           loadImage(saveCanvasBeforeShape);
           System.out.println("dragging circle");
           double current_ending_point_x = x;
           double current_ending_point_y = y;

           adjust_circle_properties( starting_point_x,
                                        starting_point_y,
                                        current_ending_point_x,
                                        current_ending_point_y,
                                        new_circle ) ;
           SnapshotParameters param = new SnapshotParameters();
           param.setDepthBuffer(true);
           param.setFill(Color.TRANSPARENT);
           if(current_ending_point_x < starting_point_x && current_ending_point_y < starting_point_y){
                gc.drawImage(new_circle.snapshot(param, null), current_ending_point_x, current_ending_point_y);

           }else if(current_ending_point_x < starting_point_x){
                gc.drawImage(new_circle.snapshot(param, null), current_ending_point_x, starting_point_y);

           } else if(current_ending_point_y < starting_point_y){
                gc.drawImage(new_circle.snapshot(param, null), starting_point_x, current_ending_point_y);

           } else {
                gc.drawImage(new_circle.snapshot(param, null), starting_point_x, starting_point_y);
           }

        }
    }

    private void ovalRelease(Double x, Double y){
        if ( new_circle_is_being_drawn == true )
        {
           // Now the drawing of the new rectangle is finished.
           // Let's set the final color for the rectangle.
           System.out.println("Circle released");
           double current_ending_point_x = x ;
           double current_ending_point_y = y ;
           new_circle.setFill(t.getColor());
           new_circle.setStroke(t.getColor());
           SnapshotParameters param = new SnapshotParameters();
           param.setDepthBuffer(true);
           param.setFill(Color.TRANSPARENT);
           if(x < starting_point_x && y < starting_point_y){
                gc.drawImage(new_circle.snapshot(param, null), current_ending_point_x, current_ending_point_y);

           }else if(x < starting_point_x){
                gc.drawImage(new_circle.snapshot(param, null), current_ending_point_x, starting_point_y);

           } else if(y < starting_point_y){
                gc.drawImage(new_circle.snapshot(param, null), starting_point_x, current_ending_point_y);

           } else {
                gc.drawImage(new_circle.snapshot(param, null), starting_point_x, starting_point_y);
           }

           new_circle = null ;
           new_circle_is_being_drawn = false ;
        }
    }

    private void rectDraw(Double x, Double y){
        if ( new_rectangle_is_being_drawn == false )
        {
           System.out.println("Starting rectangle");
           starting_point_x = x;
           starting_point_y = y;
           saveCanvasBeforeShape = createImage();
           new_rectangle = new Rectangle() ;

           // A non-finished rectangle has always the same color.
           new_rectangle.setFill( Color.SNOW ) ; // almost white color
           new_rectangle.setStroke( Color.BLACK ) ;

           gc.drawImage(new_rectangle.snapshot(null, null), starting_point_x, starting_point_y);

           new_rectangle_is_being_drawn = true ;
        }
        if ( new_rectangle_is_being_drawn == true )
        {
           gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
           loadImage(saveCanvasBeforeShape);
           System.out.println("dragging rectangle");
           double current_ending_point_x = x;
           double current_ending_point_y = y;

           adjust_rectangle_properties( starting_point_x,
                                        starting_point_y,
                                        current_ending_point_x,
                                        current_ending_point_y,
                                        new_rectangle ) ;
           SnapshotParameters param = new SnapshotParameters();
           param.setDepthBuffer(true);
           param.setFill(Color.TRANSPARENT);
           if(current_ending_point_x < starting_point_x && current_ending_point_y < starting_point_y){
                gc.drawImage(new_rectangle.snapshot(param, null), current_ending_point_x, current_ending_point_y);

           }else if(current_ending_point_x < starting_point_x){
                gc.drawImage(new_rectangle.snapshot(param, null), current_ending_point_x, starting_point_y);

           } else if(current_ending_point_y < starting_point_y){
                gc.drawImage(new_rectangle.snapshot(param, null), starting_point_x, current_ending_point_y);

           } else {
                gc.drawImage(new_rectangle.snapshot(param, null), starting_point_x, starting_point_y);
           }

        }
    }

    private void rectReleased(Double x, Double y){

        if ( new_rectangle_is_being_drawn == true )
        {
           // Now the drawing of the new rectangle is finished.
           // Let's set the final color for the rectangle.
           System.out.println("Rectangle released");
           double current_ending_point_x = x ;
           double current_ending_point_y = y ;
           new_rectangle.setFill(t.getColor());
           new_rectangle.setStroke(t.getColor());
           SnapshotParameters param = new SnapshotParameters();
           param.setDepthBuffer(true);
           param.setFill(Color.TRANSPARENT);
           if(x < starting_point_x && y < starting_point_y){
                gc.drawImage(new_rectangle.snapshot(param, null), current_ending_point_x, current_ending_point_y);
           }else if(x < starting_point_x){
                gc.drawImage(new_rectangle.snapshot(param, null), current_ending_point_x, starting_point_y);

           } else if(y < starting_point_y){
                gc.drawImage(new_rectangle.snapshot(param, null), starting_point_x, current_ending_point_y);

           } else {
                gc.drawImage(new_rectangle.snapshot(param, null), starting_point_x, starting_point_y);
           }

           new_rectangle = null ;
           new_rectangle_is_being_drawn = false ;
        }
    }


    /**
     * Get the color on the canvas based on the location
     * @param X - Mouse X on canvas
     * @param Y - Mouse Y on canvas
     * @return - The color based on the X and Y location
     * @throws AWTException
     */
    public Color locationColor(double X, double Y) throws AWTException{
        Robot robot = new Robot();
        java.awt.Color awtColor = robot.getPixelColor((int) X, (int) Y);
        int r = awtColor.getRed();
        int g = awtColor.getGreen();
        int b = awtColor.getBlue();
        int a = awtColor.getAlpha();
        double opacity = a / 255.0 ;
        javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(r, g, b, opacity);

        return fxColor;
    }

    /**
     * Draws a shape based on the parameters
     * @param gc - The graphics context of the canvas
     * @param t - The toolbar, the name of the toolbar is used in this method
     * @param x - The width of the shape
     * @param y - The height of the shape
     */
    private void drawShape(GraphicsContext gc, DrawingToolBar t, double x, double y){
        if("circleButton".equals(t.getTool())){
            gc.setFill(t.getColor());
            gc.fillOval(x - (t.getSize()/2), y - (t.getSize()/2),
                    t.getSize(), t.getSize());
        } else if ("squareButton".equals(t.getTool())){
            gc.setFill(t.getColor());
            gc.fillRect(x - (t.getSize()/2), y - (t.getSize()/2),
                    t.getSize(), t.getSize());
        }
    }

    /**
     * Determines which draw tool to use
     * @param gc - The graphics context to setup
     * @param t - The drawing toolbar to determine which tool to use
     */
    private void initDraw(GraphicsContext gc, DrawingToolBar t){
        if (t.getTool().equals("paintButton")){

            brush = createBrush(t.getSize(),t.getColor());
            brushWidthHalf = brush.getWidth()/2;
            brushHeightHalf = brush.getHeight()/2;
        } else if (t.getTool().equals("eraseButton")){
            brush = createPencil(t.getSize(), Color.WHITE);
            brushWidthHalf = brush.getWidth()/2;
            brushHeightHalf = brush.getHeight()/2;
        } else if (t.getTool().equals("pencilButton")){
            brush = createPencil(t.getSize(),t.getColor());
            brushWidthHalf = brush.getWidth()/2;
            brushHeightHalf = brush.getHeight()/2;

        }
        /**
        else if(t.getTool().equals("fillButton")){
            gc.setFill(t.getColor());
            gc.fill();
            brushWidthHalf = brush.getWidth()/2;
            brushHeightHalf = brush.getHeight()/2;
        }
        */

    }

    /**
     * Loads the canvas from the file image
     * @param img - The image to be loaded to the canvas
     */
    public void loadCanvas(Image img){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        render(gc, (WritableImage) img, 600,600,0,0);

	//set initial undo property
	obUndoList.removeAll(undoList);
        obRedoList.removeAll(redoList);
        System.out.println("Canvas Loaded.");

        prevImage = createImage();
    }

    // Using Bresenham Line Algorithms
    // https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm#Algorithm
    private void bresenhamLine(double x0, double y0, double x1, double y1)
    {
      double dx =  Math.abs(x1-x0), sx = x0<x1 ? 1. : -1.;
      double dy = -Math.abs(y1-y0), sy = y0<y1 ? 1. : -1.;
      double err = dx+dy, e2; /* error value e_xy */

      while(true){
        gc.drawImage(brush, x0 - brushWidthHalf, y0 - brushHeightHalf);

        if (x0==x1 && y0==y1) break;
        e2 = 2.*err;
        if (e2 > dy) { err += dy; x0 += sx; } /* e_xy+e_x > 0 */
        if (e2 < dx) { err += dx; y0 += sy; } /* e_xy+e_y < 0 */
      }
    }

    // Using http://stackoverflow.com/questions/31927757/paintbrush-stroke-in-javafx
    // To get an understanding on how to use a stroke properly.
    public static Image createBrushImage(Node node) {

        WritableImage wi;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;

    }

        // Using http://stackoverflow.com/questions/31927757/paintbrush-stroke-in-javafx
    // To get an understanding on how to use a stroke properly.
    public static Image createPencilImage(Node node) {

        WritableImage wi;

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);

        int imageWidth = (int) node.getBoundsInLocal().getWidth();
        int imageHeight = (int) node.getBoundsInLocal().getHeight();

        wi = new WritableImage(imageWidth, imageHeight);
        node.snapshot(parameters, wi);

        return wi;

    }

    // Creates a brush based on the image
    public static Image createBrush(double radius, Color color) {

        // create gradient image with given color
        Circle brush = new Circle(radius);

        RadialGradient gradient1 = new RadialGradient(0, 0, 0, 0, radius, false, CycleMethod.NO_CYCLE, new Stop(0, color.deriveColor(1, 1, 1, 0.3)), new Stop(1, color.deriveColor(1, 1, 1, 0)));

        brush.setFill(gradient1);

        // create image
        return createBrushImage(brush);


    }

        // Creates a brush based on the image
    public static Image createPencil(double radius, Color color) {

        // create gradient image with given color
        Circle brush = new Circle(radius);

        brush.setFill(color);

        // create image
        return createPencilImage(brush);

    }

    /**
     * Gets the observable list that contains states to undo
     * @return The undo observable list
     */
    public ObservableList getObUndoList(){
        return obUndoList;
    }

    /**
     * Gets the observable list that contains states to redo
     * @return The redo observable list
     */
    public ObservableList getObRedoList(){
        return obRedoList;
    }

    /**
     * When the undo button is pressed this method undos whatever happened on the
     * canvas.
     */
    public void undoCanvas(){
        BufferedImage c = obUndoList.get(0).getKey();
        System.out.println("Function called");
        //gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        loadImage(c);
        obRedoList.add(0, obUndoList.get(0));
        obUndoList.remove(0);
        prevImage = c;

    }

    /**
     * When the redo button is pressed this method redos whatever happened on the
     * canvas.
     */
    public void redoCanvas(){
        BufferedImage c = obRedoList.get(0).getValue();
        loadImage(c);
        obUndoList.add(0,obRedoList.get(0));
        obRedoList.remove(0);
    }

    /**
     * This creates the canvas as an image
     * @return
     */
    public BufferedImage createImage(){
        SnapshotParameters param = new SnapshotParameters();
        param.setDepthBuffer(true);
        param.setFill(Color.TRANSPARENT);
        WritableImage snapshot = canvas.snapshot(param, null);
        BufferedImage tempImg = SwingFXUtils.fromFXImage(snapshot, null);
        return tempImg;
    }

    /**
     * Loads an image from the redo/undo states
     * @param img The image that needs to be undo/redo
     */
    public void loadImage(BufferedImage img){
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        WritableImage i = new WritableImage(600,600);
        SwingFXUtils.toFXImage(img, i);
        //gc.drawImage(i, 0, 0);
        render(gc, i, 600,600,0,0);

    }



    /**
     * Gets the view of the canvas
     * @return The canvas
     */
    public Canvas getView() {
        return canvas;
    }

    /**
     * Is the undo list empty?
     * @return True if empty false otherwise
     */
    public boolean isUndoEmpty(){
        return undoList.isEmpty();
    }

    /**
     * Improved gc.drawImg because it doesnt smooth anything. Adapted from
     * stackoverflow code segment found here:
     * http://stackoverflow.com/questions/22725813/javafx-disable-image-smoothing-on-canvas-object
     * @param context The graphics context we need to get the pixel writer from
     * @param image The image to be drawn
     * @param width The width of the image
     * @param height The height of the image
     * @param srcX The source X of the image (TODO with destination X)
     * @param srcY The source Y of the image (TODO with destination Y)
     */
    public void render(GraphicsContext context, WritableImage image, int width, int height, int srcX, int srcY) {
      PixelReader reader = image.getPixelReader();
      PixelWriter writer = context.getPixelWriter();
      for (int y = 0; y < height; y++){
        for (int x = 0; x < width; x++){
            Color color = reader.getColor(srcX+x,srcY+y);
            writer.setColor(x,y, color);

         }
      }
   }
    /**
     * Is the redo list empty?
     * @return True if empty false otherwise
     */
    public boolean isRedoEmpty(){
        return redoList.isEmpty();
    }

}
