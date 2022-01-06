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
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.function.Common.loadPage
import com.foxit.pdf.function.Common.saveDFDoc
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Font
import com.foxit.sdk.common.Image
import com.foxit.sdk.pdf.*
import com.foxit.sdk.pdf.Watermark

class Watermark(private val mContext: Context) {
    fun addWatermark() {
        val inputPath = fixFolder + "AboutFoxit.pdf"
        val outputPath = getOutputFilesFolder(Common.WATERMARK) + "watermark_add.pdf"
        val doc = loadPDFDoc(mContext, inputPath, null) ?: return
        try {
            val nCount = doc.pageCount
            for (i in 0 until nCount) {
                val page = loadPage(mContext, doc, i, PDFPage.e_ParsePageNormal) ?: continue
                addTextWatermark(doc, page)
                val wm_bmp = fixFolder + "image/watermark.bmp"
                addBitmapWatermark(doc, page, wm_bmp)
                val wm_image = fixFolder + "image/sdk.png"
                addImageWatermark(doc, page, wm_image)
                addSingleWatermark(doc, page)
            }
            saveDFDoc(mContext, doc, outputPath)
        } catch (e: PDFException) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_add_watermark_failed, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Throws(PDFException::class)
    private fun addTextWatermark(doc: PDFDoc, page: PDFPage) {
        val settings = WatermarkSettings()
        settings.flags = WatermarkSettings.e_FlagASPageContents or WatermarkSettings.e_FlagOnTop
        settings.offset_x = 0f
        settings.offset_y = 0f
        settings.opacity = 90
        settings.position = Constants.e_PosTopRight
        settings.rotation = -45f
        settings.scale_x = 1f
        settings.scale_y = 1f
        val text_properties = WatermarkTextProperties()
        text_properties.alignment = Constants.e_AlignmentCenter
        text_properties.color = 0xF68C21
        text_properties.font_size = WatermarkTextProperties.e_FontStyleNormal.toFloat()
        text_properties.line_space = 1f
        text_properties.font_size = 12f
        text_properties.font = Font(Font.e_StdIDTimesB)
        val watermark =
            Watermark(doc, "Foxit PDF SDK\nwww.foxitsoftware.com", text_properties, settings)
        watermark.insertToPage(page)
    }

    @Throws(PDFException::class)
    private fun addBitmapWatermark(doc: PDFDoc, page: PDFPage, bitmap_file: String) {
        val settings = WatermarkSettings()
        settings.flags = WatermarkSettings.e_FlagASPageContents or WatermarkSettings.e_FlagOnTop
        settings.offset_y = 0f
        settings.offset_y = 0f
        settings.opacity = 60
        settings.position = Constants.e_PosCenterLeft
        settings.rotation = 90f
        val image = Image(bitmap_file)
        val bitmap = image.getFrameBitmap(0)
        settings.scale_x = page.height * 1.0f / bitmap.width
        settings.scale_y = settings.scale_x
        val watermark = Watermark(doc, bitmap, settings)
        watermark.insertToPage(page)
    }

    @Throws(PDFException::class)
    private fun addImageWatermark(doc: PDFDoc, page: PDFPage, image_file: String) {
        val settings = WatermarkSettings()
        settings.flags = WatermarkSettings.e_FlagASPageContents or WatermarkSettings.e_FlagOnTop
        settings.offset_x = 0f
        settings.offset_y = 0f
        settings.opacity = 20
        settings.position = Constants.e_PosCenter
        settings.rotation = 0.0f
        val image = Image(image_file)
        val bitmap = image.getFrameBitmap(0)
        settings.scale_x = page.width * 0.618f / bitmap.width
        settings.scale_y = settings.scale_x
        val watermark = Watermark(doc, image, 0, settings)
        watermark.insertToPage(page)
    }

    @Throws(PDFException::class)
    private fun addSingleWatermark(doc: PDFDoc, page: PDFPage) {
        val settings = WatermarkSettings()
        settings.flags = WatermarkSettings.e_FlagASPageContents or WatermarkSettings.e_FlagOnTop
        settings.offset_x = 0f
        settings.offset_y = 0f
        settings.opacity = 90
        settings.position = Constants.e_PosBottomRight
        settings.rotation = 0.0f
        settings.scale_x = 0.1f
        settings.scale_y = 0.1f
        val watermark = Watermark(doc, page, settings)
        watermark.insertToPage(page)
    }
}