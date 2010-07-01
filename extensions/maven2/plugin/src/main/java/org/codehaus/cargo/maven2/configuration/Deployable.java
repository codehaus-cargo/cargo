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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.BuildException;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Holds configuration data for the <code>&lt;deployable&gt;</code> tag used to configure
 * the plugin in the <code>pom.xml</code> file.
 *
 * @todo find a way to remove code duplication with Ant's DeployableElement
 * @version $Id$
 */
public class Deployable extends AbstractDependency
{
    private final static String UBERWAR = "uberwar";

    // JBoss needs special checks, see https://jira.codehaus.org/browse/CARGO-710
    private final static String JBOSS = "jboss-";
    private final static int JBOSS_STRIP = Deployable.JBOSS.length();

    private URL pingURL;

    private Long pingTimeout;

    private String implementation;

    private Map properties;

    public Map getProperties()
    {
        return this.properties;
    }

    public void setProperties(Map properties)
    {
        this.properties = properties;
    }

    public URL getPingURL()
    {
        return this.pingURL;
    }

    public Long getPingTimeout()
    {
        return this.pingTimeout;
    }

    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    public String getImplementation()
    {
        return this.implementation;
    }

    public org.codehaus.cargo.container.deployable.Deployable createDeployable(String containerId,
        CargoProject project) throws MojoExecutionException
    {
        project.getLog().debug("Initial deployable values: groupId = [" + getGroupId()
            + "], artifactId = [" + getArtifactId() + "], type = [" + getType()
            + "], location = [" + getLocation() + "]");

        // If no groupId is specified use the project's groupId
        if (getGroupId() == null)
        {
            setGroupId(project.getGroupId());
        }

        // If no type is specified use the project's packaging
        if (getType() == null)
        {
            setType(project.getPackaging());
        }

        // If no artifactId is specified use the project's artifactId
        if (getArtifactId() == null)
        {
            setArtifactId(project.getArtifactId());
        }

        // If no location is specified, guess the location
        if (getLocation() == null)
        {
            setLocation(computeLocation(project));
        }

        project.getLog().debug("Computed deployable values: groupId = [" + getGroupId()
            + "], artifactId = [" + getArtifactId() + "], classifier = [" + getClassifier() +"], type = [" + getType()
            + "], location = [" + getLocation() + "]");

        DeployableFactory factory = new DefaultDeployableFactory();

        // If a custom implementation class is defined register it against the deployable factory.
        if (getImplementation() != null)
        {
            try
            {
                Class deployableClass = Class.forName(getImplementation(), true,
                    this.getClass().getClassLoader());
                factory.registerDeployable(containerId, DeployableType.toType(getType()),
                    deployableClass);
            }
            catch (ClassNotFoundException cnfe)
            {
              throw new MojoExecutionException("Custom deployable implementation ["
                  + getImplementation() + "] cannot be loaded", cnfe);
            }
        }

        org.codehaus.cargo.container.deployable.Deployable deployable =
            factory.createDeployable(containerId, getLocation(), DeployableType.toType(getType()));

        // Set user-defined properties on the created deployable.
        setPropertiesOnDeployable(deployable, project);

        return deployable;
    }

    /**
     * Set user-defined properties on the created deployable.
     *
     * @param deployable the deployable on which to set the properties
     */
    protected void setPropertiesOnDeployable(
        org.codehaus.cargo.container.deployable.Deployable deployable, CargoProject project)
    {
        if (getProperties() != null)
        {
            Iterator props = getProperties().keySet().iterator();
            while (props.hasNext())
            {
                String propertyName = (String) props.next();

                project.getLog().debug("Setting deployable property [" + propertyName + "]:["
                    + getProperties().get(propertyName) + "] for [" + getLocation() + "]");

                // Maven2 doesn't like empty elements and will set them to Null. Thus we
                // need to modify that behavior and change them to an empty string. For example
                // this allows users to pass an empty context to mean the root context.
                String propertyValue = (String) getProperties().get(propertyName);
                if (propertyValue == null)
                {
                    propertyValue = "";
                }

                callMethodForProperty(deployable, propertyName, propertyValue);
            }
        }
    }

