import java.lang.Math;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class CoordOld
{
    public static final int CTYPE_RECT = 0;
    public static final int CTYPE_CYL = 1;
    
    private int type;
    private DoubleProperty[] x;
    
    public CoordOld(int _type)
    {
        this(_type, 0, 0, 0);
    }
    
    public CoordOld(int _type, double _x, double _y, double _z)
    {
        type = _type;
        
        x = new DoubleProperty[3];
        for (int n = 0; n < 3; ++n)
            x[n] = new SimpleDoubleProperty();
        
        x[0].setValue(_x);
        x[1].setValue(_y);
        x[2].setValue(_z);
    }
    
    public CoordOld(int _type, DoubleProperty _x, DoubleProperty _y, DoubleProperty _z)
    {
        type = _type;
        
        x = new DoubleProperty[3];
        x[0] = _x;
        x[1] = _y;
        x[2] = _z;
    }
    
    public void setX(int n, double val)
    {
        x[n].setValue(val);
    }
    
    public void setX(int n, DoubleProperty prop)
    {
        x[n] = prop;
    }
    
    public void setXSum(int n, DoubleProperty p1, DoubleProperty p2)
    {
        x[n].setValue(p1.get() + p2.get());
        
        p1.addListener(
            (o, oldVal, newVal) ->
            {
                x[n].setValue(p1.get() + p2.get());
            }
        );
        
        p2.addListener(
            (o, oldVal, newVal) ->
            {
                x[n].setValue(p1.get() + p2.get());
            }
        );
    }
    
    public void setXScalar(int n, DoubleProperty prop, double scalar)
    {
        x[n].setValue(prop.get() * scalar);
        
        prop.addListener(
            (o, oldVal, newVal) ->
            {
                x[n].setValue(prop.get() * scalar);
            }
        );
    }
    
    public double getX()
    {
        switch (type)
        {
            case CTYPE_RECT:
                return x[0].get();
            case CTYPE_CYL:
                return x[0].get() * Math.cos(x[1].get());
            default:
                return 0;
        }
    }
    public double getY()
    {
        switch (type)
        {
            case CTYPE_RECT:
                return x[1].get();
            case CTYPE_CYL:
                return x[0].get() * Math.sin(x[1].get());
            default:
                return 0;
        }
    }
    public double getZ()
    {
        return x[2].get();
    }
    
    public double getR()
    {
        switch (type)
        {
            case CTYPE_RECT:
                return Math.sqrt(x[0].get()*x[0].get() + x[1].get()*x[1].get());
            case CTYPE_CYL:
                return x[0].get();
            default:
                return 0;
        }
    }
    public double getTheta()
    {
        switch (type)
        {
            case CTYPE_RECT:
                double theta = Math.atan2(x[1].get(), x[0].get());
                return (theta < 0) ? (theta + 2 * Math.PI) : (theta);
            case CTYPE_CYL:
                return x[1].get();
            default:
                return 0;
        }
    }
    
    public double[] getRect()
    {
        double[] c = new double[4];
        
        c[0] = getX();
        c[1] = getY();
        c[2] = getZ();
        c[3] = 1;
        
        return c;
    }
    
    public double[] getCyl()
    {
        double[] c = new double[4];
        
        c[0] = getR();
        c[1] = getTheta();
        c[2] = getZ();
        c[3] = 1;
        
        return c;
    }
}