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

    /**
     * @param java the Ant java command to execute
     */
    public AntContainerExecutorThread(Java java)
    {
        this.java = java;
    }

    /**
     * Execute the Ant's java command.
     */
    public void run()
    {
        // Blocking call
        this.java.execute();
        
        // Only reach here when the container is stopped
    }   
}
