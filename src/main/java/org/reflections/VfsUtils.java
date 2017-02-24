package org.reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VfsUtils {

	private static final Log logger = LogFactory.getLog(VfsUtils.class);

	private static final String VFS2_PKG = "org.jboss.virtual.";
	private static final String VFS3_PKG = "org.jboss.vfs.";
	private static final String VFS_NAME = "VFS";

	private static enum VFS_VER { V2, V3 }

	private static VFS_VER version;

	private static Method VFS_METHOD_GET_ROOT_URL = null;
	
	static {
		ClassLoader loader = VfsUtils.class.getClassLoader();
		String pkg;
		Class<?> vfsClass;
		
		try {
			vfsClass = loader.loadClass(VFS2_PKG + VFS_NAME);

			version = VFS_VER.V2;
			pkg = VFS2_PKG;

			if (logger.isDebugEnabled())
				logger.debug("JBoss VFS packages for JBoss AS 5 found");
		} catch (ClassNotFoundException ex1) {
			logger.error("JBoss VFS packages (for both JBoss AS 5 and 6) were not found - JBoss VFS support disabled");
			throw new IllegalStateException("Cannot detect JBoss VFS packages", ex1);
		}
		
		String methodName = (VFS_VER.V3.equals(version) ? "getChild" : "getRoot");

		VFS_METHOD_GET_ROOT_URL = findMethod(vfsClass, methodName, URL.class);
	}
	
	public static Object getRoot(URL url) throws IOException {
		return invokeVfsMethod(VFS_METHOD_GET_ROOT_URL, null, url);
	}
	
	protected static Object invokeVfsMethod(Method method, Object target, Object... args) throws IOException {
		try {
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (targetEx instanceof IOException) {
				throw (IOException) targetEx;
			}
		}
		catch (Exception ex) {
		}

		throw new IllegalStateException("Invalid code path reached");
	}
	
	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for (Method method : methods) {
				if (name.equals(method.getName())
						&& (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
	
}
