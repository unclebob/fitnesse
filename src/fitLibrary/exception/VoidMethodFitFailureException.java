/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitLibrary.exception;

import fit.exception.FitFailureException;

public class VoidMethodFitFailureException extends FitFailureException
{
  public VoidMethodFitFailureException(String name)
  {
    super("Method "+name+" is void.");
  }
}
