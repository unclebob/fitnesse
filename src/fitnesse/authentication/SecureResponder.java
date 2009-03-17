// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.Responder;

public interface SecureResponder extends Responder {
  SecureOperation getSecureOperation();
}
