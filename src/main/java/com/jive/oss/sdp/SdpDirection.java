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

public enum SdpDirection
{
  SendOnly,
  SendRecv,
  RecvOnly,
  Inactive;

  public SdpDirection reverse()
  {
    switch (this)
    {
      case Inactive:
        return Inactive;
      case RecvOnly:
        return SendOnly;
      case SendOnly:
        return RecvOnly;
      case SendRecv:
        return SendRecv;
    }
    throw new RuntimeException("Invalid SdpDirection value");
  }

  public boolean isSend()
  {
    switch (this)
    {
      case Inactive:
        return false;
      case RecvOnly:
        return false;
      case SendOnly:
        return true;
      case SendRecv:
        return true;
    }
    throw new RuntimeException("Invalid SdpDirection value");
  }

  public boolean isReceive()
  {
    switch (this)
    {
      case Inactive:
        return false;
      case RecvOnly:
        return true;
      case SendOnly:
        return false;
      case SendRecv:
        return true;
    }
    throw new RuntimeException("Invalid SdpDirection value");

  }

}
