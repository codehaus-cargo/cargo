/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.util.List;

import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.XmlMerger;
import org.codehaus.cargo.module.merge.DescriptorMergerByTag;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.tagstrategy.MergeStrategy;
import org.codehaus.cargo.module.webapp.VendorWebAppDescriptor;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.module.webapp.WebXmlVersion;
import org.codehaus.cargo.module.webapp.elements.MimeMapping;
import org.codehaus.cargo.util.CargoException;
import org.jdom2.Element;

/**
 * Helper class that can merge two web deployment descriptors.
 */
public class WebXmlMerger extends XmlMerger
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
        descriptorMergerByTag.setStrategy(
            WebXmlType.SERVLET_MAPPING, DescriptorMergerByTag.IGNORE);
        descriptorMergerByTag.setStrategy(
            WebXmlType.MIME_MAPPING, DescriptorMergerByTag.IGNORE);

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
     */
    @Override
    public void init(Descriptor base)
    {
        this.webXml = (WebXml) base;
        descriptorMergerByTag.setDescriptorType(base.getDescriptorType());
        super.init(base);
    }

    /**
     * Set the merging strategy for a particular tag.
     * 
     * @param tag Tag to set
     * @param strategy Strategy to use
     */
    public void setMergeStrategy(String tag, MergeStrategy strategy)
    {
        descriptorMergerByTag.setStrategy(tag, strategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void merge(Descriptor theMerge)
    {
        try
        {
            WebXml theMergeWebXml = (WebXml) theMerge;
            checkServletVersions(theMergeWebXml);

            super.merge(theMerge);

            WebXmlVersion version = this.webXml.getVersion();
            if (version == null || WebXmlVersion.V2_3.compareTo(version) <= 0)
            {
                mergeFilters(theMergeWebXml);
            }
            mergeServlets(theMergeWebXml);

            mergeSecurityRoles(theMergeWebXml);

            mergeMimeMappings(theMergeWebXml);
        }
        catch (Exception e)
        {
            throw new MergeException("Exception merging web.xml files", e);
        }
    }

    /**
     * Retrieves merged descriptor.
     * @return WebXml object
     */
    public WebXml getResult()
    {
        return this.webXml;
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
        for (Descriptor d : theWebXml.getVendorDescriptors())
        {
            VendorWebAppDescriptor descriptor = (VendorWebAppDescriptor) d;
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
        if (this.webXml.getVersion() != null
            && this.webXml.getVersion().compareTo(theWebXml.getVersion()) < 0)
        {
            getLogger().warn("Merging elements from a version " + theWebXml.getVersion()
                + " descriptor into a version " + this.webXml.getVersion()
                + ", some elements may be skipped", this.getClass().getName());
        }
    }

    /**
     * Merges the filter definitions from the specified descriptor into the original descriptor.
     * 
     * @param theWebXml The descriptor that contains the filter definitions that are to be merged
     * into the original descriptor
     */
    protected final void mergeFilters(WebXml theWebXml)
    {
        try
        {
            List<String> filterNames = WebXmlUtils.getFilterNames(theWebXml);
            int count = 0;
            for (String filterName : filterNames)
            {
                if (!WebXmlUtils.hasFilter(this.webXml, filterName))
                {
                    WebXmlUtils.addFilter(this.webXml,
                        WebXmlUtils.getFilter(theWebXml, filterName));
                }
                else
                {
                    // merge the parameters
                    List<String> existingInitParams =
                        WebXmlUtils.getFilterInitParamNames(this.webXml, filterName);
                    List<String> filterInitParamNames =
                        WebXmlUtils.getFilterInitParamNames(theWebXml, filterName);
                    for (String paramName : filterInitParamNames)
                    {
                        if (!existingInitParams.contains(paramName))
                        {
                            String paramValue =
                                WebXmlUtils.getFilterInitParam(theWebXml, filterName, paramName);
                            WebXmlUtils.addFilterInitParam(
                                this.webXml, filterName, paramName, paramValue);
                        }
                    }
                }
                // merge the URL patterns
                List<String> existingFilterMappings =
                    WebXmlUtils.getFilterMappings(this.webXml, filterName);
                List<String> filterMappings =
                    WebXmlUtils.getFilterMappings(theWebXml, filterName);
                for (String urlPattern : filterMappings)
                {
                    if (!existingFilterMappings.contains(urlPattern))
                    {
                        WebXmlUtils.addFilterMapping(this.webXml, filterName, urlPattern);
                    }
                }
                WebXmlVersion version = this.webXml.getVersion();
                if (version == null || WebXmlVersion.V2_4.compareTo(version) <= 0)
                {
                    // merge the dispatchers
                    List<String> existingFilterDispatchers =
                        WebXmlUtils.getFilterDispatchers(this.webXml, filterName);
                    List<String> filterDispatchers =
                        WebXmlUtils.getFilterDispatchers(theWebXml, filterName);
                    for (String dispatcher : filterDispatchers)
                    {
                        if (!existingFilterDispatchers.contains(dispatcher))
                        {
                            WebXmlUtils.addFilterDispatcher(this.webXml, filterName, dispatcher);
                        }
                    }
                }
                count++;
            }
            getLogger().debug("Merged " + count + " filter definition"
                + (count != 1 ? "s " : " ") + "into the descriptor",
                    this.getClass().getName());
        }
        catch (Exception e)
        {
            throw new CargoException("Exception merging filter definitions", e);
        }
    }

    /**
     * Merges the servlet definitions from the specified descriptor into the original descriptor.
     * 
     * @param theWebXml The descriptor that contains the servlet definitions that are to be merged
     * into the original descriptor
     * @throws CargoException if there is any merge problem
     */
    protected final void mergeServlets(WebXml theWebXml) throws CargoException
    {
        try
        {
            List<String> servletNames = WebXmlUtils.getServletNames(theWebXml);
            int count = 0;
            for (String servletName : servletNames)
            {
                if (!WebXmlUtils.hasServlet(this.webXml, servletName))
                {
                    WebXmlUtils.addServlet(this.webXml,
                        WebXmlUtils.getServlet(theWebXml, servletName));
                }
                else
                {
                    // merge the parameters
                    List<String> existingInitParams =
                        WebXmlUtils.getServletInitParamNames(this.webXml, servletName);
                    List<String> servletInitParamNames =
                        WebXmlUtils.getServletInitParamNames(theWebXml, servletName);
                    for (String paramName : servletInitParamNames)
                    {
                        if (!existingInitParams.contains(paramName))
                        {
                            String paramValue =
                                WebXmlUtils.getServletInitParam(theWebXml, servletName, paramName);
                            WebXmlUtils.addServletInitParam(
                                this.webXml, servletName, paramName, paramValue);
                        }
                    }
                    String roleName =
                        WebXmlUtils.getServletRunAsRoleName(theWebXml, servletName);
                    if (roleName != null)
                    {
                        WebXmlUtils.addServletRunAsRoleName(this.webXml, servletName, roleName);
                    }
                }
                // merge the mappings
                List<String> existingServletMappings =
                    WebXmlUtils.getServletMappings(this.webXml, servletName);
                List<String> servletMappings =
                    WebXmlUtils.getServletMappings(theWebXml, servletName);
                for (String urlPattern : servletMappings)
                {
                    if (!existingServletMappings.contains(urlPattern))
                    {
                        WebXmlUtils.addServletMapping(this.webXml, servletName, urlPattern);
                    }
                }
                count++;
            }
            getLogger().debug("Merged " + count + " servlet definition"
                + (count != 1 ? "s " : " ") + "into the descriptor",
                    this.getClass().getName());
        }
        catch (Exception e)
        {
            throw new CargoException("Exception merging servlet definitions", e);
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
        List<String> securityRoleNames = WebXmlUtils.getSecurityRoleNames(theWebXml);
        int count = 0;
        for (String securityRoleName : securityRoleNames)
        {
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

    /**
     * Merges the servlet definitions from the specified descriptor into the original descriptor.
     * 
     * @param theWebXml The descriptor that contains the filter definitions that are to be merged
     * into the original descriptor
     */
    protected final void mergeMimeMappings(WebXml theWebXml)
    {
        int count = 0;

        List<Element> srcItems = webXml.getTags(WebXmlType.MIME_MAPPING);
        List<Element> targetItems = theWebXml.getTags(WebXmlType.MIME_MAPPING);

        for (Element targetItem : targetItems)
        {
            boolean foundItem = false;

            MimeMapping targetMimeMapping = (MimeMapping) targetItem;

            for (Element srcItem : srcItems)
            {
                MimeMapping srcMimeMapping = (MimeMapping) srcItem;

                if (targetMimeMapping.getExtension().equals(srcMimeMapping.getExtension()))
                {
                    foundItem = true;
                    break;
                }
            }

            if (!foundItem)
            {
                MimeMapping mimeMappingElement = (MimeMapping) webXml.getDescriptorType().
                    getTagByName(WebXmlType.MIME_MAPPING).create();

                mimeMappingElement.setExtension(targetMimeMapping.getExtension());
                mimeMappingElement.setMimeType(targetMimeMapping.getMimeType());

                this.webXml.addElement(mimeMappingElement.getTag(), mimeMappingElement,
                    this.webXml.getRootElement());
                this.webXml.addTag(mimeMappingElement);
            }
        }

        getLogger().debug("Merged " + count + " mime mapping definition"
            + (count != 1 ? "s " : " ") + "into the descriptor",
                this.getClass().getName());
    }
}
