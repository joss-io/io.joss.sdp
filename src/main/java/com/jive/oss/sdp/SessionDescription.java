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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.Wither;

/**
 * Represents a full session description.
 * 
 * Use {@link SdpParser} to parse a string into one.
 * 
 * Note that the instance is immutable. Mutations return a new model. Use a builder if you wish to do a lot of manipulation.
 * 
 * @author Theo Zourzouvillys
 * 
 */

@Builder
@Wither
@EqualsAndHashCode
public class SessionDescription
{

  private final Origin origin;
  private final Info info;
  private final Connection connection;
  private final List<Attribute> attributes;
  private final List<Media> medias;
  private final String subject;

  @Singular
  private final List<String> unknowns;

  public Origin origin()
  {
    return this.origin;
  }

  public Info info()
  {
    return this.info;
  }

  public Connection connection()
  {
    return this.connection;
  }

  public List<Attribute> attributes()
  {
    if (this.attributes == null)
    {
      return Collections.emptyList();
    }
    return new ArrayList<>(this.attributes);
  }

  public boolean hasAttribute(final String key)
  {
    return this.attributes.stream().filter(a -> key.equals(a.getKey())).findAny().isPresent();
  }

  public SessionDescription withAttribute(final Attribute attr)
  {
    return this.withAttributes(
        Stream.concat(
            this.attributes().stream(),
            Stream.of(attr))
            .collect(Collectors.toList()));
  }

  public SessionDescription withoutAttribute(final String key)
  {
    return this.withoutAttribute(key, null);
  }

  public SessionDescription withoutAttribute(final String key, final String value)
  {
    return this.withAttributes(
        this.attributes().stream()
            .filter((attr) -> !(key.equals(attr.getKey())
                && (value == null || value.equals(attr.getValue()))))
            .collect(Collectors.toList()));
  }

  public List<Media> medias()
  {
    if (this.medias == null)
      return Collections.emptyList();

    return new ArrayList<>(medias);
  }

  public List<Media> medias(Predicate<Media> pred)
  {
    if (this.medias == null)
    {
      return Collections.emptyList();
    }
    return this.medias.stream().filter(pred).collect(Collectors.toList());
  }

  public SessionDescription newVersion()
  {
    return this.withOrigin(this.origin.withVersion(this.origin.version() + 1));
  }

  public SessionDescription withDirection(final SdpDirection d)
  {
    final List<Attribute> attrs = this.attributes.stream().filter(a -> !Media.isDirection(a)).collect(Collectors.toList());
    attrs.add(new Attribute(d.toString().toLowerCase(), null));
    return this.withAttributes(attrs);
  }

  public SessionDescription withoutConnection()
  {
    return this.withConnection(null);
  }

  public SessionDescription withoutDirection()
  {
    final List<Attribute> attrs = this.attributes.stream().filter(a -> !Media.isDirection(a)).collect(Collectors.toList());
    return this.withAttributes(attrs);
  }

  public Optional<SdpDirection> direction()
  {

    for (final Attribute a : this.attributes())
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

  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder();

    sb.append("v=0").append("\r\n");
    if (this.origin != null)
    {
      sb.append("o=").append(this.origin).append("\r\n");
    }

    if (subject != null)
    {
      sb.append("s=").append(subject).append("\r\n");
    }
    else
    {
      sb.append("s=-").append("\r\n");
    }

    if (this.connection != null)
    {
      sb.append("c=").append(this.connection).append("\r\n");
    }
    sb.append("t=0 0").append("\r\n");

    for (final Attribute a : this.attributes())
    {
      sb.append("a=").append(a).append("\r\n");
    }

    this.medias().forEach(sb::append);

    return sb.toString();
  }

  public static SessionDescription parse(final byte[] sdp)
  {
    return parse(new String(sdp, StandardCharsets.UTF_8));
  }

  public static SessionDescription parse(final String sdp)
  {
    if (sdp.trim().length() == 0)
    {
      return SessionDescription.builder().build();
    }
    SdpReader reader = new SdpReader(sdp);
    SdpParser parser = new SdpParser(reader);
    return parser.read();
  }

