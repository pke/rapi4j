/*******************************************************************************
 * Copyright (c) 2009 Neil Bartlett.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Neil Bartlett - initial API and implementation
 ******************************************************************************/
package eu.wwuk.eclipse.extsvcs.core.internal;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

// @ThreadSafe
public class WeakCollection<T> {
	
	// @GuardedBy("this")
	private final Collection<WeakReference<T>> backingCollection = new LinkedList<WeakReference<T>>();
	
	public synchronized void add(T object) {
		backingCollection.add(new WeakReference<T>(object));
	}
	
	public void accept(Visitor<? super T> visitor) throws InvocationTargetException {
		T[] snapshot = createSnapshot();
		for (T obj : snapshot) {
			try {
				visitor.visit(obj);
			} catch (Exception e) {
				throw new InvocationTargetException(e);
			}
		}
	}
	
	private synchronized T[] createSnapshot() {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) new Object[backingCollection.size()];
		int i=0;
		for(Iterator<WeakReference<T>> iter = backingCollection.iterator(); iter.hasNext(); i++) {
			WeakReference<T> ref = iter.next();
			T object = ref.get();
			result[i] = object;
			
			if(object == null) iter.remove();
		}
		return result;
	}

}
