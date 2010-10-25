/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.Logger;

/**
 * Implementation of a Web server that serves one file.
 *
 * @version $Id$
 */
public interface ISimpleHttpFileServer
{

    /**
     * @param handler file handler to use.
     * @param filePath path of the file in the handler.
     */
    void setFile(FileHandler handler, String filePath);

    /**
     * @param listenSocket socket to listen on.
     * @param remoteDeployAddress remote hostname to use in the url, if null it will be obtained
     * from the listenSocket.
     */
    void setListeningParameters(InetSocketAddress listenSocket, String remoteDeployAddress);

    /**
     * @param logger logger to use.
     */
    void setLogger(Logger logger);

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
