package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PublishResponder implements Responder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    //todo: write css and images from jar resources
    Path templatePath = Paths.get(context.getRootPagePath(), "files", "fitnesse", "publish.html");
    String template = String.join(System.lineSeparator(), Files.readAllLines(templatePath));
    Publisher publisher = new Publisher(template, request.getInput("destination"), context.getRootPage().getPageCrawler(), this::writePage);
    SimpleResponse response = new SimpleResponse();
    response.setContent(publisher.traverse(context.getRootPage()));
    return response;
  }

  private void writePage(String content, String path) {
    Path directory = Paths.get(path).getParent();
    try {
      if (!Files.exists(directory))
        Files.createDirectories(directory);
      Files.write(Paths.get(path), content.getBytes());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
