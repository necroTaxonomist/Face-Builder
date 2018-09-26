import javafx.beans.property.DoubleProperty;

public class PowerBinder extends OpBinder
{
    public PowerBinder(DoubleProperty base, DoubleProperty exp)
    {
        super(base, exp);
    }

    protected void update(DoubleProperty lhs, DoubleProperty rhs, DoubleProperty value)
    {
        double baseVal = lhs.getValue();
        double expVal = rhs.getValue();

        value.setValue(Math.pow(baseVal, expVal));
    }
}
