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

import java.util.LinkedList;
import java.util.List;

import com.jive.oss.sdp.Connection.ConnectionBuilder;
import com.jive.oss.sdp.Media.MediaBuilder;
import com.jive.oss.sdp.Origin.OriginBuilder;
import com.jive.oss.sdp.SessionDescription.SessionDescriptionBuilder;

public class SdpParser
{

  private final SdpReader reader;
  private final SessionDescriptionBuilder b = SessionDescription.builder();

  public SdpParser(final SdpReader reader)
  {
    this.reader = reader;
  }

  public SessionDescription read()
  {

    // Session description

    if (this.reader.remaining() < 1)
    {
      throw new IllegalArgumentException("v=0 missing from SDP");
    }

    // v= (protocol version)

    if (!this.reader.nextType().equals("v"))
    {
      throw new IllegalArgumentException("Invalid SDP version line");
    }

    if (!this.reader.readValue().equals("0"))
    {
      throw new IllegalArgumentException("Invalid SDP version");
    }

    this.parseHeaders();

    return this.b.build();

  }

  private void parseHeaders()
  {

    final List<Attribute> attributes = new LinkedList<>();
    final List<Media> medias = new LinkedList<>();

    while (this.reader.remaining() > 0)
    {

      final String type = this.reader.nextType().toString();

      if (type.equals("m"))
      {
        break;
      }

      final String value = this.reader.readValue().toString();

      switch (type)
      {
        case "o":
          // o= (originator and session identifier)
          this.b.origin(this.parseOrigin(value));
          break;
        case "s":
          // s= (session name)
          this.b.subject(value);
          break;
        case "i":
          // i=* (session information)
          break;
        case "u":
          // u=* (URI of description)
          break;
        case "e":
          // e=* (email address)
          break;
        case "p":
          // p=* (phone number)
          break;
        case "c":
          // c=* (connection information -- not required if included in
          // all media)
          this.b.connection(this.parseConnection(value));
          break;
        case "b":
          // b=* (zero or more bandwidth information lines)
          break;
        case "z":
          // One or more time descriptions ("t=" and "r=" lines; see below)
          // z=* (time zone adjustments)
          break;
        case "k":
          // k=* (encryption key)
          break;
        case "a":
          // a=* (zero or more session attribute lines)
          attributes.add(Attribute.fromString(value));
          break;
        case "t":
          // Zero or more media descriptions
          // Time description
          // t= (time the session is active)
          break;
        case "r":
          // r=* (zero or more repeat times)
          break;
        default:
          this.b.unknown(this.reader.nextType().toString());
          break;

      }

    }

    this.b.attributes(attributes);

    while (this.reader.remaining() > 0)
    {
      final Media m = this.parseMedia();
      medias.add(m);
    }

    this.b.medias(medias);

  }

  private Connection parseConnection(final String value)
  {
    final ConnectionBuilder b = Connection.builder();
    final List<String> parts = SdpUtils.split(value, " ");
    b.networkType(parts.get(0));
    b.addressType(parts.get(1));
    b.address(parts.get(2));
    return b.build();
  }

  private Origin parseOrigin(final String value)
  {

    final List<String> parts = SdpUtils.split(value, " ");

    final OriginBuilder b = Origin.builder();

    b.username(parts.get(0));
    b.sessionId(Long.parseLong(parts.get(1)));
    b.version(Long.parseLong(parts.get(2)));
    b.networkType(parts.get(3));
    b.addressType(parts.get(4));
    b.address(parts.get(5));

    return b.build();

  }


  private Media parseMedia()
  {

    final MediaBuilder b = Media.builder();

    final List<Attribute> attributes = new LinkedList<>();

    final List<String> mlines = SdpUtils.split(this.reader.readValue(), " ");

    // private final String type;
    b.type(mlines.get(0));

    // private final int port;
    b.port(Integer.parseInt(mlines.get(1)));

    // private final String protocol;
    b.protocol(mlines.get(2));

    // private final List<Integer> formats;
    b.formats(mlines.subList(3, mlines.size()));

    while (this.reader.remaining() > 0)
    {

      final String type = this.reader.nextType().toString();

      if (type.equals("m"))
      {
        break;
      }

      final String value = this.reader.readValue().toString();

      switch (type)
      {
        case "i":
          // i=* (media title)
          break;
        case "c":
          // c=* (connection information -- optional if included at
          // session level)
          b.connection(this.parseConnection(value));
          break;
        case "b":
          // b=* (zero or more bandwidth information lines)
          break;
        case "k":
          // k=* (encryption key)
          break;
        case "a":
          // a=* (zero or more media attribute lines)
          attributes.add(Attribute.fromString(value));
          break;
        default:
          b.unknown(this.reader.nextType().toString());
          break;

      }

    }

    b.attributes(attributes);


    return b.build();

  }

}
