package io.github.hoangtungdinh.cpplanner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** @author Hoang Tung Dinh */
public final class Planner {
  private final ImmutableList<State> states;
  private final ImmutableList<Action> actions;
  private final ImmutableSet<State.StateValue> initialStates;
  private final ImmutableSet<State.StateValue> goals;
  private final int maxNumOfSteps;

  private Planner(Builder builder) {
    states = builder.states;
    actions = ImmutableList.copyOf(builder.actions);
    initialStates = builder.initialStates;
    goals = builder.goals;
    maxNumOfSteps = builder.maxNumOfSteps.orElse(10);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Optional<ImmutableList<Action>> plan() {
    final int[] actionIndexes = ArrayUtils.array(0, actions.size() - 1);

    for (int i = 0; i < maxNumOfSteps; i++) {
      final Optional<ImmutableList<Action>> plan = plan(i, actionIndexes);
      if (plan.isPresent()) {
        return plan;
      }
    }

    return Optional.empty();
  }

  private Optional<ImmutableList<Action>> plan(int numOfSteps, int[] actionIndexes) {
    final Model model = new Model();

    // initialize action vars
    final List<IntVar> actionVars = new ArrayList<>();
    for (int i = 0; i < numOfSteps; i++) {
      actionVars.add(model.intVar(actionIndexes));
    }

    // initialize state vars
    final Map<State, List<IntVar>> stateVars = new HashMap<>();
    states.forEach(
        state -> {
          final List<IntVar> vars = new ArrayList<>();
          for (int i = 0; i < numOfSteps + 1; i++) {
            vars.add(model.intVar(state.getIndexArray()));
          }

          stateVars.put(state, vars);
        });

    // add initial states
    initialStates.forEach(
        stateValue ->
            model
                .arithm(stateVars.get(stateValue.state()).get(0), "=", stateValue.valueIndex())
                .post());

    // add goal states
    goals.forEach(
        stateValue ->
            model
                .arithm(
                    stateVars.get(stateValue.state()).get(numOfSteps), "=", stateValue.valueIndex())
                .post());

    // add preconditions
    for (int stepIndex = 0; stepIndex < numOfSteps; stepIndex++) {
      for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
        Constraint preconditions = model.trueConstraint();
        for (final State.StateValue stateValue : actions.get(actionIndex).getPreconditions()) {
          preconditions =
              model.and(
                  preconditions,
                  model.arithm(
                      stateVars.get(stateValue.state()).get(stepIndex),
                      "=",
                      stateValue.valueIndex()));
        }
        model.ifThen(model.arithm(actionVars.get(stepIndex), "=", actionIndex), preconditions);
      }
    }

    // add effects and frame assumption
    for (int stepIndex = 0; stepIndex < numOfSteps; stepIndex++) {
      for (int actionIndex = 0; actionIndex < actions.size(); actionIndex++) {
        // effects
        Constraint effects = model.trueConstraint();
        for (final State.StateValue stateValue : actions.get(actionIndex).getEffects()) {
          effects =
              model.and(
                  effects,
                  model.arithm(
                      stateVars.get(stateValue.state()).get(stepIndex + 1),
                      "=",
                      stateValue.valueIndex()));
        }
        model.ifThen(model.arithm(actionVars.get(stepIndex), "=", actionIndex), effects);
        // frame assumptions
        final ImmutableSet<State> changableStates =
            actions
                .get(actionIndex)
                .getEffects()
                .stream()
                .map(State.StateValue::state)
                .collect(ImmutableSet.toImmutableSet());
        for (final State state : states) {
          if (!changableStates.contains(state)) {
            model.ifThen(
                model.arithm(actionVars.get(stepIndex), "=", actionIndex),
                model.arithm(
                    stateVars.get(state).get(stepIndex),
                    "=",
                    stateVars.get(state).get(stepIndex + 1)));
          }
        }
      }
    }

    final boolean hasSolution = model.getSolver().solve();
    if (hasSolution) {
      return Optional.of(
          actionVars
              .stream()
              .map(actionIndex -> actions.get(actionIndex.getValue()))
              .collect(ImmutableList.toImmutableList()));
    } else {
      return Optional.empty();
    }
  }

  /** {@code Planner} builder static inner class. */
  public static final class Builder {
    private ImmutableList<State> states = null;
    private ImmutableSet<Action> actions = null;
    private ImmutableSet<State.StateValue> initialStates = null;
    private ImmutableSet<State.StateValue> goals = null;
    private Optional<Integer> maxNumOfSteps = Optional.empty();

    private Builder() {}

    /**
     * Sets the {@code states} and returns a reference to this Builder so that the methods can be
     * chained together.
     *
     * @param val the {@code states} to set
     * @return a reference to this Builder
     */
    public Builder states(State... val) {
      states = ImmutableList.copyOf(val);
      return this;
    }

    /**
     * Sets the {@code actions} and returns a reference to this Builder so that the methods can be
     * chained together.
     *
     * @param val the {@code actions} to set
     * @return a reference to this Builder
     */
    public Builder actions(Action... val) {
      actions = ImmutableSet.copyOf(val);
      return this;
    }

    /**
     * Sets the {@code initialStates} and returns a reference to this Builder so that the methods
     * can be chained together.
     *
     * @param val the {@code initialStates} to set
     * @return a reference to this Builder
     */
    public Builder initialStates(State.StateValue... val) {
      initialStates = ImmutableSet.copyOf(val);
      return this;
    }

    /**
     * Sets the {@code goals} and returns a reference to this Builder so that the methods can be
     * chained together.
     *
     * @param val the {@code goals} to set
     * @return a reference to this Builder
     */
    public Builder goals(State.StateValue... val) {
      goals = ImmutableSet.copyOf(val);
      return this;
    }

    /**
     * Sets the {@code maxNumOfSteps} and returns a reference to this Builder so that the methods
     * can be chained together.
     *
     * @param val the {@code maxNumOfSteps} to set
     * @return a reference to this Builder
     */
    public Builder maxNumOfSteps(int val) {
      maxNumOfSteps = Optional.of(val);
      return this;
    }

    /**
     * Returns a {@code Planner} built from the parameters previously set.
     *
     * @return a {@code Planner} built with parameters of this {@code Planner.Builder}
     */
    public Planner build() {
      return new Planner(this);
    }
  }
}
