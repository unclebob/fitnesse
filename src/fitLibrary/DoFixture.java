/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, New Zealand.
 * Released under the terms of the GNU General Public License version 2 or later.
*/

package fitLibrary;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import fitLibrary.exception.ExtraCellsFailureException;
import fit.exception.FitFailureException;
import fit.*;
import fitLibrary.exception.IgnoredException;
import fitLibrary.exception.MissingCellsFailureException;
import fitLibrary.graphic.ObjectDotGraphic;
import fitLibrary.CalculateFixture;

/** An alternative to fit.ActionFixture
	@author rick mugridge, july 2003
	Updated April 2004 to include not/reject actions.
	Updated August 2004 to handle properties and
	  to automap Object to DoFixture, List, etc to ArrayFixture, etc.
  * 
  * See the FixtureFixture specifications for examples
*/
public class DoFixture extends FlowFixture {
	protected Object systemUnderTest = null;
	protected Map map = new HashMap();
	private boolean passedOntoOtherFixture = false;

	public DoFixture() {
	}
	public DoFixture(Object sut) {
	    setSystemUnderTest(sut);
	}
	/** Set the systemUnderTest. 
	 *  If an action can't be satisfied by the DoFixture, the systemUnderTest
	 *  is tried instead. Thus the DoFixture is an adapter with methods just
	 *  when they're needed.
	 */
	public void setSystemUnderTest(Object sut) {
		this.systemUnderTest = sut;	    
	}
	/** Check that the result of the action in the rest of the row matches
	 *  the expected value in the last cell of the row.
	 */
	public void check(Parse cells) throws Exception {
		cells = cells.more;
		if (cells == null)
			throw new MissingCellsFailureException();
		int args = cells.size() - 2;
		Parse expectedCell = cells.at(args + 1);
		MethodTarget target = findMethodByActionName(cells, args);
		target.invokeAndCheck(cells.more,expectedCell);
	}
	/** Add a cell containing the result of the rest of the row.
	 *  HTML is not altered, so it can be viewed directly.
	 */
	public void show(Parse cells) throws Exception {
		cells = cells.more;
		if (cells == null)
			throw new MissingCellsFailureException();
		int args = cells.size() - 1;
		MethodTarget target = findMethodByActionName(cells, args);
		Class resultType = target.getReturnType();
		TypeAdapter adapter = LibraryTypeAdapter.onResult(this, resultType);
		try {
			Object result = callGivenMethod(target, cells);
			if (resultType == String.class)
				addCell(cells, result);
			else
				addCell(cells, adapter.toString(result));
		} catch (IgnoredException e) { // No result, so ignore
		}
	}
	/** Add a cell containing the result of the rest of the row,
	 *  shown as a Dot graphic.
	 */
	public void showDot(Parse cells) throws Exception {
		cells = cells.more;
		if (cells == null)
			throw new MissingCellsFailureException();
		int args = cells.size() - 1;
		MethodTarget target = findMethodByActionName(cells, args);
		Class resultType = target.getReturnType();
		TypeAdapter adapter = LibraryTypeAdapter.onResult(this, ObjectDotGraphic.class);
		try {
			Object result = callGivenMethod(target, cells);
			addCell(cells, adapter.toString(new ObjectDotGraphic(result)));
		} catch (IgnoredException e) { // No result, so ignore
		}
	}
	private void addCell(Parse cells, Object result) {
		cells.last().more = new Parse("td", result.toString(), null, null);
	}
	/** Checks that the action in the rest of the row succeeds.
	 *  o If a boolean is returned, it must be true.
	 *  o For other result types, no exception should be thrown.
	 */
	public void ensure(Parse cells) throws Exception {
		Parse ensureCell = cells;
		cells = cells.more;
		if (cells == null)
			throw new MissingCellsFailureException();
		MethodTarget target = findMethodByActionName(cells,cells.size()-1);
		try {
			Object result = callGivenMethod(target, cells);
			if (((Boolean)result).booleanValue())
				right(ensureCell);
			else
				wrong(ensureCell);
		} catch (IgnoredException e) { // No result, so ignore
		} catch (Exception e) {
			wrong(ensureCell);
		}
	}
	/** Checks that the action in the rest of the row fails.
	 *  o If a boolean is returned, it must be false.
	 *  o For other result types, an exception should be thrown.
	 */
	public void reject(Parse cells) throws Exception {
		not(cells);
	}
	/** Same as reject()
	 */
	public void not(Parse cells) throws Exception {
		Parse notCell = cells;
		cells = cells.more;
		if (cells == null)
			throw new MissingCellsFailureException();
		MethodTarget target = findMethodByActionName(cells,cells.size()-1);
		try {
			Object result = callGivenMethod(target, cells);
			if (!(result instanceof Boolean))
				exception(notCell,"Was not rejected");
			else if (((Boolean)result).booleanValue())
				wrong(notCell);
			else
				right(notCell);
		} catch (IgnoredException e) { // No result, so ignore
		} catch (Exception e) {
			right(notCell);
		}
	}
	/** The rest of the row is ignored. 
	 */
	public void note(Parse cells) throws Exception {
	}
	/** An experimental feature, which may be changed or removed. */
	public void name(Parse cells) throws Exception {
		// |name|method|args|
		cells = cells.more;
		if (cells == null || cells.more == null)
			throw new MissingCellsFailureException();
		String name = cells.text();
		Parse methodCells = cells.more;
		int args = methodCells.size() - 1;
		MethodTarget target = findMethodByActionName(methodCells, args);
		Object result = target.invokeAndWrap(methodCells.more);
		if (result instanceof Fixture) {
			map.put(name,result);
			right(cells);
		}
		else
			throw new FitFailureException("Must return an object.");
	}
	/** An experimental feature, which may be changed or removed. */
	public Fixture use(Parse cells) {
		// |use|name|of|name|...
		cells = cells.more;
		if (cells == null )
			throw new MissingCellsFailureException();
		String name = cells.text();
		Object object = getMapper(cells.more).map.get(name);
		if (object instanceof Fixture)
			return (Fixture)object;
		throw new FitFailureException("Unknown name: "+name);
	}
	private DoFixture getMapper(Parse cells) {
		if (cells == null)
			return this;
		if (!cells.text().equals("of"))
			throw new FitFailureException("Missing 'of'.");
		if (cells.more != null) {
			cells = cells.more;
			String name = cells.text();
			Object object = getMapper(cells.more).map.get(name);
			if (object instanceof DoFixture)
				return (DoFixture)object;
			throw new FitFailureException("Unknown name: "+name);
		}
		throw new FitFailureException("Missing name.");
	}
	/** To allow for DoFixture to be used without writing any fixtures.
	 */
	public void start(Parse cells) {
		cells = cells.more;
		if (cells == null)
			throw new MissingCellsFailureException();
		if (cells.more != null)
			throw new ExtraCellsFailureException();
	    String className = cells.text();
	    try {
            setSystemUnderTest(Class.forName(className).newInstance());
        } catch (Exception e) {
            throw new FitFailureException("Unknown class: "+className);
        }
	}
	/** To allow for a CalculateFixture to be used for the rest of the table.
	 *  This is intended for use for teaching, where no fixtures need to be
	 *  written.
	 */
	public Fixture calculate() {
	    return new CalculateFixture(systemUnderTest);
	}
	//--- CODE FOR FLOW:
	public void doRows(Parse rows) {
		while(rows != null && !passedOntoOtherFixture)	{
			Parse more = rows.more;
			doRow(rows);
			rows = more;
		}
	}
    /** Note that doCells() and doCell() are not called at all.
    */
	public void doRow(Parse row) {
		try {
		    Object result = interpretCells(row.parts);
			if (result instanceof Fixture) {
				Parse restOfTable = new Parse("table","",row,null);
			    interpretTableWithFixture(restOfTable,(Fixture)result);
				passedOntoOtherFixture = true;
			}
		} catch (Exception ex) {
			exception(row, ex);
		}
	}
	protected Object interpretCells(Parse cells) {
		try {
			Object result = calledParseMethod(cells);
			if (result != null)
				return result;
			MethodTarget target = findMethodByActionName(cells,cells.size()-1);
			result = target.invokeAndWrap(cells.more);
			if (result instanceof Boolean)
				target.color(cells,((Boolean)result).booleanValue());
			return result;
		} catch (IgnoredException ex) {
		} catch (Exception ex) {
			exception(cells, ex);
		}
		return null;
	}
	// --- END CODE FOR FLOW
	private Object calledParseMethod(final Parse cells) throws Exception {
		try {
			String name = cells.text();
			if (name.equals(""))
			    return null;
			name = camel(name);
			Method parseMethod =
				getClass().getMethod(name, new Class[] { Parse.class });
			MethodTarget target = new MethodTarget(this,parseMethod,this);
			Object result = target.invoke(new Object[] { cells });
			if (result == null)
				result = ""; // As null signals there wan't one
			return result;
		} catch (NoSuchMethodException ex) {
		}
		return null;
	}
	/** Is overridden in subclass SequenceFixture to process arguments differently
	 */
	protected MethodTarget findMethodByActionName(Parse cells, int allArgs) throws Exception {
		int parms = allArgs / 2 + 1;
		int args = (allArgs + 1) / 2;
		String name = cells.text();
		for (int i = 1; i < parms; i++)
			name += " "+cells.at(i*2).text();
		MethodTarget target = findMethod(ExtendedCamelCase.camel(name),args);
		target.setEverySecond(true);
		return target;
	}
	protected MethodTarget findMethod(String name, int args) {
		MethodTarget result = MethodTarget.findSpecificMethod(name,args,this,this);
		if (result != null)
			return result;
		if (systemUnderTest != null) {
			result = MethodTarget.findSpecificMethod(name,args,systemUnderTest,this);
			if (result != null)
				return result;
		}
		String plural = "s";
		if (args == 1)
			plural = "";
		throw new FitFailureException("Unknown: \""+name+
						"\" with "+args+" argument"+plural);
	}
	protected Object callGivenMethod(MethodTarget target, final Parse rowCells)
		                                                  throws Exception {
		return target.invoke(rowCells.more);
	}
	protected void tearDown() throws Exception {
		systemUnderTest = null;
	}
}
