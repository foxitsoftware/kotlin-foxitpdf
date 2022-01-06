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
import com.foxit.pdf.function.Common.getFileNameWithoutExt
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.main.R
import com.foxit.sdk.pdf.Metadata
import java.io.File
import java.io.FileWriter

class DocInfo(private val mContext: Context) {
    fun outputDocInfo() {
        val inputPath = fixFolder + "FoxitBigPreview.pdf"
        val outputPath = getOutputFilesFolder(Common.DOCINFO) + "FoxitBigPreview_docinfo.txt"
        val doc = loadPDFDoc(mContext, inputPath, null) ?: return
        val txtFile = File(outputPath)
        try {
            val fileWriter = FileWriter(txtFile)

            //pageCount
            val pageCount = doc.pageCount
            fileWriter.write(String.format("Page Count: %d pages\r\n", pageCount))

            //title
            val metadata = Metadata(doc)
            var title = String.format("Title :%s\r\n", metadata.getValues("Title").getAt(0))
            //If there is no title info in the document, it uses the file name instead.
            if (title == "") {
                title = getFileNameWithoutExt(inputPath)
            }
            fileWriter.write(title)

            //author
            fileWriter.write(String.format("Author: %s\r\n", metadata.getValues("Author").getAt(0)))
            //subject
            fileWriter.write(
                String.format(
                    "Subject: %s\r\n",
                    metadata.getValues("Subject").getAt(0)
                )
            )
            //keywords
            fileWriter.write(
                String.format(
                    "Keywords: %s\r\n",
                    metadata.getValues("Keywords").getAt(0)
                )
            )
            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_failed_to_export_doc_error, inputPath),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
    }
}