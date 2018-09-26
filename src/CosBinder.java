import javafx.beans.property.DoubleProperty;

public class CosBinder extends OpBinder
{
    public CosBinder(DoubleProperty arg)
    {
        super(arg, null);
    }

    protected void update(DoubleProperty lhs, DoubleProperty rhs, DoubleProperty value)
    {
        double argVal = lhs.getValue();

        value.setValue(Math.cos(argVal));
    }
}
