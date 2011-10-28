package fit;

public interface FixtureLoaderInterface {
  public Fixture disgraceThenLoad(String tableName) throws Throwable;
  public void addPackageToPath(String name);
}
