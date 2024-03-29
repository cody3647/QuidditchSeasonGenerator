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

package info.codywilliams.qsg.util;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Formatters {
    public final static DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("cccc',' d LLLL yyyy 'at' h':'mm a").withZone(ZoneId.systemDefault());

    public final static DateTimeFormatter shortDateTimeFormatter =
            DateTimeFormatter.ofPattern("ccc',' d LLLL yyyy 'at' h':'mm a").withZone(ZoneId.systemDefault());

    public final static DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public final static DateTimeFormatter yearlessDateFormatter =
            DateTimeFormatter.ofPattern("d MMM").withZone(ZoneId.systemDefault());
    public final static DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

    public static String sanitizeFileNames(String fileName) {
        return fileName.replaceAll("[\\<\\>\\:\\\"\\/\\|\\?\\*\\\\ ]", "_");
    }

    public static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
