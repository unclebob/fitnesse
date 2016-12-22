package fitnesse.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runners.model.InitializationError;

/**
 * Use fitnesse.junit.FitNesseRunner.
 * @deprecated Use fitnesse.junit.FitNesseRunner.
 */
@Deprecated
public class FitNesseSuite extends FitNesseRunner {
  /**
   * The <code>Name</code> annotation specifies the name of the Fitnesse suite
   * to be run, e.g.: MySuite.MySubSuite
   * @deprecated Use FitNesseRunner.Suite
   */
  @Deprecated
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface Name {

    String value();
  }

  public FitNesseSuite(Class<?> suiteClass) throws InitializationError {
    super(suiteClass);
  }

  @Override
  protected String getSuiteName(Class<?> klass) throws InitializationError {
    Name nameAnnotation = klass.getAnnotation(Name.class);
    if (nameAnnotation == null) {
      throw new InitializationError("There must be a @Name annotation");
    }
    return nameAnnotation.value();
  }
}
