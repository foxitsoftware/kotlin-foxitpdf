/**
 * Copyright (C) 2003-2022, Foxit Software Inc..
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
import com.foxit.pdf.function.Common.fixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.pdf.Bookmark
import com.foxit.sdk.pdf.PDFDoc

class Outline(private val mContext: Context) {
    private var index = 0
    fun modifyOutline() {
        val inputPath = fixFolder + "Outline.pdf"
        val outputPath = getOutputFilesFolder(Common.OUTLINE) + "Outline_edit.pdf"
        val doc = loadPDFDoc(mContext, inputPath, null)
        if (doc == null || doc.isEmpty) {
            return
        }
        try {
            val bookmarkRoot = doc.rootBookmark ?: return
            val firstChild = bookmarkRoot.firstChild
            modifyOutline(firstChild)
            if (!doc.saveAs(outputPath, PDFDoc.e_SaveFlagNormal)) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_save_doc_error),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        } catch (e: PDFException) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_outline_run_error, e.message),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
    }

    private fun modifyOutline(bookmark: Bookmark) {
        try {
            if (bookmark.isEmpty) return
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
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_outline_run_error, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}