package http4j.core.util;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/** A case insensitive multimap */
public final class CaseInsensitiveMultimap<V> extends ForwardingMultimap<String, V> {

  private final Multimap<String, V> delegate;

  private CaseInsensitiveMultimap(Multimap<String, V> delegate) {
    this.delegate = LinkedHashMultimap.create();
    this.delegate.putAll(delegate); //copy the values
  }

  public static <V> CaseInsensitiveMultimap<V> create() {
    return new CaseInsensitiveMultimap<>(LinkedHashMultimap.create());
  }

  public static <V> CaseInsensitiveMultimap<V> create(Multimap<String, V> map) {
    return new CaseInsensitiveMultimap<>(map);
  }

  private String normalize(Object key) {
    return key == null ? null : key.toString().toLowerCase();
  }

  @Override
  public boolean containsEntry(@Nullable Object key, @Nullable Object value) {
    return super.containsEntry(normalize(key), value);
  }

  @Override
  public boolean containsKey(@Nullable Object key) {
    return super.containsKey(normalize(key));
  }

  @Override
  public boolean put(String key, V value) {
    return super.put(normalize(key), value);
  }

  @Override
  public boolean putAll(Multimap<? extends String, ? extends V> multimap) {
    // copied from AbstractMultimap
    boolean changed = false;
    for (Map.Entry<? extends String, ? extends V> entry : multimap.entries()) {
      changed |= put(normalize(entry.getKey()), entry.getValue());
    }
    return changed;
  }

  @Override
  public boolean putAll(String key, Iterable<? extends V> values) {
    return super.putAll(normalize(key), values);
  }

  @Override
  public boolean remove(@Nullable Object key, @Nullable Object value) {
    return super.remove(normalize(key), value);
  }

  @Override
  public Collection<V> removeAll(@Nullable Object key) {
    return super.removeAll(normalize(key));
  }

  @Override
  public Collection<V> replaceValues(String key, Iterable<? extends V> values) {
    return super.replaceValues(normalize(key), values);
  }

  @Override
  public Collection<V> get(final String key) {
    return delegate.get(normalize(key));
  }

  @Override
  protected Multimap<String, V> delegate() {
    return delegate;
  }
}
