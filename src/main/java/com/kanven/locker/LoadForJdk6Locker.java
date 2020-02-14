package com.kanven.locker;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.CountDownLatch;

/**
 * 类加载器加载类死锁
 * @author kanven
 *
 */
public class LoadForJdk6Locker {

	public static void main(String[] args) throws Exception {
		final CountDownLatch latch = new CountDownLatch(2);
		File file = new File("");
		String path = file.getAbsolutePath();
		final CL1 cl1 = new CL1(new URL[] { new URL("file://" + path + File.separator + "pk1" + File.separator) });
		final CL2 cl2 = new CL2(new URL[] { new URL("file://" + path + File.separator + "pk1" + File.separator) });
		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("before load class A");
					Class<?> clazz = cl1.loadClass("com.kanven.loader.pk1.A");
					System.out.println("A ---> "+clazz.getClassLoader().getClass().getName());
					System.out.println("after load class A");
					latch.countDown();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("before load class C");
					Class<?> clazz = cl2.loadClass("com.kanven.loader.pk2.C");
					System.out.println("C ---> " + clazz.getClassLoader().getClass().getName());
					System.out.println("after load class C");
					latch.countDown();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
			}
		}).start();
		latch.await();
	}
	
	private static class CL1 extends URLClassLoader {
		
		private static ClassLoader loader;

		public CL1(URL[] urls) {
			super(urls);
			loader = this;
		}
		
		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException{
	        if (name.contains("pk2")){
	        	ClassLoader loader = CL2.getClassLoader();
	        	return loader.loadClass(name);
	        }else{
	            return super.loadClass(name);
	        }
	    }
		
		public static ClassLoader getClassLoader(){
			return loader;
		}

	}
	
	private static class CL2 extends URLClassLoader {

		private static ClassLoader loader;

		public CL2(URL[] urls) {
			super(urls);
			loader = this;
		}

		public static ClassLoader getClassLoader() {
			return loader;
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			if (name.contains("pk1")) {
				ClassLoader loader = CL1.getClassLoader();
				return loader.loadClass(name);
			} else {
				return super.loadClass(name);
			}
		}

	}

}
