/* Copyrights and Licenses
 *
 * This product includes Hypersonic SQL.
 * Originally developed by Thomas Mueller and the Hypersonic SQL Group.
 *
 * Copyright (c) 1995-2000 by the Hypersonic SQL Group. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *     -  Redistributions of source code must retain the above copyright notice, this list of conditions
 *         and the following disclaimer.
 *     -  Redistributions in binary form must reproduce the above copyright notice, this list of
 *         conditions and the following disclaimer in the documentation and/or other materials
 *         provided with the distribution.
 *     -  All advertising materials mentioning features or use of this software must display the
 *        following acknowledgment: "This product includes Hypersonic SQL."
 *     -  Products derived from this software may not be called "Hypersonic SQL" nor may
 *        "Hypersonic SQL" appear in their names without prior written permission of the
 *         Hypersonic SQL Group.
 *     -  Redistributions of any form whatsoever must retain the following acknowledgment: "This
 *          product includes Hypersonic SQL."
 * This software is provided "as is" and any expressed or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the Hypersonic SQL Group or its contributors be liable for any
 * direct, indirect, incidental, special, exemplary, or consequential damages (including, but
 * not limited to, procurement of substitute goods or services; loss of use, data, or profits;
 * or business interruption). However caused any on any theory of liability, whether in contract,
 * strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
 * software, even if advised of the possibility of such damage.
 * This software consists of voluntary contributions made by many individuals on behalf of the
 * Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2002, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer, including earlier
 * license statements (above) and comply with all above license conditions.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution, including earlier
 * license statements (above) and comply with all above license conditions.
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
import java.util.Enumeration;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlHashMap;
import org.hsqldb.lib.HsqlHashSet;
import org.hsqldb.lib.HsqlObjectToIntMap;
import org.hsqldb.lib.StopWatch;
import org.hsqldb.lib.ValuePool;

// fredt@users 20020320 - doc 1.7.0 - update
// fredt@users 20020315 - patch 1.7.0 by fredt - switch for scripting
// fredt@users 20020130 - patch 476694 by velichko - transaction savepoints
// additions in different parts to support savepoint transactions
// fredt@users 20020910 - patch 1.7.1 by fredt - database readonly enforcement
// fredt@users 20020912 - patch 1.7.1 by fredt - permanent internal connection
// boucherb@users 20030512 - patch 1.7.2 - compiled statements
//                                       - session becomes execution hub
// boucherb@users 20050510 - patch 1.7.2 - generalized Result packet passing
//                                         based command execution
//                                       - batch execution handling

/**
 *  Implementation of a user session with the database.
 *
 * @version  1.7.2
 */
public class Session {

    private Database           dDatabase;
    private User               uUser;
    private HsqlArrayList      tTransaction;
    private boolean            isAutoCommit;
    private boolean            isNestedTransaction;
    private boolean            isNestedOldAutoCommit;
    private int                nestedOldTransIndex;
    private boolean            isReadOnly;
    private int                iMaxRows;
    private Object             iLastIdentity;
    private boolean            isClosed;
    private int                iId;
    private HsqlObjectToIntMap savepoints;
    private boolean            script;
    private jdbcConnection     intConnection;

    /**
     *  closes the session.
     *
     * @throws  SQLException
     */
    public void finalize() throws SQLException {
        disconnect();
    }

    /**
     * Constructs a new Session object.
     *
     * @param  db the database to which this represents a connection
     * @param  user the initial user
     * @param  autocommit the initial autocommit value
     * @param  readonly the initial readonly value
     * @param  id the session identifier, as known to the database
     */
    Session(Database db, User user, boolean autocommit, boolean readonly,
            int id) {

        iId          = id;
        dDatabase    = db;
        uUser        = user;
        tTransaction = new HsqlArrayList();
        isAutoCommit = autocommit;
        isReadOnly   = readonly;
        dbci         = new DatabaseCommandInterpreter(this);
        cse          = new CompiledStatementExecutor(this);
        cs           = new CompiledStatement();
        csm          = db.compiledStatementManager;
    }

