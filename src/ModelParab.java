import java.util.HashMap;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ModelParab extends Group implements ModelShape
{
    private Coord center;
    private Coord wh;
    private DoubleProperty rot;
    
    private double[][] tlMatrix;
    private double[][] rotMatrix;
    private double[][] tfMatrix;
    
    private int res;
    private Line[] lines;
    private Circle[] dots;
    
    private PropGroup pg;
    
    private HashMap<DoubleProperty, DoubleProperty> recordedR;
    
    public ModelParab(int _res,
                      double x, double y, double z,
                      double w, double h,
                      double _rot)
    {
        this(_res,
             new SimpleDoubleProperty(x),
             new SimpleDoubleProperty(y),
             new SimpleDoubleProperty(z),
             new SimpleDoubleProperty(w),
             new SimpleDoubleProperty(h),
             new SimpleDoubleProperty(_rot));
    }
    
    public ModelParab(int _res,
                      DoubleProperty xdp, DoubleProperty ydp, DoubleProperty zdp,
                      DoubleProperty wdp, DoubleProperty hdp,
                      DoubleProperty rotdp)
    {
        center = new Coord(xdp, ydp, zdp);
        center.setChangeCallback(
                () ->
                {
                    updateTl();
                    update();
                }
            );
        
        wh = new Coord(wdp, hdp);
        wh.setChangeCallback(
                () ->
                {
                    update();
                }
            );
        
        rot = rotdp;
        rot.addListener(
                (o, oldVal, newVal) ->
                {
                    updateRot();
                    update();
                }
            );
        
        res = _res;
        lines = new Line[res - 1];
        dots = new Circle[3];
        
        updateTl();
        updateRot();
        tfMatrix = Matrix.ident(4);
        
        initShape();
        
        recordedR = new HashMap<DoubleProperty, DoubleProperty>();
    }
    
    private void initShape()
    {
        for (int i = 0; i < res - 1; ++i)
        {
            lines[i] = new Line();
            lines[i].setStroke(Color.BLACK);
            getChildren().add(lines[i]);
        }
        
        for (int j = 0; j < 3; ++j)
        {
            dots[j] = new Circle(DOT_SIZE);
            dots[j].setFill(Color.BLACK);
            
            dots[j].addEventHandler(MouseEvent.MOUSE_PRESSED,
                (MouseEvent me) -> 
                {
                    if (pg != null)
                        Face.setShownPropGroup(pg);
                }
            );
            
            getChildren().add(dots[j]);
        }
    }
    
    public void update()
    {
        // get points
        double[] whVal = wh.getValue();
        double w = whVal[0];
        double h = whVal[1];
        
        double[][] vert = new double[4][res];
        
        double theta = 0;
        for (int c = 0; c < res; ++c)
        {
            theta = ((double)c / (res - 1)) * Math.PI;
            
            vert[0][c] = xOfTheta(theta);
            vert[1][c] = yOfX(vert[0][c]);
            vert[2][c] = 0;
            vert[3][c] = 1;
        }
        
        vert = Matrix.mult(rotMatrix, vert);
        vert = Matrix.mult(tlMatrix, vert);
        
        for (int c = 0; c < res; ++c)
        {
            double x = vert[0][c];
            double y = vert[1][c];
            double z = vert[2][c];
        }
        
        vert = Matrix.mult(tfMatrix, vert);
        
        // set lines
        lines[0].setStartX(vert[0][0]);
        lines[0].setStartY(vert[1][0]);
        
        for (int i = 1; i < res - 1; ++i)
        {
            lines[i-1].setEndX(vert[0][i]);
            lines[i-1].setEndY(vert[1][i]);
            
            lines[i].setStartX(vert[0][i]);
            lines[i].setStartY(vert[1][i]);
        }
        
        lines[res-2].setEndX(vert[0][res-1]);
        lines[res-2].setEndY(vert[1][res-1]);
        
        // set control dots
        dots[0].setCenterX(vert[0][0]);
        dots[0].setCenterY(vert[1][0]);
        
        dots[2].setCenterX(vert[0][res-1]);
        dots[2].setCenterY(vert[1][res-1]);
        
        if (res % 2 != 0)
        {
            dots[1].setCenterX(vert[0][res / 2]);
            dots[1].setCenterY(vert[1][res / 2]);
        }
        else
        {
            double[] middle = {0, h, 0, 1};
            
            middle = Matrix.tf(rotMatrix, middle);
            middle = Matrix.tf(tlMatrix, middle);
            middle = Matrix.tf(tfMatrix, middle);
            
            dots[1].setCenterX(middle[0]);
            dots[1].setCenterY(middle[1]);
        }
    }
    
    private void updateTl()
    {
        double[] pos = center.getValue();
        tlMatrix = Matrix.tl(pos[0], pos[1], pos[2]);
    }
    
    private void updateRot()
    {
        rotMatrix = Matrix.rotX(rot.getValue());
    }
    
    private double xOfTheta(double theta)
    {
        double[] whVal = wh.getValue();
        double w = whVal[0];
        double h = whVal[1];
        
        if (theta == 0)
            return w / 2;
        else if (theta == Math.PI / 2)
            return 0;
        else if (theta == Math.PI)
            return -w / 2;
        
        double A = -4 * h / (w * w) * Math.cos(rot.getValue());
        double B = w / 2;
        
        double a = A;
        double b = -Math.tan(theta);
        double c = -A * B * B;
        
        double disc = b*b - 4*a*c;
        
        double x1 = (-b - Math.sqrt(disc)) / (2*a);
        double x2 = (-b + Math.sqrt(disc)) / (2*a);
        
        return (theta < Math.PI / 2) ? -x1 : -x2;
    }
    
    private double yOfX(double x)
    {
        double[] whVal = wh.getValue();
        double w = whVal[0];
        double h = whVal[1];
        
        double A = -4 * h / (w * w);
        double B = w / 2;
        
        return A * (x + B) * (x - B);
    }
    
    public void setTF(double[][] mat, DoubleProperty scale)
    {
        tfMatrix = mat;
        update();
    }
    
    public void setPropGroup(PropGroup _pg)
    {
        pg = _pg;
    }
    
    public DoubleProperty getRProp(DoubleProperty theta)
    {
        DoubleProperty rProp = recordedR.getOrDefault(theta, null);
        if (rProp == null)
        {
            rProp = recordR(theta);
        }
        
        return rProp;
    }
    
    private DoubleProperty recordR(DoubleProperty theta)
    {
        DoubleProperty rProp = new SimpleDoubleProperty(0);
        theta.addListener(
            (o, oldVal, newVal) ->
            {
                updateR((DoubleProperty)o);
            }
        );
        
        recordedR.put(theta, rProp);
        updateR(theta);
        
        return rProp;
    }
    
    private void updateR(DoubleProperty theta)
    {
        double x = xOfTheta(theta.getValue());
        double y = yOfX(x);
        
        double[][] vert = Matrix.makeColVec(x, y, 0, 1);
        
        vert = Matrix.mult(rotMatrix, vert);
        vert = Matrix.mult(tlMatrix, vert);
        
        double[] cVert = Matrix.rectToCyl(Matrix.vectorize(vert));
        
        DoubleProperty rProp = recordedR.getOrDefault(theta, null);
        if (rProp != null)
            rProp.setValue(cVert[0]);
        
        System.out.println("r=" + cVert[0]);
    }
}