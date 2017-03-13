/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sallenlab5;

import com.sun.glass.ui.Window;
import java.awt.Desktop;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Optional;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author scott allen
 */
public class SAllenLab5 extends Application {   
    
    private enum ToolBarPos {
        LEFT, TOP, RIGHT, BOTTOM;
    }
    
    private Label mStatus;
    private BorderPane root;
    private ToolBar toolbar;
    private Point2D.Double mFrom, mTo;
    private Canvas mPermCanvas, mTempCanvas;
    private StringProperty status = new SimpleStringProperty("");
    private int mLineWidth = 1;
    private ToggleGroup mColorTG;
    private ToggleGroup mWidthTG;
    private ToolBarPos mToolbarPos = ToolBarPos.LEFT;
    private VBox mTopMenuVB;
    private VBox mBottomVB;
    private ToolBar mToolBar;
    private boolean mSave = false;
    private File mFile;
    private ArrayList<Line> mLines;
    private Stage mStage;
    private Color mColor;
    
    
    
    @Override
    public void start(Stage primaryStage) {       
        root = new BorderPane();
        
        //pane to hold overlayed canvases
        StackPane canvasStack = new StackPane();
        
        mFile = null;
        mLines = new ArrayList<>();    
        mColor = Color.BLACK;
        
        mPermCanvas = new Canvas(400, 400);
        mTempCanvas = new Canvas(400, 400);   
        
        canvasStack.getChildren().add(mTempCanvas);
        canvasStack.getChildren().add(mPermCanvas);
        fillCanvas(mTempCanvas, false);
        fillCanvas(mPermCanvas, true);
        ScrollPane mSPane = new ScrollPane(canvasStack);
        root.setCenter(mSPane);
        
        //initialize line end points
        mFrom = new Point2D.Double();
        mTo   = new Point2D.Double();
        
        //event handlers for drawing lines
        mPermCanvas.setOnMousePressed(mouseEvent->onMousePressed(mouseEvent));
        mPermCanvas.setOnMouseDragged(mouseEvent->onMouseDrag(mouseEvent, Paint.valueOf("BLACK")));
        mPermCanvas.setOnMouseReleased(mouseEvent->onMouseRelease(mouseEvent));


        MenuBar menuBar = buildMenus();
        mTopMenuVB = new VBox(menuBar);
        root.setTop(mTopMenuVB);
        
        mToolBar = buildToolBar();
        root.setLeft(mToolBar);
        
        mStatus = new Label("");
        toolbar = new ToolBar(mStatus);
        mBottomVB = new VBox(toolbar);
        root.setBottom(mBottomVB);
        
        mStage = primaryStage;
        Scene scene = new Scene(root);
        
        primaryStage.setOnCloseRequest(actionEvent -> onExit());
        primaryStage.setTitle("(Untitled)");
        primaryStage.sizeToScene();
        primaryStage.setScene(scene);
        primaryStage.show();
    }
   
    
    
    
    public ToolBar buildToolBar(){
        // build menu bar
        ToolBar toolBar = new ToolBar();
        toolBar.setOrientation(Orientation.VERTICAL);
        
        Button newButt     = new Button(),
               openButt    = new Button(), 
               saveButt    = new Button(), 
               widthButt   = new Button(), 
               colorButt   = new Button(), 
               moveButt    = new Button();
                
        ImageView buttImage = new ImageView(new Image("ButtonImages/New.png"));                
        newButt.setGraphic(buttImage);

        buttImage = new ImageView(new Image("ButtonImages/Open.png"));
        openButt.setGraphic(buttImage);

        buttImage = new ImageView(new Image("ButtonImages/Save.png"));
        saveButt.setGraphic(buttImage);

        buttImage = new ImageView(new Image("ButtonImages/Width.png"));
        widthButt.setGraphic(buttImage);

        buttImage = new ImageView(new Image("ButtonImages/Color.png"));
        colorButt.setGraphic(buttImage); 

        buttImage = new ImageView(new Image("ButtonImages/Move.png"));
        moveButt.setGraphic(buttImage);            

        //add buttons to toolbar
        toolBar.getItems().addAll(
                newButt, 
                openButt, 
                saveButt, 
                new Separator(), 
                widthButt, 
                new Separator(), 
                colorButt, 
                new Separator(), 
                moveButt);   
        
        //set event handlers for toolbar buttons
        newButt.setOnMouseClicked(ActionEvent -> onNew());
        openButt.setOnMouseClicked(ActionEvent -> onOpen());
        saveButt.setOnMouseClicked(ActionEvent -> onSave(false));
        widthButt.setOnMouseClicked(ActionEvent -> onWidth(0));
        colorButt.setOnMouseClicked(ActionEvent -> onColor(null));
        moveButt.setOnMouseClicked(ActionEvent -> onToolbarMove());     
        
        //setting tooltip properties
        newButt.tooltipProperty().setValue(new Tooltip("Create new file"));
        openButt.tooltipProperty().setValue(new Tooltip("Open file"));
        saveButt.tooltipProperty().setValue(new Tooltip("Save file"));
        widthButt.tooltipProperty().setValue(new Tooltip("Set line width"));
        colorButt.tooltipProperty().setValue(new Tooltip("Set line color"));
        moveButt.tooltipProperty().setValue(new Tooltip("Move the image"));
        
        return toolBar;       
    }   
    
