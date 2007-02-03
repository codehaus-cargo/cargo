/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.util;

import org.apache.tools.ant.Task;

/**
 * Interface that allows random classes to use Ant tasks without needing an
 * explicit reference to a project, target or task.
 *
 * @version $Id$
 */
public interface AntTaskFactory
{
    /**
     * Returns the task that is mapped to the specified name.
     *
     * Implementations of this interface should correctly initialize the task by
     * setting the name, the project and optionally the owning target.
     *
     * @param taskName The logical name of the task
     * @return A new instance of the task mapped to the name, or
     *         <code>null</code> if a corresponding task could not be created
     */
    Task createTask(String taskName);
}
