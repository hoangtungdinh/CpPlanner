package io.github.hoangtungdinh.cpplanner;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** @author Hoang Tung Dinh */
public class PlannerTest {

  @Test
  public void testPlan() {
    final State pictureAtA = State.withValuesFrom(Picture.class);
    final State uavLocation = State.withValuesFrom(Location.class);

    final Action goToA = Action.builder().effects(uavLocation.hasValue(Location.AT_A)).build();
    final Action goHome = Action.builder().effects(uavLocation.hasValue(Location.AT_HOME)).build();
    final Action takePictureAtA =
        Action.builder()
            .preconditions(
                uavLocation.hasValue(Location.AT_A), pictureAtA.hasValue(Picture.NOT_TAKEN))
            .effects(pictureAtA.hasValue(Picture.TAKEN))
            .build();

    final Planner planner =
        Planner.builder()
            .actions(goToA, goHome, takePictureAtA)
            .states(pictureAtA, uavLocation)
            .maxNumOfSteps(5)
            .initialStates(
                uavLocation.hasValue(Location.AT_HOME), pictureAtA.hasValue(Picture.NOT_TAKEN))
            .goals(uavLocation.hasValue(Location.AT_HOME), pictureAtA.hasValue(Picture.TAKEN))
            .build();

    final Optional<ImmutableList<Action>> plan = planner.plan();

    assertThat(plan).isPresent();
    assertThat(plan.get()).containsExactly(goToA, takePictureAtA, goHome);
  }

  private enum Picture {
    TAKEN,
    NOT_TAKEN
  }

  private enum Location {
    AT_A,
    AT_HOME
  }
}
