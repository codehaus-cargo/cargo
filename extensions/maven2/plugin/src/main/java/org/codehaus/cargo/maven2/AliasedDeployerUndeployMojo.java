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
package org.codehaus.cargo.maven2;

/**
 * Class used uniquely to provide a goal alias for <code>deployer-undeploy</code>.
 * 
 * @goal undeploy
 * @aggregator true
 * @requiresDependencyResolution compile
 * @see org.codehaus.cargo.maven2.DeployerUndeployMojo
 * @version $Id$
 */
public class AliasedDeployerUndeployMojo extends DeployerUndeployMojo
{
}
