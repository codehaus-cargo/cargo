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
package org.codehaus.cargo.container.internal.http.request;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.codehaus.cargo.container.internal.http.HttpConnection;
import org.codehaus.cargo.container.internal.http.HttpResult;

/**
 * Set of common HTTP(S) utility methods.
 */
public class HttpFormRequest extends HttpConnection
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
     * {@inheritDoc}
     * @see HttpConnection#HttpConnection(java.net.URL)
     */
    public HttpFormRequest(URL url)
    {
        super(url);
    }

    /**
     * {@inheritDoc}
     * @see HttpConnection#HttpConnection(java.net.URL, long)
     */
    public HttpFormRequest(URL url, long timeout)
    {
        super(url, timeout);
    }

    /**
     * Execute form request.
     * 
     * @param formData Form data to be sent by HTTP form request.
     * @param file File to be send as part of form request.
     * @return HTTP result.
     */
    public HttpResult execute(final String formData, final String file)
    {
        addRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY_VALUE);

        setRequestBody((OutputStream outputStream) ->
        {
            try (BufferedWriter httpRequestBodyWriter =
                new BufferedWriter(new OutputStreamWriter(outputStream)))
            {
                httpRequestBodyWriter.write(CRLF + BOUNDARY_LEFT + CRLF);
                httpRequestBodyWriter.write(formData);
                httpRequestBodyWriter.write(CRLF + "Content-Type: application/octet-stream"
                    + CRLF + CRLF);
                httpRequestBodyWriter.flush();

                writeFileToOutputStream(file, outputStream);

                // Mark the end of the multipart http request
                httpRequestBodyWriter.write(CRLF + BOUNDARY_BOTH + CRLF);
                httpRequestBodyWriter.flush();
            }
            outputStream.close();
        });

        return post();
    }

    /**
     * @param file File to be written to output stream.
     * @param outputStream Output stream.
     * @throws IOException If anything goes wrong.
     */
    private void writeFileToOutputStream(String file, OutputStream outputStream) throws IOException
    {
        // Write the actual file contents
        try (FileInputStream input = new FileInputStream(file))
        {
            int bytesRead;
            byte[] dataBuffer = new byte[1024];
            while ((bytesRead = input.read(dataBuffer)) != -1)
            {
                outputStream.write(dataBuffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
}
