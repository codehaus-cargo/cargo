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

security = AdminConfig.getid('/Security:/')
prop = AdminConfig.getid('/Security:/Property:@cargo.websphere.property.name@/')
if prop:
    AdminConfig.modify(prop, [['value', '@cargo.websphere.property.value@']])
else:
    AdminConfig.create('Property', security, [['name','@cargo.websphere.property.name@'], ['value','@cargo.websphere.property.value@']])