    public MenuBar buildMenus() {
        // build menu bar
        MenuBar menuBar = new MenuBar();
        
        // File menu with just a quit item for now
        Menu fileMenu = new Menu("_File");
        MenuItem newMenuItem = new MenuItem("_New");
        newMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        newMenuItem.setOnAction(actionEvent -> onNew());
        
        MenuItem openMenuItem = new MenuItem("_Open");
        openMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openMenuItem.setOnAction(actionEvent -> onOpen());
        
        MenuItem saveMenuItem = new MenuItem("_Save");
        saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveMenuItem.setOnAction(actionEvent -> onSave(false));        
        
        MenuItem saveAsMenuItem = new MenuItem("_Save_As");
        saveAsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
        saveAsMenuItem.setOnAction(actionEvent -> onSave(true));
        
        SeparatorMenuItem sep = new SeparatorMenuItem();
        
        MenuItem quitMenuItem = new MenuItem("_Quit");
        quitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        quitMenuItem.setOnAction(actionEvent -> onExit());
        
        fileMenu.getItems().addAll(newMenuItem, openMenuItem, saveMenuItem, saveAsMenuItem, sep, quitMenuItem);
        
        
        Menu widthMenu = new Menu("_Width");       
        mWidthTG = new ToggleGroup();       
        RadioMenuItem onePix   = new RadioMenuItem("_1 Pixel");
        RadioMenuItem fourPix  = new RadioMenuItem("_4 Pixels");
        RadioMenuItem eightPix = new RadioMenuItem("_8 Pixels");
        onePix.setOnAction(actionEvent -> onWidth(1));
        fourPix.setOnAction(actionEvent -> onWidth(4));
        eightPix.setOnAction(actionEvent -> onWidth(8));
        onePix.setToggleGroup(mWidthTG);
        fourPix.setToggleGroup(mWidthTG);
        eightPix.setToggleGroup(mWidthTG);       
        onePix.setSelected(true);
        widthMenu.getItems().addAll(onePix, fourPix, eightPix);
        widthMenu.setOnShowing(event -> onWidthShowing()); //
        
        
        Menu colorMenu = new Menu("_Color");
        mColorTG = new ToggleGroup();
        RadioMenuItem color_black  = new RadioMenuItem("_Black");
        RadioMenuItem color_red    = new RadioMenuItem("_Red");
        RadioMenuItem color_green  = new RadioMenuItem("_Green");
        RadioMenuItem color_blue   = new RadioMenuItem("_Blue");
        color_black.setOnAction(actionEvent -> onColor(Color.BLACK));
        color_red.setOnAction(actionEvent -> onColor(Color.RED));
        color_green.setOnAction(actionEvent -> onColor(Color.GREEN));
        color_blue.setOnAction(actionEvent -> onColor(Color.BLUE));
        color_black.setToggleGroup(mColorTG);
        color_red.setToggleGroup(mColorTG);
        color_green.setToggleGroup(mColorTG);
        color_blue.setToggleGroup(mColorTG);      
        color_black.setSelected(true);
        colorMenu.getItems().addAll(color_black, color_red, color_green, color_blue);
        colorMenu.setOnShowing(event -> onColorShowing());
        
        
        
        // Help menu with just an about item for now
        Menu helpMenu = new Menu("_Help");
        MenuItem aboutMenuItem = new MenuItem("_About");
        aboutMenuItem.setOnAction(actionEvent -> onAbout());
        helpMenu.getItems().add(aboutMenuItem);
        
        
        menuBar.getMenus().addAll(fileMenu, widthMenu, colorMenu, helpMenu);
        
        return menuBar;
    }
    
    
     
    
    
