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

package info.codywilliams.qsg.util.multipart;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FilePart implements Part {
    private final String name;
    private final Path path;
    private final String contentType;

    FilePart(String name, Path path, String contentType) {
        this.name = name;
        this.path = path;
        this.contentType = contentType;
    }

    FilePart(String name, Path path) {
        this(name, path, "application/octet-stream");
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Optional<String> filename() {
        return Optional.of(this.path.getFileName().toString());
    }

    @Override
    public Optional<String> contentType() {
        return Optional.of(this.contentType);
    }

    @Override
    public ReadableByteChannel open() throws IOException {
        return Files.newByteChannel(this.path);
    }
}
