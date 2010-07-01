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
package org.codehaus.cargo.container.property;

/**
 * Represents the transactional support of a resource, such as a <code>XA_TRANSACTION</code>.
 * 
 * @version $Id$
 */
public final class TransactionSupport
{

    /**
     * Indicates lack of transaction support.
     */
    public static final TransactionSupport NO_TRANSACTION =
        new TransactionSupport("NO_TRANSACTION");

    /**
     * Indicates support of container-managed transactions.
     */
    public static final TransactionSupport LOCAL_TRANSACTION =
        new TransactionSupport("LOCAL_TRANSACTION");

    /**
     * Indicates support of distributed transactions using XA protocol.
     */
    public static final TransactionSupport XA_TRANSACTION =
        new TransactionSupport("XA_TRANSACTION");

    /**
     * string representation of the transaction support this object represents.
     */
    private final String transactionSupport;

    /**
     * This class is a JDK 1.4 typesafe enum. That is why this constructor is private.
     * 
     * @param transactionSupport transaction support designated.
     */
    private TransactionSupport(String transactionSupport)
    {
        this.transactionSupport = transactionSupport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return transactionSupport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof TransactionSupport)
        {
            return toString().equals(((TransactionSupport) obj).toString());
        }
        else
        {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return transactionSupport.hashCode();
    }
}
