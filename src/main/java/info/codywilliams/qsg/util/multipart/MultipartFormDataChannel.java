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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Iterator;
import java.util.StringJoiner;

public class MultipartFormDataChannel implements ReadableByteChannel {
    private boolean closed = false;
    private State state = State.Boundary;
    private final String boundary;
    private final Iterator<Part> parts;
    private ByteBuffer buf = ByteBuffer.allocate(0);
    private Part current = null;
    private ReadableByteChannel channel = null;
    private final Charset charset;

    MultipartFormDataChannel(String boundary, Iterable<Part> parts, Charset charset) {
        this.boundary = boundary;
        this.parts = parts.iterator();
        this.charset = charset;
    }

    @Override
    public void close() throws IOException {
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
        this.closed = true;
    }

    @Override
    public boolean isOpen() {
        return !this.closed;
    }

    @Override
    public int read(ByteBuffer buf) throws IOException {
        while (true) {
            if (this.buf.hasRemaining()) {
                var n = Math.min(this.buf.remaining(), buf.remaining());
                var slice = this.buf.slice();
                slice.limit(n);
                buf.put(slice);
                this.buf.position(this.buf.position() + n);
                return n;
            }

            switch (this.state) {
                case Boundary:
                    if (this.parts.hasNext()) {
                        this.current = this.parts.next();
                        this.buf = ByteBuffer.wrap(("--" + this.boundary + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
                        this.state = State.Headers;
                    } else {
                        this.buf = ByteBuffer.wrap(("--" + this.boundary + "--\r\n").getBytes(StandardCharsets.ISO_8859_1));
                        this.state = State.Done;
                    }
                    break;

                case Headers:
                    this.buf = ByteBuffer.wrap(this.currentHeaders().getBytes(this.charset));
                    this.state = State.Body;
                    break;

                case Body:
                    if (this.channel == null) {
                        this.channel = this.current.open();
                    }

                    var n = this.channel.read(buf);
                    if (n == -1) {
                        this.channel.close();
                        this.channel = null;
                        this.buf = ByteBuffer.wrap("\r\n".getBytes(StandardCharsets.ISO_8859_1));
                        this.state = State.Boundary;
                    } else {
                        return n;
                    }
                    break;

                case Done:
                    return -1;
            }
        }
    }

    static String escape(String s) {
        return s.replaceAll("\"", "\\\"");
    }

    String currentHeaders() {
        var current = this.current;

        if (current == null) {
            throw new IllegalStateException();
        }

        var contentType = current.contentType();
        var filename = current.filename();
        if (contentType.isPresent() && filename.isPresent()) {
            var format = new StringJoiner("\r\n", "", "\r\n")
                    .add("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"").add("Content-Type: %s")
                    .toString();
            try (var formatter = new Formatter()) {
                return formatter
                        .format(format, escape(current.name()), escape(filename.get()), escape(contentType.get()))
                        .toString() + "\r\n"; // FIXME
            }

        } else if (contentType.isPresent()) {
            var format = new StringJoiner("\r\n", "", "\r\n").add("Content-Disposition: form-data; name=\"%s\"")
                    .add("Content-Type: %s").toString();
            try (var formatter = new Formatter()) {
                return formatter.format(format, escape(current.name()), escape(contentType.get())).toString() + "\r\n"; // FIXME
                // escape
            }

        } else if (filename.isPresent()) {
            var format = new StringJoiner("\r\n", "", "\r\n")
                    .add("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"").toString();
            try (var formatter = new Formatter()) {
                return formatter.format(format, escape(current.name()), escape(filename.get())).toString() + "\r\n"; // FIXME
                // escape
            }

        } else {
            var format = new StringJoiner("\r\n", "", "\r\n").add("Content-Disposition: form-data; name=\"%s\"")
                    .toString();
            try (var formatter = new Formatter()) {
                return formatter.format(format, escape(current.name())).toString() + "\r\n"; // FIXME escape
            }
        }
    }
}
