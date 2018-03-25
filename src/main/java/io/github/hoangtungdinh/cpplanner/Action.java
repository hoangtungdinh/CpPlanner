package io.github.hoangtungdinh.cpplanner;

import com.google.common.collect.ImmutableSet;

/** @author Hoang Tung Dinh */
public final class Action {
  private final String name;
  private final ImmutableSet<State.StateValue> preconditions;
  private final ImmutableSet<State.StateValue> effects;

  private Action(Builder builder) {
    name = builder.name;
    preconditions = builder.preconditions;
    effects = builder.effects;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getName() {
    return name;
  }

  ImmutableSet<State.StateValue> getPreconditions() {
    return preconditions;
  }

  ImmutableSet<State.StateValue> getEffects() {
    return effects;
  }

  /** {@code Action} builder static inner class. */
  public static final class Builder {
    private String name = "";
    private ImmutableSet<State.StateValue> preconditions = ImmutableSet.of();
    private ImmutableSet<State.StateValue> effects = ImmutableSet.of();

    private Builder() {}

    /**
     * Sets the {@code name} and returns a reference to this Builder so that the methods can be
     * chained together.
     *
     * @param val the {@code name} to set
     * @return a reference to this Builder
     */
    public Builder name(String val) {
      name = val;
      return this;
    }

    /**
     * Sets the {@code preconditions} and returns a reference to this Builder so that the methods
     * can be chained together.
     *
     * @param val the {@code preconditions} to set
     * @return a reference to this Builder
     */
    public Builder preconditions(State.StateValue... val) {
      preconditions = ImmutableSet.copyOf(val);
      return this;
    }

    /**
     * Sets the {@code effects} and returns a reference to this Builder so that the methods can be
     * chained together.
     *
     * @param val the {@code effects} to set
     * @return a reference to this Builder
     */
    public Builder effects(State.StateValue... val) {
      effects = ImmutableSet.copyOf(val);
      return this;
    }

    /**
     * Returns a {@code Action} built from the parameters previously set.
     *
     * @return a {@code Action} built with parameters of this {@code Action.Builder}
     */
    public Action build() {
      return new Action(this);
    }
  }
}
