package com.jive.oss.sdp;

/*
 * #%L
 * sdp
 * %%
 * Copyright (C) 2015 Jive Communications, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.jive.oss.sdp.SdpCryptoConfig.Entry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.Wither;

@Builder
@Wither
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Media
{

  @NonNull
  private final String type;

  private final int port;

  @NonNull
  private final String protocol;

  private final List<String> formats;

  @Getter
  private final Connection connection;

  private final List<Attribute> attributes;
  
  @Singular
  private final List<String> unknowns;

  /**
   * The type, e.g audio, video, image.
   */

  public String type()
  {
    return this.type;
  }

  public int port()
  {
    return this.port;
  }

  /**
   * The protocol, e.g RTP/AVP
   */

  public String protocol()
  {
    return this.protocol;
  }

  public List<String> formats()
  {
    if (this.formats == null)
      return Collections.emptyList();
    return new ArrayList<>(formats);
  }

  public String singleAttributeValue(final String key)
  {
    return this.attributes.stream().filter(a -> key.equals(a.getKey())).findFirst()
        .orElse(new Attribute("", null)).getValue();
  }

  public List<Attribute> attributes()
  {
    if (this.attributes == null)
      return Collections.emptyList();
    return new ArrayList<>(this.attributes);
  }

  public Media withAttribute(final Attribute attr)
  {

    List<Attribute> updated = new ArrayList<>();

    if (attributes != null)
    {
      updated.addAll(attributes);
    }

    updated.add(attr);

    return this.withAttributes(updated);
  }

  public Media withAttribute(final String key, final String value)
  {
    return this.withAttribute(new Attribute(key, value));
  }

  public boolean hasAttribute(final String key)
  {
    return this.attributes.stream().filter(a -> key.equals(a.getKey())).findAny().isPresent();
  }

  @Override
  public String toString()
  {

    final StringBuilder sb = new StringBuilder();

    sb.append(initialLine(this)).append("\r\n");

    if (this.connection != null)
    {
      sb.append("c=").append(this.connection).append("\r\n");
    }

    for (final Attribute a : this.attributes())
    {
      sb.append("a=").append(a).append("\r\n");
    }

    return sb.toString();
  }

  public static String initialLine(Media m)
  {

    final StringBuilder sb = new StringBuilder();

    sb.append("m=").append(m.type).append(" ").append(m.port).append(" ").append(m.protocol).append(" ");

    if (m.formats != null)
    {
      sb.append(m.formats.stream().collect(Collectors.joining(" ")));
    }

    return sb.toString();
  }

  public Optional<SdpDirection> direction()
  {

    for (final Attribute a : this.attributes)
    {
      switch (a.getKey())
      {
        case "sendonly":
          return Optional.of(SdpDirection.SendOnly);
        case "recvonly":
          return Optional.of(SdpDirection.RecvOnly);
        case "inactive":
          return Optional.of(SdpDirection.Inactive);
        case "sendrecv":
          return Optional.of(SdpDirection.SendRecv);
        default:
          break;
      }
    }

    return Optional.empty();

  }

  public Media filterFormats(final Predicate<String> filter)
  {
    return this
        .withFormats(this.formats.stream().filter(filter).collect(Collectors.toList()))
        .withAttributes(this.attributes.stream().filter(a -> this.filterRtpMap(a, filter)).collect(
            Collectors.toList()));
  }

  private boolean filterRtpMap(final Attribute a, final Predicate<String> filter)
  {
    if (a.getKey().equals("rtpmap") || a.getKey().equals("fmtp"))
    {
      return filter.test(a.getValue().substring(0, a.getValue().indexOf(' ')));
    }
    return true;
  }

  public List<RtpMapEntry> rtpmap()
  {
    final Map<Integer, RtpMapEntry> mapping = this.attributes().stream()
        .filter(a -> a.getKey().equals("rtpmap"))
        .map(a -> RtpMapEntry.parse(a.getValue()))
        .collect(Collectors.toMap(
            e -> e.getId(),
            e -> e,
            (a, b) -> a,
            () -> new LinkedHashMap<>()));

    return this.formats.stream()
        .map(id -> Integer.parseInt(id))
        .map(id -> mapping.computeIfAbsent(id, k -> PayloadMap.lookupStatic(k)))
        .filter((f) -> f != null) // in case we don't find it in our default map
        .collect(Collectors.toList());
  }

  public PayloadMap payloadMap()
  {
    return new PayloadMap(formats(), attributes());
  }

  public Media withDirection(final SdpDirection d)
  {
    final List<Attribute> attrs = attributes == null
        ? new ArrayList<>()
        : this.attributes.stream().filter(a -> !isDirection(a)).collect(Collectors.toList());
    attrs.add(new Attribute(d.toString().toLowerCase(), null));
    return this.withAttributes(attrs);
  }

  /**
   * returns a read-only copy of this media, but disabled. all attributes are removed, and the port is set to 0. The first format is
   * included to be compliant with the SDP format.
   *
   * @return
   */

  public Media disabled()
  {
    return builder()
        .type(this.type)
        .port(0)
        .protocol(this.protocol)
        .formats((this.formats.isEmpty()) ? new ArrayList<>() : this.formats.subList(0, 1))
        .attributes(Collections.emptyList())
        .build();
  }

  static boolean isDirection(final Attribute a)
  {
    switch (a.getKey())
    {
      case "sendonly":
      case "recvonly":
      case "inactive":
      case "sendrecv":
        return true;
      default:
        return false;

    }
  }

  public Media inverseDirection()
  {

    final SdpDirection d = this.direction().orElse(null);

    if (d == null)
    {
      return this;
    }

    switch (d)
    {
      case Inactive:
      case SendRecv:
        return this;
      case RecvOnly:
        return this.withDirection(SdpDirection.SendOnly);
      case SendOnly:
        return this.withDirection(SdpDirection.RecvOnly);
    }

    return this;

  }

  public Media withoutAttributes(final Predicate<Attribute> predicate)
  {
    return this.withAttributes(this.attributes.stream().filter(a -> !predicate.test(a)).collect(
        Collectors.toList()));
  }

  /**
   * cleans up the media line, removing any rtpmap or fmtp params which aren't in the format list.
   *
   * @return
   */

  public Media cleanup()
  {
    return this.withoutAttributes(a -> this.unused(a));
  }

  /**
   * true if this attribute related to a format not defined in the format list.
   *
   * @param a
   * @return
   */

  private boolean unused(final Attribute a)
  {

    if (a.getKey().equals("rtpmap") || a.getKey().equals("fmtp"))
    {
      final int idx = a.getValue().indexOf(' ');
      final int fmt = Integer.parseInt(a.getValue().substring(0, idx));
      return !this.formats.contains(fmt);
    }

    return false;
  }

  public Media withoutConnection()
  {
    return this.withConnection(null);
  }

  /**
   * returns the FIRST element that matches the given attribute name.
   */

  public Optional<String> attribute(String name)
  {
    return attributes.stream()
        .filter(a -> a.getKey().equals(name))
        .findFirst()
        .map(e -> e.getValue() == null ? "" : e.getValue());
  }

  public List<String> attributes(String name)
  {
    return attributes.stream()
        .filter(a -> a.getKey().equals(name))
        .map(e -> e.getValue() == null ? "" : e.getValue())
        .collect(Collectors.toList());
  }

  public Optional<Connection> connection()
  {
    return Optional.ofNullable(connection);
  }

  /**
   * returns the c line from the media, otherwise fallback to the entry in the sdp. Throws an {@link InvalidSessionDescriptionException} if
   * the SDP doesn't contain one (which is illegal).
   *
   * @param session
   * @return
   * @throws InvalidSessionDescriptionException
   */

  public Connection connection(SessionDescription session) throws InvalidSessionDescriptionException
  {
    Connection c = Optional.ofNullable(connection).orElse(session.connection());

    if (c == null)
    {
      throw new InvalidSessionDescriptionException("no c line in specified media or session");
    }

    return c;
  }

  public String mline()
  {
    final StringBuilder sb = new StringBuilder();

    sb.append("m=").append(this.type).append(" ").append(this.port).append(" ").append(
        this.protocol).append(" ");

    if (this.formats != null)
    {
      sb.append(formats.stream().collect(Collectors.joining(" ")));
    }
    return sb.toString();
  }

  public Media replaceAttribute(String key, String value)
  {
    return withoutAttributes(a -> a.getKey().equals(key)).withAttribute(new Attribute(key, value));
  }

  public Optional<Integer> ptime()
  {
    return attribute("ptime").map(p -> Integer.parseInt(p));
  }

  public Optional<Integer> maxptime()
  {
    return attribute("maxptime").map(p -> Integer.parseInt(p));
  }

  public Optional<SdpCryptoConfig> crypto()
  {

    List<SdpCryptoConfig.Entry> entries = new ArrayList<>();

    for (String line : attributes("crypto"))
    {
      entries.add(SdpCryptoConfig.parse(line));
    }

    if (entries.isEmpty())
    {
      return Optional.empty();
    }

    return Optional.of(new SdpCryptoConfig(entries));

  }

  public boolean isDisabled()
  {
    return port() == 0;
  }

  public boolean isEnabled()
  {
    return port() != 0;
  }

  public Media withCrypto(SdpCryptoConfig crypto)
  {

    if (crypto == null || crypto.entries().isEmpty())
    {
      return this;
    }

    List<Attribute> attrs = new ArrayList<>(attributes());

    for (Entry e : crypto.entries())
    {
      attrs.add(new Attribute("crypto", e.toString()));
    }

    return this.withAttributes(attrs);

  }



  public Media withExtraAttributes(List<Attribute> attrs)
  {
    List<Attribute> attributes = new ArrayList<>(attributes());
    attributes.addAll(attrs);
    return withAttributes(attributes);
  }

  public Media withoutAttributes(String... keys)
  {
    Set<String> ks = new HashSet<String>(Arrays.asList(keys));
    return withoutAttributes(key -> ks.contains(key.getKey()));
  }

  public static MediaBuilder rtpAudioBuilder(int port, int... formats)
  {
    return builder()
        .type("audio")
        .protocol("RTP/AVP")
        .port(port)
        .formats(Arrays.stream(formats).mapToObj(String::valueOf).collect(Collectors.toList()));
  }

}
