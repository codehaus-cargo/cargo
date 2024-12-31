/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal.util;

import java.io.IOException;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.JarUtils;

/**
 * Utility class providing informations about modules.
 */
public final class WildFlyModuleUtils
{

    /**
     * Cannot instantiate this class.
     */
    private WildFlyModuleUtils()
    {
    }

    /**
     * Check if resource is deployed as module.
     * @param container WildFly installed local container.
     * @param jarFile JAR resource representing module.
     * @return true if module representing this dependency is already deployed
     */
    public static boolean isModuleDeployed(InstalledLocalContainer container, String jarFile)
    {
        FileHandler fileHandler = container.getFileHandler();
        String moduleArtifactName = getModuleArtifactName(jarFile, fileHandler);
        String folder = container.getHome() + "/modules/org/codehaus/cargo/classpath/"
            + moduleArtifactName + "/main";

        if (fileHandler.exists(folder))
        {
            return true;
        }
        return false;
    }

    /**
     * Get module whole name for dependency.
     * @param container WildFly installed local container.
     * @param jarFile JAR resource representing module.
     * @return Name of module representing this dependency.
     */
    public static String getModuleName(InstalledLocalContainer container, String jarFile)
    {
        FileHandler fileHandler = container.getFileHandler();
        String moduleArtifactName = getModuleArtifactName(jarFile, fileHandler);
        return "org.codehaus.cargo.classpath." + moduleArtifactName;
    }

    /**
     * Get module artifact name for dependency.
     * @param jarFile JAR resource representing module.
     * @param fileHandler File handler.
     * @return Artifact name of module representing this dependency.
     */
    private static String getModuleArtifactName(String jarFile, FileHandler fileHandler)
    {
        String moduleName = fileHandler.getName(jarFile);
        // Strip extension from JAR file to get module name
        moduleName = moduleName.substring(0, moduleName.lastIndexOf('.'));
        // CARGO-1091: JBoss expects subdirectories when the module name contains dots.
        //             Replace all dots with minus to keep a version separator.
        moduleName = moduleName.replace('.', '-');

        return moduleName;
    }

    /**
     * @param container Container
     * @param ds DataSource.
     * @return Name of module containing DataSource driver class.
     */
    public static String getDataSourceDriverModuleName(InstalledLocalContainer container,
        DataSource ds)
    {
        String driverJarFile = null;
        String driverClass = ds.getDriverClass();
        try
        {
            driverJarFile = findJarFile(container, driverClass);
        }
        catch (IOException e)
        {
            throw new CargoException("Caught Exception while looking for DataSource driver "
                + "class module: " + driverClass, e);
        }

        if (driverJarFile == null)
        {
            throw new CargoException("Datasource driver class " + driverClass
                + " wasn't found in the classpath");
        }

        return getModuleName(container, driverJarFile);
    }

    /**
     * Find JAR file containing specific class.
     * @param container Container.
     * @param clazz Class to be found handler.
     * @return Path to JAR file containing defined class.
     * @throws IOException In case of I/O problem.
     */
    private static String findJarFile(InstalledLocalContainer container, String clazz)
        throws IOException
    {
        JarUtils jarUtils = new JarUtils();
        String classJarFile = null;

        for (String classpathElement : container.getExtraClasspath())
        {
            if (jarUtils.containsClass(classpathElement, clazz))
            {
                classJarFile = classpathElement;
            }
        }
        for (String classpathElement : container.getSharedClasspath())
        {
            if (jarUtils.containsClass(classpathElement, clazz))
            {
                classJarFile = classpathElement;
            }
        }

        return classJarFile;
    }
}
