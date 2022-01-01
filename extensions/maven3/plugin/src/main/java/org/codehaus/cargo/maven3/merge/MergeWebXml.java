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
package org.codehaus.cargo.maven3.merge;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.maven3.Merge;
import org.codehaus.cargo.module.merge.DescriptorMergerByTag;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.merge.tagstrategy.ChooseByNameMergeStrategy;
import org.codehaus.cargo.module.merge.tagstrategy.MergeStrategy;
import org.codehaus.cargo.module.merge.tagstrategy.NodeMergeStrategy;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.merge.WarArchiveMerger;
import org.codehaus.cargo.module.webapp.merge.WebXmlMerger;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Merge processor designed for web.xml files.
 */
public class MergeWebXml implements MergeProcessorFactory
{
    /**
     * <code>web.xml</code> merger.
     */
    private WebXmlMerger webXmlMerger;

    /**
     * Configuration directory.
     */
    private File configDirectory;

    /**
     * Saves attributes.
     * @param configDirectory Configuration directory.
     */
    public MergeWebXml(File configDirectory)
    {
        this.configDirectory = configDirectory;
    }

    /**
     * @return the configDirectory
     */
    public File getConfigDirectory()
    {
        return this.configDirectory;
    }

    /**
     * @param configDirectory the configDirectory to set
     */
    public void setConfigDirectory(File configDirectory)
    {
        this.configDirectory = configDirectory;
    }

    /**
     * {@inheritDoc}
     * @param wam WAR archive merger.
     * @param xml XML merge.
     * @return Merge processor.
     */
    @Override
    public MergeProcessor create(WarArchiveMerger wam, Merge xml)
    {
        webXmlMerger = wam.getWebXmlMerger();

        Xpp3Dom parameters = (Xpp3Dom) xml.getParameters();

        Xpp3Dom defaultNode = parameters.getChild("default");

        if (defaultNode != null)
        {
            Xpp3Dom[] tags = defaultNode.getChildren("tag");
            for (Xpp3Dom tag : tags)
            {
                String tagName = tag.getAttribute("name");
                Xpp3Dom strategy = tag.getChild("strategy");
                MergeStrategy ms = makeStrategy(strategy);

                webXmlMerger.setMergeStrategy(tagName, ms);
            }
        }
        return null;
    }

    /**
     * Create the merge strategy.
     * @param config {@link Xpp3Dom} configuration.
     * @return Merge strategy with the given configuration.
     */
    protected MergeStrategy makeStrategy(Xpp3Dom config)
    {
        if (!config.getName().equals("strategy"))
        {
            throw new CargoException("You must specify a merge strategy");
        }

        String strategyName = config.getAttribute("name");
        String strategyFile = config.getAttribute("file");

        if (strategyName.equalsIgnoreCase("Preserve"))
        {
            return DescriptorMergerByTag.PRESERVE;
        }
        else if (strategyName.equalsIgnoreCase("Overwrite"))
        {
            return DescriptorMergerByTag.OVERWRITE;
        }
        else if (strategyName.equalsIgnoreCase("ChooseByName"))
        {
            Xpp3Dom def = config.getChild("default").getChild(0);

            ChooseByNameMergeStrategy cbnms = new ChooseByNameMergeStrategy(makeStrategy(def));

            Xpp3Dom[] items = config.getChildren();
            for (Xpp3Dom item : items)
            {
                if (item.getName().equals("choice"))
                {
                    cbnms.addStrategyForName(item.getAttribute("name"),
                        makeStrategy(item.getChild(0)));
                }
            }
            return cbnms;
        }
        if (strategyName.equalsIgnoreCase("NodeMerge"))
        {
            try
            {
                if (strategyFile != null)
                {
                    File f = new File(getConfigDirectory(), strategyFile);
                    WebXml webXml = WebXmlIo.parseWebXml(new FileInputStream(f), null);
                    return new NodeMergeStrategy(webXml.getDescriptorType(),
                        webXml.getRootElement());
                }
                else
                {
                    String theXml = config.getChild(0).toString();
                    WebXml webXml = WebXmlIo.parseWebXml(
                        new ByteArrayInputStream(theXml.getBytes(StandardCharsets.UTF_8)), null);
                    return new NodeMergeStrategy(webXml.getDescriptorType(),
                        webXml.getRootElement());
                }
            }
            catch (Exception e)
            {
                throw new CargoException("Problem generating Node Merge strategy", e);
            }
        }

        throw new CargoException("Must provide a known strategy type (don't understand "
            + strategyName + ")");
    }

}
