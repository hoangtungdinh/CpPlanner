package io.github.hoangtungdinh.cpplanner;

import com.google.auto.value.AutoValue;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import static com.google.common.base.Preconditions.checkArgument;

/** @author Hoang Tung Dinh */
public final class State {
  private final String name;
  private final Class<? extends Enum<?>> stateEnumClass;
  private final EnumIndexMap enumIndexMap;

  private State(String name, Class<? extends Enum<?>> stateEnumClass) {
    this.name = name;
    this.stateEnumClass = stateEnumClass;
    this.enumIndexMap = EnumIndexMap.create(stateEnumClass);
  }

  public static State withValuesFrom(String name, Class<? extends Enum<?>> stateEnumClass) {
    return new State(name, stateEnumClass);
  }

  public static State withValuesFrom(Class<? extends Enum<?>> stateEnumClass) {
    return new State("", stateEnumClass);
  }

  public StateValue hasValue(Enum<?> value) {
    checkArgument(value.getClass() == stateEnumClass, "Value type is not correct.");
    return StateValue.create(this, value);
  }

  int getValueIndex(Enum<?> value) {
    return enumIndexMap.getIndex(value);
  }

  int[] getIndexArray() {
    return enumIndexMap.getIndexArray();
  }

  @AutoValue
  abstract static class StateValue {
    static StateValue create(State state, Enum<?> value) {
      return new AutoValue_State_StateValue(state, value);
    }

    abstract State state();

    abstract Enum<?> value();

    final int valueIndex() {
      return state().getValueIndex(value());
    }
  }

  private static final class EnumIndexMap {
    private final Object2IntMap<Object> enumToIndexMap = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<Enum<?>> indexToEnumMap = new Int2ObjectOpenHashMap<>();
    private final int[] indexArray;

    private EnumIndexMap(Class<? extends Enum<?>> enumClass) {
      int index = 0;
      for (final Enum<?> element : enumClass.getEnumConstants()) {
        enumToIndexMap.put(element, index);
        indexToEnumMap.put(index, element);
        index++;
      }
      indexArray = indexToEnumMap.keySet().toIntArray();
    }

    static EnumIndexMap create(Class<? extends Enum<?>> enumClass) {
      return new EnumIndexMap(enumClass);
    }

    Enum<?> getEnum(int index) {
      return indexToEnumMap.get(index);
    }

    <T extends Enum<?>> int getIndex(T element) {
      return enumToIndexMap.getInt(element);
    }

    int[] getIndexArray() {
      return indexArray;
    }
  }
}
