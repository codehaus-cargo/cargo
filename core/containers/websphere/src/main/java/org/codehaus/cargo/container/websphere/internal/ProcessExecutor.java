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
package org.codehaus.cargo.container.websphere.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.log.Logger;

/**
 * Execute shell command as process.
 */
public class ProcessExecutor
{
    /**
     * Logger.
     */
    private Logger logger;

    /**
     * @param logger Logger.
     */
    public ProcessExecutor(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * Execute command and wait for process to end.
     * 
     * @param command Command and arguments to be executed.
     */
    public void executeAndWait(List<String> command)
    {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Process process = null;
        int result = 0;
        try
        {
            process = new ProcessBuilder(command).start();

            Future<?> osFuture = executorService.submit(
                new ProcessOutputReader(process.getInputStream()));
            Future<?> esFuture = executorService.submit(
                new ProcessOutputReader(process.getErrorStream()));

            result = process.waitFor();

            osFuture.get();
            esFuture.get();
        }
        catch (Exception e)
        {
            throw new CargoException("Error invoking command!", e);
        }
        finally
        {
            executorService.shutdown();
            if (result != 0)
            {
                throw new CargoException("Execution of command returned " + result);
            }
        }
    }

    /**
     * Reads program's output to specified stream
     */
    private final class ProcessOutputReader implements Runnable
    {
        /**
         * Input stream from process.
         */
        private InputStream fromStream;

        /**
         * @param fromStream Input stream from process.
         */
        private ProcessOutputReader(InputStream fromStream)
        {
            this.fromStream = fromStream;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            try (InputStreamReader isr = new InputStreamReader(fromStream);
                BufferedReader br = new BufferedReader(isr))
            {
                String line = null;
                while ((line = br.readLine()) != null)
                {
                    logger.info(line, ProcessExecutor.class.getName());
                }
            }
            catch (IOException ioe)
            {
                throw new CargoException("Error reading from stream!", ioe);
            }
        }
    }
}
