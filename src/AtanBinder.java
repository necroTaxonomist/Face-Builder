import javafx.beans.property.DoubleProperty;

public class AtanBinder extends OpBinder
{
    public AtanBinder(DoubleProperty opp, DoubleProperty adj)
    {
        super(opp, adj);
    }

    protected void update(DoubleProperty lhs, DoubleProperty rhs, DoubleProperty value)
    {
        double oppVal = lhs.getValue();
        double adjVal = rhs.getValue();

        value.setValue(Math.atan2(oppVal, adjVal));
    }
}
