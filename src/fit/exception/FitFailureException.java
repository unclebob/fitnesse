// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
/*
 * @author Rick Mugridge 1/10/2003
 *
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fit.exception;

/**
 * Provides an exception's way out when things go wrong.
 * But it's trapped by Fixture.exception print the contained message instead of a stackdump of the exception.
 */
public class FitFailureException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public FitFailureException(String s) {
    super(s);
  }
}

