/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary;

import fitLibrary.exception.MissingRowFailureException;
import fitLibrary.exception.VoidMethodFitFailureException;
import fit.Parse;

/**
 * A fixture similar to ColumnFixture, except that:
 * o It separates the given and calculated columns by an empty column
 * o It doesn't treat any strings as special by default
 * o Special strings can be defined for repeats and exception-expected.
 * o A single method call is made for each expected column, rather than
 *   using public instance variables for the givens, as with ColumnFixture.
 *   With the header row:
 *       |g1 |g2 ||e1 |e2 |
 *       |1.1|1.2||2.3|0.1|
 *   Each row will lead to a call of the following methods with two (given)
 *   double arguments:
 *       e1G1G2() and e2G1G2()
 * o As with DoFixture, a systemUnderTest (SUT) may be associated with the fixture
 *   and any method calls not available on the fixture are called on the SUT.
 * 
 * See the FixtureFixture specifications for examples
 */
public class CalculateFixture extends DoFixture {
	protected int argCount = -1;
	private MethodTarget[] targets;
	protected boolean boundOK = false;
	private int methods = 0;
    private String repeatString = null;
    private String exceptionString = null;
	
	public CalculateFixture() {
	}
	public CalculateFixture(Object sut) {
	    super(sut);
	}	
	/** Defines the String that signifies that the value in the row above is
	 *  to be used again. Eg, it could be set to "" or to '"".
	 */
	public void setRepeatString(String repeat) {
		this.repeatString = repeat;
	}
	/** Defines the String that signifies that no result is expected;
	 *  instead an exception is.
	 */
    protected void setExceptionString(String exceptionString) {
        this.exceptionString = exceptionString;
    }
	public void doRows(Parse rows) {
		if (rows == null)
			throw new MissingRowFailureException();
		bind(rows.parts);
		super.doRows(rows.more);
	}
	protected void bind(Parse row) {
		Parse heads = row;
		boolean pastDoubleColumn = false;
		int rowLength = heads.size();
		String argNames = "";
		for (int i = 0; heads != null; i++, heads = heads.more) {
			String name = heads.text();
			try {
				if (name.equals("")) {
//					if (argCount > -1)
//						throw new FitFailureException("Two empty columns");
					argCount = i;
					targets = new MethodTarget[rowLength-i-1];
					pastDoubleColumn = true;
				}
				else if (pastDoubleColumn) {
					String methodName = ExtendedCamelCase.camel(name+argNames);
					MethodTarget target = findMethod(methodName,argCount);
					if (target.getReturnType() == void.class)
						throw new VoidMethodFitFailureException(methodName);
					targets[methods] = target;
					methods++;
					target.setRepeatAndExceptionString(repeatString,exceptionString);
				} else
					argNames += " " + name;
			} catch (Exception e) {
				exception(heads, e);
				return;
			}
		}
		if (methods == 0)
			exception(row,"No calculated column");
		boundOK = true;
	}
	 public void doRow(Parse row) {
	 	if (!boundOK) {
	 		ignore(row.parts);
	 		return;
	 	}
	 	if (row.parts.size() != argCount+methods+1) {
			exception(row.parts,"Row should be "+(argCount+methods+1)+" cells wide");
			return;
	 	}
	 	Parse expectedCell = row.parts.at(argCount+1);
	 	for (int i = 0; i < methods; i++) {
	 		targets[i].invokeAndCheck(row.parts,expectedCell);
			expectedCell = expectedCell.more;
	 	}
	 }
}