    public void setStatus(String newMStatus) {
        mStatus.setText(newMStatus);
    }
    
    private void fillCanvas(Canvas mCanvas, boolean transparent) {
        GraphicsContext canvasGC = mCanvas.getGraphicsContext2D();
        if(transparent) {
            canvasGC.setFill(Color.TRANSPARENT);
        } else {
            canvasGC.setFill(Color.WHITE);            
        }
        canvasGC.fillRect(0, 0, mCanvas.getWidth(), mCanvas.getHeight());
        mCanvas.setWidth(400);
        mCanvas.setHeight(400);
    }
 
      
    
    
    
/*
 * Drawing event handlers  
 */   
       
    private void onMousePressed(MouseEvent mousePress) {
        setStatus("");
        mFrom = new Point2D.Double();
        mTo = new Point2D.Double();
        mFrom.x = mousePress.getX();
        mFrom.y = mousePress.getY();
        mTo.y   = mousePress.getY();
        mTo.x   = mousePress.getX();
    }
    
    private void onMouseDrag(MouseEvent mouseDrag, Paint linePaint) {
        mTo.x = mouseDrag.getX();
        mTo.y = mouseDrag.getY();
        GraphicsContext gc = mTempCanvas.getGraphicsContext2D();
        if(mPermCanvas.contains(mTo.x, mTo.y)) {
            gc.fillRect(0, 0, mPermCanvas.getWidth(), mPermCanvas.getHeight());
            gc.strokeLine(mFrom.x, mFrom.y, mTo.x , mTo.y);
        }
    }    
    
    private void onMouseRelease(MouseEvent mouseRelease) {
        GraphicsContext gc = mTempCanvas.getGraphicsContext2D();
        gc.fillRect(0, 0, 400, 400);        //clear temp canvas
        
        mSave = true;
        Line newLine = new Line(mFrom, mTo, mLineWidth, new Color(mColor.getRed(), mColor.getGreen(), mColor.getBlue(), 1));
        mLines.add(newLine);
        newLine.draw(mPermCanvas);
        if(mFile != null && !mStage.getTitle().contains("*")) 
            mStage.setTitle("*" + mStage.getTitle());
    }
    
    
    
    
    
    
/*
 * Event handling methods
 */   

    private void onWidthShowing() {
        int buttonNum = 0;
        switch (mLineWidth) {
            case 1:
                buttonNum = 0;
                break;
            case 4:
                buttonNum = 1;
                break;
            case 8:
                buttonNum = 2;
                break;
        }
        mWidthTG.selectToggle(mWidthTG.getToggles().get(buttonNum));
    }    
    
