// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

package fit;

import java.lang.reflect.Method;

import fit.exception.CouldNotParseFitFailureException;
import fit.exception.FitFailureException;
import fit.exception.NoSuchMethodFitFailureException;

public class ActionFixture extends Fixture {
  protected Parse cells;
  private Fixture actor;

  @Override
  public void doCells(Parse cells) {
    this.cells = cells;
    String methodName = cells.text();
    try {
      getClass().getMethod(methodName).invoke(this);
    }
    catch (Exception e) {
      exception(cells, e);
    }
  }

  public void start() throws Throwable {
    Parse fixture = cells.more;
    if (isNullOrBlank(fixture))
      throw new FitFailureException("You must specify a fixture to start.");
    actor = loadFixture(fixture.text());
  }

  private boolean isNullOrBlank(Parse fixture) {
    return fixture == null || fixture.text().equals("");
  }

  public Fixture getActor() {
    return this.actor;
  }

  public void enter() throws Exception {
    Parse argumentCell = cells.more.more;
    if (argumentCell == null)
      throw new FitFailureException("You must specify an argument.");

    Method enterMethod = tryFindMethodWithArgs(1);
    Class<?> parameterType = enterMethod.getParameterTypes()[0];
    String argument = argumentCell.text();
    enterMethod.invoke(actor, adaptArgumentToType(parameterType, argument));
  }

  private Object adaptArgumentToType(Class<?> parameterType, String argument) throws Exception {
    Object arg;
    try {
      arg = TypeAdapter.on(actor, parameterType).parse(argument);
    }
    catch (NumberFormatException e) {
      throw new CouldNotParseFitFailureException(argument, parameterType.getName());
    }
    return arg;
  }

  public void press() throws Exception {
    tryFindMethodWithArgs(0).invoke(actor);
  }

  public void check() throws Throwable {
    Method checkMethod = tryFindMethodWithArgs(0);
    Class<?> returnType = checkMethod.getReturnType();

    Parse checkValueCell = cells.more.more;
    if (checkValueCell == null)
      throw new FitFailureException("You must specify a value to check.");

    check(checkValueCell, getTypeAdapter(checkMethod, returnType));
  }

  private TypeAdapter getTypeAdapter(Method checkMethod, Class<?> returnType) {
    try {
      return(TypeAdapter.on(actor, checkMethod));
    }
    catch (Throwable e) { // NOSONAR
      throw new FitFailureException("Can not parse return type: " + returnType.getName());
    }
  }

  protected Method tryFindMethodWithArgs(int args) throws NoSuchMethodException {
    Parse methodCell = cells.more;
    if (isNullOrBlank(methodCell))
      throw new FitFailureException("You must specify a method.");
    String methodName = camel(methodCell.text());
    if (actor == null)
      throw new FitFailureException("You must start a fixture using the 'start' keyword.");
    Method theMethod = findMethodWithArgs(methodName, args);
    if (theMethod == null) {
      throw new NoSuchMethodFitFailureException(methodName);
    }
    return theMethod;
  }

  private Method findMethodWithArgs(String methodName, int args) {
    Method[] methods = actor.getClass().getMethods();
    Method theMethod = null;
    for (Method m : methods) {
      if (m.getName().equals(methodName) && m.getParameterTypes().length == args) {
        if (theMethod == null) {
          theMethod = m;
        } else {
          throw new FitFailureException("You can only have one " + methodName + "(arg) method in your fixture.");
        }
      }
    }
    return theMethod;
  }

}
