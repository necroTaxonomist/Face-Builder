import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class MeanBinder extends Binder
{
    public MeanBinder(DoubleProperty dp1, DoubleProperty dp2)
    {
        super(dp1.add(dp2).multiply(.5));
    }
}