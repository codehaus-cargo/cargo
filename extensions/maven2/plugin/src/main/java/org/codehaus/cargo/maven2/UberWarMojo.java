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
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.installer.ArtifactInstaller;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.cargo.module.merge.MergeException;
import org.codehaus.cargo.module.merge.MergeProcessor;

import org.codehaus.cargo.module.merge.DocumentStreamAdapter;
import org.codehaus.cargo.module.webapp.WarArchive;
import org.codehaus.cargo.module.webapp.DefaultWarArchive;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.merge.WarArchiveMerger;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.xml.*;
import org.codehaus.plexus.util.xml.pull.*;
import org.codehaus.cargo.maven2.io.xpp3.UberWarXpp3Reader;
import org.codehaus.cargo.maven2.merge.MergeProcessorFactory;
import org.codehaus.cargo.maven2.merge.MergeWebXml;
import org.codehaus.cargo.maven2.merge.MergeXslt;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 * Build an uberwar
 *
 * @version
 * @goal uberwar
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class UberWarMojo extends AbstractUberWarMojo implements Contextualizable
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
     * Attempt to resolve dependencies, rather than simply merging the files
     * in WEB-INF/lib. This is an experimental feature.
     * 
     * @parameter
     */
    private boolean resolveDependencies = false;
    
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

	/** @component */
	private ArtifactFactory artifactFactory;

	/** @component */
	private ArtifactResolver resolver;

	/** @parameter expression="${localRepository}" */
	private ArtifactRepository localRepository;

	/** @parameter expression="${project.remoteArtifactRepositories}" */
	private List remoteRepositories;

	/** @parameter expression="${project}" */
	private MavenProject mavenProject;

	/** @component */
	private MavenProjectBuilder mavenProjectBuilder;

	/** @component */
	private ArtifactInstaller installer;

	private PlexusContainer container;
    
    protected File getConfigDirectory()
    {
      return descriptor.getParentFile();
    }
    
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

            if( resolveDependencies )
            {
            	wam.setMergeJarFiles(false);
            	addAllTransitiveJars(wam);
            }
            else
            {
            	// Just look at our dependent JAR files instead
            	addAllDependentJars(wam);
            }
            
            // List of <merge> nodes to perform, in order
            for(Iterator i = root.getMerges().iterator(); i.hasNext();)
            {
                Merge merge = (Merge)i.next();
                doMerge( wam, merge );
            }

            //WebXml merge = root.getWebXml();
            //doWebXmlMerge(wam, merge);

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
        catch (JDOMException e)
        {
            throw new MojoExecutionException("Xml format exception creating UBERWAR", e);
        }        
        catch (MergeException e)
        {
            throw new MojoExecutionException("Merging exception creating UBERWAR", e);
        }
        
    }

    
    private void doMerge(WarArchiveMerger wam, Merge merge) throws MojoExecutionException
    {
        try
        {
            String type     = merge.getType();
            String file     = merge.getFile();
            String document = merge.getDocument();
            String clazz    = merge.getClassname();

            MergeProcessor merger = null;
            
            if( type != null )
            {
              if( type.equalsIgnoreCase("web.xml") )
              {                
                merger = new MergeWebXml(getConfigDirectory()).create(wam, merge); ;                
              }                
              else if( type.equalsIgnoreCase("xslt") )
              {                
                merger = new MergeXslt(descriptor.getParentFile()).create(wam, merge); ;                
              }      
            }
            else
            {
              merger = (MergeProcessor) Class.forName(clazz).newInstance();
            }                        
              
            if( merger != null )
            {
              if (document != null)
              {
                  merger = new DocumentStreamAdapter(merger);
                  wam.addMergeProcessor(document, merger);
              }
              else if( file != null )
              {                              
                  wam.addMergeProcessor(file, merger);
              }
              
              
              //merger.performMerge();
            }
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("Problem in file merge", e);
        }
    }

    /**
     * Add all JAR files into the WAR file, calculated transitively and resolved in 
     * the normal 'maven' way (I.E if 2 war files contain different versions, resolve to using
     * only *1* version).
     * 
     * @param wam
     * @throws MojoExecutionException
     */
    protected void addAllTransitiveJars(WarArchiveMerger wam) throws MojoExecutionException
    {
    	DependencyCalculator dc = new DependencyCalculator(artifactFactory,resolver,localRepository, remoteRepositories,mavenProject, mavenProjectBuilder, installer, container);
    	
    	try
    	{
    		for(Iterator i = dc.execute().iterator();i.hasNext();)
    		{
    			wam.addMergeItem(i.next());
    		}
    	}
    	catch(Exception ex)
    	{
    		throw new MojoExecutionException("Problem merging dependent JAR files", ex);
    	}
    }
    
    /**
     * Add all the JAR files specified into the merge - these will appear
     * in the WEB-INF/lib directory.
     * @param wam
     * @throws MojoExecutionException 
     */
    protected void addAllDependentJars(WarArchiveMerger wam) throws MojoExecutionException
    {
      for (Iterator iter = getProject().getArtifacts().iterator(); iter.hasNext();)
      {
          Artifact artifact = (Artifact) iter.next();
          System.out.println("See " + artifact); 
          ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);
          if (!artifact.isOptional() && filter.include(artifact))
          {
            String type = artifact.getType();
            
            if ("jar".equals(type))
            {
              System.out.println("use " + artifact );
              try
              {
                  wam.addMergeItem(artifact.getFile());
              }
              catch(MergeException e)
              {
                  throw new MojoExecutionException("Problem merging WAR", e);
              }
              
            }
          }
      }
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

	public void contextualize(Context context) throws ContextException {
		container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
	}

}
