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
package org.codehaus.cargo.container.spi.jvm;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.internal.util.AntBuildListener;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.log.Loggable;

/**
 * The default factory to create JVM launchers.
 * 
 * @version $Id$
 */
public class DefaultJvmLauncherFactory implements JvmLauncherFactory
{

    /**
     * Ant utility class.
     */
    private final AntUtils antUtils;

    /**
     * Creates a new JVM launcher factory.
     */
    public DefaultJvmLauncherFactory()
    {
        this.antUtils = new AntUtils();
    }

    /**
     * {@inheritDoc}
     */
    public JvmLauncher createJvmLauncher(JvmLauncherRequest request)
    {
        Loggable loggable = request.getLoggable();

        Java java = (Java) antUtils.createAntTask(request.isSsh() ? "sshjava" : "java");

        java.setFork(true);

        boolean foundBuildListener = false;
        for (Object listenerObject : java.getProject().getBuildListeners())
        {
            if (listenerObject instanceof AntBuildListener)
            {
                foundBuildListener = true;
                break;
            }
        }
        if (!foundBuildListener)
        {
            java.getProject().addBuildListener(
                new AntBuildListener(loggable.getLogger(), loggable.getClass().getName()));
        }

        return new DefaultJvmLauncher(java);
    }

}
