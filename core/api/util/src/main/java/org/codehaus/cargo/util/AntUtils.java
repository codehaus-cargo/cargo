/*
 * ========================================================================
 *
 * Copyright 2003-2008 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.util;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.FilterChain;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

/**
 * Set of common Ant utility methods.
 *
 * @version $Id$
 */
public class AntUtils
{
    /**
     * The factory for creating ant tasks.
     */
    private AntTaskFactory antTaskFactory;

    /**
     * Uses the {@link DefaultAntTaskFactory} class when creating Ant
     * tasks.
     */
    public AntUtils()
    {
        this.antTaskFactory = new DefaultAntTaskFactory(createProject());
    }

    /**
     * @param factory Ant task factory class used when creating Ant tasks
     */
    public AntUtils(AntTaskFactory factory)
    {
        this.antTaskFactory = factory;
    }

    /**
     * Creates and returns a new instance of the Ant task mapped to the
     * specified logical name.
     *
     * @param taskName The logical name of the task to create
     * @return A new instance of the task
     */
    public final Task createAntTask(String taskName)
    {
        return this.antTaskFactory.createTask(taskName);
    }

    /**
     * Convenience method to create an Ant environment variable that points to
     * a file.
     *
     * @param key The key or name of the variable
     * @param file The file the variable should point to
     * @return The created environment variable
     */
    public final Environment.Variable createSysProperty(String key, File file)
    {
        Environment.Variable var = new Environment.Variable();
        var.setKey(key);
        var.setFile(file);
        return var;
    }

    /**
     * Convenience method to create an Ant environment variable that contains
     * a path.
     *
     * @param key The key or name of the variable
     * @param path The path
     * @return The created environment variable
     */
    public final Environment.Variable createSysProperty(String key, Path path)
    {
        Environment.Variable var = new Environment.Variable();
        var.setKey(key);
        var.setPath(path);
        return var;
    }

    /**
     * Convenience method to create an Ant environment variable that contains a
     * string.
     *
     * @param key The key or name of the variable
     * @param value The value
     * @return The created environment variable
     */
    public final Environment.Variable createSysProperty(String key, String value)
    {
        Environment.Variable var = new Environment.Variable();
        var.setKey(key);
        var.setValue(value);
        return var;
    }

    /**
     * <p>Convenience method to create an Ant environment variable that contains a
     * string from an URI.
     * <p><b>Note</b> that {@link java.net.URI#getPath()} will be used.
     *
     * @param key The key or name of the variable
     * @param value The URI to take the value from; {@link java.net.URI#getPath()} will be used
     * @return The created environment variable
     */
    public final Environment.Variable createSysProperty(String key, URI value)
    {
        Environment.Variable var = new Environment.Variable();
        var.setKey(key);
        var.setValue(value.getPath());
        return var;
    }

    /**
     * @return a default empty Ant {@link org.apache.tools.ant.Project }
     */
    public Project createProject()
    {
        Project defaultProject = new Project();
        defaultProject.init();

        return defaultProject;
    }

    /**
     * Add a token to an existing filter chain.
     *
     * @param filterChain the filter chain to augment
     * @param key the token key
     * @param value the token value
     */
    public void addTokenToFilterChain(FilterChain filterChain, String key,
        String value)
    {
        ReplaceTokens replaceToken = new ReplaceTokens();
        ReplaceTokens.Token token = new ReplaceTokens.Token();
        token.setKey(key);
        token.setValue(value);
        replaceToken.addConfiguredToken(token);
        filterChain.addReplaceTokens(replaceToken);
    }
  
    /**
     * Add the map of tokens to the filterChain.
     * 
     * @param filterChain The filterchain to use
     * @param map The map
     */
    public void addTokensToFilterChain(FilterChain filterChain, Map map)
    {
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            String key = (String) iterator.next();
            String value = (String) map.get(key);
            addTokenToFilterChain(filterChain, key, value);
        }
    }

}