    /**
     *  Retrieves the session identifier for this Session.
     *
     * @return the session identifier for this Session
     */
    int getId() {
        return iId;
    }

    /**
     * Closes this Session, freeing any resources associated with it
     * and rolling back any uncommited transaction it may have open.
     *
     * @throws SQLException if a database access error occurs
     */
    void disconnect() {

        if (isClosed) {
            return;
        }

        rollback();
        dDatabase.dropTempTables(this);

        dDatabase     = null;
        uUser         = null;
        tTransaction  = null;
        savepoints    = null;
        intConnection = null;
        isClosed      = true;
    }

    /**
     * Retrieves whether this Session is closed.
     *
     * @return true if this Session is closed
     */
    boolean isClosed() {
        return isClosed;
    }

    /**
     * Setter for iLastIdentity attribute.
     *
     * @param  i the new value
     */
    void setLastIdentity(Object i) {
        iLastIdentity = i;
    }

    /**
     * Getter for iLastIdentity attribute.
     *
     * @return the current value
     */
    Object getLastIdentity() {
        return iLastIdentity;
    }

    /**
     * Retrieves the Database instance to which this
     * Session represents a connection
     *
     * @return the Database object to which this Session is connected
     */
    Database getDatabase() {
        return dDatabase;
    }

    /**
     * Retrieves the name, as known to the database, of the
     * user currently controlling this Session.
     *
     * @return the name of the user currently connected within this Session
     */
    String getUsername() {
        return uUser.getName();
    }

    /**
     * Retrieves the User object representing the user currently controlling
     * this session
     *
     * @return this Session's User object
     */
    User getUser() {
        return uUser;
    }

    /**
     * Sets this Session's User object to the one specified by the
     * user argument.
     *
     * @param  user the new User object for this session
     */
    void setUser(User user) {
        uUser = user;
    }

    /**
     * Checks whether this Session's current User has the privileges of
     * the ADMIN role.
     *
     * @throws SQLException if this Session's User does not have the
     *      privileges of the ADMIN role.
     */
    void checkAdmin() throws SQLException {
        uUser.checkAdmin();
    }

    /**
     * Checks whether this Session's current User has the set of rights
     * specified by the right argument, in relation to the database
     * object identifier specified by the object argument.
     *
     * @param  object the database object to check
     * @param  right the rights to check for
     * @throws  SQLException if the Session User does not have such rights
     */
    void check(Object object, int right) throws SQLException {
        uUser.check(object, right);
    }

    /**
     * This is used for reading - writing to existing tables.
     * @throws  SQLException
     */
    void checkReadWrite() throws SQLException {
        Trace.check(!isReadOnly, Trace.DATABASE_IS_READONLY);
    }

    /**
     * This is used for creating new database objects such as tables.
     * @throws  SQLException
     */
    void checkDDLWrite() throws SQLException {

        boolean condition = uUser.isSys() ||!dDatabase.filesReadOnly;

        Trace.check(condition, Trace.DATABASE_IS_READONLY);
    }

    /**
     * Sets the session user's password to the value of the argument, s.
     *
     * @param  s
     */
    void setPassword(String s) {
        uUser.setPassword(s);
    }

    /**
     *  Adds a single-row deletion step to the transaction UNDO buffer.
     *
     * @param  table the table from which the row was deleted
     * @param  row the deleted row
     * @throws  SQLException
     */
    void addTransactionDelete(Table table, Object row[]) throws SQLException {

        if (!isAutoCommit) {
            Transaction t = new Transaction(true, isNestedTransaction, table,
                                            row);

            tTransaction.add(t);
        }
    }

    /**
     *  Adds a single-row inssertion step to the transaction UNDO buffer.
     *
     * @param  table the table into which the row was inserted
     * @param  row the inserted row
     * @throws  SQLException
     */
    void addTransactionInsert(Table table, Object row[]) throws SQLException {

        if (!isAutoCommit) {
            Transaction t = new Transaction(false, isNestedTransaction,
                                            table, row);

            tTransaction.add(t);
        }
    }

