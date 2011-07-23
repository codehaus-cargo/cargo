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
package org.codehaus.cargo.tools.jboss;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Handler that responds to username and password requests.
 * 
 * @version $Id$
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler
{

    /**
     * Username.
     */
    private String username;

    /**
     * Password.
     */
    private String password;

    /**
     * Saves the username and password.
     * @param username Username.
     * @param password Password.
     */
    public UsernamePasswordCallbackHandler(String username, String password)
    {
        this.username = username;
        this.password = password;
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
                ((NameCallback) callback).setName(this.username);
            }
            else if (callback instanceof PasswordCallback)
            {
                ((PasswordCallback) callback).setPassword(this.password.toCharArray());
            }
            else
            {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }
    
}
