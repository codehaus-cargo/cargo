/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.resin;

import org.codehaus.cargo.module.internal.util.xml.AbstractNodeList;
import org.codehaus.cargo.module.merge.MergeElement;
import org.codehaus.cargo.module.merge.MergeNodeList;
import org.codehaus.cargo.module.merge.AbstractMergeSet;
import org.codehaus.cargo.module.merge.strategy.MergeStrategy;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Class to manage the merging of two resin web descriptors.
 * 
 * @version $Id $
 */
public class ResinWebXmlMerger extends LoggedObject
{
    /**
     * Strategies for merging directory servlets.
     */
    public MergeStrategy mergeDirectoryServletStrategy = MergeStrategy.OVERWRITE;

    /**
     * Strategies for merging session config.
     */
    public MergeStrategy mergeSessionConfigStrategy = MergeStrategy.OVERWRITE;

    /**
     * Strategies for merging resource references.
     */
    public MergeStrategy mergeResourceRefsStrategy = MergeStrategy.OVERWRITE;

    /**
     * Strategies for merging jndi links.
     */
    public MergeStrategy mergeJndiLinksStrategy = MergeStrategy.OVERWRITE;

    /**
     * Strategies for merging system properties.
     */
    public MergeStrategy mergeSystemPropertiesStrategy = MergeStrategy.OVERWRITE;
  
    /**
     * The original, authorative descriptor onto which the merges are performed.
     */
    private ResinWebXml webXml;      
    
    /**
     * Constructor.
     * 
     * @param theWebXml The original descriptor
     */
    public ResinWebXmlMerger(ResinWebXml theWebXml)
    {
        if (theWebXml == null)
        {
            throw new IllegalArgumentException("Must pass a resin web xml");
        }
        this.webXml = theWebXml;
    }

    /**
     * Merges the merge descriptor with the original descriptor.
     * 
     * @param theMergeWebXml The descriptor to merge in
     */
    public final void merge(ResinWebXml theMergeWebXml)
    {
        if (theMergeWebXml == null)
        {
            throw new IllegalArgumentException("Must pass a resin web xml");
        }

        mergeSystemProperties(theMergeWebXml);
        mergeResourceRefs(theMergeWebXml);
        mergeJndiLinks(theMergeWebXml);
        mergeSessionConfig(theMergeWebXml);
        mergeDirectoryServlet(theMergeWebXml);
    }

    /**
     * Merge directory-servlet definitions.
     * 
     * @param theWebXml the web xml to merge
     */
    private void mergeDirectoryServlet(ResinWebXml theWebXml)
    {
        AbstractMergeSet merger = new MergeElement(this.webXml.getRootElement(), this.webXml
            .getDirectoryServlet(), theWebXml.getDirectoryServlet());

        int count = merger.merge(this.mergeDirectoryServletStrategy);

        getLogger().debug(
            "Merged " + count + " directory-servlet definition" + (count != 1 ? "s " : " ")
                + "into the descriptor", this.getClass().getName());

    }

    /**
     * Merge session-config definitions.
     * 
     * @param theWebXml the web xml to merge
     */
    private void mergeSessionConfig(ResinWebXml theWebXml)
    {
        AbstractMergeSet merger = new MergeElement(this.webXml.getRootElement(), this.webXml
            .getSessionConfig(), theWebXml.getSessionConfig());

        int count = merger.merge(this.mergeSessionConfigStrategy);

        getLogger().debug(
            "Merged " + count + " session-config definition" + (count != 1 ? "s " : " ")
                + "into the descriptor", this.getClass().getName());
    }

    /**
     * Merge resource-ref definitions.
     * 
     * @param theWebXml the web xml to merge
     */
    private void mergeResourceRefs(ResinWebXml theWebXml)
    {
        AbstractNodeList ourItems = this.webXml.getResourceRefs();
        AbstractNodeList mergeItems = theWebXml.getResourceRefs();

        AbstractMergeSet merger = MergeNodeList.createFromNames(ourItems, mergeItems);

        int count = merger.merge(this.mergeResourceRefsStrategy);

        getLogger().debug(
            "Merged " + count + " resource-ref definition" + (count != 1 ? "s " : " ")
                + "into the descriptor", this.getClass().getName());
    }

    /**
     * Merge jndi-link definitions.
     * 
     * @param theWebXml the web xml to merge
     */
    private void mergeJndiLinks(ResinWebXml theWebXml)
    {
        AbstractNodeList ourItems = this.webXml.getJndiLinks();
        AbstractNodeList mergeItems = theWebXml.getJndiLinks();

        AbstractMergeSet merger = MergeNodeList.createFromNames(ourItems, mergeItems);

        int count = merger.merge(this.mergeJndiLinksStrategy);

        getLogger().debug(
            "Merged " + count + " jndi-link definition" + (count != 1 ? "s " : " ")
                + "into the descriptor", this.getClass().getName());
    }

    /**
     * Merges the system-property definitions from the specified descriptor into the original
     * descriptor.
     * 
     * @param theWebXml The descriptor that contains the context-params definitions that are to be
     *            merged into the original descriptor
     */
    private void mergeSystemProperties(ResinWebXml theWebXml)
    {
        AbstractNodeList ourSystemProperties = this.webXml.getSystemProperties();
        AbstractNodeList mergeSystemProperties = theWebXml.getSystemProperties();

        AbstractMergeSet merger = MergeNodeList.createFromNames(ourSystemProperties,
            mergeSystemProperties);

        int count = merger.merge(this.mergeSystemPropertiesStrategy);

        getLogger().debug(
            "Merged " + count + " system-property definition" + (count != 1 ? "s " : " ")
                + "into the descriptor", this.getClass().getName());
    }

}
