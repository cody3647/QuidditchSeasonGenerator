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
import info.codywilliams.qsg.models.tournament.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public interface MatchGenerator {
    Logger logger = LoggerFactory.getLogger(MatchGenerator.class);

    static MatchGenerator create(long seed, int version) {
        logger.info("Creating MatchGenerator Version {} with seed: {}", version, seed);
        return switch (version) {
            default -> new MatchGeneratorV1(seed);
        };
    }

    static MatchGenerator create(long seed) {
        return create(seed, 1);
    }

    void generateMatches(Tournament tournament, List<Team> teamList);

    int getVersion();
}
