/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.module.internal.util.xml.AbstractNodeList;
import org.codehaus.cargo.module.merge.AbstractMergeSet;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.MergeNodeList;
import org.codehaus.cargo.module.merge.strategy.MergeStrategy;
import org.codehaus.cargo.module.webapp.resin.ResinWebXml;
import org.codehaus.cargo.module.webapp.resin.ResinWebXmlMerger;
import org.codehaus.cargo.util.log.LoggedObject;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * Helper class that can merge two web deployment descriptors.
 *
 * @version $Id$
 */
public class WebXmlMerger extends LoggedObject
{
    /**
     * Strategy for merging context parameters.
     */
    protected MergeStrategy mergeContextParamsStrategy = MergeStrategy.PRESERVE;

    /**
     * Strategy for merging listeners.
     */
    protected MergeStrategy mergeListenerStrategy = MergeStrategy.PRESERVE;
    
    /**
     * The original, authorative descriptor onto which the merges are performed.
     */
    private WebXml webXml;

    /**
     * Interface used as a callback for descriptors.
     *
     */
    protected interface IMergeImplementation
    {
        /**
         * Interface callback.
         *
         * @param ours in our descriptor
         * @param theirs in their descriptor to merge
         */
        void merge(VendorWebAppDescriptor ours, VendorWebAppDescriptor theirs);
    }
    
    /**
     * Constructor.
     *
     * @param theWebXml The original descriptor
     */
    public WebXmlMerger(WebXml theWebXml)
    {
        this.webXml = theWebXml;
    }

    /**
     * Set the merging strategy for context-param nodes.
     *
     * @param ms in the merging strategy
     */
    public void setMergeContextParamsStrategy(MergeStrategy ms)
    {
        this.mergeContextParamsStrategy = ms;
    }
    
    /**
     * Merges the merge descriptor with the original descriptor.
     *
     * @param theMergeWebXml The descriptor to merge in
     */
    public final void merge(WebXml theMergeWebXml)
    {
        try
        {
            checkServletVersions(theMergeWebXml);
        
            mergeContextParams(theMergeWebXml);
            if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
            {
                mergeFilters(theMergeWebXml);
            }
            mergeServlets(theMergeWebXml);
            if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
            {
                mergeResourceEnvironmentReferences(theMergeWebXml);
            }
            mergeListeners(theMergeWebXml);
            mergeResourceReferences(theMergeWebXml);
            mergeSecurityConstraints(theMergeWebXml);
            mergeLoginConfig(theMergeWebXml);
            mergeSecurityRoles(theMergeWebXml);
            mergeEnvironmentEntries(theMergeWebXml);
            mergeEjbRefs(theMergeWebXml);
            if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
            {
                mergeEjbLocalRefs(theMergeWebXml);
            }
            mergeVendorDescriptors(theMergeWebXml);

        }
        catch (Exception e)
        {
            throw new MergeException("Exception merging web.xml files", e);
        }
    }

    /**
     * Merge in any vendor specific descriptors.
     *
     * @param theWebXml xml to merge in
     */
    protected final void mergeVendorDescriptors(WebXml theWebXml)
    {
        mergeVendorDescriptors(theWebXml, ResinWebXml.class, new IMergeImplementation()
        {
            public void merge(VendorWebAppDescriptor ours, VendorWebAppDescriptor theirs)
            {
                ResinWebXmlMerger merger = new ResinWebXmlMerger((ResinWebXml) ours);
                merger.merge((ResinWebXml) theirs);
            }
        });
        
        // Add any extra descriptors here
    }
    
    /**
     * Find vendor descriptors of a specific class, and merge them if they exist.
     *
     * @param theWebXml in the web xml to be merged
     * @param clazz in the class of vendor webxml to merge 
     * @param merger in the callback class that does the merging
     */
    protected final void mergeVendorDescriptors(WebXml theWebXml, Class clazz,
        IMergeImplementation merger)
    {
        VendorWebAppDescriptor descriptorOurs = getVendorWebAppDescriptor(this.webXml, clazz);
        VendorWebAppDescriptor descriptorTheirs = getVendorWebAppDescriptor(theWebXml, clazz);

        if (descriptorTheirs != null)
        {
            if (descriptorOurs == null)
            {
                this.webXml.addVendorDescriptor(descriptorTheirs);
            }
            else
            {
                merger.merge(descriptorOurs, descriptorTheirs);

            }
        }
    }

