/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 * 
 */
package fitLibrary;

import fit.*;

/**
 *	A TypeAdapter that takes the value directly. The value could be
 *  from a Map, which takes a String key, corresponding to the header
 *  label in an ArrayFixture.
 *  This allows dynamic collections, such as from a JTable.
 */
public class MapTypeAdapter extends TypeAdapter {
    private TypeAdapter typeAdapter;
    public Object value;
    
    public MapTypeAdapter(Object value, Fixture fixture, Object key) throws SecurityException, NoSuchFieldException {
        this.value = value;
        Class type = value.getClass();
        typeAdapter = TypeAdapter.adapterFor(type);
        typeAdapter.target = this;
        typeAdapter.field = field;
        typeAdapter.fixture = fixture;
        typeAdapter.type = type;
        this.fixture = fixture;
    }
    public Object get() {
        return value;
    }
    public Object parse(String s) throws Exception {
        return typeAdapter.parse(s);
    }
    public String toString() {
        return typeAdapter.toString();
    }
}
