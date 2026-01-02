"""
  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
"""

cd('/SecurityConfiguration/@cargo.weblogic.domain.name@/Realms/myrealm/AuthenticationProviders/DefaultAuthenticator')

if not cmo.groupExists('@cargo.weblogic.group@'):
    cmo.createGroup('@cargo.weblogic.group@','@cargo.weblogic.group@')
