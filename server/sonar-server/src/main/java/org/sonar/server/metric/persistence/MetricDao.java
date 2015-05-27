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

package org.sonar.server.metric.persistence;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.ibatis.session.RowBounds;
import org.sonar.api.server.ServerSide;
import org.sonar.core.metric.db.MetricDto;
import org.sonar.core.metric.db.MetricMapper;
import org.sonar.core.persistence.DaoComponent;
import org.sonar.core.persistence.DaoUtils;
import org.sonar.core.persistence.DbSession;
import org.sonar.server.es.SearchOptions;

import static com.google.common.collect.Lists.newArrayList;

@ServerSide
public class MetricDao implements DaoComponent {

  @CheckForNull
  public MetricDto selectByKey(DbSession session, String key) {
    return mapper(session).selectByKey(key);
  }

  public List<MetricDto> selectEnabled(DbSession session) {
    return mapper(session).selectAllEnabled();
  }

  public List<MetricDto> selectEnabled(DbSession session, @Nullable Boolean isCustom, SearchOptions searchOptions) {
    Map<String, Object> properties = Maps.newHashMapWithExpectedSize(1);
    if (isCustom != null) {
      properties.put("isCustom", isCustom);
    }

    return mapper(session).selectAllEnabled(properties, new RowBounds(searchOptions.getOffset(), searchOptions.getLimit()));
  }

  public void insert(DbSession session, MetricDto dto) {
    mapper(session).insert(dto);
  }

  public List<String> selectDomains(DbSession session) {
    return newArrayList(Collections2.filter(mapper(session).selectDomains(), new Predicate<String>() {
      @Override
      public boolean apply(@Nonnull String input) {
        return !input.isEmpty();
      }
    }));
  }

  private MetricMapper mapper(DbSession session) {
    return session.getMapper(MetricMapper.class);
  }

  public List<MetricDto> selectByKeys(final DbSession session, List<String> keys) {
    return DaoUtils.executeLargeInputs(keys, new Function<List<String>, List<MetricDto>>() {
      @Override
      public List<MetricDto> apply(@Nullable List<String> input) {
        return mapper(session).selectByKeys(input);
      }
    });
  }

  public void disable(final DbSession session, List<Integer> ids) {
    DaoUtils.executeLargeInputsWithoutOutput(ids, new Function<List<Integer>, Void>() {
      @Override
      public Void apply(@Nullable List<Integer> input) {
        mapper(session).disable(input);
        return null;
      }
    });
  }
}
