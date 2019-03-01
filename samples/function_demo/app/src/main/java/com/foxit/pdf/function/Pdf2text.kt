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
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.pdf.TextPage

import java.io.File
import java.io.FileWriter
import java.io.IOException

class Pdf2text(var context: Context, var pdfFilePath: String) {

    fun doPdfToText() {
        val indexPdf = pdfFilePath.lastIndexOf(".")
        val indexSep = pdfFilePath.lastIndexOf("/")

        val filenameWithoutPdf = pdfFilePath.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.getOutputFilesFolder(Common.pdf2textModuleName) + filenameWithoutPdf + ".txt"
        var strText = ""

        val doc = Common.loadPDFDoc(context, pdfFilePath, null) ?: return

        var page: PDFPage? = null
        try {
            val pageCount = doc.pageCount

            //Traverse pages and get the text string.
            for (i in 0 until pageCount) {
                page = Common.loadPage(context, doc, i, PDFPage.e_ParsePageNormal)
                if (page == null || page.isEmpty) {
                    continue
                }

                val textSelect = TextPage(page, TextPage.e_ParseTextNormal)
                if (textSelect == null || textSelect.isEmpty) continue

                strText += textSelect.getChars(0, textSelect.charCount) + "\r\n"
                page.delete()
            }
        } catch (e: PDFException) {
            Toast.makeText(context, "Pdf to text error. " + e.message, Toast.LENGTH_LONG).show()
            return
        }

        //Output the text string to the TXT file.
        var fileWriter: FileWriter? = null
        try {
            val fileTxt = File(outputFilePath)
            fileWriter = FileWriter(fileTxt)
            fileWriter.write(strText)
            fileWriter.flush()
            fileWriter.close()
        } catch (e: IOException) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(context, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
    }
}
