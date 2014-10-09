/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.core.computation.db;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface AnalysisReportMapper {
  void insert(AnalysisReportDto report);

  List<AnalysisReportDto> selectByProjectKey(String projectKey);

  List<AnalysisReportDto> selectNextAvailableReport(@Param("availableStatus") AnalysisReportDto.Status availableStatus, @Param("busyStatus") AnalysisReportDto.Status busyStatus);

  /**
   * startup task use only
   */
  void cleanWithUpdateAllToPendingStatus(@Param("status") AnalysisReportDto.Status status, @Param("updatedAt") Date updatedAt);

  /**
   * startup task use only
   */
  void cleanWithTruncate();

  int update(AnalysisReportDto report);

  int updateWithBookingReport(@Param("id") Long id, @Param("projectKey") String projectKey, @Param("updatedAt") Date updatedAt,
    @Param("availableStatus") AnalysisReportDto.Status availableStatus,
    @Param("busyStatus") AnalysisReportDto.Status busyStatus);

  AnalysisReportDto selectById(Long id);

  void delete(Long id);
}
