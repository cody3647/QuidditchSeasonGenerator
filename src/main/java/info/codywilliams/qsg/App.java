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

import info.codywilliams.qsg.models.Context;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
import java.util.ResourceBundle;

/**
 * JavaFX App
 */
public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    public static void exceptionAlert(Exception e) {
        ResourceBundle textBundle = Context.getInstance().getTextBundle();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(textBundle.getString("alert.exception.title"));
        alert.setHeaderText(e.getClass().getSimpleName());
        alert.setContentText(e.getMessage());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        e.printStackTrace(printWriter);
        String stacktrace = stringWriter.toString();

        Label label = new Label(textBundle.getString("alert.exception.stacktrace"));
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

    @Override
    public void start(Stage window) throws IOException {
        Scene scene = new Scene(loadFXML("app"));
        window.setTitle("Quidditch Season Generator");
        window.setScene(scene);
        window.show();
        Logger logger = LoggerFactory.getLogger(App.class);

        window.addEventHandler(ActionEvent.ANY, actionEvent ->
                logger.trace("Type: {} \t\tSource: {}\t\tTarget: {}",
                        actionEvent.getEventType(), actionEvent.getSource(), actionEvent.getTarget()));
    }

    public static Parent loadFXML(String fxml) throws IOException {
        Context context = Context.getInstance();
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("views/" + fxml + ".fxml"), context.getTextBundle());
        return fxmlLoader.load();
    }

}