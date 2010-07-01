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
package org.codehaus.cargo.container.jrun;

/**
 * Gathers all JRun properties.
 * 
 * @version $Id$
 */
public interface JRun4xPropertySet
{
    /**
     * The JRun server instance name.
     */
    String SERVER_NAME = "cargo.jrun.server.name";    
    
    /**
     * The default JRun server instance name.
     */
    String DEFAULT_SERVER_NAME = "default";
    
    /**
     * The default port.
     */
    String DEFAULT_PORT = "8100";
    
    /**
     * The JRun4 installation directory.
     */
    String JRUN_HOME = "cargo.jrun4x.home";
    
    /**
     * The JRun4 classpath.
     */
    String JRUN_CLASSPATH = "cargo.jrun4x.classpath";
}
