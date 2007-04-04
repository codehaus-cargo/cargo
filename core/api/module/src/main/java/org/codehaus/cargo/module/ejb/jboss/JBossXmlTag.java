package org.codehaus.cargo.module.ejb.jboss;

import org.codehaus.cargo.module.DescriptorTag;

public class JBossXmlTag extends DescriptorTag
{
    public static final DescriptorTag EJB_NAME = new JBossXmlTag("ejb-name");
    public static final DescriptorTag JNDI_NAME = new JBossXmlTag("jndi-name");
    public static final DescriptorTag LOCAL_JNDI_NAME = new JBossXmlTag("local-jndi-name");

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in the descriptor
     */
    protected JBossXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     */
    protected JBossXmlTag(String tagName)
    {
        this(tagName, true);
    }
}
