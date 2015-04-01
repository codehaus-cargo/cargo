/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Default {@link AntTaskFactory} for creating Ant tasks.
 * 
 */
public class DefaultAntTaskFactory implements AntTaskFactory
{
    /**
     * The current {@link org.apache.tools.ant.Project} being executed.
     */
    private Project currentProject;

    /**
     * The current Ant task being executed.
     */
    private String currentTaskName;

    /**
     * The current {@link org.apache.tools.ant.Location} of the Task being executed.
     */
    private Location currentLocation;

    /**
     * The current {@link org.apache.tools.ant.Target} being executed.
     */
    private Target currentOwningTarget;

    /**
     * Constructor using default values for the current task name, current location and current
     * target.
     * 
     * @param project the Ant project used when creating Ant tasks
     */
    public DefaultAntTaskFactory(Project project)
    {
        this.currentProject = project;
        this.currentTaskName = "cargo";
        this.currentLocation = new Location("in cargo code");
        this.currentOwningTarget = new Target();
    }

    /**
     * @param project the Ant project used when creating Ant tasks
     * @param currentTaskName the current Ant task being executed
     * @param currentLocation the current {@link org.apache.tools.ant.Location} of the Task being
     * executed.
     * @param currentTarget the current {@link org.apache.tools.ant.Target} being executed
     */
    public DefaultAntTaskFactory(Project project, String currentTaskName, Location currentLocation,
        Target currentTarget)
    {
        this.currentProject = project;
        this.currentTaskName = currentTaskName;
        this.currentLocation = currentLocation;
        this.currentOwningTarget = currentTarget;
    }

    /**
     * {@inheritDoc}
     * @see AntTaskFactory#createTask(String)
     */
    public Task createTask(String theName)
    {
        ifSshLoadTaskDef(theName);
        Task retVal = this.currentProject.createTask(theName);
        if (retVal != null)
        {
            retVal.setTaskName(this.currentTaskName);
            retVal.setLocation(this.currentLocation);
            retVal.setOwningTarget(this.currentOwningTarget);
        }
        return retVal;
    }

    /**
     * 
     * Checks to ensure the ssh task is present before proceeding.
     * 
     * @param taskName name of the task
     */
    private void ifSshLoadTaskDef(String taskName)
    {
        if (taskName.equals("sshjava"))
        {
            if (!currentProject.getTaskDefinitions().containsKey("sshjava"))
            {
                try
                {
                    currentProject.addTaskDefinition("sshjava",
                        Class.forName("org.jclouds.tools.ant.taskdefs.sshjava.SSHJava"));
                }
                catch (BuildException e)
                {
                    throw new CargoException("Error adding sshjava task", e);
                }
                catch (ClassNotFoundException e)
                {
                    throw new CargoException(
                        "Please ensure jclouds-antcontrib is in the classpath", e);
                }
            }
        }
    }
}
