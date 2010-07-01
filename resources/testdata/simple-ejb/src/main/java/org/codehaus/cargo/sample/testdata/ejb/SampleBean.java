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
package org.codehaus.cargo.sample.testdata.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/**
 * Test EJB to verify Cargo can deploy EJB JAR to containers which supports it. 
 *
 * @version $Id$
 */
public class SampleBean implements SessionBean
{
    public boolean isWorking() throws RemoteException
    {
        return true;
    }

    public void ejbCreate() throws CreateException
    {
        // Nothing to do here but this method is required by the spec.
    }

    public void ejbActivate() throws EJBException, RemoteException
    {
        // Nothing to do here but this method is required by the spec.
    }

    public void ejbPassivate() throws EJBException, RemoteException
    {
        // Nothing to do here but this method is required by the spec.
    }

    public void ejbRemove() throws EJBException, RemoteException
    {
        // Nothing to do here but this method is required by the spec.
    }

    public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException
    {
        // Nothing to do here but this method is required by the spec.
    }
}
