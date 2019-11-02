package fitnesse.slim;

import java.util.concurrent.*;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

public class StatementTimeoutExecutor implements StatementExecutorInterface {
  private final StatementExecutorInterface inner;
  private final Integer timeout;
  private final ExecutorService service;

  private StatementTimeoutExecutor(StatementExecutorInterface inner, Integer timeout, ExecutorService service) {
    this.inner = inner;
    this.timeout = timeout;
    this.service = service;
  }

  public static StatementExecutorInterface decorate(StatementExecutorInterface inner, Integer timeout) {
    return decorate(inner, timeout, newSingleThreadExecutor());
  }

  public static StatementExecutorInterface decorate(StatementExecutorInterface inner, Integer timeout, ExecutorService service) {
    return new StatementTimeoutExecutor(inner, timeout, service);
  }

  @Override
  public void assign(final String name, final Object value) {
    inner.assign(name, value);
  }

  @Override
  public Object getSymbol(String symbolName) {
    return inner.getSymbol(symbolName);
  }

  @Override
  public Object getSymbolObject(String symbolName) {
    return inner.getSymbolObject(symbolName);
  }

  @Override
  public Object getInstance(String instanceName) {
    return inner.getInstance(instanceName);
  }

  @Override
  public boolean stopHasBeenRequested() {
    return inner.stopHasBeenRequested();
  }

  @Override
  public void reset() {
    inner.reset();
  }

  @Override
  public void setInstance(String actorInstanceName, Object actor) {
    inner.setInstance(actorInstanceName, actor);
  }

  @Override
  public void addPath(String path) throws SlimException {
    inner.addPath(path);
  }

  @Override
  public void create(final String instanceName, final String className, final Object... constructorArgs) throws SlimException {
    Future<?> submit = service.submit(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        inner.create(instanceName, className, constructorArgs);
        return true;
      }
    });
    getWithTimeout(submit);
  }

  @Override
  public Object callAndAssign(final String symbolName, final String instanceName, final String methodsName, final Object... arguments) throws SlimException {
    Future<Object> submit = service.submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        return inner.callAndAssign(symbolName, instanceName, methodsName, arguments);
      }
    });
    return getWithTimeout(submit);
  }

  @Override
  public Object call(final String instanceName, final String methodName, final Object... arguments) throws SlimException {
    Future<Object> submit = service.submit(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        return inner.call(instanceName, methodName, arguments);
      }
    });
    return getWithTimeout(submit);
  }

  private <T> T getWithTimeout(Future<T> submit) throws SlimException {
    try {
      return submit.get(timeout, SECONDS);
    } catch (TimeoutException e) {
      throw new SlimException(Integer.toString(timeout), SlimServer.TIMED_OUT, true);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SlimError("Statement execution was interrupted", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof SlimException) {
        throw (SlimException) cause;
      } else {
        throw new SlimException(e.getCause());
      }
    }
  }
}
