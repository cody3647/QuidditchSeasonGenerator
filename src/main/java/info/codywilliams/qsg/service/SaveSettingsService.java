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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.SerializationFeature;
import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.SaveSettings;
import info.codywilliams.qsg.util.Formatters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ResourceBundle;

import static info.codywilliams.qsg.App.mapper;

public class SaveSettingsService {
    private static final String DEFAULT_FILE_NAME = "QSG-Settings-";
    private final Context context;
    private final ResourceBundle resourceBundle;
    private File currentSaveFile;
    private final Logger logger = LoggerFactory.getLogger(SaveSettingsService.class);

    public SaveSettingsService(Context context, ResourceBundle resourceBundle) {
        this.context = context;
        this.resourceBundle = resourceBundle;
    }

    public void loadSettings(File settingsFile) throws IOException {
        try {
            SaveSettings settings = mapper.readValue(settingsFile, SaveSettings.class);

            context.clearContext();
            context.loadContext(settings);
            setCurrentSaveFile(settingsFile);
            context.setSettingsStatus(getSettingsStatus());
        } catch (IOException e) {
            logger.error("Unable to load {}", settingsFile, e);
            context.setSettingsStatus(resourceBundle.getString("error.loadSettings"));
            throw new IOException(e);
        }
    }

    public void saveSettings() throws Exception {
        if (currentSaveFile == null) {
            setCurrentSaveFile(new File(DEFAULT_FILE_NAME + Instant.now()));
        }
        SaveSettings settings = new SaveSettings(context);
        try {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(getCurrentSaveFile(), settings);

            context.setSettingsStatus(getSettingsStatus());
        } catch (StreamWriteException | DatabindException e) {
            logger.error("Unable to generate JSON from settings: {}", settings,  e);
            context.setSettingsStatus(resourceBundle.getString("error.saveSettings"));
            throw new Exception(e);
        } catch (IOException e) {
            logger.error("Unable to write to {}", currentSaveFile, e);
            context.setSettingsStatus(resourceBundle.getString("error.saveSettings"));
            throw new Exception(e);
        }
    }

    public String getSettingsJsonAsString() throws JsonProcessingException {
        SaveSettings settings = new SaveSettings(context);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(settings);
    }

    private String getSettingsStatus() {
        Instant timestamp = Instant.ofEpochMilli(currentSaveFile.lastModified());
        return String.format(
                "%s %s %s",
                resourceBundle.getString("app.lastSaved"),
                currentSaveFile.getName(),
                Formatters.dateTimeFormatter.format(timestamp)
        );
    }

    public File getCurrentSaveFile() {
        return currentSaveFile;
    }

    public void setCurrentSaveFile(File currentSaveFile) {
        this.currentSaveFile = currentSaveFile;
    }
}
