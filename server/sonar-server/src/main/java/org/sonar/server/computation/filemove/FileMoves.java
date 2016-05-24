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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class FileMoves {

  private final Map<String, String> matchingDbKeys;

  private FileMoves(Builder builder) {
    matchingDbKeys = builder.mapBuilder.build();
  }

  @CheckForNull
  public String getForKey(String fileKey) {
    return matchingDbKeys.get(fileKey);
  }

  public static final class Builder {
    private final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();

    public Builder addMatch(String key, String dbKey) {
      mapBuilder.put(key, dbKey);
      return this;
    }

    public FileMoves build() {
      return new FileMoves(this);
    }
  }
}
