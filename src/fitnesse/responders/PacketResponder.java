package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PacketResponder implements SecureResponder {
  private SimpleResponse response;
  private WikiPage page;
  private JSONObject packet;
  List<JSONObject> tables = new ArrayList<>();
  private String jsonpFunction;

  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    response = new SimpleResponse();
    jsonpFunction = request.getInput("jsonp");
    String pageName = request.getResource();
    PageCrawler pageCrawler = context.getRootPage().getPageCrawler();
    WikiPagePath resourcePath = PathParser.parse(pageName);
    page = pageCrawler.getPage(resourcePath);

    if (page == null)
      response.setStatus(404);
    else {
      buildPacket();
      response.setStatus(200);
    }
    return response;
  }

  private void buildPacket() throws UnsupportedEncodingException {
    packet = new JSONObject();
    String html = page.getHtml();

    TableScanner scanner = new HtmlTableScanner(html);

    addTablesToPacket(scanner);
    if (jsonpFunction != null)
      response.setContent(String.format("%s(%s)", jsonpFunction, packet.toString(1)));
    else
      response.setContent(packet.toString(1));
  }

  private void addTablesToPacket(TableScanner scanner) throws JSONException {
    for (int i = 0; i < scanner.getTableCount(); i++) {
      Table t = scanner.getTable(i);
      addTableToPacket(t);
    }
    packet.put("tables", tables);
  }

  private void addTableToPacket(Table t) throws JSONException {
    JSONObject table = new JSONObject();
    JSONObject[] parents = new JSONObject[10];
    parents[0] = table;
    for (int row = 0; row < t.getRowCount(); row++) {
      List<String> rowList = getRowFromTable(t, row);
      int indent = getIndent(rowList);
      if (indent >= 0) {
        String name = rowList.get(indent);
        String value = rowList.size() > (indent + 1) ? rowList.get(indent + 1) : "";
        if (null == value || "".equals(value)) {
          JSONObject parent = new JSONObject();
          parents[indent].put(name, parent);
          parents[indent + 1] = parent;
        } else {
          parents[indent].put(name, value);
        }
      }
    }
    tables.add(table);
  }

  private int getIndent(List<String> rowList) {
    for (int indent = 0; indent < rowList.size(); indent++) {
      if (!"".equals(rowList.get(indent)))
        return indent;
    }
    return -1;
  }

  private List<String> getRowFromTable(Table t, int row) {
    List<String> rowList = new ArrayList<>();
    for (int col = 0; col < t.getColumnCountInRow(row); col++)
      rowList.add(t.getCellContents(col, row));
    return rowList;
  }

  @Override
  public SecureOperation getSecureOperation() {
    return new SecureReadOperation();
  }
}
