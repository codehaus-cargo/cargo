/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5xEmbedded;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.module.webapp.tomcat.TomcatWarArchive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * {@link org.codehaus.cargo.container.deployer.Deployer} for deploying to
 * {@link Tomcat5xEmbeddedLocalContainer embedded Tomcat container}.
 *
 * @version $Id$
 */
public class Tomcat5xEmbeddedLocalDeployer extends AbstractLocalDeployer
{
    /**
     * The container that this deployer acts on.
     */
    private final Tomcat5xEmbeddedLocalContainer container;

    /**
     * Map from {@link Deployable} to {@link Tomcat5xEmbedded.Context}, representing deployed
     * objects.
     */
    private final Map deployed = new HashMap();

    /**
     * Creates a new deployer for {@link Tomcat5xEmbeddedLocalContainer}.
     *
     * @param container The container to which this deployer will work. This parameter is typed
     *        as {@link EmbeddedLocalContainer} due to the Cargo generic API requirement, but it
     *        has to be a {@link Tomcat5xEmbeddedLocalContainer}.
     */
    public Tomcat5xEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
        this.container = (Tomcat5xEmbeddedLocalContainer) container;
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#deploy(org.codehaus.cargo.container.deployable.Deployable) 
     */
    @Override
    public void deploy(Deployable deployable)
    {
        if (container.getController() == null)
        {
            // not yet started. defer the deployment until the container is started
            container.scheduleDeployment(deployable);
            return;
        }

        WAR war = (WAR) deployable;
        String docBase;

        if (!war.isExpandedWar())
        {
            String home = container.getConfiguration().getHome();
            try
            {
                docBase = getFileHandler().append(home, "webapps/" + war.getContext());
                explode(war.getFile(), docBase);
            }
            catch (IOException e)
            {
                throw new ContainerException("Failed to expand " + war.getFile(), e);
            }
        }
        else
        {
            docBase = war.getFile();
        }

        Tomcat5xEmbedded.Context context = container.getController().createContext(
            '/' + war.getContext(), docBase);

        try
        {
            TomcatWarArchive twar = new TomcatWarArchive(docBase);
            if (twar.getTomcatContextXml() != null)
            {
                Map params = twar.getTomcatContextXml().getParameters();
                for (Iterator itr = params.entrySet().iterator(); itr.hasNext();)
                {
                    Map.Entry param = (Map.Entry) itr.next();

                    //the parameter could be a string or a jdom Attribute depending on
                    //if its Tomcat 5.0.x or 5.5.x, so we need to check the value here.
                    String key;
                    String value;
                    if (param.getKey() instanceof org.jdom.Attribute)
                    {
                        key = ((org.jdom.Attribute) param.getKey()).getValue();
                    }
                    else if (param.getKey() instanceof String)
                    {
                        key = (String) param.getKey();
                    }
                    else
                    {
                        throw new ContainerException("Cannot handle Parameter Type : " 
                                + param.getKey().getClass().toString());
                    }
                    
                    if (param.getValue() instanceof org.jdom.Attribute)
                    {
                        value = ((org.jdom.Attribute) param.getValue()).getValue();
                    }
                    else if (param.getValue() instanceof String)
                    {
                        value = (String) param.getValue();
                    }
                    else
                    {
                        throw new ContainerException("Cannot handle Parameter Type : " 
                                + param.getValue().getClass().toString());
                    }
                    
                    context.addParameter(key, value);
                }
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to parse Tomcat WAR file "
                + "in [" + docBase + "]", e);
        }


        container.getHost().addChild(context);
        deployed.put(deployable, context);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        Tomcat5xEmbedded.Context context = getExistingContext(deployable);
        container.getHost().removeChild(context);
        deployed.remove(deployable);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        Tomcat5xEmbedded.Context context = getExistingContext(deployable);
        context.reload();
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        getExistingContext(deployable).setAvailable(true);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        getExistingContext(deployable).setAvailable(false);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer#getType()
     */
    public DeployerType getType()
    {
        return DeployerType.EMBEDDED;
    }

    /**
     * Gets the context that represents a deployed {@link Deployable}.
     *
     * @param deployable the deployable object that has deployed on Tomcat.
     * @return always non-null
     */
    private Tomcat5xEmbedded.Context getExistingContext(Deployable deployable)
    {
        Tomcat5xEmbedded.Context context = (Tomcat5xEmbedded.Context) deployed.get(deployable);
        if (context == null)
        {
            throw new ContainerException("Not deployed yet: " + deployable);
        }
        return context;
    }

    /**
     * Extracts a war file into a directory.
     *
     * @param war the War archive to be extracted.
     * @param exploded the directory that receives files.
     * @throws IOException any file operation failure
     */
    private void explode(String war, String exploded) throws IOException
    {
        if (getFileHandler().exists(exploded))
        {
            getFileHandler().delete(exploded);
        }

        byte[] buf = new byte[1024];

        JarFile archive = new JarFile(new File(war).getAbsoluteFile());
        Enumeration e = archive.entries();
        while (e.hasMoreElements())
        {
            JarEntry j = (JarEntry) e.nextElement();
            String dst = getFileHandler().append(exploded, j.getName());

            if (j.isDirectory())
            {
                getFileHandler().mkdirs(dst);
                continue;
            }

            getFileHandler().mkdirs(getFileHandler().getParent(dst));

            InputStream in = archive.getInputStream(j);
            FileOutputStream out = new FileOutputStream(dst);
            try
            {
                while (true)
                {
                    int sz = in.read(buf);
                    if (sz < 0)
                    {
                        break;
                    }
                    out.write(buf, 0, sz);
                }
            }
            finally
            {
                in.close();
                out.close();
            }
        }

        archive.close();
    }
}
