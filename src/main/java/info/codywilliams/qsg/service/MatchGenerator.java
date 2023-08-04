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
import info.codywilliams.qsg.models.match.*;
import info.codywilliams.qsg.models.match.Play.InjuryType;
import info.codywilliams.qsg.models.player.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MatchGenerator {
    final public String version = "1.0";
    final static int BEATERS = 2;
    final static int CHASERS = 3;
    final static long[] SNITCH_VALUE_RANGE = new long[]{10, 90};
    final static long[] SNITCH_CHANCE_RANGE_STARTING_VALUE = new long[]{0, 100};
    final static long[] SNITCH_RELEASE_MINUTES_RANGE = new long[]{15, 30};
    final static long SNITCH_BASE_INTERACTION_RANGE = 10;
    final long seed;
    final Random random;
    final private Logger logger = LoggerFactory.getLogger(MatchGenerator.class);
    Match match;
    MatchTeam homeTeam;
    MatchTeam awayTeam;
    MatchTeam attackingTeam;
    MatchTeam defendingTeam;
    Chaser attacker;
    /**
     * The snitch value that will result in an attempt to catch the snitch, doesn't change
     */
    long snitchValue;
    /**
     * If generated snitch chance is in this range, then the snitch can be sighted or the snitchChanceRange being shrunk.
     */
    long[] snitchInteractionRange;
    /**
     * SnitchChanceRange is the range of values used to generate a chance of catching or sighting a snitch, or
     * shrinking the SnitchChanceRange
     */
    long[] snitchChanceRange;

    /**
     * The number of seconds before the snitch can be in play, randomly generated between .
     */
    long snitchReleaseSeconds;
    boolean snitchReleased;

    long hours;
    int seekerRoundLoops;


    public MatchGenerator(long seed) {
        this.seed = seed;
        random = new Random();
    }

    public void run(Match match) {
        this.match = match;
        MDC.put("title", this.match.getHomeTeam().getName() + " vs " + this.match.getAwayTeam().getName());
        logger.info("Generating Match");
        setUpMatch();
        generate();
        logger.info(this.match.outcomesToString());
        cleanUp();
    }

    private void cleanUp() {
        homeTeam.cleanUpTeam();
        awayTeam.cleanUpTeam();
    }

    private void setMatchSeed() {
        LocalDateTime startDateTime = match.getStartDateTime();
        // Generate seed for specific match
        // Any change to match date, match round, or teams playing will result in a different seed
        long matchSeed = ((long) startDateTime.getYear()) << 20;
        matchSeed |= startDateTime.getDayOfYear() << 11;
        matchSeed |= startDateTime.getHour() << 6;
        matchSeed |= startDateTime.getMinute();
        matchSeed |= match.getNumber() + match.getRound();
        matchSeed |= matchSeed << 34;
        matchSeed ^= ((long) match.getHomeTeam().hashCode() << 32) | match.getAwayTeam().hashCode();
        matchSeed ^= seed;
        random.setSeed(matchSeed);
        logger.info("Match Seed: {}", matchSeed);
    }

    private void setUpMatch() {
        match.clear();
        setMatchSeed();

        homeTeam = new MatchTeam(match.getHomeTeam(), TeamType.HOME, match.getStartDateTime().toLocalDate());
        awayTeam = new MatchTeam(match.getAwayTeam(), TeamType.AWAY, match.getStartDateTime().toLocalDate());

        match.setHomeTeamRoster(homeTeam.getMatchRoster());
        match.setAwayTeamRoster(awayTeam.getMatchRoster());
        setInjuredPlayers(match.getHomeTeam(), TeamType.HOME, match.getStartDateTime().toLocalDate());
        setInjuredPlayers(match.getAwayTeam(), TeamType.AWAY, match.getStartDateTime().toLocalDate());

        // Snitch values
        snitchValue = randomNumber(SNITCH_VALUE_RANGE);
        // Combine the skills of the two teams seekers, and take the average
        double seekerCombinedSkill = (homeTeam.getSeeker().getOffenceModifier() + awayTeam.getSeeker().getOffenceModifier()) / 2;
        long snitchRangeAdjuster = (long) ((SNITCH_BASE_INTERACTION_RANGE / 2) * (1.0 + seekerCombinedSkill));

        // Use above value to set the snitch range
        snitchInteractionRange = new long[]{snitchValue - snitchRangeAdjuster, snitchValue + snitchRangeAdjuster};
        // Set starting snitch chance range
        snitchChanceRange = Arrays.copyOf(SNITCH_CHANCE_RANGE_STARTING_VALUE, 2);
        snitchReleaseSeconds = (randomNumber(SNITCH_RELEASE_MINUTES_RANGE) * 60L) + randomNumber(0, 60);
        match.setSnitchReleaseTime(Duration.ofSeconds(snitchReleaseSeconds));
        snitchReleased = false;
        seekerRoundLoops = 1;
        hours = 0;
    }

    private void setInjuredPlayers(Team team, TeamType teamType, LocalDate date) {
        Predicate<Player> filterInjured = player -> player.isInjured(date);
        Consumer<Player> addInjuredToMatch = player -> this.match.addInjuredBeforePlayer(teamType, player);
        team.getBeaters().stream()
                .filter(filterInjured)
                .forEach(addInjuredToMatch);
        team.getChasers().stream()
                .filter(filterInjured)
                .forEach(addInjuredToMatch);
        team.getKeepers().stream()
                .filter(filterInjured)
                .forEach(addInjuredToMatch);
        team.getSeekers().stream()
                .filter(filterInjured)
                .forEach(addInjuredToMatch);
    }

    private void generate() {
        // Start the game, one team gets the quaffle first
        int homeStart = modifiedRandomNumbersSum(homeTeam.getChasersSkills().getAvgOffenseModifier());
        int awayStart = modifiedRandomNumbersSum(awayTeam.getChasersSkills().getAvgOffenseModifier());

        //  Set who is starting as offense and defense
        attackingTeam = homeStart >= awayStart ? homeTeam : awayTeam;
        defendingTeam = homeStart >= awayStart ? awayTeam : homeTeam;

        // Main game loop, continue until snitch is caught
        boolean snitchCaught = false;
        while (!snitchCaught) {
            // ChaserRound
            chaserRound();

            // Seeker Round
            int loops = seekerRoundLoops;
            while (!snitchCaught && loops > 0) {
                snitchCaught = seekerRound();
                loops--;
            }

            long h = hours;
            hours = match.getMatchLength().toHours();
            if (h != hours && hours > 1) {
                if (seekerRoundLoops >= 4)
                    seekerRoundLoops += 2;
                else
                    seekerRoundLoops *= 2;
            }
        }

        int scoreHome = match.getScoreHome();
        int scoreAway = match.getScoreAway();

        if (scoreHome > scoreAway)
            match.setWinner(TeamType.HOME);
        else if (scoreAway > scoreHome)
            match.setWinner(TeamType.AWAY);

    }

    Chaser getRandomChaser(MatchTeam team) {
        int chaserNum = randomNumber(0, 2);
        return team.getChaser(chaserNum);
    }

    Beater getRandomBeater(MatchTeam team) {
        int beaterNum = randomNumber(0, 1);
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
        int bludgerPlay = randomNumber(1, 3);
        boolean outcome = false;

        if (bludgerPlay % 3 == 0) {
            play.setBeaterBlocker(getRandomBeater(targetTeam));

            int hit = modifiedRandomNumbersSum(beater.getOffenceModifier(), beaterTeam.getBeatersSkills().getAvgOffenseModifier() * beaterTeam.getBeatersSkills().getAvgTeamworkModifier());
            int block = modifiedRandomNumbersSum(targetTeam.getBeatersSkills().getAvgDefenseModifier() * targetTeam.getBeatersSkills().getAvgTeamworkModifier());
            int miss = modifiedRandomNumbersSum(target.getDefenseModifier(), targetTeam.getChasersSkills().getAvgDefenseModifier() * targetTeam.getChasersSkills().getAvgTeamworkModifier());

            if (block > hit) {
                play.setBludgerOutcome(Play.BludgerOutcome.BLOCKED);
                playerInjuredDuringPlay(play, play.getBeaterBlocker(), targetTeam.type, InjuryType.BLUDGER_BLOCKED);
            } else if (miss > hit) {
                play.setBludgerOutcome(Play.BludgerOutcome.MISSED);
            } else {
                play.setBludgerOutcome(Play.BludgerOutcome.HIT);
                playerInjuredDuringPlay(play, target, targetTeam.type, InjuryType.BLUDGER_HIT);
                outcome = true;
            }

            logger.trace("Bludger Play:  Outcome: {}, Hit: {}, Block: {}, Miss: {}", play.getBludgerOutcome(), hit, block, miss);
        } else
            logger.trace("Bludger Play: {}", bludgerPlay);

        return outcome;
    }

    void chaserRound() {
        // Turnover loop
        while (randomNumber(1, 3) < 3) {
            turnover();
        }

        // Select our chaser who will make the shot
        attacker = getRandomChaser(attackingTeam);
        Keeper keeper = defendingTeam.getKeeper();
        Chaser defender = getRandomChaser(defendingTeam);
        PlayChaser play;

        int score = modifiedRandomNumbersSum(
                attacker.getOffenceModifier(), attackingTeam.getChasersSkills().getAvgOffenseModifier() * attackingTeam.getChasersSkills().getAvgTeamworkModifier());
        int block = modifiedRandomNumbersSum(
                keeper.getDefenseModifier(), defender.getDefenseModifier() * defendingTeam.getChasersSkills().getAvgTeamworkModifier());
        int miss = modifiedRandomNumbersSum(attacker.getOffenceModifier());


        if (isFoul(score, attacker)) {
            play = new PlayFoul(attacker, attackingTeam.type);
            foul(attacker, attackingTeam, defendingTeam, (PlayFoul) play);
        } else if (isFoul(block, defender)) {
            play = new PlayFoul(defender, defendingTeam.type);
            foul(defender, defendingTeam, attackingTeam, (PlayFoul) play);
        } else if (isFoul(block, keeper)) {
            play = new PlayFoul(keeper, defendingTeam.type);
            foul(keeper, defendingTeam, attackingTeam, (PlayFoul) play);
        } else {
            play = new PlayChaser(attackingTeam.type, defendingTeam.type, attacker, defender, keeper);
            attemptGoal(score, block, miss, play);
        }

        playerInjuredDuringPlay(play, keeper, defendingTeam.type, InjuryType.KEEPER);
        chaserInjuredDuringPlay(play);
        // AddPlay finalizes scores and match length assigned to the play
        match.addPlay(play);
        swapTeams(defender);
    }

    void turnover() {
        attacker = getRandomChaser(attackingTeam);
        // Get a chaser from the defending team
        Chaser defender = getRandomChaser(defendingTeam);
        // Create the play
        PlayChaser play = new PlayChaser(attackingTeam.type, defendingTeam.type, attacker, defender, null);
        // Does the attacker get hit by a bludger
        bludgerHit(play, getRandomBeater(defendingTeam), attacker, defendingTeam, attackingTeam);
        // Possible injury?
        chaserInjuredDuringPlay(play);
        // Set the outcome, add the play to the list and update the duration.
        play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.TURNOVER);
        play.setPlayDurationSeconds(randomNumber(15, 60));
        logger.trace("Turnover: Attacking team: {}, Quaffle Outcome: {}, Bludger Outcome: {}, Seconds: {}",
                play.getAttackingTeamType(), play.getQuaffleOutcome(), play.getBludgerOutcome(), play.getPlayDurationSeconds());
        match.addPlay(play);
        // Swap the teams, the chosen defender gets the quaffle
        swapTeams(defender);
    }

    private void foul(Player fouler, MatchTeam foulerTeam, MatchTeam otherTeam, PlayFoul playFoul) {
        Chaser penaltyShotTaker = getRandomChaser(otherTeam);
        Chaser defender = getRandomChaser(foulerTeam);
        Keeper keeper = foulerTeam.getKeeper();
        playFoul.setPlayChaser(otherTeam.type, foulerTeam.type, penaltyShotTaker, defender, keeper);

        int score = modifiedRandomNumbersSum(penaltyShotTaker.getOffenceModifier());
        int block = modifiedRandomNumbersSum(keeper.getDefenseModifier());
        int miss = modifiedRandomNumbersSum(attacker.getOffenceModifier());
        switch (foulerTeam.type) {
            case HOME -> match.incrementFoulsHome();
            case AWAY -> match.incrementFoulsAway();
        }
        playerInjuredDuringPlay(playFoul, keeper, foulerTeam.type, InjuryType.KEEPER);
        attemptGoal(score, block, miss, playFoul);
    }

    private boolean isFoul(int number, Player player) {
        switch (number) {
            case 4, 9, 16, 25, 36, 49, 64, 81, 100, 121, 144 -> {
                int foul = randomNumber(0, 10);
                logger.trace("Is Foul? {} < {} = {}", foul, player.getFoulLikelihood(), foul < player.getFoulLikelihood());
                return foul < player.getFoulLikelihood();
            }
            default -> {
                return false;
            }
        }
    }

    void attemptGoal(int score, int block, int miss, PlayChaser play) {
        if (score >= block && miss > 6) {
            play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.SCORED);
            switch (attackingTeam.type) {
                case HOME -> match.homeScore();
                case AWAY -> match.awayScore();
            }
        } else if (block >= score)
            play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.BLOCKED);
        else
            play.setQuaffleOutcome(PlayChaser.QuaffleOutcome.MISSED);

        play.setPlayDurationSeconds(randomNumber(20, 120));
        logger.trace("Attempt Goal: Outcome: {}, Seconds: {},  Score: {}, Block: {}, Miss: {}",
                play.getQuaffleOutcome(), play.getPlayDurationSeconds(), score, block, miss);
    }

    boolean seekerRound() {
        // If the snitch hasn't been released then there is nothing for seekers to do.
        if (!snitchReleased && match.getMatchLength().toSeconds() <= snitchReleaseSeconds)
            return false;

        snitchReleased = true;
        // Get the chance of something happening this iteration
        int snitchChance = randomNumber(snitchChanceRange);

        // If this isn't in the snitch range,  nothing happens this round
        if (!inRange(snitchChance, snitchInteractionRange)) {
            if (snitchChance % 13 == 0) {
                PlaySeeker playSeeker = new PlaySeeker(attackingTeam.getSeeker(), attackingTeam.getSeeker(), attackingTeam.type);
                playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.SEEN);
                match.addPlay(playSeeker);
                logger.trace("Snitch Seen: Snitch Chance: {} {}, Snitch Outcome: {}", snitchChance, snitchInteractionRange, playSeeker.getSnitchOutcome());
            }
            return false;
        }

        // Get the chance for each team's seeker to be the seeker to catch or see the snitch
        int homeSeekerChance;
        int awaySeekerChance;
        int difference;
        do {
            homeSeekerChance = modifiedRandomNumber(homeTeam.getSeeker().getOffenceModifier());
            awaySeekerChance = modifiedRandomNumber(awayTeam.getSeeker().getOffenceModifier());
            difference = homeSeekerChance - awaySeekerChance;
            // If difference is 0, run this again until it's not 0 to ensure we have a definitive seeker
        } while (difference == 0);

        Seeker seeker;
        MatchTeam seekerTeam;
        TeamType seekerTeamType;
        MatchTeam otherTeam;
        // Select seeker and other team based on which seeker chance came out on top above
        if (difference > 0) {
            seeker = homeTeam.getSeeker();
            seekerTeam = homeTeam;
            seekerTeamType = TeamType.HOME;
            otherTeam = awayTeam;
        } else {
            seeker = awayTeam.getSeeker();
            seekerTeam = awayTeam;
            seekerTeamType = TeamType.AWAY;
            otherTeam = homeTeam;
        }

        logger.trace("Seeker Round: Snitch Chance: {} {}, Snitch Value: {}, Home Seeker Chance: {}, Away Seeker Chance: {}",
                snitchChance, snitchInteractionRange, snitchValue, homeSeekerChance, awaySeekerChance);
        // If snitchChance equals snitch, an attempt is made and the snitch is either Caught, Stolen, or Missed
        if (snitchChance == snitchValue) {
            PlaySeeker playSeeker = attemptCatchSnitch(seeker, seekerTeam, seekerTeamType, otherTeam);

            if (playSeeker.isSnitchCaught()) {
                switch (playSeeker.getAttackingTeamType()) {
                    case HOME -> match.homeCaughtSnitch();
                    case AWAY -> match.awayCaughtSnitch();
                }
            }

            // AddPlay finalizes scores and match length assigned to the play
            match.addPlay(playSeeker);
            logger.trace("Seeker Play: Team Type: {}, Snitch Outcome: {}, Bludger Outcome: {}", playSeeker.getAttackingTeamType(), playSeeker.getSnitchOutcome(), playSeeker.getBludgerOutcome());
            return playSeeker.isSnitchCaught();
        }

        // Snitch wasn't attempted, let's see if it was seen
        if (snitchChance % 4 == 0) {
            PlaySeeker playSeeker = new PlaySeeker(seeker, otherTeam.getSeeker(), seekerTeamType);
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.SEEN);
            match.addPlay(playSeeker);
            logger.trace("Seeker Play: Team Type: {}, Snitch Outcome: {}, Bludger Outcome: {}", playSeeker.getAttackingTeamType(), playSeeker.getSnitchOutcome(), playSeeker.getBludgerOutcome());
        }

        // Snitch was not caught, shrink the chance window
        shrinkSnitchChanceRange(snitchChance);
        return false;
    }

    PlaySeeker attemptCatchSnitch(Seeker seeker, MatchTeam seekerTeam, TeamType seekerTeamType, MatchTeam otherTeam) {
        PlaySeeker playSeeker = new PlaySeeker(seeker, otherTeam.getSeeker(), seekerTeamType);
        // The other teams beater might disrupt them
        Beater beater = getRandomBeater(otherTeam);
        boolean bludgerHit = false;
        if (random.nextBoolean())
            bludgerHit = bludgerHit(playSeeker, beater, seeker, otherTeam, seekerTeam);

        boolean stolen = bludgerHit && random.nextBoolean();
        boolean missed = bludgerHit && random.nextBoolean();

        if (stolen) {
            playSeeker.swapTeam();
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.STOLEN);
            playSeeker.setPlayDurationSeconds(randomNumber(15, 45));
        } else if (missed) {
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.MISSED);
            playSeeker.setPlayDurationSeconds(randomNumber(15, 45));
        } else {
            playSeeker.setSnitchOutcome(PlaySeeker.SnitchOutcome.CAUGHT);
            playSeeker.setPlayDurationSeconds(randomNumber(15, 60));
        }
        return playSeeker;
    }

    void shrinkSnitchChanceRange(int snitchChance) {
        if (snitchChanceRange[0] == snitchInteractionRange[0] && snitchChanceRange[1] == snitchInteractionRange[1])
            return;

        // if chance is divisible by 3 (or later in the match 2) shrink the chance range
        int divisor;
        long minutes = match.getMatchLength().toMinutes();

        if (minutes > 150)
            divisor = 1;
        else if (minutes > 110)
            divisor = 2;
        else if (minutes > 85)
            divisor = 3;
        else
            divisor = 4;

        if (snitchChance % divisor != 0)
            return;

        // Don't shrink the chance range smaller than the snitch range
        if (snitchChanceRange[0] < snitchInteractionRange[0])
            snitchChanceRange[0] += 1;
        if (snitchChanceRange[1] > snitchInteractionRange[1])
            snitchChanceRange[1] -= 1;

        logger.trace("Shrink Snitch Chance Range: Divisor: {}, Snitch Chance: {}, Snitch Chance Range: {}, Snitch Interaction Range, {}", divisor, snitchChance, snitchChanceRange, snitchInteractionRange);
    }

    private void chaserInjuredDuringPlay(Play play) {
        if (random.nextBoolean())
            playerInjuredDuringPlay(play, getRandomChaser(homeTeam), homeTeam.type, InjuryType.CHASER);
        else
            playerInjuredDuringPlay(play, getRandomChaser(awayTeam), awayTeam.type, InjuryType.CHASER);
    }

    private void playerInjuredDuringPlay(Play play, Player player, TeamType playerTeam, InjuryType injuryType) {
        if (play.getInjuryType() != InjuryType.NONE)
            return;

        List<Integer> injuryChance = List.of(
                randomNumber(1, 1000),
                randomNumber(1, 1000),
                randomNumber(1, 1000),
                randomNumber(1, 1000)
        );
        List<Boolean> willBeInjured = List.of(
                injuryChance.get(0) % 2 == 0,
                injuryChance.get(1) % 3 == 0,
                injuryChance.get(2) % 4 == 0,
                injuryChance.get(3) % 5 == 0
        );
        logger.trace("Injury: {}, {}", injuryChance, willBeInjured);

        if (willBeInjured.stream().allMatch(bool -> bool)) {
            int injury = randomNumber(1, 6);
            logger.trace("Injured: {} % 6 = {}", injury, injury % 6);
            LocalDate endDate;
            switch (injury % 6) {
                case 0, 1 -> {
                    int weeks = randomNumber(0, 2);
                    double severity = random.nextDouble(1.4, 2.5);
                    endDate = injuryCalculation(player, weeks, ChronoUnit.WEEKS, severity);
                }
                case 2 -> {
                    int weeks = randomNumber(0, 3);
                    double severity = random.nextDouble(1.7, 2.5);
                    endDate = injuryCalculation(player, weeks, ChronoUnit.WEEKS, severity);
                }
                default -> {
                    double severity = random.nextDouble(1.1, 1.7);
                    endDate = injuryCalculation(player, 0, ChronoUnit.DAYS, severity);
                }
            }
            play.setInjury(injuryType, player, playerTeam, endDate);
        }
    }

    private LocalDate injuryCalculation(Player player, int amount, TemporalUnit unit, double severity) {
        LocalDate startDate = match.getStartDateTime().toLocalDate();
        LocalDate endDate = startDate.plus(amount, unit);
        logger.debug("Injury: {}, {} {}, {}, {} - {}", player.getName(), amount, unit, severity, startDate, endDate);
        player.setCurrentlyInjured(true);
        player.setInjuryDivisor(severity);
        player.addInjuryDate(startDate, endDate);

        return endDate;
    }

    private int randomNumber(long lowestNumber, long largestNumber) {
        int number = random.nextInt((int) lowestNumber, (int) largestNumber + 1);
        logger.trace("Random Number: Number: {} [{}, {}]",
                number, lowestNumber, largestNumber);
        return number;
    }

    private int modifiedRandomNumbersSum(double... modifiers) {
        double modifier = 1.0 + Arrays.stream(modifiers).sum();
        long origin = 1;
        long bound = 48;
        int size = 3;

        long sum = random.longs(size, origin, bound + 1).sum();
        long result = Math.round(sum * modifier);

        logger.trace("Random Sum: Sum: {} [{}, {}, {}], Modifier: {}, Modifiers: {}, Result: {}",
                sum, origin, bound, size, modifier, modifiers, result);
        if (result > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Result is too large, check modifier and bounds");
        return (int) result;
    }

    private int modifiedRandomNumber(double... modifiers) {
        double modifier = 1.0 + Arrays.stream(modifiers).sum();
        int origin = 1;
        int bound = 100;

        int number = random.nextInt(origin, bound + 1);
        long result = Math.round(number * modifier);

        logger.trace("Random Number: Number: {} [{}, {}], Modifier: {}, Modifiers: {}, Result: {}",
                number, origin, bound, modifier, modifiers, result);

        if (result > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Result is too large, check modifier and bounds");

        return (int) result;
    }

    private int randomNumber(long[] range) {
        return randomNumber(range[0], range[1]);
    }

    private boolean inRange(long number, long[] range) {
        return range[0] <= number && number <= range[1];
    }

    static private class MatchTeam {
        private final Team team;
        private final TeamType type;
        private final List<Beater> beaters;
        private final List<Chaser> chasers;
        private final Keeper keeper;
        private final Seeker seeker;
        private final CollectiveSkills beatersSkills;
        private final CollectiveSkills chasersSkills;
        private final CollectiveSkills teamSkills;

        MatchTeam(Team team, TeamType type, LocalDate date) {
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
                } else {
                    player.isCurrentlyInjured = false;
                    uninjuredPlayers.add(player);
                }
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

        Map<String, List<? extends Player>> getMatchRoster() {
            return Map.of(
                    "Beaters", beaters,
                    "Chasers", chasers,
                    "Keeper", List.of(keeper),
                    "Seeker", List.of(seeker)
            );
        }
    }

    static private class CollectiveSkills {
        private double offense;
        private double defense;
        private double teamwork;
        private double foulLikelihood;

        CollectiveSkills(List<? extends Player> players) {
            offense = 0;
            defense = 0;
            teamwork = 0;
            foulLikelihood = 0;

            for (Player player : players) {
                offense += player.getOffenceModifier();
                defense += player.getDefenseModifier();
                teamwork += player.getTeamworkModifier();
                foulLikelihood += player.getFoulModifier();
            }

            // Average the modifiers
            offense /= players.size();
            defense /= players.size();
            teamwork /= players.size();
            foulLikelihood /= players.size();
        }

        double getAvgOffenseModifier() {
            return offense;
        }

        double getAvgDefenseModifier() {
            return defense;
        }

        double getAvgTeamworkModifier() {
            return teamwork;
        }

        double getAvgFoulModifier() {
            return foulLikelihood;
        }
    }

}