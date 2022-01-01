/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
 */
public class AntContainerExecutorThread extends Thread
{
    /**
     * The Ant java command to execute.
     */
    private Java java;

    /**
     * {@code true} to launch the JVM in spawn - separate thread independent of the initial thread
     */
    private boolean spawn;

    /**
     * Build exception.
     */
    private AtomicReference ex = new AtomicReference();

    /**
     * Finished flag.
     */
    private AtomicBoolean finishedFlag = new AtomicBoolean(false);

    /**
     * @param java the Ant java command to execute
     * @param spawn {@code true} to launch JVM in spawn, {@code false} to launch normal JVM.
     */
    public AntContainerExecutorThread(Java java, boolean spawn)
    {
        this.java = java;
        this.spawn = spawn;
    }

    /**
     * Returns a build exception.
     * 
     * @return the build exception
     */
    public BuildException getBuildException()
    {
        return (BuildException) this.ex.get();
    }

    /**
     * Set the build exception.
     * 
     * @param ex The build exception
     */
    private void setBuildException(BuildException ex)
    {
        this.ex.set(ex);
    }

    /**
     * Determine if its finished or not
     * 
     * @return If its finished or not
     */
    public boolean isFinished()
    {
        return finishedFlag.get();
    }

    /**
     * Set if the its finished or not
     * 
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
        this.java.setSpawn(spawn);

        // if it's not spawn, allow exception to be throw
        //    (with spawn this option is not supported by Ant task)
        if (!spawn)
        {
            // This makes Ant Java task to throw an exception when something goes wrong.
            this.java.setFailonerror(true);
        }

        try
        {
            // Blocking call
            this.java.execute();
        }
        catch (BuildException e)
        {
            if (e.getMessage().contains("Java returned: 1"))
            {
                e = new BuildException(e.getMessage()
                    + "  See Cargo log for details.", e.getCause(), e.getLocation());
            }
            this.setBuildException(e);
        }
        finally
        {
            this.setFinished(true);
        }

        // Only reach here when the container is stopped
    }
}
