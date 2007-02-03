/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.container.internal;

/**
 * All methods that a container that can be started/stopped must implement.
 * 
 * @version $Id$
 */
public interface RunnableContainer
{
    /**
     * Starts the container. It blocks until the container is fully started unless it doesn't start 
     * before the timeout period in which case an exception will be thrown. 
     */
    void start();

    /**
     * Stops the container. It blocks until the container is fully stopped unless it doesn't stop 
     * before the timeout period in which case an exception will be thrown. 
     */
    void stop();

    /**
     * @param output the file to which the container's output will be logged to. Note that we're
     *        passing a String instead of a File because we want to leave the possibility of using
     *        URIs for specifying the home location.
     */
    void setOutput(String output);

    /**
     * @return the file to which the container's output will be logged to. Note that we're returning
     *         a String instead of a File because we want to leave the possibility of using URIs for
     *         specifying the home location.
     */
    String getOutput();

    /**
     * @param timeout the timeout (in ms) after which we consider the container cannot be started 
     *        or stopped.
     */
    void setTimeout(long timeout);

    /**
     * @return the timeout (in ms) after which we consider the container cannot be started or  
     *         or stopped.
     */
    long getTimeout();

    /**
     * Sets whether output of the container should be appended to an existing file, or the existing 
     * file should be truncated.
     * 
     * @param shouldAppend Whether output should be appended to or not
     */
    void setAppend(boolean shouldAppend);

    /**
     * @return true if the output of the container should be appended to the output file or false
     *         otherwise 
     */
    boolean isAppend();
}
