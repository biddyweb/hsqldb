/* Copyright (c) 2001-2003, The HSQL Development Group
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


package org.hsqldb;

import java.sql.SQLException;

/**
 * The representation of a savepoint, which is a point within
 * the current transaction that can be referenced from the
 * <code>Connection.rollback</code> method. When a transaction
 * is rolled back to a savepoint all changes made after that
 * savepoint are undone.
 * <p>
 * Savepoints can be either named or unnamed. Unnamed
 * savepoints
 * are identified by an ID generated by the underlying
 * data source.
 *
 * <b>HSQLDB-Specific Information:</b> <p>
 *
 * As SQL draft standards do not provide for unnamed savepoints,
 * this feature is not supported in 1.7.2.<p>
 *
 * Named Savepoints can be set if the Connection is autoCommit. Such Savepoints
 * will be cleared when the next statement is issued unless autoCommit is set
 * to false prior to executing any statement.
 *
 * </span> <!-- end release-specific documentation -->
 *
 * @since 1.4
 * @author  boucherb@users.sourceforge.net
 */
public class jdbcSavepoint implements java.sql.Savepoint {

    String         name;
    jdbcConnection connection;

    jdbcSavepoint(String name, jdbcConnection conn) throws SQLException {

        if (name == null) {
            throw jdbcDriver.sqlException(Trace.INVALID_JDBC_ARGUMENT,
                                          "name is null");
        }

        this.name  = name;
        connection = conn;
    }

    /**
     * Retrieves the generated ID for the savepoint
     * that this <code>Savepoint</code> object represents.
     * @return the numeric ID of this savepoint
     * @exception SQLException if this is a named
     * savepoint
     * @since 1.4
     */
    public int getSavepointId() throws SQLException {
        throw jdbcDriver.notSupported;
    }

    /**
     * Retrieves the name of the savepoint that this
     * <code>Savepoint</code> object represents. <p>
     *
     * @return the name of this savepoint
     * @exception SQLException if this is an un-named
     * savepoint
     * @since 1.4
     */
    public String getSavepointName() throws SQLException {
        return name;
    }

    public String toString() {
        return super.toString() + "[name=" + name + "]";
    }
}
