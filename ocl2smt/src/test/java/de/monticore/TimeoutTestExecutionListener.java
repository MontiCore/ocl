package de.monticore;


import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;


public class TimeoutTestExecutionListener implements TestExecutionListener {
  static Set<String> leftOvers = new ConcurrentSkipListSet<>();
  @Override
  public void executionStarted(TestIdentifier testIdentifier) {
    if (testIdentifier.isTest()) {
      leftOvers.add(testIdentifier.getDisplayName());
      System.out.println("Starting test: " + testIdentifier.getDisplayName() + " (" + testIdentifier.getSource());
    }
  }

  @Override
  public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
    if (testIdentifier.isTest()) {
      leftOvers.remove(testIdentifier.getDisplayName());
      System.out.println("Finished test: " + testIdentifier.getDisplayName() + " - Leftover " + leftOvers);
    }
  }
}
