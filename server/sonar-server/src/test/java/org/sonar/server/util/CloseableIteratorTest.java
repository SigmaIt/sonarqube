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
package org.sonar.server.util;

import java.util.Collections;
import java.util.NoSuchElementException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class CloseableIteratorTest {

  @Test
  public void iterate() {
    SimpleCloseableIterator it = new SimpleCloseableIterator();
    assertThat(it.isClosed).isFalse();

    // multiple calls to hasNext() moves only once the cursor
    assertThat(it.hasNext()).isTrue();
    assertThat(it.hasNext()).isTrue();
    assertThat(it.hasNext()).isTrue();
    assertThat(it.next()).isEqualTo(1);
    assertThat(it.isClosed).isFalse();

    assertThat(it.hasNext()).isTrue();
    assertThat(it.next()).isEqualTo(2);
    assertThat(it.isClosed).isFalse();

    assertThat(it.hasNext()).isFalse();
    // automatic close
    assertThat(it.isClosed).isTrue();

    // explicit close does not fail
    it.close();
    assertThat(it.isClosed).isTrue();
  }

  @Test
  public void call_next_without_hasNext() {
    SimpleCloseableIterator it = new SimpleCloseableIterator();
    assertThat(it.next()).isEqualTo(1);
    assertThat(it.next()).isEqualTo(2);
    try {
      it.next();
      fail();
    } catch (NoSuchElementException expected) {

    }
  }

  @Test
  public void automatic_close_if_traversal_error() {
    FailureCloseableIterator it = new FailureCloseableIterator();
    try {
      it.next();
      fail();
    } catch (IllegalStateException expected) {
      assertThat(expected).hasMessage("expected failure");
      assertThat(it.isClosed).isTrue();
    }
  }

  @Test
  public void remove_is_not_supported_by_default() {
    SimpleCloseableIterator it = new SimpleCloseableIterator();
    try {
      it.remove();
      fail();
    } catch (UnsupportedOperationException expected) {
      assertThat(it.isClosed).isTrue();
    }
  }

  @Test
  public void remove_can_be_overridden() {
    RemovableCloseableIterator it = new RemovableCloseableIterator();
    it.remove();
    assertThat(it.isRemoved).isTrue();
  }

  @Test
  public void has_next_should_not_call_do_next_when_already_closed() {
    DoNextShouldNotBeCalledWhenClosedIterator it = new DoNextShouldNotBeCalledWhenClosedIterator();

    it.next();
    it.next();
    assertThat(it.hasNext()).isFalse();
    // this call to hasNext close the stream
    assertThat(it.hasNext()).isFalse();
    assertThat(it.isClosed).isTrue();

    // calling hasNext should not fail
    it.hasNext();
  }

  @Test
  public void emptyIterator_has_next_is_false() {
    assertThat(CloseableIterator.emptyCloseableIterator().hasNext()).isFalse();
  }

  @Test(expected = NoSuchElementException.class)
  public void emptyIterator_next_throws_NoSuchElemetException() {
    CloseableIterator.emptyCloseableIterator().next();
  }

  @Test(expected = NullPointerException.class)
  public void from_iterator_throws_early_NPE_if_arg_is_null() {
    CloseableIterator.from(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void from_iterator_throws_IAE_if_arg_is_a_CloseableIterator() {
    CloseableIterator.from(new SimpleCloseableIterator());
  }

  @Test
  public void verify_has_next_from_iterator_with_empty_iterator() {
    assertThat(CloseableIterator.from(Collections.<String>emptyList().iterator()).hasNext()).isFalse();
  }

  @Test(expected = NoSuchElementException.class)
  public void verify_next_from_iterator_with_empty_iterator() {
    CloseableIterator.from(Collections.<String>emptyList().iterator()).next();
  }

  static class SimpleCloseableIterator extends CloseableIterator {
    int count = 0;
    boolean isClosed = false;

    @Override
    protected Object doNext() {
      count++;
      if (count < 3) {
        return count;
      }
      return null;
    }

    @Override
    protected void doClose() {
      isClosed = true;
    }
  }

  static class FailureCloseableIterator extends CloseableIterator {
    boolean isClosed = false;

    @Override
    protected Object doNext() {
      throw new IllegalStateException("expected failure");
    }

    @Override
    protected void doClose() {
      isClosed = true;
    }
  }

  static class RemovableCloseableIterator extends CloseableIterator {
    boolean isClosed = false, isRemoved = false;

    @Override
    protected Object doNext() {
      return "foo";
    }

    @Override
    protected void doRemove() {
      isRemoved = true;
    }

    @Override
    protected void doClose() {
      isClosed = true;
    }
  }

  static class DoNextShouldNotBeCalledWhenClosedIterator extends SimpleCloseableIterator {

    @Override
    protected Object doNext() {
      if (!isClosed) {
        return super.doNext();
      } else {
        throw new IllegalStateException("doNext should not be called when already closed");
      }
    }
  }

}
