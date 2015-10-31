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

public class SdpCryptoConfig
{

  @Value
  public static class Entry
  {
    private int id;
    private String suite;
    private String keyParams;
    private String sessionParams;

    public String toString()
    {
      StringBuilder sb = new StringBuilder();

      sb.append(id).append(' ').append(suite).append(' ').append(keyParams);

      if (this.sessionParams != null)
      {
        sb.append(' ').append(sessionParams);
      }

      return sb.toString();
    }

  }

  private List<Entry> entries;

  public SdpCryptoConfig(List<Entry> entries)
  {
    this.entries = entries;
  }

  public static SdpCryptoConfig.Entry parse(String line)
  {

    List<String> parts = Arrays.asList(line.split(" ")); 

    if (parts.size() < 3)
    {
      throw new IllegalArgumentException("malformed crypto line");
    }

    return new Entry(
        Integer.parseInt(parts.get(0)),
        parts.get(1),
        parts.get(2),
        parts.size() > 3 ? parts.get(3) : null);

  }

  public List<Entry> entries()
  {
    return entries;
  }

}
