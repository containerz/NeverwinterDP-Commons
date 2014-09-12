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
 * Copyright (C) 2003-2014 Paolo Boldi and Sebastiano Vigna 
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
package it.unimi.dsi.fastutil.doubles;
import java.util.NoSuchElementException;
/** A type-specific heap-based priority queue.
 *
 * <P>Instances of this class represent a priority queue using a heap. The heap is enlarged as needed, but
 * it is never shrunk. Use the {@link #trim()} method to reduce its size, if necessary.
 */
public class DoubleHeapPriorityQueue extends AbstractDoublePriorityQueue {
 /** The heap array. */
 @SuppressWarnings("unchecked")
 protected double[] heap = DoubleArrays.EMPTY_ARRAY;
 /** The number of elements in this queue. */
 protected int size;
 /** The type-specific comparator used in this queue. */
 protected DoubleComparator c;
 /** Creates a new empty queue with a given capacity and comparator.
	 *
	 * @param capacity the initial capacity of this queue.
	 * @param c the comparator used in this queue, or <code>null</code> for the natural order.
	 */
 @SuppressWarnings("unchecked")
 public DoubleHeapPriorityQueue( int capacity, DoubleComparator c ) {
  if ( capacity > 0 ) this.heap = new double[ capacity ];
  this.c = c;
 }
 /** Creates a new empty queue with a given capacity and using the natural order.
	 *
	 * @param capacity the initial capacity of this queue.
	 */
 public DoubleHeapPriorityQueue( int capacity ) {
  this( capacity, null );
 }
 /** Creates a new empty queue with a given comparator.
	 *
	 * @param c the comparator used in this queue, or <code>null</code> for the natural order.
	 */
 public DoubleHeapPriorityQueue( DoubleComparator c ) {
  this( 0, c );
 }
 /** Creates a new empty queue using the natural order. 
	 */
 public DoubleHeapPriorityQueue() {
  this( 0, null );
 }
 /** Wraps a given array in a queue using a given comparator.
	 *
	 * <P>The queue returned by this method will be backed by the given array.
	 * The first <code>size</code> element of the array will be rearranged so to form a heap (this is
	 * more efficient than enqueing the elements of <code>a</code> one by one).
	 *
	 * @param a an array.
	 * @param size the number of elements to be included in the queue.
	 * @param c the comparator used in this queue, or <code>null</code> for the natural order.
	 */
 public DoubleHeapPriorityQueue( final double[] a, int size, final DoubleComparator c ) {
  this( c );
  this.heap = a;
  this.size = size;
  DoubleHeaps.makeHeap( a, size, c );
 }
 /** Wraps a given array in a queue using a given comparator.
	 *
	 * <P>The queue returned by this method will be backed by the given array.
	 * The elements of the array will be rearranged so to form a heap (this is
	 * more efficient than enqueing the elements of <code>a</code> one by one).
	 *
	 * @param a an array.
	 * @param c the comparator used in this queue, or <code>null</code> for the natural order.
	 */
 public DoubleHeapPriorityQueue( final double[] a, final DoubleComparator c ) {
  this( a, a.length, c );
 }
 /** Wraps a given array in a queue using the natural order.
	 *
	 * <P>The queue returned by this method will be backed by the given array.
	 * The first <code>size</code> element of the array will be rearranged so to form a heap (this is
	 * more efficient than enqueing the elements of <code>a</code> one by one).
	 *
	 * @param a an array.
	 * @param size the number of elements to be included in the queue.
	 */
 public DoubleHeapPriorityQueue( final double[] a, int size ) {
  this( a, size, null );
 }
 /** Wraps a given array in a queue using the natural order.
	 *
	 * <P>The queue returned by this method will be backed by the given array.
	 * The elements of the array will be rearranged so to form a heap (this is
	 * more efficient than enqueing the elements of <code>a</code> one by one).
	 *
	 * @param a an array.
	 */
 public DoubleHeapPriorityQueue( final double[] a ) {
  this( a, a.length );
 }
 @SuppressWarnings("unchecked")
 public void enqueue( double x ) {
  if ( size == heap.length ) heap = DoubleArrays.grow( heap, size + 1 );
  heap[ size++ ] = x;
  DoubleHeaps.upHeap( heap, size, size - 1, c );
 }
 public double dequeueDouble() {
  if ( size == 0 ) throw new NoSuchElementException();
  final double result = heap[ 0 ];
  heap[ 0 ] = heap[ --size ];
  if ( size != 0 ) DoubleHeaps.downHeap( heap, size, 0, c );
  return result;
 }
 public double firstDouble() {
  if ( size == 0 ) throw new NoSuchElementException();
  return heap[ 0 ];
 }
 public void changed() {
  DoubleHeaps.downHeap( heap, size, 0, c );
 }
 public int size() { return size; }
 public void clear() {
  size = 0;
 }
 /** Trims the underlying heap array so that it has exactly {@link #size()} elements.
	 */
 public void trim() {
  heap = DoubleArrays.trim( heap, size );
 }
 public DoubleComparator comparator() { return c; }
}
