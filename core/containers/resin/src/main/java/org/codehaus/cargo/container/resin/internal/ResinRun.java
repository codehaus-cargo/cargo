/* 
 * ========================================================================
 * 
 * Copyright 2001-2004 The Apache Software Foundation. Code from this file 
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
package org.codehaus.cargo.container.resin.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.spi.util.DefaultServerRun;

/**
 * Starts/stop Resin by setting up a listener socket. Supports Resin 2.0.x, 2.1.x and 3.x.
 *
 * @version $Id$
 */
public class ResinRun extends DefaultServerRun
{
    /**
     * Default keepalive socket port for Resin 3.x. We create a server socket on this port that
     * acts as a keepalive for Resin. When this socket closes Resin stops. This is a Resin feature.
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
     * Resin 3.x keepalive socket used to stop Resin (when this socket is closed, Resin is stopped).
     */
    private ServerSocket resin3xKeepAliveSocket;

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
     * @see DefaultServerRun#doStartServer
     */
    protected final void doStartServer(String[] args)
    {
        try
        {
            if (isResinVersion("2.0"))
            {
                startResin20x(args);
            }
            else if (isResinVersion("2.1"))
            {
                startResin21x(args);
            }
            else if (isResinVersion("3"))
            {
                startResin3x(args);
            }
            else
            {
                throw new ContainerException("Unsupported Resin version ["
                    + this.resinUtil.getResinVersion() + "]");
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to start Resin server", e);
        }
    }

    /**
     * Starts Resin 2.0.x.
     *
     * @param args the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void startResin20x(String[] args) throws Exception
    {
        Class resinClass = Class.forName("com.caucho.server.http.ResinServer");
        Constructor constructor = resinClass.getConstructor(
            new Class[] {args.getClass(), boolean.class});

        this.resinServer = constructor.newInstance(new Object[] {args, Boolean.TRUE});
    
        Method initMethod = this.resinServer.getClass().getMethod("init",
            new Class[] {boolean.class});

        initMethod.invoke(this.resinServer, new Object[] {Boolean.TRUE});
    }

    /**
     * Starts Resin 2.1.x.
     *
     * @param args the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void startResin21x(String[] args) throws Exception
    {
        Class resinClass = 
            Class.forName("com.caucho.server.http.ResinServer");
        Constructor constructor = resinClass.getConstructor(
            new Class[] {args.getClass(), boolean.class});

        this.resinServer = constructor.newInstance(new Object[] {args, Boolean.TRUE});
        
        Method initMethod = this.resinServer.getClass().getMethod("init",
            new Class[] {ArrayList.class});

        initMethod.invoke(this.resinServer, new Object[] {null});
    }

    /**
     * Starts Resin 3.x.
     *
     * @return the thread in which the server has been started
     * @param args the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private Thread startResin3x(final String[] args) throws Exception
    {
        // Create a server sockets that acts as a keepalive for Resin. When this socket closes
        // Resin stops.
        Thread keepaliveThread = new Thread()
        {
            public void run()
            {
                try
                {
                    // Note: We must not call accept() here as Resin is trying to connect with
                    // us in its waitForExit() loop and if we do, Resin will exit before we tell
                    // it to do so!
                    resin3xKeepAliveSocket = new ServerSocket(DEFAULT_KEEPALIVE_SOCKET_PORT);
                }                
                catch (Exception e)
                {
                    throw new ContainerException("Failed to create keepalive socket", e);
                }
            }
        };
        keepaliveThread.start();
        
        // Start the server in another thread so that it doesn't block
        // the current thread. It seems that Resin 3.x is acting differently
        // than Resin 2.x which was not blocking and thus which did not need
        // to be started in a separate thread.
        Thread startThread = new Resin3xInvoker(args);
        startThread.start();
        
        return startThread;
    }
    
    /**
     * Stops the Resin server. We use reflection so that the Resin jars do not need to be in the 
     * classpath to compile this class.
     * 
     * {@inheritDoc}
     * @see DefaultServerRun#doStopServer
     */
    protected final void doStopServer(String[] args)
    {
        try
        {
            if (isResinVersion("2.0"))
            {
                stopResin20x(args);
            }
            else if (isResinVersion("2.1"))
            {
                stopResin20x(args);
            }
            else if (isResinVersion("3"))
            {
                stopResin3x(args);
            }
            else
            {
                throw new ContainerException("Unsupported Resin version ["
                    + this.resinUtil.getResinVersion() + "]");
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to stop the running Resin server", e);
        }
    }
    
    /**
     * Stops Resin 2.0.x and 2.1.x versions.
     *
     * @param args the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void stopResin20x(String[] args) throws Exception
    {
        Method closeMethod = this.resinServer.getClass().getMethod("close", null);

        closeMethod.invoke(this.resinServer, null);
    }

    /**
     * Stops Resin 3.x.
     *
     * @param args the command line arguments for starting the server
     * @throws Exception if an error happens when starting the server
     */
    private void stopResin3x(String[] args) throws Exception
    {
        // Stop Resin by closing the keepalive socket.
        resin3xKeepAliveSocket.close();
    }
    
    /**
     * @param versionPrefix the version prefix to test for
     * @return true if the Resin version starts with versionPrefix
     */
    private boolean isResinVersion(String versionPrefix)
    {
        return this.resinUtil.getResinVersion().startsWith(versionPrefix);
    }

    /**
     * Used to start the server in another thread so that it doesn't block the current thread.
     * It seems that Resin 3.x is acting differently than Resin 2.x which was not blocking and
     * thus which did not need to be started in a separate thread.
     */
    private class Resin3xInvoker extends Thread
    {
        /**
         * The command line arguments.
         */
        private String[] args;

        /**
         * {@inheritDoc}
         * @see ResinRun#ResinRun(String[])
         */
        public Resin3xInvoker(String[] args)
        {
            this.args = args;
        }

        /**
         * {@inheritDoc}
         * @see Thread#run()
         */
        public void run()
        {
            try
            {
                Class resinClass = Class.forName("com.caucho.server.http.ResinServer");

                Method mainMethod = resinClass.getMethod("main",
                    new Class[] {String[].class});

                // Add the Resin "-socketwait" argument to setup a keepalive socket that we will
                // use to stop Resin (this is a Resin feature)
                String[] modifiedArgs = new String[args.length + 2];
                for (int i = 0; i < args.length; i++)
                {
                    modifiedArgs[i] = args[i];
                }
                modifiedArgs[args.length] = "-socketwait";
                modifiedArgs[args.length + 1] = "" + DEFAULT_KEEPALIVE_SOCKET_PORT;

                mainMethod.invoke(null, new Object[] {modifiedArgs});
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to start Resin 3.x", e);
            }
        }

    }
}
