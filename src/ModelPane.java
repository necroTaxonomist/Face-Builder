import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;

public class ModelPane extends Pane
{
    private DoubleProperty yaw, pitch, zoom;
    private DoubleProperty width, height;
    
    private ArrayList<ModelShape> shapes;
    
    private double axisSize;
    
    private double[][] screenTF;
    private double[][] rotTF;
    private double[][] zoomTF;
    private double[][] originTF;
    
    public ModelPane(double _axisSize)
    {
        yaw = new SimpleDoubleProperty(0);
        pitch = new SimpleDoubleProperty(0);
        zoom = new SimpleDoubleProperty(Face.WINDOW_HEIGHT / 2 / _axisSize);
        
        width = new SimpleDoubleProperty(Face.WINDOW_HEIGHT * .95);
        height = new SimpleDoubleProperty(Face.WINDOW_HEIGHT * .95);
        
        shapes = new ArrayList<ModelShape>();
        
        axisSize = _axisSize;
        
        screenTF = new double[4][4];
        makeAllTF();
        
        addAxes();
    }
    
    public void add(ModelShape ms)
    throws IllegalArgumentException
    {
        if (!(ms instanceof Node))
            throw new IllegalArgumentException("Provided ModelShape is not a Node");
        
        shapes.add(ms);
        getChildren().add((Node)ms);
        
        //System.out.println("ADDING");
        ms.setTF(screenTF, zoom);
    }
    
    private void makeScreenTF()
    {
        double[][] newTF = Matrix.mult(originTF, Matrix.mult(zoomTF, rotTF));
        
        for (int r = 0; r < 4; ++r)
        {
            for (int c = 0; c < 4; ++c)
            {
                screenTF[r][c] = newTF[r][c];
            }
        }
    }
    
    private void makeRotTF()
    {
        rotTF = Matrix.yawPitch(-yaw.getValue(), -pitch.getValue());
    }
    
    private void makeZoomTF()
    {
        zoomTF = Matrix.dil(zoom.getValue(), -zoom.getValue(), 1);
    }
    
    private void makeOriginTF()
    {
        originTF = Matrix.tl(width.getValue() / 2, height.getValue() / 2, 0);
        
        //System.out.println("ORIGINTF");
        //Matrix.printMat(originTF);
    }
    
    private void makeAllTF()
    {
        makeRotTF();
        makeZoomTF();
        makeOriginTF();
        makeScreenTF();
        updateAll();
    }
    
    private void updateAll()
    {
        for (ModelShape ms : shapes)
        {
            ms.update();
        }
    }
    
    public void setYaw(DoubleProperty _yaw)
    {
        yaw = _yaw;
        
        yaw.addListener(
                (o, oldVal, newVal) ->
                {
                    makeRotTF();
                    makeScreenTF();
                    updateAll();
                }
            );
        
        makeRotTF();
        makeScreenTF();
        updateAll();
    }
    
    public void setPitch(DoubleProperty _pitch)
    {
        pitch = _pitch;
        
        pitch.addListener(
                (o, oldVal, newVal) ->
                {
                    makeRotTF();
                    makeScreenTF();
                    updateAll();
                }
            );
        
        makeRotTF();
        makeScreenTF();
        updateAll();
    }
    
    public void addAxes()
    {
        ModelLine xAxis = new ModelLine(-axisSize, 0, 0, axisSize, 0, 0);
        xAxis.setColor(Color.RED);
        add(xAxis);
        
        ModelLine yAxis = new ModelLine(0, -axisSize, 0, 0, axisSize, 0);
        yAxis.setColor(Color.GREEN);
        add(yAxis);
        
        ModelLine zAxis = new ModelLine(0, 0, -axisSize, 0, 0, axisSize);
        zAxis.setColor(Color.BLUE);
        add(zAxis);
    }
}