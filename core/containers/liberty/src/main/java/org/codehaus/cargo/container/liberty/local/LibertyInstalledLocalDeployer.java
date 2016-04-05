package org.codehaus.cargo.container.liberty.local;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.liberty.internal.LibertyInstall;
import org.codehaus.cargo.container.liberty.internal.ServerConfigUtils;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Deploys the application to the Liberty server.
 */
public class LibertyInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * Creates the local deployer.
     * 
     * @param container
     *            the container to deploy to
     */
    public LibertyInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    /**
     * Get the directory to deploy the application to
     * 
     * @param deployable
     *            the thing to deploy
     * @return the path to the director to deploy to
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        LibertyInstall install = new LibertyInstall(
                (AbstractInstalledLocalContainer) getContainer());

        File serverDir = install.getServerDir(null);
        String dir = "dropins";
        if (deployable.getType() == DeployableType.WAR)
        {
            dir = "apps";
        }
        return new File(serverDir, dir).getAbsolutePath();
    }

    @Override
    protected void doDeploy(String deployableDir, Deployable app)
    {
        super.doDeploy(deployableDir, app);
        if (app.getType() == DeployableType.WAR)
        {
            LibertyInstall install = new LibertyInstall(
                    (AbstractInstalledLocalContainer) getContainer());
            
            File serverDir = install.getServerDir(null);
            File configOverrides = new File(serverDir, "configDropins/overrides");
            configOverrides.mkdirs();

            String fileName = getDeployableName(app);
            File appXML = new File(configOverrides, 
                    "cargo-app-" + fileName.replaceAll("/",  "_") + ".xml");
            try 
            {
                PrintStream writer = ServerConfigUtils.open(appXML);
    
                String ctxRoot = getContextRoot(app);

                writer.print("  <webApplication location=\"");
                writer.print(fileName);
                writer.print('\"');
                if (ctxRoot != null) 
                {
                    writer.print(" contextRoot=\"");
                    writer.print(ctxRoot);
                    writer.print('\"');
                }
                writer.println('>');

                writeLibrary(writer, app);
                writeSecurity(writer);

                writer.println("  </webApplication>");
                ServerConfigUtils.close(writer);
            }
            catch (IOException ioe)
            {
                // TODO work out what to do here.
            }
        }
    }

    /**
     * Write the role to group mapping as group - role 1-1
     * @param writer the writer
     * @throws IOException if an something goes wrong.
     */
    private void writeSecurity(PrintStream writer) throws IOException
    {
        String users = getContainer().getConfiguration().getPropertyValue(ServletPropertySet.USERS);
        if (users != null)
        {
            String[] elements = users.split("\\|");
            Set<String> groups = new HashSet<String>();
            for (String s : elements)
            {
                String[] parts = s.split(":");
                if (parts.length == 3)
                {
                    String[] groupNames = parts[2].split(",");
                    groups.addAll(Arrays.asList(groupNames));
                }
            }
            
            if (!!!groups.isEmpty())
            {
                writer.println("    <application-bnd>");
                for (String group : groups)
                {
                    writer.print("      <security-role name=\"");
                    writer.print(group);
                    writer.println("\">");
                    writer.print("        <group name=\"");
                    writer.print(group);
                    writer.print("\" access-id=\"");
                    writer.print(group);
                    writer.println("\"/>");
                    writer.println("      </security-role>");
                }
                writer.println("    </application-bnd>");
            }
        }
    }

    /**
     * Write a library for any extra classpath entries
     * @param writer the write to write the xml to.
     * @param app the application being deployed
     * @throws IOException if something goes wrong.
     */
    private void writeLibrary(PrintStream writer, Deployable app) throws IOException
    {
        List<String> cp = new ArrayList<String>();
        if (app instanceof WAR)
        {
            String[] appCp = ((WAR) app).getExtraClasspath();
            if (appCp != null)
            {
                cp.addAll(Arrays.asList(appCp));
            }
        }
        
        boolean containerLibrary = false;
        LocalContainer container = getContainer();
        if (container instanceof InstalledLocalContainer)
        {
            String[] contCp = ((InstalledLocalContainer) container).getExtraClasspath();
            if (contCp != null)
            {
                containerLibrary = true;
            }
        }
        
        writer.print("    <classloader");
        if (containerLibrary)
        {
            writer.print(" commonLibraryRef=\"cargoLib\"");
        }
        writer.println('>');
        
        if (!!!cp.isEmpty())
        {
            writer.println("      <privateLibrary>");
            
            for (String file : cp)
            {
                File f = new File(file);
                if (f.isDirectory())
                {
                    writer.print("        <folder dir=\"");
                    writer.print(f.getAbsolutePath());
                    writer.println("\"/>");
                } 
                else 
                {
                    writer.print("        <file name=\"");
                    writer.print(f.getAbsolutePath());
                    writer.println("\"/>");
                }
            }
            writer.println("      </privateLibrary>");
        }
        writer.println("    </classloader>");
    }

    /**
     * Get the context root for the deployer if possible.
     * @param deployable the deployable to get the context root for
     * @return the context root if there is one, or null if there isn't.
     */
    private String getContextRoot(Deployable deployable)
    {
        String result = null;
        
        if (deployable instanceof WAR) 
        {
            result = ((WAR) deployable).getContext();
            // if the context root is "" bind it to "/"
            if (result != null && result.length() == 0)
            {
                result = "/";
            }
        }
        
        return result;
    }

}
