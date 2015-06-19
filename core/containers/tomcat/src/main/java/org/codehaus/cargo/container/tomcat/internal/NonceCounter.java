package org.codehaus.cargo.container.tomcat.internal;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NonceCounter provides a 1,000 item LRU cache counting
 * the number of times a nonce has been seen.
 */    
public class NonceCounter {

    /**
     * MAX specifies the LRU cache size limit.
     */
    private final int MAX = 1000;

    /**
     * Map holds the nonce values and their counts
     */
    private Map<String, Integer> nonces;

    public NonceCounter() {
        nonces = new LinkedHashMap<String, Integer>(MAX+1, .75F, true) {
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX;
            }
        };
    }

    /**
     * Count returns a hexadecimal string counting the number
     * of times nonce has been seen.  The first value returned
     * for a nonce is 00000001.
     *
     * @param nonce the nonce value to count
     */
    public synchronized String Count(String nonce) {

        Integer count = nonces.get(nonce);
        if (count == null) {
            count = Integer.valueOf(1);
        } else {
            count = Integer.valueOf(count.intValue() + 1);
        }

        nonces.put(nonce, count);

        return String.format("%08x", count.intValue());
    }
}
