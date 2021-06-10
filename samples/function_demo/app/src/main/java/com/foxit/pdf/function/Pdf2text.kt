/**
 * Copyright (C) 2003-2021, Foxit Software Inc..
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
import com.foxit.pdf.function.Common.fixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.function.Common.loadPage
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.function.Common
import java.lang.StringBuilder
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.pdf.TextPage
import com.foxit.sdk.PDFException
import android.widget.Toast
import com.foxit.pdf.main.R
import java.io.FileWriter
import java.io.File
import java.io.IOException

class Pdf2text(private val mContext: Context) {
    fun doPdfToText() {
        val inputPath = fixFolder + "FoxitBigPreview.pdf"
        val outputPath = getOutputFilesFolder(Common.PDF_TO_TXT) + "FoxitBigPreview.txt"
        val strText = StringBuilder()
        val doc = loadPDFDoc(mContext, inputPath, null) ?: return
        var page: PDFPage? = null
        try {
            val pageCount = doc.pageCount

            //Traverse pages and get the text string.
            for (i in 0 until pageCount) {
                page = loadPage(mContext, doc, i, PDFPage.e_ParsePageNormal)
                if (page == null || page.isEmpty) {
                    continue
                }
                val textSelect = TextPage(page, TextPage.e_ParseTextNormal)
                if (textSelect.isEmpty) {
                    continue
                }
                strText.append(textSelect.getChars(0, textSelect.charCount)).append("\r\n")
            }
        } catch (e: PDFException) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_pdf_to_text_error, e.message),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        //Output the text string to the TXT file.
        var fileWriter: FileWriter? = null
        try {
            val fileTxt = File(outputPath)
            fileWriter = FileWriter(fileTxt)
            fileWriter.write(strText.toString())
            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
    }
}