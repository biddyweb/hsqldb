/* Copyright (c) 2001-2009, The HSQL Development Group
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

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlList;
import org.hsqldb.lib.OrderedHashSet;
import org.hsqldb.lib.OrderedIntHashSet;
import org.hsqldb.types.Type;
import org.hsqldb.types.Types;

/**
 * Parser for SQL stored procedures and functions - PSM
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public class ParserRoutine extends ParserDML {

    ParserRoutine(Session session, Scanner t) {
        super(session, t);
    }

    /**
     *  Reads a DEFAULT clause expression.
     */
    /*
     for datetime, the default must have the same fields
     */
    Expression readDefaultClause(Type dataType) {

        Expression e     = null;
        boolean    minus = false;

        if (dataType.isDateTimeType() || dataType.isIntervalType()) {
            switch (token.tokenType) {

                case Tokens.DATE :
                case Tokens.TIME :
                case Tokens.TIMESTAMP :
                case Tokens.INTERVAL : {
                    e = readDateTimeIntervalLiteral();

                    if (e.dataType.typeCode != dataType.typeCode) {

                        // error message
                        throw unexpectedToken();
                    }

                    Object defaultValue = e.getValue(session, dataType);

                    return new ExpressionValue(defaultValue, dataType);
                }
                case Tokens.X_VALUE :
                    break;

                default :
                    e = XreadDateTimeValueFunctionOrNull();
                    break;
            }
        } else if (dataType.isNumberType()) {
            if (token.tokenType == Tokens.MINUS) {
                read();

                minus = true;
            }
        } else if (dataType.isCharacterType()) {
            switch (token.tokenType) {

                case Tokens.USER :
                case Tokens.CURRENT_USER :
                case Tokens.CURRENT_ROLE :
                case Tokens.SESSION_USER :
                case Tokens.SYSTEM_USER :
                case Tokens.CURRENT_CATALOG :
                case Tokens.CURRENT_SCHEMA :
                case Tokens.CURRENT_PATH :
                    FunctionSQL function =
                        FunctionSQL.newSQLFunction(token.tokenString,
                                                   compileContext);

                    e = readSQLFunction(function);
                    break;

                default :
            }
        } else if (dataType.isBooleanType()) {
            switch (token.tokenType) {

                case Tokens.TRUE :
                    read();

                    return Expression.EXPR_TRUE;

                case Tokens.FALSE :
                    read();

                    return Expression.EXPR_FALSE;
            }
        }

        if (e == null) {
            if (token.tokenType == Tokens.NULL) {
                read();

                return new ExpressionValue(null, dataType);
            }

            if (token.tokenType == Tokens.X_VALUE) {
                e = new ExpressionValue(token.tokenValue, token.dataType);

                if (minus) {
                    e = new ExpressionArithmetic(OpTypes.NEGATE, e);

                    e.resolveTypes(session, null);
                }

                read();

                Object defaultValue = e.getValue(session, dataType);

                return new ExpressionValue(defaultValue, dataType);
            } else {
                throw unexpectedToken();
            }
        }

        e.resolveTypes(session, null);

        if (dataType.typeComparisonGroup
                != e.getDataType().typeComparisonGroup) {
            throw Error.error(ErrorCode.X_42562);
        }

        return e;
    }

    Statement compileSelectSingleRowStatement(RangeVariable[] rangeVars) {

        OrderedHashSet     variableNames = new OrderedHashSet();
        QuerySpecification select        = XreadSelect();

        readThis(Tokens.INTO);
        readColumnNamesForSelectInto(variableNames, rangeVars);
        XreadTableExpression(select);
        select.setAsTopLevel();
        select.resolve(session, rangeVars, new Type[variableNames.size()] );

        int[]          indexes   = new int[variableNames.size()];
        ColumnSchema[] variables = new ColumnSchema[variableNames.size()];

        setVariables(rangeVars, variableNames, indexes, variables);

        Statement statement = new StatementSet(session, compileContext,
                                               variables, select, indexes);

        return statement;
    }

    /**
     * Creates SET Statement for PSM or session variables from this parse context.
     */
    Statement compileSetStatement(RangeVariable rangeVars[]) {

        read();

        OrderedHashSet variableNames = new OrderedHashSet();
        HsqlArrayList  exprList      = new HsqlArrayList();

        readSetClauseList(rangeVars, variableNames, exprList);

        if (exprList.size() > 1) {
            throw Error.error(ErrorCode.X_42602);
        }

        Expression expression = (Expression) exprList.get(0);

        if (expression.getDegree() != variableNames.size()) {

//            throw Error.error(ErrorCode.X_42546);
        }

        int[]          indexes   = new int[variableNames.size()];
        ColumnSchema[] variables = new ColumnSchema[variableNames.size()];

        setVariables(rangeVars, variableNames, indexes, variables);

        HsqlList unresolved = expression.resolveColumnReferences(rangeVars,
            rangeVars.length, null, false);

        unresolved = Expression.resolveColumnSet(rangeVars, unresolved, null);

        ExpressionColumn.checkColumnsResolved(unresolved);
        expression.resolveTypes(session, null);

        StatementSet cs = new StatementSet(session, compileContext, variables,
                                           expression, indexes);

        return cs;
    }

    private static void setVariables(RangeVariable[] rangeVars,
                                     OrderedHashSet colNames, int[] indexes,
                                     ColumnSchema[] variables)
                                     throws IndexOutOfBoundsException {

        int index = -1;

        for (int i = 0; i < variables.length; i++) {
            String colName = (String) colNames.get(i);

            for (int j = 0; j < rangeVars.length; j++) {
                index = rangeVars[j].variables.getIndex(colName);

                if (index > -1) {
                    indexes[i]   = index;
                    variables[i] = rangeVars[j].getColumn(index);

                    break;
                }
            }
        }
    }

    // SQL-invoked routine
    StatementSchema compileCreateProcedureOrFunction() {

        int routineType = token.tokenType == Tokens.PROCEDURE
                          ? SchemaObject.PROCEDURE
                          : SchemaObject.FUNCTION;
        HsqlName name;

        read();

        name = readNewSchemaObjectName(routineType, false);

        Routine routine = new Routine(routineType);

        routine.setName(name);
        readThis(Tokens.OPENBRACKET);

        if (token.tokenType == Tokens.CLOSEBRACKET) {
            read();
        } else {
            while (true) {
                ColumnSchema newcolumn = readRoutineParameter(routine);

                routine.addParameter(newcolumn);

                if (token.tokenType == Tokens.COMMA) {
                    read();
                } else {
                    readThis(Tokens.CLOSEBRACKET);

                    break;
                }
            }
        }

        if (routineType != SchemaObject.PROCEDURE) {
            readThis(Tokens.RETURNS);

            if (token.tokenType == Tokens.TABLE) {
                read();

                TableDerived table =
                    new TableDerived(database, name, TableBase.FUNCTION_TABLE);

                readThis(Tokens.OPENBRACKET);

                if (token.tokenType == Tokens.CLOSEBRACKET) {
                    read();
                } else {
                    while (true) {
                        ColumnSchema newcolumn = readRoutineParameter(routine);

                        table.addColumn(newcolumn);

                        if (token.tokenType == Tokens.COMMA) {
                            read();
                        } else {
                            readThis(Tokens.CLOSEBRACKET);

                            break;
                        }
                    }
                }

                routine.setReturnTable(table);
            } else {
                Type type = readTypeDefinition(true);

                routine.setReturnType(type);
            }
        }

        readRoutineCharacteristics(routine);

        if (token.tokenType == Tokens.EXTERNAL) {
            if (routine.getLanguage() != Routine.LANGUAGE_JAVA) {
                throw unexpectedToken();
            }

            read();
            readThis(Tokens.NAME);
            checkIsValue(Types.SQL_CHAR);
            routine.setMethodURL((String) token.tokenValue);
            read();

            if (token.tokenType == Tokens.PARAMETER) {
                read();
                readThis(Tokens.STYLE);
                readThis(Tokens.JAVA);
            }
        } else {
            startRecording();

            Statement statement = compileSQLProcedureStatementOrNull(routine,
                null);
            Token[] tokenList = getRecordedStatement();
            String  sql       = Token.getSQL(tokenList);

            statement.setSQL(sql);
            routine.setProcedure(statement);
        }

        Object[] args = new Object[]{ routine };
        String   sql  = getLastPart();
        StatementSchema cs = new StatementSchema(sql,
            StatementTypes.CREATE_ROUTINE, args, null, null);

        return cs;
    }

    private void readRoutineCharacteristics(Routine routine) {

        OrderedIntHashSet set = new OrderedIntHashSet();
        boolean           end = false;

        while (!end) {
            switch (token.tokenType) {

                case Tokens.LANGUAGE : {
                    if (!set.add(Tokens.LANGUAGE)) {
                        throw unexpectedToken();
                    }

                    read();

                    if (token.tokenType == Tokens.JAVA) {
                        read();
                        routine.setLanguage(Routine.LANGUAGE_JAVA);
                    } else if (token.tokenType == Tokens.SQL) {
                        read();
                        routine.setLanguage(Routine.LANGUAGE_SQL);
                    } else {
                        throw unexpectedToken();
                    }

                    break;
                }
                case Tokens.PARAMETER : {
                    if (!set.add(Tokens.PARAMETER)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.STYLE);

                    if (token.tokenType == Tokens.JAVA) {
                        read();
                        routine.setParameterStyle(Routine.PARAM_STYLE_JAVA);
                    } else {
                        readThis(Tokens.SQL);
                        routine.setParameterStyle(Routine.PARAM_STYLE_SQL);
                    }

                    break;
                }
                case Tokens.SPECIFIC : {
                    if (!set.add(Tokens.SPECIFIC)) {
                        throw unexpectedToken();
                    }

                    read();

                    HsqlName name =
                        readNewSchemaObjectName(SchemaObject.SPECIFIC_ROUTINE,
                                                false);

                    routine.setSpecificName(name);

                    break;
                }
                case Tokens.DETERMINISTIC : {
                    if (!set.add(Tokens.DETERMINISTIC)) {
                        throw unexpectedToken();
                    }

                    read();
                    routine.setDeterministic(true);

                    break;
                }
                case Tokens.NOT : {
                    if (!set.add(Tokens.DETERMINISTIC)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.DETERMINISTIC);
                    routine.setDeterministic(false);

                    break;
                }
                case Tokens.MODIFIES : {
                    if (!set.add(Tokens.SQL)) {
                        throw unexpectedToken();
                    }

                    if (routine.getType() == SchemaObject.FUNCTION) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.SQL);
                    readThis(Tokens.DATA);
                    routine.setDataImpact(Routine.MODIFIES_SQL);

                    break;
                }
                case Tokens.NO : {
                    if (!set.add(Tokens.SQL)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.SQL);
                    routine.setDataImpact(Routine.NO_SQL);

                    break;
                }
                case Tokens.READS : {
                    if (!set.add(Tokens.SQL)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.SQL);
                    readThis(Tokens.DATA);
                    routine.setDataImpact(Routine.READS_SQL);

                    break;
                }
                case Tokens.CONTAINS : {
                    if (!set.add(Tokens.SQL)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.SQL);
                    routine.setDataImpact(Routine.CONTAINS_SQL);

                    break;
                }
                case Tokens.RETURNS : {
                    if (!set.add(Tokens.NULL) || routine.isProcedure()) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.NULL);
                    readThis(Tokens.ON);
                    readThis(Tokens.NULL);
                    readThis(Tokens.INPUT);
                    routine.setNullInputOutput(true);

                    break;
                }
                case Tokens.CALLED : {
                    if (!set.add(Tokens.NULL) || routine.isProcedure()) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.ON);
                    readThis(Tokens.NULL);
                    readThis(Tokens.INPUT);
                    routine.setNullInputOutput(false);

                    break;
                }
                case Tokens.DYNAMIC : {
                    if (!set.add(Tokens.RESULT) || routine.isFunction()) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.RESULT);
                    readThis(Tokens.SETS);
                    readBigint();

                    break;
                }
                case Tokens.NEW : {
                    if (routine.getType() == SchemaObject.FUNCTION
                            || !set.add(Tokens.SAVEPOINT)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.SAVEPOINT);
                    readThis(Tokens.LEVEL);
                    routine.setNewSavepointLevel(true);

                    break;
                }
                case Tokens.OLD : {
                    if (routine.getType() == SchemaObject.FUNCTION
                            || !set.add(Tokens.SAVEPOINT)) {
                        throw unexpectedToken();
                    }

                    read();
                    readThis(Tokens.SAVEPOINT);
                    readThis(Tokens.LEVEL);
                    routine.setNewSavepointLevel(false);

                    throw super.unsupportedFeature(Tokens.T_OLD);

                    // break;
                }
                default :
                    end = true;
                    break;
            }
        }
    }

