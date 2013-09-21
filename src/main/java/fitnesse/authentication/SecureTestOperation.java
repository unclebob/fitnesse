// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.wiki.PageData;

public class SecureTestOperation extends SecurePageOperation {
  protected String getSecurityMode() {
    return PageData.PropertySECURE_TEST;
  }
}
