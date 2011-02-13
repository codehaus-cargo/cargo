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
package org.codehaus.cargo.container.internal;

import java.util.Map;

/**
 * All methods that a container that is spawned must implement.
 *
 * @version $Id$
 */
public interface SpawnedContainer
{
    /**
     * @param classpath the extra classpath that is added to the container's classpath when it is
     *        started.
     */
    void setExtraClasspath(String[] classpath);

    /**
     * @param location the extra classpath that is added to the container's classpath when it is
     *        started.
     */
    void addExtraClasspath(String location);
    
    /**
     * @return the extra classpath that is added to the container's classpath when it is started.
     */
    String[] getExtraClasspath();

    /**
     * @param classpath the shared classpath that is shared by the container applications.
     */
    void setSharedClasspath(String[] classpath);

    /**
     * @param location the shared classpath that is added to the container's classpath when it is
     *            started.
     */
    void addSharedClasspath(String location);
    
    /**
     * @return the extra classpath that is shared by the container applications.
     */
    String[] getSharedClasspath();

    /**
     * @param properties the System properties to set in the container executing VM.
     */
    void setSystemProperties(Map<String, String> properties);

    /**
     * @return the System properties to set in the container executing VM.
     */
    Map<String, String> getSystemProperties();

}
