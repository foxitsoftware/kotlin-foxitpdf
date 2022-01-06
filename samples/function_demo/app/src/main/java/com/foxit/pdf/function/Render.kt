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
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import com.foxit.pdf.function.Common.fixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.function.Common.loadPage
import com.foxit.pdf.function.Common.saveImageFile
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.common.Renderer
import com.foxit.sdk.pdf.PDFPage

class Render(private val mContext: Context) {
    fun renderPage(index: Int) {
        val inputPath = fixFolder + "FoxitBigPreview.pdf"
        val outputPath = String.format(
            "%s_index_%d.jpg",
            getOutputFilesFolder(Common.PDF_TO_IMAGE) + "FoxitBigPreview",
            index
        )
        val doc = loadPDFDoc(mContext, inputPath, null) ?: return
        try {
            val pageCount = doc.pageCount
            if (index > pageCount || index < 0) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_the_page_index_out_of_range),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            val pdfPage = loadPage(mContext, doc, index, PDFPage.e_ParsePageNormal)
            if (pdfPage == null || pdfPage.isEmpty) {
                return
            }

            //Create the bitmap and erase its background.
            val bitmap = Bitmap.createBitmap(
                pdfPage.width.toInt(), pdfPage.height.toInt(), Bitmap.Config.RGB_565
            )
            bitmap.eraseColor(Color.WHITE)
            val matrix = pdfPage.getDisplayMatrix(
                0, 0, pdfPage.width.toInt(), pdfPage.height
                    .toInt(), Constants.e_Rotation0
            )
            val renderer = Renderer(bitmap, true)
            //Render the page to bitmap.
            val progressive = renderer.startRender(pdfPage, matrix, null)
            var state = Progressive.e_ToBeContinued
            while (state == Progressive.e_ToBeContinued) {
                state = progressive.resume()
            }
            if (state == Progressive.e_Error) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_failed_to_render_the_page, index, ""),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            //Save the render result to the jpeg image.
            if (!saveImageFile(bitmap, Bitmap.CompressFormat.JPEG, outputPath)) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_failed_to_save_image_file),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_failed_to_render_the_page, index, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}