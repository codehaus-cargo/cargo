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
    public static final JBossWebXmlTag CONTEXT_ROOT =
        new JBossWebXmlTag("context-root", false);

    public static final JBossWebXmlTag EJB_REF = new JBossWebXmlTag("ejb-ref");

    public static final JBossWebXmlTag EJB_LOCAL_REF = new JBossWebXmlTag("ejb-local-ref");

    public static final JBossWebXmlTag EJB_REF_NAME = new JBossWebXmlTag("ejb-ref-name");

    public static final JBossWebXmlTag JNDI_NAME = new JBossWebXmlTag("jndi-name");

    public static final JBossWebXmlTag LOCAL_JNDI_NAME = new JBossWebXmlTag("local-jndi-name");

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     * @param isMultipleAllowed Whether the element may occur multiple times in
     *         the descriptor
     */
    protected JBossWebXmlTag(String tagName, boolean isMultipleAllowed)
    {
        super(tagName, isMultipleAllowed);
    }

    /**
     * Constructor.
     *
     * @param tagName The tag name of the element
     */
    protected JBossWebXmlTag(String tagName)
    {
        this(tagName, true);
    }
}
