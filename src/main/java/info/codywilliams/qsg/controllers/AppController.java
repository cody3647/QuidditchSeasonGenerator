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
import info.codywilliams.qsg.util.DependencyInjector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
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
    private int teamNumber = 0;

    public AppController(Context context){
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
    void generateSeason(ActionEvent ignoredEvent) {

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
