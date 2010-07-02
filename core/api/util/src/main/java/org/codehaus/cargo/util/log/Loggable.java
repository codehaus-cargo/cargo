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
package org.codehaus.cargo.util.log;

/**
 * All classes that want to perform logging must implement this interface.
 *  
 * @version $Id$
 */
public interface Loggable
{
    /**
     * @param logger the log implementation to use for logging
     */
    void setLogger(Logger logger);
    
    /**
     * @return the logger used
     */
    Logger getLogger();
}
