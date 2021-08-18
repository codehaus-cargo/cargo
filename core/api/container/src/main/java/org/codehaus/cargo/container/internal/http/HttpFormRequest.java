/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.container.internal.http;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP request which posts a form.
 */
public class HttpFormRequest extends HttpFileRequest
{
    /**
     * Form data separator.
     */
    private static final String CRLF = "\r\n";

    /**
     * Hyphens for boundary delimiters.
     */
    private static final String HYPHENS = "--";

    /**
     * Boundary value for form content separator.
     */
    private static final String BOUNDARY_VALUE = "CargoBoundary";

    /**
     * Boundary for form content separator.
     * With hyphens in the left.
     */
    private static final String BOUNDARY_LEFT = HYPHENS + BOUNDARY_VALUE;

    /**
     * Boundary for form content separator.
     * With hyphens on both sides.
     */
    private static final String BOUNDARY_BOTH = HYPHENS + BOUNDARY_VALUE + HYPHENS;

    /**
     * Form data to be sent by HTTP form request.
     */
    private String formData;

    /**
     * @param url URL to be called.
     * @param formData Form data to be sent by HTTP form request.
     * @param file File to be put as output.
     * @see HttpFileRequest#HttpFileRequest(URL, java.io.File)
     */
    public HttpFormRequest(URL url, String formData, File file)
    {
        super(url, file);
        this.formData = formData;
    }

    /**
     * @param url URL to be called.
     * @param formData Form data to be sent by HTTP form request.
     * @param file File to be put as output.
     * @param timeout Request timeout.
     * @see HttpFileRequest#HttpFileRequest(URL, java.io.File, long)
     */
    public HttpFormRequest(URL url, String formData, File file, long timeout)
    {
        super(url, file, timeout);
        this.formData = formData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeOutputStream(HttpURLConnection connection) throws IOException
    {
        connection.setDoOutput(true);

        // When trying to upload large amount of data the internal connection buffer can
        // become too large and exceed the heap size, leading to a
        // java.lang.OutOfMemoryError.
        //
        // This was fixed in JDK 1.5 by introducing a new setChunkedStreamingMode() method.
        // As per CARGO-1418, use a sensible chunk size for fast links.
        connection.setChunkedStreamingMode(BUFFER_CHUNK_SIZE);

        try (OutputStream outputStream = connection.getOutputStream();
            BufferedWriter httpRequestBodyWriter =
                new BufferedWriter(new OutputStreamWriter(outputStream)))
        {
            httpRequestBodyWriter.write(CRLF + BOUNDARY_LEFT + CRLF);
            httpRequestBodyWriter.write(this.formData);
            httpRequestBodyWriter.write(
                CRLF + "Content-Type: application/octet-stream" + CRLF + CRLF);
            httpRequestBodyWriter.flush();

            writeFileToOutputStream(outputStream);

            // Mark the end of the multipart http request
            httpRequestBodyWriter.write(CRLF + BOUNDARY_BOTH + CRLF);
            httpRequestBodyWriter.flush();
        }
    }
}
