/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 */
package fitLibrary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fit.exception.FitFailureException;
import fitLibrary.exception.ExtraCellsFailureException;
import fitLibrary.exception.MissingCellsFailureException;
import fitLibrary.exception.MissingRowFailureException;
import fit.exception.NoSuchFieldFitFailureException;
import fit.*;

/**
  *	A fixture like fit.RowFixture, except that the order of rows is important
  * and properties are supported.
  * The algorithm used for matching is very simple; a diff approach may be better.
  * 
  * o Instead of running over the actual elements, run over the bindings for those objects
  * o Delete the bindings as they match
  * o No need to set the targets because that's already done
  * o Instead of build surplus from the remaining elements, do it from remaining bindings.
  * o Handle the case where an element doesn't have a field,
  *   in which case the expected should be empty
  * o Take a Collection as an arg to the constructor
  * 
  * See the FixtureFixture specifications for examples
*/
public class ArrayFixture extends FitLibraryFixture {
	private Collection actuals;
	private boolean[] usedField;
	
	public ArrayFixture() {
	}
	public ArrayFixture(Iterator it) {
		setActualCollection(it);
	}
	public ArrayFixture(Collection actuals) {
	    setActualCollection(actuals);
	}
	public ArrayFixture(Object[] actuals) {
	    setActualCollection(actuals);
	}
	public void setActualCollection(Collection actuals) {
		this.actuals = actuals;
	}
	public void setActualCollection(Object[] actuals) {
	    setActualCollection(Arrays.asList(actuals));
	}
	public void setActualCollection(Iterator it) {
	    List actuals = new ArrayList();
		while (it.hasNext())
			actuals.add(it.next());
		setActualCollection(actuals);
	}
	public void doRows(Parse rows) {
		if (rows == null)
			throw new MissingRowFailureException();
		if (actuals == null)
			throw new FitFailureException("Actual list missing");
		try {
		    List bindings = new ArrayList();
		    if (!actuals.isEmpty())
		        bindings = bindLabels(rows.parts,actuals);
			Parse last = rows.last();
			rows = rows.more;
			while (rows != null) {
				doRow(rows,bindings);
				rows = rows.more;
			}
			showSurplus(bindings, last);
		} catch (Exception e) {
			exception(rows.leaf(),e);
		}
	}
	protected void showSurplus(List bindings, Parse last) {
		if (!bindings.isEmpty()) {
			last.more = buildSurplusRows(bindings);
			mark(last.more,"surplus");
		}
	}
		/** List<TypeAdapter[]> */
	protected List bindLabels(Parse labels, Collection actuals) throws Exception {
    	usedField = new boolean[labels.size()];
    	for (int i = 0; i < usedField.length; i++)
    		usedField[i] = false;
    	List bindings = new ArrayList();
        for (Iterator it = actuals.iterator(); it.hasNext(); ) {
            bindings.add(bindLabels(labels,it.next()));
        }
    	for (int i = 0; i < usedField.length; i++)
    		if (!usedField[i])
    			throw new NoSuchFieldFitFailureException(ExtendedCamelCase.camel(labels.at(i).text()));
        return bindings;
    }
	protected TypeAdapter[] bindLabels(Parse labels, Object element) throws Exception {
        TypeAdapter[] columnBindings = new TypeAdapter[labels.size()];
		for (int i = 0; i < columnBindings.length; i++) {
			String field = ExtendedCamelCase.camel(labels.text());
			try {
				columnBindings[i] = bindField(field,element);
				if (columnBindings[i] != null) {
					columnBindings[i].target = element;
					usedField[i] = true;
				}
			} catch (NoSuchFieldException e) {
				throw new NoSuchFieldFitFailureException(field);
			}
			labels = labels.more;
		}
		return columnBindings;
	}
	protected TypeAdapter bindField(String name, Object element) throws Exception {
	    if (element instanceof Map) {
	        Object value = ((Map)element).get(name);
	        if (value == null)
	            return null;
	    	return new MapTypeAdapter(value,this,name);
	    }
	    Class targetClass = element.getClass();
        try {
            return LibraryTypeAdapter.on(this, targetClass.getField(extendedCamel(name)));
        } catch (NoSuchFieldException e) {
            try {
                return getMethod(targetClass, "get "+name);
            } catch (NoSuchMethodException e1) {
                try {
					return getMethod(targetClass, "is "+name);
				} catch (NoSuchMethodException e2) {
					return null;
				}
            }
        }
	}
	protected TypeAdapter getMethod(Class targetClass, String getterName) throws NoSuchMethodException {
        String extendedCamel = extendedCamel(getterName);
        return LibraryTypeAdapter.on(this, targetClass.getMethod(extendedCamel, new Class[]{}));
    }
	protected void doRow(Parse row, List actuals) throws Exception {
	    if (actuals.isEmpty()) {
	        missing(row);
	        return;
	    }
	    int rowLength = row.parts.size();
	    TypeAdapter[] columnBindings = (TypeAdapter[])actuals.get(0);
	    if (rowLength < columnBindings.length)
	        throw new MissingCellsFailureException();
	    if (rowLength > columnBindings.length)
	        throw new ExtraCellsFailureException();
	    if (!actuals.isEmpty() && matchRow(row.parts,columnBindings))
	        actuals.remove(0);
	    else
	        missing(row);
	}
	protected void missing(Parse row) {
	    row.parts.addToBody(label("missing"));
	    wrong(row.parts);
    }
    protected boolean matchRow(Parse cells, TypeAdapter[] columnBindings) throws Exception {
		boolean matched = false;
		for (int i = 0; i < columnBindings.length; i++) {
			TypeAdapter adapter = columnBindings[i];
			if (adapter == null) {
				if (cells.text().equals(""))
					right(cells);
				else
					wrong(cells,"");
			}
			else {
				boolean match = matches(adapter,cells);
				if (match)
					right(cells);
				else if (matched)
				    wrong(cells,adapter.toString(adapter.get()));  // WAS wrong(cells,adapter.get().toString());
				else
					return false;
				matched = true;
			}
			cells = cells.more;
		}
		return true;
	}
	protected boolean matches(TypeAdapter adapter, Parse cell) throws Exception {
		return adapter.equals(LibraryTypeAdapter.parse(cell,adapter),
				adapter.get());
	}
	protected void mark(Parse rows, String message) {
        String annotation = label(message);
        while (rows != null) {
            wrong(rows.parts);
            rows.parts.addToBody(annotation);
            rows = rows.more;
        }
    }
    protected Parse buildSurplusRows(List surplusBindings) {
        Parse root = new Parse(null ,null, null, null);
        Parse next = root;
        for (Iterator it = surplusBindings.iterator(); it.hasNext(); ) {
            Parse surplusCells = buildSurplusCells((TypeAdapter[])it.next());
			next = next.more = new Parse("tr", null, surplusCells, null);
        }
        return root.more;
    }
    protected Parse buildSurplusCells(TypeAdapter[] columnBindings) {
        if (columnBindings.length == 0) {
            Parse nil = new Parse("td", "null", null, null);
            nil.addToTag(" colspan="+columnBindings.length);
            return nil;
        }
        Parse root = new Parse(null, null, null, null);
        Parse next = root;
        for (int i=0; i<columnBindings.length; i++) {
            next = next.more = new Parse("td", "&nbsp;", null, null);
            TypeAdapter a = columnBindings[i];
            if (a == null) {
                ignore (next);
            } else {
                try {
                    next.body = gray(escape(a.toString(a.get())));
                } catch (Exception e) {
                    exception(next, e);
                }
            }
        }
        return root.more;
    }
}
