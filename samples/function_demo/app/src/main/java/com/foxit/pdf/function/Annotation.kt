/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
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
import com.foxit.sdk.common.fxcrt.PointF
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.TextSearch
import com.foxit.sdk.pdf.annots.Annot
import com.foxit.sdk.pdf.annots.Note
import com.foxit.sdk.pdf.annots.QuadPoints
import com.foxit.sdk.pdf.annots.QuadPointsArray
import com.foxit.sdk.pdf.annots.TextMarkup

class Annotation(var context: Context, var pdfFilePath: String) {
    private val searchText = arrayOf("Highlight_1", "Highlight_2", "Highlight_3", "Underline_1", "Underline_2", "Underline_3", "Strikeout_1", "Strikeout_2", "Strikeout_3", "Squiggly_1", "Squiggly_2", "Squiggly_3")

    private val color = intArrayOf(-0x10000, -0xff0100, -0xffff01)
    private val opacity = floatArrayOf(0.2f, 0.6f, 0.9f)
    private val textMarkupType = intArrayOf(Annot.e_Highlight, Annot.e_Underline, Annot.e_StrikeOut, Annot.e_Squiggly)

    fun addAnnotation() {
        val indexPdf = pdfFilePath.lastIndexOf(".")
        val indexSep = pdfFilePath.lastIndexOf("/")
        val filenameWithoutPdf = pdfFilePath.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.getOutputFilesFolder(Common.annotationModuleName) + filenameWithoutPdf + "_add.pdf"

        val doc = Common.loadPDFDoc(context, pdfFilePath, null) ?: return

        try {
            //Add Note annotations.
            for (i in 0..2) {
                val annot = addAnnotation(doc, 0, Annot.e_Note, RectF((100 + i * 160).toFloat(), 180f, (120 + i * 160).toFloat(), 200f))
                if (annot == null || annot.isEmpty) continue
                val noteAnnot = Note(annot)
                noteAnnot.iconName = "Comment"
                noteAnnot.borderColor = color[i % 3]

                //It should be reset appearance after being modified.
                noteAnnot.resetAppearanceStream()
            }

            //Add the TextMarkup annotations.
            for (i in searchText.indices) {
                val textMarkupAnnot = addTextMarkup(doc, searchText[i], textMarkupType[i / 3])
                if (textMarkupAnnot == null || textMarkupAnnot.isEmpty) continue
                textMarkupAnnot.borderColor = color[i % 3]
                textMarkupAnnot.opacity = opacity[i % 3]

                //It should be reset appearance after being modified.
                textMarkupAnnot.resetAppearanceStream()
            }

            doc.saveAs(outputFilePath, PDFDoc.e_SaveFlagNormal)
        } catch (e: PDFException) {
            Toast.makeText(context, "Add annotation demo run error. " + e.message, Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(context, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
        return
    }

    //Add the TextMarkup annotations.
    private fun addTextMarkup(doc: PDFDoc, keywords: String, annotType: Int): TextMarkup? {
        if (annotType != Annot.e_Squiggly
                && annotType != Annot.e_StrikeOut
                && annotType != Annot.e_Highlight
                && annotType != Annot.e_Underline) {
            return null
        }

        var textSearch: TextSearch? = null
        var textMarkupAnnot: TextMarkup? = null
        try {
            //Firstly, search the text.
            textSearch = TextSearch(doc, null)
            if (textSearch == null || textSearch.isEmpty) {
                Toast.makeText(context, "create text search error", Toast.LENGTH_LONG).show()
                return null
            }

            if (!textSearch.setPattern(keywords)) {
                Toast.makeText(context, "set keywords error", Toast.LENGTH_LONG).show()
                textSearch.delete()
                return null
            }

            val bMatch = textSearch.findNext()
            if (bMatch) {
                val rectFArray = textSearch.matchRects
                val rectCount = rectFArray.size

                //Next, calculate the quadPoints according to the matching rectangle.
                val quadPointsArray = QuadPointsArray()

                for (i in 0 until rectCount) {
                    val textRectF = rectFArray.getAt(i)

                    val quadPoints = QuadPoints()
                    quadPoints.first = PointF(textRectF.left, textRectF.top)
                    quadPoints.second = PointF(textRectF.right, textRectF.top)
                    quadPoints.third = PointF(textRectF.left, textRectF.bottom)
                    quadPoints.fourth = PointF(textRectF.right, textRectF.bottom)

                    quadPointsArray.add(quadPoints)
                }

                val pageIndex = textSearch.matchPageIndex

                //Finally, add the TextMarkup annotation to the matching page.
                textMarkupAnnot = TextMarkup(addAnnotation(doc, pageIndex, annotType, RectF(0f, 0f, 0f, 0f)))
                textMarkupAnnot.quadPoints = quadPointsArray
            }
        } catch (e: PDFException) {
            Toast.makeText(context, "Get text rect error. " + e.message, Toast.LENGTH_LONG).show()
        }
        return textMarkupAnnot
    }

    private fun addAnnotation(doc: PDFDoc, pageIndex: Int, annotType: Int, rect: RectF?): Annot? {
        if (rect == null) {
            return null
        }

        var annot: Annot? = null
        try {
            val page = doc.getPage(pageIndex)
            if (page == null || page.isEmpty) {
                return null
            }
            annot = page.addAnnot(annotType, rect)
            if (annot != null && !annot.isEmpty) {
                //Set the unique ID to the annotation.
                val uuid = Common.randomUUID(null)
                annot.uniqueID = uuid

                //Set flags to the annotation.
                annot.flags = 4

                //Set the modified datetime to the annotation.
                val dateTime = Common.currentDateTime
                annot.modifiedDateTime = dateTime
            }
        } catch (e: PDFException) {
            Toast.makeText(context, "Add annot error. " + e.message, Toast.LENGTH_LONG)
        }
        return annot
    }
}
