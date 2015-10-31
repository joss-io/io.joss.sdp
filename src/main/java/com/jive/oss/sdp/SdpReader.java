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

public class SdpReader
{

  private final List<String> lines;

  public SdpReader(final String sdp)
  {

    this.lines = new LinkedList<>();

    for (final String line : sdp.split("\r?\n"))
    {
      if (line.length() > 0)
      {
        this.lines.add(line);
      }
    }

  }

  /**
   * returns the next type without removing it. null if there isn't one, or an exception if it's not an SDP line.
   */

  public CharSequence nextType()
  {

    if (this.lines.isEmpty())
    {
      throw new IllegalStateException();
    }

    if (this.lines.get(0).length() == 0)
    {
      throw new IllegalStateException(this.lines.get(0));
    }

    return this.lines.get(0).substring(0, 1);

  }

  /**
   * reads the value of the next line, removing it in the process.
   */

  public CharSequence readValue()
  {
    final String line = this.lines.remove(0);
    return line.substring(2);
  }

  public int remaining()
  {
    return this.lines.size();
  }

  public boolean skip(final String string)
  {
    if (this.nextType().equals(string))
    {
      this.readValue();
      return true;
    }
    return false;
  }

}
