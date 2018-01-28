import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ModelBezier extends Group implements ModelShape
{
    private ArrayList<Coord> points;
    private ArrayList<Circle> dots;
    
    private int res;
    private Line[] lines;
    
    private double[][] tfMatrix;
    
    private PropGroup pg;
    
    public ModelBezier(int _res)
    {
        points = new ArrayList<Coord>();
        dots = new ArrayList<Circle>();
        
        res = _res;
        lines = new Line[res - 1];
        
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
    }
    
    public void update()
    {
        // get control point coordinates
        
        double[][] control = new double[4][points.size()];
        for (int c = 0; c < points.size(); ++c)
        {
            double[] col = getValue(c);
            
            for (int r = 0; r < 4; ++r)
            {
                control[r][c] = col[r];
            }
        }
        
        // generate t values
        
        double[] t = new double[res];
        for (int i = 0; i < res; ++i)
        {
            t[i] = (double)i / (res - 1);
        }
        
        // draw connecting lines
        
        double[][] vert = Bezier.bezier(t, control);
        
        vert = Matrix.mult(tfMatrix, vert);
        
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
        
        // draw control points as dots
        
        control = Matrix.mult(tfMatrix, control);
        
        for (int i = 0; i < dots.size(); ++i)
        {
            dots.get(i).setCenterX(control[0][i]);
            dots.get(i).setCenterY(control[1][i]);
        }
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
    
    public void addPoint(Coord c)
    {
        points.add(c);
        c.setChangeCallback(
                () ->
                {
                    update();
                }
            );
        
        Circle newDot = new Circle(DOT_SIZE);
        newDot.setFill(Color.BLACK);
        
        newDot.addEventHandler(MouseEvent.MOUSE_PRESSED,
                (MouseEvent me) -> 
                {
                    System.out.println("bezr");
                    if (pg != null)
                        Face.setShownPropGroup(pg);
                }
            );
        
        dots.add(newDot);
        getChildren().add(newDot);
    }
    
    private double[] getValue(int n)
    {
        return points.get(n).getValue();
    }
    
    public void flip()
    {
        for (Coord c : points)
        {
            c.setMirror(-1, 1, 1);
        }
    }
}