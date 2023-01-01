/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.ant;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.codehaus.cargo.util.internal.log.AbstractLogger;
import org.codehaus.cargo.util.log.LogLevel;

/**
 * Logger implementation that logs to Ant.
 */
public class AntLogger extends AbstractLogger
{
    /**
     * The Ant project.
     */
    private Project project;

    /**
     * The current target, or <code>null</code> if used outside of a target.
     */
    private Target target;

    /**
     * The task, or <code>null</code> if not used by a task.
     */
    private Task task;

    /**
     * Maps between Cargo log levels and Ant's log levels. Index is Cargo's log level and value is
     * Ant's log level.
     */
    private Map<LogLevel, Integer> levelMapper;

    /**
     * Constructor.
     * 
     * @param task The Ant task
     */
    public AntLogger(Task task)
    {
        this.project = task.getProject();
        this.task = task;
        initialize();
    }

    /**
     * Constructor.
     * 
     * @param target The current target
     */
    public AntLogger(Target target)
    {
        this.project = target.getProject();
        this.target = target;
        initialize();
    }

    /**
     * Constructor.
     * 
     * @param project The Ant project
     */
    public AntLogger(Project project)
    {
        this.project = project;
        initialize();
    }

    /**
     * Initialize log level mapping between Ant and Cargo.
     */
    private void initialize()
    {
        this.levelMapper = new HashMap<LogLevel, Integer>();
        this.levelMapper.put(LogLevel.DEBUG, Project.MSG_DEBUG);
        this.levelMapper.put(LogLevel.WARN, Project.MSG_WARN);
        this.levelMapper.put(LogLevel.INFO, Project.MSG_INFO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doLog(LogLevel level, String message, String category)
    {
        Integer antLogLevel = this.levelMapper.get(level);

        if (antLogLevel == null)
        {
            throw new IllegalStateException(
                "No Ant log level for Codehaus Cargo LogLevel " + level);
        }

        if (this.task != null)
        {
            this.project.log(this.task, message, antLogLevel);
        }
        else if (this.target != null)
        {
            this.project.log(this.target, message, antLogLevel);
        }
        else
        {
            this.project.log(message, antLogLevel);
        }
    }
}
