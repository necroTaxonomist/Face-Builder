import javafx.beans.property.DoubleProperty;

public class CylCoord extends Coord
{
    public CylCoord()
    {
        super();
        setType(Coord.CTYPE_CYL);
    }
    
    public CylCoord(double ... _n)
    {
        super(_n);
        setType(Coord.CTYPE_CYL);
    }
    
    public CylCoord(DoubleProperty ... _dp)
    {
        super(_dp);
        setType(Coord.CTYPE_CYL);
    }
}