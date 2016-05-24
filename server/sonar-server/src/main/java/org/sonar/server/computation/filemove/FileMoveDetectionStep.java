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
package org.sonar.server.computation.filemove;

import java.util.ArrayList;
import java.util.List;
import org.sonar.server.computation.component.Component;
import org.sonar.server.computation.component.TreeRootHolder;
import org.sonar.server.computation.step.ComputationStep;

public class FileMoveDetectionStep implements ComputationStep {

  private final FileMoveDetector detector;
  private final TreeRootHolder rootHolder;

  public FileMoveDetectionStep(FileMoveDetector detector, TreeRootHolder rootHolder) {
    this.detector = detector;
    this.rootHolder = rootHolder;
  }

  @Override
  public void execute() {
    List<String> keys = new ArrayList<>();
    traverse(rootHolder.getRoot(), keys);

    detector.detect(keys);
  }

  private void traverse(Component c, List<String> keys) {
    if (c.getType() == Component.Type.FILE) {
      keys.add(c.getKey());
    }
    for (Component child : c.getChildren()) {
      traverse(child, keys);
    }
  }

  @Override
  public String getDescription() {
    return "Detect file moves";
  }
}
