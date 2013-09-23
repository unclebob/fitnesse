package fit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fit.exception.FitFailureException;


public class Dispatcher {
	public final Map<String, Object> summary;
	public Counts counts;

	private final FixtureListener listener;
	
	private static boolean forcedAbort = false;  //Semaphores
	
	
	public static class RunTime {
		long start   = System.currentTimeMillis();
		long elapsed = 0;

		public String toString() {
			elapsed = System.currentTimeMillis() - start;
			if (elapsed > 600000) {
				return d(3600000) + ":" + d(600000) + d(60000) + ":" + d(10000) + d(1000);
			} else {
				return d(60000) + ":" + d(10000) + d(1000) + "." + d(100) + d(10);
			}
		}

		String d(long scale) {
			long report = elapsed / scale;
			elapsed -= report * scale;
			return Long.toString(report);
		}
	}

	public Dispatcher(FixtureListener listener) {
		counts  = new Counts();
		summary = new HashMap<String, Object>();
		this.listener = listener;
	}

	public Dispatcher() {
		this(new NullFixtureListener());
	}

	public static void setForcedAbort(boolean state) {
		forcedAbort = state;
	}  //Semaphores

	@Deprecated
	public static boolean aborting() {
		return forcedAbort;
	}  //Semaphores

	public void doTables(Parse tables) {
		summary.put("run date", new Date());
		summary.put("run elapsed time", new RunTime());
		counts = new Counts();
		while (tables != null) {
			processTable(tables);
			tables = tables.more;
		}
		listener.tablesFinished(counts);
		cleanup();
	}
	
	protected void cleanup() {
		Fixture.ClearSymbols();
		SemaphoreFixture.ClearSemaphores(); //Semaphores:  clear all at end
	}

	private void processTable(Parse table) {
		Parse heading = table.at(0, 0, 0);
		if (forcedAbort) {
			ignore(heading);  //Semaphores: ignore on failed lock
		} else if (heading != null) {
			try {
				BaseFixture fixture = getLinkedFixtureWithArgs(table);
				fixture.doTable(table);
			} catch (Throwable e) {
				exception(heading, e);
			} finally {
				listener.tableFinished(table);
			}
		}
	}

	private void ignore(Parse cell) {
		cell.addToTag(" class=\"ignore\"");
		counts.ignores++;
	}
	
	private BaseFixture getLinkedFixtureWithArgs(Parse tables) throws Throwable {
		Parse header    = tables.at(0, 0, 0);
		BaseFixture fixture = loadFixture(header.text());
		fixture.counts  = counts;
		fixture.summary = summary;
		fixture.getArgsForTable(tables);
		return fixture;
	}
	
    public static BaseFixture loadFixture(String fixtureName) throws Throwable {
      return FixtureLoader.instance().disgraceThenLoad(fixtureName);
    }

	public void exception(Parse cell, Throwable exception) {
		while (exception.getClass().equals(InvocationTargetException.class)) {
			exception = ((InvocationTargetException) exception).getTargetException();
		}
		if (isFriendlyException(exception)) {
			cell.addToBody("<hr/>" + BaseFixture.label(exception.getMessage()));
		} else {
			final StringWriter buf = new StringWriter();
			exception.printStackTrace(new PrintWriter(buf));
			cell.addToBody("<hr><pre><div class=\"fit_stacktrace\">" + (buf.toString()) + "</div></pre>");
		}
		cell.addToTag(" class=\"error\"");
		counts.exceptions++;
	}
	
	private boolean isFriendlyException(Throwable exception) {
		return exception instanceof FitFailureException;
	}
}