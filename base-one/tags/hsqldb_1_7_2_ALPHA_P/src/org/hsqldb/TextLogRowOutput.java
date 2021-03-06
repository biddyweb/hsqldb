/* Copyright (c) 2001-2002, The HSQL Development Group
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

import java.sql.*;
import java.math.*;
import java.io.IOException;
import org.hsqldb.lib.StringConverter;

class TextLogRowOutput extends DatabaseRowOutput {

    final static byte[] BYTES_NULL  = "NULL".getBytes();
    final static byte[] BYTES_TRUE  = "TRUE".getBytes();
    final static byte[] BYTES_FALSE = "FALSE".getBytes();
    final static byte[] BYTES_AND   = " AND ".getBytes();
    final static int    MODE_DELETE = 1;
    final static int    MODE_INSERT = 0;
    private boolean     isWritten;
    private int         logMode;

    void setMode(int mode) {
        logMode = mode;
    }

    protected void writeFieldPrefix() throws IOException {

        if (logMode == MODE_DELETE && isWritten) {
            write(BYTES_AND);
        }
    }

    protected void writeChar(String s, int t) throws IOException {

        write('\'');
        StringConverter.unicodeToAscii(this, s, true);
        write('\'');
    }

    protected void writeReal(Double o,
                             int type) throws IOException, HsqlException {
        writeBytes(Column.createSQLString(((Number) o).doubleValue()));
    }

    protected void writeSmallint(Number o) throws IOException, HsqlException {
        this.writeBytes(o.toString());
    }

    public void writePos(int pos) throws IOException {}

    protected void writeTime(Time o) throws IOException, HsqlException {

        write('\'');
        writeBytes(o.toString());
        write('\'');
    }

    protected void writeBinary(Binary o,
                               int t) throws IOException, HsqlException {

        write('\'');
        writeBytes(StringConverter.byteToHex(o.getBytes()));
        write('\'');
    }

    public void writeType(int type) throws IOException {}

    public void writeSize(int size) throws IOException {}

    protected void writeDate(Date o) throws IOException, HsqlException {

        write('\'');
        this.writeBytes(o.toString());
        write('\'');
    }

    public int getSize(CachedRow row) throws HsqlException {
        return 0;
    }

    protected void writeInteger(Number o) throws IOException, HsqlException {
        this.writeBytes(o.toString());
    }

    protected void writeBigint(Number o) throws IOException, HsqlException {
        this.writeBytes(o.toString());
    }

    protected void writeNull(int type) throws IOException {

        if (logMode == MODE_DELETE) {
            write('=');
        } else if (isWritten) {
            write(',');
        }

        isWritten = true;

        write(BYTES_NULL);
    }

    protected void writeOther(JavaObject o)
    throws IOException, HsqlException {

        write('\'');
        writeBytes(StringConverter.byteToHex(o.getBytes()));
        write('\'');
    }

    public void writeString(String value) throws IOException {
        StringConverter.unicodeToAscii(this, value, false);
    }

    protected void writeBit(Boolean o) throws IOException, HsqlException {
        write(o.booleanValue() ? BYTES_TRUE
                               : BYTES_FALSE);
    }

    protected void writeDecimal(BigDecimal o)
    throws IOException, HsqlException {
        this.writeBytes(o.toString());
    }

    protected void writeFieldType(int type) throws IOException {

        if (logMode == MODE_DELETE) {
            write('=');
        } else if (isWritten) {
            write(',');
        }

        isWritten = true;
    }

    public void writeIntData(int i, int position) throws IOException {}

    protected void writeTimestamp(Timestamp o)
    throws IOException, HsqlException {

        write('\'');
        this.writeBytes(o.toString());
        write('\'');
    }

    public void writeIntData(int i) throws IOException {
        writeBytes(Integer.toString(i));
    }

    public void reset() {

        super.reset();

        isWritten = false;
    }
}
