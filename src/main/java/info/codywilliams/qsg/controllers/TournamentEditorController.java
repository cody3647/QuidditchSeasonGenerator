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
import info.codywilliams.qsg.models.tournament.BlackoutDates;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.models.tournament.ValidStartTime;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;
import javafx.util.converter.LocalTimeStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class TournamentEditorController {

    @FXML
    VBox tournamentVBox;
    @FXML
    ComboBox<TournamentType> tournamentTypeComboBox;
    @FXML
    TextField hoursBetweenMatchesTextField;
    @FXML
    TextField roundsPerWeekTextField;
    @FXML
    DatePicker startDatePicker;
    @FXML
    TableView<ValidStartTime> validStartTimeTableView;
    @FXML
    TableColumn<ValidStartTime, String> dayCol;
    @FXML
    TableColumn<ValidStartTime, LocalTime> earliestCol;
    @FXML
    TableColumn<ValidStartTime, LocalTime> latestCol;
    @FXML
    TableColumn<ValidStartTime, Boolean> enableCol;
    @FXML
    TableView<BlackoutDates> blackoutDatesTable;
    @FXML
    TableColumn<BlackoutDates, LocalDate> blackoutStartCol;
    @FXML
    TableColumn<BlackoutDates, LocalDate> blackoutEndCol;
    @FXML
    TableColumn<BlackoutDates, Button> blackoutRemoveCol;
    @FXML
    DatePicker blackoutStartDatePicker;
    @FXML
    DatePicker blackoutEndDatePicker;
    @FXML
    Button blackoutAddButton;
    @FXML
    ResourceBundle resources;
    private final Context context;
    TournamentOptions tournamentOptions;

    public TournamentEditorController(Context context){
        this.context = context;
    }

    public void initialize() {
        tournamentOptions = context.getTournamentOptions();

        // Comboboxes and TextFields
        tournamentTypeComboBox.getItems().setAll(TournamentType.values());
        tournamentTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TournamentType tournamentType) {
                if (tournamentType == null)
                    return "";
                return resources.getString(tournamentType.key);
            }

            @Override
            public TournamentType fromString(String s) {
                return null;
            }
        });

        context.currentTournamentProperty().addListener(((observableValue, oldTournament, newTournament) -> {
            if(context.getCurrentTournament() != null){
                if (newTournament != null && (oldTournament == null || oldTournament.getType() != newTournament.getType())) {
                    tournamentTypeComboBox.getSelectionModel().select(newTournament.getType());
                }
            }
        }));

        roundsPerWeekTextField.textProperty().bindBidirectional(tournamentOptions.roundsPerWeekProperty(), new NumberStringConverter());
        hoursBetweenMatchesTextField.textProperty().bindBidirectional(tournamentOptions.hoursBetweenMatchesProperty(), new NumberStringConverter());

        // DatePickers
        Callback<DatePicker, DateCell> startDayCellFactory = blackoutDateDayCellFactory(DayOfWeek.MONDAY);
        Callback<DatePicker, DateCell> endDayCellFactory = blackoutDateDayCellFactory(DayOfWeek.SUNDAY);

        startDatePicker.valueProperty().bindBidirectional(tournamentOptions.startDateProperty());
        startDatePicker.setOnAction(actionEvent -> tournamentOptions.setStartDate(startDatePicker.getValue()));
        startDatePicker.setDayCellFactory(startDateDayCellFactory());

        blackoutStartDatePicker.setDayCellFactory(startDayCellFactory);
        blackoutEndDatePicker.setDayCellFactory(endDayCellFactory);


        // Valid Start Time Table Setup
        validStartTimeTableView.setItems(tournamentOptions.getValidStartTimes());

        dayCol.setCellValueFactory(
                param -> new ReadOnlyObjectWrapper<>(param.getValue().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()))
        );

        earliestCol.setCellValueFactory(new PropertyValueFactory<>("earliest"));
        earliestCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalTimeStringConverter()));

        latestCol.setCellValueFactory(new PropertyValueFactory<>("latest"));
        latestCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalTimeStringConverter()));

        enableCol.setCellValueFactory(new PropertyValueFactory<>("enableDay"));
        enableCol.setCellFactory(CheckBoxTableCell.forTableColumn(enableCol));

        // Blackout Dates Table Setup
        blackoutStartCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        blackoutStartCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));

        blackoutEndCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        blackoutEndCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));

        blackoutRemoveCol.setCellFactory(ButtonTableCell.forTableColumn("far-trash-alt", "Delete BlackoutDates",
                (BlackoutDates param) -> tournamentOptions.getBlackoutDates().remove(param)
        ));

        blackoutDatesTable.setItems(tournamentOptions.getBlackoutDates());

        setupActions();
    }

    private Callback<DatePicker, DateCell> blackoutDateDayCellFactory(DayOfWeek dayOfWeek) {
        return (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.getDayOfWeek() != dayOfWeek) {
                    setDisable(true);
                    setStyle("-fx-background-color: lightgrey;");
                }
            }
        };
    }

    private Callback<DatePicker, DateCell> startDateDayCellFactory() {
        return (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.getDayOfWeek() != tournamentOptions.validStartDayProperty().getValue()) {
                    setDisable(true);
                    setStyle("-fx-background-color: lightgrey;");
                }
            }
        };
    }

    private void setupActions() {
        tournamentTypeComboBox.setOnAction(actionEvent -> {
            TournamentType type = tournamentTypeComboBox.getValue();
            context.changeCurrentTournament(type);
        });

        blackoutStartDatePicker.setOnAction(actionEvent -> {
            LocalDate startDate = blackoutStartDatePicker.getValue();
            LocalDate endDate = blackoutEndDatePicker.getValue();
            if (endDate != null && startDate != null && startDate.isAfter(endDate)) {
                blackoutStartDatePicker.setValue(endDate);
            }
        });

        blackoutEndDatePicker.setOnAction(actionEvent -> {
            LocalDate startDate = blackoutStartDatePicker.getValue();
            LocalDate endDate = blackoutEndDatePicker.getValue();
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                blackoutEndDatePicker.setValue(startDate);
            }
        });

        blackoutAddButton.setOnAction(actionEvent -> {
            LocalDate startDate = blackoutStartDatePicker.getValue();
            LocalDate endDate = blackoutEndDatePicker.getValue();
            if (startDate != null && endDate != null) {
                BlackoutDates blackoutDates = new BlackoutDates();
                blackoutDates.setStart(startDate);
                blackoutDates.setEnd(endDate);
                if (!tournamentOptions.getBlackoutDates().contains(blackoutDates)) {
                    tournamentOptions.getBlackoutDates().add(blackoutDates);
                    FXCollections.sort(tournamentOptions.getBlackoutDates());
                }
                blackoutStartDatePicker.setValue(null);
                blackoutEndDatePicker.setValue(null);
            }

        });

    }

    static class ButtonTableCell extends TableCell<BlackoutDates, Button> {
        private final Button button;

        public ButtonTableCell(String iconCode, String buttonText, Consumer<BlackoutDates> function) {

            button = new Button();
            button.setGraphic(new FontIcon(iconCode));
            button.setAccessibleText(buttonText);

            button.setOnAction((ActionEvent event) -> function.accept(getTableView().getItems().get(getIndex())));

        }

        public static Callback<TableColumn<BlackoutDates, Button>, TableCell<BlackoutDates, Button>> forTableColumn(String iconCode, String buttonText, Consumer<BlackoutDates> function) {
            return param -> new TournamentEditorController.ButtonTableCell(iconCode, buttonText, function);
        }

        @Override
        protected void updateItem(Button unused, boolean empty) {
            super.updateItem(unused, empty);
            if (empty)
                setGraphic(null);
            else
                setGraphic(button);
        }
    }
}

