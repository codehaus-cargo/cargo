/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.tools.daemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

/**
 * Represents a form writer that allows writing form fields that are url encoded.
 *
 */
public class UrlEncodedFormWriter
{
    /**
     * The default url encoding charset.
     */
    private final String charset = "ISO-8859-1";

    /**
     * The buffer that will contain the url encoded form data.
     */
    private final StringBuilder formData = new StringBuilder();

    /**
     * The output stream to write to.
     */
    private DataOutputStream out = null;

    /**
     * Constructs an UrlEncodedFormWriter.
     *
     */
    public UrlEncodedFormWriter()
    {
    }

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
            formData.append(URLEncoder.encode(value, charset));
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
        this.out = new DataOutputStream(os);

        out.writeBytes(formData.toString());
        out.flush();
        out.close();
    }

    /**
     * @return the length of the url encoded form data
     */
    public int getLength()
    {
        return formData.toString().length();
    }

}
