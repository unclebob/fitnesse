/*
 * Copyright (c) 2003 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.ff;
import fit.*;
import fit.exception.FitFailureException;
import fitLibrary.DoFixture;

public class FlowFixtureUnderTest extends DoFixture { //COPY:ALL
	protected Parse row;
	
	public void doRow(Parse row) { // Into another class!
		this.row = row;
		super.doRow(row);
	}
	public void doCells(Parse cells) { //COPY:ALL
		try { //COPY:ALL
			String name = cells.text(); //COPY:ALL
			if (name.equals("r")) //COPY:ALL
				right(cells); //COPY:ALL
			else if (name.equals("w")) //COPY:FULL
				wrong(cells); //COPY:FULL
			else if (name.equals("i")) //COPY:FULL
				ignore(cells); //COPY:FULL
			else if (name.equals("e")) //COPY:FULL
				exception(cells, new RuntimeException("test")); //COPY:FULL
			else if (name.equals("-")); //COPY:FULL
			else if (name.equals("rw")) { //COPY:FULL
				right(cells); //COPY:FULL
				wrong(cells.more); //COPY:FULL
			} else if (name.equals("ri")) { //COPY:FULL
				right(cells); //COPY:FULL
				ignore(cells.more); //COPY:FULL
			} else if (name.equals("iw")) { //COPY:FULL
				ignore(cells); //COPY:FULL
				wrong(cells.more); //COPY:FULL
			} else if (name.equals("rwrwiwiee-")) { //COPY:FULL
				//... //COPY:FULL
				right(cells);
				wrong(cells.more);
				right(cells.more.more);
				wrong(cells.more.more.more);
				ignore(cells.more.more.more.more);
				wrong(cells.more.more.more.more.more);
				ignore(cells.more.more.more.more.more.more);
				exception(
					cells.more.more.more.more.more.more.more,
					new RuntimeException("test"));
				exception(
					cells.more.more.more.more.more.more.more.more,
					new RuntimeException("test"));
			} else if (name.equals("reports")) //COPY:FULL
				cells.more.addToBody("reported"); //COPY:FULL
			else if (name.equals("wMsg")) //COPY:FULL
				wrong(cells,"Message"); //COPY:FULL
			else if (name.equals("insertTwoRows")) //COPY:FULL
				addRows(); //COPY:FULL
			else //COPY:ALL
				throw new FitFailureException( //COPY:ALL
						"Action not known: " + name); //COPY:ALL
		} catch (Exception ex) { //COPY:ALL
			exception(cells, ex); //COPY:ALL
		} //COPY:ALL
	} //COPY:ALL
	private void addRows() { //COPY:ADD
		Parse nextRow = row.more; //COPY:ADD
		row.more = new Parse("tr", "", newRows(), //COPY:ADD
			new Parse("tr", "", newRows(), nextRow)); //COPY:ADD
	} //COPY:ADD
	private Parse newRows() { //COPY:ADD
		Parse result = new Parse("td","one",null,new Parse("td","two",null,null)); //COPY:ADD
		right(result); //COPY:ADD
		wrong(result.more); //COPY:ADD
		return result; //COPY:ADD
	} //COPY:ADD
} //COPY:ALL
