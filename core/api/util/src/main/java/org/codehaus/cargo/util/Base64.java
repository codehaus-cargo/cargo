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
package org.codehaus.cargo.util;


/**
 * Provides utility methods to Base64 encode data. This class uses the Base64 encoding as specified
 * in RFC 2045, 6.8. Base64 Content-Transfer-Encoding.
 * 
 * @version $Id$
 */
public final class Base64
{
    // constants --------------------------------------------------------------

    /**
     * The Base64 character set look-up table. This consists of the following ordered alphanumerics:
     * A-Z, a-z, 0-9, + and /.
     */
    private static final char[] ENCODE = 
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray();


    /**
     * The character to pad the output with if not a multiple of 24-bits.
     */
    private static final char PAD_CHAR = '=';

    // constructors -----------------------------------------------------------

    /**
     * Private to prevent unnecessary instantation.
     */
    private Base64()
    {
        // Private to prevent unnecessary instantation
    }

    // public methods ---------------------------------------------------------

    /**
     * Base64 encodes the specified bytes. This method is provided for signature compatibility with
     * commons-codec.
     * 
     * @param bytes the bytes to encode
     * @return the encoded bytes
     */
    public static byte[] encodeBase64(byte[] bytes)
    {
        return encode(bytes);
    }

    /**
     * Base64 encodes the specified string using the platform's default encoding.
     * 
     * @param string the string to encode
     * @return the encoded string
     */
    public static String encode(String string)
    {
        return new String(encode(string.getBytes()));
    }

    /**
     * Base64 encodes the specified bytes.
     * 
     * @param bytes the bytes to encode
     * @return the encoded bytes
     */
    public static byte[] encode(byte[] bytes)
    {
        return encodeToString(bytes).getBytes();
    }

    /**
     * Base64 encodes the specified bytes to a String.
     * 
     * @param bytes the bytes to encode
     * @return the encoded bytes as a string
     */
    public static String encodeToString(final byte[] bytes)
    {
        final int length = bytes.length;
        final StringBuilder buffer = new StringBuilder(length * 3);
        for (int i = 0; i < length; i += 3) 
        {
            // p's are the segments for each byte. For every three bytes there are 6
            // segments
            int p0 = bytes[i] & 0xFC;
            p0 >>= 2;
            int p1 = bytes[i] & 0x03;
            p1 <<= 4;

            int p2;
            int p3;
            if (i + 1 < length) 
            {
                p2 = bytes[i + 1] & 0xF0;
                p2 >>= 4;
                p3 = bytes[i + 1] & 0x0F;
                p3 <<= 2;
            } 
            else 
            {
                p2 = 0;
                p3 = 0;
            }
            int p4;
            int p5;
            if (i + 2 < length) 
            {
                p4 = bytes[i + 2] & 0xC0;
                p4 >>= 6;
                p5 = bytes[i + 2] & 0x3F;
            } 
            else 
            {
                p4 = 0;
                p5 = 0;
            }

            if (i + 2 < length) 
            {
                buffer.append(ENCODE[p0]);
                buffer.append(ENCODE[p1 | p2]);
                buffer.append(ENCODE[p3 | p4]);
                buffer.append(ENCODE[p5]);
            }
            else if (i + 1 < length) 
            {
                buffer.append(ENCODE[p0]);
                buffer.append(ENCODE[p1 | p2]);
                buffer.append(ENCODE[p3]);
                buffer.append(PAD_CHAR);
            } 
            else 
            {
                buffer.append(ENCODE[p0]);
                buffer.append(ENCODE[p1 | p2]);
                buffer.append(PAD_CHAR);
                buffer.append(PAD_CHAR);
            }
        }
        return buffer.toString();
    }
}