/*
    <SQL control statement> ::=
    <call statement>
    | <return statement>

    <compound statement>
    <case statement>
    <if statement>
    <iterate statement>
    <leave statement>
    <loop statement>
    <while statement>
    <repeat statement>
   <for statement>
   <assignment statement> SET (,,,) = (,,,) or SET a = b


*/
    private Object[] readLocalDeclarationList(Routine routine,
            StatementCompound context) {

        HsqlArrayList list = new HsqlArrayList();

        while (token.tokenType == Tokens.DECLARE) {
            Object var = readLocalVariableDeclarationOrNull();

            if (var == null) {
                var = compileLocalHandlerDeclaration(routine, context);
            }

            list.add(var);
        }

        Object[] declarations = new Object[list.size()];

        list.toArray(declarations);

        return declarations;
    }

    ColumnSchema readLocalVariableDeclarationOrNull() {

        int      position = super.getPosition();
        Type     type;
        HsqlName name;

        try {
            readThis(Tokens.DECLARE);

            if (isReservedKey()) {
                rewind(position);

                return null;
            }

            name = super.readNewSchemaObjectName(SchemaObject.VARIABLE, false);
            type = readTypeDefinition(true);
        } catch (Exception e) {

            // may be cursor
            rewind(position);

            return null;
        }

        Expression def = null;

        if (token.tokenType == Tokens.DEFAULT) {
            read();

            def = readDefaultClause(type);
        }

        ColumnSchema variable = new ColumnSchema(name, type, true, false, def);

        variable.setParameterMode(SchemaObject.ParameterModes.PARAM_INOUT);
        readThis(Tokens.SEMICOLON);

        return variable;
    }

    private StatementHandler compileLocalHandlerDeclaration(Routine routine,
            StatementCompound context) {

        int handlerType;

        readThis(Tokens.DECLARE);

        switch (token.tokenType) {

            case Tokens.CONTINUE :
                read();

                handlerType = StatementHandler.CONTINUE;
                break;

            case Tokens.EXIT :
                read();

                handlerType = StatementHandler.EXIT;
                break;

            case Tokens.UNDO :
                read();

                handlerType = StatementHandler.UNDO;
                break;

            default :
                throw unexpectedToken();
        }

        readThis(Tokens.HANDLER);
        readThis(Tokens.FOR);

        StatementHandler handler = new StatementHandler(handlerType);
        boolean          end     = false;
        boolean          start   = true;

        while (!end) {
            int conditionType = StatementHandler.NONE;

            switch (token.tokenType) {

                case Tokens.COMMA :
                    if (start) {
                        throw unexpectedToken();
                    }

                    read();

                    start = true;
                    break;

                case Tokens.SQLSTATE :
                    conditionType = StatementHandler.SQL_STATE;

                // fall through
                case Tokens.SQLEXCEPTION :
                    if (conditionType == StatementHandler.NONE) {
                        conditionType = StatementHandler.SQL_EXCEPTION;
                    }

                // fall through
                case Tokens.SQLWARNING :
                    if (conditionType == StatementHandler.NONE) {
                        conditionType = StatementHandler.SQL_WARNING;
                    }

                // fall through
                case Tokens.NOT :
                    if (conditionType == StatementHandler.NONE) {
                        conditionType = StatementHandler.SQL_NOT_FOUND;
                    }

                    if (!start) {
                        throw unexpectedToken();
                    }

                    start = false;

                    read();

                    if (conditionType == StatementHandler.SQL_NOT_FOUND) {
                        readThis(Tokens.FOUND);
                    } else if (conditionType == StatementHandler.SQL_STATE) {
                        String sqlState = parseSQLStateValue();

                        handler.addConditionState(sqlState);

                        break;
                    }

                    handler.addConditionType(conditionType);
                    break;

                default :
                    if (start) {
                        throw unexpectedToken();
                    }

                    end = true;
                    break;
            }
        }

        if (token.tokenType == Tokens.SEMICOLON) {
            read();
        } else {
            Statement e = compileSQLProcedureStatementOrNull(routine, context);

            if (e == null) {
                throw unexpectedToken();
            }

            readThis(Tokens.SEMICOLON);
            handler.addStatement(e);
        }

        return handler;
    }

    String parseSQLStateValue() {

        readIfThis(Tokens.VALUE);
        checkIsValue(Types.SQL_CHAR);

        String sqlState = token.tokenString;

        if (token.tokenString.length() != 5) {
            throw Error.error(ErrorCode.X_42607);
        }

        read();

        return sqlState;
    }

    private Statement compileCompoundStatement(Routine routine,
            StatementCompound context, HsqlName label) {

        final boolean atomic = true;

        readThis(Tokens.BEGIN);
        readThis(Tokens.ATOMIC);

        StatementCompound statement =
            new StatementCompound(StatementTypes.BEGIN_END, label);

        statement.setAtomic(atomic);
        statement.setRoot(routine);
        statement.setParent(context);

        Object[] declarations = readLocalDeclarationList(routine, context);

        statement.setLocalDeclarations(declarations);

        Statement[] statements = compileSQLProcedureStatementList(routine,
            statement);

        statement.setStatements(statements);
        readThis(Tokens.END);

        if (isSimpleName() && !isReservedKey()) {
            if (label == null) {
                throw unexpectedToken();
            }

            if (!label.name.equals(token.tokenString)) {
                throw Error.error(ErrorCode.X_42508, token.tokenString);
            }

            read();
        }

        return statement;
    }

    private Statement[] compileSQLProcedureStatementList(Routine routine,
            StatementCompound context) {

        Statement     e;
        HsqlArrayList list = new HsqlArrayList();

        while (true) {
            e = compileSQLProcedureStatementOrNull(routine, context);

            if (e == null) {
                break;
            }

            readThis(Tokens.SEMICOLON);
            list.add(e);
        }

        if (list.size() == 0) {
            throw unexpectedToken();
        }

        Statement[] statements = new Statement[list.size()];

        list.toArray(statements);

        return statements;
    }

    private Statement compileSQLProcedureStatementOrNull(Routine routine,
            StatementCompound context) {

        Statement cs    = null;
        HsqlName  label = null;
        RangeVariable[] rangeVariables = context == null
                                         ? routine.getParameterRangeVariables()
                                         : context.getRangeVariables();

        if (isSimpleName() && !isReservedKey()) {
            label = readNewSchemaObjectName(SchemaObject.LABEL, false);

            readThis(Tokens.COLON);
        }

        compileContext.reset();

        switch (token.tokenType) {

            // data
            case Tokens.SELECT : {
                cs = compileSelectSingleRowStatement(rangeVariables);

                break;
            }

            // data change
            case Tokens.INSERT :
                cs = compileInsertStatement(rangeVariables);
                break;

            case Tokens.UPDATE :
                cs = compileUpdateStatement(rangeVariables);
                break;

            case Tokens.DELETE :
            case Tokens.TRUNCATE :
                cs = compileDeleteStatement(rangeVariables);
                break;

            case Tokens.MERGE :
                cs = compileMergeStatement(rangeVariables);
                break;

            case Tokens.SET :
                cs = compileSetStatement(rangeVariables);
                break;

            // control
            case Tokens.CALL : {
                if (label != null) {
                    throw unexpectedToken();
                }

                cs = compileCallStatement(rangeVariables, true);

                break;
            }
            case Tokens.RETURN : {
                if (label != null) {
                    throw unexpectedToken();
                }

                read();

                cs = compileReturnValue(routine, context);

                break;
            }
            case Tokens.BEGIN : {
                cs = compileCompoundStatement(routine, context, label);

                break;
            }
            case Tokens.WHILE : {
                cs = compileWhile(routine, context, label);

                break;
            }
            case Tokens.REPEAT : {
                cs = compileRepeat(routine, context, label);

                break;
            }
            case Tokens.LOOP : {
                cs = compileLoop(routine, context, label);

                break;
            }
            case Tokens.FOR : {
                cs = compileFor(routine, context, label);

                break;
            }
            case Tokens.ITERATE : {
                if (label != null) {
                    throw unexpectedToken();
                }

                cs = compileIterate();

                break;
            }
            case Tokens.LEAVE : {
                if (label != null) {
                    throw unexpectedToken();
                }

                cs = compileLeave(routine, context);

                break;
            }
            case Tokens.IF : {
                if (label != null) {
                    throw unexpectedToken();
                }

                cs = compileIf(routine, context);

                break;
            }
            case Tokens.CASE : {
                if (label != null) {
                    throw unexpectedToken();
                }

                cs = compileCase(routine, context);

                break;
            }
            case Tokens.SIGNAL : {
                cs = compileSignal(routine, context, label);

                break;
            }
            case Tokens.RESIGNAL : {
                cs = compileResignal(routine, context, label);

                break;
            }
            default :
                return null;
        }

        cs.setRoot(routine);
        cs.setParent(context);

        return cs;
    }

    private Statement compileReturnValue(Routine routine,
                                         StatementCompound context) {

        Expression e = XreadValueExpressionOrNull();

        if (e == null) {
            checkIsValue();

            if (token.tokenValue == null) {
                e = new ExpressionValue(null, null);
            }
        }

        resolveOuterReferences(routine, context, e);

        return new StatementExpression(session, compileContext,
                                       StatementTypes.RETURN, e);
    }

    private Statement compileIterate() {

        readThis(Tokens.ITERATE);

        HsqlName label = readNewSchemaObjectName(SchemaObject.LABEL, false);

        return new StatementSimple(StatementTypes.ITERATE, label);
    }

    private Statement compileLeave(Routine routine,
                                   StatementCompound context) {

        readThis(Tokens.LEAVE);

        HsqlName label = readNewSchemaObjectName(SchemaObject.LABEL, false);

        return new StatementSimple(StatementTypes.LEAVE, label);
    }

    private Statement compileWhile(Routine routine, StatementCompound context,
                                   HsqlName label) {

        readThis(Tokens.WHILE);

        StatementExpression condition = new StatementExpression(session,
            compileContext, StatementTypes.CONDITION,
            XreadBooleanValueExpression());

        readThis(Tokens.DO);

        Statement[] statements = compileSQLProcedureStatementList(routine,
            context);

        readThis(Tokens.END);
        readThis(Tokens.WHILE);

        if (isSimpleName() && !isReservedKey()) {
            if (label == null) {
                throw unexpectedToken();
            }

            if (!label.name.equals(token.tokenString)) {
                throw Error.error(ErrorCode.X_42508, token.tokenString);
            }

            read();
        }

        StatementCompound statement =
            new StatementCompound(StatementTypes.WHILE, label);

        statement.setStatements(statements);
        statement.setCondition(condition);

        return statement;
    }

    private Statement compileRepeat(Routine routine,
                                    StatementCompound context,
                                    HsqlName label) {

        readThis(Tokens.REPEAT);

        Statement[] statements = compileSQLProcedureStatementList(routine,
            context);

        readThis(Tokens.UNTIL);

        StatementExpression condition = new StatementExpression(session,
            compileContext, StatementTypes.CONDITION,
            XreadBooleanValueExpression());

        readThis(Tokens.END);
        readThis(Tokens.REPEAT);

        if (isSimpleName() && !isReservedKey()) {
            if (label == null) {
                throw unexpectedToken();
            }

            if (!label.name.equals(token.tokenString)) {
                throw Error.error(ErrorCode.X_42508, token.tokenString);
            }

            read();
        }

        StatementCompound statement =
            new StatementCompound(StatementTypes.REPEAT, label);

        statement.setStatements(statements);
        statement.setCondition(condition);

        return statement;
    }

    private Statement compileLoop(Routine routine, StatementCompound context,
                                  HsqlName label) {

        readThis(Tokens.LOOP);

        Statement[] statements = compileSQLProcedureStatementList(routine,
            context);

        readThis(Tokens.END);
        readThis(Tokens.LOOP);

        if (isSimpleName() && !isReservedKey()) {
            if (label == null) {
                throw unexpectedToken();
            }

            if (!label.name.equals(token.tokenString)) {
                throw Error.error(ErrorCode.X_42508, token.tokenString);
            }

            read();
        }

        StatementCompound result = new StatementCompound(StatementTypes.LOOP,
            label);

        result.setStatements(statements);

        return result;
    }

    private Statement compileFor(Routine routine, StatementCompound context,
                                 HsqlName label) {

        readThis(Tokens.FOR);

        Statement cursorStatement = compileCursorSpecification();

        readThis(Tokens.DO);

        Statement[] statements = compileSQLProcedureStatementList(routine,
            context);

        readThis(Tokens.END);
        readThis(Tokens.FOR);

        if (isSimpleName() && !isReservedKey()) {
            if (label == null) {
                throw unexpectedToken();
            }

            if (!label.name.equals(token.tokenString)) {
                throw Error.error(ErrorCode.X_42508, token.tokenString);
            }

            read();
        }

        StatementCompound result = new StatementCompound(StatementTypes.FOR,
            label);

        result.setLoopStatement(cursorStatement);
        result.setStatements(statements);

        return result;
    }

    private Statement compileIf(Routine routine, StatementCompound context) {

        HsqlArrayList list = new HsqlArrayList();

        readThis(Tokens.IF);

        Expression condition = XreadBooleanValueExpression();

        resolveOuterReferences(routine, context, condition);

        Statement statement = new StatementExpression(session, compileContext,
            StatementTypes.CONDITION, condition);

        list.add(statement);
        readThis(Tokens.THEN);

        Statement[] statements = compileSQLProcedureStatementList(routine,
            context);

        for (int i = 0; i < statements.length; i++) {
            list.add(statements[i]);
        }

        while (token.tokenType == Tokens.ELSEIF) {
            read();

            condition = XreadBooleanValueExpression();

            resolveOuterReferences(routine, context, condition);

            statement = new StatementExpression(session, compileContext,
                                                StatementTypes.CONDITION,
                                                condition);

            list.add(statement);
            readThis(Tokens.THEN);

            statements = compileSQLProcedureStatementList(routine, context);

            for (int i = 0; i < statements.length; i++) {
                list.add(statements[i]);
            }
        }

        if (token.tokenType == Tokens.ELSE) {
            read();

            condition = Expression.EXPR_TRUE;
            statement = new StatementExpression(session, compileContext,
                                                StatementTypes.CONDITION,
                                                condition);

            list.add(statement);

            statements = compileSQLProcedureStatementList(routine, context);

            for (int i = 0; i < statements.length; i++) {
                list.add(statements[i]);
            }
        }

        readThis(Tokens.END);
        readThis(Tokens.IF);

        statements = new Statement[list.size()];

        list.toArray(statements);

        StatementCompound result = new StatementCompound(StatementTypes.IF,
            null);

        result.setStatements(statements);

        return result;
    }

    private Statement compileCase(Routine routine, StatementCompound context) {

        HsqlArrayList list      = new HsqlArrayList();
        Expression    condition = null;
        Statement     statement;
        Statement[]   statements;

        readThis(Tokens.CASE);

        if (token.tokenType == Tokens.WHEN) {
            list = readCaseWhen(routine, context);
        } else {
            list = readSimpleCaseWhen(routine, context);
        }

        if (token.tokenType == Tokens.ELSE) {
            read();

            condition = Expression.EXPR_TRUE;
            statement = new StatementExpression(session, compileContext,
                                                StatementTypes.CONDITION,
                                                condition);

            list.add(statement);

            statements = compileSQLProcedureStatementList(routine, context);

            for (int i = 0; i < statements.length; i++) {
                list.add(statements[i]);
            }
        }

        readThis(Tokens.END);
        readThis(Tokens.CASE);

        statements = new Statement[list.size()];

        list.toArray(statements);

        StatementCompound result = new StatementCompound(StatementTypes.IF,
            null);

        result.setStatements(statements);

        return result;
    }

    private HsqlArrayList readSimpleCaseWhen(Routine routine,
            StatementCompound context) {

        HsqlArrayList list      = new HsqlArrayList();
        Expression    condition = null;
        Statement     statement;
        Statement[]   statements;
        Expression    predicand = XreadRowValuePredicand();

        do {
            readThis(Tokens.WHEN);

            do {
                Expression newCondition = XreadPredicateRightPart(predicand);

                if (predicand == newCondition) {
                    newCondition =
                        new ExpressionLogical(predicand,
                                              XreadRowValuePredicand());
                }

                resolveOuterReferences(routine, context, newCondition);
                newCondition.resolveTypes(session, null);

                if (condition == null) {
                    condition = newCondition;
                } else {
                    condition = new ExpressionLogical(OpTypes.OR, condition,
                                                      newCondition);
                }

                if (token.tokenType == Tokens.COMMA) {
                    read();
                } else {
                    break;
                }
            } while (true);

            statement = new StatementExpression(session, compileContext,
                                                StatementTypes.CONDITION,
                                                condition);

            list.add(statement);
            readThis(Tokens.THEN);

            statements = compileSQLProcedureStatementList(routine, context);

            for (int i = 0; i < statements.length; i++) {
                list.add(statements[i]);
            }

            if (token.tokenType != Tokens.WHEN) {
                break;
            }
        } while (true);

        return list;
    }

    private HsqlArrayList readCaseWhen(Routine routine,
                                       StatementCompound context) {

        HsqlArrayList list      = new HsqlArrayList();
        Expression    condition = null;
        Statement     statement;
        Statement[]   statements;

        do {
            readThis(Tokens.WHEN);

            condition = XreadBooleanValueExpression();

            resolveOuterReferences(routine, context, condition);

            statement = new StatementExpression(session, compileContext,
                                                StatementTypes.CONDITION,
                                                condition);

            list.add(statement);
            readThis(Tokens.THEN);

            statements = compileSQLProcedureStatementList(routine, context);

            for (int i = 0; i < statements.length; i++) {
                list.add(statements[i]);
            }

            if (token.tokenType != Tokens.WHEN) {
                break;
            }
        } while (true);

        return list;
    }

    private Statement compileSignal(Routine routine,
                                    StatementCompound context,
                                    HsqlName label) {

        readThis(Tokens.SIGNAL);
        readThis(Tokens.SQLSTATE);

        String sqlState = parseSQLStateValue();
        StatementSimple cs = new StatementSimple(StatementTypes.SIGNAL,
            sqlState);

        return cs;
    }

    private Statement compileResignal(Routine routine,
                                      StatementCompound context,
                                      HsqlName label) {

        String sqlState = null;

        readThis(Tokens.RESIGNAL);

        if (readIfThis(Tokens.SQLSTATE)) {
            sqlState = parseSQLStateValue();
        }

        StatementSimple cs = new StatementSimple(StatementTypes.RESIGNAL,
            sqlState);

        return cs;
    }

    private ColumnSchema readRoutineParameter(Routine routine) {

        HsqlName hsqlName      = null;
        byte     parameterMode = SchemaObject.ParameterModes.PARAM_IN;

        switch (token.tokenType) {

            case Tokens.IN :
                read();
                break;

            case Tokens.OUT :
                if (routine.getType() != SchemaObject.PROCEDURE) {
                    throw unexpectedToken();
                }

                read();

                parameterMode = SchemaObject.ParameterModes.PARAM_OUT;
                break;

            case Tokens.INOUT :
                if (routine.getType() != SchemaObject.PROCEDURE) {
                    throw unexpectedToken();
                }

                read();

                parameterMode = SchemaObject.ParameterModes.PARAM_INOUT;
                break;

            default :
        }

        if (!isReservedKey()) {
            hsqlName = readNewDependentSchemaObjectName(routine.getName(),
                    SchemaObject.PARAMETER);
        }

        Type typeObject = readTypeDefinition(true);
        ColumnSchema column = new ColumnSchema(hsqlName, typeObject, true,
                                               false, null);

        column.setParameterMode(parameterMode);

        return column;
    }

    void resolveOuterReferences(Routine routine, StatementCompound context,
                                Expression e) {

        RangeVariable[] rangeVars = routine.getParameterRangeVariables();

        if (context != null) {
            rangeVars = context.getRangeVariables();
        }

        HsqlList unresolved = e.resolveColumnReferences(rangeVars,
            rangeVars.length, null, false);

        unresolved = Expression.resolveColumnSet(rangeVars, unresolved, null);

        ExpressionColumn.checkColumnsResolved(unresolved);
        e.resolveTypes(session, null);
    }
}
