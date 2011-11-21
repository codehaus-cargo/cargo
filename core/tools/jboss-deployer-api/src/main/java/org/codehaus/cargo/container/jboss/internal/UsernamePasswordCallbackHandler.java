/*
 * ========================================================================
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
package org.codehaus.cargo.container.jboss.internal;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.util.log.Logger;

/**
 * Handler that responds to username and password requests.
 * 
 * @version $Id$
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler
{

    /**
     * Logger.
     */
    private Logger logger;

    /**
     * Username.
     */
    private String username;

    /**
     * Password.
     */
    private String password;

    /**
     * Saves the username and password based on the CARGO {@link Configuration}.
     * @param configuration CARGO {@link Configuration} from which to retrieve the username,
     * password or other data.
     */
    public UsernamePasswordCallbackHandler(Configuration configuration)
    {
        this.logger = configuration.getLogger();
        this.username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        this.password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);
    }

    /**
     * {@inheritDoc}
     * @see CallbackHandler#handle(javax.security.auth.callback.Callback[]) 
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        for (Callback callback : callbacks)
        {
            if (callback instanceof NameCallback)
            {
                if (this.username == null)
                {
                    throw new NullPointerException("User name not set. Please set it using the \""
                        + RemotePropertySet.USERNAME + "\" option.");
                }

                ((NameCallback) callback).setName(this.username);
                this.logger.debug("Responded to a NameCallback", this.getClass().getName());
            }
            else if (callback instanceof PasswordCallback)
            {
                if (this.password == null)
                {
                    throw new NullPointerException("Password not set. Please set it using the \""
                        + RemotePropertySet.PASSWORD + "\" option.");
                }

                ((PasswordCallback) callback).setPassword(this.password.toCharArray());
                this.logger.debug("Responded to a PasswordCallback", this.getClass().getName());
            }
            else
            {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
    
}