    /**
     *  Setter for the autocommit attribute.
     *
     * @param  autocommit the new value
     * @throws  SQLException
     */
    void setAutoCommit(boolean autocommit) throws SQLException {

        if (autocommit != isAutoCommit) {
            commit();

            isAutoCommit = autocommit;
        }
    }

    /**
     * Commits any uncommited transaction this Session may have open
     *
     * @throws  SQLException
     */
    void commit() throws SQLException {

        tTransaction.clear();

        if (savepoints != null) {
            savepoints.clear();
        }
    }

    /**
     * Rolls back any uncommited transaction this Session may have open.
     *
     * @throws  SQLException
     */
    void rollback() {

        int i = tTransaction.size();

        while (i-- > 0) {
            Transaction t = (Transaction) tTransaction.get(i);

            t.rollback(this);
        }

        tTransaction.clear();

        if (savepoints != null) {
            savepoints.clear();
        }
    }

    /**
     *  Implements a transaction SAVEPOIINT. Applications may do a partial
     *  rollback by calling rollbackToSavepoint(). A new SAVEPOINT with the
     *  name of an existing one, replaces the old SAVEPOINT.
     *
     * @param  name Name of savepoint
     * @throws  SQLException
     */
    void savepoint(String name) throws SQLException {

        if (savepoints == null) {
            savepoints = new HsqlObjectToIntMap(4);
        }

        savepoints.put(name, tTransaction.size());
    }

    /**
     *  Implements a partial transaction ROLLBACK.
     *
     * @param  name Name of savepoint that was marked before by savepoint()
     *      call
     * @throws  SQLException
     */
    void rollbackToSavepoint(String name) throws SQLException {

        int index = -1;

        if (savepoints != null) {
            index = savepoints.get(name);
        }

        Trace.check(index >= 0, Trace.SAVEPOINT_NOT_FOUND, name);

        int i = tTransaction.size() - 1;

        for (; i >= index; i--) {
            Transaction t = (Transaction) tTransaction.get(i);

            t.rollback(this);
            tTransaction.remove(i);
        }

        // remove all rows above index
        Enumeration en = savepoints.keys();

        for (; en.hasMoreElements(); ) {
            Object key = en.nextElement();

            if (savepoints.get(key) >= index) {
                savepoints.remove(key);
            }
        }
    }

    /**
     * Starts a nested transaction.
     *
     * @throws  SQLException
     */
    void beginNestedTransaction() throws SQLException {

        Trace.doAssert(!isNestedTransaction, "beginNestedTransaction");

        isNestedOldAutoCommit = isAutoCommit;

        // now all transactions are logged
        isAutoCommit        = false;
        nestedOldTransIndex = tTransaction.size();
        isNestedTransaction = true;
    }

    /**
     * Ends a nested transaction.
     *
     * @param  rollback true to roll back or false to commit the nested transaction
     * @throws  SQLException
     */
    void endNestedTransaction(boolean rollback) throws SQLException {

        Trace.doAssert(isNestedTransaction, "endNestedTransaction");

        if (rollback) {
            int i = tTransaction.size();

            while (i-- > nestedOldTransIndex) {
                Transaction t = (Transaction) tTransaction.get(i);

                t.rollback(this);
            }
        }

        // reset after the rollback
        isNestedTransaction = false;
        isAutoCommit        = isNestedOldAutoCommit;

        if (isAutoCommit == true) {
            tTransaction.setSize(nestedOldTransIndex);
        }
    }

    /**
     * Setter for readonly attribute.
     *
     * @param  readonly the new value
     */
    void setReadOnly(boolean readonly) throws SQLException {

        if (!readonly && dDatabase.databaseReadOnly) {
            throw Trace.error(Trace.DATABASE_IS_READONLY);
        }

        isReadOnly = readonly;
    }

    /**
     *  Getter for readonly attribute.
     *
     * @return the current value
     */
    boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     *  Setter for maxRows attribute
     *
     * @param  max the new value
     */
    void setMaxRows(int max) {
        iMaxRows = max;
    }