    /**
     * Get the vendor web app descriptor out of the web xml.
     *
     * @param theWebXml in the web xml
     * @param clazz the class of vendor descriptor
     * @return the VendorWebAppDescriptor, or null if it does not exist in theWebXml
     */
    protected VendorWebAppDescriptor getVendorWebAppDescriptor(WebXml theWebXml, Class clazz)
    {
        for (Iterator i = theWebXml.getVendorDescriptors(); i.hasNext();)
        {
            VendorWebAppDescriptor descriptor = (VendorWebAppDescriptor) i.next();
            if (clazz.isInstance(descriptor))
            {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * Checks the versions of the servlet API in each descriptor, and logs a warning if a mismatch
     * might result in the loss of definitions.
     *
     * @param theWebXml The descriptor that will be merged with the original
     */
    protected final void checkServletVersions(WebXml theWebXml)
    {
        if ((this.webXml.getVersion() != null)
            && (this.webXml.getVersion().compareTo(theWebXml.getVersion()) < 0))
        {
            getLogger().warn("Merging elements from a version " + theWebXml.getVersion()
                + " descriptor into a version " + this.webXml.getVersion()
                + ", some elements may be skipped", this.getClass().getName());
        }
    }

    /**
     * Merges the context-param definitions from the specified descriptor into the original
     * descriptor.
     *
     * @param theWebXml The descriptor that contains the context-params definitions that are to be
     * merged into the original descriptor
     * @throws MergeException if there is a problem performing the merge
     */
    protected final void mergeContextParams(WebXml theWebXml) throws MergeException
    {
        
        AbstractNodeList ourItems = this.webXml.getContextParams();
        AbstractNodeList mergeItems = theWebXml.getContextParams();

        AbstractMergeSet merger = MergeNodeList.createFromNames(ourItems, mergeItems);

        try
        {        
            int count = merger.merge(this.mergeContextParamsStrategy);
    
            getLogger().debug(
                "Merged " + count + " context-param definition" + (count != 1 ? "s " : " ")
                    + "into the descriptor", this.getClass().getName());
        }
        catch (Exception e)
        {
            throw new MergeException("Exception in the merge", e);
        }
    }

    /**
     * Merges the servlet definitions from the specified descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the filter definitions that are to be merged
     * into the original descriptor
     */
    protected final void mergeFilters(WebXml theWebXml)
    {
        Iterator filterNames = theWebXml.getFilterNames();
        int count = 0;
        while (filterNames.hasNext())
        {
            String filterName = (String) filterNames.next();
            if (!this.webXml.hasFilter(filterName))
            {
                this.webXml.addFilter(theWebXml.getFilter(filterName));
            }
            else
            {
                // merge the parameters
                Iterator filterInitParamNames =
                    theWebXml.getFilterInitParamNames(filterName);
                while (filterInitParamNames.hasNext())
                {
                    String paramName = (String) filterInitParamNames.next();
                    String paramValue =
                        theWebXml.getFilterInitParam(filterName, paramName);
                    this.webXml.addFilterInitParam(
                        filterName, paramName, paramValue);
                }
            }
            // merge the mappings
            Iterator filterMappings = theWebXml.getFilterMappings(filterName);
            while (filterMappings.hasNext())
            {
                String urlPattern = (String) filterMappings.next();
                this.webXml.addFilterMapping(filterName, urlPattern);
            }
            count++;
        }
        getLogger().debug("Merged " + count + " filter definition"
            + (count != 1 ? "s " : " ") + "into the descriptor",
            this.getClass().getName());
    }

    /**
     * Merges the servlet definitions from the specified descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the servlet definitions that are to be merged
     * into the original descriptor
     */
    protected final void mergeServlets(WebXml theWebXml)
    {
        Iterator servletNames = theWebXml.getServletNames();
        int count = 0;
        while (servletNames.hasNext())
        {
            String servletName = (String) servletNames.next();
            if (!this.webXml.hasServlet(servletName))
            {
                this.webXml.addServlet(theWebXml.getServlet(servletName));
            }
            else
            {
                // merge the parameters
                Iterator servletInitParamNames =
                    theWebXml.getServletInitParamNames(servletName);
                while (servletInitParamNames.hasNext())
                {
                    String paramName = (String) servletInitParamNames.next();
                    String paramValue =
                        theWebXml.getServletInitParam(servletName, paramName);
                    this.webXml.addServletInitParam(
                        servletName, paramName, paramValue);
                }
                String roleName =
                    theWebXml.getServletRunAsRoleName(servletName);
                if (roleName != null)
                {
                    this.webXml.addServletRunAsRoleName(servletName, roleName);
                }
            }
            // merge the mappings
            Iterator servletMappings =
                theWebXml.getServletMappings(servletName);
            while (servletMappings.hasNext())
            {
                String urlPattern = (String) servletMappings.next();
                this.webXml.addServletMapping(servletName, urlPattern);
            }
            count++;
        }
        getLogger().debug("Merged " + count + " servlet definition"
            + (count != 1 ? "s " : " ") + "into the descriptor",
            this.getClass().getName());
    }

    /**
     * Merges the listener elements from the provided descriptor into the original
     * descriptor.
     * 
     * @param theWebXml The descriptor that contains the references that are to be merged into the
     * original descriptor
     */
    protected final void mergeListeners(WebXml theWebXml)
    {
        AbstractNodeList ourItems = this.webXml.getListeners();
        AbstractNodeList mergeItems = theWebXml.getListeners();

        AbstractMergeSet merger = MergeNodeList.createFromNames(ourItems, mergeItems);

        try
        {        
            int count = merger.merge(this.mergeListenerStrategy);
    
            getLogger().debug(
                "Merged " + count + " listener definition" + (count != 1 ? "s " : " ")
                    + "into the descriptor", this.getClass().getName());
        }
        catch (Exception e)
        {
            throw new MergeException("Exception in the merge", e);
        }
    }
    
    /**
     * Merges the resource environment references from the provided descriptor into the original
     * descriptor.
     *
     * @param theWebXml The descriptor that contains the references that are to be merged into the
     * original descriptor
     */
    protected final void mergeResourceEnvironmentReferences(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.RESOURCE_ENV_REF);
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " resource environment "
                + "reference" + (count != 1 ? "s " : " ") + "into the "
                + "descriptor", this.getClass().getName());
        }
    }

