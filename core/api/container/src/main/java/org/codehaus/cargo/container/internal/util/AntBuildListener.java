/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.internal.util;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.container.ContainerException;

/**
 * Ant build listener used to collect logs from Ant tasks and to redirect them to a
 * {@link Logger}.
 *  
 * @version $Id$
 */
public class AntBuildListener implements BuildListener
{
    /**
     * Logger to where to redirect Ant logs.
     */
    private Logger logger;

    /**
     * Category to log to. Usually this is name of the class being logged.
     */
    private String category;
    
    /**
     * @param logger the logger to which to log the Ant messages received
     * @param category the category to log to. Usually this is name of the class being logged
     */
    public AntBuildListener(Logger logger, String category)
    {
        this.logger = logger;
        this.category = category;
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
     */
    public void buildStarted(BuildEvent event)
    {
        // Voluntarily do nothing
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
     */
    public void buildFinished(BuildEvent event)
    {
        // Voluntarily do nothing
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
     */
    public void targetStarted(BuildEvent event)
    {
        // Voluntarily do nothing
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
     */
    public void targetFinished(BuildEvent event)
    {
        // Voluntarily do nothing
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
     */
    public void taskStarted(BuildEvent event)
    {
        // Voluntarily do nothing
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
     */
    public void taskFinished(BuildEvent event)
    {
        // Voluntarily do nothing
    }

    /**
     * {@inheritDoc}
     * @see BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
     */
    public void messageLogged(BuildEvent event)
    {
        if ((event.getPriority() == Project.MSG_DEBUG)
            || (event.getPriority() == Project.MSG_VERBOSE))
        {
            this.logger.debug(event.getMessage(), this.category);
        }
        else if (event.getPriority() == Project.MSG_INFO)
        {
            this.logger.info(event.getMessage(), this.category);
        }
        else if ((event.getPriority() == Project.MSG_WARN)
            || (event.getPriority() == Project.MSG_ERR))
        {
            this.logger.warn(event.getMessage(), this.category);
        }
        else
        {
            throw new ContainerException("Unhandled Ant logging priority [" + event.getPriority()
                + "]");
        }
    }
}
