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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SdpUtils
{

  /**
   * returns a trimmed, string split on the given regex, with any empty strings removed.
   *
   * @param regex
   * @return
   */

  public static List<String> split(final CharSequence val, final String regex)
  {
    return Arrays.stream(val.toString().split(regex)).map(String::trim).filter(e -> !e.isEmpty()).collect(Collectors.toList());
  }

  public static String getAddressType(final InetAddress addr)
  {
    if (addr instanceof Inet4Address)
    {
      return "IP4";
    }
    else if (addr instanceof Inet6Address)
    {
      return "IP6";
    }
    throw new RuntimeException("Unknown Address Type");
  }

}
