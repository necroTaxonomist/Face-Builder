import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class SumBinder extends Binder
{
    public SumBinder(DoubleProperty dp1, DoubleProperty dp2)
    {
        super(dp1.add(dp2));
    }
}