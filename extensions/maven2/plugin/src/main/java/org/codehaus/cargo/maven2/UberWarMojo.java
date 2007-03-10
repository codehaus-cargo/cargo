/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.maven2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.merge.strategy.ChooseByNameMergeStrategy;
import org.codehaus.cargo.module.merge.strategy.MergeStrategy;
import org.codehaus.cargo.module.merge.strategy.NodeMergeStrategy;
import org.codehaus.cargo.module.merge.DocumentStreamAdapter;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WarArchiveMerger;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.xml.*;
import org.codehaus.plexus.util.xml.pull.*;
import org.codehaus.cargo.maven2.io.xpp3.UberWarXpp3Reader;
import org.xml.sax.SAXException;

/**
 * Build an uberwar.
 *
 * @goal uberwar
 * @phase package
 * @requiresDependencyResolution runtime
 * @version $Id: $
 */
public class UberWarMojo extends AbstractCommonMojo
{
    /**
     * The directory for the generated WAR.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String outputDirectory;

    /**
     * The name of the generated war.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String warName;

    /**
     * The file name to use for the merge descriptor
     *
     * @parameter
     */
    private File descriptor;

    /**
     * The id to use for the merge descriptor
     *
     * @parameter
     */
    private String descriptorId;

    /**
     * The list of WAR files to use (in order)
     *
     * @parameter
     */
    private List wars;

   /**
     * The settings to use when building the uber war
     *
     * @parameter
     */
    private PlexusConfiguration settings;

