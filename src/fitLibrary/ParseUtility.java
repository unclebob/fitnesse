/*
 * @author Rick Mugridge on Dec 27, 2004
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import fit.Parse;

/**
 *
 */
public class ParseUtility {
    public static final String ASCII_ENCODING = "ASCII";
    private static final String START_BODY = "<body>";
    public static final String END_BODY = "</body>";

    public static String toString(Parse tables) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        tables.print(printWriter);
        printWriter.close();
        return stringWriter.toString();
    }
    public static void printParse(Parse tables, String title) {
        System.out.println("---------Parse tables for "+title+":----------");
        if (tables != null) {
            PrintWriter printWriter = new PrintWriter(System.out);
            tables.print(printWriter);
            printWriter.flush();
        }
        System.out.println("-------------------");
    }
    public static void addRowToTable(Parse table, String[] cells) {
        if (cells.length == 0)
            throw new RuntimeException("Can't have an empty row.");
        Parse root = new Parse(null, null, null, null);
        Parse here = root;
        for (int i = 0; i < cells.length; i++) {
            here.more = new Parse("td",cells[i],null,null);
            here = here.more;
        }
        table.parts.last().more = new Parse("tr","",root.more,null);
    }
    protected void addRowToTable(Parse table, String s) {
        addRowToTable(table, new String[]{s});
    }
    /** Append the second Parse to the first, which is a setup.
     *  Transfer trailer on the front to the leader of the back.
     */
    public static void appendToSetUp(Parse front, Parse back) {
        if (back == null)
            return;
        changeHeader(front,removeHeader(back));
        fixTrailers(front,back);
        front.last().more = back;
    }
    /** Append the second Parse to the first, transferring
     *  any trailer on the front to the leader of the back */
    public static void append(Parse front, Parse back) {
        if (back == null)
            return;
        removeHeader(back);
        fixTrailers(front,back);
        front.last().more = back;
    }
    /** Move the last trailer of the front onto the leader of the back.
     *  That's because the only the trailer of the last table is printed.
     */
    protected static void fixTrailers(Parse front, Parse back) {
        // NB, Parse makes the leader a "\n" by default.
        Parse frontLast = front.last();
        String frontTrailer = frontLast.trailer;
        String extra = frontTrailer;
        int index = frontTrailer.indexOf(END_BODY);
        if (index >= 0)
            extra = frontTrailer.substring(0,index);
        if (!extra.equals("")) {
            if (back.leader.equals("\n<br>"))
                back.leader = extra + back.leader;
            else
                back.leader = extra + "<br>" + back.leader;
        }
        frontLast.trailer = "";
    }
    public static String removeHeader(Parse tables) {
        int index = tables.leader.indexOf(START_BODY);
        if (index < 0)
            return "";
        index += START_BODY.length();
        String result = tables.leader.substring(0,index);
        tables.leader = tables.leader.substring(index);
        return result;
    }
    public static void changeHeader(Parse tables, String tablesHeader) {
        int index = tables.leader.indexOf(START_BODY);
        if (index < 0)
            tables.leader = tablesHeader + tables.leader;
        else
            tables.leader = tablesHeader + tables.leader.substring(index+START_BODY.length());
    }
    public static void completeTrailer(Parse tables) {
        Parse last = tables.last();
        int index = last.trailer.indexOf(END_BODY);
        if (index < 0)
            last.trailer += "\n</body></html>\n";
    }
    public static Parse copyParse(Parse tables) {
        if (tables == null)
            return null;
        Parse parse = new Parse("",tables.body,
                copyParse(tables.parts),copyParse(tables.more));
        parse.tag = tables.tag;
        parse.end = tables.end;
        parse.leader = tables.leader;
        parse.trailer = tables.trailer;
        return parse;
    }
    public static void writeParse(File report, Parse parse) throws UnsupportedEncodingException, FileNotFoundException {
        PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(report),
                ASCII_ENCODING));
        parse.print(output);
        output.close();
    }
}
