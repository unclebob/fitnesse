// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import fitnesse.wiki.*;

public class SecureTestOperation extends SecurePageOperation
{
  protected String getSecurityMode()
  {
    return WikiPage.SECURE_TEST;
  }
}
