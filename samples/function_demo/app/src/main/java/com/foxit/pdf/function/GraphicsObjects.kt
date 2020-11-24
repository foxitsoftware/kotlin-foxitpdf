/**
 * Copyright (C) 2003-2020, Foxit Software Inc..
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
import com.foxit.pdf.function.Common.getFixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.function.Common.loadPage
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Font
import com.foxit.sdk.common.Image
import com.foxit.sdk.common.Path
import com.foxit.sdk.common.fxcrt.Matrix2D
import com.foxit.sdk.common.fxcrt.PointF
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.pdf.graphics.*
import java.io.FileWriter

class GraphicsObjects(private val context: Context) {
    fun addGraphicsObjects() {
        val inputPath = getFixFolder() + "graphics_objects.pdf"
        val outputPath = getOutputFilesFolder(Common.GRAPHICS_OBJECTS) + "graphics_objects.pdf"
        val doc = loadPDFDoc(context, inputPath, null) ?: return
        try {
            // Get original shading objects from PDF page.
            val original_page = loadPage(context, doc, 0, PDFPage.e_ParsePageNormal) ?: return
            var position = original_page.getFirstGraphicsObjectPosition(GraphicsObject.e_TypeShading)
            if (position == 0L) return
            val black_pieces = original_page.getGraphicsObject(position)
            position = original_page.getNextGraphicsObjectPosition(position, GraphicsObject.e_TypeShading)
            val white_pieces = original_page.getGraphicsObject(position)

            // Add a new PDF page and insert text objects.
            var page = doc.insertPage(0, PDFPage.e_SizeLetter)
            addTextObjects(page)

            // Add a new PDF page and insert image objects.
            page = doc.insertPage(1, PDFPage.e_SizeLetter)
            val image_file = getFixFolder() + "image/sdk.png"
            addImageObjects(page, image_file)

            // Add a new PDF page and insert path objects, and copy shading objects.
            page = doc.insertPage(2, PDFPage.e_SizeLetter)
            addPathObjects(page, black_pieces, white_pieces)
            val ret = doc.saveAs(outputPath, PDFDoc.e_SaveFlagNoOriginal)
            if (ret) Toast.makeText(context, getSuccessInfo(context, getOutputFilesFolder(Common.GRAPHICS_OBJECTS)!!), Toast.LENGTH_LONG).show() else Toast.makeText(context, context.getString(R.string.fx_add_graphics_failed), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.fx_add_graphics_failed), Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(PDFException::class)
    private fun addTextObjects(page: PDFPage) {
        val position = page.getLastGraphicsObjectPosition(GraphicsObject.e_TypeText)
        var text_object = TextObject.create()
        if (text_object == null) {
            Toast.makeText(context, context.getString(R.string.fx_add_graphics_failed), Toast.LENGTH_SHORT).show()
            return
        }
        text_object.fillColor = -0x8100

        // Prepare text state
        val state = TextState()
        state.font_size = 80.0f
        state.font = Font("Simsun", Font.e_StylesSmallCap, Font.e_CharsetGB2312, 0)
        state.textmode = TextState.e_ModeFill
        text_object.setTextState(page, state, false, 750)

        // Set text.
        text_object.text = "Foxit Software"
        val last_position = page.insertGraphicsObject(position, text_object)
        var rect = text_object.rect
        var offset_x = (page.width - (rect.right - rect.left)) / 2
        var offset_y = page.height * 0.8f - (rect.top - rect.bottom) / 2
        text_object.transform(Matrix2D(1F, 0F, 0F, 1F, offset_x, offset_y), false)

        // Generate content
        page.generateContent()

        // Clone a text object from the old text object.
        text_object = text_object.clone().textObject
        state.font = Font(Font.e_StdIDTimes)
        state.font_size = 48.0f
        state.textmode = TextState.e_ModeFillStrokeClip
        text_object.setTextState(page, state, true, 750)
        text_object.text = "www.foxitsoftware.com"
        text_object.fillColor = -0x555556
        text_object.strokeColor = -0x973df
        page.insertGraphicsObject(last_position, text_object)
        rect = text_object.rect
        offset_x = (page.width - (rect.right - rect.left)) / 2
        offset_y = page.height * 0.618f - (rect.top - rect.bottom) / 2
        text_object.transform(Matrix2D(1F, 0F, 0F, 1F, offset_x, offset_y), false)

        // Generate content again after transformation.
        page.generateContent()

        //Show how to get the characters' information of text object.
        try {
            val text_object_charcount = text_object.charCount
            val file_writer = FileWriter(getOutputFilesFolder(Common.GRAPHICS_OBJECTS) + "text_objects_info.txt", false)
            file_writer.write("""
    The new text object has ${text_object_charcount}characters.

    """.trimIndent())
            for (i in 0 until text_object_charcount) {
                //The character's position.
                val char_pos = text_object.getCharPos(i)
                //The character's width.
                val width = text_object.getCharWidthByIndex(i)
                //The character's height.
                val height = text_object.getCharHeightByIndex(i)
                file_writer.write("""The position of the $i characters is (${char_pos.x},${char_pos.y}).The width and height of the characters is ($width,$height).
""")
            }
            file_writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(PDFException::class)
    private fun addPieces(page: PDFPage, orignal_pieces: GraphicsObject, dst_rect: RectF) {
        val position = page.getFirstGraphicsObjectPosition(GraphicsObject.e_TypeAll)
        val pieces = orignal_pieces.clone()
        val piece_rect = pieces.rect

        // Calculates the transformation matrix between dst_rect and  piece_rect.
        val a = (dst_rect.right - dst_rect.left) / (piece_rect.right - piece_rect.left)
        val d = (dst_rect.top - dst_rect.bottom) / (piece_rect.top - piece_rect.bottom)
        val e = dst_rect.left - piece_rect.left * a
        val f = dst_rect.top - piece_rect.top * d

        // Transform rect.
        pieces.transform(Matrix2D(a, 0F, 0F, d, e, f), false)
        page.insertGraphicsObject(position, pieces)
        page.generateContent()
    }

    @Throws(PDFException::class)
    private fun addPathObjects(page: PDFPage, black_pieces: GraphicsObject, white_pieces: GraphicsObject) {
        val position = page.getLastGraphicsObjectPosition(GraphicsObject.e_TypePath)
        val path_object = PathObject.create()
        if (path_object == null) {
            Toast.makeText(context, context.getString(R.string.fx_add_graphics_failed), Toast.LENGTH_SHORT).show()
            return
        }
        val path = Path()
        val page_width = page.width
        val page_height = page.height
        val width = Math.min(page_width, page_height) / 20.0f
        val start_x = (page_width - width * 18.0f) / 2.0f
        val start_y = (page_height - width * 18.0f) / 2.0f

        // Draw a chess board
        for (i in 0..18) {
            var x1 = start_x
            var y1 = i * width + start_y
            val x2 = start_x + 18 * width
            path.moveTo(PointF(x1, y1))
            path.lineTo(PointF(x2, y1))
            x1 = i * width + start_x
            y1 = start_y
            val y2 = 18 * width + start_y
            path.moveTo(PointF(x1, y1))
            path.lineTo(PointF(x1, y2))
        }
        val star = intArrayOf(3, 9, 15)
        for (i in 0..2) {
            for (j in 0..2) {
                val rect = RectF(start_x + star[i] * width - width / 12, start_y + star[j] * width - width / 12,
                        start_x + star[i] * width + width / 12, start_y + star[j] * width + width / 12)
                path.appendEllipse(rect)
            }
        }
        path_object.pathData = path
        path_object.fillColor = -0x1000000
        path_object.fillMode = Constants.e_FillModeAlternate
        path_object.strokeState = true
        path_object.strokeColor = -0x1000000
        page.insertGraphicsObject(position, path_object)
        page.generateContent()

        // Draw pieces
        val pieces_vector = arrayOf(arrayOf(PointF(3F, 3F), PointF(3F, 7F), PointF(3F, 15F), PointF(13F, 2F),
                PointF(13F, 16F), PointF(13F, 17F), PointF(15F, 16F), PointF(16F, 16F)), arrayOf<PointF>(PointF(11F, 16F), PointF(12F, 14F), PointF(14F, 4F), PointF(14F, 15F),
                PointF(15F, 3F), PointF(15F, 9F), PointF(15F, 15F), PointF(16F, 15F)))
        for (k in 0..1) {
            for (i in 0 until pieces_vector[k].size) {
                val x = pieces_vector[k][i].x.toInt()
                val y = pieces_vector[k][i].y.toInt()
                addPieces(page, if (k % 2 != 0) white_pieces else black_pieces,
                        RectF(start_x + x * width - width / 2f, start_y + y * width - width / 2f,
                                start_x + x * width + width / 2f, start_y + y * width + width / 2f))
            }
        }
    }

    @Throws(PDFException::class)
    private fun addImageObjects(page: PDFPage, image_file: String) {
        val position = page.getLastGraphicsObjectPosition(GraphicsObject.e_TypeImage)
        val image = Image(image_file)
        val image_object = ImageObject.create(page.document)
        if (image_object == null) {
            Toast.makeText(context, context.getString(R.string.fx_add_graphics_failed), Toast.LENGTH_SHORT).show()
            return
        }
        image_object.setImage(image, 0)
        val width = image.width.toFloat()
        val height = image.height.toFloat()
        val page_width = page.width
        val page_height = page.height

        // Please notice the matrix value.
        image_object.matrix = Matrix2D(width, 0F, 0F, height, (page_width - width) / 2.0f, (page_height - height) / 2.0f)
        page.insertGraphicsObject(position, image_object)
        page.generateContent()
    }

}