/*
 * Copyright 2006-2009 the original author or authors.
 *
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
 *
 */

package eu.wwuk.eclipse.extsvcs.core.internal;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

public class BundleDelegatingClassLoader extends ClassLoader {

	private static final String NULL_STRING = "null";

	/** use degradable logger */
	//private static final Log log = LogUtils.createLogger(BundleDelegatingClassLoader.class);

	private final ClassLoader bridge;

	private final Bundle backingBundle;


	/**
	 * Factory method for creating a class loader over the given bundle.
	 * 
	 * @param aBundle bundle to use for class loading and resource acquisition
	 * @return class loader adapter over the given bundle
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(Bundle aBundle) {
		return createBundleClassLoaderFor(aBundle, null);
	}

	/**
	 * Factory method for creating a class loader over the given bundle and with
	 * a given class loader as fall-back. In case the bundle cannot find a class
	 * or locate a resource, the given class loader will be used as fall back.
	 * 
	 * @param bundle bundle used for class loading and resource acquisition
	 * @param bridge class loader used as fall back in case the bundle cannot
	 *        load a class or find a resource. Can be <code>null</code>
	 * @return class loader adapter over the given bundle and class loader
	 */
	public static BundleDelegatingClassLoader createBundleClassLoaderFor(final Bundle bundle, final ClassLoader bridge) {
		return AccessController.doPrivileged(new PrivilegedAction<BundleDelegatingClassLoader>() {

			public BundleDelegatingClassLoader run() {
				return new BundleDelegatingClassLoader(bundle, bridge);
			}
		});
	}

	/**
	 * Private constructor.
	 * 
	 * Constructs a new <code>BundleDelegatingClassLoader</code> instance.
	 * 
	 * @param bundle
	 * @param bridgeLoader
	 */
	protected BundleDelegatingClassLoader(Bundle bundle, ClassLoader bridgeLoader) {
		super(null);
		this.backingBundle = bundle;
		this.bridge = bridgeLoader;
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			return this.backingBundle.loadClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]",
				cnfe);
		}
		catch (NoClassDefFoundError ncdfe) {
			// This is almost always an error
			// This is caused by a dependent class failure,
			// so make sure we search for the right one.
			String cname = ncdfe.getMessage().replace('/', '.');
			NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle ["
					+ nullSafeNameAndSymName(backingBundle) + "]");
			e.initCause(ncdfe);
			throw e;
		}
	}

	protected URL findResource(String name) {
		URL url = this.backingBundle.getResource(name);
		return url;
	}

	@SuppressWarnings("unchecked")
	protected Enumeration<URL> findResources(String name) throws IOException {
		Enumeration<URL> enm = this.backingBundle.getResources(name);
		return enm;
	}

	public URL getResource(String name) {
		URL resource = findResource(name);
		if (bridge != null && resource == null) {
			resource = bridge.getResource(name);
		}
		return resource;
	}

	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clazz = null;
		try {
			clazz = findClass(name);
		}
		catch (ClassNotFoundException cnfe) {
			if (bridge != null)
				clazz = bridge.loadClass(name);
			else
				throw cnfe;
		}
		if (resolve) {
			resolveClass(clazz);
		}
		return clazz;
	}

	public String toString() {
		return "BundleDelegatingClassLoader for [" + nullSafeNameAndSymName(backingBundle) + "]";
	}

	/**
	 * Returns the bundle to which this class loader delegates calls to.
	 * 
	 * @return the backing bundle
	 */
	public Bundle getBundle() {
		return backingBundle;
	}
	
	/**
	 * Returns the bundle name and symbolic name - useful when logging bundle info.
	 * 
	 * @param bundle OSGi bundle (can be null)
	 * @return the bundle name and symbolic name
	 */
	public static String nullSafeNameAndSymName(Bundle bundle) {
		if (bundle == null)
			return NULL_STRING;

		Dictionary dict = bundle.getHeaders();

		if (dict == null)
			return NULL_STRING;

		StringBuilder buf = new StringBuilder();
		String name = (String) dict.get(org.osgi.framework.Constants.BUNDLE_NAME);
		if (name == null)
			buf.append(NULL_STRING);
		else
			buf.append(name);
		buf.append(" (");
		String sname = (String) dict.get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);

		if (sname == null)
			buf.append(NULL_STRING);
		else
			buf.append(sname);

		buf.append(")");

		return buf.toString();
	}
}