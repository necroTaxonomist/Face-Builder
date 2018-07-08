import java.lang.Math;

import javafx.beans.property.DoubleProperty;

import javafx.geometry.Orientation;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RotatePane extends HBox
{
    private DoubleProperty yaw, pitch;

    public RotatePane()
    {
        VBox yawBox = new VBox();
        VBox pitchBox = new VBox();
        getChildren().addAll(yawBox, pitchBox);

        yawBox.getChildren().add(new Label("Yaw"));
        pitchBox.getChildren().add(new Label("Pitch"));

        Slider yawSlider = new Slider(-Math.PI, Math.PI, 0);
        Slider pitchSlider = new Slider(-Math.PI / 2, Math.PI / 2, 0);
        //pitchSlider.setOrientation(Orientation.VERTICAL);

        yawBox.getChildren().add(yawSlider);
        pitchBox.getChildren().add(pitchSlider);

        yaw = yawSlider.valueProperty();
        pitch = pitchSlider.valueProperty();
    }

    public DoubleProperty getYaw() { return yaw; }
    public DoubleProperty getPitch() { return pitch; }
}
