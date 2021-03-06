/* Copyright (c) 2001-2004, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG, 
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * A collection of static file management methods.
 *
 * @author fredt@users
 * @author boucherb@users
 */
public class FileUtil {

    // a new File("...")'s path is not canonicalized, only resolved
    // and normalized (e.g. redundant separator chars removed),
    // so as of JDK 1.4.2, this is a valid test for case insensitivity,
    // at least when it is assumed that we are dealing with a configuration
    // that only needs to consider the host platform's native file system,
    // even if, unlike for File.getCanonicalPath(), (new File("a")).exists() or
    // (new File("A")).exits(), regardless of the hosting system's
    // file path case sensitivity policy.
    public static final boolean fsIsIgnoreCase =
        (new File("A")).equals(new File("a"));

    // posix separator normalized to File.separator?
    // CHECKME: is this true for every file system under Java?
    public static final boolean fsNormalizesPosixSeparator =
        (new File("/")).getPath().endsWith(File.separator);

    // only available in JDK 1.2 or better
    static final Method deleteOnExitMethod = getDeleteOnExitMethod();

    // for JDK 1.1 createTempFile
    static final Random random = new Random(System.currentTimeMillis());

    // retrieve the method, or null of not available
    private static Method getDeleteOnExitMethod() {

        try {
            return File.class.getMethod("deleteOnExit", new Class[0]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Delete the named file
     */
    public static void delete(String filename) throws IOException {

        try {
            (new File(filename)).delete();
        } catch (Throwable e) {
            throw toIOException(e);
        }
    }

    /**
     * Requests, in a JDK 1.1 compliant way, that the file or directory denoted
     * by the given abstract pathname be deleted when the virtual machine
     * terminates. <p>
     *
     * Deletion will be attempted only for JDK 1.2 and greater runtime
     * environments and only upon normal termination of the virtual
     * machine, as defined by the Java Language Specification. <p>
     *
     * Once deletion has been sucessfully requested, it is not possible to
     * cancel the request. This method should therefore be used with care. <p>
     *
     * @param f the abstract pathname of the file be deleted when the virtual
     *       machine terminates
     */
    public static void deleteOnExit(File f) {

        if (deleteOnExitMethod == null) {

            // do nothing
        } else {
            try {
                deleteOnExitMethod.invoke(f, new Object[0]);
            } catch (Exception e) {}
        }
    }

    /**
     * Return true or false based on whether the named file exists.
     */
    static public boolean exists(String filename) throws IOException {

        try {
            return (new File(filename)).exists();
        } catch (Throwable e) {
            throw toIOException(e);
        }
    }

    /**
     * Rename the file with oldname to newname. If a file with oldname does not
     * exist, nothing occurs. If a file with newname already exists, it is
     * deleted it before the renaming operation proceeds.
     */
    static public void renameOverwrite(String oldname,
                                       String newname) throws IOException {

        try {
            if (exists(oldname)) {
                delete(newname);

                File file = new File(oldname);

                file.renameTo(new File(newname));
            }
        } catch (Throwable e) {
            throw toIOException(e);
        }
    }

    static IOException toIOException(Throwable e) {

        if (e instanceof IOException) {
            return (IOException) e;
        } else {
            return new IOException(e.getMessage());
        }
    }

    /**
     * Retrieves the absolute path, given some path specification.
     *
     * @param path the path for which to retrieve the absolute path
     * @return the absolute path
     */
    public static String absolutePath(String path) {
        return (new File(path)).getAbsolutePath();
    }

    /**
     * Retrieves the canonical file for the given file, in a
     * JDK 1.1 complaint way.
     *
     * @param f the File for which to retrieve the absolute File
     * @return the canonical File
     */
    public static File canonicalFile(File f) throws IOException {
        return new File(f.getCanonicalPath());
    }

    /**
     * Retrieves the canonical file for the given path, in a
     * JDK 1.1 complaint way.
     *
     * @param path the path for which to retrieve the canonical File
     * @return the canonical File
     */
    public static File canonicalFile(String path) throws IOException {
        return new File(new File(path).getCanonicalPath());
    }

    /**
     * Retrieves the canonical path for the given File, in a
     * JDK 1.1 complaint way.
     *
     * @param f the File for which to retrieve the canonical path
     * @return the canonical path
     */
    public static String canonicalPath(File f) throws IOException {
        return f.getCanonicalPath();
    }

    /**
     * Retrieves the canonical path for the given path, in a
     * JDK 1.1 complaint way.
     *
     * @param path the path for which to retrieve the canonical path
     * @return the canonical path
     */
    public static String canonicalPath(String path) throws IOException {
        return new File(path).getCanonicalPath();
    }

    /**
     * Retrieves the canonical path for the given path, or the absolute
     * path if attemting to retrieve the canonical path fails.
     *
     * @param path the path for which to retrieve the canonical or
     *      absolute path
     * @return the canonical or absolute path
     */
    public static String canonicalOrAbsolutePath(String path) {

        try {
            return canonicalPath(path);
        } catch (Exception e) {
            return absolutePath(path);
        }
    }
}
