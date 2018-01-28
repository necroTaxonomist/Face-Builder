import java.util.ArrayList;

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
    
    public ModelParab(int _res,
                      double x, double y, double z,
                      double w, double h,
                      double _rot)
    {
        center = new Coord(x, y, z);
        center.setChangeCallback(
                () ->
                {
                    updateTl();
                    update();
                }
            );
        
        wh = new Coord(w, h);
        wh.setChangeCallback(
                () ->
                {
                    update();
                }
            );
        
        rot = new SimpleDoubleProperty(_rot);
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
        
        for (int c = 0; c < res; ++c)
        {
            vert[0][c] = w * ((double)c/(res-1) - .5);
            
            vert[1][c] = yOfX(vert[0][c]);
            
            vert[2][c] = 0;
            
            vert[3][c] = 1;
        }
        
        vert = Matrix.mult(rotMatrix, vert);
        vert = Matrix.mult(tlMatrix, vert);
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
        
        double A = -4 * h / (w * w);
        double B = w / 2;
        
        double disc = Math.pow(Math.tan(theta), 2) + Math.pow(2*A*B, 2);
        
        double x1 = (Math.tan(theta) + Math.sqrt(disc)) / (2*A);
        double x2 = (Math.tan(theta) - Math.sqrt(disc)) / (2*A);
        
        return (theta > Math.PI / 2 != x1 > 0) ? x1 : x2;
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
}