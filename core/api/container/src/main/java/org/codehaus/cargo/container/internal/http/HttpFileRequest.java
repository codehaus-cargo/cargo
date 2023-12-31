/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP request which puts the content of a file as output.
 */
public class HttpFileRequest extends HttpRequest
{
    /**
     * File to be put as output.
     */
    private File file;

    /**
     * @param url URL to be called.
     * @param file File to be put as output.
     * @see HttpRequest#HttpRequest(java.net.URL)
     */
    public HttpFileRequest(URL url, File file)
    {
        super(url);
        this.file = file;
    }

    /**
     * @param url URL to be called.
     * @param file File to be put as output.
     * @param timeout Request timeout.
     * @see HttpRequest#HttpRequest(java.net.URL, long)
     */
    public HttpFileRequest(URL url, File file, long timeout)
    {
        super(url, timeout);
        this.file = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeOutputStream(HttpURLConnection connection) throws IOException
    {
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(BUFFER_CHUNK_SIZE);

        try (InputStream fileInputStream = new FileInputStream(this.file);
            OutputStream outputStream = connection.getOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream))
        {
            int n;
            byte[] bytes = new byte[BUFFER_CHUNK_SIZE];
            while ((n = fileInputStream.read(bytes)) != -1)
            {
                outputStream.write(bytes, 0, n);
            }
            outputStream.flush();
        }
    }
}
