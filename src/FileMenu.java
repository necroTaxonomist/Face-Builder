
import java.io.File;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

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

    private String working;

    public FileMenu(XMLModel _face)
    {
        super("File");

        face = _face;

        initNewItem();
        initOpenItem();
        initSaveItem();
        initSaveAsItem();

        working = null;
    }

    private void initNewItem()
    {
        newItem = new MenuItem("New");
        getItems().add(newItem);
        newItem.setOnAction((e) ->
        {
            newFile();
        });
    }

    private void initOpenItem()
    {
        openItem = new MenuItem("Open");
        getItems().add(openItem);
        openItem.setOnAction((e) ->
        {
            openFile();
        });
    }

    private void initSaveItem()
    {
        saveItem = new MenuItem("Save");
        getItems().add(saveItem);
        saveItem.setOnAction((e) ->
        {
            saveFile();
        });
    }

    private void initSaveAsItem()
    {
        saveAsItem = new MenuItem("Save As...");
        getItems().add(saveAsItem);
        saveAsItem.setOnAction((e) ->
        {
            saveAsFile();
        });
    }

    private void newFile()
    {
        if (face != null && face.isChanged() && askForSave())
        {
            working = null;
            face.reset();
        }
    }

    private void openFile()
    {
        if (face != null && face.isChanged() && !askForSave())
        {
            return;
        }

        XMLChooser chooser = new XMLChooser("Open", 'r');
        String fn = chooser.choose();
        if (fn != null && face != null)
        {
            face.loadStateFromFile(fn);
            working = fn;
            face.resetChange();
        }
    }

    private void saveFile()
    {
        if (working == null)
            saveAsFile();
        else
        {
            face.saveStateToFile(working);
            face.resetChange();
        }
    }

    private void saveAsFile()
    {
        XMLChooser chooser = new XMLChooser("Save", 'w');
        String fn = chooser.choose();
        if (fn != null && face != null)
        {
            face.saveStateToFile(fn);
            working = fn;
            face.resetChange();
        }
    }

    private boolean askForSave()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText("Save the current file before continuing?");

        ButtonType saveBtn = new ButtonType("Yes");
        ButtonType continueBtn = new ButtonType("No");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, continueBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == continueBtn)
        {
            return true;
        }
        else if (result.get() == saveBtn)
        {
            saveFile();
            return true;
        }
        else
        {
            return false;
        }
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
