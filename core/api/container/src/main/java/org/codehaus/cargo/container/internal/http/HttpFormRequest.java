/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.codehaus.cargo.container.internal.http.writer.MultipartFormWriter;
import org.codehaus.cargo.container.internal.http.writer.UrlEncodedFormWriter;

/**
 * HTTP request which posts a form represented as a child of {@link FormContentType}.
 */
public class HttpFormRequest extends HttpRequest
{

    /**
     * Form data to be sent by HTTP form request.
     */
    private FormContentType formData;

    /**
     * @param url URL to be called.
     * @param formData Form data to be sent by HTTP form request.
     * @see HttpRequest#HttpRequest(URL)
     */
    public HttpFormRequest(URL url, FormContentType formData)
    {
        super(url);
        this.formData = formData;
    }

    /**
     * @param url URL to be called.
     * @param formData Form data to be sent by HTTP form request.
     * @param timeout Request timeout.
     * @see HttpRequest#HttpRequest(URL, long)
     */
    public HttpFormRequest(URL url, FormContentType formData, long timeout)
    {
        super(url, timeout);
        this.formData = formData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeOutputStream(HttpURLConnection connection) throws IOException
    {
        connection.setRequestProperty("Content-Type", formData.getContentType());
        connection.setDoOutput(true);

        if (formData instanceof MultipartFormContentType)
        {
            MultipartFormContentType multipartFormData = (MultipartFormContentType) formData;
            connection.setChunkedStreamingMode(BUFFER_CHUNK_SIZE);

            try (MultipartFormWriter writer =
                new MultipartFormWriter(multipartFormData, connection.getOutputStream()))
            {
                for (Map.Entry<String, String> entry
                    : multipartFormData.getFormContents().entrySet())
                {
                    writer.writeField(entry.getKey(), entry.getValue());
                }

                for (Map.Entry<String, File> entry : multipartFormData.getFormFiles().entrySet())
                {
                    writer.writeFile(entry.getKey(), entry.getValue());
                }
            }
        }
        else
        {
            UrlEncodedFormWriter urlEncodedFormWriter = new UrlEncodedFormWriter();

            for (Map.Entry<String, String> entry : formData.getFormContents().entrySet())
            {
                urlEncodedFormWriter.addField(entry.getKey(), entry.getValue());
            }

            connection.setRequestProperty("Content-Length",
                String.valueOf(urlEncodedFormWriter.getLength()));
            urlEncodedFormWriter.write(connection.getOutputStream());
        }
    }
}
