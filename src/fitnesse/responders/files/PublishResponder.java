package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.updates.ReplacingFileUpdate;
import fitnesse.updates.Update;
import fitnesse.wiki.PathParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PublishResponder implements Responder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    report = new StringBuilder();
    destination = request.getInput("destination");

    Path resourcePath = Paths.get(context.getRootPagePath(), PathParser.FILES, "fitnesse", "publishResources.txt");
    Files.readAllLines(resourcePath).stream().filter(l -> !l.trim().isEmpty()).map(this::makeUpdate).forEach(this::doUpdate);

    Path templatePath = Paths.get(context.getRootPagePath(), PathParser.FILES, "fitnesse", "publish.vm");
    String template = String.join(System.lineSeparator(), Files.readAllLines(templatePath));
    Publisher publisher = new Publisher(template, destination, context.getRootPage().getPageCrawler(), this::writePage);
    report.append(publisher.traverse(context.getRootPage()));

    SimpleResponse response = new SimpleResponse();
    response.setContent(report.toString());
    return response;
  }

  private void doUpdate(Update update) {
    try {
      update.doUpdate();
    } catch (IOException e) {
      report.append(e).append("<br>");
    }
  }

  private Update makeUpdate(String resource) {
    report.append(resource).append("<br>");
    return new ReplacingFileUpdate("fitnesse/resources/" + resource, Paths.get(destination, PathParser.FILES, "fitnesse", resource).toFile().getParentFile());
  }

  private void writePage(String content, String path) {
    Path directory = Paths.get(path).getParent();
    try {
      if (!Files.exists(directory))
        Files.createDirectories(directory);
      Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String destination;
  private StringBuilder report;
}
