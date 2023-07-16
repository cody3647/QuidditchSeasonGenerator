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

import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.util.Formatters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ResourceBundle;

public class TournamentInfoController {
    @FXML
    Label typeLabel;
    @FXML
    Label numTeamsLabel;
    @FXML
    Label numLocationsLabel;
    @FXML
    Label startDateLabel;
    @FXML
    Label endDateLabel;
    @FXML
    Label numRoundsLabel;
    @FXML
    Label numMatchesLabel;
    @FXML
    ResourceBundle resources;
    Context context;

    public TournamentInfoController(Context context) {
        this.context = context;
    }

    public void initialize() {
        TournamentOptions tournamentOptions = context.getTournamentOptions();

        numTeamsLabel.textProperty().bind(Bindings.format("%s %d", resources.getString("info.tournament.numTeams"), context.numTeamsProperty()));
        numLocationsLabel.textProperty().bind(Bindings.format("%s %d", resources.getString("info.tournament.numLocations"), context.numLocationsProperty()));

        StringBinding startDate = Bindings.createStringBinding(() -> tournamentOptions.startDateProperty().getValue().format(Formatters.dateFormatter), tournamentOptions.startDateProperty());
        startDateLabel.textProperty().bind(Bindings.format("%s %s", resources.getString("info.tournament.startDate"), startDate));

        context.currentTournamentProperty().addListener(((observableValue, oldTournament, newTournament) -> {
            if (context.getCurrentTournament() != null) {
                if (oldTournament != null) {
                    endDateLabel.textProperty().unbind();
                    numRoundsLabel.textProperty().unbind();
                    numMatchesLabel.textProperty().unbind();
                }

                if (newTournament != null) {
                    Tournament tournament = context.getCurrentTournament();
                    typeLabel.textProperty().set(String.format("%s %s", resources.getString("info.tournament.type"), resources.getString(tournament.getType().key)));
                    numRoundsLabel.textProperty().bind(Bindings.format("%s %d", resources.getString("info.tournament.numRounds"), tournament.numRoundsProperty()));
                    numMatchesLabel.textProperty().bind(Bindings.format("%s %d", resources.getString("info.tournament.numMatches"), tournament.numMatchesProperty()));
                    endDateLabel.textProperty().bind(Bindings.format("%s %s", resources.getString("info.tournament.endDate"), tournament.endDateStringBinding()));
                }
            }

        }));


    }

}

