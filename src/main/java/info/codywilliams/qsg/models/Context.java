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

package info.codywilliams.qsg.models;

import info.codywilliams.qsg.generators.NameGenerator;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class Context {
    volatile static private Context instance;
    private final ObjectProperty<Team> currentTeam;
    final private ListProperty<Team> teams;
    final private StringProperty leftStatus;
    final private StringProperty rightStatus;


    final private NameGenerator femaleNames;
    final private NameGenerator maleNames;
    final private NameGenerator nonBinaryNames;
    final private NameGenerator surnames;
    final private NameGenerator teamNames;
    private final Locale locale;
    private final ResourceBundle textBundle;
    private final DateTimeFormatter dateTimeFormatter;
    private File currentSaveFile;


    private Context() {
        locale = Locale.getDefault();
        dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).withZone(ZoneId.systemDefault());
        textBundle = ResourceBundle.getBundle("info.codywilliams.qsg.language.Text", locale);

        currentTeam = new SimpleObjectProperty<>(this, "currentTeam");
        teams = new SimpleListProperty<>(this, "teams", FXCollections.observableList(new ArrayList<>(), team -> new Observable[]{team.nameProperty()}));
        leftStatus = new SimpleStringProperty(this, "leftStatus", textBundle.getString("app.newStatus"));
        rightStatus = new SimpleStringProperty(this, "rightStatus");

        femaleNames = new NameGenerator("femaleNames");
        maleNames = new NameGenerator("maleNames");
        nonBinaryNames = new NameGenerator("nonBinaryNames");
        surnames = new NameGenerator("surnames");
        teamNames = new NameGenerator("teamNames");
    }

    public static Context getInstance() {
        if (instance == null) {
            synchronized (Context.class) {
                if (instance == null) instance = new Context();
            }
        }
        return instance;
    }

    public void clearContext() {
        currentTeam.set(null);
        teams.clear();
    }

    public void loadContext(SaveSettings settings) {
        teams.addAll(settings.getTeams());
    }

    public Team getCurrentTeam() {
        return currentTeam.get();
    }

    public void setCurrentTeam(Team currentTeam) {
        this.currentTeam.set(currentTeam);
    }

    public ObjectProperty<Team> currentTeamProperty() {
        return currentTeam;
    }

    public ObservableList<Team> getTeams() {
        return teams.get();
    }

    public ListProperty<Team> teamsProperty() {
        return teams;
    }

    public String getLeftStatus() {
        return leftStatus.get();
    }

    public void setLeftStatus(String leftStatus) {
        this.leftStatus.set(leftStatus);
    }

    public StringProperty leftStatusProperty() {
        return leftStatus;
    }

    public String getRightStatus() {
        return rightStatus.get();
    }

    public void setRightStatus(String rightStatus) {
        this.rightStatus.set(rightStatus);
    }

    public StringProperty rightStatusProperty() {
        return rightStatus;
    }

    public NameGenerator getFemaleNames() {
        return femaleNames;
    }

    public NameGenerator getMaleNames() {
        return maleNames;
    }

    public NameGenerator getNonBinaryNames() {
        return nonBinaryNames;
    }

    public NameGenerator getSurnames() {
        return surnames;
    }

    public NameGenerator getTeamNames() {
        return teamNames;
    }

    public ResourceBundle getTextBundle() {
        return textBundle;
    }

    public Locale getLocale() {
        return locale;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public File getCurrentSaveFile() {
        return currentSaveFile;
    }

    public void setCurrentSaveFile(File currentSaveFile) {
        this.currentSaveFile = currentSaveFile;
    }
}
