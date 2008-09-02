/*
 * ========================================================================
 *
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.jboss;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.DescriptorType;

/**
 * Represents the various top-level tags in a JBoss web deployment descriptor
 * as a typesafe enumeration.
 *
 * @version $Id$
 */
public final class JBossWebXmlTag extends DescriptorTag
{
    /**
     * Element name 'context-root'.
     */
    public static final String CONTEXT_ROOT = "context-root";

    /**
     * Element name 'ejb-ref'.
     */
    public static final String EJB_REF = "ejb-ref";

    /**
     * Element name 'ejb-local-ref'.
     */
    public static final String EJB_LOCAL_REF = "ejb-local-ref";

    /**
     * Element name 'ejb-ref-name'.
     */
    public static final String EJB_REF_NAME = "ejb-ref-name";

    /**
     * Element name 'jndi-name'.
     */
    public static final String JNDI_NAME = "jndi-name";

    /**
     * Element name 'local-jndi-name'.
     */
    public static final String LOCAL_JNDI_NAME = "local-jndi-name";

    /**
     * Constructor.
     *
     * @param type Descriptor type
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in
     *         the descriptor
     */
    protected JBossWebXmlTag(DescriptorType type, String tagName, boolean isMultipleAllowed)
    {
        super(type, tagName, null, isMultipleAllowed, null, null);
    }

    /**
     * Constructor.
     *
     * @param type Descriptor type
     * @param tagName The tag name of the element
     */
    protected JBossWebXmlTag(DescriptorType type, String tagName)
    {
        this(type, tagName, true);
    }
}