    private void onColorShowing() {
        int colorNum = 0;
        if (mColor == Color.RED) {
            colorNum = 1;
        } else if (mColor == Color.GREEN) {
            colorNum = 2;
        } else if (mColor == Color.BLUE) {
            colorNum = 3;
        }
        mColorTG.selectToggle(mColorTG.getToggles().get(colorNum));
    }

    private void onOpen() {
        // TODO: if the work needs saving, call onSave() first
        if(mSave == true) {
            Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION,
                                        "Would you like to save the current work first?",
                                        ButtonType.YES,
                                        ButtonType.NO);
            saveAlert.setTitle("Current work not saved");
            //saveAlert.setHeaderText("Would you like to save the current work first?");
            
            Optional<ButtonType> choice = saveAlert.showAndWait();
            if(choice.isPresent() && choice.get() == ButtonType.YES) {
                onSave(false);      
            } else if(choice.get() == ButtonType.NO) {
                saveAlert.close();
            }
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open a line File");
        chooser.setInitialDirectory(new File("."));
        chooser.getExtensionFilters().addAll(
            new ExtensionFilter("Line Files", "*.line"),
            new ExtensionFilter("All Files", "*.*"));
        
        File selectedFile = chooser.showOpenDialog(mStage);
        
        // create an alert informing file was null???
        if(selectedFile == null) return;
        try {
            // TODO: open a stream, read the stuff, close the stream           
            FileInputStream fileStream = new FileInputStream(selectedFile);
            ObjectInputStream in = new ObjectInputStream(fileStream);
            clear();
            mLines = (ArrayList<Line>)in.readObject(); 
            
            //
            // Why can't I call defaultReadObject() here?????????????????????????????????????????????
            //
            
            for(Line l : mLines) {
                l.color = new Color(in.readDouble(), in.readDouble(), in.readDouble(), 1);
                l.draw(mPermCanvas);
            }
               
            in.close();
            mSave = false;
        } catch (IOException | ClassNotFoundException e) {
            setStatus("***Error: deserialization/stream error in onOpen()");
            return;
        }
        
        setStatus("Opened " + selectedFile.getName());
        mStage.setTitle(selectedFile.getName());
        mFile = selectedFile;
    }
    
    private void onNew() {
        if(mSave == true) {
            Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
            saveAlert.setTitle("Current work not saved");
            saveAlert.setHeaderText("Work hasn't been saved. Do you want to discard it?");
            
            Optional<ButtonType> choice = saveAlert.showAndWait();
            if(choice.get() == ButtonType.OK) {
                mSave = false;
                mFile = null;
                setStatus("(Untitled)");    
        
                GraphicsContext gc = mPermCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, 400, 400);
                status.set("Created New Canvas");        
            } else {
                saveAlert.close();
            }
        }
        mFile = null;
        setStatus("New Document created");  
        mStage.setTitle("(Untitled)");

