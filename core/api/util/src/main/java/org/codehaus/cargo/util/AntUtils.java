/*
 * ========================================================================
 *
 * Copyright 2003-2008 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.util;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Set of common Ant utility methods.
 */
public class AntUtils
{
    /**
     * The factory for creating ant tasks.
     */
    private AntTaskFactory antTaskFactory;

    /**
     * Uses the {@link DefaultAntTaskFactory} class when creating Ant tasks.
     */
    public AntUtils()
    {
        this.antTaskFactory = new DefaultAntTaskFactory(createProject());
    }

    /**
     * @param factory Ant task factory class used when creating Ant tasks
     */
    public AntUtils(AntTaskFactory factory)
    {
        this.antTaskFactory = factory;
    }

    /**
     * Creates and returns a new instance of the Ant task mapped to the specified logical name.
     * 
     * @param taskName The logical name of the task to create
     * @return A new instance of the task
     */
    public Task createAntTask(String taskName)
    {
        return this.antTaskFactory.createTask(taskName);
    }

    /**
     * @return a default empty Ant {@link org.apache.tools.ant.Project }
     */
    public Project createProject()
    {
        Project defaultProject = new Project();
        defaultProject.init();

        return defaultProject;
    }

}
