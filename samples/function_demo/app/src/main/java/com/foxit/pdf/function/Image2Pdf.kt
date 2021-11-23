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
import android.widget.Toast
import com.foxit.pdf.function.Common.fixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Image
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.common.fxcrt.PointF
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFPage

class Image2Pdf(private val mContext: Context) {
    fun doImage2Pdf() {
        try {
            val inputPath = fixFolder + "image/"
            val outputPath = getOutputFilesFolder(Common.IMAGE_TO_PDF)
            run {

                // Convert .bmp file to PDF document.
                val input_file = inputPath + "watermark.bmp"
                val output_file = outputPath + "watermark_bmp.pdf"
                image2PDF(input_file, output_file)
            }
            run {

                // Convert .jpg file to PDF document.
                val input_file = inputPath + "image_samples.jpg"
                val output_file = outputPath + "image_samples_jpg.pdf"
                image2PDF(input_file, output_file)
            }
            run {

                // Convert .tif file to PDF document.
                val input_file = inputPath + "TIF2Pages.tif"
                val output_file = outputPath + "TIF2Pages_tif.pdf"
                image2PDF(input_file, output_file)
            }
            run {

                // Convert .gif file to PDF document.
                val input_file = inputPath + "image005.gif"
                val output_file = outputPath + "image005_gif.pdf"
                image2PDF(input_file, output_file)
            }
            Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_image_to_pdf_failed, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Throws(PDFException::class)
    private fun image2PDF(input_file: String, output_file: String) {
        Common.checkDirectoryAvailable(output_file.substring(0, output_file.lastIndexOf("/")))
        val image = Image(input_file)
        val count = image.frameCount
        val doc = PDFDoc()
        for (i in 0 until count) {
            val page = doc.insertPage(i, image.width.toFloat(), image.height.toFloat())
            val progressive = page.startParse(PDFPage.e_ParsePageNormal, null, false)
            var state = Progressive.e_ToBeContinued
            while (state == Progressive.e_ToBeContinued) {
                state = progressive.resume()
            }
            if (state == Progressive.e_Finished) {
                // Add image to page
                page.addImage(
                    image,
                    i,
                    PointF(0f, 0f),
                    image.width.toFloat(),
                    image.height.toFloat(),
                    true
                )
            }
        }
        doc.saveAs(output_file, PDFDoc.e_SaveFlagNoOriginal)
    }
}