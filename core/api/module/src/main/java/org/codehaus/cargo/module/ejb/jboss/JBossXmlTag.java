package org.codehaus.cargo.module.ejb.jboss;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;

public class JBossXmlTag extends DescriptorTag
{
    public static final String EJB_NAME = "ejb-name";
    public static final String JNDI_NAME = "jndi-name";
    public static final String LOCAL_JNDI_NAME = "local-jndi-name";

    /**
     * Constructor.
     *
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected JBossXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     *
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected JBossXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }
    
}
