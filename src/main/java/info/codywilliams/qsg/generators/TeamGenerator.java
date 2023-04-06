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
    private TeamGenerator(){}
    static public Team randomTeam(){
        Team team = new Team();
        String name = teamNames.getNextName();
        String[] parts = name.split(" ");
        String shortName = parts[parts.length - 1];

        team.setName(name);
        team.setHome(parts[0]);
        team.setShortName(shortName);


        for(int i = 0; i < Team.TOTAL_BEATERS; i++)
            team.getBeaters().add(PlayerGenerator.randomBeater());

        for(int i = 0; i < Team.TOTAL_CHASERS; i++)
            team.getChasers().add(PlayerGenerator.randomChaser());

        for(int i = 0; i < Team.TOTAL_KEEPERS; i++)
            team.getKeepers().add(PlayerGenerator.randomKeeper());

        for(int i = 0; i < Team.TOTAL_SEEKERS; i++)
            team.getSeekers().add(PlayerGenerator.randomSeeker());

        return team;
    }

    static public Team newTeam(int num, ResourceBundle resources){
        Team team = new Team();
        String teamName = resources.getString("gen.team.newName") + ' ' + num;

        team.setName(teamName);
        team.setShortName(teamName);
        team.setHome(teamName + ' ' + resources.getString("gen.team.newHome"));

        String name;
        for(int i = 1; i <= Team.TOTAL_BEATERS; i++) {
            name = resources.getString("player.beater") + ' ' + i;
            team.getBeaters().add(PlayerGenerator.newBeater(name));
        }

        for(int i = 1; i <= Team.TOTAL_CHASERS; i++) {
            name = resources.getString("player.chaser") + ' ' + i;
            team.getChasers().add(PlayerGenerator.newChaser(name));
        }

        for(int i = 1; i <= Team.TOTAL_KEEPERS; i++) {
            name = resources.getString("player.keeper") + ' ' + i;
            team.getKeepers().add(PlayerGenerator.newKeeper(name));
        }

        for(int i = 1; i <= Team.TOTAL_SEEKERS; i++) {
            name = resources.getString("player.seeker") + ' ' + i;
            team.getSeekers().add(PlayerGenerator.newSeeker(name));
        }
        return team;
    }



}
