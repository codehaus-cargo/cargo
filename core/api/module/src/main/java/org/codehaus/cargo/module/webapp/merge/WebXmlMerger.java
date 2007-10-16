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
package org.codehaus.cargo.module.webapp.merge;

import java.util.Iterator;

import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.XmlMerger;
import org.codehaus.cargo.module.merge.DescriptorMergerByTag;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.elements.FilterMapping;
import org.codehaus.cargo.util.CargoException;

/**
 * Helper class that can merge two web deployment descriptors.
 *
 * @version $Id: WebXmlMerger.java 1462 2007-05-01 20:22:33Z magnayn $
 */
public class WebXmlMerger  extends XmlMerger 
{    
    /**
     * The original, authorative descriptor onto which the merges are performed.
     */
    private WebXml webXml;
        
    /**
     * Our merger.
     */
    private DescriptorMergerByTag descriptorMergerByTag;
    /**
     * Constructor.
     */
    public WebXmlMerger()
    {        
        descriptorMergerByTag = new DescriptorMergerByTag();

        // Default behaviours
        descriptorMergerByTag.setStrategy(
            WebXmlType.LOGIN_CONFIG, DescriptorMergerByTag.OVERWRITE);
        descriptorMergerByTag.setStrategy(
            WebXmlType.FILTER, DescriptorMergerByTag.IGNORE);
        descriptorMergerByTag.setStrategy(
            WebXmlType.FILTER_MAPPING, DescriptorMergerByTag.IGNORE);
        descriptorMergerByTag.setStrategy(
            WebXmlType.SERVLET, DescriptorMergerByTag.IGNORE);
      
        addMerger(descriptorMergerByTag);
    }

    /**
     * Constructor.
     * @param base Descriptor to use
     */
    public WebXmlMerger(Descriptor base)
    {
        this();
        init(base);
    }
    
    /**
     * {@inheritDoc}
     * @see #init(Descriptor)
     */
    public void init(Descriptor base)
    {
        this.webXml = (WebXml) base;     
        descriptorMergerByTag.setDescriptorType(base.getDescriptorType());
        super.init(base);
    }
    
    /**
     * {@inheritDoc}
     * @see #merge(Descriptor)
     */
    public final void merge(Descriptor theMerge)
    {
        try
        {
            WebXml theMergeWebXml = (WebXml) theMerge;
            checkServletVersions(theMergeWebXml);
        
            super.merge(theMerge);            
            
            if (WebXmlVersion.V2_3.compareTo(this.webXml.getVersion()) <= 0)
            {
                mergeFilters(theMergeWebXml);
            }
            mergeServlets(theMergeWebXml);
            
            mergeSecurityRoles(theMergeWebXml);
                        
        }
        catch (Exception e)
        {
            throw new MergeException("Exception merging web.xml files", e);
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
     * Merges the servlet definitions from the specified descriptor into the original descriptor.
     *
     * @param theWebXml The descriptor that contains the filter definitions that are to be merged
     * into the original descriptor
     */
    protected final void mergeFilters(WebXml theWebXml)
    {
        Iterator filterNames = WebXmlUtils.getFilterNames(theWebXml);
        int count = 0;
        while (filterNames.hasNext())
        {
            String filterName = (String) filterNames.next();
            if (!WebXmlUtils.hasFilter(webXml, filterName))
            {
                WebXmlUtils.addFilter(this.webXml, 
                    WebXmlUtils.getFilter(theWebXml, filterName));
            }
            else
            {
                // merge the parameters
                Iterator filterInitParamNames =
                    WebXmlUtils.getFilterInitParamNames(theWebXml, filterName);
                while (filterInitParamNames.hasNext())
                {
                    String paramName = (String) filterInitParamNames.next();
                    String paramValue =
                        WebXmlUtils.getFilterInitParam(theWebXml, filterName, paramName);
                    WebXmlUtils.addFilterInitParam(this.webXml,
                        filterName, paramName, paramValue);
                }
            }
            // merge the mappings
            Iterator filterMappings = WebXmlUtils.getFilterMappingElements(theWebXml, filterName);
            while (filterMappings.hasNext())
            {
                FilterMapping mapping = (FilterMapping) filterMappings.next();
                WebXmlUtils.addFilterMapping(this.webXml, mapping);
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
     * @throws CargoException if there is any merge problem
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws NoSuchMethodException 
     * @throws IllegalArgumentException 
     * @throws SecurityException 
     */
    protected final void mergeServlets(WebXml theWebXml) throws CargoException
    {
        try
        {
            Iterator servletNames = WebXmlUtils.getServletNames(theWebXml);
            int count = 0;
            while (servletNames.hasNext())
            {
                String servletName = (String) servletNames.next();
                if (!WebXmlUtils.hasServlet(this.webXml, servletName))
                {
                    WebXmlUtils.addServlet(this.webXml, 
                        WebXmlUtils.getServlet(theWebXml, servletName));
                }
                else
                {
                    // merge the parameters
                    Iterator servletInitParamNames =
                        WebXmlUtils.getServletInitParamNames(theWebXml, servletName);
                    while (servletInitParamNames.hasNext())
                    {
                        String paramName = (String) servletInitParamNames.next();
                        String paramValue =
                            WebXmlUtils.getServletInitParam(theWebXml, servletName, paramName);
                        WebXmlUtils.addServletInitParam(this.webXml,
                            servletName, paramName, paramValue);
                    }
                    String roleName =
                        WebXmlUtils.getServletRunAsRoleName(theWebXml, servletName);
                    if (roleName != null)
                    {
                        WebXmlUtils.addServletRunAsRoleName(this.webXml, servletName, roleName);
                    }
                }
                // merge the mappings
                Iterator servletMappings =
                    WebXmlUtils.getServletMappings(theWebXml, servletName);
                while (servletMappings.hasNext())
                {
                    String urlPattern = (String) servletMappings.next();
                    WebXmlUtils.addServletMapping(this.webXml, servletName, urlPattern);
                }
                count++;
            }
            getLogger().debug("Merged " + count + " servlet definition"
                + (count != 1 ? "s " : " ") + "into the descriptor",
                this.getClass().getName());
        }
        catch (Exception ex)
        {
            throw new CargoException("Exception merging servlet definitions", ex);
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
        Iterator securityRoleNames = WebXmlUtils.getSecurityRoleNames(theWebXml);
        int count = 0;
        while (securityRoleNames.hasNext())
        {
            String securityRoleName = (String) securityRoleNames.next();
            if (!WebXmlUtils.hasSecurityRole(this.webXml, securityRoleName))
            {
                WebXmlUtils.addSecurityRole(this.webXml, securityRoleName);
            }
        }
        if (count > 0)
        {
            getLogger().debug("Merged " + count + " security roles into the descriptor",
                this.getClass().getName());
        }
    }
}
