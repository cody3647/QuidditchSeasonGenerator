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
import info.codywilliams.qsg.layout.TournamentCalendar;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.output.Page;
import info.codywilliams.qsg.service.Mediawiki;
import info.codywilliams.qsg.service.OutputService;
import info.codywilliams.qsg.service.PageService;
import info.codywilliams.qsg.service.TeamFactory;
import info.codywilliams.qsg.util.DependencyInjector;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AppController {

    private final Context context;
    private final TeamFactory teamFactory;
    private final PageService pageService;
    private final OutputService outputService;
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
    Label settingsStatus;
    @FXML
    Label outputStatus;
    @FXML
    ResourceBundle resources;
    @FXML
    Separator buttonSeparator;
    @FXML
    Button viewTournamentCalendarButton;
    @FXML
    CheckBox matchPagesCheckBox;
    @FXML
    CheckBox playerDetailsCheckBox;
    @FXML
    Button generateSeasonHtmlButton;
    @FXML
    Button generateSeasonWikitextButton;
    MenuBar menuBar;
    AnchorPane teamEditorPane;
    AnchorPane tournamentEditorPane;
    VBox tournamentInfoBox;
    Node storedPane;
    private int teamNumber = 0;
    Logger logger = LoggerFactory.getLogger(AppController.class);

    public AppController(Context context, TeamFactory teamFactory, PageService pageService, OutputService outputService) {
        this.context = context;
        this.teamFactory = teamFactory;
        this.pageService = pageService;
        this.outputService = outputService;
    }

    public void initialize() {
        try {
            menuBar = (MenuBar) DependencyInjector.load("menu");
            teamEditorPane = (AnchorPane) DependencyInjector.load("teamEditor");
            tournamentEditorPane = (AnchorPane) DependencyInjector.load("tournamentEditor");
            tournamentInfoBox = (VBox) DependencyInjector.load("tournamentInfo");
            storedPane = tournamentEditorPane;
        } catch (IOException e) {
            App.exceptionAlert(e, resources);
        }


        main.getChildren().add(0, menuBar);

        int index = leftPane.getChildren().indexOf(buttonSeparator);

        leftPane.getChildren().add(index, tournamentInfoBox);
        rightPane.setContent(teamEditorPane);


        settingsStatus.textProperty().bind(context.settingsStatusProperty());
        context.settingsStatusProperty().set(resources.getString("app.newStatus"));
        outputStatus.textProperty().bind(context.outputStatusProperty());

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

        viewTournamentCalendarButton.disableProperty().bind(context.matchesReadyProperty().not());
        generateSeasonHtmlButton.disableProperty().bind(context.matchesReadyProperty().not());
        generateSeasonWikitextButton.disableProperty().bind(Bindings.or(context.loggedInToMediawikiProperty().not(), context.matchesReadyProperty().not()));
    }

    @FXML
    void displayTournamentCalendar(ActionEvent ignoredEvent) {
        TournamentCalendar.displayTournamentCalendarWindow(context, resources);
    }

    @FXML
    void generateHTMLOutput(ActionEvent ignoredEvent) {
        List<Page> pages = pageService.buildPages(
                context.getCurrentTournament(), context.getTeams(), matchPagesCheckBox.isSelected(), playerDetailsCheckBox.isSelected(), context.getSeed()
        );
        String tournamentTitle = pageService.getTournamentTitle();
        outputService.writePagesToHtml(tournamentTitle, pages);
    }

    @FXML
    void generateWikitextOutput(ActionEvent ignoreEvent) {
        List<Page> pages = pageService.buildPages(
                context.getCurrentTournament(), context.getTeams(), matchPagesCheckBox.isSelected(), playerDetailsCheckBox.isSelected(), context.getSeed()
        );
        String tournamentTitle = pageService.getTournamentTitle();
        Mediawiki mediawiki = context.getMediawiki();

        try {
            if(mediawiki.pageExists(tournamentTitle)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(resources.getString("alert.mediawiki.title"));
                alert.setHeaderText(resources.getString("alert.mediawiki.header"));
                alert.setContentText(resources.getString("alert.mediawiki.content"));
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK)
                    return;
            }

            outputService.writePagesToMediawiki(tournamentTitle, pages, mediawiki);
        } catch (IOException e) {
            logger.error("Error communicating with mediawiki instance", e);
            throw new RuntimeException(e);
        }


    }

    public void createNewTeam() {
        teamNumber++;
        context.getTeams().add(teamFactory.newTeam(teamNumber, resources));
    }

    public void createRandomTeam() {
        context.getTeams().add(teamFactory.randomTeam());
    }

    public void removeTeam() {
        context.getTeams().remove(leftPaneListView.getSelectionModel().getSelectedItem());
    }
}
