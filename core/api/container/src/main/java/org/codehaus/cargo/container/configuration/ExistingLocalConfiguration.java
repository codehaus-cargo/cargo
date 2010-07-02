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
package org.codehaus.cargo.container.configuration;

/**
 * An existing configuration allows you to point Cargo to a container configuration that you have
 * already set somewhere on your local file system. For example for JBoss this points to a JBoss
 * server configuration (usually a directory located in <code>JBOSSHOME/server</code>, such as
 * <code>JBOSSHOME/server/default</code> for example). The default configuration locations depend
 * on the container you're using. 
 *  
 * @version $Id$
 */
public interface ExistingLocalConfiguration extends LocalConfiguration
{
}
