/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere.util;

import org.codehaus.cargo.util.CargoException;

/**
 * Represents byte size.
 */
public enum ByteUnit
{
    /**
     * Bytes
     */
    BYTES("b", null),
    /**
     * Kilobytes
     */
    KILOBYTES("k", BYTES),
    /**
     * Megabytes
     */
    MEGABYTES("m", KILOBYTES),
    /**
     * Gigabytes
     */
    GIGABYTES("g", MEGABYTES),
    /**
     * Terabytes
     */
    TERABYTES("t", GIGABYTES);

    /**
     * Conversion rate between this and higher/lower unit.
     */
    private static final long CONVERSION_RATE = 1024L;
    /**
     * Suffix of unit.
     */
    private String suffix;
    /**
     * Reference to higher unit.
     */
    private ByteUnit higherUnit;
    /**
     * Reference to lower unit.
     */
    private ByteUnit lowerUnit;

    /**
     * Constructor.
     * @param suffix Suffix of unit, i.e. k or m.
     * @param lowerUnit Unit which is one degree lower than this one.
     */
    private ByteUnit(String suffix, ByteUnit lowerUnit)
    {
        this.suffix = suffix;
        setLowerUnit(lowerUnit);
        if (lowerUnit != null)
        {
            lowerUnit.setHigherUnit(this);
        }
    }

    /**
     * @return Higher unit.
     */
    public ByteUnit getHigherUnit()
    {
        return higherUnit;
    }

    /**
     * @param higherUnit Higher unit.
     */
    private void setHigherUnit(ByteUnit higherUnit)
    {
        this.higherUnit = higherUnit;
    }

    /**
     * @return Lower unit.
     */
    public ByteUnit getLowerUnit()
    {
        return lowerUnit;
    }

    /**
     * @param lowerUnit Lower unit.
     */
    private void setLowerUnit(ByteUnit lowerUnit)
    {
        this.lowerUnit = lowerUnit;
    }

    /**
     * @return Suffix.
     */
    public String getSuffix()
    {
        return suffix;
    }

    /**
     * @param currentValue Size in current unit.
     * @return Size in higher unit.
     */
    public long getHigherSize(long currentValue)
    {
        return currentValue / CONVERSION_RATE;
    }

    /**
     * @param currentValue Size in current unit.
     * @return Size in lower unit.
     */
    public long getLowerSize(long currentValue)
    {
        return currentValue * CONVERSION_RATE;
    }

    /**
     * @param suffix Suffix of unit.
     * @return Unit representing this suffix.
     */
    public static ByteUnit toByteUnit(String suffix)
    {
        for (ByteUnit unit : values())
        {
            if (unit.getSuffix().equals(suffix))
            {
                return unit;
            }
        }
        throw new CargoException("Byte unit for suffix " + suffix + "not found!");
    }
}
