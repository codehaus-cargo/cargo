/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.maven3.configuration;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.maven3.util.CargoProject;

/**
 * Holds configuration data for the <code>&lt;deployable&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.<br><br>
 * <b>TODO</b>: Find a way to remove code duplication with Ant's DeployableElement
 */
public class Deployable extends AbstractDependency
{
    /**
     * Constant for Maven 3 project type <code>ejb</code>.
     */
    private static final String EJB = "ejb";

    /**
     * Constant for Maven 3 project type <code>bundle</code>.
     */
    private static final String BUNDLE = "bundle";

    /**
     * Constant for Maven 3 project type <code>uberwar</code>.
     */
    private static final String UBERWAR = "uberwar";

    /**
     * Constant for Maven 3 project type starting with <code>jboss-</code>.<br><br>
     * JBoss needs special checks, see https://codehaus-cargo.atlassian.net/browse/CARGO-710
     */
    private static final String JBOSS = "jboss-";

    /**
     * Length of {@link Deployable#JBOSS}
     */
    private static final int JBOSS_STRIP = Deployable.JBOSS.length();

    /**
     * Ping URL.
     */
    private URL pingURL;

    /**
     * Ping URL path.
     */
    private String pingUrlPath;

    /**
     * Ping timeout.
     */
    private Long pingTimeout;

    /**
     * Implementation.
     */
    private String implementation;

    /**
     * Deployable properties.
     */
    private Map<String, String> properties;

    /**
     * @return Deployable properties.
     */
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    /**
     * @param properties Deployable properties.
     */
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    /**
     * @return Ping URL.
     */
    public URL getPingURL()
    {
        return this.pingURL;
    }

    /**
     * @return Ping URL path.
     */
    public String getPingUrlPath()
    {
        return this.pingUrlPath;
    }

    /**
     * @return Ping timeout.
     */
    public Long getPingTimeout()
    {
        return this.pingTimeout;
    }

    /**
     * @param implementation Implementation.
     */
    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    /**
     * @return Implementation.
     */
    public String getImplementation()
    {
        return this.implementation;
    }

    /**
     * Create a deployable.
     * @param containerId Container identifier.
     * @param project Cargo project.
     * @return Deployable.
     * @throws MojoExecutionException If location computation or deployable instanciation fails.
     */
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

        project.getLog().debug(
            "Computed deployable values: groupId = [" + getGroupId()
                + "], artifactId = [" + getArtifactId() + "], classifier = [" + getClassifier()
                + "], type = [" + getType()
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

        String name = null;
        String context = null;
        if (getProperties() != null)
        {
            name = getProperties().get("name");
            context = getProperties().get("context");
        }
        if (deployable instanceof EAR && name == null)
        {
            EAR ear = (EAR) deployable;
            ear.setName(getArtifactId());
        }
        if (deployable instanceof WAR && context == null)
        {
            WAR war = (WAR) deployable;
            if (getGroupId().equals(project.getGroupId())
                && getArtifactId().equals(project.getArtifactId()))
            {
                // CARGO-1279: Honor the finalName if it was set manually
                context = project.getFinalName();
                if (context == null || context.startsWith(getArtifactId() + '-'))
                {
                    context = getArtifactId();
                }
            }
            else
            {
                context = getArtifactId();
            }
            war.setContext(context);
        }

        return deployable;
    }

    /**
     * Set user-defined properties on the created deployable.
     * @param deployable the deployable on which to set the properties
     * @param project Cargo project.
     * @throws MojoExecutionException If property  name is invalid for deployable type
     */
    protected void setPropertiesOnDeployable(
        org.codehaus.cargo.container.deployable.Deployable deployable, CargoProject project)
        throws MojoExecutionException
    {
        if (getProperties() != null)
        {
            for (Map.Entry<String, String> property : getProperties().entrySet())
            {
                String propertyName = property.getKey();

                project.getLog().debug("Setting deployable property [" + propertyName + "]:["
                    + getProperties().get(propertyName) + "] for [" + getLocation() + "]");

                // Maven 3 doesn't like empty elements and will set them to Null. Thus we
                // need to modify that behavior and change them to an empty string. For example
                // this allows users to pass an empty context to mean the root context.
                String propertyValue = property.getValue();
                if (propertyValue == null)
                {
                    propertyValue = "";
                }

                try
                {
                    callMethodForProperty(deployable, propertyName, propertyValue, project);
                }
                catch (Exception e)
                {
                    throw new MojoExecutionException("Invalid property [" + propertyName
                        + "] for deployable type [" + deployable.getType() + "]", e);
                }
            }
        }
    }

