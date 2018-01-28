import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

// converts rectangular to polar
public class ConvertBinder
{
    private DoubleProperty r;
    private DoubleProperty theta;
    
    public ConvertBinder(DoubleProperty x, DoubleProperty z)
    {
        r = new SimpleDoubleProperty(0);
        theta = new SimpleDoubleProperty(0);
        
        x.addListener(
                (o, oldVal, newVal) ->
                {
                    update(x,z);
                }
            );
        z.addListener(
                (o, oldVal, newVal) ->
                {
                    update(x,z);
                }
            );
        
        update(x,z);
    }
    
    private void update(DoubleProperty x, DoubleProperty z)
    {
        double[] oldCoords = {x.getValue(), 0, z.getValue()};
        double[] newCoords = Matrix.rectToCyl(oldCoords);
        
        r.setValue(newCoords[0]);
        theta.setValue(newCoords[1]);
        
        //System.out.println("Theta=" + theta.getValue());
    }
    
    public DoubleProperty getR()
    {
        return r;
    }
    
    public DoubleProperty getTheta()
    {
        return theta;
    }
}