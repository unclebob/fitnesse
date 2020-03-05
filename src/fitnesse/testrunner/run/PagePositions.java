package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import org.apache.velocity.util.StringBuilderWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Describes the order in which pages will be executed in a test run.
 */
public class PagePositions {
  public static final String PAGE_HEADER = "Page";
  public static final String ORDER_HEADER = "Order";

  private final Map<List<Object>, List<Object>> groupCache = new HashMap<>();
  private String lineSep = "\n";
  private List<String> groupNames = new ArrayList<>(2);
  private Map<String, List<PagePosition>> positions = new LinkedHashMap<>();

  public List<String> getGroupNames() {
    return groupNames;
  }

  public Integer getGroupIndex(String name) {
    return groupNames.indexOf(name);
  }

  public List<String> getPages() {
    return new ArrayList<>(positions.keySet());
  }

  public List<PagePosition> getPositions(String page) {
    return positions.computeIfAbsent(page, p -> new LinkedList<>());
  }

  public boolean hasPositions(String page) {
    return positions.containsKey(page);
  }

  public void addPosition(String page, List<Object> group, int positionInGroup) {
    // re-use group objects if same elements are used to define group
    List<Object> groupFromCache = groupCache.computeIfAbsent(group, g -> group);
    getPositions(page).add(new PagePosition(groupFromCache, positionInGroup));
  }

  public Comparator<String> createByPositionInGroupComparator() {
    Map<String, Integer> testOrdering = new HashMap<>();
    for (String page : getPages()) {
      for (PagePosition pos : getPositions(page)) {
        testOrdering.put(page, pos.getPositionInGroup());
      }
    }
    return (page1, page2) -> {
      int pos1 = testOrdering.getOrDefault(page1, -1);
      int pos2 = testOrdering.getOrDefault(page2, -1);
      return pos1 == pos2 ? page1.compareTo(page2) : pos1 - pos2;
    };
  }

  public void appendTo(Writer writer, String groupSep) throws IOException {
    appendHeader(writer, groupSep);
    for (Map.Entry<String, List<PagePosition>> entry : positions.entrySet()) {
      appendIndices(writer, entry.getKey(), entry.getValue(), groupSep);
    }
    writer.flush();
  }

  public static PagePositions parseFrom(Reader reader, String groupSep) throws IOException {
    BufferedReader b = new BufferedReader(reader);
    PagePositions positions = new PagePositions();
    positions.parseHeader(b, groupSep);
    positions.parseRows(b, groupSep);
    return positions;
  }

  protected void parseHeader(BufferedReader reader, String groupSep) throws IOException {
    splitNextLine(reader, groupSep)
      .map(parts -> Collections.addAll(groupNames, stripHeadAndTail(parts)))
      .orElseThrow(() -> new IllegalArgumentException("No header line"));
  }

  protected void parseRows(BufferedReader reader, String groupSep) throws IOException {
    while(true) {
      Optional<String[]> linePresent = splitNextLine(reader, groupSep);
      if (!linePresent.isPresent()) {
        break;
      }
      String[] parts = linePresent.get();
      List<Object> group = asList((Object[]) stripHeadAndTail(parts));
      Integer pos = Integer.valueOf(parts[parts.length - 1]);
      addPosition(parts[0], group, pos);
    }
  }

  protected String[] stripHeadAndTail(String[] parts) {
    int count = parts.length;
    if (count < 3) throw new IllegalArgumentException("Expected at least 2 columns, got: " + count);
    return Arrays.copyOfRange(parts, 1, count - 1);
  }

  protected Optional<String[]> splitNextLine(BufferedReader reader, String groupSep) throws IOException {
    return Optional.ofNullable(reader.readLine()).map(l -> l.split(groupSep));
  }

  @Override
  public String toString() {
    try {
      Writer writer = new StringBuilderWriter();
      appendTo(writer, "\t");
      return writer.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to write data", e);
    }
  }

  protected void appendHeader(Writer writer, String groupSep) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(PAGE_HEADER).append(groupSep);
    sb.append(String.join(groupSep, groupNames));
    sb.append(groupSep);
    sb.append(ORDER_HEADER);
    sb.append(lineSep);
    writer.append(sb.toString());
  }

  protected void appendIndices(Writer writer, String page, List<PagePosition> indices, String groupSep) throws IOException {
    StringBuilder sb = new StringBuilder();
    for (PagePosition index : indices) {
      sb.append(page)
        .append(groupSep);
      for (Object dimension : index.getGroup()) {
        String dimensionStr = formatDimension(dimension);
        sb.append(dimensionStr).append(groupSep);
      }
      sb.append(index.getPositionInGroup()).append(lineSep);
      writer.append(sb.toString());
      sb.setLength(0);
    }
  }

  public String formatDimension(Object dimension) {
    if (dimension instanceof WikiPageIdentity) {
      return ((WikiPageIdentity) dimension).testSystem();
    } else {
      return String.valueOf(dimension);
    }
  }
}
