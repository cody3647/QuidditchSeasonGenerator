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
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.Team;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class AppController {
    @FXML
    MenuController menuController;
    @FXML
    TournamentInfoController tournamentInfoController;
    @FXML
    TournamentEditorController tournamentEditorController;
    @FXML
    TeamEditorController teamEditorController;
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
    Node storedPane;
    private Context context;
    private int teamNumber = 0;

    public void initialize() {
        context = Context.getInstance();

        leftStatus.textProperty().bind(context.leftStatusProperty());
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

        storedPane = App.loadFXML("tournamentEditor");
        editorToggleGroup.selectedToggleProperty().addListener(((observableValue, prevToggle, currentToggle) -> {
            Node temp = rightPane.getContent();
            rightPane.setContent(storedPane);
            storedPane = temp;
        }));

    }

    public void createNewTeam() {
        teamNumber++;
        context.getTeams().add(TeamGenerator.newTeam(teamNumber));
    }

    public void createRandomTeam() {
        context.getTeams().add(TeamGenerator.randomTeam());
    }

    public void removeTeam() {
        context.getTeams().remove(leftPaneListView.getSelectionModel().getSelectedItem());
    }
}
