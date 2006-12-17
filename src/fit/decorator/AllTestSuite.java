package fit.decorator;

import junit.framework.Test;
import fit.decorator.performance.MaxTimeTest;
import fit.decorator.performance.TimeRangeTest;
import fit.decorator.util.DeltaTest;
import fit.decorator.util.TableTest;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
    public static Test suite()
    {
        return TestSuiteMaker.makeSuite("fit.decorator", new Class[] {
            CopyAndAppendLastRowTest.class,
            IncrementColumnsValueTest.class,
            LoopTest.class,
            MaxTimeTest.class,
            TimeRangeTest.class,
            DeltaTest.class,
            TableTest.class,
        });
  }
}