        mLines.clear();
        clear();
        status.set("Created New Canvas");
        
    }
    
    private void onSave(boolean saveAs) {
        File selectedFile = mFile;
        
        //new save or saveAs
        if (mFile==null || saveAs) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Line File");
            fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Line Files", "*.line"),
                new ExtensionFilter("All Files", "*.*"));
            if (mFile!=null)
                fileChooser.setInitialFileName(mFile.getName());
            selectedFile = fileChooser.showSaveDialog(mStage);
        }
        
        //updating current save
        if (selectedFile != null) {
            try {
                // TODO: open a stream, write the stuff, close the stream
                FileOutputStream fileStream = new FileOutputStream(selectedFile);
                ObjectOutputStream obStream = new ObjectOutputStream(fileStream);
                obStream.writeObject(mLines);
                for(Line l : mLines) {
                    obStream.writeDouble(l.color.getRed());
                    obStream.writeDouble(l.color.getGreen());
                    obStream.writeDouble(l.color.getBlue());
                }                
                obStream.close();
            } catch (IOException ex) {
                System.out.println("The output stream couldn't find the lines file");
                return;
            }
            
            mFile = selectedFile;
            setStatus("Saved " + mFile.getName());
            mStage.setTitle(mFile.getName());
            mSave = false;
        }
    }
    
    private void onWidth(int width) {
        GraphicsContext gc  = mTempCanvas.getGraphicsContext2D();
        GraphicsContext gc2 = mPermCanvas.getGraphicsContext2D();
        if(width == 0) {
            switch (mLineWidth) {
                case 1:
                    gc.setLineWidth(4);
                    gc2.setLineWidth(4);
                    mLineWidth = 4;
                    break;
                case 4:
                    gc.setLineWidth(8);
                    gc2.setLineWidth(8);
                    mLineWidth = 8;
                    break;
                case 8:
                    gc.setLineWidth(1);
                    gc2.setLineWidth(1);
                    mLineWidth = 1;
                    break;
                default:
                    gc.setLineWidth(1);
                    gc2.setLineWidth(1);
                    mLineWidth = 1;
                    break;
            }
        } else {
            gc.setLineWidth(width);
            gc2.setLineWidth(width);
            mLineWidth = width;
        }
        
        status.set("Line width set to" + width);
    }
    
    private void onColor(Color newColor) {
        GraphicsContext gc  = mTempCanvas.getGraphicsContext2D();
        GraphicsContext gc2 = mPermCanvas.getGraphicsContext2D();        
        
        if(newColor == null) {
            mColor = Line.getNextColor(mColor);
        } else {
            mColor = newColor;
        }
        gc.setStroke(mColor);
        gc2.setStroke(mColor);        
        status.set("Line color set to " + mColor);
    }
    
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Scott Allen, CSCD 370 Lab 1, Winter 2017");
        alert.showAndWait();
    }
    
    private void onToolbarMove() {
        switch(mToolbarPos) {
            case LEFT:
                root.setLeft(null);
                mToolBar.setOrientation(Orientation.HORIZONTAL);
                mTopMenuVB.getChildren().add(mToolBar);
                mToolbarPos = ToolBarPos.TOP;
                break;
            case TOP:
                mTopMenuVB.getChildren().remove(mToolBar);
                mToolBar.setOrientation(Orientation.VERTICAL);
                root.setRight(mToolBar);
                mToolbarPos = ToolBarPos.RIGHT;
                break;
            case RIGHT:
                root.setRight(null);
                mToolBar.setOrientation(Orientation.HORIZONTAL);
                mBottomVB.getChildren().add(0,mToolBar);
                mToolbarPos = ToolBarPos.BOTTOM;
                break;
            case BOTTOM:
                mBottomVB.getChildren().remove(0);
                mToolBar.setOrientation(Orientation.VERTICAL);
                root.setLeft(mToolBar);
                mToolbarPos = ToolBarPos.LEFT;
                break;
            default:
                mToolBar.setOrientation(Orientation.VERTICAL);
                root.setLeft(mToolBar);
                mToolbarPos = ToolBarPos.LEFT;
                break;
        }
        status.set("***Move Event");
    }
    
    private void onExit() {
        if(mSave == true) {
            Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
            saveAlert.setTitle("Current work not saved");
            saveAlert.setHeaderText("Work hasn't been saved. Do you want to discard it?");
            
            Optional<ButtonType> choice = saveAlert.showAndWait();
            if(choice.get() == ButtonType.OK) {
                mSave = false;
                mFile = null;
                setStatus("(Untitled)");    
        
                GraphicsContext gc = mPermCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, 400, 400);
                status.set("Created New Canvas");        
            } else {
                saveAlert.close();
            }
        }

        Platform.exit();
    }    
    
    private void clear() {
        GraphicsContext gc = mPermCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 400, 400);
    }
    
    public ArrayList<Line> getLines() {
        return this.mLines;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }     
    
}