/*
 * ========================================================================
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
package org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.util.ComplexPropertyUtils;
import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WarArchiveIo;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;
import org.jdom2.Element;

/**
 * Implementation of deploying deployable configuration script command.
 */
public class DeployDeployableScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * Deployable.
     */
    private Deployable deployable;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param deployable Deployable to be deployed.
     */
    public DeployDeployableScriptCommand(Configuration configuration, String resourcePath,
            Deployable deployable)
    {
        super(configuration, resourcePath);
        this.deployable = deployable;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "deployment/deploy-deployable.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        // CARGO-1484: Replace \ characters so, for example, the \t of \target in Windows
        // doesn't become a TAB character in the middle of the file name
        propertiesMap.put("cargo.deployable.path.absolute",
            deployable.getFile().replace('\\', '/'));
        propertiesMap.put("cargo.deployable.id", deployable.getName());

        StringBuilder additionalArguments = new StringBuilder();
        addApplicationName(additionalArguments);
        addContext(additionalArguments);
        addResourceReferenceMapping(additionalArguments);
        addJndiForEjbMessageBindingMapping(additionalArguments);
        addApplicationSecurityRolesMapping(additionalArguments);
        propertiesMap.put("cargo.deployable.websphere.arguments", additionalArguments.toString());
    }

    /**
     * @param arguments Arguments passed to configuration script.
     */
    private void addResourceReferenceMapping(StringBuilder arguments)
    {
        List<String> resRefList = new ArrayList<String>();
        FileHandler fileHandler = ((AbstractDeployable) deployable).getFileHandler();

        if (deployable instanceof WAR)
        {
            try
            {
                WarArchive warArchive = WarArchiveIo.open(deployable.getFile());
                WebXml webXml = warArchive.getWebXml();
                List<Element> resources = webXml.getElements(WebXmlType.RESOURCE_REF);
                for (Element resource : resources)
                {
                    String resRefName = resource.getChildText(WebXmlType.RES_REF_NAME);
                    String resType = resource.getChildText(WebXmlType.RES_TYPE);

                    List<String> entryList = new ArrayList<String>();
                    entryList.add(fileHandler.getName(deployable.getFile().replace('\\', '/')));
                    entryList.add(resRefName);
                    entryList.add(fileHandler.getName(
                        deployable.getFile().replace('\\', '/')) + ",WEB-INF/web.xml");
                    entryList.add(resRefName);
                    entryList.add(resType);
                    entryList.add(resRefName);
                    resRefList.add(convertListToString(entryList, " "));
                }
            }
            catch (Exception e)
            {
                throw new CargoException("Error when retrieving resource ref mapping!", e);
            }
        }

        String bindingString = getConfiguration().
                getPropertyValue(WebSpherePropertySet.EJB_TO_RES_REF_BINDING);
        List<List<String>> parsedBinding = ComplexPropertyUtils.parseProperty(bindingString);

        for (List<String> bindingItem : parsedBinding)
        {
            if (bindingItem.size() == 4)
            {
                String deployableName = bindingItem.get(0);
                String ejbName = bindingItem.get(1);
                String ejbResourceName = bindingItem.get(2);
                String jndiName = bindingItem.get(3);

                if (deployableName.equals(deployable.getName()))
                {
                    // find resource with name passed as argument
                    for (Resource resource : ((LocalConfiguration) getConfiguration()).
                            getResources())
                    {
                        if (jndiName.equals(resource.getName()))
                        {
                            List<String> entryList = new ArrayList<String>();
                            entryList.add(".*");
                            entryList.add(ejbName);
                            entryList.add(".*");
                            entryList.add(ejbResourceName);
                            entryList.add(resource.getType());
                            entryList.add(jndiName);
                            resRefList.add(convertListToString(entryList, " "));
                        }
                    }
                }
            }
            else
            {
                throw new CargoException("Resource reference property has to have 4 items,"
                        + "currently it has " + bindingItem.size() + " items.");
            }
        }

        if (resRefList.size() > 0)
        {
            arguments.append(",'-MapResRefToEJB','");
            arguments.append(convertListToString(resRefList, ""));
            arguments.append("'");
        }
    }

    /**
     * @param arguments Arguments passed to configuration script.
     */
    private void addJndiForEjbMessageBindingMapping(StringBuilder arguments)
    {
        List<String> resRefList = new ArrayList<String>();

        FileHandler fileHandler = ((AbstractDeployable) deployable).getFileHandler();
        String bindingString = getConfiguration().
                getPropertyValue(WebSpherePropertySet.EJB_TO_ACT_SPEC_BINDING);
        List<List<String>> parsedBinding = ComplexPropertyUtils.parseProperty(bindingString);

        for (List<String> bindingItem : parsedBinding)
        {
            if (bindingItem.size() == 3)
            {
                String deployableName = bindingItem.get(0);
                String ejbName = bindingItem.get(1);
                String queueJndiName = bindingItem.get(2);

                if (deployableName.equals(deployable.getName()))
                {
                    // find resource with name passed as argument
                    for (Resource resource : ((LocalConfiguration) getConfiguration()).
                            getResources())
                    {
                        if (queueJndiName.equals(resource.getName()))
                        {
                            List<String> entryList = new ArrayList<String>();
                            entryList.add(".*");
                            entryList.add(ejbName);
                            entryList.add(fileHandler.getName(
                                deployable.getFile().replace('\\', '/')) + ",WEB-INF/ejb-jar.xml");
                            entryList.add("\"\"");
                            entryList.add("jms/activation/" + resource.getId());
                            entryList.add(resource.getName());
                            entryList.add("\"\"");
                            resRefList.add(convertListToString(entryList, " "));
                        }
                    }
                }
            }
            else
            {
                throw new CargoException("EJB to act spec property has to have 3 items,"
                        + "currently it has " + bindingItem.size() + " items.");
            }
        }

        if (resRefList.size() > 0)
        {
            arguments.append(",'-BindJndiForEJBMessageBinding','");
            arguments.append(convertListToString(resRefList, ""));
            arguments.append("'");
        }
    }

    /**
     * @param arguments Arguments passed to configuration script.
     */
    private void addApplicationSecurityRolesMapping(StringBuilder arguments)
    {
        List<String> resRefList = new ArrayList<String>();

        if (deployable instanceof WAR)
        {
            try
            {
                WarArchive warArchive = WarArchiveIo.open(deployable.getFile());
                WebXml webXml = warArchive.getWebXml();
                List<String> securityRoleNames = WebXmlUtils.getSecurityRoleNames(webXml);
                for (String securityRoleName : securityRoleNames)
                {
                    List<String> entryList = new ArrayList<String>();
                    entryList.add(securityRoleName);
                    entryList.add("AppDeploymentOption.No AppDeploymentOption.No \"\"");
                    entryList.add(securityRoleName);
                    entryList.add("AppDeploymentOption.No \"\"");
                    entryList.add("group:defaultWIMFileBasedRealm/cn=" + securityRoleName
                            + ",o=defaultWIMFileBasedRealm");
                    resRefList.add(convertListToString(entryList, " "));
                }
            }
            catch (Exception e)
            {
                throw new CargoException("Error when retrieving security roles!", e);
            }
        }

        if (resRefList.size() > 0)
        {
            arguments.append(",'-MapRolesToUsers','");
            arguments.append(convertListToString(resRefList, ""));
            arguments.append("'");
        }
    }

    /**
     * Adds application name.
     * @param arguments Arguments passed to configuration script.
     */
    private void addApplicationName(StringBuilder arguments)
    {
        arguments.append("'-appname','");
        arguments.append(deployable.getName());
        arguments.append("'");
    }

    /**
     * Adds context parameter if deployable is WAR file.
     * @param arguments Arguments passed to configuration script.
     */
    private void addContext(StringBuilder arguments)
    {
        if (deployable instanceof WAR)
        {
            arguments.append(",'-contextroot','");
            arguments.append(((WAR) deployable).getContext());
            arguments.append("'");
        }
    }

    /**
     * @param list List to be converted.
     * @param separator Separator.
     * @return Converted list as String.
     */
    private String convertListToString(List<String> list, String separator)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(String.join(separator, list));
        sb.append("]");
        return sb.toString();
    }
}
