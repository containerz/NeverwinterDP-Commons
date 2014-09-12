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
/* Primitive-type-only definitions (values) */
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
package it.unimi.dsi.fastutil.doubles;
import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.longs.LongListIterator;
/** A type-specific AVL tree map with a fast, small-footprint implementation.
 *
 * <P>The iterators provided by the views of this class are type-specific {@linkplain
 * it.unimi.dsi.fastutil.BidirectionalIterator bidirectional iterators}.
 * Moreover, the iterator returned by <code>iterator()</code> can be safely cast
 * to a type-specific {@linkplain java.util.ListIterator list iterator}.
 */
public class Double2LongAVLTreeMap extends AbstractDouble2LongSortedMap implements java.io.Serializable, Cloneable {
 /** A reference to the root entry. */
 protected transient Entry tree;
 /** Number of entries in this map. */
 protected int count;
 /** The first key in this map. */
 protected transient Entry firstEntry;
 /** The last key in this map. */
 protected transient Entry lastEntry;
 /** Cached set of entries. */
 protected transient volatile ObjectSortedSet<Double2LongMap.Entry > entries;
 /** Cached set of keys. */
 protected transient volatile DoubleSortedSet keys;
 /** Cached collection of values. */
 protected transient volatile LongCollection values;
 /** The value of this variable remembers, after a <code>put()</code> 
	 * or a <code>remove()</code>, whether the <em>domain</em> of the map
	 * has been modified. */
 protected transient boolean modified;
 /** This map's comparator, as provided in the constructor. */
 protected Comparator<? super Double> storedComparator;
 /** This map's actual comparator; it may differ from {@link #storedComparator} because it is
		always a type-specific comparator, so it could be derived from the former by wrapping. */
 protected transient DoubleComparator actualComparator;
 private static final long serialVersionUID = -7046029254386353129L;
 private static final boolean ASSERTS = false;
 {
  allocatePaths();
 }
 /** Creates a new empty tree map. 
	 */
 public Double2LongAVLTreeMap() {
  tree = null;
  count = 0;
 }
 /** Generates the comparator that will be actually used.
	 *
	 * <P>When a specific {@link Comparator} is specified and stored in {@link
	 * #storedComparator}, we must check whether it is type-specific.  If it is
	 * so, we can used directly, and we store it in {@link #actualComparator}. Otherwise,
	 * we generate on-the-fly an anonymous class that wraps the non-specific {@link Comparator}
	 * and makes it into a type-specific one.
	 */
 @SuppressWarnings("unchecked")
 private void setActualComparator() {
  /* If the provided comparator is already type-specific, we use it. Otherwise,
		   we use a wrapper anonymous class to fake that it is type-specific. */
  if ( storedComparator == null || storedComparator instanceof DoubleComparator ) actualComparator = (DoubleComparator)storedComparator;
  else actualComparator = new DoubleComparator () {
    public int compare( double k1, double k2 ) {
     return storedComparator.compare( (Double.valueOf(k1)), (Double.valueOf(k2)) );
    }
    public int compare( Double ok1, Double ok2 ) {
     return storedComparator.compare( ok1, ok2 );
    }
   };
 }
 /** Creates a new empty tree map with the given comparator.
	 *
	 * @param c a (possibly type-specific) comparator.
	 */
 public Double2LongAVLTreeMap( final Comparator<? super Double> c ) {
  this();
  storedComparator = c;
  setActualComparator();
 }
 /** Creates a new tree map copying a given map.
	 *
	 * @param m a {@link Map} to be copied into the new tree map. 
	 */
 public Double2LongAVLTreeMap( final Map<? extends Double, ? extends Long> m ) {
  this();
  putAll( m );
 }
 /** Creates a new tree map copying a given sorted map (and its {@link Comparator}).
	 *
	 * @param m a {@link SortedMap} to be copied into the new tree map. 
	 */
 public Double2LongAVLTreeMap( final SortedMap<Double,Long> m ) {
  this( m.comparator() );
  putAll( m );
 }
 /** Creates a new tree map copying a given map.
	 *
	 * @param m a type-specific map to be copied into the new tree map. 
	 */
 public Double2LongAVLTreeMap( final Double2LongMap m ) {
  this();
  putAll( m );
 }
 /** Creates a new tree map copying a given sorted map (and its {@link Comparator}).
	 *
	 * @param m a type-specific sorted map to be copied into the new tree map. 
	 */
 public Double2LongAVLTreeMap( final Double2LongSortedMap m ) {
  this( m.comparator() );
  putAll( m );
 }
 /** Creates a new tree map using the elements of two parallel arrays and the given comparator.
	 *
	 * @param k the array of keys of the new tree map.
	 * @param v the array of corresponding values in the new tree map.
	 * @param c a (possibly type-specific) comparator.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
 public Double2LongAVLTreeMap( final double[] k, final long v[], final Comparator<? super Double> c ) {
  this( c );
  if ( k.length != v.length ) throw new IllegalArgumentException( "The key array and the value array have different lengths (" + k.length + " and " + v.length + ")" );
  for( int i = 0; i < k.length; i++ ) this.put( k[ i ], v[ i ] );
 }
 /** Creates a new tree map using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new tree map.
	 * @param v the array of corresponding values in the new tree map.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
 public Double2LongAVLTreeMap( final double[] k, final long v[] ) {
  this( k, v, null );
 }
 /*
	 * The following methods implements some basic building blocks used by
	 * all accessors.  They are (and should be maintained) identical to those used in AVLTreeSet.drv.
	 *
	 * The put()/remove() code is derived from Ben Pfaff's GNU libavl
	 * (http://www.msu.edu/~pfaffben/avl/). If you want to understand what's
	 * going on, you should have a look at the literate code contained therein
	 * first.  
	 */
 /** Compares two keys in the right way. 
	 *
	 * <P>This method uses the {@link #actualComparator} if it is non-<code>null</code>.
	 * Otherwise, it resorts to primitive type comparisons or to {@link Comparable#compareTo(Object) compareTo()}.
	 *
	 * @param k1 the first key.
	 * @param k2 the second key.
	 * @return a number smaller than, equal to or greater than 0, as usual
	 * (i.e., when k1 &lt; k2, k1 = k2 or k1 &gt; k2, respectively).
	 */
 @SuppressWarnings("unchecked")
 final int compare( final double k1, final double k2 ) {
  return actualComparator == null ? ( Double.compare((k1),(k2)) ) : actualComparator.compare( k1, k2 );
 }
 /** Returns the entry corresponding to the given key, if it is in the tree; <code>null</code>, otherwise.
	 *
	 * @param k the key to search for.
	 * @return the corresponding entry, or <code>null</code> if no entry with the given key exists.
	 */
 final Entry findKey( final double k ) {
  Entry e = tree;
  int cmp;
  while ( e != null && ( cmp = compare( k, e.key ) ) != 0 ) e = cmp < 0 ? e.left() : e.right();
  return e;
 }
 /** Locates a key.
	 *
	 * @param k a key.
	 * @return the last entry on a search for the given key; this will be
	 * the given key, if it present; otherwise, it will be either the smallest greater key or the greatest smaller key.
	 */
 final Entry locateKey( final double k ) {
  Entry e = tree, last = tree;
  int cmp = 0;
  while ( e != null && ( cmp = compare( k, e.key ) ) != 0 ) {
   last = e;
   e = cmp < 0 ? e.left() : e.right();
  }
  return cmp == 0 ? e : last;
 }
 /** This vector remembers the directions followed during 
	 * the current insertion. It suffices for about 2<sup>32</sup> entries. */
 private transient boolean dirPath[];
 private void allocatePaths() {
  dirPath = new boolean[ 48 ];
 }
 /* After execution of this method, modified is true iff a new entry has
	been inserted. */
 public long put( final double k, final long v ) {
  modified = false;
  if ( tree == null ) { // The case of the empty tree is treated separately.
   count++;
   tree = lastEntry = firstEntry = new Entry ( k, v );
   modified = true;
  }
  else {
   Entry p = tree, q = null, y = tree, z = null, e = null, w = null;
   int cmp, i = 0;
   while( true ) {
    if ( ( cmp = compare( k, p.key ) ) == 0 ) {
     final long oldValue = p.value;
     p.value = v;
     return oldValue;
    }
    if ( p.balance() != 0 ) {
     i = 0;
     z = q;
     y = p;
    }
    if ( dirPath[ i++ ] = cmp > 0 ) {
     if ( p.succ() ) {
      count++;
      e = new Entry ( k, v );
      modified = true;
      if ( p.right == null ) lastEntry = e;
      e.left = p;
      e.right = p.right;
      p.right( e );
      break;
     }
     q = p;
     p = p.right;
    }
    else {
     if ( p.pred() ) {
      count++;
      e = new Entry ( k, v );
      modified = true;
      if ( p.left == null ) firstEntry = e;
      e.right = p;
      e.left = p.left;
      p.left( e );
      break;
     }
     q = p;
     p = p.left;
    }
   }
   p = y;
   i = 0;
   while( p != e ) {
    if ( dirPath[ i ] ) p.incBalance();
    else p.decBalance();
    p = dirPath[ i++ ] ? p.right : p.left;
   }
   if ( y.balance() == -2 ) {
    Entry x = y.left;
    if ( x.balance() == -1 ) {
     w = x;
     if ( x.succ() ) {
      x.succ( false );
      y.pred( x );
     }
     else y.left = x.right;
     x.right = y;
     x.balance( 0 );
     y.balance( 0 );
    }
    else {
     if ( ASSERTS ) assert x.balance() == 1;
     w = x.right;
     x.right = w.left;
     w.left = x;
     y.left = w.right;
     w.right = y;
     if ( w.balance() == -1 ) {
      x.balance( 0 );
      y.balance( 1 );
     }
     else if ( w.balance() == 0 ) {
      x.balance( 0 );
      y.balance( 0 );
     }
     else {
      x.balance( -1 );
      y.balance( 0 );
     }
     w.balance( 0 );
     if ( w.pred() ) {
      x.succ( w );
      w.pred( false );
     }
     if ( w.succ() ) {
      y.pred( w );
      w.succ( false );
     }
    }
   }
   else if ( y.balance() == +2 ) {
    Entry x = y.right;
    if ( x.balance() == 1 ) {
     w = x;
     if ( x.pred() ) {
      x.pred( false );
      y.succ( x );
     }
     else y.right = x.left;
     x.left = y;
     x.balance( 0 );
     y.balance( 0 );
    }
    else {
     if ( ASSERTS ) assert x.balance() == -1;
     w = x.left;
     x.left = w.right;
     w.right = x;
     y.right = w.left;
     w.left = y;
     if ( w.balance() == 1 ) {
      x.balance( 0 );
      y.balance( -1 );
     }
     else if ( w.balance() == 0 ) {
      x.balance( 0 );
      y.balance( 0 );
     }
     else {
      x.balance( 1 );
      y.balance( 0 );
     }
     w.balance( 0 );
     if ( w.pred() ) {
      y.succ( w );
      w.pred( false );
     }
     if ( w.succ() ) {
      x.pred( w );
      w.succ( false );
     }
    }
   }
   else return defRetValue;
   if ( z == null ) tree = w;
   else {
    if ( z.left == y ) z.left = w;
    else z.right = w;
   }
  }
  if ( ASSERTS ) checkTree( tree );
  return defRetValue;
 }
 /** Finds the parent of an entry.
	 *
	 * @param e a node of the tree.
	 * @return the parent of the given node, or <code>null</code> for the root.
	 */
 private Entry parent( final Entry e ) {
  if ( e == tree ) return null;
  Entry x, y, p;
  x = y = e;
  while( true ) {
   if ( y.succ() ) {
    p = y.right;
    if ( p == null || p.left != e ) {
     while( ! x.pred() ) x = x.left;
     p = x.left;
    }
    return p;
   }
   else if ( x.pred() ) {
    p = x.left;
    if ( p == null || p.right != e ) {
     while( ! y.succ() ) y = y.right;
     p = y.right;
    }
    return p;
   }
   x = x.left;
   y = y.right;
  }
 }
 /* After execution of this method, {@link #modified} is true iff an entry
	has been deleted. */
 @SuppressWarnings("unchecked")
 public long remove( final double k ) {
  modified = false;
  if ( tree == null ) return defRetValue;
  int cmp;
  Entry p = tree, q = null;
  boolean dir = false;
  final double kk = k;
  while( true ) {
   if ( ( cmp = compare( kk, p.key ) ) == 0 ) break;
   else if ( dir = cmp > 0 ) {
    q = p;
    if ( ( p = p.right() ) == null ) return defRetValue;
   }
   else {
    q = p;
    if ( ( p = p.left() ) == null ) return defRetValue;
   }
  }
  if ( p.left == null ) firstEntry = p.next();
  if ( p.right == null ) lastEntry = p.prev();
  if ( p.succ() ) {
   if ( p.pred() ) {
    if ( q != null ) {
     if ( dir ) q.succ( p.right );
     else q.pred( p.left );
    }
    else tree = dir ? p.right : p.left;
   }
   else {
    p.prev().right = p.right;
    if ( q != null ) {
     if ( dir ) q.right = p.left;
     else q.left = p.left;
    }
    else tree = p.left;
   }
  }
  else {
   Entry r = p.right;
   if ( r.pred() ) {
    r.left = p.left;
    r.pred( p.pred() );
    if ( ! r.pred() ) r.prev().right = r;
    if ( q != null ) {
     if ( dir ) q.right = r;
     else q.left = r;
    }
    else tree = r;
    r.balance( p.balance() );
    q = r;
    dir = true;
   }
   else {
    Entry s;
    while( true ) {
     s = r.left;
     if ( s.pred() ) break;
     r = s;
    }
    if ( s.succ() ) r.pred( s );
    else r.left = s.right;
    s.left = p.left;
    if ( ! p.pred() ) {
     p.prev().right = s;
     s.pred( false );
    }
    s.right = p.right;
    s.succ( false );
    if ( q != null ) {
     if ( dir ) q.right = s;
     else q.left = s;
    }
    else tree = s;
    s.balance( p.balance() );
    q = r;
    dir = false;
   }
  }
  Entry y;
  while( q != null ) {
   y = q;
   q = parent( y );
   if ( ! dir ) {
    dir = q != null && q.left != y;
    y.incBalance();
    if ( y.balance() == 1 ) break;
    else if ( y.balance() == 2 ) {
     Entry x = y.right;
     if ( ASSERTS ) assert x != null;
     if ( x.balance() == -1 ) {
      Entry w;
      if ( ASSERTS ) assert x.balance() == -1;
      w = x.left;
      x.left = w.right;
      w.right = x;
      y.right = w.left;
      w.left = y;
      if ( w.balance() == 1 ) {
       x.balance( 0 );
       y.balance( -1 );
      }
      else if ( w.balance() == 0 ) {
       x.balance( 0 );
       y.balance( 0 );
      }
      else {
       if ( ASSERTS ) assert w.balance() == -1;
       x.balance( 1 );
       y.balance( 0 );
      }
      w.balance( 0 );
      if ( w.pred() ) {
       y.succ( w );
       w.pred( false );
      }
      if ( w.succ() ) {
       x.pred( w );
       w.succ( false );
      }
      if ( q != null ) {
       if ( dir ) q.right = w;
       else q.left = w;
      }
      else tree = w;
     }
     else {
      if ( q != null ) {
       if ( dir ) q.right = x;
       else q.left = x;
      }
      else tree = x;
      if ( x.balance() == 0 ) {
       y.right = x.left;
       x.left = y;
       x.balance( -1 );
       y.balance( +1 );
       break;
      }
      if ( ASSERTS ) assert x.balance() == 1;
      if ( x.pred() ) {
       y.succ( true );
       x.pred( false );
      }
      else y.right = x.left;
      x.left = y;
      y.balance( 0 );
      x.balance( 0 );
     }
    }
   }
   else {
    dir = q != null && q.left != y;
    y.decBalance();
    if ( y.balance() == -1 ) break;
    else if ( y.balance() == -2 ) {
     Entry x = y.left;
     if ( ASSERTS ) assert x != null;
     if ( x.balance() == 1 ) {
      Entry w;
      if ( ASSERTS ) assert x.balance() == 1;
      w = x.right;
      x.right = w.left;
      w.left = x;
      y.left = w.right;
      w.right = y;
      if ( w.balance() == -1 ) {
       x.balance( 0 );
       y.balance( 1 );
      }
      else if ( w.balance() == 0 ) {
       x.balance( 0 );
       y.balance( 0 );
      }
      else {
       if ( ASSERTS ) assert w.balance() == 1;
       x.balance( -1 );
       y.balance( 0 );
      }
      w.balance( 0 );
      if ( w.pred() ) {
       x.succ( w );
       w.pred( false );
      }
      if ( w.succ() ) {
       y.pred( w );
       w.succ( false );
      }
      if ( q != null ) {
       if ( dir ) q.right = w;
       else q.left = w;
      }
      else tree = w;
     }
     else {
      if ( q != null ) {
       if ( dir ) q.right = x;
       else q.left = x;
      }
      else tree = x;
      if ( x.balance() == 0 ) {
       y.left = x.right;
       x.right = y;
       x.balance( +1 );
       y.balance( -1 );
       break;
      }
      if ( ASSERTS ) assert x.balance() == -1;
      if ( x.succ() ) {
       y.pred( true );
       x.succ( false );
      }
      else y.left = x.right;
      x.right = y;
      y.balance( 0 );
      x.balance( 0 );
     }
    }
   }
  }
  modified = true;
  count--;
  if ( ASSERTS ) checkTree( tree );
  return p.value;
 }
 public Long put( final Double ok, final Long ov ) {
  final long oldValue = put( ((ok).doubleValue()), ((ov).longValue()) );
  return modified ? (null) : (Long.valueOf(oldValue));
 }
 public Long remove( final Object ok ) {
  final long oldValue = remove( ((((Double)(ok)).doubleValue())) );
  return modified ? (Long.valueOf(oldValue)) : (null);
 }
 public boolean containsValue( final long v ) {
  final ValueIterator i = new ValueIterator();
  long ev;
  int j = count;
  while( j-- != 0 ) {
   ev = i.nextLong();
   if ( ( (ev) == (v) ) ) return true;
  }
  return false;
 }
 public void clear() {
  count = 0;
  tree = null;
  entries = null;
  values = null;
  keys = null;
  firstEntry = lastEntry = null;
 }
 /** This class represent an entry in a tree map.
	 *
	 * <P>We use the only "metadata", i.e., {@link Entry#info}, to store
	 * information about balance, predecessor status and successor status.
	 *
	 * <P>Note that since the class is recursive, it can be
	 * considered equivalently a tree.
	 */
 private static final class Entry implements Cloneable, Double2LongMap.Entry {
  /** If the bit in this mask is true, {@link #right} points to a successor. */
  private final static int SUCC_MASK = 1 << 31;
  /** If the bit in this mask is true, {@link #left} points to a predecessor. */
  private final static int PRED_MASK = 1 << 30;
  /** The bits in this mask hold the node balance info. You can get it just by casting to byte. */
  private final static int BALANCE_MASK = 0xFF;
  /** The key of this entry. */
  double key;
  /** The value of this entry. */
  long value;
  /** The pointers to the left and right subtrees. */
  Entry left, right;
  /** This integers holds different information in different bits (see {@link #SUCC_MASK}, {@link #PRED_MASK} and {@link #BALANCE_MASK}). */
  int info;
  Entry() {}
  /** Creates a new entry with the given key and value.
		 *
		 * @param k a key.
		 * @param v a value.
		 */
  Entry( final double k, final long v ) {
   this.key = k;
   this.value = v;
   info = SUCC_MASK | PRED_MASK;
  }
  /** Returns the left subtree. 
		 *
		 * @return the left subtree (<code>null</code> if the left
		 * subtree is empty).
		 */
  Entry left() {
   return ( info & PRED_MASK ) != 0 ? null : left;
  }
  /** Returns the right subtree. 
		 *
		 * @return the right subtree (<code>null</code> if the right
		 * subtree is empty).
		 */
  Entry right() {
   return ( info & SUCC_MASK ) != 0 ? null : right;
  }
  /** Checks whether the left pointer is really a predecessor.
		 * @return true if the left pointer is a predecessor.
		 */
  boolean pred() {
   return ( info & PRED_MASK ) != 0;
  }
  /** Checks whether the right pointer is really a successor.
		 * @return true if the right pointer is a successor.
		 */
  boolean succ() {
   return ( info & SUCC_MASK ) != 0;
  }
  /** Sets whether the left pointer is really a predecessor.
		 * @param pred if true then the left pointer will be considered a predecessor.
		 */
  void pred( final boolean pred ) {
   if ( pred ) info |= PRED_MASK;
   else info &= ~PRED_MASK;
  }
  /** Sets whether the right pointer is really a successor.
		 * @param succ if true then the right pointer will be considered a successor.
		 */
  void succ( final boolean succ ) {
   if ( succ ) info |= SUCC_MASK;
   else info &= ~SUCC_MASK;
  }
  /** Sets the left pointer to a predecessor.
		 * @param pred the predecessr.
		 */
  void pred( final Entry pred ) {
   info |= PRED_MASK;
   left = pred;
  }
  /** Sets the right pointer to a successor.
		 * @param succ the successor.
		 */
  void succ( final Entry succ ) {
   info |= SUCC_MASK;
   right = succ;
  }
  /** Sets the left pointer to the given subtree.
		 * @param left the new left subtree.
		 */
  void left( final Entry left ) {
   info &= ~PRED_MASK;
   this.left = left;
  }
  /** Sets the right pointer to the given subtree.
		 * @param right the new right subtree.
		 */
  void right( final Entry right ) {
   info &= ~SUCC_MASK;
   this.right = right;
  }
  /** Returns the current level of the node.
		 * @return the current level of this node.
		 */
  int balance() {
   return (byte)info;
  }
  /** Sets the level of this node.
		 * @param level the new level of this node.
		 */
  void balance( int level ) {
   info &= ~BALANCE_MASK;
   info |= ( level & BALANCE_MASK );
  }
  /** Increments the level of this node. */
  void incBalance() {
   info = info & ~BALANCE_MASK | ( (byte)info + 1 ) & 0xFF;
  }
  /** Decrements the level of this node. */
  protected void decBalance() {
   info = info & ~BALANCE_MASK | ( (byte)info - 1 ) & 0xFF;
  }
  /** Computes the next entry in the set order.
		 *
		 * @return the next entry (<code>null</code>) if this is the last entry).
		 */
  Entry next() {
   Entry next = this.right;
   if ( ( info & SUCC_MASK ) == 0 ) while ( ( next.info & PRED_MASK ) == 0 ) next = next.left;
   return next;
  }
  /** Computes the previous entry in the set order.
		 *
		 * @return the previous entry (<code>null</code>) if this is the first entry).
		 */
  Entry prev() {
   Entry prev = this.left;
   if ( ( info & PRED_MASK ) == 0 ) while ( ( prev.info & SUCC_MASK ) == 0 ) prev = prev.right;
   return prev;
  }
  public Double getKey() {
   return (Double.valueOf(key));
  }
  public double getDoubleKey() {
   return key;
  }
  public Long getValue() {
   return (Long.valueOf(value));
  }
  public long getLongValue() {
   return value;
  }
  public long setValue(final long value) {
   final long oldValue = this.value;
   this.value = value;
   return oldValue;
  }
  public Long setValue(final Long value) {
   return (Long.valueOf(setValue(((value).longValue()))));
  }
  @SuppressWarnings("unchecked")
  public Entry clone() {
   Entry c;
   try {
    c = (Entry )super.clone();
   }
   catch(CloneNotSupportedException cantHappen) {
    throw new InternalError();
   }
   c.key = key;
   c.value = value;
   c.info = info;
   return c;
  }
  @SuppressWarnings("unchecked")
  public boolean equals( final Object o ) {
   if (!(o instanceof Map.Entry)) return false;
   Map.Entry<Double, Long> e = (Map.Entry<Double, Long>)o;
   return ( Double.doubleToLongBits(key) == Double.doubleToLongBits(((e.getKey()).doubleValue())) ) && ( (value) == (((e.getValue()).longValue())) );
  }
  public int hashCode() {
   return it.unimi.dsi.fastutil.HashCommon.double2int(key) ^ it.unimi.dsi.fastutil.HashCommon.long2int(value);
  }
  public String toString() {
   return key + "=>" + value;
  }
  /*
		public void prettyPrint() {
			prettyPrint(0);
		}

		public void prettyPrint(int level) {
			if ( pred() ) {
				for (int i = 0; i < level; i++)
					System.err.print("  ");
				System.err.println("pred: " + left );
			}
			else if (left != null)
				left.prettyPrint(level +1 );
			for (int i = 0; i < level; i++)
				System.err.print("  ");
			System.err.println(key + "=" + value + " (" + balance() + ")");
			if ( succ() ) {
				for (int i = 0; i < level; i++)
					System.err.print("  ");
				System.err.println("succ: " + right );
			}
			else if (right != null)
				right.prettyPrint(level + 1);
		}
		*/
 }
 /*
	public void prettyPrint() {
		System.err.println("size: " + count);
		if (tree != null) tree.prettyPrint();
	}
	*/
 @SuppressWarnings("unchecked")
 public boolean containsKey( final double k ) {
  return findKey( k ) != null;
 }
 public int size() {
  return count;
 }
 public boolean isEmpty() {
  return count == 0;
 }
 @SuppressWarnings("unchecked")
 public long get( final double k ) {
  final Entry e = findKey( k );
  return e == null ? defRetValue : e.value;
 }
 public double firstDoubleKey() {
  if ( tree == null ) throw new NoSuchElementException();
  return firstEntry.key;
 }
 public double lastDoubleKey() {
  if ( tree == null ) throw new NoSuchElementException();
  return lastEntry.key;
 }
 /** An abstract iterator on the whole range.
	 *
	 * <P>This class can iterate in both directions on a threaded tree.
	 */
 private class TreeIterator {
  /** The entry that will be returned by the next call to {@link java.util.ListIterator#previous()} (or <code>null</code> if no previous entry exists). */
  Entry prev;
  /** The entry that will be returned by the next call to {@link java.util.ListIterator#next()} (or <code>null</code> if no next entry exists). */
  Entry next;
  /** The last entry that was returned (or <code>null</code> if we did not iterate or used {@link #remove()}). */
  Entry curr;
  /** The current index (in the sense of a {@link java.util.ListIterator}). Note that this value is not meaningful when this {@link TreeIterator} has been created using the nonempty constructor.*/
  int index = 0;
  TreeIterator() {
   next = firstEntry;
  }
  TreeIterator( final double k ) {
   if ( ( next = locateKey( k ) ) != null ) {
    if ( compare( next.key, k ) <= 0 ) {
     prev = next;
     next = next.next();
    }
    else prev = next.prev();
   }
  }
  public boolean hasNext() { return next != null; }
  public boolean hasPrevious() { return prev != null; }
  void updateNext() {
   next = next.next();
  }
  Entry nextEntry() {
   if ( ! hasNext() ) throw new NoSuchElementException();
   curr = prev = next;
   index++;
   updateNext();
   return curr;
  }
  void updatePrevious() {
   prev = prev.prev();
  }
  Entry previousEntry() {
   if ( ! hasPrevious() ) throw new NoSuchElementException();
   curr = next = prev;
   index--;
   updatePrevious();
   return curr;
  }
  public int nextIndex() {
   return index;
  }
  public int previousIndex() {
   return index - 1;
  }
  public void remove() {
   if ( curr == null ) throw new IllegalStateException();
   /* If the last operation was a next(), we are removing an entry that preceeds
			   the current index, and thus we must decrement it. */
   if ( curr == prev ) index--;
   next = prev = curr;
   updatePrevious();
   updateNext();
   Double2LongAVLTreeMap.this.remove( curr.key );
   curr = null;
  }
  public int skip( final int n ) {
   int i = n;
   while( i-- != 0 && hasNext() ) nextEntry();
   return n - i - 1;
  }
  public int back( final int n ) {
   int i = n;
   while( i-- != 0 && hasPrevious() ) previousEntry();
   return n - i - 1;
  }
 }
 /** An iterator on the whole range.
	 *
	 * <P>This class can iterate in both directions on a threaded tree.
	 */
 private class EntryIterator extends TreeIterator implements ObjectListIterator<Double2LongMap.Entry > {
  EntryIterator() {}
  EntryIterator( final double k ) {
   super( k );
  }
  public Double2LongMap.Entry next() { return nextEntry(); }
  public Double2LongMap.Entry previous() { return previousEntry(); }
  public void set( Double2LongMap.Entry ok ) { throw new UnsupportedOperationException(); }
  public void add( Double2LongMap.Entry ok ) { throw new UnsupportedOperationException(); }
 }
 public ObjectSortedSet<Double2LongMap.Entry > double2LongEntrySet() {
  if ( entries == null ) entries = new AbstractObjectSortedSet<Double2LongMap.Entry >() {
    final Comparator<? super Double2LongMap.Entry > comparator = new Comparator<Double2LongMap.Entry > () {
     public int compare( final Double2LongMap.Entry x, final Double2LongMap.Entry y ) {
      return Double2LongAVLTreeMap.this.actualComparator.compare( x.getDoubleKey(), y.getDoubleKey() );
     }
    };
    public Comparator<? super Double2LongMap.Entry > comparator() {
     return comparator;
    }
    public ObjectBidirectionalIterator<Double2LongMap.Entry > iterator() {
     return new EntryIterator();
    }
    public ObjectBidirectionalIterator<Double2LongMap.Entry > iterator( final Double2LongMap.Entry from ) {
     return new EntryIterator( from.getDoubleKey() );
    }
    @SuppressWarnings("unchecked")
    public boolean contains( final Object o ) {
     if (!(o instanceof Map.Entry)) return false;
     final Map.Entry <Double, Long> e = (Map.Entry <Double, Long>)o;
     final Entry f = findKey( ((e.getKey()).doubleValue()) );
     return e.equals( f );
    }
    @SuppressWarnings("unchecked")
    public boolean remove( final Object o ) {
     if (!(o instanceof Map.Entry)) return false;
     final Map.Entry <Double, Long> e = (Map.Entry <Double, Long>)o;
     final Entry f = findKey( ((e.getKey()).doubleValue()) );
     if ( f != null ) Double2LongAVLTreeMap.this.remove( f.key );
     return f != null;
    }
    public int size() { return count; }
    public void clear() { Double2LongAVLTreeMap.this.clear(); }
    public Double2LongMap.Entry first() { return firstEntry; }
    public Double2LongMap.Entry last() { return lastEntry; }
    public ObjectSortedSet<Double2LongMap.Entry > subSet( Double2LongMap.Entry from, Double2LongMap.Entry to ) { return subMap( from.getDoubleKey(), to.getDoubleKey() ).double2LongEntrySet(); }
    public ObjectSortedSet<Double2LongMap.Entry > headSet( Double2LongMap.Entry to ) { return headMap( to.getDoubleKey() ).double2LongEntrySet(); }
    public ObjectSortedSet<Double2LongMap.Entry > tailSet( Double2LongMap.Entry from ) { return tailMap( from.getDoubleKey() ).double2LongEntrySet(); }
   };
  return entries;
 }
 /** An iterator on the whole range of keys.
	 *
	 * <P>This class can iterate in both directions on the keys of a threaded tree. We 
	 * simply override the {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and possibly
	 * their type-specific counterparts) so that they return keys instead of entries.
	 */
 private final class KeyIterator extends TreeIterator implements DoubleListIterator {
  public KeyIterator() {}
  public KeyIterator( final double k ) { super( k ); }
  public double nextDouble() { return nextEntry().key; }
  public double previousDouble() { return previousEntry().key; }
  public void set( double k ) { throw new UnsupportedOperationException(); }
  public void add( double k ) { throw new UnsupportedOperationException(); }
  public Double next() { return (Double.valueOf(nextEntry().key)); }
  public Double previous() { return (Double.valueOf(previousEntry().key)); }
  public void set( Double ok ) { throw new UnsupportedOperationException(); }
  public void add( Double ok ) { throw new UnsupportedOperationException(); }
 };
 /** A keyset implementation using a more direct implementation for iterators. */
 private class KeySet extends AbstractDouble2LongSortedMap .KeySet {
  public DoubleBidirectionalIterator iterator() { return new KeyIterator(); }
  public DoubleBidirectionalIterator iterator( final double from ) { return new KeyIterator( from ); }
 }
 /** Returns a type-specific sorted set view of the keys contained in this map.
	 *
	 * <P>In addition to the semantics of {@link java.util.Map#keySet()}, you can
	 * safely cast the set returned by this call to a type-specific sorted
	 * set interface.
	 *
	 * @return a type-specific sorted set view of the keys contained in this map.
	 */
 public DoubleSortedSet keySet() {
  if ( keys == null ) keys = new KeySet();
  return keys;
 }
 /** An iterator on the whole range of values.
	 *
	 * <P>This class can iterate in both directions on the values of a threaded tree. We 
	 * simply override the {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and possibly
	 * their type-specific counterparts) so that they return values instead of entries.
	 */
 private final class ValueIterator extends TreeIterator implements LongListIterator {
  public long nextLong() { return nextEntry().value; }
  public long previousLong() { return previousEntry().value; }
  public void set( long v ) { throw new UnsupportedOperationException(); }
  public void add( long v ) { throw new UnsupportedOperationException(); }
  public Long next() { return (Long.valueOf(nextEntry().value)); }
  public Long previous() { return (Long.valueOf(previousEntry().value)); }
  public void set( Long ok ) { throw new UnsupportedOperationException(); }
  public void add( Long ok ) { throw new UnsupportedOperationException(); }
 };
 /** Returns a type-specific collection view of the values contained in this map.
	 *
	 * <P>In addition to the semantics of {@link java.util.Map#values()}, you can
	 * safely cast the collection returned by this call to a type-specific collection
	 * interface.
	 *
	 * @return a type-specific collection view of the values contained in this map.
	 */
 public LongCollection values() {
  if ( values == null ) values = new AbstractLongCollection () {
    public LongIterator iterator() {
     return new ValueIterator();
    }
    public boolean contains( final long k ) {
     return containsValue( k );
    }
    public int size() {
     return count;
    }
    public void clear() {
     Double2LongAVLTreeMap.this.clear();
    }
   };
  return values;
 }
 public DoubleComparator comparator() {
  return actualComparator;
 }
 public Double2LongSortedMap headMap( double to ) {
  return new Submap( ((double)0), true, to, false );
 }
 public Double2LongSortedMap tailMap( double from ) {
  return new Submap( from, false, ((double)0), true );
 }
 public Double2LongSortedMap subMap( double from, double to ) {
  return new Submap( from, false, to, false );
 }
 /** A submap with given range.
	 *
	 * <P>This class represents a submap. One has to specify the left/right
	 * limits (which can be set to -&infin; or &infin;). Since the submap is a
	 * view on the map, at a given moment it could happen that the limits of
	 * the range are not any longer in the main map. Thus, things such as
	 * {@link java.util.SortedMap#firstKey()} or {@link java.util.Collection#size()} must be always computed
	 * on-the-fly.  
	 */
 private final class Submap extends AbstractDouble2LongSortedMap implements java.io.Serializable {
     private static final long serialVersionUID = -7046029254386353129L;
  /** The start of the submap range, unless {@link #bottom} is true. */
  double from;
  /** The end of the submap range, unless {@link #top} is true. */
  double to;
  /** If true, the submap range starts from -&infin;. */
  boolean bottom;
  /** If true, the submap range goes to &infin;. */
  boolean top;
  /** Cached set of entries. */
  @SuppressWarnings("hiding")
  protected transient volatile ObjectSortedSet<Double2LongMap.Entry > entries;
  /** Cached set of keys. */
  @SuppressWarnings("hiding")
   protected transient volatile DoubleSortedSet keys;
  /** Cached collection of values. */
  @SuppressWarnings("hiding")
  protected transient volatile LongCollection values;
  /** Creates a new submap with given key range.
		 *
		 * @param from the start of the submap range.
		 * @param bottom if true, the first parameter is ignored and the range starts from -&infin;.
		 * @param to the end of the submap range.
		 * @param top if true, the third parameter is ignored and the range goes to &infin;.
		 */
  public Submap( final double from, final boolean bottom, final double to, final boolean top ) {
   if ( ! bottom && ! top && Double2LongAVLTreeMap.this.compare( from, to ) > 0 ) throw new IllegalArgumentException( "Start key (" + from + ") is larger than end key (" + to + ")" );
   this.from = from;
   this.bottom = bottom;
   this.to = to;
   this.top = top;
   this.defRetValue = Double2LongAVLTreeMap.this.defRetValue;
  }
  public void clear() {
   final SubmapIterator i = new SubmapIterator();
   while( i.hasNext() ) {
    i.nextEntry();
    i.remove();
   }
  }
  /** Checks whether a key is in the submap range.
		 * @param k a key.
		 * @return true if is the key is in the submap range.
		 */
  final boolean in( final double k ) {
   return ( bottom || Double2LongAVLTreeMap.this.compare( k, from ) >= 0 ) &&
    ( top || Double2LongAVLTreeMap.this.compare( k, to ) < 0 );
  }
  public ObjectSortedSet<Double2LongMap.Entry > double2LongEntrySet() {
   if ( entries == null ) entries = new AbstractObjectSortedSet<Double2LongMap.Entry >() {
     public ObjectBidirectionalIterator<Double2LongMap.Entry > iterator() {
      return new SubmapEntryIterator();
     }
     public ObjectBidirectionalIterator<Double2LongMap.Entry > iterator( final Double2LongMap.Entry from ) {
      return new SubmapEntryIterator( from.getDoubleKey() );
     }
     public Comparator<? super Double2LongMap.Entry > comparator() { return Double2LongAVLTreeMap.this.entrySet().comparator(); }
     @SuppressWarnings("unchecked")
     public boolean contains( final Object o ) {
      if (!(o instanceof Map.Entry)) return false;
      final Map.Entry <Double, Long> e = (Map.Entry <Double, Long>)o;
      final Double2LongAVLTreeMap.Entry f = findKey( ((e.getKey()).doubleValue()) );
      return f != null && in( f.key ) && e.equals( f );
     }
     @SuppressWarnings("unchecked")
     public boolean remove( final Object o ) {
      if (!(o instanceof Map.Entry)) return false;
      final Map.Entry <Double, Long> e = (Map.Entry <Double, Long>)o;
      final Double2LongAVLTreeMap.Entry f = findKey( ((e.getKey()).doubleValue()) );
      if ( f != null && in( f.key ) ) Submap.this.remove( f.key );
      return f != null;
     }
     public int size() {
      int c = 0;
      for( Iterator<?> i = iterator(); i.hasNext(); i.next() ) c++;
      return c;
     }
     public boolean isEmpty() {
      return ! new SubmapIterator().hasNext();
     }
     public void clear() {
      Submap.this.clear();
     }
     public Double2LongMap.Entry first() { return firstEntry(); }
     public Double2LongMap.Entry last() { return lastEntry(); }
     public ObjectSortedSet<Double2LongMap.Entry > subSet( Double2LongMap.Entry from, Double2LongMap.Entry to ) { return subMap( from.getDoubleKey(), to.getDoubleKey() ).double2LongEntrySet(); }
     public ObjectSortedSet<Double2LongMap.Entry > headSet( Double2LongMap.Entry to ) { return headMap( to.getDoubleKey() ).double2LongEntrySet(); }
     public ObjectSortedSet<Double2LongMap.Entry > tailSet( Double2LongMap.Entry from ) { return tailMap( from.getDoubleKey() ).double2LongEntrySet(); }
    };
   return entries;
  }
  private class KeySet extends AbstractDouble2LongSortedMap .KeySet {
   public DoubleBidirectionalIterator iterator() { return new SubmapKeyIterator(); }
   public DoubleBidirectionalIterator iterator( final double from ) { return new SubmapKeyIterator( from ); }
  }
  public DoubleSortedSet keySet() {
   if ( keys == null ) keys = new KeySet();
   return keys;
  }
  public LongCollection values() {
   if ( values == null ) values = new AbstractLongCollection () {
     public LongIterator iterator() {
      return new SubmapValueIterator();
     }
     public boolean contains( final long k ) {
      return containsValue( k );
     }
     public int size() {
      return Submap.this.size();
     }
     public void clear() {
      Submap.this.clear();
     }
    };
   return values;
  }
  @SuppressWarnings("unchecked")
  public boolean containsKey( final double k ) {
   return in( k ) && Double2LongAVLTreeMap.this.containsKey( k );
  }
  public boolean containsValue( final long v ) {
   final SubmapIterator i = new SubmapIterator();
   long ev;
   while( i.hasNext() ) {
    ev = i.nextEntry().value;
    if ( ( (ev) == (v) ) ) return true;
   }
   return false;
  }
  @SuppressWarnings("unchecked")
  public long get(final double k) {
   final Double2LongAVLTreeMap.Entry e;
   final double kk = k;
   return in( kk ) && ( e = findKey( kk ) ) != null ? e.value : this.defRetValue;
  }
  public long put(final double k, final long v) {
   modified = false;
   if ( ! in( k ) ) throw new IllegalArgumentException( "Key (" + k + ") out of range [" + ( bottom ? "-" : String.valueOf( from ) ) + ", " + ( top ? "-" : String.valueOf( to ) ) + ")" );
   final long oldValue = Double2LongAVLTreeMap.this.put( k, v );
   return modified ? this.defRetValue : oldValue;
  }
  public Long put( final Double ok, final Long ov ) {
   final long oldValue = put( ((ok).doubleValue()), ((ov).longValue()) );
   return modified ? (null) : (Long.valueOf(oldValue));
  }
  @SuppressWarnings("unchecked")
  public long remove( final double k ) {
   modified = false;
   if ( ! in( k ) ) return this.defRetValue;
   final long oldValue = Double2LongAVLTreeMap.this.remove( k );
   return modified ? oldValue : this.defRetValue;
  }
  public Long remove( final Object ok ) {
   final long oldValue = remove( ((((Double)(ok)).doubleValue())) );
   return modified ? (Long.valueOf(oldValue)) : (null);
  }
  public int size() {
   final SubmapIterator i = new SubmapIterator();
   int n = 0;
   while( i.hasNext() ) {
    n++;
    i.nextEntry();
   }
   return n;
  }
  public boolean isEmpty() {
   return ! new SubmapIterator().hasNext();
  }
  public DoubleComparator comparator() {
   return actualComparator;
  }
  public Double2LongSortedMap headMap( final double to ) {
   if ( top ) return new Submap( from, bottom, to, false );
   return compare( to, this.to ) < 0 ? new Submap( from, bottom, to, false ) : this;
  }
  public Double2LongSortedMap tailMap( final double from ) {
   if ( bottom ) return new Submap( from, false, to, top );
   return compare( from, this.from ) > 0 ? new Submap( from, false, to, top ) : this;
  }
  public Double2LongSortedMap subMap( double from, double to ) {
   if ( top && bottom ) return new Submap( from, false, to, false );
   if ( ! top ) to = compare( to, this.to ) < 0 ? to : this.to;
   if ( ! bottom ) from = compare( from, this.from ) > 0 ? from : this.from;
    if ( ! top && ! bottom && from == this.from && to == this.to ) return this;
   return new Submap( from, false, to, false );
  }
  /** Locates the first entry.
		 *
		 * @return the first entry of this submap, or <code>null</code> if the submap is empty.
		 */
  public Double2LongAVLTreeMap.Entry firstEntry() {
   if ( tree == null ) return null;
   // If this submap goes to -infinity, we return the main map first entry; otherwise, we locate the start of the map.
   Double2LongAVLTreeMap.Entry e;
   if ( bottom ) e = firstEntry;
   else {
    e = locateKey( from );
    // If we find either the start or something greater we're OK.
    if ( compare( e.key, from ) < 0 ) e = e.next();
   }
   // Finally, if this subset doesn't go to infinity, we check that the resulting key isn't greater than the end.
   if ( e == null || ! top && compare( e.key, to ) >= 0 ) return null;
   return e;
  }
  /** Locates the last entry.
		 *
		 * @return the last entry of this submap, or <code>null</code> if the submap is empty.
		 */
  public Double2LongAVLTreeMap.Entry lastEntry() {
   if ( tree == null ) return null;
   // If this submap goes to infinity, we return the main map last entry; otherwise, we locate the end of the map.
   Double2LongAVLTreeMap.Entry e;
   if ( top ) e = lastEntry;
   else {
    e = locateKey( to );
    // If we find something smaller than the end we're OK.
    if ( compare( e.key, to ) >= 0 ) e = e.prev();
   }
   // Finally, if this subset doesn't go to -infinity, we check that the resulting key isn't smaller than the start.
   if ( e == null || ! bottom && compare( e.key, from ) < 0 ) return null;
   return e;
  }
  public double firstDoubleKey() {
   Double2LongAVLTreeMap.Entry e = firstEntry();
   if ( e == null ) throw new NoSuchElementException();
   return e.key;
  }
  public double lastDoubleKey() {
   Double2LongAVLTreeMap.Entry e = lastEntry();
   if ( e == null ) throw new NoSuchElementException();
   return e.key;
  }
  public Double firstKey() {
   Double2LongAVLTreeMap.Entry e = firstEntry();
   if ( e == null ) throw new NoSuchElementException();
   return e.getKey();
  }
  public Double lastKey() {
   Double2LongAVLTreeMap.Entry e = lastEntry();
   if ( e == null ) throw new NoSuchElementException();
   return e.getKey();
  }
  /** An iterator for subranges.
		 * 
		 * <P>This class inherits from {@link TreeIterator}, but overrides the methods that
		 * update the pointer after a {@link java.util.ListIterator#next()} or {@link java.util.ListIterator#previous()}. If we would
		 * move out of the range of the submap we just overwrite the next or previous
		 * entry with <code>null</code>.
		 */
  private class SubmapIterator extends TreeIterator {
   SubmapIterator() {
    next = firstEntry();
   }
   SubmapIterator( final double k ) {
    this();
    if ( next != null ) {
     if ( ! bottom && compare( k, next.key ) < 0 ) prev = null;
     else if ( ! top && compare( k, ( prev = lastEntry() ).key ) >= 0 ) next = null;
     else {
      next = locateKey( k );
      if ( compare( next.key, k ) <= 0 ) {
       prev = next;
       next = next.next();
      }
      else prev = next.prev();
     }
    }
   }
   void updatePrevious() {
    prev = prev.prev();
    if ( ! bottom && prev != null && Double2LongAVLTreeMap.this.compare( prev.key, from ) < 0 ) prev = null;
   }
   void updateNext() {
    next = next.next();
    if ( ! top && next != null && Double2LongAVLTreeMap.this.compare( next.key, to ) >= 0 ) next = null;
   }
  }
  private class SubmapEntryIterator extends SubmapIterator implements ObjectListIterator<Double2LongMap.Entry > {
   SubmapEntryIterator() {}
   SubmapEntryIterator( final double k ) {
    super( k );
   }
   public Double2LongMap.Entry next() { return nextEntry(); }
   public Double2LongMap.Entry previous() { return previousEntry(); }
   public void set( Double2LongMap.Entry ok ) { throw new UnsupportedOperationException(); }
   public void add( Double2LongMap.Entry ok ) { throw new UnsupportedOperationException(); }
  }
  /** An iterator on a subrange of keys.
		 *
		 * <P>This class can iterate in both directions on a subrange of the
		 * keys of a threaded tree. We simply override the {@link
		 * java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and possibly their
		 * type-specific counterparts) so that they return keys instead of
		 * entries.
		 */
  private final class SubmapKeyIterator extends SubmapIterator implements DoubleListIterator {
   public SubmapKeyIterator() { super(); }
   public SubmapKeyIterator( double from ) { super( from ); }
   public double nextDouble() { return nextEntry().key; }
   public double previousDouble() { return previousEntry().key; }
   public void set( double k ) { throw new UnsupportedOperationException(); }
   public void add( double k ) { throw new UnsupportedOperationException(); }
   public Double next() { return (Double.valueOf(nextEntry().key)); }
   public Double previous() { return (Double.valueOf(previousEntry().key)); }
   public void set( Double ok ) { throw new UnsupportedOperationException(); }
   public void add( Double ok ) { throw new UnsupportedOperationException(); }
  };
  /** An iterator on a subrange of values.
		 *
		 * <P>This class can iterate in both directions on the values of a
		 * subrange of the keys of a threaded tree. We simply override the
		 * {@link java.util.ListIterator#next()}/{@link java.util.ListIterator#previous()} methods (and possibly their
		 * type-specific counterparts) so that they return values instead of
		 * entries.  
		 */
  private final class SubmapValueIterator extends SubmapIterator implements LongListIterator {
   public long nextLong() { return nextEntry().value; }
   public long previousLong() { return previousEntry().value; }
   public void set( long v ) { throw new UnsupportedOperationException(); }
   public void add( long v ) { throw new UnsupportedOperationException(); }
   public Long next() { return (Long.valueOf(nextEntry().value)); }
   public Long previous() { return (Long.valueOf(previousEntry().value)); }
   public void set( Long ok ) { throw new UnsupportedOperationException(); }
   public void add( Long ok ) { throw new UnsupportedOperationException(); }
  };
 }
 /** Returns a deep copy of this tree map.
	 *
	 * <P>This method performs a deep copy of this tree map; the data stored in the
	 * set, however, is not cloned. Note that this makes a difference only for object keys.
	 *
	 * @return a deep copy of this tree map.
	 */
 @SuppressWarnings("unchecked")
 public Double2LongAVLTreeMap clone() {
  Double2LongAVLTreeMap c;
  try {
   c = (Double2LongAVLTreeMap )super.clone();
  }
  catch(CloneNotSupportedException cantHappen) {
   throw new InternalError();
  }
  c.keys = null;
  c.values = null;
  c.entries = null;
  c.allocatePaths();
  if ( count != 0 ) {
   // Also this apparently unfathomable code is derived from GNU libavl.
   Entry e, p, q, rp = new Entry (), rq = new Entry ();
   p = rp;
   rp.left( tree );
   q = rq;
   rq.pred( null );
   while( true ) {
    if ( ! p.pred() ) {
     e = p.left.clone();
     e.pred( q.left );
     e.succ( q );
     q.left( e );
     p = p.left;
     q = q.left;
    }
    else {
     while( p.succ() ) {
      p = p.right;
      if ( p == null ) {
       q.right = null;
       c.tree = rq.left;
       c.firstEntry = c.tree;
       while( c.firstEntry.left != null ) c.firstEntry = c.firstEntry.left;
       c.lastEntry = c.tree;
       while( c.lastEntry.right != null ) c.lastEntry = c.lastEntry.right;
       return c;
      }
      q = q.right;
     }
     p = p.right;
     q = q.right;
    }
    if ( ! p.succ() ) {
     e = p.right.clone();
     e.succ( q.right );
     e.pred( q );
     q.right( e );
    }
   }
  }
  return c;
 }
 private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
  int n = count;
  EntryIterator i = new EntryIterator();
  Entry e;
  s.defaultWriteObject();
  while(n-- != 0) {
   e = i.nextEntry();
   s.writeDouble( e.key );
   s.writeLong( e.value );
  }
 }
 /** Reads the given number of entries from the input stream, returning the corresponding tree. 
	 *
	 * @param s the input stream.
	 * @param n the (positive) number of entries to read.
	 * @param pred the entry containing the key that preceeds the first key in the tree.
	 * @param succ the entry containing the key that follows the last key in the tree.
	 */
 @SuppressWarnings("unchecked")
 private Entry readTree( final java.io.ObjectInputStream s, final int n, final Entry pred, final Entry succ ) throws java.io.IOException, ClassNotFoundException {
  if ( n == 1 ) {
   final Entry top = new Entry ( s.readDouble(), s.readLong() );
   top.pred( pred );
   top.succ( succ );
   return top;
  }
  if ( n == 2 ) {
   /* We handle separately this case so that recursion will
			 *always* be on nonempty subtrees. */
   final Entry top = new Entry ( s.readDouble(), s.readLong() );
   top.right( new Entry ( s.readDouble(), s.readLong() ) );
   top.right.pred( top );
   top.balance( 1 );
   top.pred( pred );
   top.right.succ( succ );
   return top;
  }
  // The right subtree is the largest one.
  final int rightN = n / 2, leftN = n - rightN - 1;
  final Entry top = new Entry ();
  top.left( readTree( s, leftN, pred, top ) );
  top.key = s.readDouble();
  top.value = s.readLong();
  top.right( readTree( s, rightN, top, succ ) );
  if ( n == ( n & -n ) ) top.balance( 1 ); // Quick test for determining whether n is a power of 2.
  return top;
 }
 private void readObject( java.io.ObjectInputStream s ) throws java.io.IOException, ClassNotFoundException {
  s.defaultReadObject();
  /* The storedComparator is now correctly set, but we must restore
		   on-the-fly the actualComparator. */
  setActualComparator();
  allocatePaths();
  if ( count != 0 ) {
   tree = readTree( s, count, null, null );
   Entry e;
   e = tree;
   while( e.left() != null ) e = e.left();
   firstEntry = e;
   e = tree;
   while( e.right() != null ) e = e.right();
   lastEntry = e;
  }
  if ( ASSERTS ) checkTree( tree );
 }
 @SuppressWarnings("rawtypes")
 private static int checkTree( @SuppressWarnings("unused") Entry e ) { return 0; }
}
