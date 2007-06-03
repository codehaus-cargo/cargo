/*
 * ========================================================================
 *
 * Copyright 2005-2007 Vincent Massol.
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
package org.codehaus.cargo.module.webapp;

/**
 * Represents a ejb-ref element of a web application descriptor.
 *
 * @version $Id $
 */
public class EjbRef
{
    /**
     * Constant representing an session ejb reference.
     */
    public static final String SESSION = "Session";
    /**
     * Constant representing an entity ejb reference.
     */
    public static final String ENTITY = "Entity";

    /**
     * Name of the references. For example ejb/MyEjb.
     */
    private String name;
    /**
     * Class name of the ejb interface.
     */
    private String ejbInterface;
    /**
     * Class name of the ejb home interface.
     */
    private String ejbHomeInterface;
    /**
     * Name of the referenced ejb.
     */
    private String ejbName;
    /**
     * Jndi name of the referenced ejb.
     */
    private String jndiName;
    /**
     * Indicates if the referenced ejb is a local ejb.
     */
    private boolean local = true;
    /**
     * Type of ejb. Possible value is {@link SESSION} or {@link ENTITY}.
     */
    private String type = SESSION;

    /**
     * Empty constructor.
     */
    public EjbRef()
    {
    }

    /**
     * Construct a new ejb-ref.
     *
     * @param name Name of the reference. For example ejb/MyEjb.
     * @param ejbInterface Class name of the ejb interface.
     * @param ejbHomeInterface Class name of the ejb home interface.
     */
    public EjbRef(String name, String ejbInterface, String ejbHomeInterface)
    {
        this.name = name;
        this.ejbInterface = ejbInterface;
        this.ejbHomeInterface = ejbHomeInterface;
    }

    /**
     * Returns the ejb home inteface.
     *
     * @return ejb home interface.
     */
    public String getEjbHomeInterface()
    {
        return ejbHomeInterface;
    }

    /**
     * Sets the ejb home interface.
     *
     * @param ejbHomeInterface the home interface of the ejb.
     */
    public void setEjbHomeInterface(String ejbHomeInterface)
    {
        this.ejbHomeInterface = ejbHomeInterface;
    }

    /**
     * Returns the ejb inteface.
     *
     * @return ejb interface.
     */
    public String getEjbInterface()
    {
        return ejbInterface;
    }

    /**
     * Sets the ejb interface.
     *
     * @param ejbInterface the interface of the ejb.
     */
    public void setEjbInterface(String ejbInterface)
    {
        this.ejbInterface = ejbInterface;
    }

    /**
     * Returns the ejb name.
     *
     * @return ejb name.
     */
    public String getEjbName()
    {
        return ejbName;
    }

    /**
     * Sets the ejb name. This will result in an ejb ref by ejb-link.
     *
     * @param ejbName the name of the ejb.
     */
    public void setEjbName(String ejbName)
    {
        this.ejbName = ejbName;
    }

    /**
     * Returns the jndi inteface.
     *
     * @return jndi interface.
     */
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * Sets the jndi name. This will result in an ejb ref by vendor specific mappings.
     *
     * @param jndiName jndi name of the referenced ejb.
     */
    public void setJndiName(String jndiName)
    {
        this.jndiName = jndiName;
    }

    /**
     * Indicates if the ejb referenced is a local ejb.
     *
     * @return true if the ejb referenced is a local ejb.
     */
    public boolean isLocal()
    {
        return local;
    }

    /**
     * Set if the referenced ejb is a local ejb.
     *
     * @param local true if the referenced ejb is local.
     */
    public void setLocal(boolean local)
    {
        this.local = local;
    }

    /**
     * Returns the name of the reference.
     *
     * @return name of the reference.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the reference.
     *
     * @param name name of the reference.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the type of reference.
     *
     * @see EjbRef.SESSION
     * @see EjbRef.ENTITY
     * @return type of reference.
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the type of the reference.
     *
     * @see EjbRef.SESSION
     * @see EjbRef.ENTITY
     * @param type type of reference.
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
