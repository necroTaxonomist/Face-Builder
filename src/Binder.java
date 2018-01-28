import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Binder
{
    private DoubleBinding binding;
    private DoubleProperty value;
    
    public Binder(DoubleBinding _binding)
    {
        binding = _binding;
        value = new SimpleDoubleProperty(binding.getValue());
        
        binding.addListener(
                (o, oldVal, newVal) ->
                {
                    value.setValue(binding.getValue());
                }
            );
    }
    
    public DoubleProperty valueProperty()
    {
        return value;
    }
}