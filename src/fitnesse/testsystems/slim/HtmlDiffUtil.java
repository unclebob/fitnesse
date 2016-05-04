package fitnesse.testsystems.slim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import fitnesse.html.HtmlUtil;

public class HtmlDiffUtil {

  public abstract static class Builder {
    List<Character> text;
    final Patch<Character> patch;
    final StringBuilder stringBuilder = new StringBuilder();
    boolean isInDiffArea = false;
    String openingTag = "<span class=\"diff\">";
    String closingTag = "</span>";

    protected Builder(final String actual, final String expected) {
      this.patch = DiffUtils.diff(stringToCharacterList(actual),
          stringToCharacterList(expected));
    }

    public Builder setOpeningTag(final String openingTag) {
      this.openingTag = openingTag;
      return this;
    }

    public Builder setClosingTag(final String closingTag) {
      this.closingTag = closingTag;
      return this;
    }

    public String build() {
      for (int i = 0; i < getText().size(); i++) {
        addElementToStringBuilder(i);
      }
      return stringBuilder.toString();
    }

    protected abstract Chunk<Character> getChunk(Delta<Character> d);

    private void addElementToStringBuilder(final int i) {
      Delta<Character> delta = getDeltaByIndex(i, patch);
      addOpeningTagIfDeltaAtFirstElement(i, delta);
      addTagIfDeltaSwitching(delta);
      addCharacterFromDeltaOrText(i, delta);
      addClosingTagIfDeltaAtLastElement(i, delta);
    }

    private void addCharacterFromDeltaOrText(final int i,
        final Delta<Character> delta) {
      if (delta == null) {
        stringBuilder.append(HtmlUtil.escapeHTML(getText().get(i).toString()));
      } else {
        stringBuilder.append(HtmlUtil
            .escapeHTML(getFromChunkByIndex(getChunk(delta), i).toString()));
      }
    }

    private void addTagIfDeltaSwitching(final Delta<Character> delta) {
      if (delta == null) {
        if (isInDiffArea) {
          addClosingTag();
        }
      } else {
        if (!isInDiffArea) {
          addOpeningTag();
        }
      }
    }

    private void addClosingTagIfDeltaAtLastElement(final int i,
        final Delta<Character> delta) {
      if (delta != null && isLastElement(i)) {
        addClosingTag();
      }
    }

    private void addClosingTag() {
      isInDiffArea = false;
      stringBuilder.append(closingTag);
    }

    private void addOpeningTagIfDeltaAtFirstElement(final int i,
        final Delta<Character> delta) {
      if (delta != null && isFirstElement(i)) {
        addOpeningTag();
      }
    }

    private void addOpeningTag() {
      isInDiffArea = true;
      stringBuilder.append(openingTag);
    }

    private Delta<Character> getDeltaByIndex(final int i,
        final Patch<Character> patch) {
      for (Delta<Character> delta : patch.getDeltas()) {
        if (isInChunk(getChunk(delta), i)) {
          return delta;
        }
      }
      return null;
    }

    private boolean isInChunk(final Chunk<Character> chunk, final int i) {
      return i >= chunk.getPosition() && i < chunk.getPosition() + chunk.size();
    }

    private Character getFromChunkByIndex(final Chunk<Character> chunk,
        final int i) {
      int j = i - chunk.getPosition();
      return chunk.getLines().get(j);
    }

    private List<Character> getText() {
      return text;
    }

    private boolean isFirstElement(final int i) {
      return i == 0;
    }

    private boolean isLastElement(final int i) {
      return i == getText().size() - 1;
    }

    protected List<Character> stringToCharacterList(final String s) {
      if (s == null || s.isEmpty()) {
        return Collections.<Character> emptyList();
      }
      List<Character> characterList = new ArrayList<>(s.length());
      for (char c : s.toCharArray()) {
        characterList.add(c);
      }
      return characterList;
    }

  }

  public static class ActualBuilder extends Builder {

    public ActualBuilder(final String actual, final String expected) {
      super(actual, expected);
      text = stringToCharacterList(actual);
    }

    @Override
    protected Chunk<Character> getChunk(final Delta<Character> d) {
      return d.getOriginal();
    }

  }

  public static class ExpectedBuilder extends Builder {

    public ExpectedBuilder(final String actual, final String expected) {
      super(actual, expected);
      text = stringToCharacterList(expected);
    }

    @Override
    protected Chunk<Character> getChunk(final Delta<Character> d) {
      return d.getRevised();
    }

  }

  public static String buildActual(final String actual, final String expected) {
    return new ActualBuilder(actual, expected).build();
  }

  public static String buildExpected(final String actual,
      final String expected) {
    return new ExpectedBuilder(actual, expected).build();
  }

}
