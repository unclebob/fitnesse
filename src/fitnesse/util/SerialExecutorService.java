package fitnesse.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.NotImplementedException;

/**
 * This implementation of {@link java.util.concurrent.ExecutorService} is a dummy/debug version of an execution
 * service. The tasks are executed instantly, in the current execution thread.
 */
public class SerialExecutorService implements ExecutorService {

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    try {
      return new FutureIsNow<>(task.call());
    } catch (Exception e) {
      return new FutureIsNow<>(e);
    }
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    task.run();
    return new FutureIsNow<>(result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    task.run();
    return new FutureIsNow<Object>(null);
  }

  @Override
  public void execute(Runnable command) {
    command.run();
  }

  @Override
  public void shutdown() {
  }

  @Override
  public List<Runnable> shutdownNow() {
    return Collections.emptyList();
  }

  @Override
  public boolean isShutdown() {
    return true;
  }

  @Override
  public boolean isTerminated() {
    return true;
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return true;
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    throw new NotImplementedException();
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    throw new NotImplementedException();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    throw new NotImplementedException();
  }

}

class FutureIsNow<T> implements Future<T> {

  private final T result;
  private final Exception exc;

  public FutureIsNow(T result) {
    this.result = result;
    this.exc = null;
  }

  public FutureIsNow(Exception exc) {
    this.result = null;
    this.exc = exc;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    if (exc != null) {
      throw new ExecutionException(exc);
    }
    return result;
  }

  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return get();
  }
}
