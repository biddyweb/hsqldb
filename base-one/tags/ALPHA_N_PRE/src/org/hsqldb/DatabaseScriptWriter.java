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

import java.io.*;
import java.sql.SQLException;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.HsqlStringBuffer;
import org.hsqldb.lib.StringConverter;
import org.hsqldb.lib.StopWatch;

/**
 * Handles all logging to file operations. A log consists of three blocks:<p>
 *
 * DDL BLOCK: definition of DB objects, users and rights at startup time<br>
 * DATA BLOCK: all data for MEMORY tables at startup time<br>
 * LOG BLOCK: SQL statements logged since startup or the last CHECKPOINT<br>
 *
 * The implementation of this class and its subclasses support the formats
 * used for writing the data. In versions up to 1.7.2, this data is written
 * to the *.script file for the database. Since 1.7.2 the data can also be
 * written as binray in order to speed up shutdown and startup.<p>
 *
 * In 1.7.2, two separate files are used, one for the DDL + DATA BLOCK and
 * the other for the LOG BLOCK.<p>
 *
 * A related use for this class is for saving a current snapshot of the
 * database data to a user-defined file. This happens in the SHUTDOWN COMPACT
 * process or done as a result of the SCRIPT command. In this case, the
 * DATA block contains the CACHED table data as well.<p>
 *
 * DatabaseScriptReader and its subclasses read back the data at startup time.
 *
 * todo: this class will be enhanced or extended to support the following:<p>
 *
 * periodic flushing of data according to a property<br>
 * constant backup of *.scrip file data<br>
 * optional encryption / compression of data<br>
 *
 * @author fredt@users
 * @version 1.7.2
 * @since 1.7.2
 */

// todo - can lock the database engine as readonly in a wrapper for this when
// used at checkpoint
class DatabaseScriptWriter {

    Database          db;
    String            outFile;
    OutputStream      fileStreamOut;
    FileDescriptor    outDescriptor;
    DatabaseRowOutput binaryOut = new BinaryServerRowOutput();
    int               tableRowCount;
    StopWatch         sw = new StopWatch();

    /**
     * this determines if the script is the normal script (false) used
     * internally by the engine or a user-initiated snapshot of the DB (true)
     */
    boolean          includeCachedData;
    long             byteCount;
    int              writeDelay;
    volatile boolean needsSync;
    volatile boolean forceSync;
    volatile boolean busyWriting;
    static final int INSERT = 0;

    /** the ID of the last session that wrote to log */
    int sessionId;

    // todo - perhaps move this global into a lib utility class
    static byte[] lineSep;

    static {
        String sLineSep = System.getProperty("line.separator", "\n");

        lineSep = new byte[sLineSep.length()];

        for (int i = 0; i < sLineSep.length(); i++) {
            lineSep[i] = (byte) sLineSep.charAt(i);
        }
    }

    final static int SCRIPT_TEXT_170          = 0;
    final static int SCRIPT_BINARY_172        = 1;
    final static int SCRIPT_ZIPPED_BINARY_172 = 3;

    static DatabaseScriptWriter newDatabaseScriptWriter(Database db,
            String file, boolean includeCachedData, boolean newFile,
            int scriptType) throws SQLException {

        if (scriptType == SCRIPT_TEXT_170) {
            return new DatabaseScriptWriter(db, file, includeCachedData,
                                            newFile);
        } else if (scriptType == SCRIPT_BINARY_172) {
            return new BinaryDatabaseScriptWriter(db, file,
                                                  includeCachedData, newFile);
        } else {
            return new ZippedDatabaseScriptWriter(db, file,
                                                  includeCachedData, newFile);
        }
    }

    DatabaseScriptWriter(Database db, String file, boolean includeCachedData,
                         boolean newFile) throws SQLException {

        File newFileFile = new File(file);

        if (newFileFile.exists()) {
            if (newFile) {
                throw Trace.error(Trace.FILE_IO_ERROR, file);
            } else {
                byteCount = newFileFile.length();
            }
        }

        this.db                = db;
        this.includeCachedData = includeCachedData;
        outFile                = file;

        openFile();
    }

    /**
     *  Not used in current implementation.
     */
    void setWriteDelay(int delay) {
        writeDelay = delay;
    }

