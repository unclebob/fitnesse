package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.components.ComponentFactory;
import fitnesse.wiki.fs.DiskFileSystem;
import fitnesse.wiki.fs.FileVersionsController;
import fitnesse.wiki.fs.SimpleFileVersionsController;

class FileVersionsControllerFactory {
  static FileVersionsController getVersionsController(FitNesseContext context) {
    Object controller = new ComponentFactory(context.getProperties()).createComponent(
            ComponentFactory.VERSIONS_CONTROLLER_CLASS);

    if (controller instanceof FileVersionsController) {
      return (FileVersionsController) controller;
    }

    return new SimpleFileVersionsController(new DiskFileSystem());
  }
}