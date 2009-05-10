package fitnesse.threadlocal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThreadLocalUtilTest {
  private String valueFound;

  @Before
  public void init() {
    valueFound = "";
  }

  @After
  public void clear() {
    ThreadLocalUtil.clear();
  }

  @Test
  public void assertCanSetAndGetValue() {
    ThreadLocalUtil.setValue("CurrentPage", "cp");
    assertEquals("cp", ThreadLocalUtil.getValue("CurrentPage"));
  }

  @Test
  public void assertOnceSetAdditionalSetsIgnored() {
    ThreadLocalUtil.setValue("CurrentPage", "cp");
    ThreadLocalUtil.setValue("CurrentPage", "xx");
    assertEquals("cp", ThreadLocalUtil.getValue("CurrentPage"));
  }

  @Test
  public void assertOnceSetAdditionalSetsIncreasesSetCount() {
    ThreadLocalUtil.setValue("CurrentPage", "cp");
    ThreadLocalUtil.setValue("CurrentPage", "xx");
    assertEquals(2, ThreadLocalUtil.getSetCount("CurrentPage"));
  }

  @Test
  public void assertThatDefaultValueIsNotReturnedIfValueIsSet() {
    ThreadLocalUtil.setValue("key", "value");
    assertEquals("value", ThreadLocalUtil.getValue("key", "xx"));
  }

  @Test
  public void assertThatDefaultValueReturnedIfValueNotSet() {
    assertEquals("xx", ThreadLocalUtil.getValue("bogus", "xx"));
  }

  @Test
  public void assertCannotReadValueSetInOneThreadInAnotherThread()
      throws Exception {
    ThreadLocalUtil.setValue("CurrentPage", "cp");

    Thread otherThread = new Thread(new Runnable() {
      public void run() {
        valueFound = ThreadLocalUtil.getValue("CurrentPage");
      }
    });

    otherThread.start();
    otherThread.join();
    assertNull(valueFound);
  }

  CountDownLatch t1Latch = new CountDownLatch(1);
  CountDownLatch t2Latch = new CountDownLatch(1);

  class T1 implements Runnable {
    public void run() {
      try {
        ThreadLocalUtil.setValue("t1", "value");
        t2Latch.countDown();
        Thread.yield();
        t1Latch.await();
        valueFound = ThreadLocalUtil.getValue("t1");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  class T2 implements Runnable {
    public void run() {
      try {
        t2Latch.await();
        ThreadLocalUtil.clear();
        t1Latch.countDown();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  public void assertThatClearInOneThreadDoesNotMessUpAnotherThread()
      throws InterruptedException {
    Thread t1 = new Thread(new T1());
    Thread t2 = new Thread(new T2());
    t1.start();
    t2.start();
    t1.join();
    t2.join();
    assertEquals("value", valueFound);
  }
}
