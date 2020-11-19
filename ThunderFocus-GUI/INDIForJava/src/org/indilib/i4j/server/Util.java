package org.indilib.i4j.server;

/*
 * #%L
 * INDI for Java Server Library
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.jboss.jandex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for classpath and url funktions.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class Util {

    /**
     * the classpath jandex index.
     */
    private static IndexView classPathIndex;

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    /**
     * the number of entries in the system classpath (to check if they changed).
     */
    private static int nrOfSystemClasspathEntries;

    /**
     * the scheme for urls to files.
     */
    private static final String SCHEME_FILE = "file";

    /**
     * private accessor for utility class.
     */
    private Util() {
    }

    /**
     * Returns whether the given URI refers to a local file system URI.
     * 
     * @param uri
     *            The URI to check
     * @return <code>true</code> if the URI is a local file system location, and
     *         <code>false</code> otherwise
     */
    public static boolean isFileURI(URI uri) {
        return SCHEME_FILE.equalsIgnoreCase(uri.getScheme());
    }

    /**
     * Returns the URI as a local file, or <code>null</code> if the given URI
     * does not represent a local file.
     * 
     * @param uri
     *            The URI to return the file for
     * @return The local file corresponding to the given URI, or
     *         <code>null</code>
     */
    public static File toFile(URI uri) {
        if (!isFileURI(uri)) {
            return null;
        }
        // assume all illegal characters have been properly encoded, so use URI
        // class to unencode
        return new File(uri.getSchemeSpecificPart());
    }

    /**
     * @return the current classpath jandex index.
     */
    protected static IndexView classPathIndex() {
        URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        if (classPathIndex == null || nrOfSystemClasspathEntries != loader.getURLs().length) {

            try {
                reindex(loader.getURLs(), null);
            } catch (Exception e) {
                throw new IllegalStateException("could not initialize", e);
            }
        }
        return classPathIndex;
    }

    /**
     * extend the classpath with one file or a directory with classes. (and
     * reindex everything.
     * 
     * @param dirOrJar
     *            the directory or jar file
     * @return the index of the jar/directory file.
     */
    protected static IndexView extendClasspath(File dirOrJar) {
        try {
            URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            URL[] urls = sysLoader.getURLs();
            URL udir = dirOrJar.toURI().toURL();
            String udirs = udir.toString();
            for (URL url : urls) {
                if (url.toString().equalsIgnoreCase(udirs)) {
                    Indexer indexer = new Indexer();
                    Result result = JarIndexer.createJarIndex(dirOrJar, indexer, false, false, false);
                    return result.getIndex();
                }
            }
            Class<URLClassLoader> sysClass = URLClassLoader.class;

            Method method = sysClass.getDeclaredMethod("addURL", new Class[]{
                URL.class
            });
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{
                udir
            });
            return reindex(sysLoader.getURLs(), udir);
        } catch (Exception e) {
            throw new IllegalStateException("Could not include jar in the system classpath!", e);
        }
    }

    /**
     * index one file or directory using the specified indexer.
     * 
     * @param file
     *            the file to index.
     * @param indexer
     *            the indexer to use.
     */
    private static void index(File file, Indexer indexer) {
        if (file.isFile() && file.getName().endsWith(".class")) {
            try {
                FileInputStream in = new FileInputStream(file);
                indexer.index(in);
                in.close();
            } catch (Exception e) {
                LOG.error("could not scan " + file.getAbsolutePath());
            }
        } else if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                index(child, indexer);
            }
        }
    }

    /**
     * reindex the list of urls and return the index of the current url.
     * 
     * @param urLs
     *            the list of all urls
     * @param currentUrl
     *            the url for with to return the index
     * @return the index of the current url.
     * @throws Exception
     *             if something seriun went wrong.
     */
    private static IndexView reindex(URL[] urLs, URL currentUrl) throws Exception {
        Index jarIndex = null;
        Indexer indexer = new Indexer();
        List<IndexView> indexes = new ArrayList<>();
        for (URL url : urLs) {
            File file = Util.toFile(url.toURI());
            if (file.isDirectory()) {
                index(file, indexer);
                indexes.add(indexer.complete());
            } else if (file.getName().endsWith("jar")) {
                Result result = JarIndexer.createJarIndex(file, indexer, false, false, false);
                indexes.add(result.getIndex());
                if (currentUrl != null && url.toString().equalsIgnoreCase(currentUrl.toString())) {
                    jarIndex = result.getIndex();
                }
            }
        }
        indexer.complete();
        classPathIndex = CompositeIndex.create(indexes);
        return jarIndex;
    }
}
