
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class FileMenu extends Menu
{
    private ModelFace mf;

    private MenuItem newItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem saveAsItem;

    public FileMenu(ModelFace _mf)
    {
        super("File");

        mf = _mf;

        initNewItem();
        initOpenItem();
        initSaveItem();
        initSaveAsItem();
    }

    private void initNewItem()
    {
        newItem = new MenuItem("New");
        getItems().add(newItem);
    }

    private void initOpenItem()
    {
        openItem = new MenuItem("Open");
        getItems().add(openItem);
    }

    private void initSaveItem()
    {
        saveItem = new MenuItem("Save");
        getItems().add(saveItem);
    }

    private void initSaveAsItem()
    {
        saveAsItem = new MenuItem("Save As...");
        getItems().add(saveAsItem);
    }


}
