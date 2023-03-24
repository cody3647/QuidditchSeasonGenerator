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

package info.codywilliams.qsg.controllers;

import info.codywilliams.qsg.App;
import info.codywilliams.qsg.generators.TeamGenerator;
import info.codywilliams.qsg.layout.TournamentCalendar;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.output.Page;
import info.codywilliams.qsg.util.DependencyInjector;
import info.codywilliams.qsg.util.Formatters;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.ResourceBundle;

public class AppController {

    @FXML
    VBox main;
    @FXML
    VBox leftPane;
    @FXML
    ScrollPane rightPane;
    @FXML
    ToggleGroup editorToggleGroup;
    @FXML
    ListView<Team> leftPaneListView;
    @FXML
    FlowPane buttonFlowPane;
    @FXML
    Label leftStatus;
    @FXML
    Label rightStatus;
    @FXML
    ResourceBundle resources;
    @FXML
    Separator buttonSeparator;
    @FXML
    Button viewTournamentCalendarButton;
    @FXML
    Button generateSeasonButton;
    MenuBar menuBar;
    AnchorPane teamEditorPane;
    AnchorPane tournamentEditorPane;
    VBox tournamentInfoBox;
    Node storedPane;
    private final Context context;
    private final ResourceBundle outputBundle;
    private int teamNumber = 0;

    public AppController(Context context, ResourceBundle outputBundle){
        this.context = context;
        this.outputBundle = outputBundle;
    }
    public void initialize() {
        try {
            menuBar = (MenuBar) DependencyInjector.load("menu");
            teamEditorPane = (AnchorPane) DependencyInjector.load("teamEditor");
            tournamentEditorPane = (AnchorPane) DependencyInjector.load("tournamentEditor");
            tournamentInfoBox = (VBox) DependencyInjector.load("tournamentInfo");
            storedPane = tournamentEditorPane;
        } catch (IOException e){
            App.exceptionAlert(e, resources);
        }



        main.getChildren().add(0, menuBar);

        int index = leftPane.getChildren().indexOf(buttonSeparator);

        leftPane.getChildren().add(index, tournamentInfoBox);
        rightPane.setContent(teamEditorPane);



        leftStatus.textProperty().bind(context.leftStatusProperty());
        context.leftStatusProperty().set(resources.getString("app.newStatus"));
        rightStatus.textProperty().bind(context.rightStatusProperty());

        leftPaneListView.setItems(context.getTeams());

        leftPaneListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                if (empty || team == null || team.getName() == null) setText(null);
                else setText(team.getName());
            }
        });

        leftPaneListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTeam, newTeam) -> context.setCurrentTeam(newTeam));

        editorToggleGroup.selectedToggleProperty().addListener(((observableValue, prevToggle, currentToggle) -> {
            Node temp = rightPane.getContent();
            rightPane.setContent(storedPane);
            storedPane = temp;
        }));

    }

    @FXML
    void displayTournamentCalendar(ActionEvent ignoredEvent){
        TournamentCalendar.displayTournamentCalendarWindow(context, resources);
    }

    @FXML
    void generateHTMLOutput(ActionEvent ignoredEvent) {
        // Get the list of pages
        List<Page> pages = context.getCurrentTournament().buildOutput(context.getTeams(), context.getSeed());
        // Set up an output directory with a subdirectory named after the league and year
        String tournamentTitle = context.getCurrentTournament().getTournamentTitle();
        Path outputPath = Paths.get("output", Formatters.sanitizeFileNames(tournamentTitle));

        try {
            Files.createDirectories(outputPath);
            for (Page page : pages) {
                Path pageFile = outputPath.resolve(Formatters.sanitizeFileNames(page.getFileName()) + ".html");
                Files.writeString(pageFile, page.toHtml(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void createNewTeam() {
        teamNumber++;
        context.getTeams().add(TeamGenerator.newTeam(teamNumber, resources));
    }

    public void createRandomTeam() {
        context.getTeams().add(TeamGenerator.randomTeam());
    }

    public void removeTeam() {
        context.getTeams().remove(leftPaneListView.getSelectionModel().getSelectedItem());
    }
}
