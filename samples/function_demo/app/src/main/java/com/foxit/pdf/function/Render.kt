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
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast

import com.foxit.sdk.common.CommonDefines
import com.foxit.sdk.common.PDFException
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.pdf.Renderer

class Render(context: Context, path: String) {
    private var mContext: Context? = null
    private var mPath: String? = null

    init {
        mContext = context
        mPath = path
    }

    fun renderPage(index: Int) {
        val doc = Common.loadPDFDoc(mContext!!, mPath, null) ?: return

        try {
            val pageCount = doc.pageCount
            if (index > pageCount || index < 0) {
                Toast.makeText(mContext, String.format("The page index is out of range!"), Toast.LENGTH_LONG).show()
                return
            }

            val name = mPath!!.substring(mPath!!.lastIndexOf("/") + 1, mPath!!.lastIndexOf("."))
            val outputFilePath = String.format("%s_index_%d.jpg", Common.GetOutputFilesFolder(Common.renderModuleName) + name, index)
            val pdfPage = Common.loadPage(mContext!!, doc, index, PDFPage.e_parsePageNormal) ?: return

            //Create the bitmap and erase its background.
            val bitmap = Bitmap.createBitmap(pdfPage.width.toInt(), pdfPage.height.toInt(), Bitmap.Config.ARGB_8888)

            //If the page has transparency, the bitmap should be erased "Color.TRANSPARENT".
            if (pdfPage.hasTransparency()) {
                bitmap.eraseColor(Color.TRANSPARENT)
            } else {
                bitmap.eraseColor(Color.WHITE)
            }

            val matrix = pdfPage.getDisplayMatrix(0, 0, pdfPage.width.toInt(), pdfPage.height.toInt(), CommonDefines.e_rotation0)

            val renderer = Renderer(bitmap)

            //Render the page to bitmap.
            val progressive = renderer.startRender(pdfPage, matrix, null)
            var state = CommonDefines.e_progressToBeContinued
            while (state == CommonDefines.e_progressToBeContinued) {
                state = progressive.continueProgress()
            }
            progressive.release()
            if (state == CommonDefines.e_progressError) {
                Toast.makeText(mContext, String.format("Failed to render the page No.%d failed!", index), Toast.LENGTH_LONG).show()
                return
            }

            //Save the render result to the jpeg image.
            if (false == Common.SaveImageFile(bitmap, Bitmap.CompressFormat.JPEG, outputFilePath)) {
                Toast.makeText(mContext, String.format("Failed to Save Image File!"), Toast.LENGTH_LONG).show()
                return
            }

            renderer.release()
            doc.closePage(index)
            Toast.makeText(mContext, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(mContext, String.format("Failed to render the page No.%d! %s", index, e.message), Toast.LENGTH_LONG).show()
        } finally {
            Common.releaseDoc(mContext!!, doc)
        }
    }
}
