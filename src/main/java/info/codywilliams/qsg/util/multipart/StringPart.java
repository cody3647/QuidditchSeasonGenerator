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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

public class StringPart implements Part {
    private final String name;
    private final String value;
    private final Charset charset;

    StringPart(String name, String value, Charset charset) {
        this.name = name;
        this.value = value;
        this.charset = charset;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ReadableByteChannel open() throws IOException {
        var input = new ByteArrayInputStream(this.value.getBytes(this.charset));
        return Channels.newChannel(input);
    }
}
