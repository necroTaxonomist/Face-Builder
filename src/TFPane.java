import java.lang.Math;

import javafx.beans.property.DoubleProperty;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TFPane extends HBox
{
    DigitalSetter x, y, scale;

    public TFPane()
    {
        HBox xBox = new HBox();
        HBox yBox = new HBox();
        HBox scaleBox = new HBox();
        getChildren().addAll(xBox, yBox, scaleBox);

        xBox.getChildren().add(new Label("x:"));
        yBox.getChildren().add(new Label("y:"));
        scaleBox.getChildren().add(new Label("scale:"));

        x = new DigitalSetter(0, 1);
        y = new DigitalSetter(0, 1);
        scale = new DigitalSetter(1, .005);

        xBox.getChildren().add(x);
        yBox.getChildren().add(y);
        scaleBox.getChildren().add(scale);
    }

    public DoubleProperty getX() { return x.valueProperty(); }
    public DoubleProperty getY() { return y.valueProperty(); }
    public DoubleProperty getScale() { return scale.valueProperty(); }
}
