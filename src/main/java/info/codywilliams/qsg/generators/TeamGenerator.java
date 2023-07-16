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

package info.codywilliams.qsg.generators;

import info.codywilliams.qsg.models.Team;

import java.util.ResourceBundle;

public class TeamGenerator {
    private static final NameGenerator teamNames = new NameGenerator("teamNames");

    private TeamGenerator() {
    }

    static public Team randomTeam() {
        String name = teamNames.getNextName();
        String[] parts = name.split(" ");
        String shortName = parts[parts.length - 1];

        return randomTeam(name, shortName, parts[0]);
    }

    static public Team randomTeam(String name, String shortName, String home) {
        Team team = new Team();
        team.setName(name);
        team.setShortName(shortName);
        team.setHome(home);

        for (int i = 0; i < Team.TOTAL_BEATERS; i++)
            team.getBeaters().add(PlayerGenerator.randomBeater());

        for (int i = 0; i < Team.TOTAL_CHASERS; i++)
            team.getChasers().add(PlayerGenerator.randomChaser());

        for (int i = 0; i < Team.TOTAL_KEEPERS; i++)
            team.getKeepers().add(PlayerGenerator.randomKeeper());

        for (int i = 0; i < Team.TOTAL_SEEKERS; i++)
            team.getSeekers().add(PlayerGenerator.randomSeeker());

        return team;
    }

    static public Team newTeam(int num, ResourceBundle resources) {
        String teamName = resources.getString("gen.team.newName") + ' ' + num;

        return newTeam(teamName, teamName, teamName + ' ' + resources.getString("gen.team.newHome"), resources);
    }

    static public Team newTeam(String name, String shortName, String home, ResourceBundle resources) {
        Team team = new Team();
        team.setName(name);
        team.setShortName(shortName);
        team.setHome(home);

        String playerName;
        for (int i = 1; i <= Team.TOTAL_BEATERS; i++) {
            playerName = resources.getString("player.beater") + ' ' + i;
            team.getBeaters().add(PlayerGenerator.newBeater(playerName));
        }

        for (int i = 1; i <= Team.TOTAL_CHASERS; i++) {
            playerName = resources.getString("player.chaser") + ' ' + i;
            team.getChasers().add(PlayerGenerator.newChaser(playerName));
        }

        for (int i = 1; i <= Team.TOTAL_KEEPERS; i++) {
            playerName = resources.getString("player.keeper") + ' ' + i;
            team.getKeepers().add(PlayerGenerator.newKeeper(playerName));
        }

        for (int i = 1; i <= Team.TOTAL_SEEKERS; i++) {
            playerName = resources.getString("player.seeker") + ' ' + i;
            team.getSeekers().add(PlayerGenerator.newSeeker(playerName));
        }

        return team;
    }


}
