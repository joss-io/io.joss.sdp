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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayloadMap
{

  // ones we might care about from
  // http://www.iana.org/assignments/rtp-parameters/rtp-parameters.xhtml#rtp-parameters-1
  private static final Map<Integer, RtpMapEntry> DEFAULT_PAYLOAD_MAP_TYPES;

  static
  {
    DEFAULT_PAYLOAD_MAP_TYPES = Arrays.asList(
        new RtpMapEntry(0, "PCMU", 8000, 1),
        new RtpMapEntry(3, "GSM", 8000, 1),
        new RtpMapEntry(4, "G723", 8000, 1),
        new RtpMapEntry(8, "PCMA", 8000, 1),
        new RtpMapEntry(9, "G722", 8000, 1),
        new RtpMapEntry(18, "G729", 8000, 1))
        .stream()
        .collect(Collectors.toMap(RtpMapEntry::getId, (e) -> e));
  }

  private final List<Integer> formats;
  private final Map<Integer, PayloadMapEntry> byId = new HashMap<>();
  private final Map<String, PayloadMapEntry> byName = new HashMap<>();

  public PayloadMapEntry get(final int format)
  {
    return this.byId.get(format);
  }

  public PayloadMap(final List<String> f, final List<Attribute> a)
  {

    this.formats = f.stream().map(e -> Integer.parseInt(e)).collect(Collectors.toList());

    for (final int pt : this.formats)
    {
      final RtpMapEntry id = getNameForId(a, pt);
      final String fmtps = getFormatParams(a, pt);
      final PayloadMapEntry e = new PayloadMapEntry(id, fmtps);
      this.byId.put(pt, e);

      if (e.getEntry() != null)
      {
        this.byName.put(e.getEntry().getFormat().toLowerCase(), e);
      }

    }

    this.formats.removeIf(pt -> !this.byId.containsKey(pt));

  }

  public PayloadMapEntry getPreferred()
  {
    return this.byId.get(this.formats.get(0));
  }

  private static String getFormatParams(final List<Attribute> attributes, final int pt)
  {

    final String key = Integer.toString(pt) + " ";

    for (final Attribute a : attributes)
    {
      if (!a.getKey().equals("fmtp"))
      {
        continue;
      }

      if (!a.getValue().startsWith(key))
      {
        continue;
      }

      return a.getValue().substring(key.length()).trim();

    }

    return null;
  }

  private static RtpMapEntry getNameForId(final List<Attribute> attributes, final int pt)
  {

    for (final Attribute a : attributes)
    {

      if (!a.getKey().equals("rtpmap"))
      {
        continue;
      }

      final RtpMapEntry e = RtpMapEntry.parse(a.getValue());

      if (e.getId() == pt)
      {
        return e;
      }

    }

    return DEFAULT_PAYLOAD_MAP_TYPES.get(pt);

  }

  public static RtpMapEntry lookupStatic(final int k)
  {
    return DEFAULT_PAYLOAD_MAP_TYPES.get(k);
  }

}
