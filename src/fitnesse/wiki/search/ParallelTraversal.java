// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PagePruningStrategy;
import fitnesse.wiki.WikiPage;

/**
 * Traverses a page tree like {@link fitnesse.wiki.PageCrawler#traverse}, but reads pages ahead on worker threads:
 * the child pages (and optionally the page data) of all children of a page are loaded in parallel.
 * Pages are still checked against the pruning strategy and passed to the listener one at a time,
 * on the calling thread and in the same order, so neither has to be thread safe.
 */
class ParallelTraversal {
  private final TraversalListener<? super WikiPage> listener;
  private final PagePruningStrategy pruningStrategy;
  private final boolean loadPageData;
  private final boolean skipSymbolicLinks;
  private final int threads;
  private ExecutorService executor;

  ParallelTraversal(TraversalListener<? super WikiPage> listener, PagePruningStrategy pruningStrategy, boolean loadPageData,
                    int threads) {
    this.listener = listener;
    this.pruningStrategy = pruningStrategy;
    this.loadPageData = loadPageData;
    this.skipSymbolicLinks = pruningStrategy.skipSymbolicLinks();
    this.threads = threads;
  }

  void traverse(WikiPage startPage) {
    executor = Executors.newFixedThreadPool(threads, ParallelTraversal::newThread);
    try {
      traverse(startPage, load(startPage));
    } finally {
      executor.shutdownNow();
    }
  }

  private void traverse(WikiPage page, Future<List<WikiPage>> loadedChildren) {
    // wait for the page to be loaded before the pruning strategy or the listener may look at it
    List<WikiPage> children = await(loadedChildren);
    if (pruningStrategy.skipPageAndChildren(page)) {
      return;
    }
    listener.process(page);

    List<Future<List<WikiPage>>> loadedGrandChildren = new ArrayList<>(children.size());
    for (WikiPage child : children) {
      loadedGrandChildren.add(load(child));
    }
    for (int i = 0; i < children.size(); i++) {
      traverse(children.get(i), loadedGrandChildren.get(i));
    }
  }

  private Future<List<WikiPage>> load(WikiPage page) {
    return executor.submit(() -> {
      if (loadPageData) {
        page.getData();
      }
      return skipSymbolicLinks ? page.getChildrenWithoutSymbolicLinks() : page.getChildren();
    });
  }

  private static <T> T await(Future<T> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while loading pages", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw new IllegalStateException(cause);
    }
  }

  private static Thread newThread(Runnable runnable) {
    Thread thread = new Thread(runnable, "page loader");
    thread.setDaemon(true);
    return thread;
  }
}
