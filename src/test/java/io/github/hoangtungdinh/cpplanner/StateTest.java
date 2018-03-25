package io.github.hoangtungdinh.cpplanner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/** @author Hoang Tung Dinh */
public class StateTest {

  @Test
  public void testHasValue() {
    final State state = State.withValuesFrom(FirstEnum.class);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> state.hasValue(SecondEnum.A));
  }

  private enum FirstEnum {
    A,
    B
  }

  private enum SecondEnum {
    A,
    B
  }
}
