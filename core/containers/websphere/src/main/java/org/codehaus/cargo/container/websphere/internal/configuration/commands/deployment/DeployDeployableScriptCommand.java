/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
import java.util.StringTokenizer;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.AbstractScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.WarArchiveIo;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;
import org.jdom.Element;

/**
 * Implementation of deploying deployable configuration script command.
 */
public class DeployDeployableScriptCommand extends AbstractScriptCommand
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
        propertiesMap.put("cargo.deployable.path.absolute", deployable.getFile());
        propertiesMap.put("cargo.deployable.id", deployable.getName());

        StringBuffer additionalArguments = new StringBuffer();
        addApplicationName(additionalArguments);
        addContext(additionalArguments);
        addResourceReferenceMapping(additionalArguments);
        addJndiForEjbMessageBindingMapping(additionalArguments);
        propertiesMap.put("cargo.deployable.websphere.arguments", additionalArguments.toString());
    }

    /**
     * @param arguments Arguments passed to configuration script.
     */
    private void addResourceReferenceMapping(StringBuffer arguments)
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
                    entryList.add(fileHandler.getName(deployable.getFile()));
                    entryList.add(resRefName);
                    entryList.add(fileHandler.getName(deployable.getFile()) + ",WEB-INF/web.xml");
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

        if (bindingString != null && bindingString.length() > 0)
        {
            StringTokenizer bindingEntries = new StringTokenizer(bindingString, "|");
            while (bindingEntries.hasMoreTokens())
            {
                String bindingEntry = bindingEntries.nextToken().trim();
                StringTokenizer bindingValues = new StringTokenizer(bindingEntry, ":");

                String deployableName = bindingValues.nextToken().trim();
                String ejbName = bindingValues.nextToken().trim();
                String ejbResourceName = bindingValues.nextToken().trim();
                String jndiName = bindingValues.nextToken().trim();

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
    private void addJndiForEjbMessageBindingMapping(StringBuffer arguments)
    {
        List<String> resRefList = new ArrayList<String>();

        FileHandler fileHandler = ((AbstractDeployable) deployable).getFileHandler();
        String bindingString = getConfiguration().
                getPropertyValue(WebSpherePropertySet.EJB_TO_ACT_SPEC_BINDING);

        if (bindingString != null && bindingString.length() > 0)
        {
            StringTokenizer bindingEntries = new StringTokenizer(bindingString, "|");
            while (bindingEntries.hasMoreTokens())
            {
                String bindingEntry = bindingEntries.nextToken().trim();
                StringTokenizer bindingValues = new StringTokenizer(bindingEntry, ":");

                String deployableName = bindingValues.nextToken().trim();
                String ejbName = bindingValues.nextToken().trim();
                String queueJndiName = bindingValues.nextToken().trim();

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
                            entryList.add(fileHandler.getName(deployable.getFile())
                                    + ",WEB-INF/ejb-jar.xml");
                            entryList.add("\"\"");
                            entryList.add("jms/activation/" + resource.getId());
                            entryList.add(resource.getName());
                            entryList.add("\"\"");
                            resRefList.add(convertListToString(entryList, " "));
                        }
                    }
                }
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
     * Adds application name.
     * @param arguments Arguments passed to configuration script.
     */
    private void addApplicationName(StringBuffer arguments)
    {
        arguments.append("'-appname','");
        arguments.append(deployable.getName());
        arguments.append("'");
    }

    /**
     * Adds context parameter if deployable is WAR file.
     * @param arguments Arguments passed to configuration script.
     */
    private void addContext(StringBuffer arguments)
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
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (String item : list)
        {
            if (sb.length() > 1)
            {
                sb.append(separator);
            }
            sb.append(item);
        }
        sb.append("]");
        return sb.toString();
    }
}
