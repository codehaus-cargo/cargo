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
package org.codehaus.cargo.documentation;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Generate project structure documentation using Confluence markup language. The generated text is
 * meant to be copied on the Cargo Confluence web site. Warning: Uses a relative path to determine
 * the base cargo directory and the sandbox is hard-coded.
 * 
 * @version $Id: ConfluenceProjectStructureDocumentationGenerator.java 2407 2010-08-06 14:11:02Z
 * alitokmen $
 */
public class ConfluenceProjectStructureDocumentationGenerator
{
    /** The MavenXpp3Reader used to get project info from pom files. */
    private static final MavenXpp3Reader POM_READER = new MavenXpp3Reader();

    /** Relative path to the cargo-trunks base directory. */
    private static final String PROJECT_BASE = System.getProperty("basedir") + "/../../";

    /** Constant for known POM file name. */
    private static final String POM = "/pom.xml";

    /** Constant holding the value of the line.separator system property. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Opening tag for the red markup. */
    private static final String START_COLOR = "{color:red}{*}";

    /** Closing tag for color markup. */
    private static final String END_COLOR = "*{color}";

    /** Constant for the "*" character. */
    private static final String ASTERISK = "*";

    /**
     * Generates the project structure documentation.
     * @return the project structure documentation to write to file.
     * @throws Exception if something unexpected occurs while generating documentation.
     */
    public String generateDocumentation() throws Exception
    {
        StringBuilder markup = new StringBuilder();
        markup.append(getPageHeader());

        // create markup for the base maven build and all modules.
        File baseDir = new File(PROJECT_BASE).getCanonicalFile();
        File basePom = new File(baseDir, POM);
        MavenProject baseProject = createProjectFromPom(basePom);
        markup.append(getProjectInfo(baseProject, 1));

        // create markup for the non-maven sandbox directory not found via module discovery.
        markup.append("** *sandbox/* : Base directory for cargo plugin projects");
        markup.append(LINE_SEPARATOR);
        markup.append("*** *intellijidea/* : IntelliJ IDEA plugin");
        markup.append(LINE_SEPARATOR);
        markup.append("*** *netbeans/* : Netbeans plugin");
        markup.append(LINE_SEPARATOR);

        return markup.toString();
    }

    /**
     * Creates the markup for the page header which includes the intro text and legend.
     * @return the markup for the page header which includes the intro text and legend.
     */
    private String getPageHeader()
    {
        StringBuilder markup = new StringBuilder();
        markup.append("Cargo's directory organization can be daunting for a newcomer. "
                + "So here's some information on how the project is organized.");
        markup.append(LINE_SEPARATOR);
        markup.append("{info:title=Legend}");
        markup.append(LINE_SEPARATOR);
        markup.append("* *directory/* : represents a directory");
        markup.append(LINE_SEPARATOR);
        markup
            .append("* {color:red}{*}directory/*{color} : represents a directory containing a Maven project");
        markup.append(LINE_SEPARATOR);
        markup.append("{info}");
        markup.append(LINE_SEPARATOR);

        return markup.toString();
    }

    /**
     * Writes the current MavenProject information.
     * @param aProject the MavenProject we are currently documenting.
     * @param treeIndex the current project level.
     * @return the markup for the given MavenProject.
     */
    private String getProjectInfo(MavenProject aProject, int treeIndex)
    {
        StringBuilder markup = new StringBuilder("");

        markup.append(ASTERISK);
        markup.append(" ");
        markup.append(START_COLOR);
        markup.append(aProject.getFile().getParentFile().getName());
        markup.append("/");
        markup.append(END_COLOR);

        String description = aProject.getDescription() != null ? aProject.getDescription()
            : aProject.getName();
        markup.append(": ");
        markup.append(description);
        markup.append(LINE_SEPARATOR);

        markup.append(getModuleTree(aProject, treeIndex));

        return markup.toString();
    }

    /**
     * Creates the wiki markup for any Modules found for the given MavenProject instance.
     * @param aProject the current MavenProject we are analyzing for modules.
     * @param treeIndex the current project level in the source tree.
     * @return wiki markup for the given MavenProject's modules.
     */
    private String getModuleTree(MavenProject aProject, int treeIndex)
    {
        StringBuilder markup = new StringBuilder();
        List modules = aProject.getModules();
        int newTreeIndex = modules.size() > 0 ? treeIndex + 1 : treeIndex;
        for (Object moduleArtifactId : modules)
        {
            File moduleDirectory = new File(aProject.getFile().getParent(),
                (String) moduleArtifactId);
            MavenProject moduleProject = createProjectFromPom(new File(moduleDirectory, POM));
            for (int i = 0; i < treeIndex; i++)
            {
                markup.append(ASTERISK);
            }
            markup.append(getProjectInfo(moduleProject, newTreeIndex));
        }
        return markup.toString();
    }

    /**
     * Creates a MavenProject from a given POM.XML file.
     * @param pom the POM.XML file to create a MavenProject from.
     * @return a MavenProject represented by the given POM.XML file.
     */
    private MavenProject createProjectFromPom(File pom)
    {
        Model model = new Model();
        try
        {
            model = POM_READER.read(new FileReader(pom));
        }
        catch (IOException ioe)
        {
            System.out.println("Caught Exception reading pom.xml: " + ioe);
        }
        catch (XmlPullParserException ppe)
        {
            System.out.println("Caught Exception reading pom.xml: " + ppe);
        }

        MavenProject project = new MavenProject(model);
        project.setFile(pom);

        return project;
    }

}