    /**
     *  Getter for maxRows attribute
     *
     * @return the current value
     */
    int getMaxRows() {
        return iMaxRows;
    }

    /**
     *  Getter for nestedTransaction attribute.
     *
     * @return the current value
     */
    boolean isNestedTransaction() {
        return isNestedTransaction;
    }

    /**
     *  Getter for autoCommite attribute.
     *
     * @return the current value
     */
    boolean getAutoCommit() {
        return isAutoCommit;
    }

    /**
     *  A switch to set scripting on the basis of type of statement executed.
     *  A method in Database.java sets this value to false before other
     *  methods are called to act on an SQL statement, which may set this to
     *  true. Afterwards the method reponsible for logging uses
     *  getScripting() to determine if logging is required for the executed
     *  statement. (fredt@users)
     *
     * @param  script The new scripting value
     */
    void setScripting(boolean script) {
        this.script = script;
    }

    /**
     * Getter for scripting attribute.
     *
     * @return  scripting for the last statement.
     */
    boolean getScripting() {
        return script;
    }

    String getAutoCommitStatement(boolean auto) {
        return auto ? "SET AUTOCOMMIT TRUE"
                    : "SET AUTOCOMMIT FALSE";
    }

    /**
     * Retrieves a Connection object equivalent to the one
     * that created this Session.
     *
     * @return  internal connection.
     */
    jdbcConnection getInternalConnection() throws SQLException {

        if (intConnection == null) {
            intConnection = new jdbcConnection(this);
        }

        return intConnection;
    }

// boucherb@users.sf.net 20020810 metadata 1.7.2
//----------------------------------------------------------------
    private final long connectTime = System.currentTimeMillis();

// more effecient for MetaData concerns than checkAdmin

    /**
     * Getter for admin attribute.
     *
     * @ return the current value
     */
    boolean isAdmin() {
        return uUser.isAdmin();
    }

    /**
     * Getter for connectTime attribute.
     *
     * @return the value
     */
    long getConnectTime() {
        return connectTime;
    }

    /**
     * Getter for transactionSise attribute.
     *
     * @return the current value
     */
    int getTransactionSize() {
        return tTransaction.size();
    }

    /**
     * Retrieves whether the database object identifier by the dbobject
     * argument is accessible by the current Session User.
     *
     * @return true if so, else false
     */
    boolean isAccessible(Object dbobject) throws SQLException {
        return uUser.isAccessible(dbobject);
    }

    /**
     * Retrieves the set of the fully qualified names of the classes on
     * which this Session's current user has been granted execute access.
     * If the current user has the privileges of the ADMIN role, the
     * set of all class grants made to all users is returned, including
     * the PUBLIC user, regardless of the value of the andToPublic argument.
     * In reality, ADMIN users have the right to invoke the methods of any
     * and all classes on the class path, but this list is still useful in
     * an ADMIN user context, for other reasons.
     *
     * @param andToPublic if true, grants to public are included
     * @return the list of the fully qualified names of the classes on
     *      which this Session's current user has been granted execute
     *      access.
     */
    HsqlHashSet getGrantedClassNames(boolean andToPublic) {
        return (isAdmin()) ? dDatabase.getUserManager().getGrantedClassNames()
                           : uUser.getGrantedClassNames(andToPublic);
    }

// boucherb@users 20030417 - patch 1.7.2 - compiled statement support
//-------------------------------------------------------------------
    DatabaseCommandInterpreter dbci;
    CompiledStatement          cs;
    CompiledStatementExecutor  cse;
    CompiledStatementManager   csm;

