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
package org.codehaus.cargo.container.configuration.script;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;

/**
 * Implementation of general functionality for existing script files.
 */
public class FileScriptCommand extends AbstractScriptCommand
{

    /**
     * Path to configuration script file.
     */
    private String filePath;

    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param filePath Path to configuration script file.
     */
    public FileScriptCommand(Configuration configuration, String filePath)
    {
        super(configuration);

        this.filePath = filePath;
        this.antUtils = new AntUtils();
    }

    /**
     * @return Filtered script.
     */
    @Override
    public String readScript()
    {
        try
        {
            FilterChain filterChain = new FilterChain();
            antUtils.addTokensToFilterChain(filterChain, getConfiguration().getProperties());

            try (BufferedReader primaryReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(this.filePath), StandardCharsets.UTF_8)))
            {
                ChainReaderHelper helper = new ChainReaderHelper();
                helper.setBufferSize(8192);
                helper.setPrimaryReader(primaryReader);
                Vector<FilterChain> filterChains = new Vector<FilterChain>();
                filterChains.add(filterChain);
                helper.setFilterChains(filterChains);
                try (BufferedReader in =
                    new BufferedReader(DefaultFileHandler.getAssembledReader(helper)))
                {
                    String line;
                    StringBuilder out = new StringBuilder();
                    while ((line = in.readLine()) != null)
                    {
                        if (line.isEmpty())
                        {
                            out.append(NEW_LINE);
                        }
                        else
                        {
                            if (out.length() > 0)
                            {
                                out.append(NEW_LINE);
                            }
                            out.append(line);
                        }
                    }
                    return out.toString();
                }
            }
        }
        catch (IOException e)
        {
            throw new CargoException("Error while reading file [" + filePath + "] ", e);
        }
    }
}
