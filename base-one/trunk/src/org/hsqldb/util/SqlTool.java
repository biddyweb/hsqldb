/*
 * $Id: SqlTool.java,v 1.6 2004/01/20 22:53:21 unsaved Exp $
 *
 * Copyright (c) 2001-2003, The HSQL Development Group
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

package org.hsqldb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

/**
 * Sql Tool.  A command-line and/or interactive SQL tool.
 * (Note:  For every Javadoc block comment, I'm using a single blank line 
 *  immediately after the description, just like's Sun's examples in
 *  their Coding Conventions document).
 *
 * @version $Revision: 1.6 $
 */
public class SqlTool {
    final static private String DEFAULT_JDBC_DRIVER = "org.hsqldb.jdbcDriver";
    static private Connection conn;

    final static private String DEFAULT_RCFILE =
            System.getProperty("user.home") + "/hsqldb.rc";

    /**
     * All the info we need to connect up to a database.
     * If it is anticipated that SqlTool.execute() will be executed
     * from other code directly rather than from SqlTool.main(), then
     * make this a global-level class.
     * I expect other Java code to invoke SqlFile.execute(), but not
     * anything in this class.
     */
    private static class ConnectData {
        public void report() {
            System.err.println("urlid: " + id + ", url: " + url
                + ", username: " + username + ", password: " + password);
        }
        public ConnectData(String inFile, String dbKey) throws Exception {
            File file = new File((inFile == null) ? DEFAULT_RCFILE : inFile);
            if (!file.canRead()) {
                throw new IOException("Please set up rc file '"
                       + file + "'");
            }
            // System.err.println("Using RC file '" + file + "'");
            StringTokenizer tokenizer = null;
            boolean thisone = false;
            String s;
            String keyword, value;
            BufferedReader br = new BufferedReader(new FileReader(file));
            int linenum = 0;
            while ((s = br.readLine()) != null) {
                ++linenum;
                s = s.trim();
                if (s.length() == 0) {
                    continue;
                }
                if (s.charAt(0) == '#') {
                    continue;
                }
                tokenizer = new StringTokenizer(s);
                if (tokenizer.countTokens() != 2) {
                    throw new Exception("Bad line " + linenum + " in '" + file
                            + "':  " + s);
                }
                keyword = tokenizer.nextToken();
                value = tokenizer.nextToken();
                if (dbKey == null) {
                    if (keyword.equals("urlid")) System.out.println(value);
                    continue;
                }
                if (keyword.equals("urlid")) {
                    if (value.equals(dbKey)) {
                        if (id == null) {
                            id  = dbKey;
                            thisone = true;
                        } else {
                            throw new Exception("Key '" + dbKey
                                    + " redefined at"
                                    + " line " + linenum + " in '" + file);
                        }
                    } else {
                        thisone = false;
                    }
                    continue;
                }
                if (thisone) {
                    if (keyword.equals("url")) {
                        url = value;
                    } else if (keyword.equals("username")) {
                        username = value;
                    } else if (keyword.equals("password")) {
                        password = value;
                    } else {
                        throw new Exception("Bad line " + linenum + " in '"
                                + file + "':  " + s);
                    }
                }
            }
            if (dbKey == null) return;
            if (url == null || username == null || password == null) {
                throw new Exception("url or username or password not set "
                        + "for '" + dbKey + "' in file '" + file + "'");
            }
        }
        String id = null;
        String url = null;
        String username = null;
        String password = null;
    }

    static final private String SYNTAX_MESSAGE =
            "Usage: java org.hsqldb.util.SqlTool [--optname [optval...]] "
            + "urlid [file1.sql...]\n"
            + "where arguments are:\n"
            + "    --help                   Prints this message\n"
            + "    --list                   List urlids in the rcfile\n"
            + "    --debug                  print Debug info to stderr\n"
        + "    --rcfile /file/path.rc   Connect Info File [$HOME/hsqldb.rc]\n"
            + "    --driver a.b.c.Driver    JDBC driver class ["
            +           DEFAULT_JDBC_DRIVER + "]\n"
        + "    urlid                    ID of url/userame/password in rcfile\n"
            + "    file1.sql...             SQL files to be executed [stdin]\n"
            + "                             "
            + "(Use '-' for non-interactively stdin)";

