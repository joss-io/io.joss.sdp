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

import lombok.EqualsAndHashCode;
import lombok.experimental.Builder;

@Builder
@EqualsAndHashCode
public class Connection
{

  // e.g, "IN"
  private final String networkType;

  // e.g "IP4"
  private final String addressType;

  // e.g, "1.2.3.4"
  private final String address;

  public String addressType()
  {
    return this.addressType;
  }

  public String networkType()
  {
    return this.networkType;
  }

  public String address()
  {
    return this.address;
  }

  @Override
  public String toString()
  {

    final StringBuilder sb = new StringBuilder();

    sb.append(this.networkType).append(" ");
    sb.append(this.addressType).append(" ");
    sb.append(this.address);

    return sb.toString().trim();

  }

  public static Connection create(final InetAddress addr)
  {
    if (addr instanceof Inet4Address)
    {
      return new Connection("IN", "IP4", addr.getHostAddress());
    }
    if (addr instanceof Inet6Address)
    {
      return new Connection("IN", "IP6", addr.getHostAddress());
    }
    throw new RuntimeException("Unknown inet address type");
  }

  public static Connection create(InetAddressType addressType, String address)
  {
    return new Connection("IN", addressType.toString(), address);
  }

}
