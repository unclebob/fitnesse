package fit;

import fit.exception.CouldNotParseFitFailureException;
import fit.exception.FitFailureException;
import fit.exception.FitMatcherException;

import java.text.ParseException;

public class CellComparator {

  private Object result = null;

  private Object expected = null;

  private TypeAdapter typeAdapter;

  private Parse cell;

  private Fixture fixture;


  CellComparator(Object Result, Object Expected, TypeAdapter typeadapter, Parse cell, Fixture fix) {

    this.result = Result;
    this.expected = Expected;
    this.typeAdapter = typeadapter;
    this.cell = cell;
    this.fixture = fix;
  }


  public void compareCellToResult(TypeAdapter a, Parse theCell) {
    typeAdapter = a;
    cell = theCell;

    try {
      result = typeAdapter.get();
      expected = parseCell();
      if (expected instanceof Fixture.Unparseable)
        tryRelationalMatch();
      else
        compare();
    } catch (Exception e) {
      fixture.exception(cell, e);
    }
  }

  private void compare() {
    if (typeAdapter.equals(expected, result)) {
      fixture.right(cell);
    } else {
      fixture.wrong(cell, typeAdapter.toString(result));
    }
  }

  private Object parseCell() {
    try {
      return typeAdapter.isRegex ? cell.text() : typeAdapter.parse(cell.text());
    }
    // Ignore parse exceptions, print non-parse exceptions,
    // return null so that compareCellToResult tries relational matching.
    catch (NumberFormatException e) {
    } catch (ParseException e) {
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new Fixture.Unparseable();
  }

  private void tryRelationalMatch() {
    Class<?> adapterType = typeAdapter.type;
    FitFailureException cantParseException = new CouldNotParseFitFailureException(cell.text(), adapterType
      .getName());
    if (result != null) {
      FitMatcher matcher = new FitMatcher(cell.text(), result);
      try {
        if (matcher.matches())
          fixture.right(cell);
        else
          fixture.wrong(cell);
        cell.body = matcher.message();
      } catch (FitMatcherException fme) {
        fixture.exception(cell, cantParseException);
      } catch (Exception e) {
        fixture.exception(cell, e);
      }
    } else {
      // TODO-RcM Is this always accurate?
      fixture.exception(cell, cantParseException);
    }
  }
}
