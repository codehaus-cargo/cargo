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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a form writer capable of sending files and form data as multipart chunks.
 *
 * @version $Id$
 */
public class MultipartFormWriter
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
     * @param mimeType the file content type (optional, recommended)
     * @param file the file (the file must exist)
     * @throws IOException on input/output errors
     */
    public void writeFile(String name, String mimeType, File file) throws IOException
    {
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
        writeFile(name, mimeType, file.getCanonicalPath(), new FileInputStream(file));
    }

    /**
     * Writes a input stream's contents.
     *
     * @param name The field name
     * @param mimeType The file content type (optional, recommended)
     * @param fileName The file name (required)
     * @param is The input stream
     * @throws IOException on input/output errors
     */
    public void writeFile(String name, String mimeType, String fileName, InputStream is)
        throws IOException
    {
        if (is == null)
        {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }
        if (fileName == null || fileName.length() == 0)
        {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        // write boundary
        out.writeBytes(PREFIX);
        out.writeBytes(boundary);
        out.writeBytes(NEWLINE);
        // write content header
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
            + fileName + "\"");
        out.writeBytes(NEWLINE);
        if (mimeType != null)
        {
            out.writeBytes("Content-Type: " + mimeType);
            out.writeBytes(NEWLINE);
        }
        out.writeBytes(NEWLINE);
        // write content
        byte[] data = new byte[1024];
        int r = 0;
        while ((r = is.read(data, 0, data.length)) != -1)
        {
            out.write(data, 0, r);
        }
        // close input stream, but ignore any possible exception for it
        is.close();
        out.writeBytes(NEWLINE);
        out.flush();
    }

    /**
     * Writes the given bytes.
     *
     * @param name the field name
     * @param mimeType the file content type (optional, recommended)
     * @param fileName the file name (required)
     * @param data the file data
     * @throws IOException on input/output errors
     */
    public void writeFile(String name, String mimeType, String fileName, byte[] data)
        throws IOException
    {
        if (data == null)
        {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (fileName == null || fileName.length() == 0)
        {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }
        /*
         * --boundary\r\n Content-Disposition: form-data; name="<fieldName>";
         * filename="<filename>"\r\n Content-Type: <mime-type>\r\n \r\n <file-data>\r\n
         */
        // write boundary
        out.writeBytes(PREFIX);
        out.writeBytes(boundary);
        out.writeBytes(NEWLINE);
        // write content header
        out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\""
            + fileName + "\"");
        out.writeBytes(NEWLINE);
        if (mimeType != null)
        {
            out.writeBytes("Content-Type: " + mimeType);
            out.writeBytes(NEWLINE);
        }
        out.writeBytes(NEWLINE);
        // write content
        out.write(data, 0, data.length);
        out.writeBytes(NEWLINE);
        out.flush();
    }

    /**
     * Closes the writer.
     *
     * This method must be called.
     *
     * @throws IOException on input/output errors
     */
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

//    public static URLConnection createConnection(URL url) throws IOException
//    {
//        URLConnection urlConn = url.openConnection();
//        if (urlConn instanceof HttpURLConnection)
//        {
//            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
//            httpConn.setRequestMethod("POST");
//        }
//        urlConn.setDoInput(true);
//        urlConn.setDoOutput(true);
//        urlConn.setUseCaches(false);
//        urlConn.setDefaultUseCaches(false);
//        return urlConn;
//    }

}
