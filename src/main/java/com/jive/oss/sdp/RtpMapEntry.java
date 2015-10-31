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
import java.util.List;

import lombok.Value;

@Value
public class RtpMapEntry
{

  private int id;
  private String format;
  private Integer rate;
  private Integer channels;

  public static RtpMapEntry parse(final String value)
  {
    int idx = value.indexOf(' ');
    int id = Integer.parseInt(value.substring(0, idx));
    List<String> items = Arrays.asList(value.substring(idx + 1).split("/"));
    return new RtpMapEntry(
        id,
        items.get(0),
        items.size() > 1 ? Integer.parseInt(items.get(1)) : null,
        items.size() > 2 ? Integer.parseInt(items.get(2)) : null);
  }

  @Override
  public String toString()
  {
    return id + " " + format
        + (rate == null ? "" : ("/" + rate))
        + (format.equals("opus") ? "/2" : "");
  }

}
