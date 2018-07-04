import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

// offsets a rectanglar coord by a radius and angle
public class OffsetBinder
{
    private DoubleProperty centerX;
    private DoubleProperty centerY;

    private DoubleProperty radius;
    private DoubleProperty angle;

    private DoubleProperty endX;
    private DoubleProperty endY;

    public OffsetBinder(DoubleProperty _centerX, DoubleProperty _centerY,
                        DoubleProperty _radius, DoubleProperty _angle)
    {
        centerX = _centerX;
        centerY = _centerY;
        radius = _radius;
        angle = _angle;

        endX = new SimpleDoubleProperty(0);
        endY = new SimpleDoubleProperty(0);

        centerX.addListener( (o, oldVal, newVal) -> { update(); } );
        centerY.addListener( (o, oldVal, newVal) -> { update(); } );
        radius.addListener( (o, oldVal, newVal) -> { update(); } );
        angle.addListener( (o, oldVal, newVal) -> { update(); } );

        update();
    }

    private void update()
    {
        double newX = centerX.getValue() + radius.getValue() * Math.cos(angle.getValue());
        double newY = centerY.getValue() + radius.getValue() * Math.sin(angle.getValue());

        endX.setValue(newX);
        endY.setValue(newY);
    }

    public DoubleProperty getX()
    {
        return endX;
    }

    public DoubleProperty getY()
    {
        return endY;
    }
}
