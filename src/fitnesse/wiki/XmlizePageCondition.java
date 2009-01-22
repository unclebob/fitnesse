// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

public interface XmlizePageCondition {
  boolean canBeXmlized(WikiPage page) throws Exception;
}
