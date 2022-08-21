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

package info.codywilliams.qsg.generators;

import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.player.*;

import java.util.Random;

public class PlayerGenerator {
    private PlayerGenerator(){}
    public static Beater randomBeater(){
        Beater beater = new Beater();
        randomPlayer(beater);

        return beater;
    }

    public static Chaser randomChaser(){
        Chaser chaser = new Chaser();
        randomPlayer(chaser);

        return chaser;
    }

    public static Keeper randomKeeper(){
        Keeper keeper = new Keeper();
        randomPlayer(keeper);

        return keeper;
    }

    public static Seeker randomSeeker(){
        Seeker seeker = new Seeker();
        randomPlayer(seeker);

        return seeker;
    }

    public static Beater newBeater(String name){
        Beater beater = new Beater();
        beater.setName(name);
        randomSkills(beater);

        return beater;
    }

    public static Chaser newChaser(String name){
        Chaser chaser = new Chaser();
        chaser.setName(name);
        randomSkills(chaser);

        return chaser;
    }

    public static Keeper newKeeper(String name){
        Keeper keeper = new Keeper();
        keeper.setName(name);
        randomSkills(keeper);

        return keeper;
    }

    public static Seeker newSeeker(String name){
        Seeker seeker = new Seeker();
        seeker.setName(name);
        randomSkills(seeker);

        return seeker;
    }

    private static void randomPlayer(Player player){
        Context context = Context.getInstance();
        NameGenerator femaleNames = context.getFemaleNames();
        NameGenerator maleNames = context.getMaleNames();
        NameGenerator nonBinaryNames = context.getNonBinaryNames();
        NameGenerator surnames = context.getSurnames();
        Random random = new Random();

        int i = random.nextInt(0, 100);
        String name;
        if(i < 30)
            name = femaleNames.getNextName();
        else if (i < 90)
            name = maleNames.getNextName();
        else
            name = nonBinaryNames.getNextName();

        name = name + ' ' + surnames.getNextName();


        player.setName(name);
        randomSkills(player);
    }

    private static void randomSkills(Player player){
        Random random = new Random();

        player.setSkillOffense(random.nextInt(1, 11));
        player.setSkillDefense(random.nextInt(1, 11));
        player.setSkillTeamwork(random.nextInt(1, 11));
        player.setFoulLikelihood((random.nextInt(1,15) % 10) + 1);
    }
}
