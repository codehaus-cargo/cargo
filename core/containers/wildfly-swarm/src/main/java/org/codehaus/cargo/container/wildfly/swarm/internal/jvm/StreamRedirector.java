/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm.internal.jvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Redirects the output of a process into an OutputStream by periodically pumping data.
 */
class StreamRedirector implements Runnable
{
    /**
     * The size of the buffer that will contain the output data of the process.
     */
    private static final int BUFFERSIZE = 4096;

    /**
     * The input stream of the process
     */
    private final InputStream inputStream;

    /**
     * The output stream to redirect to.
     */
    private final OutputStream outputStream;

    /**
     * Exception that might occur in redirecting the stream.
     */
    private Exception error;

    /**
     * Creates a new redirector.
     * 
     * @param is the input stream
     * @param os the output stream
     */
    public StreamRedirector(InputStream is, OutputStream os)
    {
        inputStream = is;
        outputStream = os;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        final byte[] buf = new byte[BUFFERSIZE];

        int length;
        try
        {
            while ((length = inputStream.read(buf)) > 0)
            {
                outputStream.write(buf, 0, length);
            }
            outputStream.flush();
        }
        catch (Exception e)
        {
            this.error = e;
            return;
        }
    }

    /**
     * Closes the associated input stream.
     * @throws IOException when input stream cannot be closed.
     */
    public void close() throws IOException
    {
        inputStream.close();
    }

    /**
     * Getter for error.
     * @return error instance if any exception occured; null otherwise.
     */
    public Exception getError()
    {
        return error;
    }

    /**
     * Tells whether any error occurred.
     * @return true in case error occurred, false otherwise.
     */
    public boolean hasError()
    {
        return getError() != null;
    }
}
