import javafx.beans.property.DoubleProperty;

public class SinBinder extends OpBinder
{
    public SinBinder(DoubleProperty arg)
    {
        super(arg, null);
    }

    protected void update(DoubleProperty lhs, DoubleProperty rhs, DoubleProperty value)
    {
        double argVal = lhs.getValue();

        value.setValue(Math.sin(argVal));
    }
}
