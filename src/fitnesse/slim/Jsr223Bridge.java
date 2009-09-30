package fitnesse.slim;


import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.Closeable;
import java.io.IOException;

public abstract class Jsr223Bridge {

  private ScriptEngine engine;

  public abstract Object getStatementExecutor() throws Exception;

  public abstract Object invokeMethod(Object thiz, String name, Object... args)
      throws Exception;

  public ScriptEngine getScriptEngine() {
    if (engine == null) {
      engine = new ScriptEngineManager().getEngineByName(getEngineName());
    }
    return engine;
  }

  public void close() {
    try {
      ((Closeable)getScriptEngine()).close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public abstract String getEngineName();
}
