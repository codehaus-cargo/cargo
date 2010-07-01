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
package org.codehaus.cargo.container.property;

/**
 * Gathers all properties related to Servlet/JSP containers.
 * 
 * @version $Id$
 */
public interface ServletPropertySet
{
    /**
     * Port on which the Servlet/JSP container is listening to.
     */
    String PORT = "cargo.servlet.port";

    /**
     * Allow defining users and map to roles. The format is
     * <code>name1:pwd1:role11,...,role1N|name2:pwd2:role21,...,role2N|...</code>.
     */
    String USERS = "cargo.servlet.users";
}
