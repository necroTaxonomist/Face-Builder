import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ModelSphere extends Group implements ModelShape
{
    private Coord center;
    private DoubleProperty r;

    private double[][] tfMatrix;
    private DoubleProperty rScale;

    private Circle outline;
    private Circle centerDot;

    private PropGroup pg;

    public ModelSphere(double x, double y, double z, double _r)
    {
        this(new SimpleDoubleProperty(x),
             new SimpleDoubleProperty(y),
             new SimpleDoubleProperty(z),
             new SimpleDoubleProperty(_r));
    }

    public ModelSphere(DoubleProperty xdp,
                       DoubleProperty ydp,
                       DoubleProperty zdp,
                       DoubleProperty rdp)
    {
        center = new Coord(xdp, ydp, zdp);
        center.setChangeCallback(
                () ->
                {
                    update();
                }
            );

        r = rdp;
        r.addListener(
                (o, oldVal, newVal) ->
                {
                    update();
                }
            );

        tfMatrix = Matrix.ident(4);
        rScale = new SimpleDoubleProperty(1);

        initShape();
    }

    private void initShape()
    {
        outline = new Circle();
        outline.setFill(Color.TRANSPARENT);
        outline.setStroke(Color.BLACK);

        centerDot = new Circle(DOT_SIZE);
        centerDot.setFill(Color.BLACK);
        centerDot.addEventHandler(MouseEvent.MOUSE_PRESSED,
            (MouseEvent me) ->
            {
                System.out.println("shper");
                if (pg != null)
                    Face.setShownPropGroup(pg);
            }
        );

        getChildren().add(outline);
        getChildren().add(centerDot);

        update();
    }

    public void update()
    {
        //System.out.print("UPDATE\n");
        //Matrix.printMat(tfMatrix);
        //Matrix.printVec(center.getValue());

        double[] pos = Matrix.tf(tfMatrix, center.getValue());

        //Matrix.printVec(pos);

        outline.setCenterX(pos[0]);
        outline.setCenterY(pos[1]);

        outline.setRadius(r.getValue() * rScale.getValue());

        centerDot.setCenterX(pos[0]);
        centerDot.setCenterY(pos[1]);
    }

    public void setTF(double[][] mat, DoubleProperty scale)
    {
        tfMatrix = mat;
        rScale = scale;
        update();
    }

    public void setPropGroup(PropGroup _pg)
    {
        pg = _pg;
    }

    public void flip()
    {
        center.setMirror(-1, 1, 1);
    }
}
