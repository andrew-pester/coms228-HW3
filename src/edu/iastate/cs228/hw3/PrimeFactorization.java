package edu.iastate.cs228.hw3;

/**
 *  
 * @author Andrew Pester
 *
 */

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class PrimeFactorization implements Iterable<PrimeFactor> {
	private static final long OVERFLOW = -1;
	private long value; // the factored integer
						// it is set to OVERFLOW when the number is greater than 2^63-1, the
						// largest number representable by the type long.

	/**
	 * Reference to dummy node at the head.
	 */
	private Node head;

	/**
	 * Reference to dummy node at the tail.
	 */
	private Node tail;

	private int size; // number of distinct prime factors

	// ------------
	// Constructors
	// ------------

	/**
	 * Default constructor constructs an empty list to represent the number 1.
	 * 
	 * Combined with the add() method, it can be used to create a prime
	 * factorization.
	 */
	public PrimeFactorization() {

		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		size = 0;
	}

	/**
	 * Obtains the prime factorization of n and creates a doubly linked list to
	 * store the result. Follows the direct search factorization algorithm in
	 * Section 1.2 of the project description.
	 * 
	 * @param n
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization(long n) throws IllegalArgumentException {

		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		if (n < 1) {
			throw new IllegalArgumentException();
		}
		for (int i = 2; i <= n; i++) {
			while (n % i == 0) {
				add(i, 1);
				n /= i;
			}
		}
		value = value();
	}

	/**
	 * Copy constructor. It is unnecessary to verify the primality of the numbers in
	 * the list.
	 * 
	 * @param pf
	 */
	public PrimeFactorization(PrimeFactorization pf) {

		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		PrimeFactorizationIterator I = pf.iterator();
		while (I.hasNext()) {
			PrimeFactor tmp = I.next();
			add(tmp.prime, tmp.multiplicity);
		}
		value = value();
	}

	/**
	 * Constructs a factorization from an array of prime factors. Useful when the
	 * number is too large to be represented even as a long integer.
	 * 
	 * @param pflist
	 */
	public PrimeFactorization(PrimeFactor[] pfList) {

		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		PrimeFactorizationIterator I = this.iterator();
		for (int i = 0; i < pfList.length; i++) {
			I.add(pfList[i]);
		}

	}

	// --------------
	// Primality Test
	// --------------

	/**
	 * Test if a number is a prime or not. Check iteratively from 2 to the largest
	 * integer not exceeding the square root of n to see if it divides n.
	 * 
	 * @param n
	 * @return true if n is a prime false otherwise
	 */
	public static boolean isPrime(long n) {

		if (n == 1) {
			return false;
		}
		for (int i = 2; i < n; i++) {
			for (int j = 0; i * j <= n; j++) {
				if (i * j == n) {
					return false;
				}
			}
		}
		return true;
	}

	// ---------------------------
	// Multiplication and Division
	// ---------------------------

	/**
	 * Multiplies the integer v represented by this object with another number n.
	 * Note that v may be too large (in which case this.value == OVERFLOW). You can
	 * do this in one loop: Factor n and traverse the doubly linked list
	 * simultaneously. For details refer to Section 3.1 in the project description.
	 * Store the prime factorization of the product. Update value and size.
	 * 
	 * @param n
	 * @throws IllegalArgumentException if n < 1
	 */
	public void multiply(long n) throws IllegalArgumentException {

		if (n < 1) {
			throw new IllegalArgumentException();
		}
		for (int i = 2; i <= n; i++) {
			while (n % i == 0) {
				add(i, 1);
				n /= i;
			}
		}

	}

	/**
	 * Multiplies the represented integer v with another number in the factorization
	 * form. Traverse both linked lists and store the result in this list object.
	 * See Section 3.1 in the project description for details of algorithm.
	 * 
	 * @param pf
	 */
	public void multiply(PrimeFactorization pf) {

		PrimeFactorizationIterator I = pf.iterator();
		while (I.hasNext()) {
			Node tmp = new Node(I.next());
			add(tmp.pFactor.prime, tmp.pFactor.multiplicity);
		}

	}

	/**
	 * Multiplies the integers represented by two PrimeFactorization objects.
	 * 
	 * @param pf1
	 * @param pf2
	 * @return object of PrimeFactorization to represent the product
	 */
	public static PrimeFactorization multiply(PrimeFactorization pf1, PrimeFactorization pf2) {

		PrimeFactorizationIterator i = pf1.iterator();
		PrimeFactorizationIterator j = pf2.iterator();
		PrimeFactorization ret = new PrimeFactorization();
		while (i.hasNext()) {
			PrimeFactor p1 = i.next();
			ret.add(p1.prime, p1.multiplicity);
		}
		while (j.hasNext()) {
			PrimeFactor p2 = j.next();
			ret.add(p2.prime, p2.multiplicity);
		}

		return ret;
	}

	/**
	 * Divides the represented integer v by n. Make updates to the list, value, size
	 * if divisible. No update otherwise. Refer to Section 3.2 in the project
	 * description for details.
	 * 
	 * @param n
	 * @return true if divisible false if not divisible
	 * @throws IllegalArgumentException if n <= 0
	 */
	public boolean dividedBy(long n) throws IllegalArgumentException {

		if (n <= 0) {
			throw new IllegalArgumentException();
		}
		if (value() % n == 0) {
			PrimeFactorization copy = new PrimeFactorization(value() / n);
			clearList();
			PrimeFactorizationIterator I = copy.iterator();
			for (int i = 0; i < copy.size; i++) {
				PrimeFactor p = I.next();
				add(p.prime, p.multiplicity);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Division where the divisor is represented in the factorization form. Update
	 * the linked list of this object accordingly by removing those nodes housing
	 * prime factors that disappear after the division. No update if this number is
	 * not divisible by pf. Algorithm details are given in Section 3.2.
	 * 
	 * @param pf
	 * @return true if divisible by pf false otherwise
	 */
	public boolean dividedBy(PrimeFactorization pf) {

		if (this.value() != -1 && pf.value() != -1 && this.value() < pf.value) {
			return false;
		}
		if (this.value() != -1 && pf.value() == -1) {
			return false;
		}
		if (this.value() % pf.value() == 0) {
			PrimeFactorization copy = new PrimeFactorization(value() / pf.value());
			clearList();
			PrimeFactorizationIterator I = copy.iterator();
			while (I.hasNext()) {
				PrimeFactor tmp = I.next();
				add(tmp.prime, tmp.multiplicity);
			}
			return true;
		}
		return false;
	}

	/**
	 * Divide the integer represented by the object pf1 by that represented by the
	 * object pf2. Return a new object representing the quotient if divisible. Do
	 * not make changes to pf1 and pf2. No update if the first number is not
	 * divisible by the second one.
	 * 
	 * @param pf1
	 * @param pf2
	 * @return quotient as a new PrimeFactorization object if divisible null
	 *         otherwise
	 */
	public static PrimeFactorization dividedBy(PrimeFactorization pf1, PrimeFactorization pf2) {

		if (pf2.value() == 0) {
			return pf1;
		}
		if (pf1.value() % pf2.value() == 0) {
			PrimeFactorization copy = new PrimeFactorization(pf1.value() / pf2.value());
			return copy;
		}
		return pf1;
	}

	// -----------------------
	// Greatest Common Divisor
	// -----------------------

	/**
	 * Computes the greatest common divisor (gcd) of the represented integer v and
	 * an input integer n. Returns the result as a PrimeFactor object. Calls the
	 * method Euclidean() if this.value != OVERFLOW.
	 * 
	 * It is more efficient to factorize the gcd than n, which can be much greater.
	 * 
	 * @param n
	 * @return prime factorization of gcd
	 * @throws IllegalArgumentException if n < 1
	 */
	public PrimeFactorization gcd(long n) throws IllegalArgumentException {

		if (n < 1) {
			throw new IllegalArgumentException();
		}
		if (this.value() != OVERFLOW) {
			PrimeFactorization ret = new PrimeFactorization(Euclidean(value(), n));
			return ret;
		}
		return null;
		// only does this if the value is == OVERFLOW wasn't specified what you wanted
		// returned in this instance.
	}

	/**
	 * Implements the Euclidean algorithm to compute the gcd of two natural numbers
	 * m and n. The algorithm is described in Section 4.1 of the project
	 * description.
	 * 
	 * @param m
	 * @param n
	 * @return gcd of m and n.
	 * @throws IllegalArgumentException if m < 1 or n < 1
	 */
	public static long Euclidean(long m, long n) throws IllegalArgumentException {

		if (m < 1 || n < 1) {
			throw new IllegalArgumentException();
		}
		long remainder;
		while (m % n != 0) {
			remainder = m % n;
			m = n;
			n = remainder;

		}

		return n;
	}

	/**
	 * Computes the gcd of the values represented by this object and pf by
	 * traversing the two lists. No direct computation involving value and pf.value.
	 * Refer to Section 4.2 in the project description on how to proceed.
	 * 
	 * @param pf
	 * @return prime factorization of the gcd
	 */
	public PrimeFactorization gcd(PrimeFactorization pf) {

		PrimeFactorizationIterator I = iterator();
		PrimeFactorizationIterator J = pf.iterator();
		PrimeFactorization ret = new PrimeFactorization();
		for (int i = 0; i < size; i++) {
			PrimeFactor tmp1 = I.next();
			for (int j = 0; j < pf.size; j++) {
				PrimeFactor tmp2 = J.next();
				if (tmp1.prime == tmp2.prime) {
					ret.add(tmp1.prime, Math.min(tmp1.multiplicity, tmp2.multiplicity));
				}
			}
		}
		return ret;
	}

	/**
	 * 
	 * @param pf1
	 * @param pf2
	 * @return prime factorization of the gcd of two numbers represented by pf1 and
	 *         pf2
	 */
	public static PrimeFactorization gcd(PrimeFactorization pf1, PrimeFactorization pf2) {

		PrimeFactorization ret = pf1.gcd(pf2);
		return ret;
	}

	// ------------
	// List Methods
	// ------------

	/**
	 * Traverses the list to determine if p is a prime factor.
	 * 
	 * Precondition: p is a prime.
	 * 
	 * @param p
	 * @return true if p is a prime factor of the number v represented by this
	 *         linked list false otherwise
	 * @throws IllegalArgumentException if p is not a prime
	 */
	public boolean containsPrimeFactor(int p) throws IllegalArgumentException {

		PrimeFactorizationIterator I = new PrimeFactorizationIterator();
		if (!isPrime(p)) {
			throw new IllegalArgumentException();
		}
		while (I.hasNext()) {
			PrimeFactor tmp = I.next();
			if (tmp.prime == p) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a prime factor p of multiplicity m. Search for p in the linked list. If
	 * p is found at a node N, add m to N.multiplicity. Otherwise, create a new node
	 * to store p and m.
	 * 
	 * Precondition: p is a prime.
	 * 
	 * @param p prime
	 * @param m multiplicity
	 * @return true if m >= 1 false if m < 1
	 */
	public boolean add(int p, int m) {

		PrimeFactorizationIterator I = iterator();
		PrimeFactor pf = new PrimeFactor(p, m);
		if (m >= 1) {
			while (I.hasNext()) {
				PrimeFactor tmp = I.next();
				if (tmp.prime == p) {

					I.previous().multiplicity = tmp.multiplicity + m;
					return true;
				}
				if (tmp.prime > p) {
					I.previous();
					I.add(pf);
					return true;
				}

			}
			if (!I.hasNext()) {
				I.add(pf);
				return true;
			}
			if (size == 0) {
				I.add(pf);
				return true;
			}
			return true;
		}
		if (m < 1) {
			return false;
		}
		// to please the compiler
		return false;
	}

	/**
	 * Removes m from the multiplicity of a prime p on the linked list. It starts by
	 * searching for p. Returns false if p is not found, and true if p is found. In
	 * the latter case, let N be the node that stores p. If N.multiplicity > m,
	 * subtracts m from N.multiplicity. If N.multiplicity <= m, removes the node N.
	 * 
	 * Precondition: p is a prime.
	 * 
	 * @param p
	 * @param m
	 * @return true when p is found. false when p is not found.
	 * @throws IllegalArgumentException if m < 1
	 */
	public boolean remove(int p, int m) throws IllegalArgumentException {

		PrimeFactorizationIterator I = new PrimeFactorizationIterator();
		if (m < 1) {
			throw new IllegalArgumentException();
		}
		while (I.hasNext()) {

			PrimeFactor tmp = I.next();
			if (tmp.prime == p) {
				if (tmp.multiplicity > m) {
					I.previous().multiplicity = tmp.multiplicity - m;
					return true;
				} else if (tmp.multiplicity <= m) {
					I.remove();
					size--;
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @return size of the list
	 */
	public int size() {

		return size;
	}

	/**
	 * Writes out the list as a factorization in the form of a product. Represents
	 * exponentiation by a caret. For example, if the number is 5814, the returned
	 * string would be printed out as "2 * 3^2 * 17 * 19".
	 */
	@Override
	public String toString() {

		if (size == 0) {
			return "1";
		}
		PrimeFactorizationIterator p = new PrimeFactorizationIterator();
		String ret = p.next().toString();

		for (int i = 0; i < size - 1; i++) {
			ret += " * " + p.next().toString();
		}
		return ret;
	}

	/**
	 * @return true if this PrimeFactorization is representing a value that is too
	 *         large to be within long's range. e.g. 999^999. false otherwise.
	 */
	public boolean valueOverflow() {

		return value == OVERFLOW;
	}

	/**
	 * @return value represented by this PrimeFactorization, or -1 if
	 *         valueOverflow()
	 */
	public long value() {

		updateValue();
		return value;
	}

	/**
	 * constructs an array containing all the PrimeFactors of this
	 * PrimeFactorization
	 * 
	 * @return an array of Prime Factors
	 */
	public PrimeFactor[] toArray() {

		PrimeFactor[] arr = new PrimeFactor[size];
		int i = 0;
		for (PrimeFactor pf : this)
			arr[i++] = pf;
		return arr;
	}

	/**
	 * constructs a new iterator for the PrimeFactorization that calls this
	 */
	@Override
	public PrimeFactorizationIterator iterator() {

		return new PrimeFactorizationIterator();
	}

	/**
	 * Doubly-linked node type for this class.
	 */
	private class Node {
		public PrimeFactor pFactor; // prime factor
		public Node next;
		public Node previous;

		/**
		 * Default constructor for creating a dummy node.
		 */
		public Node() {

			pFactor = null;
		}

		/**
		 * Precondition: p is a prime
		 * 
		 * @param p prime number
		 * @param m multiplicity
		 * @throws IllegalArgumentException if m < 1
		 */
		public Node(int p, int m) throws IllegalArgumentException {

			if (m < 1) {
				throw new IllegalArgumentException();
			}
			pFactor = new PrimeFactor(p, m);
		}

		/**
		 * Constructs a node over a provided PrimeFactor object.
		 * 
		 * @param pf
		 * @throws IllegalArgumentException
		 */
		public Node(PrimeFactor pf) {

			if (!isPrime(pf.prime)) {
				throw new IllegalArgumentException();
			}
			pFactor = pf;
		}

		/**
		 * Printed out in the form: prime + "^" + multiplicity. For instance "2^3".
		 * Also, deal with the case pFactor == null in which a string "dummy" is
		 * returned instead.
		 */
		@Override
		public String toString() {

			if (pFactor == null) {
				return "dummy";
			} else {
				return pFactor.toString();
			}
		}
	}

	private class PrimeFactorizationIterator implements ListIterator<PrimeFactor> {
		// Class invariants:
		// 1) logical cursor position is always between cursor.previous and cursor
		// 2) after a call to next(), cursor.previous refers to the node just returned
		// 3) after a call to previous() cursor refers to the node just returned
		// 4) index is always the logical index of node pointed to by cursor
		private static final int FORWARD = -1;
		private static final int BACKWARD = 1;
		private static final int NONE = 0;
		private Node cursor = head.next;
		private Node pending = null; // node pending for removal
		private int index = 0;
		private int direction;

		/**
		 * Default constructor positions the cursor before the smallest prime factor.
		 */
		public PrimeFactorizationIterator() {

		}

		/**
		 * @return true if there is next node else false
		 */
		@Override
		public boolean hasNext() {

			return index < size;
		}

		/**
		 * @return true if there is previous node else false
		 */
		@Override
		public boolean hasPrevious() {

			return index > 0;
		}

		/**
		 * moves the cursor forward an index
		 * 
		 * @return the primeFactor the cursor passed over
		 */
		@Override
		public PrimeFactor next() {

			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			PrimeFactor ret = cursor.pFactor;
			pending = cursor;
			cursor = cursor.next;
			index++;
			direction = FORWARD;
			return ret;
		}

		/**
		 * moved the cursor back an index
		 * 
		 * @return the primeFactor the cursor passed over
		 */
		@Override
		public PrimeFactor previous() {

			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			pending = cursor;
			cursor = cursor.previous;
			index--;
			direction = BACKWARD;
			return cursor.pFactor;
		}

		/**
		 * Removes the prime factor returned by next() or previous()
		 * 
		 * @throws IllegalStateException if pending == null
		 */
		@Override
		public void remove() throws IllegalStateException {

			if (pending == null) {
				throw new IllegalStateException();
			} else {
				if (direction == BACKWARD) {
					Node n = cursor.next;
					unlink(cursor);
					cursor = n;
				} else {
					unlink(cursor.previous);
					index--;
				}
			}
		}

		/**
		 * Adds a prime factor at the cursor position. The cursor is at a wrong position
		 * in either of the two situations below:
		 * 
		 * a) pf.prime < cursor.previous.pFactor.prime if cursor.previous != head. b)
		 * pf.prime > cursor.pFactor.prime if cursor != tail.
		 * 
		 * Take into account the possibility that pf.prime == cursor.pFactor.prime.
		 * 
		 * Precondition: pf.prime is a prime.
		 * 
		 * @param pf
		 * @throws IllegalArgumentException if the cursor is at a wrong position.
		 */
		@Override
		public void add(PrimeFactor pf) throws IllegalArgumentException {

			if (cursor.previous != head && pf.prime < cursor.previous.pFactor.prime) {
				throw new IllegalArgumentException();
			}
			if (cursor != tail && pf.prime > cursor.pFactor.prime) {
				throw new IllegalArgumentException();
			}
			if (size == 0) {
				Node temp = new Node(pf);
				head.next = temp;
				temp.previous = head;
				temp.next = tail;
				tail.previous = temp;
				index++;
				size++;
				direction = NONE;
			} else {
				Node temp = new Node(pf);
				link(cursor.previous, temp);

				index++;
				size++;
				direction = NONE;
			}
		}

		/**
		 * @return the next index
		 */
		@Override
		public int nextIndex() {

			return index;
		}

		/**
		 * @return the previous index
		 */
		@Override
		public int previousIndex() {

			return index - 1;
		}
		@Deprecated
		@Override
		public void set(PrimeFactor pf) {
			throw new UnsupportedOperationException(getClass().getSimpleName() + " does not support set method");
		}
	}

	// --------------
	// Helper methods
	// --------------

	/**
	 * Inserts toAdd into the list after current without updating size.
	 * 
	 * Precondition: current != null, toAdd != null
	 */
	private void link(Node current, Node toAdd) {

		toAdd.previous = current;
		toAdd.next = current.next;
		current.next.previous = toAdd;
		current.next = toAdd;
	}

	/**
	 * Removes toRemove from the list without updating size.
	 */
	private void unlink(Node toRemove) {

		toRemove.previous.next = toRemove.next;
		toRemove.next.previous = toRemove.previous;
	}

	/**
	 * Remove all the nodes in the linked list except the two dummy nodes.
	 * 
	 * Made public for testing purpose. Ought to be private otherwise.
	 */
	public void clearList() {

		PrimeFactorizationIterator I = this.iterator();
		for (int i = 0; i < size; i++) {
			unlink(I.cursor);
			I.next();
		}
		size = 0;

	}

	/**
	 * Multiply the prime factors (with multiplicities) out to obtain the
	 * represented integer. Use Math.multiply(). If an exception is throw, assign
	 * OVERFLOW to the instance variable value. Otherwise, assign the multiplication
	 * result to the variable.
	 * 
	 */
	private void updateValue() {

		try {
			PrimeFactorizationIterator p = iterator();
			value = 1;
			for (int i = 0; i < size; i++) {
				PrimeFactor tmp = p.next();
				for (int j = 0; j < tmp.multiplicity; j++) {

					value = Math.multiplyExact(value, (long) tmp.prime);
				}
			}

		}

		catch (ArithmeticException e) {
			value = OVERFLOW;
		}

	}
}
