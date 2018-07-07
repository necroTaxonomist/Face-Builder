import java.util.ArrayList;

import javafx.beans.property.DoubleProperty;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PropGroup extends VBox
{
    private Label name;
    private ArrayList<Prop> props;

    public PropGroup(String _name)
    {
        name = new Label(_name);
        props = new ArrayList<Prop>();

        getChildren().add(name);
    }

    public void add(String name, double min, double max)
    {
        Prop p = new Prop(name, min, max, (min + max)/2);
        addProp(p);
    }

    public void add(String name, double min, double max, double value)
    {
        Prop p = new Prop(name, min, max, value);
        addProp(p);
    }

    public void addProp(Prop p)
    {
        props.add(p);
        getChildren().add(p);
    }

    public Prop getProp(String name)
    {
        for (Prop p : props)
        {
            if (p.getName().equals(name))
                return p;
        }
        return null;
    }

    public Prop getProp(int index)
    {
        return props.get(index);
    }

    public DoubleProperty valueProperty(String name)
    {
        for (Prop p : props)
        {
            if (p.getName().equals(name))
                return p.valueProperty();
        }
        return null;
    }

    public Prop searchProps(String contains)
    {
        for (Prop p : props)
        {
            if (p.getName().indexOf(contains) >= 0)
                return p;
        }
        return null;
    }

    public String getName()
    {
        return name.getText();
    }

    public int getNumProps()
    {
        return props.size();
    }
}
