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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.jive.oss.sdp.Attribute;
import com.jive.oss.sdp.Connection;
import com.jive.oss.sdp.Info;
import com.jive.oss.sdp.Media;
import com.jive.oss.sdp.Origin;
import com.jive.oss.sdp.SdpDirection;
import com.jive.oss.sdp.SessionDescription;

public class SessionDescriptionTest
{

  @Test
  public void test()
  {

    SessionDescription sdp = SessionDescription.parse("v=0\n" +
        "o=- 1111111 1408401718 IN IP4 192.168.1.242\n" +
        "s=-\n" +
        "c=IN IP4 192.168.1.242\n" +
        "t=0 0\n" +
        "m=audio 16028 RTP/AVP 9 8 2 18 0 101\n" +
        "a=rtpmap:9 G722/8000\n" +
        "a=rtpmap:8 PCMA/8000\n" +
        "a=rtpmap:2 G726-32/8000\n" +
        "a=rtpmap:18 G729/8000\n" +
        "a=rtpmap:0 PCMU/8000\n" +
        "a=rtpmap:101 telephone-event/8000\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +
        "a=ptime:20");

    System.err.println(sdp);
    System.err.println(sdp.medias().get(0).rtpmap());

    sdp = sdp.mutateMedia(m -> m.withPort(0).filterFormats(id -> id.equals("0")));

    Assert.assertEquals(SdpDirection.SendRecv, sdp.medias().get(0).direction().orElse(null));

    sdp = sdp.mutateMedia(m -> m.withDirection(SdpDirection.Inactive)).newVersion();
    System.err.println(sdp);
    sdp = sdp.disable((idx, m) -> idx > 0);
    System.err.println(sdp);

  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidVersion()
  {
    SessionDescription.parse("v=1\r\n");
  }

  @Test
  public void test1()
  {

    SessionDescription sdp = SessionDescription.parse("v=0\n" +
        "o=- 1111111 1408401718 IN IP4 192.168.1.242\n" +
        "s=-\n" +
        "c=IN IP4 192.168.1.242\n" +
        "t=0 0\n" +
        "m=audio 16028 RTP/SAVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +

        "m=audio 16028 RTP/AVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendonly\n" +

        "m=audio 16028 RTP/AVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +

        "m=audio 16028 RTP/AVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +

        "a=ptime:20");

    final AtomicBoolean set = new AtomicBoolean(false);

    sdp = sdp
        .withOrigin(new Origin("raisin", 1, 1, "IN", "IP4", "1.2.3.4"))
        .withConnection(Connection.create(InetAddresses.forString("1.2.3.4")))
        .disable((idx, m) -> !m.protocol().equals("RTP/AVP") || !m.type().equals("audio"))
        .disable((idx, m) -> !set.compareAndSet(false, true))
        .mutateMedia(m -> m.inverseDirection()
            .withPort(12345)
            .cleanup());

  }

  @Test
  public void testParser()
  {

    final SessionDescription sdp = SessionDescription.parse("v=0\n" +
        "o=- 1111111 1408401718 IN IP4 192.168.1.242\n" +
        "s=-\n" +
        "c=IN IP4 192.168.1.242\n" +
        "t=0 0\n" +

        "m=audio 2 RTP/SAVP 0 2 3 101\n" +
        "c=IN IP4 1.2.3.4\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +

        "m=audio 4 RTP/AVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendonly\n" +

        "m=audio 6 RTP/AVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +

        "m=audio 8 RTP/AVP 0\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +
        "a=ptime:20");

    Assert.assertEquals(
        sdp.origin(),
        new Origin("-", 1111111, 1408401718, "IN", "IP4", "192.168.1.242"));

    Assert.assertEquals(
        sdp.connection(),
        Connection.create(InetAddresses.forString("192.168.1.242")));

    Assert.assertEquals(
        4,
        sdp.medias().size());

    Assert.assertEquals("audio", sdp.media(0).type());
    Assert.assertEquals(2, sdp.media(0).port());
    Assert.assertEquals("RTP/SAVP", sdp.media(0).protocol());
    Assert.assertEquals(Lists.newArrayList("0", "2", "3", "101"), sdp.media(0).formats());
    Assert.assertEquals(Connection.create(InetAddresses.forString("1.2.3.4")), sdp.media(0)
        .connection().get());
    Assert.assertEquals(Lists.newArrayList(new Attribute("fmtp", "101 0-15"), new Attribute(
        "sendrecv")), sdp.media(0).attributes());

    System.err.println(sdp);

  }

  @Test
  public void rtpMapTest()
  {

    final SessionDescription sdp = SessionDescription.parse("v=0\n" +
        "o=- 1111111 1408401718 IN IP4 192.168.1.242\n" +
        "s=-\n" +
        "c=IN IP4 192.168.1.242\n" +
        "t=0 0\n" +
        "m=audio 16028 RTP/AVP 9 8 2 3 18 0 101\n" +
        // "a=rtpmap:9 G722/8000\n" +
        // "a=rtpmap:8 PCMA/8000\n" +
        "a=rtpmap:2 G726-32/8000\n" +
        // "a=rtpmap:18 G729/8000\n" +
        // "a=rtpmap:0 PCMU/8000\n" +
        "a=rtpmap:101 telephone-event/8000\n" +
        "a=fmtp:101 0-15\n" +
        "a=sendrecv\n" +
        "a=ptime:20");

    // verify that the static entries without an rtpmap attribute show up in the output
    sdp.medias().get(0).rtpmap().forEach(Assert::assertNotNull);
  }

  @Test
  public void canProduceEmptySessionDescription()
  {
    assertNotNull(SessionDescription.builder().build());
    assertNotNull(SessionDescription.parse(""));
  }

  @Test
  public void canAddAnAttribute() throws Exception
  {
    SessionDescription sdp = SessionDescription.parse("v=0\n" +
        "o=- 1111111 1408401718 IN IP4 192.168.1.242\n" +
        "s=-\n" +
        "c=IN IP4 192.168.1.242\n" +
        "t=0 0\n" +
        "m=audio 16028 RTP/AVP 9 8 3 18 0\n" +
        "a=sendrecv\n" +
        "a=ptime:20");
    assertEquals(0,
        sdp.attributes().stream().filter((attr) -> "raisin".equals(attr.getKey())).count());

    sdp = sdp.withAttribute(new Attribute("raisin", "raisinValue"));

    assertEquals(1,
        sdp.attributes().stream().filter((attr) -> "raisin".equals(attr.getKey())).count());

    assertEquals(
        sdp.attributes().stream()
        .filter((attr) -> "raisin".equals(attr.getKey()))
        .map(Attribute::getValue)
        .findFirst()
        .get(),
        "raisinValue");

    sdp = SessionDescription.parse("v=0\n" +
        "o=- 4164567 4164567 IN IP4 10.20.150.205\n" +
        "s=-\n" +
        "c=IN IP4 10.20.150.205\n" +
        "t=0 0\n" +
        "m=audio 10174 RTP/AVP 0 9 101\n" +
        "a=rtpmap:0 PCMU/8000\n" +
        "a=rtpmap:9 G722/8000\n" +
        "a=rtpmap:101 telephone-event/8000\n" +
        "a=fmtp:101 0-16\n" +
        "a=sendrecv\n" +
        "a=ptime:20");

    assertEquals(0,
        sdp.attributes().stream().filter((attr) -> "raisin".equals(attr.getKey())).count());

    assertFalse(sdp.toString().contains("a=raisin"));

    sdp = sdp.withAttribute(new Attribute("raisin", "raisinValue"));

    assertEquals(1,
        sdp.attributes().stream().filter((attr) -> "raisin".equals(attr.getKey())).count());

    assertEquals("raisinValue",
        sdp.attributes().stream()
        .filter((attr) -> "raisin".equals(attr.getKey()))
        .map(Attribute::getValue)
        .findFirst()
        .get());
    assertTrue(sdp.toString().contains("a=raisin"));

    sdp = sdp.withoutAttribute("raisin");
    assertEquals(0,
        sdp.attributes().stream().filter((attr) -> "raisin".equals(attr.getKey())).count());

    assertFalse(sdp.toString().contains("a=raisin"));

  }

  @Test
  public void notGoingToWasteMultipleDaysOfDebuggingWithStupidWhitespaceAgain() throws Exception
  {
    final SessionDescription sd = SessionDescription.parse("v=0\n" +
        "o=- 4164567 4164567 IN IP4 10.20.150.205\n" +
        "s=-\n" +
        "c=IN IP4 10.20.150.205\n" +
        "t=0 0\n" +
        "m=audio 10174 RTP/AVP 0 9 101\n" +
        "a=rtpmap:0 PCMU/8000\n" +
        "a=rtpmap:9 G722/8000\n" +
        "c=IN IP4 10.20.150.205\n" +
        "a=rtpmap:101 telephone-event/8000\n" +
        "a=fmtp:101 0-16\n" +
        "a=sendrecv\n" +
        "a=ptime:20");

    final String sdp = sd.toString();

    // No orphan LF
    assertEquals(-1, sdp.replace("\r\n", "CRLF").indexOf("\n"));

    // No orphan CR
    assertEquals(-1, sdp.replace("\r\n", "CRLF").indexOf("\r"));

    // No leading or trailing whitespace (except at the end of the SDP string)
    assertEquals(
        sdp.trim(),
        Lists.newArrayList(
            sdp.split("\r\n")).stream()
        .map(String::trim)
        .collect(Collectors.joining("\r\n")));

    // No more pain. I like to believe.

  }

  @Test
  public void noAttributesNoProblem() throws Exception
  {
    final SessionDescription sdp = SessionDescription.builder()
        .origin(Origin.create("a", InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 })))
        .info(new Info("", "", "", "", ""))
        .connection(Connection.create(InetAddress.getByAddress(new byte[] { 1, 1, 1, 1 })))
        .medias(Lists.newArrayList(
            Media.builder() // no attributes
            .port(1234)
            .type("audio")
            .formats(Lists.newArrayList("0", "9"))
            .protocol("RTP/AVP")
            .build()))
        .build();
    sdp.medias().get(0).attributes();
  }
}
