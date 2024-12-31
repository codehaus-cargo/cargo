/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.ant;

/**
 * Holds configuration data for the <code>&lt;role&gt;</code> tag used to configure the ANT plugin
 */
public class RoleElement
{
    /**
     * Role name.
     */
    private String name;

    /**
     * @return User name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name User name.
     */
    public void setName(String name)
    {
        this.name = name;
    }
}
