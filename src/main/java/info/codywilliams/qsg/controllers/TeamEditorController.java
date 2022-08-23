/*
 * Quidditch Season NameGenerator
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

import info.codywilliams.qsg.generators.PlayerGenerator;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.player.Player;
import info.codywilliams.qsg.models.player.PlayerType;
import javafx.beans.NamedArg;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class TeamEditorController {
    @FXML
    VBox teamVBox;
    @FXML
    TextField teamNameTextField;
    @FXML
    TextField teamHomeTextField;
    @FXML
    TreeTableView<Player> teamTable;
    @FXML
    TreeTableColumn<Player, String> nameCol;
    @FXML
    TreeTableColumn<Player, Integer> offenseCol;
    @FXML
    TreeTableColumn<Player, Integer> defenseCol;
    @FXML
    TreeTableColumn<Player, Integer> teamworkCol;
    @FXML
    TreeTableColumn<Player, Integer> foulingCol;
    @FXML
    TreeTableColumn<Player, Button> randomNameCol;
    @FXML
    TreeTableColumn<Player, Button> randomSkillsCol;
    Context context;

    private Map<String, TreeItem<Player>> playerPositions;


    public void initialize() {
        context = Context.getInstance();
        TreeItem<Player> treeRoot = new TreeItem<>(new PlayerType("Players"));
        playerPositions = new TreeMap<>();

        setupPlayerPosition("beaters");
        setupPlayerPosition("chasers");
        setupPlayerPosition("keepers");
        setupPlayerPosition("seekers");

        for (TreeItem<Player> item : playerPositions.values())
            treeRoot.getChildren().add(item);
        teamTable.setRowFactory(table -> new TreeTableRow<>(){
            @Override
            public void updateItem(Player player, boolean empty){
                super.updateItem(player, empty);
                if(!isEmpty() && player instanceof PlayerType)
                    setEditable(false);
                else
                    setEditable(true);
            }
        });

        EventHandler<TreeTableColumn.CellEditEvent<Player, Integer>> skillCommitHandler = integerCellEditEvent -> {
            final TreeTableView<Player> treeTableView = integerCellEditEvent.getTreeTableView();
            final TreeTableColumn<Player, Integer> treeTableColumn = integerCellEditEvent.getTableColumn();
            final TreeItem<Player> item = integerCellEditEvent.getRowValue();

            if (treeTableView == null || treeTableColumn == null || item == null)
                return;

            ObservableValue<Integer> cellObservableValue = treeTableColumn.getCellObservableValue(item);
            if (cellObservableValue == null) return;

            if (!(cellObservableValue instanceof WritableValue))
                return;

            Integer newInt = integerCellEditEvent.getNewValue();
            if (newInt == null)
                newInt = integerCellEditEvent.getOldValue();

            newInt = Player.validateSkill(newInt);

            ((WritableValue<Integer>) cellObservableValue).setValue(newInt);
            treeTableView.refresh();
        };

        teamTable.setRoot(treeRoot);

        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

        offenseCol.setCellValueFactory(new PlayerTypePropertyValueFactory<>("skillOffense"));
        offenseCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerStringConverter()));
        offenseCol.setOnEditCommit(skillCommitHandler);

        defenseCol.setCellValueFactory(new PlayerTypePropertyValueFactory<>("skillDefense"));
        defenseCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerStringConverter()));
        defenseCol.setOnEditCommit(skillCommitHandler);

        teamworkCol.setCellValueFactory(new PlayerTypePropertyValueFactory<>("skillTeamwork"));
        teamworkCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerStringConverter()));
        teamworkCol.setOnEditCommit(skillCommitHandler);

        foulingCol.setCellValueFactory(new PlayerTypePropertyValueFactory<>("foulLikelihood"));
        foulingCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerStringConverter()));
        foulingCol.setOnEditCommit(skillCommitHandler);

        Function<Player, Player> randomNameFunction = (Player player) -> {
            PlayerGenerator.randomFullName(player);
            return player;
        };

        Function<Player, Player> randomSkillsFunction = (Player player) -> {
            PlayerGenerator.randomSkills(player);
            return player;
        };

        randomNameCol.setCellFactory(ButtonTreeTableCell.forTreeTableColumn("fas-dice", "Randomize Player's Name", randomNameFunction));

        randomSkillsCol.setCellFactory(ButtonTreeTableCell.forTreeTableColumn("fas-dice", "Randomize Player's Name", randomSkillsFunction));

        context.currentTeamProperty().addListener(((observableValue, oldTeam, newTeam) -> changeTeam(oldTeam, newTeam)));
    }

    private void setupPlayerPosition(String position) {
        String playerHeader = context.getTextBundle().getString("team.playerHeader." + position);
        TreeItem<Player> playerTreeItem = new TreeItem<>(new PlayerType(playerHeader));
        playerTreeItem.setExpanded(true);
        playerPositions.put(position, playerTreeItem);
    }

    public void changeTeam(Team oldTeam, Team newTeam) {
        if (oldTeam != null) {
            teamNameTextField.textProperty().unbindBidirectional(oldTeam.nameProperty());
            teamHomeTextField.textProperty().unbindBidirectional(oldTeam.homeProperty());
        }

        if (newTeam != null) {
            teamNameTextField.textProperty().bindBidirectional(newTeam.nameProperty());
            teamHomeTextField.textProperty().bindBidirectional(newTeam.homeProperty());
        }


        // Handle players
        for (Map.Entry<String, TreeItem<Player>> positionEntry : playerPositions.entrySet()) {
            positionEntry.getValue().getChildren().clear();

            if (newTeam != null) {
                ObservableList<? extends Player> players = switch (positionEntry.getKey()) {
                    case "beaters" -> newTeam.getBeaters();
                    case "chasers" -> newTeam.getChasers();
                    case "keepers" -> newTeam.getKeepers();
                    case "seekers" -> newTeam.getSeekers();
                    default -> null;
                };

                if (players != null) for (Player player : players) {
                    positionEntry.getValue().getChildren().add(new TreeItem<>(player));
                }
            }
        }
    }

    static class PlayerTypePropertyValueFactory<S, T> extends TreeItemPropertyValueFactory<S, T> {

        public PlayerTypePropertyValueFactory(@NamedArg("property") String property) {
            super(property);
        }

        @Override
        public ObservableValue<T> call(TreeTableColumn.CellDataFeatures<S, T> param) {
            if (!param.getValue().getValue().getClass().getSimpleName().equals("PlayerType")) return super.call(param);
            return null;
        }
    }

    static class ButtonTreeTableCell extends TreeTableCell<Player, Button> {
        private final Button button;

        public ButtonTreeTableCell(String iconCode, String buttonText, Function<Player, Player> function) {

            button = new Button();
            button.setGraphic(new FontIcon(iconCode));
            button.setAccessibleText(buttonText);

            button.setOnAction((ActionEvent event) -> function.apply(getTreeTableView().getTreeItem(getIndex()).getValue()));

        }

        public static Callback<TreeTableColumn<Player, Button>, TreeTableCell<Player, Button>> forTreeTableColumn(String iconCode, String buttonText, Function<Player, Player> function) {
            return param -> new ButtonTreeTableCell(iconCode, buttonText, function);
        }

        @Override
        protected void updateItem(Button unused, boolean empty) {
            super.updateItem(unused, empty);
            TreeItem<Player> treeItem = getTreeTableView().getTreeItem(getIndex());
            if (treeItem != null) if (treeItem.getValue() instanceof PlayerType) empty = true;
            if (empty) setGraphic(null);
            else setGraphic(button);
        }
    }

}
