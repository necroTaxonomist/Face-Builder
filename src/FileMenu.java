
import java.io.File;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

public class FileMenu extends Menu
{
    private XMLModel face;

    private MenuItem newItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem saveAsItem;

    public FileMenu(XMLModel _face)
    {
        super("File");

        face = _face;

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
        openItem.setOnAction((e) ->
        {
            XMLChooser chooser = new XMLChooser("Open", 'r');
            System.out.println(chooser.choose());
        });
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
        saveAsItem.setOnAction((e) ->
        {
            XMLChooser chooser = new XMLChooser("Save", 'w');
            System.out.println(chooser.choose());
        });
    }


    private static class XMLChooser
    {
        private FileChooser fc;
        private char rw;

        public XMLChooser(String title, char _rw)
        {
            fc = new FileChooser();
            rw = _rw;

            fc.setTitle(title);
            fc.setInitialDirectory(new File(System.getProperty("user.dir")));

            fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        }

        public String choose()
        {
            File f = null;

            if (rw == 'r')
                f = fc.showOpenDialog(Face.getMainStage());
            else if (rw == 'w')
                f = fc.showSaveDialog(Face.getMainStage());

            if (f != null)
                return f.getPath();
            else
                return null;
        }
    }

}
