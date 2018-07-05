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

import com.foxit.sdk.pdf.Metadata

import java.io.File
import java.io.FileWriter

class DocInfo(context: Context, path: String) {
    private var mContext: Context? = null
    private var mPath: String? = null

    init {
        mContext = context
        mPath = path
    }

    fun outputDocInfo() {
        val doc = Common.loadPDFDoc(mContext!!, mPath!!, null) ?: return

        val filenameWithoutPdf = mPath!!.substring(mPath!!.lastIndexOf("/") + 1, mPath!!.lastIndexOf("."))
        val outputFilePath = Common.getOutputFilesFolder(Common.docInfoModuleName) + filenameWithoutPdf + "_docinfo.txt"
        val txtFile = File(outputFilePath)
        try {
            val fileWriter = FileWriter(txtFile)

            //pageCount
            val pageCount = doc.pageCount
            fileWriter.write(String.format("Page Count: %d pages\r\n", pageCount))

            //title
            val metadata = Metadata(doc)

            var title: String? = String.format("Title :%s\r\n", metadata.getValues("Title").getAt(0))
            //If there is no title info in the document, it uses the file name instead.
            if (title == null && title == "") {
                title = filenameWithoutPdf
            }
            fileWriter.write(title!!)

            //author
            fileWriter.write(String.format("Author: %s\r\n", metadata.getValues("Author").getAt(0)))
            //subject
            fileWriter.write(String.format("Subject: %s\r\n", metadata.getValues("Subject").getAt(0)))
            //keywords
            fileWriter.write(String.format("Keywords: %s\r\n", metadata.getValues("Keywords").getAt(0)))

            fileWriter.flush()
            fileWriter.close()
        } catch (e: Exception) {
            Toast.makeText(mContext, String.format("Failed to export doc info of %s!", mPath), Toast.LENGTH_LONG).show()
            return
        } finally {
            Common.releaseDoc(mContext!!, doc)
        }

        Toast.makeText(mContext, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
    }
}
