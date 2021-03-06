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
 * Copyright (c) 2001-2004, The HSQL Development Group
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

import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.Iterator;
import org.hsqldb.HsqlNameManager.HsqlName;

// fredt@users 20020225 - patch 1.7.0 by boucherb@users - named constraints
// fredt@users 20020320 - doc 1.7.0 - update
// tony_lai@users 20020820 - patch 595156 - violation of Integrity constraint name

/**
 * Implementation of a table constraint with references to the indexes used
 * by the constraint.<p>
 *
 * Methods for checking constraint violation must be called from within a
 * synchronized context that locks the ConsraintCore object.
 *
 * @version    1.7.2
 */
class Constraint {

/*
SQL CLI codes

Referential Constraint 0 CASCADE
Referential Constraint 1 RESTRICT
Referential Constraint 2 SET NULL
Referential Constraint 3 NO ACTION
Referential Constraint 4 SET DEFAULT
*/
    static final int CASCADE     = 0,
                     SET_NULL    = 2,
                     NO_ACTION   = 3,
                     SET_DEFAULT = 4;
    static final int FOREIGN_KEY = 0,
                     MAIN        = 1,
                     UNIQUE      = 2,
                     CHECK       = 3;
    ConstraintCore   core;
    HsqlName         constName;
    int              constType;

    /**
     *  Constructor declaration for UNIQUE
     *
     * @param  name
     * @param  t
     * @param  index
     */
    Constraint(HsqlName name, Table t, Index index) {

        core           = new ConstraintCore();
        constName      = name;
        constType      = UNIQUE;
        core.mainTable = t;
        core.mainIndex = index;
        /* fredt - in unique constraints column list for iColMain is the
           visible columns of iMain
        */
        core.mainColArray = ArrayUtil.arraySlice(index.getColumns(), 0,
                index.getVisibleColumns());
        core.colLen = core.mainColArray.length;
    }

    /**
     *  Constructor for main constraints (foreign key references in PK table)
     *
     * @param  name
     * @param  t
     * @param  index
     */
    Constraint(HsqlName name, Constraint fkconstraint) {

        constName = name;
        constType = MAIN;
        core      = fkconstraint.core;
    }

    /**
     *  Constructor for foreign key constraints
     *
     * @param  pkname
     * @param  fkname
     * @param  main
     * @param  ref
     * @param  colmain
     * @param  colref
     * @param  imain
     * @param  iref
     * @param  deleteAction
     * @param  updateAction
     * @exception  HsqlException  Description of the Exception
     */
    Constraint(HsqlName pkname, HsqlName fkname, Table main, Table ref,
               int colmain[], int colref[], Index imain, Index iref,
               int deleteAction, int updateAction) throws HsqlException {

        core           = new ConstraintCore();
        core.pkName    = pkname;
        core.fkName    = fkname;
        constName      = fkname;
        constType      = FOREIGN_KEY;
        core.mainTable = main;
        core.refTable  = ref;
        /* fredt - in FK constraints column lists for iColMain and iColRef have
           identical sets to visible columns of iMain and iRef respectively
           but the order of columns can be different and must be preserved
        */
        core.mainColArray = colmain;
        core.colLen       = core.mainColArray.length;
        core.refColArray  = colref;
        core.mainIndex    = imain;
        core.refIndex     = iref;
        core.deleteAction = deleteAction;
        core.updateAction = updateAction;
    }

    /**
     * temp constraint constructor
     */
    Constraint(HsqlName name, int[] mainCol, Table refTable, int[] refCol,
               int type, int deleteAction, int updateAction) {

        core              = new ConstraintCore();
        constName         = name;
        constType         = type;
        core.mainColArray = mainCol;
        core.refTable     = refTable;
        core.refColArray  = refCol;
        core.deleteAction = deleteAction;
        core.updateAction = updateAction;
    }

    private Constraint() {}

    HsqlName getName() {
        return constName;
    }