    /**
     * Executes the UberWarMojo on the current project.
     *
     * @throws MojoExecutionException if an error occured while building the webapp
     */
    public void execute() throws MojoExecutionException
    {
        Reader r = null;

        if ( this.descriptor != null )
        {
            try
            {
                r = new FileReader( this.descriptor );
            }
            catch(FileNotFoundException ex)
            {
                throw new MojoExecutionException( "Could not find specified descriptor" );
            }
        }
        else if ( this.descriptorId != null )
        {
            InputStream resourceAsStream = getClass().getResourceAsStream( "/uberwar/" + this.descriptorId + ".xml" );
            if ( resourceAsStream == null )
            {
                throw new MojoExecutionException( "Descriptor with ID '" + this.descriptorId + "' not found" );
            }
            r = new InputStreamReader( resourceAsStream );
        }
        else
        {
            throw new MojoExecutionException( "You must specify descriptor or descriptorId" );
        }

        try
        {
            UberWarXpp3Reader reader = new UberWarXpp3Reader();
            MergeRoot root = reader.read(r);

            Xpp3Dom dom;

            // Add the war files
            WarArchiveMerger wam = new WarArchiveMerger();
            List wars = root.getWars();
            if( wars.size() == 0 )
                addAllWars(wam);
            else
            {
                for(Iterator i = wars.iterator();i.hasNext();)
                {
                    String id = (String)i.next();
                    addWar(wam, id );
                }
            }

            for(Iterator i = root.getMerges().iterator(); i.hasNext();)
            {
                Merge merge = (Merge)i.next();
                doMerge( wam, merge );
            }

            WebXml merge = root.getWebXml();
            doWebXmlMerge(wam, merge);

            File warFile = new File(this.outputDirectory, this.warName + ".war");

            WarArchive output = (WarArchive) wam.performMerge();
            output.store(warFile);

            getProject().getArtifact().setFile(warFile);


        }
        catch(XmlPullParserException e)
        {
            throw new MojoExecutionException("Invalid XML descriptor", e);
        }
        catch(IOException e)
        {
            throw new MojoExecutionException("IOException creating UBERWAR", e);
        }
        catch (SAXException e)
        {
            throw new MojoExecutionException("Xml format exception creating UBERWAR", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new MojoExecutionException("Parsing exception creating UBERWAR", e);
        }
        catch (MergeException e)
        {
            throw new MojoExecutionException("Merging exception creating UBERWAR", e);
        }
    }

    private void doWebXmlMerge(WarArchiveMerger wam, WebXml merge) throws MojoExecutionException
    {
        Xpp3Dom dom = (Xpp3Dom)merge.getContextParams();

        try
        {
            MergeStrategy ms = makeStrategy(dom.getChild(0));

            wam.getWebXmlMerger().setMergeContextParamsStrategy(ms);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("Exception merging web.xml", e);
        }

    }

    private void doMerge(WarArchiveMerger wam, Merge merge) throws MojoExecutionException
    {
        try
        {
            String file     = merge.getFile();
            String document = merge.getDocument();
            String clazz    = merge.getClassname();

            MergeProcessor merger = (MergeProcessor) Class.forName(clazz)
                .newInstance();

            if (document != null)
            {
                merger = new DocumentStreamAdapter(merger);
                wam.addMergeProcessor(document, merger);
            }
            else
            {
                wam.addMergeProcessor(file, merger);
            }
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("Problem in file merge", e);
        }
    }


    protected MergeStrategy makeStrategy(Xpp3Dom config) throws MojoExecutionException
    {

        if( !config.getName().equals("strategy") )
        {
            throw new MojoExecutionException("You must specify a merge strategy");
        }

        String strategyName = config.getAttribute("name");

        if( strategyName.equalsIgnoreCase("Preserve"))
        {
            return MergeStrategy.PRESERVE;
        }
        else if( strategyName.equalsIgnoreCase("Overwrite"))
        {
            return MergeStrategy.OVERWRITE;
        }
        else if( strategyName.equalsIgnoreCase("ChooseByName"))
        {
            Xpp3Dom def = config.getChild("default").getChild(0);

            ChooseByNameMergeStrategy cbnms = new ChooseByNameMergeStrategy(makeStrategy(def));

            Xpp3Dom[] items = config.getChildren();
            for(int i=0; i<items.length;i++)
            {
                Xpp3Dom item = items[i];
                if( item.getName().equals("choice") )
                {
                    cbnms.addStrategyForName(item.getAttribute("name"), makeStrategy(item.getChild(0)));
                }
            }
            return cbnms;
        }
        if( strategyName.equalsIgnoreCase("NodeMerge"))
        {
            String theXml = config.getChild(0).toString();

            try
            {
                return new NodeMergeStrategy(new ByteArrayInputStream(theXml.getBytes()));
            }
            catch (Exception e)
            {
                throw new MojoExecutionException("Problem generating Node Merge strategy");
            }
        }

        throw new MojoExecutionException("Must provide a known strategy type (don't understand " + strategyName + ")");
    }

    protected void addWar(WarArchiveMerger wam, String artifactIdent)
        throws MojoExecutionException, IOException
    {
        for (Iterator iter = getProject().getArtifacts().iterator(); iter.hasNext();)
        {
            Artifact artifact = (Artifact) iter.next();

            ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            if (!artifact.isOptional() && filter.include(artifact))
            {
                String type = artifact.getType();
                if ("war".equals(type))
                {
                    String name = artifact.getGroupId() + ":" + artifact.getArtifactId();
                    if (name.equals(artifactIdent))
                    {
                        try
                        {
                            wam.addMergeItem(new DefaultWarArchive(artifact.getFile().getPath()));
                        }
                        catch(MergeException e)
                        {
                            throw new MojoExecutionException("Problem merging WAR", e);
                        }
                        return;
                    }
                }
            }
        }
        // If we get here, we specified something for which there was no dependency
        // matching
        throw new MojoExecutionException("Could not find a dependent WAR file matching "
            + artifactIdent);
    }

    protected void addAllWars(WarArchiveMerger wam) throws MojoExecutionException,IOException
    {
        for (Iterator iter = getProject().getArtifacts().iterator(); iter.hasNext();)
        {
            Artifact artifact = (Artifact) iter.next();

            ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
            if (!artifact.isOptional() && filter.include(artifact))
            {
                String type = artifact.getType();
                if ("war".equals(type))
                {
                    try
                    {
                        wam.addMergeItem(new DefaultWarArchive(artifact.getFile().getPath()));
                    }
                    catch(MergeException e)
                    {
                        throw new MojoExecutionException("Problem merging WAR", e);
                    }
                }
            }
        }
    }

}
