package http4j.core.util;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.net.UrlEscapers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Very minimal URI path template parser. A URI template is a URI-like String that contains
 * variables enclosed by braces ({@code {}}) which can be expanded to produce an actual URI.
 */
public final class UriTemplate {

  /** the original template */
  private final String template;

  /** The pattern to match against */
  private final Pattern pattern;

  /** The variables parsed from the template that were enclosed by braces. */
  private final List<String> variables;

  private UriTemplate(String template, Pattern pattern, List<String> variables) {
    this.template = template;
    this.pattern = pattern;
    this.variables = variables;
  }

  /** Create a UriTemplate ex. "/echo/{message}" */
  public static UriTemplate parse(String template) {
    List<String> variables = new ArrayList<>();

    List<String> templateParts =
        Splitter.on("/")
            .omitEmptyStrings() //important to omit empty strings if template starts with /
            .trimResults()
            .splitToList(template);

    List<String> regexParts = new ArrayList<>();
    for (String part : templateParts) {
      if (isVariable(part)) {
        String variable = parseVariable(part);
        variables.add(variable);
        regexParts.add("(.*)");
      } else {
        regexParts.add(part);
      }
    }

    String regex = Joiner.on("/").join(regexParts);

    if (template.startsWith("/")) {
      regex = "/" + regex;
    }

    if (template.endsWith("/")) {
      regex += "/";
    }

    return new UriTemplate(template, Pattern.compile(regex), variables);
  }

  /**
   * Replace the variables in the template with the provided values.
   *
   * @param values The values to replace in the template
   * @return The replaced template
   * @throws IllegalArgumentException if incorrect number of values provided
   */
  public String expand(String... values) {
    if (values.length != variables.size()) {
      throw new IllegalArgumentException("Incorrect amount of values given to expand.");
    }

    int counter = 0;
    List<String> templateParts =
        Splitter.on("/")
            .omitEmptyStrings() //important to omit empty strings if template starts with /
            .trimResults()
            .splitToList(template);

    List<String> expandedParts = new ArrayList<>();
    for (String part : templateParts) {
      if (isVariable(part)) {
        expandedParts.add(values[counter]);
        counter += 1;
      } else {
        expandedParts.add(part);
      }
    }

    String expanded = Joiner.on("/").join(expandedParts);

    if (template.startsWith("/")) {
      expanded = "/" + expanded;
    }

    if (template.endsWith("/")) {
      expanded += "/";
    }

    return UrlEscapers.urlFragmentEscaper().escape(expanded);
  }

  private static String parseVariable(String value) {
    return value.substring(1, value.length() - 1);
  }

  private static boolean isVariable(String value) {
    return value.startsWith("{") && value.endsWith("}");
  }

  public String getTemplate() {
    return template;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public List<String> getVariables() {
    return Collections.unmodifiableList(variables);
  }

  /**
   * Match the given URI to a map of variable values. Keys in the returned map are variable names,
   * values are variable values, as occurred in the given URI.
   *
   * <p>Example:
   *
   * <pre class="code">
   * UriTemplate template = new UriTemplate("http://example.com/hotels/{hotel}/bookings/{booking}");
   * System.out.println(template.match("http://example.com/hotels/1/bookings/42"));
   * </pre>
   *
   * will print:
   *
   * <blockquote>
   *
   * {@code {hotel=1, booking=42}}
   *
   * </blockquote>
   *
   * @param uri the URI to match to
   * @return a map of variable values
   */
  public Map<String, String> match(String uri) {
    Preconditions.checkNotNull(uri);
    Map<String, String> result = new HashMap<>(variables.size());
    Matcher matcher = pattern.matcher(uri);
    if (matcher.find()) {
      for (int i = 1; i <= matcher.groupCount(); i++) {
        String name = this.variables.get(i - 1);
        String value = matcher.group(i);
        result.put(name, value);
      }
    }
    return result;
  }

  /**
   * Tests if the provided uri matches the template
   *
   * @param uri the URI to match to
   * @return {@code true} if it matches; {@code false} otherwise
   */
  public boolean matches(@Nullable String uri) {
    if (uri == null) {
      return false;
    }
    return pattern.matcher(uri).matches();
  }

  @Override
  public String toString() {
    return template;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UriTemplate that = (UriTemplate) o;
    return Objects.equals(template, that.template);
  }

  @Override
  public int hashCode() {
    return Objects.hash(template);
  }
}
