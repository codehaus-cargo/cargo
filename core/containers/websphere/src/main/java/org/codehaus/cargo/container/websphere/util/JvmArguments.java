/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

/**
 * Used for parsing and retrieving JVM arguments.
 */
public final class JvmArguments
{
    /**
     * Initial heap size in bytes.
     */
    private long initialHeap;
    /**
     * Maximum heap size in bytes.
     */
    private long maxHeap;
    /**
     * Other arguments.
     */
    private String genericArgs;

    /**
     * Private constructor to prevent getting an instance.
     */
    private JvmArguments()
    {
        // Utility classes have no public constructors
    }

    /**
     * Used for parsing JVM arguments string into specific values.
     * @param arguments JVM arguments.
     * @return Instance containing parsed values.
     */
    public static JvmArguments parseArguments(String arguments)
    {
        JvmArguments jvmArguments = new JvmArguments();

        StringBuilder genericArgs = new StringBuilder();
        if (arguments != null)
        {
            for (String arg : arguments.split(" "))
            {
                if (arg.startsWith("-Xms"))
                {
                    long initialHeap = getHeapArgValueInBytes(arg);
                    jvmArguments.setInitialHeap(initialHeap);
                }
                else if (arg.startsWith("-Xmx"))
                {
                    long maxHeap = getHeapArgValueInBytes(arg);
                    jvmArguments.setMaxHeap(maxHeap);
                }
                else
                {
                    if (genericArgs.length() > 0)
                    {
                        genericArgs.append(' ');
                    }
                    genericArgs.append(arg);
                }
            }
        }
        jvmArguments.setGenericArgs(genericArgs.toString());

        return jvmArguments;
    }

    /**
     * @param heapString Heap string to be parsed like -Xms200m
     * @return Heap value in bytes.
     */
    private static long getHeapArgValueInBytes(String heapString)
    {
        String size = heapString.substring(4, heapString.length() - 1);
        String suffix = heapString.toLowerCase().substring(heapString.length() - 1);
        ByteUnit byteUnit = ByteUnit.toByteUnit(suffix);

        return convertToValue(Long.valueOf(size), byteUnit, ByteUnit.BYTES);
    }

    /**
     * @param value Value to be converted like 150.
     * @param valueUnit Unit of value to be converted like m.
     * @param convertedUnit Unit of result value.
     * @return Result value.
     */
    private static long convertToValue(long value, ByteUnit valueUnit, ByteUnit convertedUnit)
    {
        long result = 0;
        if (value <= 0)
        {
            return result;
        }

        // Convert to bytes.
        long valueInBytes = value;
        ByteUnit valueUnitMarker = valueUnit;
        while (valueUnitMarker != ByteUnit.BYTES)
        {
            valueInBytes = valueUnitMarker.getLowerSize(valueInBytes);
            valueUnitMarker = valueUnitMarker.getLowerUnit();
        }

        // Convert to defined unit.
        result = valueInBytes;
        while (valueUnitMarker != convertedUnit)
        {
            result = valueUnitMarker.getHigherSize(result);
            valueUnitMarker = valueUnitMarker.getHigherUnit();
        }

        return result;
    }

    /**
     * @param expectedUnit Expected byte unit.
     * @return Initial heap size.
     */
    public long getInitialHeap(ByteUnit expectedUnit)
    {
        if (initialHeap == 0)
        {
            return getMaxHeap(expectedUnit);
        }
        return convertToValue(initialHeap, ByteUnit.BYTES, expectedUnit);
    }

    /**
     * @param initialHeap Initial heap size.
     */
    private void setInitialHeap(long initialHeap)
    {
        this.initialHeap = initialHeap;
    }

    /**
     * @param expectedUnit Expected byte unit.
     * @return Maximum heap size.
     */
    public long getMaxHeap(ByteUnit expectedUnit)
    {
        if (maxHeap == 0)
        {
            return convertToValue(512, ByteUnit.MEGABYTES, expectedUnit);
        }
        return convertToValue(maxHeap, ByteUnit.BYTES, expectedUnit);
    }

    /**
     * @param maxHeap Maximum heap size.
     */
    private void setMaxHeap(long maxHeap)
    {
        this.maxHeap = maxHeap;
    }

    /**
     * @return Other arguments.
     */
    public String getGenericArgs()
    {
        return genericArgs;
    }

    /**
     * @param genericArgs Other arguments.
     */
    private void setGenericArgs(String genericArgs)
    {
        this.genericArgs = genericArgs;
    }
}
