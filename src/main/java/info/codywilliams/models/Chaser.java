/*
 * Copyright (c) 2022. Cody Williams
 *
 * Chaser.java is part of Quidditch Season Generator.
 *
 * Quidditch Season Generator is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quidditch Season Generator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.models;

public class Chaser extends Player {

    public Chaser(String name, String[] pronouns, int skillDefense, int skillOffense,  int skillTeamwork, int foulLikelihood) {
        super(name, pronouns, skillDefense, skillOffense, skillTeamwork, foulLikelihood);
    }

    @Override
    public String toString() {
        return "Chaser" + super.toString();
    }


}
