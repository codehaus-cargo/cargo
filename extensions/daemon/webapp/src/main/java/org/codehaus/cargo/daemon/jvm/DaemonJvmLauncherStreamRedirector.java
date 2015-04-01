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
package org.codehaus.cargo.daemon.jvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Redirects the output of a process into an OutputStream by periodically pumping data.
 *
 */
class DaemonJvmLauncherStreamRedirector implements Runnable
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
     * Creates a new redirector.
     *
     * @param is the input stream
     * @param os the output stream
     */
    public DaemonJvmLauncherStreamRedirector(InputStream is, OutputStream os)
    {
        inputStream = is;
        outputStream = os;
    }

    /**
     * {@inheritDoc}
     */
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
        }
        catch (Exception e)
        {
            return;
        }
        finally
        {
            try
            {
                PrintWriter writer = new PrintWriter(outputStream);
                writer.println();
                writer.println("--- LOG END ---");
                writer.flush();
                inputStream.close();
            }
            catch (IOException e)
            {
                return;
            }
        }
    }
}
