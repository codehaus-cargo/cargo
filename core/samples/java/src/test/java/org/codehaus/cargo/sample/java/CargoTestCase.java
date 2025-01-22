/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.sample.java;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.DefaultContainerFactory;

/**
 * Whether the given test case can be executed in the given condition.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(CargoTestCase.CargoTestcaseInvocationContextProvider.class)
public @interface CargoTestCase
{
    /**
     * Current Cargo test case invocation context, i.e. the container id and type.
     */
    class CargoTestcaseInvocationContext implements TestTemplateInvocationContext
    {
        /**
         * Container id.
         */
        private String containerId;

        /**
         * Container type.
         */
        private ContainerType containerType;

        /**
         * Saves the container id and type.
         * @param containerId Container id.
         * @param containerType Container type.
         */
        public CargoTestcaseInvocationContext(String containerId, ContainerType containerType)
        {
            this.containerId = containerId;
            this.containerType = containerType;
        }

        /**
         * @return Container id.
         */
        public String getContainerId()
        {
            return containerId;
        }

        /**
         * @return Container type.
         */
        public ContainerType getContainerType()
        {
            return containerType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Extension> getAdditionalExtensions()
        {
            return Arrays.asList(preProcessor());
        }

        /**
         * {@inheritDoc}
         * @return Container id and type.
         */
        @Override
        public String getDisplayName(int invocationIndex)
        {
            return containerId + "," + containerType.getType();
        }

        /**
         * Set the {@link CargoTestcaseInvocationContext} to the {@link AbstractCargoTestCase}.
         * @return The preprocessor that populates the {@link CargoTestcaseInvocationContext} to
         * the {@link AbstractCargoTestCase}.
         */
        private BeforeTestExecutionCallback preProcessor()
        {
            return new BeforeTestExecutionCallback()
            {
                /**
                 * {@inheritDoc}
                 * Set the {@link CargoTestcaseInvocationContext} to the
                 * {@link AbstractCargoTestCase}.
                 */
                @Override
                public void beforeTestExecution(ExtensionContext context) throws Exception
                {
                    ((AbstractCargoTestCase) context.getTestInstance().get()).setUp(
                        CargoTestCase.CargoTestcaseInvocationContext.this, context);
                }
            };
        }
    }

    /**
     * Creates the testcase invocation contexts for the various tests. In practice, this is how we
     * can run the same test for the Embedded and Installed Local Container instances.
     */
    class CargoTestcaseInvocationContextProvider implements TestTemplateInvocationContextProvider
    {
        /**
         * The system property for the container identifiers to run tests on.
         */
        public static final String SYSTEM_PROPERTY_CONTAINER_IDS = "cargo.containers";

        /**
         * Registered containers.
         */
        private Map<String, Set<ContainerType>> registeredContainers;

        /**
         * Container identifiers on which to run tests.
         */
        private List<String> containerIds;

        public CargoTestcaseInvocationContextProvider()
        {
            this.registeredContainers = new DefaultContainerFactory().getContainerIds();
            this.containerIds = new ArrayList<String>();

            if (System.getProperty(SYSTEM_PROPERTY_CONTAINER_IDS) == null)
            {
                throw new IllegalArgumentException("System property \""
                    + SYSTEM_PROPERTY_CONTAINER_IDS + "\" must be defined.");
            }

            StringTokenizer tokens =
                new StringTokenizer(System.getProperty(SYSTEM_PROPERTY_CONTAINER_IDS), ",");
            while (tokens.hasMoreTokens())
            {
                String token = tokens.nextToken().trim();
                if (!this.registeredContainers.containsKey(token))
                {
                    throw new IllegalArgumentException("Invalid container id: " + token);
                }
                this.containerIds.add(token);
            }
        }

        /**
         * {@inheritDoc}
         * @return <code>true</code>
         */
        @Override
        public boolean supportsTestTemplate(ExtensionContext context)
        {
            return true;
        }

        /**
         * {@inheritDoc}
         * @return List of {@link CargoTestcaseInvocationContext}, with all combinations of
         * container and type for the testcases, supported by the test.
         */
        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
            ExtensionContext context)
        {
            if (!AbstractCargoTestCase.class.isAssignableFrom(context.getTestClass().get()))
            {
                throw new IllegalArgumentException(
                    "Not an AbstractCargoTestCase: " + context.getTestClass().get());
            }
            AbstractCargoTestCase testCase;
            if (context.getTestInstance().isPresent())
            {
                testCase = (AbstractCargoTestCase) context.getTestInstance().get();
            }
            else
            {
                try
                {
                    testCase = (AbstractCargoTestCase)
                        context.getTestClass().get().getDeclaredConstructor().newInstance();
                }
                catch (Throwable t)
                {
                    throw new IllegalArgumentException(t);
                }
            }
            File currentJavaHomeDirectory = null;
            String currentJavaHome = System.getProperty("java.home");
            if (currentJavaHome != null && !currentJavaHome.isEmpty())
            {
                currentJavaHomeDirectory = new File(currentJavaHome).getAbsoluteFile();
            }
            List<TestTemplateInvocationContext> invocationContexts =
                new ArrayList<TestTemplateInvocationContext>();
            for (String containerid : this.containerIds)
            {
                for (ContainerType containerType : this.registeredContainers.get(containerid))
                {
                    if (containerType.equals(ContainerType.EMBEDDED))
                    {
                        String containerJavaHome =
                            System.getProperty("cargo." + containerid + ".java.home");
                        if (containerJavaHome != null && !containerJavaHome.isEmpty())
                        {
                            File containerJavaHomeDirectory =
                                new File(containerJavaHome).getAbsoluteFile();
                            if (!currentJavaHomeDirectory.equals(containerJavaHomeDirectory))
                            {
                                // Container JAVA_HOME has been set to a different folder than the
                                // JAVA_HOME used by the build / testing process.
                                // Do not run embedded tests.
                                continue;
                            }
                        }
                    }
                    if (testCase.isSupported(
                        containerid, containerType, context.getTestMethod().get()))
                    {
                        invocationContexts.add(
                            new CargoTestcaseInvocationContext(containerid, containerType));
                    }
                }
            }
            Assumptions.assumeFalse(invocationContexts.isEmpty(),
                "None of the contaier ids support the requested test");
            return invocationContexts.stream();
        }
    }
}
