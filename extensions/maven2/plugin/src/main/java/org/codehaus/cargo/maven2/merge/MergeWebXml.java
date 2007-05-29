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

import java.io.ByteArrayInputStream;

import org.codehaus.cargo.maven2.Merge;
import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.merge.DescriptorMergerByTag;
import org.codehaus.cargo.module.merge.MergeProcessor;
import org.codehaus.cargo.module.merge.tagstrategy.ChooseByNameMergeStrategy;
import org.codehaus.cargo.module.merge.tagstrategy.MergeStrategy;
import org.codehaus.cargo.module.merge.tagstrategy.NodeMergeStrategy;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.merge.WarArchiveMerger;
import org.codehaus.cargo.module.webapp.merge.WebXmlMerger;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Merge processor designed for web.xml files.
 *  
 * @version $Id: 
 */
public class MergeWebXml implements MergeProcessorFactory
{
  WebXmlMerger webXmlMerger;
  Descriptor firstItem = null;
  
  
  /* (non-Javadoc)
   * @see org.codehaus.cargo.maven2.merge.MergeType#init(org.codehaus.cargo.maven2.Merge)
   */
  public MergeProcessor create(WarArchiveMerger wam, Merge xml) 
  {
    webXmlMerger = wam.getWebXmlMerger();
    
    Xpp3Dom parameters = (Xpp3Dom)xml.getParameters();
    
    Xpp3Dom defaultNode = parameters.getChild("default");
    
    if( defaultNode != null )
    {
    
      DescriptorMergerByTag dmt = (DescriptorMergerByTag)webXmlMerger.getMergers().get(0);

      Xpp3Dom[] tags = defaultNode.getChildren("tag");
      for(int i=0; i<tags.length;i++)
      {
        Xpp3Dom tag = tags[i];
        
        String tagName = tag.getAttribute("name");
        Xpp3Dom strategy = tag.getChild("strategy");
        MergeStrategy ms = makeStrategy(strategy);
        
        dmt.setStrategy(WebXmlType.getInstance().getTagByName(tagName), ms);
      }
    }
    return null;
    
  }
  
  protected MergeStrategy makeStrategy(Xpp3Dom config)
  {

      if( !config.getName().equals("strategy") )
      {
          throw new CargoException("You must specify a merge strategy");
      }

      String strategyName = config.getAttribute("name");

      if( strategyName.equalsIgnoreCase("Preserve"))
      {
          return DescriptorMergerByTag.PRESERVE;
      }
      else if( strategyName.equalsIgnoreCase("Overwrite"))
      {
          return DescriptorMergerByTag.OVERWRITE;
      }
      else if( strategyName.equalsIgnoreCase("ChooseByName"))
      {
          Xpp3Dom def = config.getChild("default").getChild(0);

          ChooseByNameMergeStrategy cbnms = new ChooseByNameMergeStrategy(makeStrategy(def));

          Xpp3Dom[] items = config.getChildren();
          for(int i=0; i<items.length;i++)
          {
              Xpp3Dom item = items[i];
              if( item.getName().equals("choice") )
              {
                  cbnms.addStrategyForName(item.getAttribute("name"), makeStrategy(item.getChild(0)));
              }
          }
          return cbnms;
      }
      if( strategyName.equalsIgnoreCase("NodeMerge"))
      {
          String theXml = config.getChild(0).toString();

          try
          {
              return new NodeMergeStrategy(WebXmlType.getInstance(), new ByteArrayInputStream(theXml.getBytes()));
          }
          catch (Exception e)
          {
              throw new CargoException("Problem generating Node Merge strategy");
          }
      }

      throw new CargoException("Must provide a known strategy type (don't understand " + strategyName + ")");
  }
     
}