    private static class BadCmdline extends Exception {};
    private static BadCmdline bcl = new BadCmdline();

    /**
     * Main method.
     * Like most main methods, this is not intended to be thread-safe.
     *
     * @param arg
     */
    public static void main(String arg[]) {
        String rcFile = null;
        String driver = DEFAULT_JDBC_DRIVER;
        String targetDb = null;
        boolean debug = false;
        File[] scriptFiles = { null };
        int i = -1;
        boolean listMode = false;
        boolean interactive = false;

        try {
            while ((i + 1 < arg.length) && arg[i+1].startsWith("--")) {
                i++;
                if (arg[i].length() == 2) break; // "--"
                if (arg[i].substring(2).equals("help")) {
                    System.out.println(SYNTAX_MESSAGE);
                    System.exit(0);
                }
                if (arg[i].substring(2).equals("list")) {
                    listMode = true;
                    continue;
                }
                if (arg[i].substring(2).equals("rcfile")) {
                    if (++i == arg.length) throw bcl;
                    rcFile = arg[i];
                    continue;
                }
                if (arg[i].substring(2).equals("debug")) {
                    debug = true;
                    continue;
                }
                if (arg[i].substring(2).equals("driver")) {
                    if (++i == arg.length) throw bcl;
                    driver = arg[i];
                    continue;
                }
                throw bcl;
            }
            if (!listMode) {
                if (++i == arg.length) throw bcl;
                targetDb = arg[i];
            }
            int scriptIndex = 0;
            if (arg.length > i + 1) {
                interactive = false;
                if (arg.length != i + 2 || !arg[i + 1].equals("-")) {
                    scriptFiles = new File[arg.length - i - 1];
                    if (debug) {
                        System.err.println(
                                "scriptFiles has " + scriptFiles.length 
                                        + " elements");
                    }
                    while (i + 1 < arg.length) {
                        scriptFiles[scriptIndex++]  = new File(arg[++i]);
                    }
                }
            } else {
                interactive = true;
            }
        } catch (BadCmdline bcl) {
            System.err.println(SYNTAX_MESSAGE);
            System.exit(2);
        }
        ConnectData conData = null;
        try {
            conData = new ConnectData(rcFile, targetDb);
        } catch (Exception e) {
            System.err.println(
                    "Failed to retrieve connection info for database '"
                    + targetDb + "': " + e.getMessage());
            System.exit(1);
        }
        if (listMode) System.exit(0);
        if (debug) conData.report();

        try {
            // As described in the JDBC FAQ:
            // http://java.sun.com/products/jdbc/jdbc-frequent.html;
            // Why doesn't calling class.forName() load my JDBC driver?
            // There is a bug in the JDK 1.1.x that can cause Class.forName()
            // to fail. // new org.hsqldb.jdbcDriver();
            Class.forName(driver).newInstance();

            conn = DriverManager.getConnection(conData.url, conData.username,
                                                conData.password);
        } catch (Exception e) {
            System.err.println(
                    "Failed to get a connection to " + conData.url
                    + ".  " + e.getMessage());
            //e.printStackTrace();
            // Let's not continuing as if nothing is wrong.
            throw new RuntimeException(e.getMessage());
        }

        SqlFile[] sqlFiles = new SqlFile[scriptFiles.length];
        try {
            for (int j = 0; j < scriptFiles.length; j++) {
                sqlFiles[j] = new SqlFile(scriptFiles[j], interactive);
            }
        } catch (IOException ioe) {
            try {
                conn.close();
            } catch (Exception e) {}
            System.err.println(ioe.getMessage());
            System.exit(2);
        }

        int retval = 0; // Value we will return via System.exit().
        try {
            for (int j = 0; j < scriptFiles.length; j++) {
                sqlFiles[j].execute(conn);
            }
        } catch (IOException ioe) {
            System.err.println("Failed to execute SQL:  " + ioe.getMessage());
            retval = 3;
        // These two Exception types are handled properly inside of SqlFile.
        // We just need to return an appropriate error status.
        } catch (SqlToolError ste) {
            retval = 2;
        } catch (SQLException se) {
            retval = 1;
        }
        try {
            conn.close();
        } catch (Exception e) {}
        System.exit(retval);
    }
}
