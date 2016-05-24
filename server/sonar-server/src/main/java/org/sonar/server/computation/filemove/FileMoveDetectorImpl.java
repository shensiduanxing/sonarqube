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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class FileMoveDetectorImpl implements FileMoveDetector {

  private static final Logger LOGGER = Loggers.get(FileMoveDetectorImpl.class);
  private static final int MIN_REQUIRED_SCORE = 90;

  private final FileMoveDataLoader dataLoader;
  private final FileSimilarity fileSimilarity;

  public FileMoveDetectorImpl(FileMoveDataLoader dataLoader, FileSimilarity fileSimilarity) {
    this.dataLoader = dataLoader;
    this.fileSimilarity = fileSimilarity;
  }

  @Override
  public FileMoves detect(Collection<String> keys) {
    // TODO match with files that have exactly the same key but are disabled ?
    // TODO match only if same file suffix
    // TODO what about "extract interface" refactoring (filename is kept) ?
    // TODO log statistics
    // TODO increase score if same filename
    // TODO only if open issues ?

    // - do nothing if no files in db (first analysis)
    // - remove the files which path exists in both input. Remains
    // -- new files in report (N)
    // -- deleted files in db (D)
    // - if size(N)=0 or size(D)=0, stop
    // - select line hashes of all db files
    // - as long as size(D)>0, for each new file
    // -- compute line hashes
    // -- compute similarity with D
    // -- associate with the similar file with highest score and score>x%
    // -- remove file from D
    FileMoves.Builder result = new FileMoves.Builder();

    if (keys.isEmpty()) {
      LOGGER.info("----->> no files ");
      return result.build();
    }

    Set<String> dbKeys = dataLoader.getFileKeysInDb();
    if (dbKeys.isEmpty()) {
      // the current analysis is probably the first time ever on the project
      LOGGER.info("----->> no db files ");
      return result.build();
    }

    // remove all the files that already existed in previous analysis
    Set<String> unmatchedKeys = new HashSet<>();
    for (String key : keys) {
      if (dbKeys.contains(key)) {
        // dbKeys.remove(key);
      } else {
        unmatchedKeys.add(key);
      }
    }
    if (unmatchedKeys.isEmpty() || dbKeys.isEmpty()) {
      return result.build();
    }

    SortedSet<MatchingScore> scores = new TreeSet<>(MatchingScoreComparator.INSTANCE);
    for (String dbKey : dbKeys) {
      scores.clear();
      List<String> dbLineHashes = dataLoader.getLineHashesFromDb(dbKey);
      FileSimilarity.File fileInDb = new FileSimilarity.File(dbKey, dbLineHashes);
      LOGGER.info("--- similarities of " + dbKey);
      for (String unmatchedKey : unmatchedKeys) {
        List<String> unmatchedLineHashes = dataLoader.getLineHashes(unmatchedKey);
        FileSimilarity.File unmatchedFile = new FileSimilarity.File(unmatchedKey, unmatchedLineHashes);
        int score = fileSimilarity.score(fileInDb, unmatchedFile);
        LOGGER.info("    {} --> " + unmatchedKey);
        // no need for storing all scores in memory, keep only the good ones
        if (isAcceptableScore(score)) {
          scores.add(new MatchingScore(unmatchedKey, score));
        }
      }
      if (!scores.isEmpty()) {
        // take the highest one
        MatchingScore matchingScore = scores.first();
        result.addMatch(matchingScore.key, dbKey);
        unmatchedKeys.remove(matchingScore.key);
        LOGGER.info("-- Match {} - {} and {}", matchingScore.score, matchingScore.key, dbKey);
      }
    }
    return result.build();
  }

  private static boolean isAcceptableScore(int score) {
    return score >= MIN_REQUIRED_SCORE;
  }

  private static class MatchingScore {
    private final String key;
    private final int score;

    public MatchingScore(String key, int score) {
      this.key = key;
      this.score = score;
    }
  }

  private enum MatchingScoreComparator implements Comparator<MatchingScore> {
    INSTANCE;
    @Override
    public int compare(MatchingScore o1, MatchingScore o2) {
      // TODO sure about order ?
      return o1.score - o2.score;
    }
  }

}
