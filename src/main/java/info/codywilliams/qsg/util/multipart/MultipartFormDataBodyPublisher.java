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


import java.io.InputStream;
import java.math.BigInteger;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Flow.Subscriber;
import java.util.function.Supplier;

/**
 * multipart/form-data BodyPublisher.
 */
public class MultipartFormDataBodyPublisher implements BodyPublisher {
    private static String nextBoundary() {
        var random = new BigInteger(128, new Random());
        try (var formatter = new Formatter()) {
            return formatter.format("-----------------------------%039d", random).toString();
        }
    }

    private final String boundary = nextBoundary();
    private final List<Part> parts = new ArrayList<>();
    private Charset charset;
    private final BodyPublisher delegate = BodyPublishers.ofInputStream(
            () -> Channels.newInputStream(new MultipartFormDataChannel(this.boundary, this.parts, this.charset)));

    /**
     * Construct {@link MultipartFormDataBodyPublisher}
     */
    public MultipartFormDataBodyPublisher() {
        this(StandardCharsets.UTF_8);
    }

    /**
     * Construct {@link MultipartFormDataBodyPublisher}
     *
     * @param charset
     *            character encoding
     */
    public MultipartFormDataBodyPublisher(Charset charset) {
        this.charset = charset;
    }

    private MultipartFormDataBodyPublisher add(Part part) {
        this.parts.add(part);
        return this;
    }

    /**
     * Add part.
     *
     * @param name
     *            field name
     * @param value
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher add(String name, String value) {
        return this.add(new StringPart(name, value, this.charset));
    }

    /**
     * Add part. Content using specified path.
     *
     * @param name
     *            field name
     * @param path
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher addFile(String name, Path path) {
        return this.add(new FilePart(name, path));
    }

    /**
     * Add part. Content using specified path.
     *
     * @param name
     *            field name
     * @param path
     *            field value
     * @param contentType
     *            Content-Type
     * @return this
     */
    public MultipartFormDataBodyPublisher addFile(String name, Path path, String contentType) {
        return this.add(new FilePart(name, path, contentType));
    }

    /**
     * Add part with {@link InputStream}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher addStream(String name, String filename, Supplier<InputStream> supplier) {
        return this.add(new StreamPart(name, filename, () -> Channels.newChannel(supplier.get())));
    }

    /**
     * Add part with {@link InputStream}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @param contentType
     *            Content-Type
     * @return this
     */
    public MultipartFormDataBodyPublisher addStream(String name, String filename, Supplier<InputStream> supplier,
                                                    String contentType) {
        return this.add(new StreamPart(name, filename, () -> Channels.newChannel(supplier.get()), contentType));
    }

    /**
     * Add part with {@link ReadableByteChannel}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @return this
     */
    public MultipartFormDataBodyPublisher addChannel(String name, String filename,
                                                     Supplier<ReadableByteChannel> supplier) {
        return this.add(new StreamPart(name, filename, supplier));
    }

    /**
     * Add part with {@link ReadableByteChannel}
     *
     * @param name
     *            field name
     * @param filename
     *            file name
     * @param supplier
     *            field value
     * @param contentType
     *            Content-Type
     * @return this
     */
    public MultipartFormDataBodyPublisher addChannel(String name, String filename,
                                                     Supplier<ReadableByteChannel> supplier, String contentType) {
        return this.add(new StreamPart(name, filename, supplier, contentType));
    }

    /**
     * Get Content-Type
     *
     * @return Content-Type
     */
    public String contentType() {
        try (var formatter = new Formatter()) {
            return formatter.format("multipart/form-data; boundary=%s", this.boundary).toString();
        }
    }

    @Override
    public void subscribe(Subscriber<? super ByteBuffer> s) {
        delegate.subscribe(s);
    }

    @Override
    public long contentLength() {
        return delegate.contentLength();
    }

}

