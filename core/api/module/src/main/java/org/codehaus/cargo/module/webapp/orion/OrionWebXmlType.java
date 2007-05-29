/**
 * 
 */
package org.codehaus.cargo.module.webapp.orion;

import org.codehaus.cargo.module.AbstractDescriptorType;
import org.codehaus.cargo.module.Dtd;

/**
 * @version $Id: $
 *
 */
public class OrionWebXmlType extends AbstractDescriptorType
{
    /**
     * Single instance.
     */
    private static OrionWebXmlType instance = new OrionWebXmlType();
  
    /**
     * Protected Constructor.
     */
    protected OrionWebXmlType()
    {
        super(null, OrionWebXml.class, new Dtd(
            "http://www.oracle.com/technology/ias/dtds/orion-web-9_04.dtd"));
        setDescriptorIo(new OrionWebXmlIo(this));
    }
    /**
     * Get Static Singleton instance.
     * @return OrionWebXmlType
     */
    public static OrionWebXmlType getInstance()
    {
        return instance;
    }
  
  
}