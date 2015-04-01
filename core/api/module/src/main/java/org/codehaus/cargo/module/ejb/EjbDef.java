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
package org.codehaus.cargo.module.ejb;

/**
 * Class representing an EJB definition in a ejb-jar.xml descriptor.
 * 
 */
public class EjbDef
{
    /**
     * Id attribute of the EJB definition element.
     */
    private String id;

    /**
     * Value of the nested name tag.
     */
    private String name;

    /**
     * Value of the nested local tag.
     */
    private String local;

    /**
     * Value of the nested local-home tag.
     */
    private String localHome;

    /**
     * Constructor.
     */
    public EjbDef()
    {
    }

    /**
     * Constructor.
     * 
     * @param name name of the ejb
     */
    public EjbDef(String name)
    {
        this.name = name;
    }

    /**
     * Constructor.
     * 
     * @param name name of the ejb
     * @param id id of the ejb
     */
    public EjbDef(String name, String id)
    {
        this.name = name;
        this.id = id;
    }

    /**
     * Id accessor.
     * 
     * @return Returns the id.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Id setter.
     * 
     * @param id The id to set.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Name accessor.
     * 
     * @return Returns the name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Name setter.
     * 
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Local accessor.
     * 
     * @return Returns the local.
     */
    public String getLocal()
    {
        return this.local;
    }

    /**
     * Local setter.
     * 
     * @param local The local to set.
     */
    public void setLocal(String local)
    {
        this.local = local;
    }

    /**
     * LocalHome accessor.
     * 
     * @return Returns the localHome.
     */
    public String getLocalHome()
    {
        return this.localHome;
    }

    /**
     * LocalHome setter.
     * 
     * @param localHome The localHome to set.
     */
    public void setLocalHome(String localHome)
    {
        this.localHome = localHome;
    }
}
