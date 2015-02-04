package com.intech.cms.utils.sxssfwriter;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomGeneratedCollection implements Collection {

	private int size;

	public RandomGeneratedCollection(int size) {
		this.size = size;
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator iterator() {
		return new Iterator<Object>() {
			int current = 0;
			Random random = new Random();

			public boolean hasNext() {
				return current < size;
			}

			public Object next() {

				if (current++ > size) {
					throw new NoSuchElementException();
				}

				TestBean bean = new TestBean(current, RandomStringUtils.randomAscii(8), RandomStringUtils.randomAlphabetic(10),
						RandomStringUtils.randomAlphabetic(15), RandomStringUtils.randomAlphanumeric(20));
				
				if (random.nextBoolean()){
					bean.getAwards().add(RandomStringUtils.randomAscii(8));
				}
				if (random.nextBoolean()){
					bean.getAwards().add(RandomStringUtils.randomAscii(8));
				}
				if (random.nextBoolean()){
					bean.getAwards().add(RandomStringUtils.randomAscii(8));
				}
				
				return bean; 
			}

			public void remove() {
			}
		};
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] toArray(Object[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean add(Object e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection c) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

}
