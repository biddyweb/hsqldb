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

// tony_lai@users 20020820 - patch 595099 - user define PK name

/**
 *  Class declaration
 * @author sqlbob@users (RMP)
 * @version    1.7.0
 */
class TextTable extends org.hsqldb.Table {

    private String  dataSource = "";
    private String  firstLine  = "";
    private boolean isReversed = false;

    /**
     *  Constructor declaration
     *
     * @param  db
     * @param  isTemp is a temp text table
     * @param  name
     * @exception  HsqlException  Description of the Exception
     */
    TextTable(Database db, HsqlNameManager.HsqlName name, int type,
              int sessionid) throws HsqlException {
        super(db, name, type, sessionid);
    }

    /**
     * This method does some of the work involved with managing the creation
     * and openning of the cache, the rest is done in Log.java and
     * TextCache.java.
     *
     * Better clarification of the role of the methods is needed.
     */
    private void openCache(String dataSourceNew, boolean isReversedNew,
                           boolean isReadOnlyNew) throws HsqlException {

        if (dataSourceNew == null) {
            dataSourceNew = "";
        }

        // Close old cache:
        database.logger.closeTextCache(this);

        cache = null;

        setIndexRootsNull();

        // Open new cache:
        if (dataSourceNew.length() > 0) {
            try {
                cache = database.logger.openTextCache(this, dataSourceNew,
                                                      isReadOnlyNew,
                                                      isReversedNew);

                // force creation of Row objects with nextPos pointers
                ((TextCache) cache).setSourceIndexing(true);

                // read and insert all the rows from the source file
                PointerCachedDataRow row = (PointerCachedDataRow) getRow(0,
                    null);

                while (row != null) {
                    insertNoChange(row);

                    row = (PointerCachedDataRow) getRow(row.nextPos, null);
                }

                ((TextCache) cache).setSourceIndexing(false);
            } catch (HsqlException e) {
                int linenumber = cache == null ? 0
                                               : ((TextCache) cache)
                                                   .getLineNumber();

                if (!dataSource.equals(dataSourceNew)
                        || isReversedNew != isReversed
                        || isReadOnlyNew != isReadOnly) {

                    // Restore old cache.
                    // fredt - todo - recursion works - but code is not clear
                    openCache(dataSource, isReversed, isReadOnly);
                } else {
                    if (cache != null) {
                        cache.closeFile();
                    }

                    //fredt added
                    cache      = null;
                    dataSource = "";
                    isReversed = false;
                }

                // everything is in order here.
                // At this point table should either have a valid (old) data
                // source and cache or have an empty source and null cache.
                throw Trace.error(Trace.TEXT_FILE, new Object[] {
                    new Integer(linenumber), e.getMessage()
                });
            }
        }

        dataSource = dataSourceNew;
        isReversed = (isReversedNew && dataSourceNew.length() > 0);
    }

    boolean equals(String other, Session c) {

        boolean isEqual = super.equals(other, c);

        if (isEqual && isReversed) {
            try {
                openCache(dataSource, isReversed, isReadOnly);
            } catch (HsqlException e) {
                return false;
            }
        }

        return isEqual;
    }

    boolean equals(String other) {

        boolean isEqual = super.equals(other);

        if (isEqual && isReversed) {
            try {
                openCache(dataSource, isReversed, isReadOnly);
            } catch (HsqlException e) {
                return false;
            }
        }

        return isEqual;
    }

    /**
     * High level command to assign a data source to the table definition.
     * Reassings only if the data source or direction has changed.
     */
    protected void setDataSource(String dataSourceNew, boolean isReversedNew,
                                 Session s) throws HsqlException {

        if (isTemp) {
            Trace.check(s.getId() == ownerSessionId, Trace.ACCESS_IS_DENIED);
        } else {
            s.checkAdmin();
        }

        dataSourceNew = dataSourceNew.trim();

        //-- Open if descending, direction changed, or file changed.
        if (isReversedNew || (isReversedNew != isReversed)
                ||!dataSource.equals(dataSourceNew)) {
            openCache(dataSourceNew, isReversedNew, isReadOnly);
        }

        if (isReversed) {
            isReadOnly = true;
        }
    }

    protected String getDataSource() {
        return dataSource;
    }

    protected boolean isDescDataSource() {
        return isReversed;
    }

    /**
     * Used by INSERT, DELETE, UPDATE operations. This class will return
     * a more appropriate message when there is no data source.
     */
    void checkDataReadOnly() throws HsqlException {

        if (dataSource.length() == 0) {
            throw Trace.error(Trace.UNKNOWN_DATA_SOURCE);
        }

        if (isReadOnly) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }
    }

    void setDataReadOnly(boolean value) throws HsqlException {

        if (isReversed && value == true) {
            throw Trace.error(Trace.DATA_IS_READONLY);
        }

        openCache(dataSource, isReversed, value);

        isReadOnly = value;
    }

    boolean isIndexCached() {
        return false;
    }

    protected Table duplicate() throws HsqlException {
        return new TextTable(database, tableName, tableType, ownerSessionId);
    }

    CachedRow getRow(int pos, Node primarynode) throws HsqlException {

        CachedDataRow r = (CachedDataRow) cache.getRow(pos, this);

        if (r == null) {
            return null;
        }

        if (primarynode == null) {
            r.setNewNodes();
        } else {
            r.setPrimaryNode(primarynode);
        }

        return r;
    }

    void drop() throws HsqlException {
        openCache("", false, false);
    }

    void setIndexRoots(String s) throws HsqlException {

        // do nothing
    }
}
