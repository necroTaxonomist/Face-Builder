import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;


public abstract class OpBinder extends Binder
{
    private DoubleProperty lhs;
    private DoubleProperty rhs;
    private DoubleProperty value;

    public OpBinder(DoubleProperty _lhs, DoubleProperty _rhs)
    {
        super(null);

        lhs = _lhs;
        rhs = _rhs;
        value = new SimpleDoubleProperty(0);

        if (lhs != null)
        {
            lhs.addListener(
                    (o, oldVal, newVal) ->
                    {
                        update(lhs, rhs, value);
                    }
                );
        }

        if (rhs != null)
        {
            rhs.addListener(
                    (o, oldVal, newVal) ->
                    {
                        update(lhs, rhs, value);
                    }
                );
        }

        update(lhs, rhs, value);
    }

    public DoubleProperty valueProperty()
    {
        return value;
    }

    protected abstract void update(DoubleProperty lhs, DoubleProperty rhs, DoubleProperty value);
}
