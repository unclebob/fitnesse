package fitnesse.slim;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class StatementTimeoutExecutorTest {

  private static final String INSTANCE_NAME = "instanceName";
  private static final String CLASS_NAME = "className";
  private static final String ARG = "arg";
  private static final String SYMBOL_NAME = "symbol";
  private static final String METHOD_NAME = "method name";
  private static final int TIMEOUT = 4000;

  @Mock
  private StatementExecutorInterface inner;
  @Mock
  private ExecutorService executorService;
  private StatementExecutorInterface executor;

  @Before
  public void setup() {
    initMocks(this);
    executor = StatementTimeoutExecutor.decorate(inner, TIMEOUT);
  }

  @Test
  public void testCreateDelegatesToInner() throws SlimException {
    executor.create(INSTANCE_NAME, CLASS_NAME, ARG);
    verify(inner).create(INSTANCE_NAME, CLASS_NAME, ARG);
  }

  @Test
  public void testCallAndAssignDelegatesToInner() throws SlimException {
    when(inner.callAndAssign(SYMBOL_NAME, INSTANCE_NAME, METHOD_NAME, ARG)).thenReturn("answer");
    Object answer = executor.callAndAssign(SYMBOL_NAME, INSTANCE_NAME, METHOD_NAME, ARG);
    assertEquals("answer", answer);
  }

  @Test
  public void testCallDelegatesToInner() throws SlimException {
    when(inner.call(INSTANCE_NAME, METHOD_NAME, ARG)).thenReturn("answer");
    Object answer = executor.call(INSTANCE_NAME, METHOD_NAME, ARG);
    assertEquals("answer", answer);
  }

  @Test
  public void testCreateRethrowsSlimException() throws Exception {
    Exception error = givenSlimExceptionThrownOnFutureGet();
    Exception actual = createExpectingException();
    assertEquals(actual, error);
  }

  @Test
  public void testCallAndAssignRethrowsSlimException() throws Exception {
    Exception error = givenSlimExceptionThrownOnFutureGet();
    Exception actual = callAndAssignExpectingException();
    assertEquals(actual, error);
  }

  @Test
  public void testCallRethrowsSlimException() throws Exception {
    Exception error = givenSlimExceptionThrownOnFutureGet();
    Exception actual = callExpectingException();
    assertEquals(actual, error);
  }

  @Test
  public void testCreateWrapsOtherErrorsThrownInFuture() throws Exception {
    Exception error = givenRuntimeExceptionThrownOnFutureGet();
    Exception actual = createExpectingException();
    assertNotNull(actual);
    assertEquals(error, actual.getCause());
  }

  @Test
  public void testCallAndAssignWrapsOtherErrorsThrownInFuture() throws Exception {
    Exception error = givenRuntimeExceptionThrownOnFutureGet();
    Exception actual = callAndAssignExpectingException();
    assertNotNull(actual);
    assertEquals(error, actual.getCause());
  }

  @Test
  public void testCallWrapsOtherErrorsThrownInFuture() throws Exception {
    Exception error = givenRuntimeExceptionThrownOnFutureGet();
    Exception actual = callExpectingException();
    assertNotNull(actual);
    assertEquals(error, actual.getCause());
  }

  @Test
  public void testCreateThrowsWhenTimeoutExceptionThrown() throws Exception {
    givenTimeoutExceptionThrownOnFutureGet();
    Exception actual = createExpectingException();
    assertNotNull(actual);
    assertEquals("4000", actual.getMessage());
  }

  @Test
  public void testCallAndAssignThrowsWhenTimeoutExceptionThrown() throws Exception {
    givenTimeoutExceptionThrownOnFutureGet();
    Exception actual = callAndAssignExpectingException();
    assertNotNull(actual);
    assertEquals("4000", actual.getMessage());
  }

  @Test
  public void testCallThrowsWhenTimeoutExceptionThrown() throws Exception {
    givenTimeoutExceptionThrownOnFutureGet();
    Exception actual = callExpectingException();
    assertNotNull(actual);
    assertEquals("4000", actual.getMessage());
  }

  private Exception createExpectingException() {
    Exception actual = null;
    try {
      executor.create(INSTANCE_NAME, CLASS_NAME, ARG);
    } catch (SlimException e) {
      actual = e;
    }
    return actual;
  }

  private Exception callAndAssignExpectingException() {
    Exception actual = null;
    try {
      executor.callAndAssign(SYMBOL_NAME, INSTANCE_NAME, METHOD_NAME, ARG);
    } catch (SlimException e) {
      actual = e;
    }
    return actual;
  }

  private Exception callExpectingException() {
    Exception actual = null;
    try {
      executor.call(INSTANCE_NAME, METHOD_NAME, ARG);
    } catch (SlimException e) {
      actual = e;
    }
    return actual;
  }

  private Exception givenRuntimeExceptionThrownOnFutureGet() throws InterruptedException, ExecutionException, TimeoutException {
    RuntimeException error = new RuntimeException("error");
    ExecutionException executionException = new ExecutionException(error);
    givenExceptionThrownOnFutureGet(executionException);
    return error;
  }

  private Exception givenTimeoutExceptionThrownOnFutureGet() throws InterruptedException, ExecutionException, TimeoutException {
    TimeoutException error = new TimeoutException();
    givenExceptionThrownOnFutureGet(error);
    return error;
  }

  private Exception givenSlimExceptionThrownOnFutureGet() throws InterruptedException, ExecutionException, TimeoutException {
    SlimException error = new SlimException("error");
    ExecutionException executionException = new ExecutionException(error);
    givenExceptionThrownOnFutureGet(executionException);
    return error;
  }

  private void givenExceptionThrownOnFutureGet(Exception executionException) throws InterruptedException, ExecutionException, TimeoutException {
    executor = StatementTimeoutExecutor.decorate(inner, TIMEOUT, executorService);
    Future future = Mockito.mock(Future.class);
    when(future.get(TIMEOUT, TimeUnit.SECONDS)).thenThrow(executionException);
    when(executorService.submit(any(Callable.class))).thenReturn(future);
  }

}
