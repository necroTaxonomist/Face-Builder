import javafx.beans.property.DoubleProperty;

public interface ModelShape
{
    public static final int DOT_SIZE = 3;
    
    public void update();
    public void setTF(double[][] mat, DoubleProperty scale);
    public void setPropGroup(PropGroup _pg);
}