    /**
     * Changes constraint name.
     *
     * @param name
     * @param isquoted
     */
    private void setName(String name, boolean isquoted) throws HsqlException {
        constName.rename(name, isquoted);
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index refereneced by a foreign key
     */
    String getPkName() {
        return core.pkName == null ? null
                                   : core.pkName.name;
    }

    /**
     *  probably a misnomer, but DatabaseMetaData.getCrossReference specifies
     *  it this way (I suppose because most FKs are declared against the PK of
     *  another table)
     *
     *  @return name of the index for the referencing foreign key
     */
    String getFkName() {
        return core.fkName == null ? null
                                   : core.fkName.name;
    }

    /**
     *  Method declaration
     *
     * @return name of the index for the foreign key column (child)
     */
    int getType() {
        return constType;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getMain() {
        return core.mainTable;
    }

    Index getMainIndex() {
        return core.mainIndex;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    Table getRef() {
        return core.refTable;
    }

    Index getRefIndex() {
        return core.refIndex;
    }

    /**
     *  The action of (foreign key) constraint on delete
     *
     * @return
     */
    int getDeleteAction() {
        return core.deleteAction;
    }

    /**
     *  The action of (foreign key) constraint on update
     *
     * @return
     */
    int getUpdateAction() {
        return core.updateAction;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getMainColumns() {
        return core.mainColArray;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    int[] getRefColumns() {
        return core.refColArray;
    }

    /**
     *  See if an index is part this constraint and the constraint is set for
     *  a foreign key. Used for tests before dropping an index. (fredt@users)
     *
     * @return
     */
    boolean isIndexFK(Index index) {

        if (constType == FOREIGN_KEY || constType == MAIN) {
            if (core.mainIndex == index || core.refIndex == index) {
                return true;
            }
        }

        return false;
    }

    /**
     *  See if an index is part this constraint and the constraint is set for
     *  a unique constraint. Used for tests before dropping an index.
     *  (fredt@users)
     *
     * @return
     */
    boolean isIndexUnique(Index index) {

        if (constType == UNIQUE && core.mainIndex == index) {
            return true;
        }

        return false;
    }

    /**
     * Only for check constraints
     */
    boolean hasColumn(Table table, String colname) {

        if (constType != CHECK) {
            return false;
        }

        Expression.Collector coll = new Expression.Collector();

        coll.addAll(core.check, Expression.COLUMN);

        Iterator it = coll.iterator();

        for (; it.hasNext(); ) {
            Expression e = (Expression) it.next();

            if (e.getColumnName().equals(colname)
                    && table.tableName.name.equals(e.getTableName())) {
                return true;
            }
        }

        return false;
    }

// fredt@users 20020225 - patch 1.7.0 by fredt - duplicate constraints

    /**
     * Compares this with another constraint column set. This implementation
     * only checks UNIQUE constraints.
     */
    boolean isEquivalent(int col[], int type) {

        if (type != constType || constType != UNIQUE
                || core.colLen != col.length) {
            return false;
        }

        return ArrayUtil.haveEqualSets(core.mainColArray, col, core.colLen);
    }

    /**
     * Compares this with another constraint column set. This implementation
     * only checks FOREIGN KEY constraints.
     */
    boolean isEquivalent(Table tablemain, int colmain[], Table tableref,
                         int colref[]) {

        if (constType != Constraint.MAIN
                || constType != Constraint.FOREIGN_KEY) {
            return false;
        }

        if (tablemain != core.mainTable || tableref != core.refTable) {
            return false;
        }

        return ArrayUtil.areEqualSets(core.mainColArray, colmain)
               && ArrayUtil.areEqualSets(core.refColArray, colref);
    }

    /**
     *  Used to update constrains to reflect structural changes in a table.
     *  Prior checks must ensure that this method does not throw.
     *
     * @param  oldt reference to the old version of the table
     * @param  newt referenct to the new version of the table
     * @param  colindex index at which table column is added or removed
     * @param  adjust -1, 0, +1 to indicate if column is added or removed
     * @throws  HsqlException
     */
    void replaceTable(Table oldt, Table newt, int colindex,
                      int adjust) throws HsqlException {

        if (oldt == core.mainTable) {
            core.mainTable = newt;

            // excluede CHECK
            if (core.mainIndex != null) {
                core.mainIndex =
                    core.mainTable.getIndex(core.mainIndex.getName().name);
                core.mainColArray =
                    ArrayUtil.toAdjustedColumnArray(core.mainColArray,
                                                    colindex, adjust);
            }
        }

        if (oldt == core.refTable) {
            core.refTable = newt;

            if (core.refIndex != null) {
                core.refIndex =
                    core.refTable.getIndex(core.refIndex.getName().name);

                if (core.refIndex != core.mainIndex) {
                    core.refColArray =
                        ArrayUtil.toAdjustedColumnArray(core.refColArray,
                                                        colindex, adjust);
                }
            }
        }
    }

    /**
     *  Checks for foreign key violation when inserting a row in the child
     *  table.
     *
     * @param  row
     * @throws  HsqlException
     */
    void checkInsert(Object row[]) throws HsqlException {

        if (constType == Constraint.MAIN || constType == Constraint.UNIQUE) {

            // inserts in the main table are never a problem
            // unique constraints are checked by the unique index
            return;
        }

        if (constType == Constraint.CHECK) {
            checkCheckConstraint(row);

            return;
        }

        if (core.mainIndex.isNull(row, core.refColArray)) {
            return;
        }

        // a record must exist in the main table
        if (core.mainIndex.findNotNull(row, core.refColArray, true) == null) {

            // special case: self referencing table and self referencing row
            if (core.mainTable == core.refTable) {
                boolean match = true;

                for (int i = 0; i < core.colLen; i++) {
                    if (!row[core.refColArray[i]].equals(
                            row[core.mainColArray[i]])) {
                        match = false;

                        break;
                    }
                }

                if (match) {
                    return;
                }
            }

            throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT,
                              Trace.Constraint_violation, new Object[] {
                core.fkName.name, core.mainTable.getName().name
            });
        }
    }

    private void checkCheckConstraint(Object[] row) throws HsqlException {

        core.checkFilter.currentData = row;

        if (!core.check.test()) {
            core.checkFilter.currentRow = null;

            throw Trace.error(Trace.CHECK_CONSTRAINT_VIOLATION,
                              Trace.Constraint_violation, new Object[] {
                constName.name, core.mainTable.tableName.name
            });
        }

        core.checkFilter.currentData = null;

        return;
    }

// fredt@users 20020225 - patch 1.7.0 - cascading deletes

    /**
     * New method to find any referencing node (containing the row) for a
     * foreign key (finds row in child table). If ON DELETE CASCADE is
     * supported by this constraint, then the method finds the first row
     * among the rows of the table ordered by the index and doesn't throw.
     * Without ON DELETE CASCADE, the method attempts to finds any row that
     * exists, in which case it throws an exception. If no row is found,
     * null is returned.
     * (fredt@users)
     *
     * @param  array of objects for a database row
     * @param  forDelete should we allow 'ON DELETE CASCADE' or 'ON UPDATE CASCADE'
     * @return Node object or null
     * @throws  HsqlException
     */
    Node findFkRef(Object row[], boolean forDelete) throws HsqlException {

        if (row == null) {
            return null;
        }

        if (core.refIndex.isNull(row, core.mainColArray)) {
            return null;
        }

        // there must be no record in the 'slave' table
        // sebastian@scienion -- dependent on forDelete | forUpdate
        boolean findfirst = forDelete ? core.deleteAction != NO_ACTION
                                      : core.updateAction != NO_ACTION;
        Node node = core.refIndex.findNotNull(row, core.mainColArray,
                                              findfirst);

        // tony_lai@users 20020820 - patch 595156
        // sebastian@scienion -- check whether we should allow 'ON DELETE CASCADE' or 'ON UPDATE CASCADE'
        if (!(node == null || findfirst)) {
            throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION,
                              Trace.Constraint_violation, new Object[] {
                core.fkName.name, core.refTable.getName().name
            });
        }

        return node;
    }

    /**
     * Method to find any referring node in the main table. This is used
     * to check referential integrity when updating a node. We have to make
     * sure that the main table still holds a valid main record. If a valid
     * row is found the corresponding <code>Node</code> is returned.
     * Otherwise a 'INTEGRITY VIOLATION' Exception gets thrown.
     *
     * @param row Obaject[]; the row containing the key columns which have to be
     * checked.
     *
     * @see Table#checkUpdateCascade(Table,Object[],Object[],Session,boolean)
     *
     * @throws HsqlException
     */
    Node findMainRef(Object row[]) throws HsqlException {

        if (core.mainIndex.isNull(row, core.refColArray)) {
            return null;
        }

        Node node = core.mainIndex.findNotNull(row, core.refColArray, true);

        // -- there has to be a valid node in the main table
        // --
        if (node == null) {
            throw Trace.error(Trace.INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT,
                              Trace.Constraint_violation, new Object[] {
                core.fkName.name, core.refTable.getName().name
            });
        }

        return node;
    }

    /**
     *  Checks if updating a set of columns in a table row breaks the
     *  check and referential integrity constraint.
     *
     * @param  col array of column indexes for columns to check
     * @param  deleted  rows to delete
     * @param  inserted rows to insert
     * @throws  HsqlException
     */
    void checkUpdate(Result inserted) throws HsqlException {

        if (constType == Constraint.CHECK) {

            // check inserted records
            Record r = inserted.rRoot;

            while (r != null) {
                checkCheckConstraint(r.data);

                r = r.next;
            }

            return;
        }
    }

    static boolean hasReferencedRow(Object[] rowdata, int[] rowColArray,
                                    Index mainIndex) throws HsqlException {

        // check for nulls and return true if any
        for (int i = 0; i < rowColArray.length; i++) {
            Object o = rowdata[rowColArray[i]];

            if (o == null) {
                return true;
            }
        }

        // else a record must exist in the main index
        if (mainIndex.find(rowdata, rowColArray) == null) {
            return false;
        }

        return true;
    }

    static void checkReferencedRows(Table table, int[] rowColArray,
                                    Index mainIndex) throws HsqlException {

        Index index = table.getPrimaryIndex();
        Node  node  = index.first();

        while (node != null) {
            Object[] rowdata = node.getData();

            if (!Constraint.hasReferencedRow(rowdata, rowColArray,
                                             mainIndex)) {
                String colvalues = "";

                for (int i = 0; i < rowColArray.length; i++) {
                    Object o = rowdata[rowColArray[i]];

                    colvalues += o;
                    colvalues += ",";
                }

                throw Trace.error(
                    Trace.INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT,
                    Trace.Constraint_violation, new Object[] {
                    colvalues, table.getName().name
                });
            }

            node = index.next(node);
        }
    }
}
