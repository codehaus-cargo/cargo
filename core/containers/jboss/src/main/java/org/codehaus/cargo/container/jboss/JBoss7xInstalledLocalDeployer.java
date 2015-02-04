/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jboss;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableException;

/**
 * Static deployer that deploys WARs and EARs to the JBoss <code>deployments</code> directory.
 * 
 * @version $Id$
 */
public class JBoss7xInstalledLocalDeployer extends JBossInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public JBoss7xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For JBoss container the target is the <code>deployments</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        String altDeployDir = getContainer().getConfiguration().
        getPropertyValue(JBossPropertySet.ALTERNATIVE_DEPLOYMENT_DIR);
        if (altDeployDir != null && !"".equals(altDeployDir))
        {
            getContainer().getLogger().info("Using non-default deployment target directory "
                + altDeployDir, this.getClass().getName());
            return getFileHandler().append(getContainer().getConfiguration().getHome(),
                altDeployDir);
        }
        else
        {
            return getFileHandler().append(getContainer().
                getConfiguration().getHome(), "deployments");
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer#deploy(Deployable)
     */
    @Override
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        Deployable deployableToDeploy;
        try
        {
            deployableToDeploy = modifyManifestForClasspathEntries(deployable);
        }
        catch (Exception e)
        {
            throw new DeployableException("Cannot update the MANIFEST of deployable"
                + " with the JBoss container classpath", e);
        }

        super.doDeploy(deployableDir, deployableToDeploy);

        if (deployableToDeploy.isExpanded())
        {
            String deployableName = getDeployableName(deployableToDeploy);
            getFileHandler().createFile(getFileHandler().append(deployableDir, deployableName
                + ".dodeploy"));
        }
    }

    /**
     * Modify the classpath via the <code>MANIFEST.MF</code> as explained on
     * https://community.jboss.org/wiki/HowToPutAnExternalFileInTheClasspath
     * @param originalDeployable Original deployable.
     * @return Modified deployable.
     * @throws Exception If anything goes wrong.
     */
    protected Deployable modifyManifestForClasspathEntries(Deployable originalDeployable)
        throws Exception
    {
        InstalledLocalContainer container = (InstalledLocalContainer) getContainer();

        Set<String> classpath = new TreeSet<String>();
        if (container.getExtraClasspath() != null && container.getExtraClasspath().length != 0)
        {
            for (String classpathElement : container.getExtraClasspath())
            {
                String moduleName = getFileHandler().getName(classpathElement);

                // Strip extension from JAR file to get module name
                moduleName = moduleName.substring(0, moduleName.lastIndexOf('.'));
                // CARGO-1091: JBoss expects subdirectories when the module name contains dots.
                //             Replace all dots with minus to keep a version separator.
                moduleName = moduleName.replace('.', '-');

                classpath.add(moduleName);
            }
        }
        if (container.getSharedClasspath() != null && container.getSharedClasspath().length != 0)
        {
            for (String classpathElement : container.getSharedClasspath())
            {
                String moduleName = getFileHandler().getName(classpathElement);

                // Strip extension from JAR file to get module name
                moduleName = moduleName.substring(0, moduleName.lastIndexOf('.'));
                // CARGO-1091: JBoss expects subdirectories when the module name contains dots.
                //             Replace all dots with minus to keep a version separator.
                moduleName = moduleName.replace('.', '-');

                classpath.add(moduleName);
            }
        }

        if (classpath.isEmpty())
        {
            return originalDeployable;
        }

        if (classpath.size() > 0 && originalDeployable.isExpanded())
        {
            getLogger().warn("The extra classpath and shared classpath options are not"
                + " supported with expanded deployables on " + container.getId(),
                    this.getClass().getName());
            return originalDeployable;
        }

        String outputFile =
            getFileHandler().append(getContainer().getConfiguration().getHome(), "tmp/cargo");
        getFileHandler().mkdirs(outputFile);
        outputFile = getFileHandler().append(outputFile, getDeployableName(originalDeployable));

        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(originalDeployable.getFile()));
        try
        {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
            try
            {
                ZipEntry entry = zin.getNextEntry();
                while (entry != null)
                {
                    String name = entry.getName();
                    if (name.equalsIgnoreCase("META-INF/MANIFEST.MF"))
                    {
                        Manifest manifest = new Manifest(zin);
                        String dependencies =
                            manifest.getMainAttributes().getValue("Dependencies");
                        if (dependencies == null)
                        {
                            dependencies = "";
                        }
                        for (String classpathEntry : classpath)
                        {
                            if (!dependencies.contains(classpathEntry))
                            {
                                if (dependencies.length() > 0)
                                {
                                    dependencies += ", ";
                                }
                                dependencies += "org.codehaus.cargo.classpath." + classpathEntry;
                            }
                        }
                        manifest.getMainAttributes().putValue("Dependencies", dependencies);
                        out.putNextEntry(new ZipEntry(name));
                        manifest.write(out);
                        out.closeEntry();
                    }
                    else
                    {
                        out.putNextEntry(new ZipEntry(name));
                        int len;
                        while ((len = zin.read(buf)) > 0)
                        {
                            out.write(buf, 0, len);
                        }
                    }
                    entry = zin.getNextEntry();
                }
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            zin.close();
        }

        return originalDeployable.getClass().getConstructor(String.class).newInstance(outputFile);
    }
}
