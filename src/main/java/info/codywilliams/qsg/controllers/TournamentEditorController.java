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
import info.codywilliams.qsg.models.tournament.MatchDayTime;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateStringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TournamentEditorController {

    @FXML
    VBox tournamentVBox;
    @FXML
    ComboBox<TournamentType> tournamentTypeComboBox;
    @FXML
    TextField leagueNameTextField;
    static LocalTime defaultMatchTime = LocalTime.of(20, 0);
    @FXML
    DatePicker startDatePicker;
    @FXML
    Label  matchesPerWeekLabel;
    @FXML
    TableView<MatchDayTime> matchDayTimeTableView;
    @FXML
    TableColumn<MatchDayTime, DayOfWeek> matchDayTimeDayCol;
    @FXML
    TableColumn<MatchDayTime, LocalTime> matchDayTimeStartTimeCol;
    @FXML
    TableColumn<MatchDayTime, Integer> matchDayTimePriorityCol;
    @FXML
    TableColumn<MatchDayTime, Integer> matchDayTimeCountCol;
    @FXML
    TableColumn<MatchDayTime, Button> matchDayTimeRemoveCol;
    @FXML
    ComboBox<DayOfWeek> matchDayTimeDayComboBox;
    @FXML
    TextField matchDayTimeTimeTextField;
    @FXML
    TextField matchDayTimePriorityTextField;
    TextFormatter<LocalTime> matchDayTimeTimeTextFieldFormatter;
    TextFormatter<Integer> matchDayTimePriorityTextFieldFormatter;
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
    @FXML
    Button matchDayTimeAddButton;

    public TournamentEditorController(Context context){
        this.context = context;
    }
    EventHandler<TableColumn.CellEditEvent<MatchDayTime, DayOfWeek>> matchDayTimeDayCommitHandler = (dayOfWeekEditEvent) -> {
        MatchDayTime matchDayTime = dayOfWeekEditEvent.getRowValue();
        matchDayTime.setDayOfWeek(dayOfWeekEditEvent.getNewValue());

        matchDayTimeDuplicateRemover(matchDayTime);
    };
    EventHandler<TableColumn.CellEditEvent<MatchDayTime, LocalTime>> matchDayTimeStartTimeCommitHandler = (startTimeEditEvent) -> {
        MatchDayTime matchDayTime = startTimeEditEvent.getRowValue();
        matchDayTime.setLocalTime(startTimeEditEvent.getNewValue());

        matchDayTimeDuplicateRemover(matchDayTime);
    };

    public void initialize() {
        tournamentOptions = context.getTournamentOptions();

        // Comboboxes and TextFields
        tournamentTypeComboBox.getItems().setAll(TournamentType.values());
        tournamentTypeComboBox.setConverter(new TournamentTypeStringConverter());

        context.currentTournamentProperty().addListener(((observableValue, oldTournament, newTournament) -> {
            if (context.getCurrentTournament() != null) {
                if (newTournament != null && (oldTournament == null || oldTournament.getType() != newTournament.getType())) {
                    tournamentTypeComboBox.getSelectionModel().select(newTournament.getType());
                }
            }
        }));

        leagueNameTextField.textProperty().bindBidirectional(tournamentOptions.leagueNameProperty());

        matchDayTimeDayComboBox.setItems(FXCollections.observableArrayList(DayOfWeek.values()));
        matchDayTimeTimeTextFieldFormatter = new TextFormatter<>(
                new MatchTimeStringConverter(),
                defaultMatchTime,
                MatchTimeStringConverter::filter);
        matchDayTimeTimeTextField.setTextFormatter(matchDayTimeTimeTextFieldFormatter);

        matchDayTimePriorityTextFieldFormatter = new TextFormatter<>(new IntegerStringConverter(), 0, TournamentEditorController::integerFilter);
        matchDayTimePriorityTextField.setTextFormatter(matchDayTimePriorityTextFieldFormatter);
        // DatePickers
        startDatePicker.valueProperty().bindBidirectional(tournamentOptions.startDateProperty());
        startDatePicker.setOnAction(actionEvent -> tournamentOptions.setStartDate(startDatePicker.getValue()));
        startDatePicker.setDayCellFactory(startDateDayCellFactory());


        // Match Date and Times Table Setup
        matchDayTimeDayCol.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        matchDayTimeDayCol.setCellFactory(ComboBoxTableCell.forTableColumn(new DayOfWeekConverter(), DayOfWeek.values()));
        matchDayTimeDayCol.setOnEditCommit(matchDayTimeDayCommitHandler);

        matchDayTimeStartTimeCol.setCellValueFactory(new PropertyValueFactory<>("localTime"));
        matchDayTimeStartTimeCol.setCellFactory(TextFieldTableCell.forTableColumn(new MatchTimeStringConverter()));
        matchDayTimeStartTimeCol.setOnEditCommit(matchDayTimeStartTimeCommitHandler);

        matchDayTimePriorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        matchDayTimePriorityCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        matchDayTimeCountCol.setCellValueFactory(new PropertyValueFactory<>("count"));
        matchDayTimeCountCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        matchDayTimeRemoveCol.setCellFactory(
                ButtonTableCell.forTableColumn("far-trash-alt", "Delete Match Date & Time",
                        (MatchDayTime param) -> tournamentOptions.getMatchDayTimeList().remove(param)
                )
        );

        matchDayTimeTableView.setItems(tournamentOptions.getSortedMatchDayTimeList());
        matchesPerWeekLabel.textProperty().bind(Bindings.format("%s %d", resources.getString("tournament.label.matchDay.matchesPerWeek"), tournamentOptions.matchesPerWeekProperty()));

        // Blackout Dates Table Setup
        blackoutStartCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        blackoutStartCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));

        blackoutEndCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        blackoutEndCol.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));

        blackoutRemoveCol.setCellFactory(
                ButtonTableCell.forTableColumn("far-trash-alt", "Delete BlackoutDates",
                        (BlackoutDates param) -> tournamentOptions.getBlackoutDates().remove(param)
                )
        );

        blackoutDatesTable.setItems(tournamentOptions.getBlackoutDates());

        setupActions();
    }

    private void matchDayTimeDuplicateRemover(MatchDayTime matchDayTime) {
        ObservableList<MatchDayTime> matchDayTimeList = tournamentOptions.getMatchDayTimeList();
        int i = matchDayTimeList.indexOf(matchDayTime);
        int j = matchDayTimeList.lastIndexOf(matchDayTime);

        if (i == j)
            return;

        MatchDayTime iMatchDayTime = matchDayTimeList.get(i);
        MatchDayTime jMatchDayTime = matchDayTimeList.remove(j);
        int iCount = iMatchDayTime.getCount();
        iMatchDayTime.setCount(jMatchDayTime.getCount() + iCount);
    }

    private Callback<DatePicker, DateCell> startDateDayCellFactory() {
        return (final DatePicker datePicker) -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.getDayOfWeek() != DayOfWeek.MONDAY) {
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

        matchDayTimeAddButton.setOnAction(actionEvent -> {
            DayOfWeek dayOfWeek = matchDayTimeDayComboBox.getValue();
            LocalTime localTime = matchDayTimeTimeTextFieldFormatter.getValue();
            int priority = matchDayTimePriorityTextFieldFormatter.getValue();
            if (dayOfWeek != null && localTime != null) {
                MatchDayTime matchDayTime = new MatchDayTime(dayOfWeek, localTime, priority);
                ObservableList<MatchDayTime> matchDayTimeList = tournamentOptions.getMatchDayTimeList();

                if (matchDayTimeList.contains(matchDayTime)) {
                    int index = matchDayTimeList.indexOf(matchDayTime);
                    matchDayTimeList.get(index).incrementCount();
                } else {
                    matchDayTimeList.add(matchDayTime);
                    FXCollections.sort(matchDayTimeList, Comparator.naturalOrder());
                }

                matchDayTimeDayComboBox.setValue(null);
                matchDayTimeTimeTextField.setText(null);
            }
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

    static class ButtonTableCell<T> extends TableCell<T, Button> {
        private final Button button;

        public ButtonTableCell(String iconCode, String buttonText, Consumer<T> function) {

            button = new Button();
            button.setGraphic(new FontIcon(iconCode));
            button.setAccessibleText(buttonText);

            button.setOnAction((ActionEvent event) -> function.accept(getTableView().getItems().get(getIndex())));

        }

        public static <TT> Callback<TableColumn<TT, Button>, TableCell<TT, Button>> forTableColumn(String iconCode, String buttonText, Consumer<TT> function) {
            return param -> new TournamentEditorController.ButtonTableCell<>(iconCode, buttonText, function);
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

    static private final Pattern intPattern = Pattern.compile("^\\d*$");

    private static TextFormatter.Change integerFilter(TextFormatter.Change change) {
        if(!change.isContentChange() || change.getText().isEmpty()) return change;

        Matcher matcher = intPattern.matcher(change.getText());
        if(matcher.matches()) return change;

        change.setText("");
        return change;
    }

    private static class MatchTimeStringConverter extends StringConverter<LocalTime> {
        final static Pattern pattern = Pattern.compile(
                "^(?<hour>[0-2]?\\d)[\\.: ]?(?<min>[0-5]\\d)?[\\.: ]? ?(?<ampm>[ap]m?)?",
                Pattern.CASE_INSENSITIVE
        );
        final static Pattern inProgressPattern = Pattern.compile(
                "^[0-2]?\\d?[: \\.]?[0-5]?\\d? ?[apm]{0,2}$",
                Pattern.CASE_INSENSITIVE
        );
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

        static TextFormatter.Change filter(TextFormatter.Change change) {
            if (change.isContentChange()) {
                Matcher matcher = MatchTimeStringConverter.inProgressPattern.matcher(change.getControlNewText());
                if (matcher.matches()) return change;
                change.setText("");
            }
            return change;
        }

        @Override
        public String toString(LocalTime localTime) {
            if (localTime == null)
                return null;
            return formatter.format(localTime);
        }

        @Override
        public LocalTime fromString(String s) {
            Matcher matcher = pattern.matcher(s);

            if (!matcher.matches()) return TournamentEditorController.defaultMatchTime;

            String ampm = matcher.group("ampm");
            String hourStr = matcher.group("hour");
            String minStr = matcher.group("min");

            int hour;
            int min;

            hour = Integer.parseInt(hourStr);
            min = minStr == null ? 0 : Integer.parseInt(minStr);

            if (min < 0 || min > 59)
                min = 0;

            if (ampm != null) {
                if (hour > 12)
                    hour -= 12;

                if (hour == 12)
                    hour = 0;

                if (ampm.equalsIgnoreCase("PM") || ampm.equalsIgnoreCase("P"))
                    hour += 12;
            }

            return LocalTime.of(hour, min, 0);
        }
    }

    private static class DayOfWeekConverter extends StringConverter<DayOfWeek> {
        static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("cccc");
        @Override
        public String toString(DayOfWeek dayOfWeek) {
            return dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
        }

        @Override
        public DayOfWeek fromString(String s) {
            TemporalAccessor accessor = formatter.parse(s);
            return DayOfWeek.from(accessor);
        }
    }

    private class TournamentTypeStringConverter extends StringConverter<TournamentType> {
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
    }
}

