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

import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.common.Renderer

class Render(var context: Context, var path: String) {

    fun renderPage(index: Int) {
        val doc = Common.loadPDFDoc(context, path, null) ?: return

        try {
            val pageCount = doc.pageCount
            if (index > pageCount || index < 0) {
                Toast.makeText(context, String.format("The page index is out of range!"), Toast.LENGTH_LONG).show()
                return
            }

            val name = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."))
            val outputFilePath = String.format("%s_index_%d.jpg", Common.getOutputFilesFolder(Common.renderModuleName) + name, index)
            val pdfPage = Common.loadPage(context, doc, index, PDFPage.e_ParsePageNormal)
            if (pdfPage == null || pdfPage.isEmpty) {
                return
            }

            //Create the bitmap and erase its background.
            val bitmap = Bitmap.createBitmap(pdfPage.width.toInt(), pdfPage.height.toInt(), Bitmap.Config.RGB_565)
            bitmap.eraseColor(Color.WHITE)


            val matrix = pdfPage.getDisplayMatrix(0, 0, pdfPage.width.toInt(), pdfPage.height.toInt(), Constants.e_Rotation0)

            val renderer = Renderer(bitmap, true)

            //Render the page to bitmap.
            val progressive = renderer.startRender(pdfPage, matrix, null)
            var state = Progressive.e_ToBeContinued
            while (state == Progressive.e_ToBeContinued) {
                state = progressive.resume()
            }

            if (state == Progressive.e_Error) {
                Toast.makeText(context, String.format("Failed to render the page No.%d failed!", index), Toast.LENGTH_LONG).show()
                return
            }

            //Save the render result to the jpeg image.
            if (!Common.saveImageFile(bitmap, Bitmap.CompressFormat.JPEG, outputFilePath)) {
                Toast.makeText(context, String.format("Failed to Save Image File!"), Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(context, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(context, String.format("Failed to render the page No.%d! %s", index, e.message), Toast.LENGTH_LONG).show()
        }
    }
}
