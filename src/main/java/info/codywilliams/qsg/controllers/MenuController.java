package info.codywilliams.qsg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import info.codywilliams.qsg.App;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.SaveSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ResourceBundle;

public class MenuController {
    private Context context;
    private Logger logger;

    public void initialize() {
        context = Context.getInstance();
        logger = LoggerFactory.getLogger(AppController.class);

    }

    @FXML
    void menuFileNew(ActionEvent ignoredEvent) {
        context.clearContext();
    }

    @FXML
    void fileOpen(ActionEvent event) {
        System.out.println(event);
        File settingsFile = selectFile(event, FileAction.OPEN);

        if (settingsFile != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                SaveSettings settings = mapper.readValue(settingsFile, SaveSettings.class);
                context.clearContext();
                context.loadContext(settings);
                context.setCurrentSaveFile(settingsFile);
                updateLeftStatus(settingsFile);
            } catch (IOException e) {
                App.exceptionAlert(e);
                logger.error("Error loading file", e);
            }
        }
    }

    private File selectFile(ActionEvent event, FileAction fileAction) {
        Window window = ((MenuItem) event.getTarget()).getParentPopup().getOwnerWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("").getAbsoluteFile());

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(context.getTextBundle().getString("file.ext.json"), "*.json"));

        switch (fileAction) {
            case OPEN -> {
                return fileChooser.showOpenDialog(window);
            }
            case SAVE -> {
                return fileChooser.showSaveDialog(window);
            }
        }

        return null;
    }

    private void updateLeftStatus(File saveFile) {
        Instant timestamp = Instant.ofEpochMilli(saveFile.lastModified());
        context.setLeftStatus(String.format("%s %s %s", context.getTextBundle().getString("app.lastSaved"), saveFile.getName(), context.getDateTimeFormatter().format(timestamp)));
    }

    @FXML
    void fileSaveAction(ActionEvent event) {
        if (context.getCurrentSaveFile() == null) {
            context.setCurrentSaveFile(selectFile(event, FileAction.SAVE));
        }

        saveSettings(context.getCurrentSaveFile());
    }

    private void saveSettings(File saveFile) {
        if (saveFile != null) {
            try {
                SaveSettings settings = new SaveSettings(context);

                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(saveFile, settings);

                updateLeftStatus(saveFile);

                logger.debug("Saved to file {}", saveFile);
            } catch (IOException e) {
                App.exceptionAlert(e);
                logger.error("Error saving file", e);
            }
        }
    }

    @FXML
    void fileSaveAs(ActionEvent event) {
        context.setCurrentSaveFile(selectFile(event, FileAction.SAVE));

        saveSettings(context.getCurrentSaveFile());
    }

    @FXML
    void menuHelpAbout(ActionEvent ignoredEvent) {
        ResourceBundle textBundle = context.getTextBundle();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(textBundle.getString("alert.about.title"));
        alert.setHeaderText(textBundle.getString("alert.about.header"));
        alert.setContentText(textBundle.getString("alert.about.content"));

        alert.show();
    }

    private enum FileAction {
        OPEN, SAVE
    }

}
