/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.computation.step;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.core.util.logs.Profiler;

public final class ComputationStepExecutor {
  private static final Logger LOGGER = Loggers.get(ComputationStepExecutor.class);

  private final ComputationSteps steps;
  @CheckForNull
  private final Listener listener;

  /**
   * Used when no {@link org.sonar.server.computation.step.ComputationStepExecutor.Listener} is available in pico
   * container.
   */
  public ComputationStepExecutor(ComputationSteps steps) {
    this(steps, null);
  }

  public ComputationStepExecutor(ComputationSteps steps, @Nullable Listener listener) {
    this.steps = steps;
    this.listener = listener;
  }

  public void execute() {
    Profiler stepProfiler = Profiler.create(LOGGER);
    boolean allStepsExecuted = false;
    try {
      executeSteps(stepProfiler);
      allStepsExecuted = true;
    } finally {
      if (listener != null) {
        listener.finished(allStepsExecuted);
      }
    }
  }

  private void executeSteps(Profiler stepProfiler) {
    for (ComputationStep step : steps.instances()) {
      stepProfiler.start();
      step.execute();
      stepProfiler.stopInfo(step.getDescription());
    }
  }

  public interface Listener {
    void finished(boolean allStepsExecuted);
  }
}