    protected String computeLocation(CargoProject project) throws MojoExecutionException
    {
        String location;

        // If the groupId and artifactId match those of the project then we consider that the
        // deployable is the artifact generated by the project. We also check that the defined
        // Cargo type matches Maven's defined packaging. If it doesn't we look for the deployable
        // in the project's dependency list.
        if (project.getGroupId().equals(getGroupId())
            && project.getArtifactId().equals(getArtifactId())
            && isTypeCompatible(project))
        {
            String classifier = getClassifier();
            // Compute default location.
            if (classifier == null)
            {
                location = new File(project.getBuildDirectory(), project.getFinalName() + "."
                + computeExtension(project.getPackaging())).getPath();
            }
            else
            {
                location = new File(project.getBuildDirectory(), project.getFinalName() + "-"+ classifier + "."
                        + computeExtension(project.getPackaging())).getPath();
            }
        }
        else
        {
            // Display a warning if the groupId and artifactId are the same as the project's but
            // the type is different.
            if (project.getGroupId().equals(getGroupId())
                && project.getArtifactId().equals(getArtifactId()))
            {
                project.getLog().warn("The defined deployable has the same groupId and artifactId "
                    + "as your project's main artifact but the type is different. You've defined a "
                    + "[" + getType() + "] type whereas the project's packaging is ["
                    + project.getPackaging() + "]. This is possibly an error and as a consequence "
                    + "the plugin will try to find this deployable in the project's dependencies.");
            }

            // Let's look in the project's dependencies and find a match.
            location = findArtifactLocation(project.getArtifacts(), project.getLog());
        }

        return location;
    }

    /**
     * @return true if the deployable type is compatible with the project's packaging
     */
    protected boolean isTypeCompatible(CargoProject project)
    {
        boolean isMatching = false;

        if (getType().equalsIgnoreCase(project.getPackaging())
            ||  (getType().equalsIgnoreCase("war")
                && project.getPackaging().equalsIgnoreCase(Deployable.UBERWAR))
            ||  (project.getPackaging().startsWith(Deployable.JBOSS)
                && getType().equalsIgnoreCase(project.getPackaging().substring(Deployable.JBOSS_STRIP))))
        {
            isMatching = true;
        }

        return isMatching;
    }

    /**
     * @param packaging the Maven project packaging (ex: ejb, ear, rar, war, etc)
     * @return the artifact extension matching the packaging
     */
    protected String computeExtension(String packaging)
    {
        String extension;
        if (packaging.equalsIgnoreCase("ejb"))
        {
            extension = "jar";
        }
        else if (packaging.equalsIgnoreCase(Deployable.UBERWAR))
        {
            extension = "war";
        }
        else if (packaging.startsWith(Deployable.JBOSS))
        {
            extension = packaging.substring(Deployable.JBOSS_STRIP);
        }
        else
        {
            extension = packaging;
        }
        return extension;
    }

    /**
     * Call setter methods corresponding to deployable properties.
     *
     * @param deployable the deployable on which to call the setter method corresponding to the
     *        specified property
     */
    private void callMethodForProperty(
        org.codehaus.cargo.container.deployable.Deployable deployable, String name, String value)
    {
        try
        {
            Method method = deployable.getClass().getMethod(getSetterMethodName(name),
                new Class[] {String.class});
            method.invoke(deployable, new Object[] {value});
        }
        catch (Exception e)
        {
            throw new BuildException("Invalid property [" + name + "] for deployable type ["
                + deployable.getType() + "]", e);
        }
    }

    /**
     * Transform a property into a method name by transforming the first letter of the property
     * name to uppercase.
     *
     * @param propertyName the property name to transform into a setter method
     * @return the setter method's name
     */
    protected String getSetterMethodName(String propertyName)
    {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }
}
