/*
 * Quidditch Season Generator
 * Copyright (C) 2022.  Cody Williams
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.qsg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import info.codywilliams.qsg.controllers.*;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.service.*;
import info.codywilliams.qsg.util.DependencyInjector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * JavaFX App
 */
public class App extends Application {
    final static public ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    public static void main(String[] args) {
        launch();
    }

    public static void exceptionAlert(Exception e, ResourceBundle resources) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(resources.getString("alert.exception.title"));
        alert.setHeaderText(e.getClass().getSimpleName());
        alert.setContentText(e.getMessage());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);
        String stacktrace = stringWriter.toString();

        Label label = new Label(resources.getString("alert.exception.stacktrace"));
        TextArea textArea = new TextArea(stacktrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxHeight(1000);
        textArea.setMaxWidth(1000);

        VBox.setVgrow(textArea, Priority.ALWAYS);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(label, textArea);

        alert.getDialogPane().setExpandableContent(vBox);

        alert.show();
    }

    public static void close() {
        Platform.exit();
    }

    @Override
    public void start(Stage window) throws IOException {
        setupLocale();
        setupDependencyInjector();

        Scene scene = new Scene(DependencyInjector.load("app"));
        DependencyInjector.addStylesheet(scene, "styles.css");

        DependencyInjector.setUpAndShowStage(window, scene, "app.title");

        Logger logger = LoggerFactory.getLogger(App.class);

        window.addEventHandler(ActionEvent.ANY, actionEvent ->
                logger.trace("Type: {} \t\tSource: {}\t\tTarget: {}",
                        actionEvent.getEventType(), actionEvent.getSource(), actionEvent.getTarget()));
    }

    private void setupDependencyInjector() {
        Locale locale = Locale.getDefault();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("info.codywilliams.qsg.language.App", locale);
        ResourceBundle outputResourceBundle = ResourceBundle.getBundle("info.codywilliams.qsg.language.Output", locale);

        Context context = new Context();
        SaveSettingsService saveSettingsService = new SaveSettingsService(context, resourceBundle);
        PageService pageService = new PageService(outputResourceBundle);
        OutputService outputService = new OutputService(outputResourceBundle, saveSettingsService);


        Random random = new Random();

        NameGenerator surnames = new NameGenerator("surnames");
        NameGenerator femaleNames = new NameGenerator("femaleNames");
        NameGenerator maleNames = new NameGenerator("maleNames");
        NameGenerator nonBinaryNames = new NameGenerator("nonBinaryNames");
        NameGenerator teamNames = new NameGenerator("teamNames");
        PlayerFactory playerFactory = new PlayerFactory(random, surnames, femaleNames, maleNames, nonBinaryNames);
        TeamFactory teamFactory = new TeamFactory(teamNames, playerFactory, resourceBundle);


        DependencyInjector.setBundle(resourceBundle);

        DependencyInjector.addInjectionMethod(AppController.class, type -> new AppController(context, teamFactory, pageService, outputService));
        DependencyInjector.addInjectionMethod(MenuController.class, type -> new MenuController(context, teamFactory, saveSettingsService));
        DependencyInjector.addInjectionMethod(TeamEditorController.class, type -> new TeamEditorController(context, playerFactory));
        DependencyInjector.addInjectionMethod(TournamentEditorController.class, type -> new TournamentEditorController(context));
        DependencyInjector.addInjectionMethod(TournamentInfoController.class, type -> new TournamentInfoController(context));
        DependencyInjector.addInjectionMethod(MediawikiSetupController.class, type -> new MediawikiSetupController(context));
    }

    private void setupLocale() {
        // Setup custom locale with first day of the week being Monday
        Locale.setDefault(Locale.Category.FORMAT, new Locale.Builder()
                .setLocale(Locale.getDefault())
                .setExtension(Locale.UNICODE_LOCALE_EXTENSION, "fw-" + DayOfWeek.MONDAY.toString().substring(0, 3))
                .build());
    }
}