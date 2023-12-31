/*
 * ========================================================================
 *
 * Copyright 2001-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.resin.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts/stop Resin by setting up a listener socket. Supports Resin 3.x onwards.<br>
 * <br>
 * When this application is first called to start the server, a listener socket is set up. Then,
 * when it is later called to stop the server, we connect to the listener socket and tell the
 * server to stop.
 */
public class ResinRun extends Thread
{
    /**
     * Default keepalive socket port for Resin. We create a server socket on this port that acts
     * as a keepalive for Resin. When this socket closes Resin stops. This is a Resin feature.
     */
    public static final int DEFAULT_KEEPALIVE_SOCKET_PORT = 7778;

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
     * Resin keepalive socket used to stop Resin (when this socket is closed, Resin is stopped).
     */
    private ServerSocket resinKeepAliveSocket;

    /**
     * @param args the command line arguments
     */
    public ResinRun(String[] args)
    {
        this.args = args;
    }

    /**
     * Entry point to start/stop the Resin server.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        ResinRun resin = new ResinRun(args);

        resin.doRun();
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
            throw new ResinException("Error starting server", e);
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
            throw new ResinException(
                "Error opening socket to [" + this.host + ":" + this.port + "]", e);
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
                throw new ResinException("Cannot close client socket", e);
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
            throw new ResinException(
                "Error accepting connection for server socket [" + serverSocket + "]", e);
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
                throw new ResinException(
                    "Cannot close server socket [" + serverSocket + "]", e);
            }
        }

        // Stop server
        try
        {
            this.doStopServer(this.args);
        }
        catch (Exception e)
        {
            throw new ResinException("Cannot stop server", e);
        }

        // Stop server socket
        try
        {
            serverSocket.close();
        }
        catch (IOException e)
        {
            throw new ResinException("Cannot close server socket [" + serverSocket + "]", e);
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
            throw new ResinException("Error setting up the server listener socket", e);
        }

        return serverSocket;
    }

    /**
     * Start the Resin server. We use reflection so that the Resin jars do not need to be in the
     * classpath to compile this class.
     * 
     * @param args the command line arguments
     */
    protected void doStartServer(String[] args)
    {
        try
        {
            // Add the Resin "-socketwait" argument to setup a keepalive socket that we will
            // use to stop Resin (this is a Resin feature)
            boolean socketwait = false;
            for (String arg : args)
            {
                if ("-socketwait".equals(arg))
                {
                    socketwait = true;
                    break;
                }
            }

            final String[] modifiedArgs;
            if (socketwait)
            {
                modifiedArgs = args;
            }
            else
            {
                modifiedArgs = new String[args.length + 2];
                System.arraycopy(args, 0, modifiedArgs, 0, args.length);
                modifiedArgs[args.length] = "-socketwait";
                modifiedArgs[args.length + 1] =
                    Integer.toString(DEFAULT_KEEPALIVE_SOCKET_PORT);
            }

            // Create a server sockets that acts as a keepalive for Resin.
            // When this socket closes Resin stops.
            Thread keepaliveThread = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // Add the Resin "-socketwait" argument to setup a keepalive socket that we
                        // will use to stop Resin (this is a Resin feature)
                        int socketwait = -1;
                        for (int i = 0; i < modifiedArgs.length; i++)
                        {
                            if ("-socketwait".equals(modifiedArgs[i]))
                            {
                                socketwait = Integer.parseInt(modifiedArgs[i + 1]);
                                break;
                            }
                        }

                        // Note: We must not call accept() here as Resin is trying to connect with
                        // us in its waitForExit() loop and if we do, Resin will exit before we
                        // tell it to do so!
                        resinKeepAliveSocket = new ServerSocket(socketwait);
                    }
                    catch (Exception e)
                    {
                        throw new ResinException("Failed to create keepalive socket", e);
                    }
                }
            };
            keepaliveThread.start();

            // Start the server in another thread so that it doesn't block the current thread.
            Thread startThread = new ResinInvoker(args);
            startThread.start();
        }
        catch (Exception e)
        {
            throw new ResinException("Failed to start Resin server", e);
        }
    }

    /**
     * Stops the Resin server by closing the keepalive socket.
     * 
     * @param args the command line arguments
     */
    protected void doStopServer(String[] args)
    {
        try
        {
            resinKeepAliveSocket.close();
        }
        catch (Exception e)
        {
            throw new ResinException("Failed to stop the running Resin server", e);
        }
    }

    /**
     * Used to start the server in another thread so that it doesn't block the current thread.
     */
    private class ResinInvoker extends Thread
    {
        /**
         * The command line arguments.
         */
        private String[] args;

        /**
         * {@inheritDoc}
         * @see ResinRun#ResinRun(String[])
         */
        public ResinInvoker(String[] args)
        {
            this.args = args;
        }

        /**
         * {@inheritDoc}
         * @see Thread#run()
         */
        @Override
        public void run()
        {
            Class resinClass;
            String resinVersion = "3.x";

            try
            {
                resinClass = Class.forName("com.caucho.server.http.ResinServer");
            }
            catch (ClassNotFoundException e)
            {
                try
                {
                    resinClass = Class.forName("com.caucho.server.resin.Resin");
                }
                catch (ClassNotFoundException ee)
                {
                    throw new ResinException("Failed to start Resin: " + e + ", " + ee);
                }

                resinVersion = "3.1.x or 4.x";
            }

            try
            {
                Method mainMethod = resinClass.getMethod("main", String[].class);
                mainMethod.invoke(null, new Object[] {args});
            }
            catch (Exception e)
            {
                throw new ResinException("Failed to start Resin " + resinVersion, e);
            }
        }

    }
}
