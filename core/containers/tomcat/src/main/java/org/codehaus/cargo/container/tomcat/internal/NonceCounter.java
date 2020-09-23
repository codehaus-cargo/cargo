/*
 * ========================================================================
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
package org.codehaus.cargo.container.tomcat.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NonceCounter provides a 1,000 item LRU cache counting
 * the number of times a nonce has been seen.
 */
public class NonceCounter
{

    /**
     * LRU cache size limit.
     */
    private final int maxLruCacheSizeLimit = 1000;

    /**
     * Map holds the nonce values and their counts
     */
    private Map<String, Integer> nonces;

    /**
     * Nonce counter.
     */
    public NonceCounter()
    {
        nonces = new LinkedHashMap<String, Integer>(maxLruCacheSizeLimit + 1, .75F, true)
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean removeEldestEntry(Map.Entry<String, Integer> eldest)
            {
                return size() > maxLruCacheSizeLimit;
            }
        };
    }

    /**
     * Count returns a hexadecimal string counting the number
     * of times nonce has been seen.  The first value returned
     * for a nonce is 00000001.
     * 
     * @param nonce the nonce value to count
     * @return formatted nonce value
     */
    public synchronized String count(String nonce)
    {
        Integer count = nonces.get(nonce);
        if (count == null)
        {
            count = 1;
        }
        else
        {
            count = count + 1;
        }

        nonces.put(nonce, count);

        return String.format("%08x", count);
    }
}
