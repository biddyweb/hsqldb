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


package org.hsqldb.util;

import java.awt.*;
import java.util.Vector;

/**
 * Class declaration
 *
 *
 * @version 1.0.0.1
 */
public class Grid extends Panel {

    // drawing
    private Dimension   dMinimum;
    private Font        fFont;
    private FontMetrics fMetrics;
    private Graphics    gImage;
    private Image       iImage;

    // height / width
    private int iWidth, iHeight;
    private int iRowHeight, iFirstRow;
    private int iGridWidth, iGridHeight;
    private int iX, iY;

    // data
    private String sColHead[];
    private Vector vData;
    private int    iColWidth[];
    private int    iColCount, iRowCount;

    // scrolling
    private Scrollbar sbHoriz, sbVert;
    private int       iSbWidth, iSbHeight;
    private boolean   bDrag;
    private int       iXDrag, iColDrag;

    /**
     * Constructor declaration
     *
     */
    public Grid() {

        super();

        fFont = new Font("Dialog", Font.PLAIN, 12);

        setLayout(null);

        sbHoriz = new Scrollbar(Scrollbar.HORIZONTAL);

        add(sbHoriz);

        sbVert = new Scrollbar(Scrollbar.VERTICAL);

        add(sbVert);
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    String[] getHead() {
        return sColHead;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    Vector getData() {
        return vData;
    }

    /**
     * Method declaration
     *
     *
     * @param d
     */
    public void setMinimumSize(Dimension d) {
        dMinimum = d;
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public void reshape(int x, int y, int w, int h) {

        super.reshape(x, y, w, h);

        iSbHeight = sbHoriz.getPreferredSize().height;
        iSbWidth  = sbVert.getPreferredSize().width;
        iHeight   = h - iSbHeight;
        iWidth    = w - iSbWidth;

        sbHoriz.reshape(0, iHeight, iWidth, iSbHeight);
        sbVert.reshape(iWidth, 0, iSbWidth, iHeight);
        adjustScroll();

        iImage = null;

        repaint();
    }

    /**
     * Method declaration
     *
     *
     * @param head
     */
    public void setHead(String head[]) {

        vData     = new Vector();
        iColCount = head.length;
        sColHead  = new String[iColCount];
        iColWidth = new int[iColCount];

        for (int i = 0; i < iColCount; i++) {
            sColHead[i]  = head[i];
            iColWidth[i] = 100;
        }

        iRowCount  = 0;
        iRowHeight = 0;
    }

    /**
     * Method declaration
     *
     *
     * @param data
     */
    public void addRow(String data[]) {

        if (data.length != iColCount) {
            return;
        }

        String row[] = new String[iColCount];

        for (int i = 0; i < iColCount; i++) {
            row[i] = data[i];
        }

        vData.addElement(row);

        iRowCount++;
    }

    /**
     * Method declaration
     *
     */
    public void update() {
        adjustScroll();
        repaint();
    }

    /**
     * Method declaration
     *
     */
    void adjustScroll() {

        if (iRowHeight == 0) {
            return;
        }

        int w = 0;

        for (int i = 0; i < iColCount; i++) {
            w += iColWidth[i];
        }

        iGridWidth  = w;
        iGridHeight = iRowHeight * (iRowCount + 1);

        sbHoriz.setValues(iX, iWidth, 0, iGridWidth);

        int v = iY / iRowHeight,
            h = iHeight / iRowHeight;

        sbVert.setValues(v, h, 0, iRowCount + 1);

        iX = sbHoriz.getValue();
        iY = iRowHeight * sbVert.getValue();
    }

    /**
     * Method declaration
     *
     *
     * @param e
     *
     * @return
     */
    public boolean handleEvent(Event e) {

        switch (e.id) {

            case Event.SCROLL_LINE_UP :
            case Event.SCROLL_LINE_DOWN :
            case Event.SCROLL_PAGE_UP :
            case Event.SCROLL_PAGE_DOWN :
            case Event.SCROLL_ABSOLUTE :
                iX = sbHoriz.getValue();
                iY = iRowHeight * sbVert.getValue();

                repaint();

                return true;
        }

        return super.handleEvent(e);
    }

    /**
     * Method declaration
     *
     *
     * @param g
     */
    public void paint(Graphics g) {

        if (g == null) {
            return;
        }

        if (iWidth <= 0 || iHeight <= 0) {
            return;
        }

        g.setColor(SystemColor.control);
        g.fillRect(iWidth, iHeight, iSbWidth, iSbHeight);

        if (iImage == null) {
            iImage = createImage(iWidth, iHeight);
            gImage = iImage.getGraphics();

            gImage.setFont(fFont);

            if (fMetrics == null) {
                fMetrics = gImage.getFontMetrics();
            }
        }

        if (iRowHeight == 0) {
            iRowHeight = getMaxHeight(fMetrics);

            for (int i = 0; i < iColCount; i++) {
                calcAutoWidth(i);
            }

            adjustScroll();
        }

        gImage.setColor(Color.white);
        gImage.fillRect(0, 0, iWidth, iHeight);
        gImage.setColor(Color.darkGray);
        gImage.drawLine(0, iRowHeight, iWidth, iRowHeight);

        int x = -iX;

        for (int i = 0; i < iColCount; i++) {
            int w = iColWidth[i];

            gImage.setColor(SystemColor.control);
            gImage.fillRect(x + 1, 0, w - 2, iRowHeight);
            gImage.setColor(Color.black);
            gImage.drawString(sColHead[i], x + 2, iRowHeight - 5);
            gImage.setColor(Color.darkGray);
            gImage.drawLine(x + w - 1, 0, x + w - 1, iRowHeight - 1);
            gImage.setColor(Color.white);
            gImage.drawLine(x + w, 0, x + w, iRowHeight - 1);

            x += w;
        }

        gImage.setColor(SystemColor.control);
        gImage.fillRect(0, 0, 1, iRowHeight);
        gImage.fillRect(x + 1, 0, iWidth - x, iRowHeight);
        gImage.drawLine(0, 0, 0, iRowHeight - 1);

        int y = iRowHeight + 1 - iY;
        int j = 0;

        while (y < iRowHeight + 1) {
            j++;

            y += iRowHeight;
        }

        iFirstRow = j;
        y         = iRowHeight + 1;

        for (; y < iHeight && j < iRowCount; j++, y += iRowHeight) {
            x = -iX;

            for (int i = 0; i < iColCount; i++) {
                int   w = iColWidth[i];
                Color b = Color.white,
                      t = Color.black;

                gImage.setColor(b);
                gImage.fillRect(x, y, w - 1, iRowHeight - 1);
                gImage.setColor(t);
                gImage.drawString(getDisplay(i, j), x + 2,
                                  y + iRowHeight - 5);
                gImage.setColor(Color.lightGray);
                gImage.drawLine(x + w - 1, y, x + w - 1, y + iRowHeight - 1);
                gImage.drawLine(x, y + iRowHeight - 1, x + w - 1,
                                y + iRowHeight - 1);

                x += w;
            }

            gImage.setColor(Color.white);
            gImage.fillRect(x, y, iWidth - x, iRowHeight - 1);
        }

        g.drawImage(iImage, 0, 0, this);
    }

    /**
     * Method declaration
     *
     *
     * @param g
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * Method declaration
     *
     *
     * @param e
     * @param x
     * @param y
     *
     * @return
     */
    public boolean mouseMove(Event e, int x, int y) {

        if (y <= iRowHeight) {
            int xb = x;

            x += iX - iGridWidth;

            int i = iColCount - 1;

            for (; i >= 0; i--) {
                if (x > -7 && x < 7) {
                    break;
                }

                x += iColWidth[i];
            }

            if (i >= 0) {
                if (!bDrag) {
                    setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));

                    bDrag    = true;
                    iXDrag   = xb - iColWidth[i];
                    iColDrag = i;
                }

                return true;
            }
        }

        return mouseExit(e, x, y);
    }

    /**
     * Method declaration
     *
     *
     * @param e
     * @param x
     * @param y
     *
     * @return
     */
    public boolean mouseDrag(Event e, int x, int y) {

        if (bDrag && x < iWidth) {
            int w = x - iXDrag;

            if (w < 0) {
                w = 0;
            }

            iColWidth[iColDrag] = w;

            adjustScroll();
            repaint();
        }

        return true;
    }

    /**
     * Method declaration
     *
     *
     * @param e
     * @param x
     * @param y
     *
     * @return
     */
    public boolean mouseExit(Event e, int x, int y) {

        if (bDrag) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            bDrag = false;
        }

        return true;
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    public Dimension preferredSize() {
        return minimumSize();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    public Dimension getPreferredSize() {
        return minimumSize();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * Method declaration
     *
     *
     * @return
     */
    public Dimension minimumSize() {
        return dMinimum;
    }

    /**
     * Method declaration
     *
     *
     * @param i
     */
    private void calcAutoWidth(int i) {

        int w = 10;

        w = Math.max(w, fMetrics.stringWidth(sColHead[i]));

        for (int j = 0; j < iRowCount; j++) {
            String s[] = (String[]) (vData.elementAt(j));

            w = Math.max(w, fMetrics.stringWidth(s[i]));
        }

        iColWidth[i] = w + 6;
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param y
     *
     * @return
     */
    private String getDisplay(int x, int y) {
        return (((String[]) (vData.elementAt(y)))[x]);
    }

    /**
     * Method declaration
     *
     *
     * @param x
     * @param y
     *
     * @return
     */
    private String get(int x, int y) {
        return (((String[]) (vData.elementAt(y)))[x]);
    }

    /**
     * Method declaration
     *
     *
     * @param f
     *
     * @return
     */
    private static int getMaxHeight(FontMetrics f) {
        return f.getHeight() + 4;
    }
}
