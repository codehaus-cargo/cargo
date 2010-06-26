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
package org.codehaus.cargo.container.internal;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;

/**
 * Executor that executes an Ant's {@link Java} command in a separate thread.
 * 
 * @version $Id$
 */
public class AntContainerExecutorThread extends Thread
{
    /**
     * The Ant java command to execute.
     */
    private Java java;
    
    /** Build exception. */
    private AtomicReference ex = new AtomicReference();
    
    /** Finished flag. */
    private AtomicBoolean finishedFlag = new AtomicBoolean(false);

    /**
     * @param java the Ant java command to execute
     */
    public AntContainerExecutorThread(Java java)
    {
        this.java = java;
    }

    /**
     * Returns a build exception.
     * @return the build exception
     */
    public BuildException getBuildException()
    {
        return (BuildException) this.ex.get();
    }

    /**
     * Set the build exception.
     * @param ex The build exception
     */
    private void setBuildException(BuildException ex)
    {
        this.ex.set(ex);
    }

    /**
     * Determine if its finished or not
     * @return If its finished or not
     */
    public boolean isFinished()
    {
        return finishedFlag.get();
    }

    /**
     * Set if the its finished or not
     * @param b sets finish state
     */
    public void setFinished(boolean b)
    {
        finishedFlag.set(b);
    }

    /**
     * Execute the Ant's java command.
     */
    @Override
    public void run()
    {        
        // This makes Ant Java task to throw an exception when something goes
        // wrong.
        this.java.setFailonerror(true);

        try
        {
            // Blocking call
            this.java.execute();
        }
        catch (BuildException ex)
        {
            if (ex.getMessage().contains("Java returned: 1"))
            {
                ex = new BuildException(ex.getMessage()
                    + "  See Cargo log for details.", ex.getCause(), ex.getLocation());
            }
            this.setBuildException(ex);
        }
        finally
        {
            this.setFinished(true);
        }

        // Only reach here when the container is stopped
    }
}