    /**
     * Compute the location of the current deployable.
     * @param project Cargo project.
     * @return Location of current deployable.
     * @throws MojoExecutionException If location cannot be found.
     */
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
            String type = this.getType();
            this.setType(project.getPackaging());
            try
            {
                // Let's look in the project's dependencies and find a match.
                location = findArtifactLocation(project.getArtifacts(), project.getLog());
            }
            catch (MojoExecutionException e)
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
                    location = new File(project.getBuildDirectory(), project.getFinalName() + "-"
                        + classifier + "." + computeExtension(project.getPackaging())).getPath();
                }
            }
            finally
            {
                this.setType(type);
            }
        }
        else
        {
            // Display a warning if the groupId and artifactId are the same as the project's but
            // the type is different.
            if (project.getGroupId().equals(getGroupId())
                && project.getArtifactId().equals(getArtifactId()))
            {
                project
                    .getLog()
                    .warn("The defined deployable has the same groupId and artifactId "
                        + "as your project's main artifact but the type is different. You've "
                        + "defined a [" + getType() + "] type whereas the project's packaging is ["
                        + project.getPackaging() + "]. This is possibly an error and as a "
                        + "consequence the plugin will try to find this deployable in the "
                        + "project's dependencies.");
            }

            // Let's look in the project's dependencies and find a match.
            location = findArtifactLocation(project.getArtifacts(), project.getLog());
        }

        return location;
    }

    /**
     * Checks if deployable type is compatible with the project's packaging.
     * @param project Cargo project.
     * @return <code>true</code> if the deployable type is compatible with the project's packaging.
     */
    protected boolean isTypeCompatible(CargoProject project)
    {
        boolean isMatching = false;

        if (getType().equalsIgnoreCase(project.getPackaging())
            || getType().equalsIgnoreCase("file"))
        {
            isMatching = true;
        }
        else if (getType().equalsIgnoreCase("war")
            && project.getPackaging().equalsIgnoreCase(Deployable.UBERWAR))
        {
            isMatching = true;
        }
        else if (project.getPackaging().startsWith(Deployable.JBOSS)
            && getType().equalsIgnoreCase(
                project.getPackaging().substring(Deployable.JBOSS_STRIP)))
        {
            isMatching = true;
        }

        return isMatching;
    }

    /**
     * Compute the extension for a given Maven 3 packaging.
     * @param packaging Maven 3 project packaging (ex: ejb, ear, rar, war, etc)
     * @return Artifact extension matching the packaging
     */
    protected String computeExtension(String packaging)
    {
        String extension;
        if (packaging.equalsIgnoreCase(Deployable.EJB)
            || packaging.equalsIgnoreCase(Deployable.BUNDLE))
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
     * @param deployable Deployable on which to call the setter method corresponding to the
     * specified property.
     * @param name Property name.
     * @param value Property value.
     * @param project Cargo project.
     * @throws Exception if anything goes wrong.
     */
    private void callMethodForProperty(
        org.codehaus.cargo.container.deployable.Deployable deployable, String name, String value,
        CargoProject project) throws Exception
    {
        String setterMethodName = getSetterMethodName(name);

        Method method;
        Object argument;

        try
        {
            method = deployable.getClass().getMethod(setterMethodName, String.class);
            argument = value;
        }
        catch (NoSuchMethodException e)
        {
            // If we reach this line, it means there is no String setter for the given property
            // name with a String argument. Check if there is a setter with String[] argument; if
            // there is one split the value at each line and call the setter.
            method = deployable.getClass().getMethod(setterMethodName, String[].class);

            List<String> valueList = new ArrayList<String>();
            StringTokenizer commaSeparatedValue = new StringTokenizer(value, ",");
            while (commaSeparatedValue.hasMoreTokens())
            {
                String commaSeparatedLine = commaSeparatedValue.nextToken().trim();
                if (!commaSeparatedLine.isEmpty())
                {
                    valueList.add(commaSeparatedLine);
                }
            }
            String[] valueArray = new String[valueList.size()];
            valueArray = valueList.toArray(valueArray);
            argument = valueArray;
        }

        project.getLog().debug("Invoking setter method " + method + " for deployable "
            + deployable + " with argument " + argument);

        method.invoke(deployable, argument);
    }

    /**
     * Transform a property into a method name by transforming the first letter of the property name
     * to uppercase.
     * @param propertyName Property name to transform into a setter method
     * @return Setter method's name
     */
    protected String getSetterMethodName(String propertyName)
    {
        return "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH)
            + propertyName.substring(1);
    }
}
