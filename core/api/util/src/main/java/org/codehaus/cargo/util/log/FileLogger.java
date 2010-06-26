/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.util.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.internal.log.AbstractLogger;

/**
 * File implementation which sends logged messages to a file.
 * 
 * @version $Id$
 */
public class FileLogger extends AbstractLogger
{
    /**
     * The OutputStream to log to.
     */
    private OutputStream output;

    /**
     * Date format used when logging to the file.
     */
    private final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * @param file the file to log to
     * @param append if true the file is appended to insted of being erased
     */
    public FileLogger(File file, boolean append)
    {
        try
        {
            FileOutputStream fileOutputStream = new FileOutputStream(file, append);
            this.output = new BufferedOutputStream(fileOutputStream);
        }
        catch (FileNotFoundException e)
        {
            throw new CargoException("Failed to create file [" + file + "]", e);
        }
    }

    /**
     * @param file the file to log to
     * @param append if true the file is appended to insted of being erased
     */
    public FileLogger(String file, boolean append)
    {
        this(new File(file), append);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLogger#doLog(LogLevel, String, String)
     */
    @Override
    protected void doLog(LogLevel level, String message, String category)
    {
        final String formattedCategory = category.length() > 20
            ? category.substring(category.length() - 20) : category;
        
        final String msg = "[" + this.format.format(new Date()) + "]"
            + "[" + level.getLevel() + "][" + formattedCategory + "] " + message + "\n";
        try
        {
            this.output.write(msg.getBytes());
            this.output.flush();
        }
        catch (IOException e)
        {
            throw new CargoException("Failed to write log message ["
                + msg + "]", e);
        }
    }
}
