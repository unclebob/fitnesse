/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import fitLibrary.*;
import fit.Parse;

/**
 * A fixture for entering data for setup (or anything else).
 * Serves a similar purpose to Michael Feather's RowEntryFixture
 * It operates the same as CalcuateFixture, except that there is no empty column
 * and thus no expected columns.
 * It calls setUp() before a call of the method for each row.
 * It calls tearDown() afterwards.
 */
public class SetUpFixture extends CalculateFixture { //COPY:ALL
	private MethodTarget target; //COPY:ALL
	 //COPY:ALL
	public void doTable(Parse table) { //COPY:ALL
		try { //COPY:ALL
			setUp(); //COPY:ALL
			super.doTable(table); //COPY:ALL
			tearDown(); //COPY:ALL
		} catch (Exception e) { //COPY:ALL
			exception(table.at(0, 0, 0),e); //COPY:ALL
		} //COPY:ALL
	} //COPY:ALL
	protected void bind(Parse headerRow) { //COPY:ALL
		Parse cells = headerRow; //COPY:ALL
		argCount = cells.size(); //COPY:ALL
		String argNames = ""; //COPY:ALL
		while (cells != null) { //COPY:ALL
			argNames += " " + cells.text(); //COPY:ALL
			cells = cells.more; //COPY:ALL
		} //COPY:ALL
		String methodName = ExtendedCamelCase.camel(argNames); //COPY:ALL
		try { //COPY:ALL
            target = findMethod(methodName,argCount); //COPY:ALL
            boundOK = true; //COPY:ALL
        } catch (Exception e) { //COPY:ALL
			exception(headerRow, e); //COPY:ALL
        } //COPY:ALL
	} //COPY:ALL
	 public void doRow(Parse row) { //COPY:ALL
	 	if (!boundOK) { //COPY:ALL
	 		ignore(row.parts); //COPY:ALL
	 		return; //COPY:ALL
	 	} //COPY:ALL
	 	if (row.parts.size() != argCount) { //COPY:ALL
			exception(row.parts,"Row should be "+argCount+" cells wide"); //COPY:ALL
			return; //COPY:ALL
	 	} //COPY:ALL
	 	try { //COPY:ALL
			target.invoke(row.parts); //COPY:ALL
		} catch (Exception e) { //COPY:ALL
			exception(row.parts,e); //COPY:ALL
		} //COPY:ALL
	 } //COPY:ALL
	/**
	 * Override if you wish to do something before entering data
	 */
	protected void setUp() throws Exception { //COPY:ALL
	} //COPY:ALL
	/**
	 * Override if you wish to do something after entering data
	 */
	protected void tearDown() throws Exception { //COPY:ALL
	} //COPY:ALL
} //COPY:ALL

