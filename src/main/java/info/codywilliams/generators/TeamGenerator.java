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

package info.codywilliams.generators;

import info.codywilliams.models.Context;
import info.codywilliams.models.Team;

public class TeamGenerator {
    private TeamGenerator(){}
    static public Team randomTeam(){
        Context context = Context.getInstance();
        Team team = new Team();

        team.setName(context.getTeamNames().getNextName());
        team.setHome(team.getName() + " Pitch");

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




}