  /**
   * Modifies the media streams by passing through a function.
   */

  public SessionDescription mutateMedia(final Function<Media, Media> mutator)
  {
    return withMedias(this.medias.stream().map(mutator).collect(Collectors.toList()));
  }

  /**
   * Modifies a single media stream by passing through a function.
   */

  public SessionDescription mutateMedia(int index, final Function<Media, Media> mutator)
  {
    ArrayList<Media> nm = new ArrayList<>(medias);
    nm.set(index, mutator.apply(nm.get(index)));
    return withMedias(nm);
  }

  /**
   * Disable media streams that match the given predicate and returns a new SDP instance.
   *
   * The predicate receives the index and the media. return true if it should be disabled.
   *
   * will not be applied for already disabled streams.
   *
   */

  public SessionDescription disable(final BiPredicate<Integer, Media> predicate)
  {

    List<Media> medias = new ArrayList<>();

    for (int i = 0; i < this.medias.size(); ++i)
    {

      Media m = media(i);

      if (m.port() == 0)
      {
        medias.add(m);
      }
      else if (predicate.test(i, m))
      {
        medias.add(m.disabled());
      }
      else
      {
        medias.add(m);
      }

    }

    return withMedias(medias);
  }

  public SessionDescription replaceMedia(final Media from, final Media to)
  {
    return withMedias(this.medias.stream().map(m -> m == from ? to : m).collect(Collectors.toList()));
  }

  public Optional<Attribute> getAttribute(final String key)
  {
    return this.attributes.stream().filter(a -> key.equals(a.getKey())).findFirst();
  }

  private static final List<String> DISTRIBUTABLE_ATTRS = Arrays.asList("fingerprint");

  public SessionDescription getCanonicalForm()
  {
    Optional<Connection> sdpConnection = Optional.ofNullable(this.connection());
    List<Attribute> attrsToDistribute = this.attributes().stream().filter((a) -> DISTRIBUTABLE_ATTRS.contains(a.getKey()))
        .collect(Collectors.toList());
    // Should I remove the connection and attributes from the session-level...?
    return this.withMedias(
        this.medias().stream()
            .map((m) -> canonicalize(m, sdpConnection, attrsToDistribute))
            .collect(Collectors.toList()));
  }

  private Media canonicalize(
      final Media m, final Optional<Connection> conn, final List<Attribute> attrs)
  {
    Media newMedia = m;
    if (conn.isPresent() && m.getConnection() == null)
    {
      newMedia = newMedia.withConnection(
          conn.get());
    }
    for (final Attribute attr : attrs)
    {
      if (!newMedia.hasAttribute(attr.getKey()))
      {
        newMedia = newMedia.withAttribute(attr);
      }
    }
    return newMedia;
  }

  public Media media(int i)
  {
    return medias().get(i);
  }

  public Optional<String> attribute(String name)
  {
    return attributes()
        .stream()
        .filter(a -> a.getKey().equals(name))
        .findAny()
        .map(e -> e.getValue() == null ? "" : e.getValue());
  }

  public Optional<Integer> ptime()
  {
    return attribute("ptime").map(p -> Integer.parseInt(p));
  }

  public Optional<Integer> maxptime()
  {
    return attribute("maxptime").map(p -> Integer.parseInt(p));
  }

  /**
   * returns a new {@link SessionDescription} instance with normalized connection attributes. If all enabled media lines contain the same c
   * line, moves it to the main session description.
   * 
   * @return
   */

  public SessionDescription normalize()
  {

    // TODO: remove this hack to work around raisin bug.

    Connection c = null;

    for (Media mx : this.medias(Media::isEnabled))
    {

      Connection mine = mx.connection().orElse(connection());

      if (c == null)
      {
        c = mine;
      }
      else if (!c.equals(mine))
      {
        return this;
      }

    }

    if (c == null)
    {
      return this;
    }

    Connection xc = c;

    return this.mutateMedia(m -> m.withoutConnection()).withConnection(c);

  }

}
