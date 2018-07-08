import javafx.application.Application;

import javafx.scene.control.MenuBar;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javafx.scene.Scene;

import javafx.stage.Stage;

public class Face extends Application
{
    public static Stage mainStage;
    public static Scene mainScene;
    public static Pane mainPane;

    public static final int WINDOW_WIDTH = 1000;
    public static final int WINDOW_HEIGHT = 800;

    public static final double AXIS_SIZE = 1.2;

    public static ModelPane model;
    public static RotatePane rot;
    public static TFPane tf;
    public static XMLModel face;
    public static MenuBar menuBar;

    private static Face mainFace;

    public static void main(String[] args)
    {
        launch(args);
    }

    public void start(Stage primaryStage)
    {
        // set main stage
        mainStage = primaryStage;

        // set main pane
        setPane(new BorderPane());

        // set main face
        mainFace = this;

        // init rotate pane
        rot = new RotatePane();

        // init transform pane
        tf = new TFPane();

        // Bottom vbox
        VBox bottom = new VBox();
        bottom.getChildren().add(rot);
        bottom.getChildren().add(tf);

        // init model
        model = new ModelPane(AXIS_SIZE);
        model.setYaw(rot.getYaw());
        model.setPitch(rot.getPitch());
        model.setBG("/test.jpg");
        model.setBGTF(tf);

        face = new XMLModel(model, "xml/ModelFace.xml");

        // init menu bar
        menuBar = new MenuBar();
        menuBar.getMenus().add(new FileMenu(face));

        // add to main pane
        ((BorderPane)mainPane).setCenter(model);
        ((BorderPane)mainPane).setBottom(bottom);
        ((BorderPane)mainPane).setTop(menuBar);

        // show
        mainStage.show();
    }

    private void setPane(Pane pane)
    {
        mainPane = pane;
        mainScene = new Scene(mainPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        mainStage.setScene(mainScene);
    }

    public static void setShownPropGroup(PropGroup pg)
    {
        ((BorderPane)mainFace.mainPane).setRight(pg);
        System.out.println("Switched to " + pg);
    }

    public static Stage getMainStage()
    {
        return mainFace.mainStage;
    }
}
