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

import java.net.InetAddress;
import java.util.concurrent.ThreadLocalRandom;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

@Builder
@Wither
@AllArgsConstructor
@EqualsAndHashCode
public class Origin
{

  // o=- 1408401718 1408401718 IN IP4 192.168.1.242
  private final String username;

  //
  private final long sessionId;

  //
  private final long version;

  // e.g, "IN"
  private final String networkType;

  // e.g "IP4"
  private final String addressType;

  // e.g, "1.2.3.4"
  private final String address;

  public String username()
  {
    return this.username;
  }

  public long sessionId()
  {
    return this.sessionId;
  }

  public long version()
  {
    return this.version;
  }

  public String networkType()
  {
    return this.networkType;
  }

  public String addressType()
  {
    return this.addressType;
  }

  public String address()
  {
    return this.address;
  }

  @Override
  public String toString()
  {

    final StringBuilder sb = new StringBuilder();

    sb.append(this.username).append(" ");
    sb.append(this.sessionId).append(" ");
    sb.append(this.version).append(" ");
    sb.append(this.networkType).append(" ");
    sb.append(this.addressType).append(" ");
    sb.append(this.address);

    return sb.toString();

  }

  public static Origin create(final String username, final InetAddress local)
  {
    return new Origin(
        username,
        ThreadLocalRandom.current().nextLong(0, Integer.MAX_VALUE),
        ThreadLocalRandom.current().nextLong(0, Integer.MAX_VALUE),
        "IN",
        SdpUtils.getAddressType(local),
        local.getHostAddress());
  }

  public static Origin create(final String username, final InetAddress local, long sessionId)
  {
    return new Origin(
        username,
        sessionId,
        ThreadLocalRandom.current().nextLong(0, Integer.MAX_VALUE),
        "IN",
        SdpUtils.getAddressType(local),
        local.getHostAddress());
  }

  public static Origin create(
      final String username,
      InetAddressType addrType,
      final String host,
      long sessionId,
      long version)
  {
    return new Origin(
        username,
        sessionId,
        version,
        "IN",
        addrType.toString(),
        host);
  }

  public static Origin create(
      final String username,
      SdpAddress addr,
      long sessionId,
      long version)
  {
    return new Origin(
        username,
        sessionId,
        version,
        addr.getNetworkType(),
        addr.getAddressType(),
        addr.getAddress());
  }

  public static Origin create(
      final String username,
      SdpAddress addr,
      long sessionId)
  {
    return new Origin(
        username,
        sessionId,
        1,
        addr.getNetworkType(),
        addr.getAddressType(),
        addr.getAddress());
  }

  public Origin newVersion()
  {
    return withVersion(version() + 1);
  }

  public static Origin createRandomSessionId(String name, SdpAddress addr)
  {
    return create(name, addr, ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE / 2));
  }

}
