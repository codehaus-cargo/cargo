/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.cargo.container.internal.http.MultipartFormContentType;

/**
 * Represents a form writer capable of sending files and form data as multipart chunks.
 */
public class MultipartFormWriter implements AutoCloseable
{
    /**
     * The line end characters.
     */
    private static final String NEWLINE = "\r\n";

    /**
     * The boundary prefix.
     */
    private static final String PREFIX = "--";

    /**
     * The output stream to write to.
     */
    private DataOutputStream out = null;

    /**
     * The multipart boundary string.
     */
    private String boundary = null;

    /**
     * Attaches to the outputstream and allows writing form data or files to it.
     * 
     * @param contentType The form content type
     * @param os the output stream
     */
    public MultipartFormWriter(MultipartFormContentType contentType, OutputStream os)
    {
        if (os == null)
        {
            throw new IllegalArgumentException("Output stream is required.");
        }
        this.out = new DataOutputStream(os);
        this.boundary = contentType.getBoundary();
    }

    /**
     * Writes a string field value.
     * 
     * @param name the field name (required)
     * @param value the field value
     * @throws IOException on input/output errors
     */
    public void writeField(String name, String value) throws IOException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }

        // write boundary
        out.writeBytes(PREFIX);
        out.writeBytes(boundary);
        out.writeBytes(NEWLINE);
        // write content header
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"");
        out.writeBytes(NEWLINE);
        out.writeBytes(NEWLINE);
        // write content
        if (value != null)
        {
            out.writeBytes(value);
        }
        out.writeBytes(NEWLINE);
        out.flush();
    }

    /**
     * Writes a file's contents.
     * 
     * @param name the field name
     * @param file the file (the file must exist)
     * @throws IOException on input/output errors
     */
    public void writeFile(String name, File file) throws IOException
    {
        writeFile(name, file, "application/octet-stream");
    }

    /**
     * Writes a input stream's contents.
     * 
     * @param name the field name
     * @param file the file
     * @param mimeType The file content type
     * @throws IOException on input/output errors
     */
    public void writeFile(String name, File file, String mimeType) throws IOException
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (file == null)
        {
            throw new IllegalArgumentException("File cannot be null.");
        }
        if (!file.exists())
        {
            throw new IllegalArgumentException("File does not exist.");
        }
        if (file.isDirectory())
        {
            throw new IllegalArgumentException("File cannot be a directory.");
        }
        if (mimeType == null || mimeType.trim().isEmpty())
        {
            throw new IllegalArgumentException("File content type cannot be null");
        }
        try (InputStream fileInputStream = new FileInputStream(file))
        {
            // write boundary
            out.writeBytes(PREFIX);
            out.writeBytes(boundary);
            out.writeBytes(NEWLINE);
            // write content header
            out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
                + file.getName() + "\"");
            out.writeBytes(NEWLINE);
            out.writeBytes("Content-Type: " + mimeType);
            out.writeBytes(NEWLINE);
            out.writeBytes(NEWLINE);
            // write content
            byte[] data = new byte[1024];
            int r = 0;
            while ((r = fileInputStream.read(data, 0, data.length)) != -1)
            {
                out.write(data, 0, r);
            }
            out.writeBytes(NEWLINE);
            out.flush();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        // write final boundary
        out.writeBytes(PREFIX);
        out.writeBytes(boundary);
        out.writeBytes(PREFIX);
        out.writeBytes(NEWLINE);
        out.flush();
        out.close();
    }

}
