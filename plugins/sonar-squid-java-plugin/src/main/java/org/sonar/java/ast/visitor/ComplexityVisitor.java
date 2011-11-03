/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.ast.visitor;

import java.util.Arrays;
import java.util.List;

import org.sonar.squid.api.SourceCode;
import org.sonar.squid.measures.Metric;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class ComplexityVisitor extends JavaAstVisitor {

  @Override
  public List<Integer> getWantedTokens() {
    return WANTED_TOKENS;
  }

  @Override
  public void leaveToken(DetailAST ast) {
    SourceCode res = peekSourceCode();
    res.setMeasure(Metric.COMPLEXITY, res.getInt(Metric.COMPLEXITY) + res.getInt(Metric.BRANCHES) + 1);
  }

  private static final List<Integer> WANTED_TOKENS = Arrays.asList(TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF, TokenTypes.INSTANCE_INIT,
      TokenTypes.STATIC_INIT);
}
