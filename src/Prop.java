import javafx.beans.property.DoubleProperty;

import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import javafx.scene.layout.VBox;

public class Prop extends VBox
{
    private Label name;
    private Slider slider;
    private double defaultVal;

    public Prop(String _name, double min, double max)
    {
        this(_name, min, max, (min + max) / 2);
    }

    public Prop(String _name, double min, double max, double value)
    {
        name = new Label(_name);
        slider = new Slider(min, max, value);
        defaultVal = value;

        getChildren().add(name);
        getChildren().add(slider);
    }

    public String getName() { return name.getText(); }
    public double getMin() { return slider.getMin(); }
    public double getMax() { return slider.getMax(); }
    public double getValue() { return slider.getValue(); }
    public DoubleProperty valueProperty() { return slider.valueProperty(); }

    public void setValue(double value)
    {
        slider.setValue(value);
    }

    public void setToDefault()
    {
        slider.setValue(defaultVal);
    }
}
