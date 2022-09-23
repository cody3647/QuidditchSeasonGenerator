package info.codywilliams.qsg.controllers;


import info.codywilliams.qsg.App;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.Match;
import info.codywilliams.qsg.models.SaveSettings;
import info.codywilliams.qsg.models.tournament.TimeEntry;
import info.codywilliams.qsg.util.Formatters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ResourceBundle;

public class MenuController {
    private final Context context;
    private final Logger logger;
    @FXML
    ResourceBundle resources;

    public MenuController(Context context){
        this.context = context;
        logger = LoggerFactory.getLogger(AppController.class);
    }

    public void initialize() {
    }

    @FXML
    void menuFileNew(ActionEvent ignoredEvent) {
        context.clearContext();
    }

    @FXML
    void fileOpen(ActionEvent event) {
        File settingsFile = selectFile(event, FileAction.OPEN);

        if (settingsFile != null) {
            try {
                SaveSettings settings = SaveSettings.loadFromFile(settingsFile);

                context.clearContext();
                context.loadContext(settings);
                context.setCurrentSaveFile(settingsFile);
                updateLeftStatus(settingsFile);
            } catch (IOException e) {
                App.exceptionAlert(e, resources);
                logger.error("Error loading file", e);
            }
        }
    }

    private File selectFile(ActionEvent event, FileAction fileAction) {
        Window window = ((MenuItem) event.getTarget()).getParentPopup().getOwnerWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("").getAbsoluteFile());

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(resources.getString("file.ext.json"), "*.json"));

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
        context.setLeftStatus(String.format("%s %s %s", resources.getString("app.lastSaved"), saveFile.getName(), Formatters.dateTimeFormatter.format(timestamp)));
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

                settings.saveToFile(saveFile);

                updateLeftStatus(saveFile);

                logger.debug("Saved to file {}", saveFile);
            } catch (IOException e) {
                App.exceptionAlert(e, resources);
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("alert.about.title"));
        alert.setHeaderText(resources.getString("alert.about.header"));
        alert.setContentText(resources.getString("alert.about.content"));

        alert.show();
    }

    @FXML
    void displayMatchCalendar(ActionEvent ignoredEvent){
        Stage calendarWindow = new Stage();
        calendarWindow.initModality(Modality.NONE);
        VBox calendarVbox = new VBox();
        ScrollPane scrollPane = new ScrollPane(calendarVbox);
        Scene calendarScene = new Scene(scrollPane, 1000, 1000);

        if(context.getCurrentTournament() != null) {
            for (TimeEntry entry : context.getCurrentTournament().getTemplate()) {
                calendarVbox.getChildren().add(new Label(String.format("TimeEntry: %s %s\tCount: %d", entry.getDayOfWeek(), entry.getLocalTime().format(Formatters.timeFormatter), entry.getCount())));
            }
            calendarVbox.getChildren().add(new Label(""));
            calendarVbox.getChildren().add(new Label("Calendar"));
            for (Match match : context.getCurrentTournament().getMatches()) {
                calendarVbox.getChildren().add(new Label(String.format("Round: %2d\tMatch: %2d\t\tDate: %s", match.getRound(), match.getNumber(), match.getStartDateTime().format(Formatters.dateTimeFormatter))));
            }
        }
        else
            calendarVbox.getChildren().add(new Label("No matches to show yet, please configure the tournament"));
        calendarWindow.setScene(calendarScene);
        calendarWindow.show();



    }

    private enum FileAction {
        OPEN, SAVE
    }

}
