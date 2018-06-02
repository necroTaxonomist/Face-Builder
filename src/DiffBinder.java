import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class DiffBinder extends Binder
{
    public DiffBinder(DoubleProperty dp1, DoubleProperty dp2)
    {
        super(dp1.subtract(dp2));
    }
}