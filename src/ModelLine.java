import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class ModelLine extends Group implements ModelShape
{
    private Coord c1, c2;
    private double[][] tfMatrix;
    
    private Line line;
    private Circle tip;
    
    public ModelLine(double x1, double y1, double z1,
                     double x2, double y2, double z2)
    {
        c1 = new Coord(x1, y1, z1);
        c2 = new Coord(x2, y2, z2);
        
        tfMatrix = Matrix.ident(4);
        
        initShape();
    }
    
    private void initShape()
    {
        line = new Line();
        line.setStroke(Color.BLACK);
        
        tip = new Circle(DOT_SIZE);
        tip.setFill(Color.BLACK);

        getChildren().add(line);
        getChildren().add(tip);
        
        update();
    }
    
    public void update()
    {
        double[] start = Matrix.tf(tfMatrix, c1.getValue());
        double[] end = Matrix.tf(tfMatrix, c2.getValue());
        
        line.setStartX(start[0]);
        line.setStartY(start[1]);
        
        line.setEndX(end[0]);
        line.setEndY(end[1]);
        
        tip.setCenterX(end[0]);
        tip.setCenterY(end[1]);
    }
    
    public void setTF(double[][] mat, DoubleProperty scale)
    {
        tfMatrix = mat;
        update();
    }
    
    public void setPropGroup(PropGroup _pg)
    {
    }
    
    public void setColor(Paint p)
    {
        line.setStroke(p);
        tip.setFill(p);
    }
}