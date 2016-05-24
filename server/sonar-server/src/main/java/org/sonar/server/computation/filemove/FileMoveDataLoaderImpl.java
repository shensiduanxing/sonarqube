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

import com.google.common.base.Function;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.ComponentDto;
import org.sonar.server.computation.batch.BatchReportReader;
import org.sonar.server.computation.component.Component;
import org.sonar.server.computation.component.TreeRootHolder;

import static com.google.common.collect.FluentIterable.from;

public class FileMoveDataLoaderImpl implements FileMoveDataLoader {

  private final DbClient dbClient;
  private final TreeRootHolder rootHolder;
  private final BatchReportReader reportReader;

  public FileMoveDataLoaderImpl(DbClient dbClient, TreeRootHolder rootHolder, BatchReportReader reportReader) {
    this.dbClient = dbClient;
    this.rootHolder = rootHolder;
    this.reportReader = reportReader;
  }

  @Override
  public Set<String> getFileKeysInDb() {
    try (DbSession dbSession = dbClient.openSession(false)) {
      List<ComponentDto> dtos = dbClient.componentDao().selectAllComponentsFromProjectKey(dbSession, rootHolder.getRoot().getKey());
      return from(dtos).transform(ComponentDtoToKeyFunction.INSTANCE).toSet();
    }
  }

  @Override
  public List<String> getLineHashesFromDb(String fileKey) {
    try (DbSession dbSession = dbClient.openSession(false)) {
      List<ComponentDto> dtos = dbClient.fileSourceDao().selectLineHashes(dbSession, );
      return from(dtos).transform(ComponentDtoToKeyFunction.INSTANCE).toSet();
    }
  }

  @Override
  public List<String> getLineHashes(String fileKey) {
    Component c = rootHolder.getComponentByKey(fileKey);
    // reportReader.readFileSource(c.get)
    return c;
  }

  private enum ComponentDtoToKeyFunction implements Function<ComponentDto, String> {
    INSTANCE;

    @Override
    public String apply(@Nonnull ComponentDto input) {
      return input.getKey();
    }
  }
}
