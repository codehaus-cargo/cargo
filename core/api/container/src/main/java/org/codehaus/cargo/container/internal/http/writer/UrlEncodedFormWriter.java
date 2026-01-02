/*
 * ========================================================================
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
package org.codehaus.cargo.container.internal.http.writer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Represents a form writer that allows writing form fields that are URL encoded.
 */
public class UrlEncodedFormWriter
{
    /**
     * The buffer that will contain the url encoded form data.
     */
    private final StringBuilder formData = new StringBuilder();

    /**
     * Writes a string field value.
     * 
     * @param name the field name (required)
     * @param value the field value
     * @throws IOException on input/output errors
     */
    public void addField(String name, String value) throws IOException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }

        if (formData.length() > 0)
        {
            formData.append("&");
        }

        formData.append(name + "=");
        if (value != null)
        {
            // TODO: URLEncoder.encode(String, Charset) was introduced in Java 10,
            //       simplify the below code when Codehaus Cargo is on Java 10+
            formData.append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
        }
    }

    /**
     * Writes the form data to the output stream.
     * 
     * @param os The output stream
     * @throws IOException on input/output errors
     */
    public void write(OutputStream os) throws IOException
    {
        if (os == null)
        {
            throw new IllegalArgumentException("Output stream is required.");
        }
        try (DataOutputStream out = new DataOutputStream(os))
        {
            out.writeBytes(formData.toString());
            out.flush();
        }
    }

    /**
     * @return the length of the url encoded form data
     */
    public int getLength()
    {
        return formData.length();
    }

}
