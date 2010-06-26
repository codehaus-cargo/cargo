/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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

import java.io.ByteArrayOutputStream;

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
    private static final char[] ENCODE = new char[64];

    /**
     * The character to pad the output with if not a multiple of 24-bits.
     */
    private static final char PAD_CHAR = '=';

    // static -----------------------------------------------------------------

    static
    {
        // create Base64 character look-up table
        for (int i = 0; i < 26; i++)
        {
            ENCODE[i] = (char) ('A' + i);
            ENCODE[i + 26] = (char) ('a' + i);
        }
        for (int i = 0; i < 10; i++)
        {
            ENCODE[i + 52] = (char) ('0' + i);
        }
        ENCODE[62] = '+';
        ENCODE[63] = '/';
    }

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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = 0;
        int carry = 0;

        for (byte b : bytes)
        {
            switch (count++ % 3)
            {
                // first byte of 24-bits: write 6-bits and carry 2-bits
                case 0:
                    out.write(ENCODE[b >> 2]);
                    carry = b & 0x03;
                    break;

                // second byte of 24-bits: write carry + 4-bits, carry 4-bits
                case 1:
                    out.write(ENCODE[(carry << 4) + (b >> 4)]);
                    carry = b & 0x0F;
                    break;

                // third byte of 24-bits: write carry + 2-bits, write 6-bits
                case 2:
                    out.write(ENCODE[(carry << 2) + (b >> 6)]);
                    out.write(ENCODE[b & 0x3F]);
                    break;

                default:
                    throw new InternalError();
            }
        }

        switch (count % 3)
        {
            // third byte of 24-bits: 24-bit aligned
            case 0:
                break;

            // first byte of 24-bits: write 4-bit carry and pad 16-bits
            case 1:
                out.write(ENCODE[carry << 4]);
                out.write(PAD_CHAR);
                out.write(PAD_CHAR);
                break;

            // second byte of 24-bits: write 2-bit carry and pad 8-bits
            case 2:
                out.write(ENCODE[carry << 2]);
                out.write(PAD_CHAR);
                break;

            default:
                throw new InternalError();
        }

        return out.toByteArray();
    }
}
