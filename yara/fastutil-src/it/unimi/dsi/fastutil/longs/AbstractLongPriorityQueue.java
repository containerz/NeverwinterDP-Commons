/* Generic definitions */
/* Assertions (useful to generate conditional code) */
/* Current type and class (and size, if applicable) */
/* Value methods */
/* Interfaces (keys) */
/* Interfaces (values) */
/* Abstract implementations (keys) */
/* Abstract implementations (values) */
/* Static containers (keys) */
/* Static containers (values) */
/* Implementations */
/* Synchronized wrappers */
/* Unmodifiable wrappers */
/* Other wrappers */
/* Methods (keys) */
/* Methods (values) */
/* Methods (keys/values) */
/* Methods that have special names depending on keys (but the special names depend on values) */
/* Equality */
/* Object/Reference-only definitions (keys) */
/* Primitive-type-only definitions (keys) */
/* Object/Reference-only definitions (values) */
/*		 
 * Copyright (C) 2002-2014 Sebastiano Vigna 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package it.unimi.dsi.fastutil.longs;
import it.unimi.dsi.fastutil.AbstractPriorityQueue;
/**  An abstract class providing basic methods for priority queues implementing a type-specific interface.
 *
 */
public abstract class AbstractLongPriorityQueue extends AbstractPriorityQueue<Long> implements LongPriorityQueue {
 /** Delegates to the corresponding type-specific method. */
 public void enqueue( final Long x ) { enqueue( x.longValue() ); }
 /** Delegates to the corresponding type-specific method. */
 public Long dequeue() { return (Long.valueOf(dequeueLong())); }
 /** Delegates to the corresponding type-specific method. */
 public Long first() { return (Long.valueOf(firstLong())); }
 /** Delegates to the corresponding type-specific method. */
 public Long last() { return (Long.valueOf(lastLong())); }
 /** Throws an {@link UnsupportedOperationException}. */
 public long lastLong() { throw new UnsupportedOperationException(); }
}