    /**
     *  Called externally in write delay intervals.
     */
    synchronized void sync() throws IOException {

        if (needsSync) {
            if (busyWriting) {
                forceSync = true;

                return;
            }

            Trace.printSystemOut("file sync interval: " + sw.elapsedTime());
            sw.zero();
            fileStreamOut.flush();
            outDescriptor.sync();
            Trace.printSystemOut("file sync: " + sw.elapsedTime());
            sw.zero();

            needsSync = false;
            forceSync = false;
        }
    }

    void close() throws SQLException {
        try {
            fileStreamOut.flush();
            fileStreamOut.close();
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR);
        }
    }

    long size() {
        return byteCount;
    }

    void writeAll() throws SQLException {

        try {
            writeDDL();
            writeExistingData();
            finishStream();
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR);
        }
    }

    /**
     *  File is opened in append mode although in current usage the file
     *  never pre-exists
     */
    protected void openFile() throws SQLException {

        try {
            FileOutputStream fos = new FileOutputStream(outFile, true);

            outDescriptor = fos.getFD();
            fileStreamOut = fos;
        } catch (IOException e) {
            throw Trace.error(Trace.FILE_IO_ERROR, outFile);
        }
    }

    /**
     * This is not really useful in the current usage but may be if this
     * class is used in a different way.
     */
    protected void finishStream() throws IOException {}

    protected void writeDDL() throws IOException, SQLException {

        Result ddlPart = DatabaseScript.getScript(db, !includeCachedData);

        writeSingleColumnResult(ddlPart);
    }

    protected void writeExistingData() throws SQLException, IOException {

        boolean       wroteTable = false;
        HsqlArrayList tables     = db.getTables();

        for (int i = 0, size = tables.size(); i < size; i++) {
            Table t = (Table) tables.get(i);

            // write all memory table data
            // write cached table data unless index roots have been written
            // write all text table data apart from readonly text tables
            // unless index roots have been written
            boolean script = false;

            switch (t.tableType) {

                case Table.MEMORY_TABLE :
                    script = true;
                    break;

                case Table.CACHED_TABLE :
                    script = includeCachedData;
                    break;

                case Table.TEXT_TABLE :
                    script = includeCachedData &&!t.isReadOnly;
                    break;
            }

            if (script) {
                writeTableInit(t);

                Index primary = t.getPrimaryIndex();
                Node  x       = primary.first();

                while (x != null) {
                    writeRow(x.getData(), t);

                    x = primary.next(x);
                }

                writeTableTerm(t);
            }
        }

        writeDataTerm();
    }

    protected void writeTableInit(Table t) throws SQLException, IOException {}

    protected void writeTableTerm(Table t) throws SQLException, IOException {

        if (t.isDataReadOnly() &&!t.isTemp() &&!t.isText()) {
            HsqlStringBuffer a = new HsqlStringBuffer("SET TABLE ");

            a.append(t.getName().statementName);
            a.append(" READONLY TRUE");
            writeLogStatement(a.toString());
        }
    }

    protected void writeSingleColumnResult(Result r)
    throws SQLException, IOException {

        Record n = r.rRoot;

        while (n != null) {
            writeLogStatement((String) n.data[0]);

            n = n.next;
        }
    }

    protected void writeRow(Object[] data,
                            Table t) throws SQLException, IOException {
        writeLogStatement(t.getInsertStatement(data));
    }

    protected void writeDataTerm() throws IOException {}

    void writeLogStatement(String s,
                           int sid) throws IOException, SQLException {

        if (sid != sessionId) {
            s         = "/*C" + sid + "*/" + s;
            sessionId = sid;
        }

        writeLogStatement(s);
    }

    private void writeLogStatement(String s)
    throws IOException, SQLException {

        busyWriting = true;

        binaryOut.reset();
        StringConverter.unicodeToAscii(binaryOut, s);
        binaryOut.write(lineSep);
        fileStreamOut.write(binaryOut.getBuffer(), 0, binaryOut.size());

        byteCount += binaryOut.size();

        fileStreamOut.flush();

        needsSync   = true;
        busyWriting = false;

        if (forceSync) {
            sync();
        }
    }
}
