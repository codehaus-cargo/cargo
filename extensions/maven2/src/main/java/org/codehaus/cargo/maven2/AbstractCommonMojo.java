/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.maven2;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * Common MOJO providing an accessor to the Maven Project object (this is the object containing
 * all information contained in the POM on which the MOJO is called).
 *
 * @version $Id: $
 */
public abstract class AbstractCommonMojo extends AbstractMojo
{
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     * @see #getProject()  
     */
    private MavenProject project;

    /**
     * @return the Maven project object (this is the object containing all information contained
     *         in the POM on which the MOJO is called).
     */
    public MavenProject getProject()
    {
        return this.project;
    }
}
