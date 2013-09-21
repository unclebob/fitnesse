// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

public class NoSuchVersionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public NoSuchVersionException(String message) {
    super(message);
  }
}
