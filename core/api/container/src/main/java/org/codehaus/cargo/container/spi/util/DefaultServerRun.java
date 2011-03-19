/*
 * ========================================================================
 *
 * Copyright 2001-2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.spi.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.ContainerException;

/**
 * Helper class to start/stop a container. When this application is first called to start the
 * server, a listener socket is set up. Then, we it is later called to stop the server, we connect
 * to the listener socket and tell the server to stop.
 * 
 * @version $Id$
 */
public class DefaultServerRun extends Thread
{
    /**
     * Internal socket port that we use to stop the server. To change the default value pass
     * <code>"-port newPortValue"</code> in the args list specified in this class's constructor.
     */
    private int port = 7777;

    /**
     * Host name. We assume the server is started and stoppped in the same local machine.
     */
    private String host = "127.0.0.1";

    /**
     * The command line arguments.
     */
    private String[] args;

    /**
     * Flag that specifies if the server is already started to prevent starting it if it is.
     */
    private boolean isStarted;

    /**
     * @param theArgs the command line arguments
     */
    public DefaultServerRun(String[] theArgs)
    {
        this.args = theArgs;
    }

    /**
     * Starts the server (in a blocking mode) and set up a socket listener.
     * 
     * @param theArgs the command line arguments
     * @exception Exception if any error happens when starting the server
     */
    protected void doStartServer(String[] theArgs) throws Exception
    {
        // Voluntarily do nothing by default
    }

    /**
     * Stops the server by connecting to the socket set up when the server was started.
     * 
     * @param theArgs the command line arguments
     * @exception Exception if any error happens when stopping the server
     */
    protected void doStopServer(String[] theArgs) throws Exception
    {
        // Voluntarily do nothing by default
    }

    /**
     * Parse and process the command line to start/stop the server.
     */
    protected final void doRun()
    {
        // Look for a -start or -stop flag
        boolean isStart = true;
        List<String> newArgs = new ArrayList<String>();

        for (int i = 0; i < this.args.length; i++)
        {
            if (this.args[i].equalsIgnoreCase("-start"))
            {
                isStart = true;
            }
            else if (this.args[i].equalsIgnoreCase("-stop"))
            {
                isStart = false;
            }
            else if (this.args[i].equalsIgnoreCase("-port"))
            {
                this.port = Integer.parseInt(this.args[i + 1]);
                i++;
            }
            else
            {
                newArgs.add(this.args[i]);
            }
        }

        // Remove the command line arguments that should not be part of the
        // server command line (i.e. our own arguments).
        String[] strArgs = new String[0];

        this.args = newArgs.toArray(strArgs);

        if (isStart)
        {
            startServer();
        }
        else
        {
            stopServer();
        }
    }

    /**
     * Starts the server.
     */
    private void startServer()
    {
        // If the server is already started, do nothing
        if (this.isStarted)
        {
            return;
        }

        try
        {
            doStartServer(this.args);
        }
        catch (Exception e)
        {
            throw new ContainerException("Error starting server", e);
        }

        // Server is now started
        this.isStarted = true;

        // Start a socket listener that will listen for stop commands.
        start();
    }

    /**
     * Stops the running server.
     */
    private void stopServer()
    {
        // Open socket connection
        Socket clientSocket = null;

        try
        {
            clientSocket = new Socket(this.host, this.port);
        }
        catch (Exception e)
        {
            throw new ContainerException("Error opening socket to [" + this.host + ":" + this.port
                + "]", e);
        }
        finally
        {
            try
            {
                if (clientSocket != null)
                {
                    clientSocket.close();
                }
            }
            catch (IOException e)
            {
                throw new ContainerException("Cannot close client socket", e);
            }
        }
    }

    /**
     * Sets up a listener socket and wait until we receive a request on it to stop the running
     * server.
     */
    @Override
    public void run()
    {
        ServerSocket serverSocket = setUpListenerSocket();

        // Accept a client socket connection
        try
        {
            serverSocket.accept();
        }
        catch (IOException e)
        {
            throw new ContainerException("Error accepting connection for server socket ["
                + serverSocket + "]", e);
        }
        finally
        {
            // Stop server socket
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                throw new ContainerException("Cannot close server socket [" + serverSocket + "]",
                    e);
            }
        }

        // Stop server
        try
        {
            this.doStopServer(this.args);
        }
        catch (Exception e)
        {
            throw new ContainerException("Cannot stop server", e);
        }

        // Stop server socket
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            throw new ContainerException("Cannot close server socket [" + serverSocket + "]", e);
        }
    }

    /**
     * Sets up the listener socket.
     * 
     * @return the listener socket that has been set up
     */
    private ServerSocket setUpListenerSocket()
    {
        ServerSocket serverSocket;

        try
        {
            serverSocket = new ServerSocket(this.port);
        }
        catch (IOException e)
        {
            throw new ContainerException("Error setting up the server listener socket", e);
        }

        return serverSocket;
    }
}
