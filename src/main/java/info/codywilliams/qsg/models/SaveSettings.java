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

package info.codywilliams.qsg.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SaveSettings {
    private List<Team> teams;

    public SaveSettings() {
    }

    public SaveSettings(List<Team> teams) {
        this.teams = teams;
    }

    public SaveSettings(Context context) {
        teams = new ArrayList<>();
        teams.addAll(context.getTeams());
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    static public SaveSettings loadFromFile(File settingsFile) throws IOException {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        return mapper.readValue(settingsFile, SaveSettings.class);
    }

    public void saveToFile(File saveFile) throws IOException {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(saveFile, this);
    }
}
