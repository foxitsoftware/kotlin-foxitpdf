/**
 * Copyright (C) 2003-2019, Foxit Software Inc..
 * All Rights Reserved.
 *
 *
 * http://www.foxitsoftware.com
 *
 *
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.foxit.pdf.function

import android.content.Context
import android.widget.Toast

import com.foxit.sdk.PDFException
import com.foxit.sdk.pdf.Bookmark
import com.foxit.sdk.pdf.PDFDoc

class Outline(var context: Context, var pdfFilePath: String) {

    fun modifyOutline() {
        val indexPdf = pdfFilePath.lastIndexOf(".")
        val indexSep = pdfFilePath.lastIndexOf("/")

        val filenameWithoutPdf = pdfFilePath.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.getOutputFilesFolder(Common.outlineModuleName) + filenameWithoutPdf + "_edit.pdf"

        var doc: PDFDoc? = null
        doc = Common.loadPDFDoc(context, pdfFilePath, null)
        if (doc == null || doc.isEmpty) {
            return
        }
        try {
            val bookmarkRoot = doc.rootBookmark ?: return

            val firstChild = bookmarkRoot.firstChild
            modifyOutline(firstChild)

            if (!doc.saveAs(outputFilePath, PDFDoc.e_SaveFlagNormal)) {
                Toast.makeText(context, "Save document error!", Toast.LENGTH_LONG).show()
                return
            }
        } catch (e: PDFException) {
            Toast.makeText(context, "Outline demo run error. " + e.message, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(context, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
    }

    private fun modifyOutline(bookmark: Bookmark) {
        try {
            if (bookmark.isEmpty)
                return

            if (index % 2 == 0) {
                bookmark.color = -0x10000
                bookmark.style = Bookmark.e_StyleBold
            } else {
                bookmark.color = -0xff0100
                bookmark.style = Bookmark.e_StyleItalic
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
            Toast.makeText(context, "Outline demo run error. " + e.message, Toast.LENGTH_LONG).show()
        }

    }

    companion object {
        private var index = 0
    }
}
