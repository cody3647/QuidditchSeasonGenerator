package info.codywilliams.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import info.codywilliams.models.*;
import info.codywilliams.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Load program settings from config file.
 */
public class Configurator {
    /**
     * Path of json file with program settings
     */
    private Path settingsFile;

    /**
     * Creates the default config directory {@link Util#DEFAULT_SETTINGS_DIR} (if it doesn't exist) before calling {@link #setSettingsFile setConfigFile} to set configFile to the default location.
     *
     * @throws IOException if an I/O error occurs
     */
    public Configurator() throws IOException {
        // Create the config directory for the application
        if (!Files.exists(Util.DEFAULT_SETTINGS_DIR)) {
            Files.createDirectories(Util.DEFAULT_SETTINGS_DIR);
        }
        setSettingsFile(Util.DEFAULT_SETTINGS_DIR);
    }

    /**
     * If path is a directory, configFile is set to config.json in the directory provided.  Otherwise, sets configFile to the file provided.
     *
     * @param path the Path of the configFile or directory where config file is located.
     */
    private void setSettingsFile(Path path) {
        this.settingsFile = path;
    }

    /**
     * Sets up the Path to the location of the config file passed in via command line before calling {@link #setSettingsFile setConfigFile} to set configFile.
     *
     * @param settingsFileLocation the string location of the config file
     */
    public Configurator(String settingsFileLocation) {
        Path configFile = Paths.get(settingsFileLocation);
        setSettingsFile(configFile);
    }

    /**
     * Reads in the settings from the config file and returns a Settings object with the program settings.
     *
     * @return the Settings object with program settings for this instance of the program
     * @throws IOException if an I/O error occurs
     */
    public Settings loadSettings() throws IOException {
        if (settingsFile == null)
            return null;

        if (!Files.exists(settingsFile))
            throw new FileNotFoundException(settingsFile.toString() + " does not exist. Unable to load settings");

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(settingsFile.toFile(), Settings.class);
    }

    public boolean exportSettings(Settings settings, int year) {
        settingsFile = getSettingsFileName(year);


        try {
            backupSettingsFile();
        } catch (IOException e) {
            System.out.println("Issue creating backup of already existing settings file.");
            System.out.println(e);
            return false;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            mapper.writeValue(settingsFile.toFile(), settings);
        } catch (IOException e) {
            System.out.println("Issue writing settings to file.");
            System.out.println(e);
            return false;
        }

        return true;
    }

    private Path getSettingsFileName(int year) {
        return Path.of(Util.DEFAULT_SETTINGS_FILENAME[0] + '-' + year + '.' + Util.DEFAULT_SETTINGS_FILENAME[1]);
    }

    private void backupSettingsFile() throws IOException {
        if (Files.exists(settingsFile)) {
            String filename = "backup-" + Files.getLastModifiedTime(settingsFile).toInstant().getEpochSecond() + '.' + settingsFile.getFileName().toString();
            Path backup = settingsFile.resolveSibling(filename);
            Files.move(settingsFile, backup);
        }
    }

    /**
     * Populates a {@link Settings} object with dummy values, serializes the object to json, and creates the {@link #settingsFile}.
     *
     * @throws IOException if an I/O error occurs
     */
    public void createBlankConfigFile() throws IOException {
        int year = LocalDateTime.now().getYear();
        int seed = 100;

        Random rand = new Random(seed);
        settingsFile = getSettingsFileName(year);

        backupSettingsFile();

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<Team> teams = new ArrayList<>();
        int numOfTeams = 13;
        Team team;
        char teamCh = 'A';

        String playerName = "Player ";
        int playerCh = 1;

        List<String[]> pronouns = new ArrayList<>();
        pronouns.add(new String[]{"He", "Him", "His"});
        pronouns.add(new String[]{"She", "Her", "Hers"});
        pronouns.add(new String[]{"They", "Them", "Their"});


        for (int t = 1; t <= numOfTeams; t++) {
            team = new Team("Team " + teamCh, "Home " + teamCh);
            teamCh++;
            List<Chaser> chasers = new ArrayList<>();
            for (int p = 1; p <= Team.teamSize.CHASERS.getTotalNum(); p++) {
                chasers.add(
                        new Chaser(playerName + playerCh, pronouns.get(rand.nextInt(3)),
                                rand.nextInt(10), rand.nextInt(10), rand.nextInt(10), rand.nextInt(10))
                );
                playerCh++;
            }
            List<Beater> beaters = new ArrayList<>();
            for (int p = 1; p <= Team.teamSize.BEATERS.getTotalNum(); p++) {
                beaters.add(
                        new Beater(playerName + playerCh, pronouns.get(rand.nextInt(3)),
                                rand.nextInt(10), rand.nextInt(10), rand.nextInt(10), rand.nextInt(10))
                );
                playerCh++;
            }
            List<Keeper> keepers = new ArrayList<>();
            for (int p = 1; p <= Team.teamSize.CHASERS.getTotalNum(); p++) {
                keepers.add(
                        new Keeper(playerName + playerCh, pronouns.get(rand.nextInt(3)),
                                rand.nextInt(10), rand.nextInt(10), rand.nextInt(10), rand.nextInt(10))
                );
                playerCh++;
            }
            List<Seeker> seekers = new ArrayList<>();
            for (int p = 1; p <= Team.teamSize.CHASERS.getTotalNum(); p++) {
                seekers.add(
                        new Seeker(playerName + playerCh, pronouns.get(rand.nextInt(3)),
                                rand.nextInt(10), rand.nextInt(10), rand.nextInt(10), rand.nextInt(10))
                );
                playerCh++;
            }
            team.setChasers(chasers);
            team.setBeaters(beaters);
            team.setKeepers(keepers);
            team.setSeekers(seekers);

            teams.add(team);
            System.out.println(team);
        }


        // Create Settings object
        Settings settings = new Settings(teams, year, seed);
        mapper.writeValue(settingsFile.toFile(), settings);
    }

}


