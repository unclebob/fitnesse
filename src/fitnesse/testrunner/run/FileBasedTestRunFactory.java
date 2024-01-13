package fitnesse.testrunner.run;

import fitnesse.FitNesseContext;
import fitnesse.util.partitioner.ListPartitioner;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Partitions test pages based on the file in the files section,
 * indicated with the partitionIndexFile context parameter.
 */
public class FileBasedTestRunFactory extends PartitioningTestRunFactory {
  public static final String PARTITION_FILE_ARG = "partitionIndexFile";
  private final FitNesseContext context;
  private Function<File, ListPartitioner<WikiPage>> partitionFunctionFactory;

  public FileBasedTestRunFactory(FitNesseContext context) {
    this.context = context;
    partitionFunctionFactory = this::createPartitioner;
    setPartitioner(p -> new PageListPartitionerImpl(getPartitionFunction(p)));
  }

  @Override
  public boolean canRun(List<WikiPage> pages) {
    boolean canRun = super.canRun(pages) && !pages.isEmpty();
    if (canRun) {
      canRun = getFile(pages)
        .map(f -> {
          if (!f.canRead()) throw new IllegalArgumentException("Unable to read: " + f.getAbsolutePath());
          return true;
        })
        .orElse(false);
    }
    return canRun;
  }

  protected Optional<File> getFile(List<WikiPage> pages) {
    return Optional.ofNullable(getFilename(pages)).map(this::getPartitionFile);
  }

  protected ListPartitioner<WikiPage> getPartitionFunction(List<WikiPage> pages) {
    return getFile(pages)
      .map(partitionFunctionFactory)
      .orElseThrow(() -> new IllegalStateException("Unable to read file, which was present earlier: " + getFilename(pages)));
  }

  protected File getPartitionFile(String paramValue) {
    return new File(new File(context.getRootPagePath(), PathParser.FILES), paramValue);
  }

  protected String getFilename(List<WikiPage> pages) {
    return pages.get(0).getVariable(PARTITION_FILE_ARG);
  }

  protected ListPartitioner<WikiPage> createPartitioner(File f) {
    PagePositions pagePositions = readPositionMap(f, "\t");
    return new PagePositionsBasedWikiPagePartitioner(pagePositions);
  }

  protected PagePositions readPositionMap(File file, String separator) {
    try (FileReader f = new FileReader(file);
         BufferedReader b = new BufferedReader(f)) {
      return PagePositions.parseFrom(b, separator);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to load: " + file.getAbsolutePath(), e);
    }
  }

  public Function<File, ListPartitioner<WikiPage>> getPartitionFunctionFactory() {
    return partitionFunctionFactory;
  }

  public void setPartitionFunctionFactory(Function<File, ListPartitioner<WikiPage>> partitionFunctionFactory) {
    this.partitionFunctionFactory = partitionFunctionFactory;
  }

  public FitNesseContext getContext() {
    return context;
  }
}
