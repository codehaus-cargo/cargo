/* 
 * ========================================================================
 * 
 * Copyright 2005-2007 Vincent Massol.
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
package org.codehaus.cargo.maven2.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.codehaus.cargo.maven2.Merge;
import org.codehaus.cargo.module.merge.DocumentMergerByXslt;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.webapp.merge.WarArchiveMerger;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Merge processor that uses XSLT
 *  
 * @version $Id: 
 */
public class MergeXslt implements MergeProcessorFactory
{
  File workingDirectory;
  
  public MergeXslt(File directory)
  {
    this.workingDirectory = directory;
  }
 
  /**
   * {@inheritDoc}
   */
  public MergeProcessor create(WarArchiveMerger wam, Merge xml)
  {
    try
    {
      Xpp3Dom parameters = (Xpp3Dom)xml.getParameters();
      String filename = parameters.getChild("file").getValue();
      
      File file = new File(workingDirectory, filename);
      
      InputStream is = new FileInputStream(file);
      
      DocumentMergerByXslt documentMergerByXslt = new DocumentMergerByXslt(is);            
      
      return documentMergerByXslt;
    }
    catch(Exception ex)
    {
      throw new CargoException("Exception creating XSLT Merge",ex);
    }
    
  }

}