    CompiledStatement sqlCompileStatement(String sql) throws SQLException {

        Tokenizer         tokenizer;
        String            token;
        Parser            parser;
        int               cmd;
        CompiledStatement cs;
        boolean           cmdok;

        tokenizer = new Tokenizer(sql);
        parser    = new Parser(dDatabase, tokenizer, this);
        token     = tokenizer.getString();

        // get first token and its command id
        cmd   = Token.get(token);
        cmdok = true;

        switch (cmd) {

            case Token.SELECT : {
                cs = parser.compileSelectStatement(null);

                break;
            }
            case Token.INSERT : {
                cs = parser.compileInsertStatement(null);

                break;
            }
            case Token.UPDATE : {
                cs = parser.compileUpdateStatement(null);

                break;
            }
            case Token.DELETE : {
                cs = parser.compileDeleteStatement(null);

                break;
            }
            case Token.CALL : {
                cs = parser.compileCallStatement(null);

                break;
            }
            default : {
                cmdok = false;
                cs    = null;

                break;
            }
        }

        // In addition to requiring that the compilation was successful,
        // we also require that the submitted sql represents a _single_
        // valid DML statement.
        if (!cmdok || tokenizer.getPosition() != tokenizer.getLength()) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        // - need to be able to key cs against its sql in statement pool
        // - also need to be able to revalidate its sql occasionally
        cs.sql = sql;

        return cs;
    }

    /**
     * Executes the sql command encapsulated by the cmd argument.
     *
     * @param cmd the command to execute
     * @return the result of executing the command
     */
    Result executeCommand(Result cmd) {

        int type;

        HsqlRuntime.getHsqlRuntime().gc();

        type = cmd.iMode;

        switch (type) {

            case Result.SQLEXECUTE : {
                if (cmd.getSize() > 1) {
                    return sqlExecuteBatch(cmd);
                } else {
                    return sqlExecute(cmd);
                }
            }
            case Result.SQLEXECDIRECT : {
                return sqlExecuteDirect(cmd.getStatement());
            }
            case Result.SQLPREPARE : {
                return sqlPrepare(cmd.getStatement());
            }
            case Result.SQLFREESTMT : {
                return sqlFreeStatement(cmd.getStatementID());
            }
            default : {
                String msg = "operation type:" + type;

                return new Result(msg, Trace.OPERATION_NOT_SUPPORTED);
            }
        }
    }

    /**
     * Directly executes all of the sql statements in the collection
     * represented by the sql argument string.
     *
     * @param sql a sql string
     * @return the result of the last sql statement in the collection
     */
    Result sqlExecuteDirect(String sql) {
        return dbci.execute(sql);
    }

    /**
     * Executes the statement represented by the compiled statement argument.
     *
     * @param cs the compiled statement to execute
     * @return the result of executing the compiled statement
     */
    Result sqlExecuteCompiled(CompiledStatement cs) {
        return cse.execute(cs);
    }

    /**
     * Retrieves an encapsulation of a compiled statement
     * object prepared for execution in this session context. <p>
     *
     * The result may encapsulate a newly constructed object
     * or a compatible object retrieved from a repository of
     * previously compiled statement objects.
     *
     * @param sql a string describing the desired statement object
     * @throws SQLException is a database access error occurs
     * @return the result of preparing the statement
     */
    Result sqlPrepare(String sql) {

        CompiledStatement cs;
        int               csid;
        Result            result;
        boolean           hasSubqueries;

        result        = null;
        cs            = null;
        hasSubqueries = false;

        // get...
        csid = csm.getStatementID(sql);

        // ...check valid...
        if (csid > 0 && csm.isValid(csid, iId)) {
            result       = new Result();
            result.iMode = Result.SQLPREPARE;

            result.setStatementID(csid);

            return result;
        }

        // ...compile or (re)validate
        try {
            cs = sqlCompileStatement(sql);

            Trace.doAssert(
                cs.parameters.length == 0 || cs.subqueries.length == 0,
                "subqueries in parametric statements not yet supported");
        } catch (Throwable t) {
            return new Result(t, sql);
        }

        if (csid <= 0) {
            csid = csm.registerStatement(cs);
        }

        csm.setValidated(csid, iId, dDatabase.getDDLSCN());

        result       = new Result();
        result.iMode = Result.SQLPREPARE;

        result.setStatementID(csid);

        return result;
    }

