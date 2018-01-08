package http4j.core.util;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CaseInsensitiveMap<V> extends ForwardingMap<String, V> {

  private final Map<String, V> delegate;

  private CaseInsensitiveMap(Map<String, V> delegate) {
    this.delegate = delegate;
    this.delegate.putAll(delegate); //copy the values
  }

  public static <V> CaseInsensitiveMap<V> create() {
    return new CaseInsensitiveMap<>(Maps.newHashMap());
  }

  public static <V> CaseInsensitiveMap<V> create(Map<String, V> map) {
    return new CaseInsensitiveMap<>(map);
  }

  private String normalize(Object key) {
    return key == null ? null : key.toString().toLowerCase();
  }

  @Override
  protected Map<String, V> delegate() {
    return delegate;
  }

  @Override
  public V getOrDefault(Object key, V defaultValue) {
    return super.getOrDefault(normalize(key), defaultValue);
  }

  @Override
  public V putIfAbsent(String key, V value) {
    return super.putIfAbsent(normalize(key), value);
  }

  @Override
  public boolean remove(Object key, Object value) {
    return super.remove(normalize(key), value);
  }

  @Override
  public boolean replace(String key, V oldValue, V newValue) {
    return super.replace(normalize(key), oldValue, newValue);
  }

  @Override
  public V replace(String key, V value) {
    return super.replace(normalize(key), value);
  }

  @Override
  public V computeIfAbsent(String key, Function<? super String, ? extends V> mappingFunction) {
    return super.computeIfAbsent(normalize(key), mappingFunction);
  }

  @Override
  public V computeIfPresent(
      String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
    return super.computeIfPresent(normalize(key), remappingFunction);
  }

  @Override
  public V compute(
      String key, BiFunction<? super String, ? super V, ? extends V> remappingFunction) {
    return super.compute(normalize(key), remappingFunction);
  }

  @Override
  public V merge(
      String key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
    return super.merge(normalize(key), value, remappingFunction);
  }
}
