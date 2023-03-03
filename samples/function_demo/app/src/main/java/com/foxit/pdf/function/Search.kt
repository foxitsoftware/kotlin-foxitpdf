/**
 * Copyright (C) 2003-2023, Foxit Software Inc..
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
import com.foxit.sdk.PDFException
import com.foxit.sdk.pdf.TextPage
import com.foxit.sdk.pdf.TextSearch
import java.io.FileWriter
import java.io.IOException

class Search(private val mContext: Context) {
    fun startSearch() {
        val inputPath = fixFolder + "AboutFoxit.pdf"
        val outputPath = getOutputFilesFolder(Common.SEARCH) + "search.txt"
        val doc = loadPDFDoc(mContext, inputPath, null) ?: return
        var text_out: FileWriter? = null
        try {
            text_out = FileWriter(outputPath, false)

            // sample 1: search for all pages of doc.
            val search = TextSearch(doc, null, TextPage.e_ParseTextNormal)
            val start_index = 0
            val end_index = doc.pageCount - 1
            search.setStartPage(0)
            search.setEndPage(doc.pageCount - 1)
            val pattern = "Foxit"
            search.setPattern(pattern)
            val flags = TextSearch.e_SearchNormal
            // If want to specify flags, you can do as followings:
            // flags |= TextSearch::e_SearchMatchCase;
            // flags |= TextSearch::e_SearchMatchWholeWord;
            // flags |= TextSearch::e_SearchConsecutive;
            search.setSearchFlags(flags)
            wrapperWrite(text_out, "Begin search $pattern at $inputPath.\n")
            wrapperWrite(text_out, "Start index:\t$start_index\r\n")
            wrapperWrite(text_out, "End index:\t$end_index\r\n")
            wrapperWrite(text_out, "Match key:\t$pattern\r\n")
            val match_case = if (flags and TextSearch.e_SearchMatchCase != 0) "Yes" else "No"
            wrapperWrite(text_out, "Match Case\t$match_case\r\n")
            val match_whole_word =
                if (flags and TextSearch.e_SearchMatchWholeWord != 0) "Yes" else "No"
            wrapperWrite(text_out, "Match whole word:\t$match_whole_word\r\n")
            val match_consecutive =
                if (flags and TextSearch.e_SearchConsecutive != 0) "Yes" else "No"
            wrapperWrite(text_out, "Consecutive:\t$match_consecutive\r\n")
            var match_count = 0
            while (search.findNext()) {
                outputMatchedInfo(text_out, search, match_count)
                match_count++
            }
            text_out.close()
            Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
        }
    }

    @Throws(IOException::class)
    private fun wrapperWrite(text_out: FileWriter, formatbuff: String) {
        text_out.write(formatbuff)
    }

    @Throws(PDFException::class, IOException::class)
    private fun outputMatchedInfo(text_out: FileWriter, search: TextSearch, matched_index: Int) {
        val page_index = search.matchPageIndex
        wrapperWrite(text_out, "Index of matched pattern:\t$matched_index\r\n")
        wrapperWrite(text_out, "\tpage:\t$page_index\r\n")
        wrapperWrite(
            text_out, """	match char start index:	${search.matchStartCharIndex}
"""
        )
        wrapperWrite(
            text_out, """	match char end index:	${search.matchEndCharIndex}
"""
        )
        wrapperWrite(
            text_out, """	match sentence start index:	${search.matchSentenceStartIndex}
"""
        )
        wrapperWrite(
            text_out, """	match sentence:	${search.matchSentence}
"""
        )
        val rect_array = search.matchRects
        val rect_count = rect_array.size
        wrapperWrite(text_out, "\tmatch rectangles count:\t$rect_count\r\n")
        for (i in 0 until rect_count) {
            val rect = rect_array.getAt(i)
            wrapperWrite(
                text_out,
                String.format(
                    "\trectangle(in PDF space) :%d\t[left = %.4f, bottom = %.4f, right = %.4f, top = %.4f]\r\n",
                    i,
                    rect.left,
                    rect.bottom,
                    rect.right,
                    rect.top
                )
            )
        }
    }
}