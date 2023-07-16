/*
 * Quidditch Season Generator
 * Copyright (C) 2023.  Cody Williams
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

package info.codywilliams.qsg.controllers;

import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.service.Mediawiki;
import info.codywilliams.qsg.util.DependencyInjector;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ResourceBundle;

public class MediawikiSetupController {
    private final Context context;
    private final Logger logger;
    @FXML
    TextField apiUrlTextField;
    @FXML
    TextField usernameTextField;
    @FXML
    PasswordField passwordPasswordField;
    @FXML
    Button loginButton;
    @FXML
    Text loginStatusText;
    @FXML
    ResourceBundle resources;

    public MediawikiSetupController(Context context) {
        this.context = context;
        logger = LoggerFactory.getLogger(MediawikiSetupController.class);
    }

    public static void displayMediawikiSetupWindow() throws IOException {
        Stage mediawikiSetupWindow = new Stage();
        mediawikiSetupWindow.initModality(Modality.NONE);
        ScrollPane scrollPane = new ScrollPane();
        Scene setupScene = new Scene(scrollPane, 640, 480);

        DependencyInjector.setUpAndShowStage(mediawikiSetupWindow, setupScene, "app.mediawiki.title");

        scrollPane.setContent(DependencyInjector.load("mediawikiSetup"));
    }

    public void initialize() {
        Mediawiki mediawiki = this.context.getMediawiki();
        if (mediawiki.getApiUrlString() != null)
            apiUrlTextField.setText(mediawiki.getApiUrlString());
        if (mediawiki.getUsername() != null)
            usernameTextField.setText(mediawiki.getUsername());
        if (mediawiki.isLoggedIn())
            loginStatusText.setText("Logged in to: " + mediawiki.getApiUrlString());

    }

    @FXML
    void mediawikiLogin(ActionEvent ignoredEvent) {
        final LoginTask task = new LoginTask(
                this.context.getMediawiki(),
                apiUrlTextField.getText(),
                usernameTextField.getText(),
                passwordPasswordField.getText()
        );

        Thread thread = new Thread(task);
        thread.start();
    }

    public class LoginTask extends Task<Mediawiki.Response> {
        private final Mediawiki mediawiki;
        private final String apiUrl;
        private final String username;
        private final String password;

        public LoginTask(Mediawiki mediawiki, String apiUrl, String username, String password) {
            this.mediawiki = mediawiki;
            this.apiUrl = apiUrl;
            this.username = username;
            this.password = password;
        }

        @Override
        protected Mediawiki.Response call() {
            try {
                return mediawiki.login(apiUrl, username, password);
            } catch (Exception e) {
                logger.error("Exception while trying to log in to mediawiki instance.", e);
                return new Mediawiki.Response(false, "exception", e.getMessage());
            }
        }

        @Override
        protected void running() {
            super.running();
            loginButton.setDisable(true);
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            loginButton.setDisable(false);
            loginStatusText.setText(getValue().message);
            context.setLoggedInToMediawiki(getValue().isSuccess());
        }


    }
}
