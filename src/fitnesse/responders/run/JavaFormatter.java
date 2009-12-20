package fitnesse.responders.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnesse.responders.run.formatters.BaseFormatter;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class JavaFormatter extends BaseFormatter {
  
  public static final String SUMMARY_FOOTER = "</table>";
  public static final String SUMMARY_HEADER = "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>";
  public interface ResultsRepository {
    void open(String string) throws IOException;
    void close() throws IOException;
    void write(String content) throws IOException;
  }
  public static class FolderResultsRepository implements ResultsRepository{
    private String outputPath;
    private Writer currentWriter;
    public FolderResultsRepository(String outputPath, String fitNesseRoot) throws IOException{
      this.outputPath=outputPath;
      initFolder(fitNesseRoot);
    }
    @Override
    public void close() throws IOException {
      if (currentWriter!=null) {
        currentWriter.write("</body></html>");
        currentWriter.close();      
      }
    }
    @Override
    public void open(String testName) throws IOException {
      currentWriter=new FileWriter(new File(outputPath,testName+".html"));
      
      currentWriter.write("<head><title>");
      currentWriter.write(testName);
      currentWriter.write(
          "</title><link rel='stylesheet' type='text/css' href='fitnesse.css' media='screen'/>"
              + "<link rel='stylesheet' type='text/css' href='fitnesse_print.css' media='print'/>"
              + "<script src='fitnesse.js' type='text/javascript'></script>"
              + "</head><body><h2>");
      currentWriter.write(testName);
      currentWriter.write("</h2>");

    }
    @Override
    public void write(String content) throws IOException {
      currentWriter.write(content.replace("src=\"/files/images/", "src=\"images/"));
    }
    public void addFile(File f, String relativeFilePath) throws IOException {
      File dst = new File(outputPath, relativeFilePath);
      dst.getParentFile().mkdirs();
      copy(f, dst);
    }
   
    private void copy(File src, File dst) throws IOException {
      InputStream in = new FileInputStream(src);
      OutputStream out = new FileOutputStream(dst);
      // Transfer bytes from in to out
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      in.close();
      out.close();
    }
    private void initFolder(String fitnesseRoot) throws IOException{
      File filesFolder = new File(
          new File(new File(fitnesseRoot), "FitNesseRoot"), "files");
      File cssDir = new File(filesFolder, "css");
      addFile(new File(cssDir, "fitnesse_base.css"),
          "fitnesse.css");
      File javascriptDir = new File(filesFolder, "javascript");
      addFile(new File(javascriptDir, "fitnesse.js"),
          "fitnesse.js");
      File imagesDir = new File(filesFolder, "images");
      addFile(new File(imagesDir, "collapsableOpen.gif"),
          "images/collapsableOpen.gif");
      addFile(new File(imagesDir, "collapsableClosed.gif"),
          "images/collapsableClosed.gif");
    }
  }
  public static TestSummary totalSummary=new TestSummary();

  @Override
  public void writeHead(String pageType) throws Exception {

  }

  public String getFullPath(final WikiPage wikiPage) throws Exception{
    return new WikiPagePath(wikiPage).toString();
//    
//    Stack<String> stack=new Stack<String>();
//    WikiPage current=wikiPage;
//    while (current!=null){
//       stack.push(current.getName());
//       current=current.getParent();
//    }
//    StringBuffer sb=new StringBuffer();
//    while (stack.size()>0){
//      if (sb.length()>0) sb.append(".");
//      sb.append(stack.pop());
//    }
//    return sb.toString();
  }
  private List<String> visitedTestPages=new ArrayList<String>();
  private Map<String,TestSummary> testSummaries=new HashMap<String,TestSummary>();
  
  @Override
  public void newTestStarted(WikiPage test, long time) throws Exception {
    resultsRepository.open(getFullPath(test));
    
  }

  @Override
  public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log)
      throws Exception {
  }

  @Override
  public void testComplete(WikiPage test, TestSummary testSummary) throws Exception {
    System.out.println (test.getName() + " r " + testSummary.getRight() + " w "+  testSummary.getWrong() + " e " + testSummary.getExceptions());
    String fullPath=getFullPath(test);
    visitedTestPages.add(fullPath);
    totalSummary.add(testSummary);
    testSummaries.put(fullPath, testSummary);
    resultsRepository.close();
  }

  @Override
  public void testOutputChunk(String output) throws Exception {
    resultsRepository.write(output);
  }

  @Override
  public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
      throws Exception {

  }

  private ResultsRepository resultsRepository;

  public TestSummary getTotalSummary() {
    return totalSummary;
  }

  public void setTotalSummary(TestSummary testSummary) {
    totalSummary=testSummary;
  }

  public void setResultsRepository(ResultsRepository mockResultsRepository) {
    this.resultsRepository = mockResultsRepository;
    
  }
  
  
  /** singleton ugliness */
  private static JavaFormatter instance;
  JavaFormatter(){
    
  }
  public synchronized static JavaFormatter getInstance() {
    if (instance==null) instance=new JavaFormatter();
    return instance;
  }

  public void writeSummary(String suiteName) throws IOException {
    resultsRepository.open(suiteName);
    resultsRepository.write(SUMMARY_HEADER);
    for (String s:visitedTestPages){
      resultsRepository.write(summaryRow(s,testSummaries.get(s)));
    }
    resultsRepository.write(SUMMARY_FOOTER);
    resultsRepository.close();
  }

  public String summaryRow(String testName, TestSummary testSummary) {
    StringBuffer sb=new StringBuffer();
    sb.append("<tr class=\"").append(getCssClass(testSummary)).append(
        "\"><td>").append("<a href=\"").append(testName)
        .append(".html\">").append(testName).append("</a>").append(
            "</td><td>").append(testSummary.right).append("</td><td>")
        .append(testSummary.wrong).append("</td><td>").append(
            testSummary.exceptions).append("</td></tr>");
    return sb.toString();
  }
  private String getCssClass(TestSummary ts) {
    if (ts.exceptions > 0)
      return "error";
    if (ts.wrong > 0)
      return "fail";
    if (ts.right > 0)
      return "pass";
    return "plain";
  }

}
