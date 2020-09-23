/*
 * ========================================================================
 *
 * Copyright 2001-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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

import java.lang.reflect.Method;
import java.net.ServerSocket;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.spi.util.DefaultServerRun;

/**
 * Starts/stop Resin by setting up a listener socket. Supports Resin 3.x onwards.
 */
public class ResinRun extends DefaultServerRun
{
    /**
     * Default keepalive socket port for Resin. We create a server socket on this port that acts
     * as a keepalive for Resin. When this socket closes Resin stops. This is a Resin feature.
     */
    public static final int DEFAULT_KEEPALIVE_SOCKET_PORT = 7778;

    /**
     * The started Resin server class. We use <code>Object</code> instead of the Resin class so
     * that we don't need the Resin jars in the classpath to compile this class.
     */
    private Object resinServer;

    /**
     * Reference to the Resin utility class.
     */
    private ResinUtil resinUtil = new ResinUtil();

    /**
     * Resin keepalive socket used to stop Resin (when this socket is closed, Resin is stopped).
     */
    private ServerSocket resinKeepAliveSocket;

    /**
     * @param args the command line arguments
     */
    public ResinRun(String[] args)
    {
        super(args);
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
     * Start the Resin server. We use reflection so that the Resin jars do not need to be in the
     * classpath to compile this class.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void doStartServer(String[] args)
    {
        try
        {
            // Add the Resin "-socketwait" argument to setup a keepalive socket that we will
            // use to stop Resin (this is a Resin feature)
            boolean socketwait = false;
            for (int i = 0; i < args.length; i++)
            {
                if ("-socketwait".equals(args[i]))
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
                        throw new ContainerException("Failed to create keepalive socket", e);
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
            throw new ContainerException("Failed to start Resin server", e);
        }
    }

    /**
     * Stops the Resin server by closing the keepalive socket.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void doStopServer(String[] args)
    {
        try
        {
            resinKeepAliveSocket.close();
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to stop the running Resin server", e);
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
                    throw new ContainerException(
                        "Failed to start Resin: " + e + ", " + ee);
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
                throw new ContainerException("Failed to start Resin " + resinVersion, e);
            }
        }

    }
}
