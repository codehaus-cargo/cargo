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
package org.codehaus.cargo.container.jboss.internal;

import java.net.InetSocketAddress;
import java.net.URL;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.Logger;

/**
 * Implementation of a Web server that serves one file.
 */
public interface ISimpleHttpFileServer
{

    /**
     * @param fileHandler file handler to use.
     */
    void setFileHandler(FileHandler fileHandler);

    /**
     * @param logger logger to use.
     */
    void setLogger(Logger logger);

    /**
     * @param deployable deployable to handle.
     * @param keepOriginalWarFilename whether to keep the original file name, see
     * {@link JBossPropertySet#KEEP_ORIGINAL_WAR_FILENAME} for details.
     */
    void setFile(Deployable deployable, String keepOriginalWarFilename);

    /**
     * @param listenSocket socket to listen on.
     * @param remoteDeployAddress remote hostname to use in the url, if null it will be obtained
     * from the listenSocket.
     */
    void setListeningParameters(InetSocketAddress listenSocket, String remoteDeployAddress);

    /**
     * @return url this server serves.
     */
    URL getURL();

    /**
     * starts the server.
     */
    void start();

    /**
     * @return the number of successful calls received.
     */
    int getCallCount();

    /**
     * @return exception, if any occured.
     */
    Throwable getException();

    /**
     * stops the server.
     */
    void stop();

}
