/* Copyright (c) 2001-2010, The HSQL Development Group
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


package org.hsqldb.types;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.hsqldb.OpTypes;
import org.hsqldb.Session;
import org.hsqldb.SessionInterface;
import org.hsqldb.Tokens;
import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.java.JavaSystem;
import org.hsqldb.store.ValuePool;

/**
 * Type subclass for all NUMBER types.<p>
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public final class NumberType extends Type {

    static final int        tinyintPrecision             = 3;
    static final int        smallintPrecision            = 5;
    static final int        integerPrecision             = 10;
    static final int        bigintPrecision              = 19;
    static final int        doublePrecision              = 0;
    public static final int defaultNumericPrecision      = 100;
    public static final int defaultNumericScale          = 10;
    public static final int maxNumericPrecision          = Integer.MAX_VALUE;
    static final int        bigintSquareNumericPrecision = 40;

    //
    static final int TINYINT_WIDTH  = 8;
    static final int SMALLINT_WIDTH = 16;
    static final int INTEGER_WIDTH  = 32;
    static final int BIGINT_WIDTH   = 64;
    static final int DOUBLE_WIDTH   = 128;    // nominal width
    static final int DECIMAL_WIDTH  = 256;    // nominal width

    //
    public static final Type SQL_NUMERIC_DEFAULT_INT =
        new NumberType(Types.NUMERIC, defaultNumericPrecision, 0);

    //
    public static final BigDecimal MAX_LONG =
        BigDecimal.valueOf(Long.MAX_VALUE);
    public static final BigDecimal MIN_LONG =
        BigDecimal.valueOf(Long.MIN_VALUE);
    public static final BigDecimal MAX_INT =
        BigDecimal.valueOf(Integer.MAX_VALUE);
    public static final BigDecimal MIN_INT =
        BigDecimal.valueOf(Integer.MIN_VALUE);

    //
    public static final BigInteger MIN_LONG_BI = MIN_LONG.toBigInteger();
    public static final BigInteger MAX_LONG_BI = MAX_LONG.toBigInteger();

    //
    final int typeWidth;

    public NumberType(int type, long precision, int scale) {

        super(Types.SQL_NUMERIC, type, precision, scale);

        switch (type) {

            case Types.TINYINT :
                typeWidth = TINYINT_WIDTH;
                break;

            case Types.SQL_SMALLINT :
                typeWidth = SMALLINT_WIDTH;
                break;

            case Types.SQL_INTEGER :
                typeWidth = INTEGER_WIDTH;
                break;

            case Types.SQL_BIGINT :
                typeWidth = BIGINT_WIDTH;
                break;

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                typeWidth = DOUBLE_WIDTH;
                break;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                typeWidth = DECIMAL_WIDTH;
                break;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    /**
     * Returns decimal precision for NUMERIC/DECIMAL. Returns binary precision
     * for other parts.
     */
    public int getPrecision() {

        switch (typeCode) {

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
            case Types.SQL_BIGINT :
                return typeWidth;

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return 64;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return (int) precision;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public int displaySize() {

        switch (typeCode) {

            case Types.SQL_DECIMAL :
            case Types.SQL_NUMERIC :
                if (scale == 0) {
                    if (precision == 0) {
                        return 646456995;    // precision + "-.".length()}
                    }

                    return (int) precision + 1;
                }

                if (precision == scale) {
                    return (int) precision + 3;
                }

                return (int) precision + 2;

            case Types.SQL_FLOAT :
            case Types.SQL_REAL :
            case Types.SQL_DOUBLE :
                return 23;                   // String.valueOf(-Double.MAX_VALUE).length();

            case Types.SQL_BIGINT :
                return 20;                   // decimal precision + "-".length();

            case Types.SQL_INTEGER :
                return 11;                   // decimal precision + "-".length();

            case Types.SQL_SMALLINT :
                return 6;                    // decimal precision + "-".length();

            case Types.TINYINT :
                return 4;                    // decimal precision + "-".length();

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public int getJDBCTypeCode() {
        return typeCode == Types.SQL_BIGINT ? Types.BIGINT
                                            : typeCode;
    }

    public String getJDBCClassName() {

        switch (typeCode) {

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
            case Types.SQL_BIGINT :
                return "java.lang.Integer";

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return "java.lang.Double";

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return "java.math.BigDecimal";

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public String getNameString() {

        switch (typeCode) {

            case Types.TINYINT :
                return Tokens.T_TINYINT;

            case Types.SQL_SMALLINT :
                return Tokens.T_SMALLINT;

            case Types.SQL_INTEGER :
                return Tokens.T_INTEGER;

            case Types.SQL_BIGINT :
                return Tokens.T_BIGINT;

            case Types.SQL_REAL :
                return Tokens.T_REAL;

            case Types.SQL_FLOAT :
                return Tokens.T_FLOAT;

            case Types.SQL_DOUBLE :
                return Tokens.T_DOUBLE;

            case Types.SQL_NUMERIC :
                return Tokens.T_NUMERIC;

            case Types.SQL_DECIMAL :
                return Tokens.T_DECIMAL;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public String getFullNameString() {

        switch (typeCode) {

            case Types.SQL_DOUBLE :
                return "DOUBLE PRECISION";

            default :
                return getNameString();
        }
    }

    public String getDefinition() {

        switch (typeCode) {

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                StringBuffer sb = new StringBuffer(16);

                sb.append(getNameString());
                sb.append('(');
                sb.append(precision);

                if (scale != 0) {
                    sb.append(',');
                    sb.append(scale);
                }

                sb.append(')');

                return sb.toString();

            default :
                return getNameString();
        }
    }

    public long getMaxPrecision() {

        switch (typeCode) {

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return maxNumericPrecision;

            default :
                return getNumericPrecisionInRadix();
        }
    }

    public int getMaxScale() {

        switch (typeCode) {

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return Short.MAX_VALUE;

            default :
                return 0;
        }
    }

    public boolean acceptsPrecision() {

        switch (typeCode) {

            case Types.SQL_DECIMAL :
            case Types.SQL_NUMERIC :
            case Types.SQL_FLOAT :
                return true;

            default :
                return false;
        }
    }

    public boolean acceptsScale() {

        switch (typeCode) {

            case Types.SQL_DECIMAL :
            case Types.SQL_NUMERIC :
                return true;

            default :
                return false;
        }
    }

    public int getPrecisionRadix() {

        if (typeCode == Types.SQL_DECIMAL || typeCode == Types.SQL_NUMERIC) {
            return 10;
        }

        return 2;
    }

    public boolean isNumberType() {
        return true;
    }

    public boolean isIntegralType() {

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return false;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return scale == 0;

            default :
                return true;
        }
    }

    public boolean isExactNumberType() {

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return false;

            default :
                return true;
        }
    }

    public int precedenceDegree(Type other) {

        if (other.isNumberType()) {
            int otherWidth = ((NumberType) other).typeWidth;

            return otherWidth - typeWidth;
        }

        return Integer.MIN_VALUE;
    }

    public Type getAggregateType(Type other) {

        if (this == other) {
            return this;
        }

        if (other.isCharacterType()) {
            return other.getAggregateType(this);
        }

        switch (other.typeCode) {

            case Types.SQL_ALL_TYPES :
                return this;

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
            case Types.SQL_BIGINT :
                break;

            default :
                throw Error.error(ErrorCode.X_42562);
        }

        if (typeWidth == DOUBLE_WIDTH) {
            return this;
        }

        if (((NumberType) other).typeWidth == DOUBLE_WIDTH) {
            return other;
        }

        if (typeWidth <= BIGINT_WIDTH
                && ((NumberType) other).typeWidth <= BIGINT_WIDTH) {
            return (typeWidth > ((NumberType) other).typeWidth) ? this
                                                                : other;
        }

        int newScale = scale > other.scale ? scale
                                           : other.scale;
        long newDigits = precision - scale > other.precision - other.scale
                         ? precision - scale
                         : other.precision - other.scale;

        return getNumberType(Types.SQL_DECIMAL, newDigits + newScale,
                             newScale);
    }

    /**
     *  Returns a SQL type "wide" enough to represent the result of the
     *  expression.<br>
     *  A type is "wider" than the other if it can represent all its
     *  numeric values.<BR>
     *  Arithmetic operation terms are promoted to a type that can
     *  represent the resulting values and avoid incorrect results.<p>
     *  FLOAT/REAL/DOUBLE used in an operation results in the same type,
     *  regardless of the type of the other operand.
     *  When the result or the expression is converted to the
     *  type of the target column for storage, an exception is thrown if the
     *  resulting value cannot be stored in the column<p>
     *  Types narrower than INTEGER (int) are promoted to
     *  INTEGER. The order of promotion is as follows<p>
     *
     *  INTEGER, BIGINT, NUMERIC/DECIMAL<p>
     *
     *  TINYINT and SMALLINT in any combination return INTEGER<br>
     *  TINYINT/SMALLINT/INTEGER and INTEGER return BIGINT<br>
     *  TINYINT/SMALLINT/INTEGER and BIGINT return NUMERIC/DECIMAL<br>
     *  BIGINT and BIGINT return NUMERIC/DECIMAL<br>
     *  REAL/FLOAT/DOUBLE and any type return REAL/FLOAT/DOUBLE<br>
     *  NUMERIC/DECIMAL any type other than REAL/FLOAT/DOUBLE returns NUMERIC/DECIMAL<br>
     *  In the case of NUMERIC/DECIMAL returned, the result precision is always
     *  large enough to express any value result, while the scale depends on the
     *  operation:<br>
     *  For ADD/SUBTRACT/DIVIDE, the scale is the larger of the two<br>
     *  For MULTIPLY, the scale is the sum of the two scales<br>
     */
    public Type getCombinedType(Type other, int operation) {

        if (other.typeCode == Types.SQL_ALL_TYPES) {
            other = this;
        }

        switch (operation) {

            case OpTypes.ADD :
                break;

            case OpTypes.MULTIPLY :
                if (other.isIntervalType()) {
                    return other.getCombinedType(this, OpTypes.MULTIPLY);
                }
                break;

            case OpTypes.DIVIDE :
                if (typeWidth == DECIMAL_WIDTH) {
                    break;
                }
            case OpTypes.SUBTRACT :
            default :

                // all derivatives of equality ops or comparison ops
                return getAggregateType(other);
        }

        // resolution for ADD and MULTIPLY only
        if (!other.isNumberType()) {
            throw Error.error(ErrorCode.X_42562);
        }

        if (typeWidth == DOUBLE_WIDTH
                || ((NumberType) other).typeWidth == DOUBLE_WIDTH) {
            return Type.SQL_DOUBLE;
        }

        int sum = typeWidth + ((NumberType) other).typeWidth;

        if (sum <= INTEGER_WIDTH) {
            return Type.SQL_INTEGER;
        }

        if (sum <= BIGINT_WIDTH) {
            return Type.SQL_BIGINT;
        }

        int  newScale;
        long newDigits;

        switch (operation) {

            case OpTypes.ADD :
                newScale = scale > other.scale ? scale
                                               : other.scale;
                newDigits = precision - scale > other.precision - other.scale
                            ? precision - scale
                            : other.precision - other.scale;

                newDigits++;
                break;

            case OpTypes.DIVIDE :
                newDigits = precision - scale + other.scale;
                newScale  = scale > other.scale ? scale
                                                : other.scale;
                break;

            case OpTypes.MULTIPLY :
                newDigits = precision - scale + other.precision - other.scale;
                newScale  = scale + other.scale;
                break;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }

        return getNumberType(Types.SQL_DECIMAL, newScale + newDigits,
                             newScale);
    }

    public int compare(Session session, Object a, Object b) {

        if (a == b) {
            return 0;
        }

        if (a == null) {
            return -1;
        }

        if (b == null) {
            return 1;
        }

        switch (typeCode) {

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER : {
                if (b instanceof Integer) {
                    int ai = ((Number) a).intValue();
                    int bi = ((Number) b).intValue();

                    return (ai > bi) ? 1
                                     : (bi > ai ? -1
                                                : 0);
                } else if (b instanceof Double) {
                    double ai = ((Number) a).doubleValue();
                    double bi = ((Number) b).doubleValue();

                    return (ai > bi) ? 1
                                     : (bi > ai ? -1
                                                : 0);
                } else if (b instanceof BigDecimal) {
                    BigDecimal ad = convertToDecimal(a);
                    int        i  = ad.compareTo((BigDecimal) b);

                    return (i == 0) ? 0
                                    : (i < 0 ? -1
                                             : 1);
                }
            }

            // fall through
            case Types.SQL_BIGINT : {
                if (b instanceof Long) {
                    long longa = ((Number) a).longValue();
                    long longb = ((Number) b).longValue();

                    return (longa > longb) ? 1
                                           : (longb > longa ? -1
                                                            : 0);
                } else if (b instanceof Double) {
                    BigDecimal ad =
                        BigDecimal.valueOf(((Number) a).longValue());
                    BigDecimal bd = new BigDecimal(((Double) b).doubleValue());
                    int        i  = ad.compareTo(bd);

                    return (i == 0) ? 0
                                    : (i < 0 ? -1
                                             : 1);
                } else if (b instanceof BigDecimal) {
                    BigDecimal ad =
                        BigDecimal.valueOf(((Number) a).longValue());
                    int i = ad.compareTo((BigDecimal) b);

                    return (i == 0) ? 0
                                    : (i < 0 ? -1
                                             : 1);
                }
            }

            // fall through
            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {

                /** @todo big-decimal etc */
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return (ad > bd) ? 1
                                 : (bd > ad ? -1
                                            : 0);
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                BigDecimal bd = convertToDecimal(b);
                int        i  = ((BigDecimal) a).compareTo(bd);

                return (i == 0) ? 0
                                : (i < 0 ? -1
                                         : 1);
            }
            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    /** @todo - review usage to see if range enforcement / java type conversion is necessary */
    public Object convertToTypeLimits(SessionInterface session, Object a) {

        if (a == null) {
            return null;
        }

        switch (typeCode) {

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
            case Types.SQL_BIGINT :
                return a;

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return a;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                BigDecimal dec = (BigDecimal) a;

                if (scale != dec.scale()) {
                    dec = dec.setScale(scale, BigDecimal.ROUND_HALF_DOWN);
                }

                int valuePrecision = JavaSystem.precision(dec);

                if (valuePrecision > precision) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return dec;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public Object convertToType(SessionInterface session, Object a,
                                Type otherType) {

        if (a == null) {
            return a;
        }

        if (otherType.typeCode == typeCode) {
            switch (typeCode) {

                case Types.SQL_NUMERIC :
                case Types.SQL_DECIMAL :
                    if (otherType.scale == scale
                            && otherType.precision <= precision) {
                        return a;
                    }
                    break;

                default :
                    return a;
            }
        }

        if (otherType.isIntervalType()) {
            int endType = ((IntervalType) otherType).endIntervalType;

            switch (endType) {

                case Types.SQL_INTERVAL_YEAR :
                case Types.SQL_INTERVAL_MONTH :
                case Types.SQL_INTERVAL_DAY :
                case Types.SQL_INTERVAL_HOUR :
                case Types.SQL_INTERVAL_MINUTE : {
                    Long value = ValuePool.getLong(
                        ((IntervalType) otherType).convertToLong(a));

                    return convertToType(session, value, Type.SQL_BIGINT);
                }
                case Types.SQL_INTERVAL_SECOND : {
                    long seconds = ((IntervalSecondData) a).units;
                    long nanos   = ((IntervalSecondData) a).nanos;
                    BigDecimal value =
                        ((DTIType) otherType).getSecondPart(seconds, nanos);

                    return value;
                }
            }
        }

        switch (otherType.typeCode) {

            case Types.SQL_CLOB :
                a = ((ClobData) a).getSubString(
                    session, 0L, (int) ((ClobData) a).length(session));

            // fall through
            case Types.SQL_CHAR :
            case Types.SQL_VARCHAR :
            case Types.VARCHAR_IGNORECASE : {
                a = session.getScanner().convertToNumber((String) a, this);

                return convertToDefaultType(session, a);
            }
            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
            case Types.SQL_BIGINT :
            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                break;

            case Types.SQL_BIT :
            case Types.SQL_BIT_VARYING :
                if (otherType.precision == 1) {
                    if (((BinaryData) a).getBytes()[0] == 0) {
                        a = ValuePool.INTEGER_0;
                    } else {
                        a = ValuePool.INTEGER_1;
                    }

                    break;
                }
            default :
                throw Error.error(ErrorCode.X_42561);
        }

        switch (this.typeCode) {

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
                return convertToInt(a, this.typeCode);

            case Types.SQL_BIGINT :
                return convertToLong(a);

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return convertToDouble(a);

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                BigDecimal value = convertToDecimal(a);

                return convertToTypeLimits(session, value);

            default :
                throw Error.error(ErrorCode.X_42561);
        }
    }

    public Object convertToTypeJDBC(SessionInterface session, Object a,
                                    Type otherType) {

        if (a == null) {
            return a;
        }

        if (otherType.isLobType()) {
            throw Error.error(ErrorCode.X_42561);
        }

        switch (otherType.typeCode) {

            case Types.SQL_BOOLEAN :
                a         = ((Boolean) a).booleanValue() ? ValuePool.INTEGER_1
                                                         : ValuePool.INTEGER_0;
                otherType = Type.SQL_INTEGER;
        }

        return convertToType(session, a, otherType);
    }

    /**
     * Converts a value to this type
     */
    public Object convertToDefaultType(SessionInterface session, Object a) {

        if (a == null) {
            return a;
        }

        Type otherType;

        if (a instanceof Number) {
            if (a instanceof BigInteger) {
                a = new BigDecimal((BigInteger) a);
            } else if (a instanceof Float) {
                a = new Double(((Float) a).doubleValue());
            } else if (a instanceof Byte) {
                a = ValuePool.getInt(((Byte) a).intValue());
            } else if (a instanceof Short) {
                a = ValuePool.getInt(((Short) a).intValue());
            }

            if (a instanceof Integer) {
                otherType = Type.SQL_INTEGER;
            } else if (a instanceof Long) {
                otherType = Type.SQL_BIGINT;
            } else if (a instanceof Double) {
                otherType = Type.SQL_DOUBLE;
            } else if (a instanceof BigDecimal) {
                if (typeCode == Types.SQL_DECIMAL
                        || typeCode == Types.SQL_NUMERIC) {
                    return convertToTypeLimits(session, a);
                }

                BigDecimal val = (BigDecimal) a;

                otherType = getNumberType(Types.SQL_DECIMAL,
                                          JavaSystem.precision(val), scale);
            } else {
                throw Error.error(ErrorCode.X_42561);
            }
        } else if (a instanceof String) {
            otherType = Type.SQL_VARCHAR;
        } else {
            throw Error.error(ErrorCode.X_42561);
        }

        return convertToType(session, a, otherType);
    }

    /**
     * Type narrowing from DOUBLE/DECIMAL/NUMERIC to BIGINT / INT / SMALLINT / TINYINT
     * following SQL rules. When conversion is from a non-integral type,
     * digits to the right of the decimal point are lost.
     */

    /**
     * Converter from a numeric object to Integer. Input is checked to be
     * within range represented by the given number type.
     */
    static Integer convertToInt(Object a, int type) {

        int value;

        if (a instanceof Integer) {
            if (type == Types.SQL_INTEGER) {
                return (Integer) a;
            }

            value = ((Integer) a).intValue();
        } else if (a instanceof Long) {
            long temp = ((Long) a).longValue();

            if (Integer.MAX_VALUE < temp || temp < Integer.MIN_VALUE) {
                throw Error.error(ErrorCode.X_22003);
            }

            value = (int) temp;
        } else if (a instanceof BigDecimal) {
            BigDecimal bd = ((BigDecimal) a);

            if (bd.compareTo(MAX_INT) > 0 || bd.compareTo(MIN_INT) < 0) {
                throw Error.error(ErrorCode.X_22003);
            }

            value = bd.intValue();
        } else if (a instanceof Double || a instanceof Float) {
            double d = ((Number) a).doubleValue();

            if (Double.isInfinite(d) || Double.isNaN(d)
                    || d >= (double) Integer.MAX_VALUE + 1
                    || d <= (double) Integer.MIN_VALUE - 1) {
                throw Error.error(ErrorCode.X_22003);
            }

            value = (int) d;
        } else {
            throw Error.error(ErrorCode.X_42561);
        }

        if (type == Types.TINYINT) {
            if (Byte.MAX_VALUE < value || value < Byte.MIN_VALUE) {
                throw Error.error(ErrorCode.X_22003);
            }
        } else if (type == Types.SQL_SMALLINT) {
            if (Short.MAX_VALUE < value || value < Short.MIN_VALUE) {
                throw Error.error(ErrorCode.X_22003);
            }
        }

        return ValuePool.getInt(value);
    }

    /**
     * Converter from a numeric object to Long. Input is checked to be
     * within range represented by Long.
     */
    static Long convertToLong(Object a) {

        if (a instanceof Integer) {
            return ValuePool.getLong(((Integer) a).intValue());
        } else if (a instanceof Long) {
            return (Long) a;
        } else if (a instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) a;

            if (bd.compareTo(MAX_LONG) > 0 || bd.compareTo(MIN_LONG) < 0) {
                throw Error.error(ErrorCode.X_22003);
            }

            return ValuePool.getLong(bd.longValue());
        } else if (a instanceof Double || a instanceof Float) {
            double d = ((Number) a).doubleValue();

            if (Double.isInfinite(d) || Double.isNaN(d)
                    || d >= (double) Long.MAX_VALUE + 1
                    || d <= (double) Long.MIN_VALUE - 1) {
                throw Error.error(ErrorCode.X_22003);
            }

            return ValuePool.getLong((long) d);
        } else {
            throw Error.error(ErrorCode.X_42561);
        }
    }

    /**
     * Converter from a numeric object to Double. Input is checked to be
     * within range represented by Double
     */
    private static Double convertToDouble(Object a) {

        double value;

        if (a instanceof java.lang.Double) {
            return (Double) a;
        } else if (a instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) a;

            value = bd.doubleValue();

            int        signum = bd.signum();
            BigDecimal bdd    = new BigDecimal(value + signum);

            if (bdd.compareTo(bd) != signum) {
                throw Error.error(ErrorCode.X_22003);
            }
        } else {
            value = ((Number) a).doubleValue();
        }

        return ValuePool.getDouble(Double.doubleToLongBits(value));
    }

    public static double toDouble(Object a) {

        double value;

        if (a instanceof java.lang.Double) {
            return ((Double) a).doubleValue();
        } else if (a instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) a;

            value = bd.doubleValue();

            int        signum = bd.signum();
            BigDecimal bdd    = new BigDecimal(value + signum);

            if (bdd.compareTo(bd) != signum) {
                throw Error.error(ErrorCode.X_22003);
            }
        } else if (a instanceof Number) {
            value = ((Number) a).doubleValue();
        } else {
            throw Error.error(ErrorCode.X_22501);
        }

        return value;
    }

    private static BigDecimal convertToDecimal(Object a) {

        if (a instanceof BigDecimal) {
            return (BigDecimal) a;
        } else if (a instanceof Integer || a instanceof Long) {
            return BigDecimal.valueOf(((Number) a).longValue());
        } else if (a instanceof Double) {
            double value = ((Number) a).doubleValue();

            if (Double.isInfinite(value) || Double.isNaN(value)) {
                return null;
            }

            return new BigDecimal(value);
        } else {
            throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public String convertToString(Object a) {

        if (a == null) {
            return null;
        }

        switch (this.typeCode) {

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
            case Types.SQL_BIGINT :
                return a.toString();

            case Types.SQL_REAL :
            case Types.SQL_DOUBLE :
                double value = ((Double) a).doubleValue();

                /** @todo - java 5 format change */
                if (value == Double.NEGATIVE_INFINITY) {
                    return "-1E0/0";
                }

                if (value == Double.POSITIVE_INFINITY) {
                    return "1E0/0";
                }

                if (Double.isNaN(value)) {
                    return "0E0/0E0";
                }

                String s = Double.toString(value);

                // ensure the engine treats the value as a DOUBLE, not DECIMAL
                if (s.indexOf('E') < 0) {
                    s = s.concat("E0");
                }

                return s;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return JavaSystem.toString((BigDecimal) a);

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public String convertToSQLString(Object a) {

        if (a == null) {
            return Tokens.T_NULL;
        }

        return convertToString(a);
    }

    public boolean canConvertFrom(Type otherType) {

        if (otherType.typeCode == Types.SQL_ALL_TYPES) {
            return true;
        }

        if (otherType.isNumberType()) {
            return true;
        }

        if (otherType.isIntervalType()) {
            return true;
        }

        if (otherType.isCharacterType()) {
            return true;
        }

        if (otherType.isBitType() && otherType.precision == 1) {
            return true;
        }

        return false;
    }

    public int compareToTypeRange(Object o) {

        if (!(o instanceof Number)) {
            return 0;
        }

        if (o instanceof Integer || o instanceof Long) {
            long temp = ((Number) o).longValue();
            int  min;
            int  max;

            switch (typeCode) {

                case Types.TINYINT :
                    min = Byte.MIN_VALUE;
                    max = Byte.MAX_VALUE;
                    break;

                case Types.SQL_SMALLINT :
                    min = Short.MIN_VALUE;
                    max = Short.MAX_VALUE;
                    break;

                case Types.SQL_INTEGER :
                    min = Integer.MIN_VALUE;
                    max = Integer.MAX_VALUE;
                    break;

                case Types.SQL_BIGINT :
                    return 0;

                case Types.SQL_DECIMAL :
                case Types.SQL_NUMERIC :
                default :
                    return 0;
            }

            if (max < temp) {
                return 1;
            }

            if (temp < min) {
                return -1;
            }

            return 0;
        }

        return 0;
    }

    public Object add(Object a, Object b, Type otherType) {

        if (a == null || b == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad + bd));

//                return new Double(ad + bd);
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                a = convertToDefaultType(null, a);
                b = convertToDefaultType(null, b);

                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.add(bbd);
            }
            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return ValuePool.getInt(ai + bi);
            }
            case Types.SQL_BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return ValuePool.getLong(longa + longb);
            }
            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public Object subtract(Object a, Object b, Type otherType) {

        if (a == null || b == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad - bd));
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                a = convertToDefaultType(null, a);
                b = convertToDefaultType(null, b);

                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                return abd.subtract(bbd);
            }
            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return ValuePool.getInt(ai - bi);
            }
            case Types.SQL_BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return ValuePool.getLong(longa - longb);
            }
            default :
        }

        throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
    }

    public Object multiply(Object a, Object b) {

        if (a == null || b == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad * bd));
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                if (!(a instanceof BigDecimal)) {
                    a = convertToDefaultType(null, a);
                }

                if (!(b instanceof BigDecimal)) {
                    b = convertToDefaultType(null, b);
                }

                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;
                BigDecimal bd  = abd.multiply(bbd);

                return convertToDefaultType(null, bd);
            }
            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                return ValuePool.getInt(ai * bi);
            }
            case Types.SQL_BIGINT : {
                long longa = ((Number) a).longValue();
                long longb = ((Number) b).longValue();

                return ValuePool.getLong(longa * longb);
            }
            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public Object divide(Object a, Object b) {

        if (a == null || b == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = ((Number) a).doubleValue();
                double bd = ((Number) b).doubleValue();

                if (bd == 0) {
                    throw Error.error(ErrorCode.X_22012);
                }

                return ValuePool.getDouble(Double.doubleToLongBits(ad / bd));
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                if (!(a instanceof BigDecimal)) {
                    a = convertToDefaultType(null, a);
                }

                if (!(b instanceof BigDecimal)) {
                    b = convertToDefaultType(null, b);
                }

                BigDecimal abd = (BigDecimal) a;
                BigDecimal bbd = (BigDecimal) b;

                if (bbd.signum() == 0) {
                    throw Error.error(ErrorCode.X_22012);
                }

                BigDecimal bd = abd.divide(bbd, scale, BigDecimal.ROUND_DOWN);

                return convertToDefaultType(null, bd);
            }
            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER : {
                int ai = ((Number) a).intValue();
                int bi = ((Number) b).intValue();

                if (bi == 0) {
                    throw Error.error(ErrorCode.X_22012);
                }

                return ValuePool.getInt(ai / bi);
            }
            case Types.SQL_BIGINT : {
                long al = ((Number) a).longValue();
                long bl = ((Number) b).longValue();

                if (bl == 0) {
                    throw Error.error(ErrorCode.X_22012);
                }

                return ValuePool.getLong(al / bl);
            }
            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public Object modulo(Object a, Object b, Type otherType) {

        if (!otherType.isNumberType()) {
            throw Error.error(ErrorCode.X_42561);
        }

        Object temp = divide(a, b);

        temp = multiply(temp, b);
        temp = convertToDefaultType(null, temp);
        temp = subtract(a, temp, this);

        return temp;
    }

    public Object absolute(Object a) {
        return isNegative(a) ? negate(a)
                             : a;
    }

    public Object negate(Object a) {

        if (a == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = -((Number) a).doubleValue();

                return ValuePool.getDouble(Double.doubleToLongBits(ad));
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return ((BigDecimal) a).negate();

            case Types.TINYINT : {
                int value = ((Number) a).intValue();

                if (value == Byte.MIN_VALUE) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return ValuePool.getInt(-value);
            }
            case Types.SQL_SMALLINT : {
                int value = ((Number) a).intValue();

                if (value == Short.MIN_VALUE) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return ValuePool.getInt(-value);
            }
            case Types.SQL_INTEGER : {
                int value = ((Number) a).intValue();

                if (value == Integer.MIN_VALUE) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return ValuePool.getInt(-value);
            }
            case Types.SQL_BIGINT : {
                long value = ((Number) a).longValue();

                if (value == Long.MIN_VALUE) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return ValuePool.getLong(-value);
            }
            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public int getNumericPrecisionInRadix() {

        switch (typeCode) {

            case Types.TINYINT :
                return 8;

            case Types.SQL_SMALLINT :
                return 16;

            case Types.SQL_INTEGER :
                return 32;

            case Types.SQL_BIGINT :
                return 64;

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return 64;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return (int) precision;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public Type getIntegralType() {

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE :
                return SQL_NUMERIC_DEFAULT_INT;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return scale == 0 ? this
                                  : new NumberType(typeCode, precision, 0);

            default :
                return this;
        }
    }

    public static boolean isZero(Object a) {

        if (a instanceof BigDecimal) {
            return ((BigDecimal) a).signum() == 0;
        } else if (a instanceof Double) {
            return ((Double) a).doubleValue() == 0 || ((Double) a).isNaN();
        } else {
            return ((Number) a).longValue() == 0;
        }
    }

    public boolean isNegative(Object a) {

        if (a == null) {
            return false;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = ((Number) a).doubleValue();

                return ad < 0;
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return ((BigDecimal) a).signum() < 0;

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER :
                return ((Number) a).intValue() < 0;

            case Types.SQL_BIGINT :
                return ((Number) a).longValue() < 0;

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public int compareToZero(Object a) {

        if (a == null) {
            return 0;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = ((Number) a).doubleValue();

                return ad == 0 ? 0
                               : ad < 0 ? -1
                                        : 1;
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return ((BigDecimal) a).signum();

            case Types.TINYINT :
            case Types.SQL_SMALLINT :
            case Types.SQL_INTEGER : {
                int ai = ((Number) a).intValue();

                return ai == 0 ? 0
                               : ai < 0 ? -1
                                        : 1;
            }
            case Types.SQL_BIGINT : {
                long al = ((Number) a).longValue();

                return al == 0 ? 0
                               : al < 0 ? -1
                                        : 1;
            }
            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }

    public static long scaledDecimal(Object a, int scale) {

        if (a == null) {
            return 0;
        }

        if (scale == 0) {
            return 0;
        }

        BigDecimal value = ((BigDecimal) a);

        if (value.scale() == 0) {
            return 0;
        }

        value = value.setScale(0, BigDecimal.ROUND_FLOOR);
        value = ((BigDecimal) a).subtract(value);

        return value.movePointRight(scale).longValue();
    }

    public static boolean isInLongLimits(BigDecimal result) {

        if (NumberType.MIN_LONG.compareTo(result) > 0
                || NumberType.MAX_LONG.compareTo(result) < 0) {
            return false;
        }

        return true;
    }

    public static boolean isInLongLimits(BigInteger result) {

        if (MAX_LONG_BI.compareTo(result) < 0
                || MIN_LONG_BI.compareTo(result) > 0) {
            return false;
        }

        return true;
    }

    public Object ceiling(Object a) {

        if (a == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double ad = Math.ceil(((Double) a).doubleValue());

                if (Double.isInfinite(ad)) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return ValuePool.getDouble(Double.doubleToLongBits(ad));
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                BigDecimal value = ((BigDecimal) a).setScale(0,
                    BigDecimal.ROUND_CEILING);

                if (JavaSystem.precision(value) > precision) {
                    throw Error.error(ErrorCode.X_22003);
                }
            }

            // fall through
            default :
                return a;
        }
    }

    public Object floor(Object a) {

        if (a == null) {
            return null;
        }

        switch (typeCode) {

            case Types.SQL_REAL :
            case Types.SQL_FLOAT :
            case Types.SQL_DOUBLE : {
                double value = Math.floor(((Double) a).doubleValue());

                if (Double.isInfinite(value)) {
                    throw Error.error(ErrorCode.X_22003);
                }

                return ValuePool.getDouble(Double.doubleToLongBits(value));
            }
            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL : {
                BigDecimal value = ((BigDecimal) a).setScale(0,
                    BigDecimal.ROUND_FLOOR);

                if (JavaSystem.precision(value) > precision) {
                    throw Error.error(ErrorCode.X_22003);
                }
            }

            // fall through
            default :
                return a;
        }
    }

    public Object truncate(Object a, int s) {

        if (a == null) {
            return null;
        }

        if (s >= scale) {
            return a;
        }

        BigDecimal dec = convertToDecimal(a);

        dec = dec.setScale(s, BigDecimal.ROUND_DOWN);
        dec = dec.setScale(scale, BigDecimal.ROUND_DOWN);

        return convertToDefaultType(null, dec);
    }

    public static NumberType getNumberType(int type, long precision,
                                           int scale) {

        switch (type) {

            case Types.SQL_INTEGER :
                return SQL_INTEGER;

            case Types.SQL_SMALLINT :
                return SQL_SMALLINT;

            case Types.SQL_BIGINT :
                return SQL_BIGINT;

            case Types.TINYINT :
                return TINYINT;

            case Types.SQL_REAL :
            case Types.SQL_DOUBLE :
                return SQL_DOUBLE;

            case Types.SQL_NUMERIC :
            case Types.SQL_DECIMAL :
                return new NumberType(type, precision, scale);

            default :
                throw Error.runtimeError(ErrorCode.U_S0500, "NumberType");
        }
    }
}
