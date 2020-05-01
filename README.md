Cargo is a thin Java wrapper that allows you to manipulate various type of application containers (J2EE, Java EE, Jakarta EE and others) in a standard way.

Cargo provides the following APIs and tools:

* A Java API to start / stop / configure any of the supported containers.
* A Java API to (remotely or locally) deploy modules into these containers, be it a server alone, a farm or a cluster.
* A Java API to parse / create/merge J2EE modules.
* ANT tasks wrapping the Java API for configuring, starting, stopping and deploying applications to all supported containers.
* A Web interface that wraps the Java API that can be used to configure, start and stop all containers supported by Cargo remotely and at any time.
* Maven2/Maven3 plugins wrapping the Java API for configuring, starting, stopping and deploying applications to all supported containers as well as parsing, creating and merging J2EE / Java EE modules.

These tools and APIs can be used in a standalone fashion or via various IDEs. The typical use cases for Codehaus Cargo are around configuring different application containers in a standard way, deploying your application in them in order to have continous integration / testing and managing application containers remotely for centralized (and potentially multi-target) deployments.

To learn more about Codehaus Cargo or ask questions:

* Check out our Web site - Which contains all our documentation - Be it for understanding what Codehaus Cargo is, learn how to use it with many examples, detailed documentation on container features as well as articles from various users: https://codehaus-cargo.github.io/
* Mailing list: https://groups.google.com/d/forum/codehaus-cargo
* Issue tracker (JIRA): https://codehaus-cargo.atlassian.net/projects/CARGO
