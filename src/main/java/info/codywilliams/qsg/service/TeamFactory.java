/*
 * Quidditch Season Generator
 * Copyright (C) 2023.  Cody Williams
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

package info.codywilliams.qsg.service;

import info.codywilliams.qsg.models.Team;

import java.util.ResourceBundle;

public class TeamFactory {
    private final NameGenerator teamNames;
    private final PlayerFactory playerFactory;
    private final ResourceBundle resourceBundle;

    public TeamFactory(NameGenerator teamNames, PlayerFactory playerFactory, ResourceBundle resourceBundle) {
        this.teamNames = teamNames;
        this.playerFactory = playerFactory;
        this.resourceBundle = resourceBundle;
    }

    public Team randomTeam() {
        String name = teamNames.getNextName();
        String[] parts = name.split(" ");
        String shortName = parts[parts.length - 1];

        return randomTeam(name, shortName, parts[0]);
    }

    public Team randomTeam(String name, String shortName, String home) {
        Team team = new Team();
        team.setName(name);
        team.setShortName(shortName);
        team.setHome(home);

        for (int i = 0; i < Team.TOTAL_BEATERS; i++)
            team.getBeaters().add(playerFactory.randomBeater());

        for (int i = 0; i < Team.TOTAL_CHASERS; i++)
            team.getChasers().add(playerFactory.randomChaser());

        for (int i = 0; i < Team.TOTAL_KEEPERS; i++)
            team.getKeepers().add(playerFactory.randomKeeper());

        for (int i = 0; i < Team.TOTAL_SEEKERS; i++)
            team.getSeekers().add(playerFactory.randomSeeker());

        return team;
    }

    public Team newTeam(int num, ResourceBundle resources) {
        String teamName = resources.getString("gen.team.newName") + ' ' + num;

        return newTeam(teamName, teamName, teamName + ' ' + resourceBundle.getString("gen.team.newHome"));
    }

    public Team newTeam(String name, String shortName, String home) {
        Team team = new Team();
        team.setName(name);
        team.setShortName(shortName);
        team.setHome(home);

        String playerName;
        for (int i = 1; i <= Team.TOTAL_BEATERS; i++) {
            playerName = resourceBundle.getString("player.beater") + ' ' + i;
            team.getBeaters().add(playerFactory.newBeater(playerName));
        }

        for (int i = 1; i <= Team.TOTAL_CHASERS; i++) {
            playerName = resourceBundle.getString("player.chaser") + ' ' + i;
            team.getChasers().add(playerFactory.newChaser(playerName));
        }

        for (int i = 1; i <= Team.TOTAL_KEEPERS; i++) {
            playerName = resourceBundle.getString("player.keeper") + ' ' + i;
            team.getKeepers().add(playerFactory.newKeeper(playerName));
        }

        for (int i = 1; i <= Team.TOTAL_SEEKERS; i++) {
            playerName = resourceBundle.getString("player.seeker") + ' ' + i;
            team.getSeekers().add(playerFactory.newSeeker(playerName));
        }

        return team;
    }


}
