package info.codywilliams.qsg.controllers;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import info.codywilliams.qsg.App;
import info.codywilliams.qsg.generators.MatchGenerator;
import info.codywilliams.qsg.generators.TeamGenerator;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.SaveSettings;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.tournament.MatchDayTime;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.service.Mediawiki;
import info.codywilliams.qsg.util.Formatters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HexFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuController {
    private final Context context;
    private final Logger logger;
    @FXML
    TextField seedTextField;
    @FXML
    ResourceBundle resources;

    static final List<String> britishTeams = List.of("appleby", "ballycastle", "caerphilly", "chudley",
            "falmouth", "holyhead", "kenmare", "montrose",
            "portree", "puddlemere", "tutshill", "wigtown", "wimbourne"
    );

    public MenuController(Context context) {
        this.context = context;
        logger = LoggerFactory.getLogger(MenuController.class);
    }

    public void initialize() {
        seedTextField.textProperty().bindBidirectional(context.seedProperty(), new HexConverter());
        seedTextField.setTextFormatter(new TextFormatter<>(new HexConverter(), context.getSeed(), HexConverter::filter));
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
    void displayMediawikiSetupWindow(ActionEvent ignoredEvent) {
        try {
            MediawikiSetupController.displayMediawikiSetupWindow();
        } catch (IOException e) {
            logger.error("Unable to load Mediawiki Setup Window", e);
            throw new RuntimeException(e);
        }
    }

    @FXML
    void loadBlankBritishQuidditchLeague(ActionEvent ignoredEvent) {
        standardBritishQuidditchLeague(britishTeams.stream()
                .map(name -> TeamGenerator.newTeam(
                                resources.getString("tournament.BIQL." + name + ".name"),
                                resources.getString("tournament.BIQL." + name + ".name.short"),
                                resources.getString("tournament.BIQL." + name + ".home"),
                                resources
                        )
                )
                .toList()
        );
    }

    @FXML
    void loadRandomBritishQuidditchLeague(ActionEvent ignoredEvent) {
        standardBritishQuidditchLeague(britishTeams.stream()
                .map(name -> TeamGenerator.randomTeam(
                                resources.getString("tournament.BIQL." + name + ".name"),
                                resources.getString("tournament.BIQL." + name + ".name.short"),
                                resources.getString("tournament.BIQL." + name + ".home")
                        )
                )
                .toList()
        );
    }

    void standardBritishQuidditchLeague(List<Team> teams) {
        TournamentOptions options = context.getTournamentOptions();
        options.setLeagueName(resources.getString("tournament.BIQL.name"));
        options.matchDayTimeListProperty().get().clear();
        List<MatchDayTime> matchDayTimeList = List.of(
                new MatchDayTime(DayOfWeek.FRIDAY, LocalTime.of(19, 30, 0), 1),
                new MatchDayTime(DayOfWeek.SATURDAY, LocalTime.of(10, 0, 0), 2),
                new MatchDayTime(DayOfWeek.SATURDAY, LocalTime.of(2, 0, 0), 3),
                new MatchDayTime(DayOfWeek.SATURDAY, LocalTime.of(19, 0, 0), 4),
                new MatchDayTime(DayOfWeek.SUNDAY, LocalTime.of(13, 0, 0), 5),
                new MatchDayTime(DayOfWeek.SUNDAY, LocalTime.of(16, 30, 0), 6)
        );
        options.matchDayTimeListProperty().addAll(matchDayTimeList);
        context.changeCurrentTournament(TournamentType.STRAIGHT_ROUND_ROBIN_HOME_AWAY);

        context.getTeams().clear();
        context.getTeams().addAll(teams);

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
    void changeMediawikiLogLevelInfo(ActionEvent ignoredEvent) {
        logger.info("Changed mediawiki log level to {}", Level.INFO);
        changeLogLevel(Mediawiki.class, Level.INFO);
    }

    @FXML
    void changeMediawikiLogLevelDebug(ActionEvent ignoredEvent) {
        logger.info("Changed mediawiki log level to {}", Level.DEBUG);
        changeLogLevel(Mediawiki.class, Level.DEBUG);
    }

    @FXML
    void changeMediawikiLogLevelTrace(ActionEvent ignoredEvent) {
        logger.info("Changed mediawiki log level to {}", Level.TRACE);
        changeLogLevel(Mediawiki.class, Level.TRACE);
    }

    @FXML
    void changeMatchGeneratorLogLevelInfo(ActionEvent ignoredEvent) {
        logger.info("Changed match generator log level to {}", Level.INFO);
        changeLogLevel(MatchGenerator.class, Level.INFO);
    }

    @FXML
    void changeMatchGeneratorLogLevelDebug(ActionEvent ignoredEvent) {
        logger.info("Changed match generator log level to {}", Level.DEBUG);
        changeLogLevel(MatchGenerator.class, Level.DEBUG);
    }

    @FXML
    void changeMatchGeneratorLogLevelTrace(ActionEvent ignoredEvent) {
        logger.info("Changed match generator log level to {}", Level.TRACE);
        changeLogLevel(MatchGenerator.class, Level.TRACE);
    }

    void changeLogLevel(Class<?> loggerName, Level level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger loggerToChange = loggerContext.getLogger(loggerName);
        loggerToChange.setLevel(level);
    }

    private enum FileAction {
        OPEN, SAVE
    }

    public static class HexConverter extends NumberStringConverter {
        final static Pattern pattern = Pattern.compile(
                "^[0-9A-F ]+?",
                Pattern.CASE_INSENSITIVE
        );

        static TextFormatter.Change filter(TextFormatter.Change change) {
            if (change.isContentChange()) {
                Matcher matcher = pattern.matcher(change.getControlNewText());
                if (change.getControlNewText().length() > 16) {
                    change.setText("");
                    return change;
                }
                if (matcher.matches()) {
                    change.setText(change.getText().toUpperCase());
                    return change;
                }
                change.setText("");
            }
            return change;
        }

        @Override
        public Number fromString(String s) {
            if (s == null)
                return null;
            else {
                s = s.trim();
                if (s.isEmpty())
                    return null;
                else
                    return HexFormat.fromHexDigitsToLong(s);
            }
        }

        @Override
        public String toString(Number number) {


            if (number == null)
                return "";
            else
                return HexFormat.of().withUpperCase().toHexDigits((long) number);
        }

        @Override
        protected NumberFormat getNumberFormat() {
            return super.getNumberFormat();
        }
    }

}
