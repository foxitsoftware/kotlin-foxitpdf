/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
 * All Rights Reserved.
 *
 *
 * http://www.foxitsoftware.com
 *
 *
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit Mobile PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.foxit.pdf.function

import android.content.Context
import android.widget.Toast

import com.foxit.sdk.common.PDFException
import com.foxit.sdk.pdf.Bookmark
import com.foxit.sdk.pdf.PDFDoc

class Outline(context: Context, pdfFilePath: String) {
    private var mFilePath = ""
    private var mContext: Context? = null

    init {
        mFilePath = pdfFilePath
        mContext = context
    }

    fun modifyOutline() {
        val indexPdf = mFilePath.lastIndexOf(".")
        val indexSep = mFilePath.lastIndexOf("/")

        val filenameWithoutPdf = mFilePath.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.GetOutputFilesFolder(Common.outlineModuleName) + filenameWithoutPdf + "_edit.pdf"

        var doc: PDFDoc? = null
        doc = Common.loadPDFDoc(mContext!!, mFilePath, null)
        if (doc == null) {
            return
        }
        try {
            val bookmarkRoot = doc.firstBookmark ?: return

            val firstChild = bookmarkRoot.firstChild
            modifyOutline(firstChild)

            if (false == doc.saveAs(outputFilePath, PDFDoc.e_saveFlagNormal.toLong())) {
                Toast.makeText(mContext, "Save document error!", Toast.LENGTH_LONG).show()
                return
            }
        } catch (e: PDFException) {
            Toast.makeText(mContext, "Outline demo run error. " + e.message, Toast.LENGTH_LONG).show()
            return
        } finally {
            Common.releaseDoc(mContext!!, doc)
        }
        Toast.makeText(mContext, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
    }

    private fun modifyOutline(bookmark: Bookmark?) {
        try {
            if (bookmark == null)
                return

            if (index % 2 == 0) {
                bookmark.color = -0x10000
                bookmark.style = Bookmark.e_bookmarkStyleBold.toLong()
            } else {
                bookmark.color = -0xff0100
                bookmark.style = Bookmark.e_bookmarkStyleItalic.toLong()
            }

            bookmark.title = "foxitbookmark$index"
            index++

            //Traverse the brother nodes and modify their appearance and titles.
            val nextSibling = bookmark.nextSibling
            modifyOutline(nextSibling)

            //Traverse the children nodes and modify their appearance and titles.
            val child = bookmark.firstChild
            modifyOutline(child)

        } catch (e: PDFException) {
            Toast.makeText(mContext, "Outline demo run error. " + e.message, Toast.LENGTH_LONG).show()
        }

    }

    companion object {
        private var index = 0
    }
}
