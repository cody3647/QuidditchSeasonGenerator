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

import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.match.Play;
import info.codywilliams.qsg.models.match.PlayChaser;
import info.codywilliams.qsg.models.match.PlaySeeker;
import info.codywilliams.qsg.models.player.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class MatchGenerator {
    final static int BEATERS = 2;
    final static int CHASERS = 3;
    final static int[] SNITCH_VALUE_RANGE = new int[]{10,90};
    final static int[] SNITCH_CHANCE_RANGE_STARTING_VALUE = new int[]{0, 100};
    final static int[] SNITCH_RELEASE_SECONDS_RANGE = new int[] {13 * 60, 17 * 60};
    final long seed;
    final Random random;
    Match match;
    MatchTeam homeTeam;
    MatchTeam awayTeam;
    MatchTeam attackingTeam;
    MatchTeam defendingTeam;
    Chaser attacker;
    /**
     * The snitch value that will result in an attempt to catch the snitch, doesn't change
     */
    int snitchValue;
    /**
     * If generated snitch chance is in this range, then the snitch can be sighted or the snitchChanceRange being shrunk.
     */
    int[] snitchInteractionRange;
    /**
     * SnitchChanceRange is the range of values used to generate a chance of catching or sighting a snitch, or
     * shrinking the SnitchChanceRange
     */
    int[] snitchChanceRange;

    /**
     * The number of seconds before the snitch can be in play, randomly generated between .
     */
    int snitchReleaseSeconds;
    boolean snitchReleased;


    public MatchGenerator(long seed) {
        this.seed = seed;
        random = new Random();
    }

    public static void main(String[] args) {
        Locale locale = Locale.getDefault();
        ResourceBundle playProperties = ResourceBundle.getBundle("info.codywilliams.qsg.language.Output", locale);

        Team homeTeam1 = TeamGenerator.randomTeam();
        Team awayTeam1 = TeamGenerator.randomTeam();
        Team awayTeam2 = TeamGenerator.randomTeam();

        System.out.println("New Match");
        Match match = new Match(1, 1, LocalDateTime.now());
        match.setHomeTeam(homeTeam1);
        match.setAwayTeam(awayTeam1);
        MatchGenerator matchGenerator = new MatchGenerator(new Random(0).nextLong());
        matchGenerator.setUpMatch(match);
        matchGenerator.generate();
        System.out.println("Match Generation Done");
        System.out.println("Home Team: " + homeTeam1.getName() + " \t\t Away Team: " + awayTeam1.getName());
        for(Play play: match.getPlays()) {
            System.out.println(play.outputWithDetails(playProperties, homeTeam1.getName(), awayTeam1.getName()));
        }
        System.out.println();
        System.out.println("Home Score: " + match.getScoreHome() + "\t\t Away Score: " + match.getScoreAway() +
                "\t\t Duration: " + match.getMatchLength());

        System.out.println("New Match");
        match = new Match(2, 1, LocalDateTime.now());
        match.setHomeTeam(homeTeam1);
        match.setAwayTeam(awayTeam2);
        matchGenerator.setUpMatch(match);
        matchGenerator.generate();
        System.out.println("Match Generation Done");
        System.out.println("Home Team: " + homeTeam1.getName() + " \t\t Away Team: " + awayTeam2.getName());
        for(Play play: match.getPlays()) {
            System.out.println(play.outputWithDetails(playProperties, homeTeam1.getName(), awayTeam2.getName()));
        }
        System.out.println();
        System.out.println("Home Score: " + match.getScoreHome() + "\t\t Away Score: " + match.getScoreAway() +
                "\t\t Duration: " + match.getMatchLength());
    }

    public void setUpMatch(Match match) {
        this.match = match;
        LocalDateTime startDateTime = match.getStartDateTime();

        // Generate seed for specific match
        // Any change to match date, match round, or teams playing will result in a different seed
        long matchSeed = ((long) startDateTime.getYear()) << 20;
        matchSeed |= startDateTime.getDayOfYear() << 11;
        matchSeed |= startDateTime.getHour() << 6;
        matchSeed |= startDateTime.getMinute();
        matchSeed |= match.getNumber() + match.getRound();
        matchSeed |= matchSeed << 34;
        matchSeed ^= ((long) match.getHomeTeam().hashCode() << 32) | match.getHomeTeam().hashCode();
        matchSeed ^= seed;
        random.setSeed(matchSeed);

        homeTeam = new MatchTeam(match.getHomeTeam(), Play.TeamType.HOME, startDateTime.toLocalDate());
        awayTeam = new MatchTeam(match.getAwayTeam(), Play.TeamType.AWAY, startDateTime.toLocalDate());

        // Snitch values
        snitchValue = randomNumber(SNITCH_VALUE_RANGE);
        // Combine the skills of the two teams seekers, offense - defense, and then take average
        int snitchRangeAdjuster = Math.abs(homeTeam.getSeeker().getSkillOffense() - awayTeam.getSeeker().getSkillDefense()
                + awayTeam.getSeeker().getSkillOffense() - homeTeam.getSeeker().getSkillDefense()) / 2;
        // Use above value to set the snitch range
        snitchInteractionRange = new int[]{snitchValue - snitchRangeAdjuster, snitchValue + snitchRangeAdjuster};
        // Set starting snitch chance range
        snitchChanceRange = Arrays.copyOf(SNITCH_CHANCE_RANGE_STARTING_VALUE, 2);
        snitchReleaseSeconds = randomNumber(SNITCH_RELEASE_SECONDS_RANGE);
        snitchReleased = false;
    }

    public void generate() {
        // Start the game, one team gets the quaffle first
        int homeStart = randomNumbersSum(1, 10, 5) + homeTeam.getChasersSkills().getOffense();
        int awayStart = randomNumbersSum(1, 10, 5) + awayTeam.getChasersSkills().getOffense();

        //  Set who is starting as offense and defense, set time elapsed during first play
        attackingTeam = homeStart >= awayStart ? homeTeam : awayTeam;
        defendingTeam = homeStart >= awayStart ? awayTeam : homeTeam;

        // Set the chaser with the quaffle
        attacker = getRandomChaser(attackingTeam);
        boolean snitchCaught = false;

        // Main game loop, continue until snitch is caught
        do {
            // ChaserRound
            // Turnover loop
            while(randomNumber(1,3) < 3){
                turnover();
            }
            attemptGoal();

            // Seeker Round
            snitchCaught = seekerRound();

        } while (!snitchCaught);

    }

    Chaser getRandomChaser(MatchTeam team) {
        int chaserNum = randomNumber(0,2);
        return team.getChaser(chaserNum);
    }

    Beater getRandomBeater(MatchTeam team) {
        int beaterNum = randomNumber(0,1);
        return team.getBeater(beaterNum);
    }

    void swapTeams(Chaser newAttacker) {
        MatchTeam temp = attackingTeam;
        attackingTeam = defendingTeam;
        defendingTeam = temp;
        attacker = newAttacker;
    }

    /**
     * @param play       the ongoing play
     * @param beater     the beater who tries to hit the bludger
     * @param target     the player the beater is trying to hit
     * @param beaterTeam Team of the beater
     * @param targetTeam Team of the targeted player
     * @return whether the target was hit or not.
     */
    boolean bludgerHit(Play play, Beater beater, Player target, MatchTeam beaterTeam, MatchTeam targetTeam) {
        play.setBeaterHitter(beater);
        // Maximum chance is 50
        int hitChance = randomNumbersSum(1, 10, 3) +
                beater.getSkillOffense() + (beaterTeam.getBeatersSkills().getTeamwork() / 2);

        if (hitChance > 25) {
            play.setBeaterBlocker(getRandomBeater(targetTeam));
            int blockChance = randomNumbersSum(1, 10, 3) +
                    (targetTeam.getBeatersSkills().getDefense() / 2) + (beaterTeam.getBeatersSkills().getTeamwork() / 2);

            if (blockChance > hitChance) {
                play.setBludgerOutcome(Play.BludgerOutcome.BLOCKED);
                return false;
            }

            int missChance = randomNumbersSum(2, 11, 3) + target.getSkillDefense() + target.getSkillTeamwork();
            if (missChance > 25) {
                play.setBludgerOutcome(Play.BludgerOutcome.MISSED);
                return false;
            }

            play.setBludgerOutcome(Play.BludgerOutcome.HIT);
            return true;
        }
        return false;
    }

    void turnover() {
        attacker = getRandomChaser(attackingTeam);
        // Get a chaser from the defending team
        Chaser defender = getRandomChaser(defendingTeam);
        // Create the play
        PlayChaser play = new PlayChaser(attackingTeam.type, defendingTeam.type, attacker, defender, null);
        // Does the attacker get hit by a bludger
        bludgerHit(play, getRandomBeater(defendingTeam), attacker, defendingTeam, attackingTeam);

        // Set the outcome, add the play to the list and update the duration.
        play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.TURNOVER);
        play.setPlayDurationSeconds(randomNumber(10,45));
        match.addPlay(play);

        // Swap the teams, the chosen defender gets the quaffle
        swapTeams(defender);
    }

    void attemptGoal() {
        // Select our chaser who will make the shot
        attacker = getRandomChaser(attackingTeam);
        Keeper keeper = defendingTeam.getKeeper();
        Chaser defender = getRandomChaser(defendingTeam);
        PlayChaser play = new PlayChaser(attackingTeam.type, defendingTeam.type, attacker, defender, keeper);

        // Up to a third of the chance comes from the skill of the players
        int chaserChance = randomNumbersSum(1,10, 2) + attacker.getSkillOffense();
        int keeperChance = randomNumbersSum(1,10, 2) + keeper.getSkillDefense();


        if(chaserChance > keeperChance) {
            play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.SCORED);
            switch(attackingTeam.type) {
                case HOME -> match.homeScore();
                case AWAY -> match.awayScore();
            }
        }
        else if(keeperChance > chaserChance)
            play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.BLOCKED);
        else
            play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.MISSED);

        play.setPlayDurationSeconds(randomNumber(20,120));
        match.addPlay(play);
        swapTeams(defender);
    }

    boolean seekerRound() {
        // If the snitch hasn't been released then there is nothing for seekers to do.
        if (!snitchReleased && match.getMatchLength().toSeconds() <= snitchReleaseSeconds)
            return false;

        // Get the chance of something happening this iteration
        int snitchChance = randomNumber(snitchChanceRange);

        // If this isn't in the snitch range,  nothing happens this round
        if (!inRange(snitchChance, snitchInteractionRange))
            return false;

        // Get the chance for each team's seeker to be the seeker to catch or see the snitch
        int homeSeekerChance;
        int awaySeekerChance;
        int difference;
        do {
            homeSeekerChance = randomNumber(1, 1000) + (homeTeam.getSeeker().getSkillOffense() * 20);
            awaySeekerChance = randomNumber(1, 1000) + (awayTeam.getSeeker().getSkillOffense() * 20);
            difference = homeSeekerChance - awaySeekerChance;
            // If difference is 0, run this again until it's not 0 to ensure we have a definitive seeker
        } while (difference == 0);

        Seeker seeker;
        MatchTeam seekerTeam;
        Play.TeamType seekerTeamType;
        MatchTeam otherTeam;
        // Select seeker and other team based on which seeker chance came out on top above
        if (difference > 0) {
            seeker = homeTeam.getSeeker();
            seekerTeam = homeTeam;
            seekerTeamType = Play.TeamType.HOME;
            otherTeam = awayTeam;
        } else {
            seeker = awayTeam.getSeeker();
            seekerTeam = awayTeam;
            seekerTeamType = Play.TeamType.AWAY;
            otherTeam = homeTeam;
        }

        // If snitchChance equals snitch, an attempt is made and the snitch is either Caught, Stolen, or Missed
        if (snitchChance == snitchValue) {
            PlaySeeker playSeeker = attemptCatchSnitch(seeker, seekerTeam, seekerTeamType, otherTeam);
            match.addPlay(playSeeker);

            if(playSeeker.isSnitchCaught()) {
                switch (playSeeker.getAttackingTeamType()) {
                    case HOME -> match.homeCaughtSnitch();
                    case AWAY -> match.awayCaughtSnitch();
                }
            }

            return playSeeker.isSnitchCaught();
        }

        // Snitch wasn't attempted, let's see if it was seen
        if (snitchChance % 4 == 0) {
            PlaySeeker playSeeker = new PlaySeeker(seeker, otherTeam.getSeeker(), seekerTeamType);
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.SEEN);
            match.addPlay(playSeeker);
        }

        // Snitch was not caught, shrink the chance window
        shrinkSnitchChanceRange(snitchChance);
        return false;
    }

    PlaySeeker attemptCatchSnitch(Seeker seeker, MatchTeam seekerTeam, Play.TeamType seekerTeamType, MatchTeam otherTeam) {
        PlaySeeker playSeeker = new PlaySeeker(seeker, otherTeam.getSeeker(), seekerTeamType);
        // The other teams beater might disrupt them
        Beater beater = getRandomBeater(otherTeam);
        boolean bludgerHit = false;
        if (random.nextBoolean())
            bludgerHit = bludgerHit(playSeeker, beater, seeker, otherTeam, seekerTeam);

        boolean stolen = bludgerHit && random.nextBoolean();
        boolean missed = bludgerHit && random.nextBoolean();

        if(stolen) {
            playSeeker.swapTeam();
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.STOLEN);
            playSeeker.setPlayDurationSeconds(randomNumber(30,75));
        }
        else if (missed) {
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.MISSED);
            playSeeker.setPlayDurationSeconds(randomNumber(30,75));
        }
        else {
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.CAUGHT);
            playSeeker.setPlayDurationSeconds(randomNumber(15,60));
        }
        return playSeeker;
    }

    void shrinkSnitchChanceRange(int snitchChance) {
        // if chance is divisible by 3 (or later in the match 2) shrink the chance range
        int divisor = match.getMatchLength().toMinutes() >= 120 ? 2 : 3;
        if(snitchChance % divisor != 0)
            return;

        // Don't shrink the chance range smaller than the snitch range
        if(snitchChanceRange[0] < snitchInteractionRange[0])
            snitchChanceRange[0] += 1;
        if(snitchChanceRange[1] > snitchInteractionRange[1])
            snitchChanceRange[1] -= 1;
    }

    private int randomNumber(int lowestNumber, int largestNumber) {
        return random.nextInt(lowestNumber, largestNumber + 1);
    }

    private int randomNumbersSum(int lowestNumber, int largestNumber, int num) {
        return random.ints(num, lowestNumber, largestNumber + 1).sum();
    }

    private int randomNumber(int[] range) {
        return randomNumber(range[0], range[1]);
    }

    private boolean inRange(int number, int[] range) {
        return inRange(number, range[0], range[1]);
    }
    private boolean inRange(int number, int lowEnd, int highEnd) {
        return lowEnd <= number && number <= highEnd;
    }

    public void cleanUp() {
        homeTeam.cleanUpTeam();
        awayTeam.cleanUpTeam();
    }

    static private class MatchTeam {
        private final Team team;
        private final Play.TeamType type;
        private final List<Beater> beaters;
        private final List<Chaser> chasers;
        private final Keeper keeper;
        private final Seeker seeker;
        private final CollectiveSkills beatersSkills;
        private final CollectiveSkills chasersSkills;
        private final CollectiveSkills teamSkills;

        MatchTeam(Team team, Play.TeamType type, LocalDate date) {
            this.team = team;
            this.type = type;
            beaters = getPlayersForMatch(date, team.getBeaters()).subList(0, BEATERS);
            chasers = getPlayersForMatch(date, team.getChasers()).subList(0, CHASERS);
            keeper = getPlayersForMatch(date, team.getKeepers()).get(0);
            seeker = getPlayersForMatch(date, team.getSeekers()).get(0);

            beatersSkills = new CollectiveSkills(beaters);
            chasersSkills = new CollectiveSkills(chasers);
            List<Player> players = new ArrayList<>();
            players.addAll(beaters);
            players.addAll(chasers);
            players.add(keeper);
            players.add(seeker);
            teamSkills = new CollectiveSkills(players);
        }

        <T extends Player> ArrayList<T> getPlayersForMatch(LocalDate date, List<T> players) {
            ArrayList<T> uninjuredPlayers = new ArrayList<>();
            ArrayList<T> injuredPlayers = new ArrayList<>();

            for (T player : players) {
                if (player.isInjured(date)) {
                    player.isCurrentlyInjured = true;
                    injuredPlayers.add(player);
                } else
                    uninjuredPlayers.add(player);
            }

            Collections.sort(uninjuredPlayers);
            Collections.sort(injuredPlayers);
            uninjuredPlayers.addAll(injuredPlayers);

            return uninjuredPlayers;
        }

        void cleanUpTeam() {
            markNotCurrentlyInjured(team.getBeaters());
            markNotCurrentlyInjured(team.getChasers());
            markNotCurrentlyInjured(team.getKeepers());
            markNotCurrentlyInjured(team.getSeekers());
        }

        void markNotCurrentlyInjured(List<? extends Player> players) {
            for (Player player : players)
                player.isCurrentlyInjured = false;
        }

        Beater getBeater(int idx) {
            return beaters.get(idx % BEATERS);
        }

        Chaser getChaser(int idx) {
            return chasers.get(idx % CHASERS);
        }

        Keeper getKeeper() {
            return keeper;
        }

        Seeker getSeeker() {
            return seeker;
        }

        CollectiveSkills getBeatersSkills() {
            return beatersSkills;
        }

        CollectiveSkills getChasersSkills() {
            return chasersSkills;
        }

        CollectiveSkills getTeamSkills() {
            return teamSkills;
        }
    }

    static private class CollectiveSkills {
        private int offense;
        private int defense;
        private int teamwork;
        private int foulLikelihood;

        CollectiveSkills(List<? extends Player> players) {
            offense = 0;
            defense = 0;
            teamwork = 0;
            foulLikelihood = 0;

            for (Player player : players) {
                offense += player.getSkillOffense();
                defense += player.getSkillDefense();
                teamwork += player.getSkillTeamwork();
                foulLikelihood += player.getFoulLikelihood();
            }
        }

        int getOffense() {
            return offense;
        }

        int getDefense() {
            return defense;
        }

        int getTeamwork() {
            return teamwork;
        }

        int getFoulLikelihood() {
            return foulLikelihood;
        }
    }

}