    Result sqlExecuteBatch(Result cmd) {

        int               csid;
        Object[]          pvals;
        int[]             ptypes;
        Record            record;
        Result            in;
        Result            out;
        Result            err;
        CompiledStatement cs;
        int               batchStatus;
        Expression[]      parameters;
        int[]             updateCounts;
        int               count;

        csid = cmd.getStatementID();
        cs   = csm.getStatement(csid);

        if (cs == null) {
            String msg = "Statement not prepared for csid: " + csid + ").";

            return new Result(msg, Trace.INVALID_IDENTIFIER);
        }

        if (!csm.isValid(csid, iId)) {
            out = sqlPrepare(cs.sql);

            if (out.iMode == Result.ERROR) {
                return out;
            }
        }

        parameters     = cs.parameters;
        ptypes         = cmd.getParameterTypes();
        count          = 0;
        updateCounts   = new int[cmd.getSize()];
        record         = cmd.rRoot;
        out            = new Result(1);
        out.colType[0] = DITypes.INTEGER;
        err            = new Result();
        err.iMode      = Result.ERROR;

        while (record != null) {
            pvals = record.data;
            in    = err;

            try {
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i].bind(pvals[i], ptypes[i]);
                }

                in = sqlExecuteCompiled(cs);
            } catch (Throwable t) {

                // if (t instanceof OutOfMemoryError) {
                // System.gc();
                // }
                // "in" alread equals "err"
                // maybe test for OOME and do a gc() ?
                // t.printStackTrace();
            }

            // On the client side, iterate over the vals and throw
            // a BatchUpdateException if a batch status value of
            // -3 is encountered in the result
            switch (in.iMode) {

                case Result.UPDATECOUNT : {
                    batchStatus = in.iUpdateCount;

                    break;
                }
                case Result.DATA : {

                    // FIXME:  we don't have what it takes yet
                    // to differentiate between things like
                    // stored procedure calls to methods with
                    // void return type and select statements with
                    // a single row/column containg null
                    batchStatus = -2;

                    break;
                }
                case Result.ERROR :
                default : {
                    batchStatus = -3;

                    break;
                }
            }

            updateCounts[count] = batchStatus;
            record              = record.next;
        }

        out.add(new Object[]{ updateCounts });

        return out;
    }

    /**
     * Retrieves the result of executing the prepared statement whose csid
     * and parameter values/types are encapsulated by the cmd argument.
     *
     * @return the result of executing the statement
     */
    Result sqlExecute(Result cmd) {

        int               csid;
        Object[]          pvals;
        int[]             ptypes;
        CompiledStatement cs;
        Expression[]      parameters;

        csid   = cmd.getStatementID();
        pvals  = cmd.getParameterData();
        ptypes = cmd.getParameterTypes();
        cs     = csm.getStatement(csid);

        if (cs == null) {
            String msg = "Statement not prepared for csid: " + csid + ").";

            return new Result(msg, Trace.INVALID_IDENTIFIER);
        }

        if (!csm.isValid(csid, iId)) {
            Result r = sqlPrepare(cs.sql);

            if (r.iMode == Result.ERROR) {
                return r;
            }
        }

        parameters = cs.parameters;

        // don't bother with array length checks...trust the client
        // to send pvals and ptyptes with length at least as long
        // as parameters array
        try {
            for (int i = 0; i < parameters.length; i++) {
                parameters[i].bind(pvals[i], ptypes[i]);
            }
        } catch (Throwable t) {
            return new Result(t, cs.sql);
        }

        return sqlExecuteCompiled(cs);
    }

    /**
     * Retrieves the result of freeing the statement with the given id.
     *
     * @param csid the numeric identifier of the statement
     * @return the result of freeing the indicated statement
     */
    Result sqlFreeStatement(int csid) {

        boolean existed;
        Result  result;

        existed = csm.freeStatement(csid, iId);
        result  = new Result();

        if (existed) {
            result.iUpdateCount = 1;
        }

        return result;
    }

//------------------------------------------------------------------------------
}
