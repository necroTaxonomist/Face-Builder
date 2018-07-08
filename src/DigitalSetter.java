import java.lang.Math;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class DigitalSetter extends HBox
{
    private DoubleProperty value;

    private Button plus, minus;
    private TextField content;

    private double increment;

    public DigitalSetter(double _value, double _increment)
    {
        value = new SimpleDoubleProperty(_value);
        increment = _increment;

        minus = new Button("-");
        minus.setOnAction((e)->
        {
            decValue();
        });
        getChildren().add(minus);

        content = new TextField("" + _value);
        content.setOnAction((e)->
        {
            try
            {
                double newVal = Double.parseDouble(content.getText());
                setValue(newVal, false);
            }
            catch (Exception ex)
            {
                content.setText("" + value.getValue());
            }
        });
        content.setPrefColumnCount(8);
        content.setAlignment(Pos.CENTER);
        getChildren().add(content);

        plus = new Button("+");
        plus.setOnAction((e)->
        {
            incValue();
        });
        getChildren().add(plus);
    }

    public void decValue()
    {
        setValue(value.getValue() - increment);
    }

    public void incValue()
    {
        setValue(value.getValue() + increment);
    }

    public void setValue(double _value)
    {
        setValue(_value, true);
    }
    private void setValue(double _value, boolean updateText)
    {
        value.setValue(_value);
        if (updateText)
            content.setText("" + _value);
    }

    public DoubleProperty valueProperty()
    {
        return value;
    }
}