    /**
     * Merges the resource references from the provided descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the resource refs that are to be merged into
     * the original descriptor
     */
    protected final void mergeResourceReferences(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.RESOURCE_REF);
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " resource reference"
                + (count != 1 ? "s " : " ") + "into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * @param theWebXml The descriptor that contains the security constraints that are to be merged
     * into the original descriptor
     */
    protected final void mergeSecurityConstraints(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.SECURITY_CONSTRAINT);
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " security constraint"
                + (count != 1 ? "s " : " ") + "into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * Merges the login configuration from the provided descriptor into the original descriptor,
     * thereby eventually replacing the existing login config.
     *
     * @param theWebXml The descriptor that contains the login config that is to be merged into the
     * original descriptor
     */
    protected final void mergeLoginConfig(WebXml theWebXml)
    {
        boolean replaced = replaceElement(theWebXml, WebXmlTag.LOGIN_CONFIG);
        if (replaced)
        {
            getLogger().debug(
                "Merged the login configuration into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * Merges the security roles from the provided descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the security roles that are to be merged into
     * the original descriptor
     */
    protected final void mergeSecurityRoles(WebXml theWebXml)
    {
        Iterator securityRoleNames = theWebXml.getSecurityRoleNames();
        int count = 0;
        while (securityRoleNames.hasNext())
        {
            String securityRoleName = (String) securityRoleNames.next();
            if (!this.webXml.hasSecurityRole(securityRoleName))
            {
                this.webXml.addSecurityRole(securityRoleName);
            }
        }
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " security roles into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * Merges the environment entries from the provided descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the environment entries that are to be merged
     * into the original descriptor
     */
    protected final void mergeEnvironmentEntries(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.ENV_ENTRY);
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " environment entr"
                + (count != 1 ? "ies " : "y ") + "into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * Merges the EJB references from the provided descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the EJB refs that are to be merged into the
     * original descriptor
     */
    protected final void mergeEjbRefs(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.EJB_REF);
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " EJB reference"
                + (count != 1 ? "s " : "y ") + "into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * Merges the EJB local references from the provided descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the EJB local refs that are to be merged into
     * the original descriptor
     */
    protected final void mergeEjbLocalRefs(WebXml theWebXml)
    {
        int count = insertElements(theWebXml, WebXmlTag.EJB_LOCAL_REF);
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " EJB local reference"
                + (count != 1 ? "s " : "y ") + "into the descriptor",
                this.getClass().getName());
        }
    }

    /**
     * Insert all elements of the specified tag from the given descriptor into the original
     * descriptor, and returns the number of elements that were added.
     *
     * @param theWebXml The descriptor that contains the elements that are to be merged into the
     * original descriptor
     * @param theTag Defines which elements will get merged
     *
     * @return The number of elements inserted into the original descriptor
     */
    private int insertElements(WebXml theWebXml, WebXmlTag theTag)
    {
        Iterator elements = theWebXml.getElements(theTag);
        int count = 0;
        while (elements.hasNext())
        {
            Element element = (Element) elements.next();
            this.webXml.addRootElement(theTag, element);
            count++;
        }
        return count;
    }

    /**
     * Replaces the element of the specified tag in the original descriptor with the equivalent
     * element in the specified descriptor.
     *
     * @param theWebXml The descriptor that contains the element that is to be added to the original
     * descriptor, replacing the original definition
     * @param theTag Defines which element will get replaced
     *
     * @return Whether the element was replaced
     */
    private boolean replaceElement(WebXml theWebXml, WebXmlTag theTag)
    {
        Iterator elements = theWebXml.getElements(theTag);
        if (elements.hasNext())
        {
            this.webXml.replaceRootElement(theTag, (Element) elements.next());
            return true;
        }
        return false;
    }
}
