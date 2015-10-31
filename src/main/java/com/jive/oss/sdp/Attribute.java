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

import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.Value;

@Value
public class Attribute
{

  private String key;
  private String value;

  public Attribute(String key, String value)
  {
    this.key = key;
    this.value = value;
  }

  public Attribute(String key)
  {
    this(key, null);
  }

  public String toString()
  {
    if (value != null)
    {
      return String.format("%s:%s", key, value);
    }
    return key;
  }

  public static Attribute fromString(String value)
  {
    int pos = value.indexOf(':');
    if (pos == -1)
    {
      return new Attribute(value);
    }
    return new Attribute(value.substring(0, pos), value.substring(pos + 1));
  }

  public <K, V, R> R parse(Function<String, K> key, Function<String, V> value, BiFunction<K, V, R> factory)
  {
    return parseValue(this.value, key, value, factory);
  }

  public static <K, V, R> R parseValue(String value, Function<String, K> key, Function<String, V> parser, BiFunction<K, V, R> factory)
  {
    int pos = value.indexOf(' ');
    if (pos == -1)
    {
      return factory.apply(key.apply(value), null);
    }
    return factory.apply(key.apply(value.substring(0, pos)), parser.apply(value.substring(pos + 1)));
  }

}
