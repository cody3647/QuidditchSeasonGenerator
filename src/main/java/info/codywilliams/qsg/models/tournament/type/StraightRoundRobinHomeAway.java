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

package info.codywilliams.qsg.models.tournament.type;

import info.codywilliams.qsg.models.tournament.TournamentOptions;

public class StraightRoundRobinHomeAway extends StraightRoundRobin{
    public StraightRoundRobinHomeAway(TournamentOptions tournamentOptions) {
        super(tournamentOptions, TournamentType.STRAIGHT_ROUND_ROBIN_HOME_AWAY);
    }

    @Override
    protected void calculateNums(int numTeams) {
        super.calculateNums(numTeams);

        setNumMatches(numMatches.get() * 2);
        setNumRounds(numRounds.get() * 2);
    }
}
