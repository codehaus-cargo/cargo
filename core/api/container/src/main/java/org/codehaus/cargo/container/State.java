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
package org.codehaus.cargo.container;

/**
 * Represent the container states (started, starting, stopped, etc).
 */
public final class State
{
    /**
     * State when container is starting.
     */
    public static final State STARTING = new State("starting");

    /**
     * State when container is started.
     */
    public static final State STARTED = new State("started");

    /**
     * State when a container is stopped due to its start having failed.
     */
    public static final State STOPPED_FAILED_START =
        new State("stopped due to failed start");

    /**
     * State when container is stopping.
     */
    public static final State STOPPING = new State("stopping");

    /**
     * State when container is stopped.
     */
    public static final State STOPPED = new State("stopped");

    /**
     * Unknown state.
     */
    public static final State UNKNOWN = new State("unknown");

    /**
     * Textual representation of the state.
     */
    private String stateText;

    /**
     * @param stateText the textual representation of the state
     */
    private State(String stateText)
    {
        this.stateText = stateText;
    }

    /**
     * @return the textual representation of the state
     */
    @Override
    public String toString()
    {
        return this.stateText;
    }

    /**
     * @return true if the container is starting
     */
    public boolean isStarting()
    {
        return this.stateText.equals(State.STARTING.stateText);
    }

    /**
     * @return true if the container is started
     */
    public boolean isStarted()
    {
        return this.stateText.equals(State.STARTED.stateText);
    }

    /**
     * @return true if the container is stopping
     */
    public boolean isStopping()
    {
        return this.stateText.equals("stopping");
    }

    /**
     * @return true if the container is stopped
     */
    public boolean isStopped()
    {
        return this.stateText.equals(State.STOPPED.stateText)
            || this.stateText.equals(State.STOPPED_FAILED_START.stateText);
    }
}
