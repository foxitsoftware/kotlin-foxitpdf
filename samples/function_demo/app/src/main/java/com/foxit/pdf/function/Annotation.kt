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
import android.graphics.PointF
import android.graphics.RectF
import android.widget.Toast

import com.foxit.sdk.common.PDFException
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFTextSearch
import com.foxit.sdk.pdf.annots.Annot
import com.foxit.sdk.pdf.annots.Note
import com.foxit.sdk.pdf.annots.QuadPoints
import com.foxit.sdk.pdf.annots.TextMarkup

class Annotation(context: Context, pdfFilePath: String) {
    private var mFilePath = ""
    private var mContext: Context? = null

    private val searchText = arrayOf("Highlight_1", "Highlight_2", "Highlight_3", "Underline_1", "Underline_2", "Underline_3", "Strikeout_1", "Strikeout_2", "Strikeout_3", "Squiggly_1", "Squiggly_2", "Squiggly_3")

    private val color = longArrayOf(-0x10000, -0xff0100, -0xffff01)
    private val opacity = floatArrayOf(0.2f, 0.6f, 0.9f)
    private val textMarkupAnnotType = intArrayOf(Annot.e_annotHighlight, Annot.e_annotUnderline, Annot.e_annotStrikeOut, Annot.e_annotSquiggly)

    init {
        mFilePath = pdfFilePath
        mContext = context
    }

    fun addAnnotation() {
        val indexPdf = mFilePath.lastIndexOf(".")
        val indexSep = mFilePath.lastIndexOf("/")
        val filenameWithoutPdf = mFilePath.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.GetOutputFilesFolder(Common.annotationModuleName) + filenameWithoutPdf + "_add.pdf"

        val doc = Common.loadPDFDoc(mContext!!, mFilePath, null) ?: return

        try {
            //Add Note annotations.
            for (i in 0..2) {
                val noteAnnot = addAnnotation(doc, 0, Annot.e_annotNote, RectF((100 + i * 160).toFloat(), 200f, (120 + i * 160).toFloat(), 180f)) as Note?
                        ?: continue
                noteAnnot.iconName = "Comment"
                noteAnnot.borderColor = color[i % 3]

                //It should be reset appearance after being modified.
                noteAnnot.resetAppearanceStream()
            }

            //Add the TextMarkup annotations.
            for (i in searchText.indices) {
                val textMarkupAnnot = addTextmarkupAnnot(doc, searchText[i], textMarkupAnnotType[i / 3])
                        ?: continue
                textMarkupAnnot.borderColor = color[i % 3]
                textMarkupAnnot.opacity = opacity[i % 3]

                //It should be reset appearance after being modified.
                textMarkupAnnot.resetAppearanceStream()
            }

            doc.saveAs(outputFilePath, PDFDoc.e_saveFlagNormal.toLong())
        } catch (e: PDFException) {
            Toast.makeText(mContext, "Add annotation demo run error. " + e.message, Toast.LENGTH_LONG).show()
            return
        } finally {
            Common.releaseDoc(mContext!!, doc)
        }

        Toast.makeText(mContext, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
        return
    }

    //Add the TextMarkup annotations.
    private fun addTextmarkupAnnot(doc: PDFDoc, keywords: String, annotType: Int): TextMarkup? {
        if (annotType != Annot.e_annotSquiggly
                && annotType != Annot.e_annotStrikeOut
                && annotType != Annot.e_annotHighlight
                && annotType != Annot.e_annotUnderline) {
            return null
        }

        var textSearch: PDFTextSearch? = null
        var textMarkupAnnot: TextMarkup? = null
        try {
            //Firstly, search the text.
            textSearch = PDFTextSearch(doc, null)
            if (textSearch == null) {
                Toast.makeText(mContext, "create text search error", Toast.LENGTH_LONG).show()
                return null
            }

            if (!textSearch.setKeyWords(keywords)) {
                Toast.makeText(mContext, "set keywords error", Toast.LENGTH_LONG).show()
                textSearch.release()
                return null
            }

            val bMatch = textSearch.findNext()
            if (bMatch) {
                val rectCount = textSearch.matchRectCount

                //Next, calculate the quadPoints according to the matching rectangle.
                val quadPoints = arrayOfNulls<QuadPoints>(rectCount)

                for (i in 0 until rectCount) {
                    val textRectF = textSearch.getMatchRect(i)

                    quadPoints[i] = QuadPoints()
                    quadPoints[i]?.setFirst(PointF(textRectF.left, textRectF.top))
                    quadPoints[i]?.setSecond(PointF(textRectF.right, textRectF.top))
                    quadPoints[i]?.setThird(PointF(textRectF.left, textRectF.bottom))
                    quadPoints[i]?.setFourth(PointF(textRectF.right, textRectF.bottom))
                }

                val pageIndex = textSearch.matchPageIndex

                //Finally, add the TextMarkup annotation to the matching page.
                textMarkupAnnot = addAnnotation(doc, pageIndex, annotType, RectF(0f, 0f, 0f, 0f)) as TextMarkup?
                textMarkupAnnot!!.setQuadPoints(quadPoints)
            }
        } catch (e: PDFException) {
            Toast.makeText(mContext, "Get text rect error. " + e.message, Toast.LENGTH_LONG).show()
        } finally {
            try {
                if (textSearch != null) {
                    textSearch.release()
                }
            } catch (ee: PDFException) {
            }

            return textMarkupAnnot
        }
    }

    private fun addAnnotation(doc: PDFDoc, pageIndex: Int, annotType: Int, rect: RectF?): Annot? {
        if (rect == null) {
            return null
        }

        var annot: Annot? = null
        try {
            val page = doc.getPage(pageIndex) ?: return null
            annot = page.addAnnot(annotType, rect)
            if (annot != null) {
                //Set the unique ID to the annotation.
                val uuid = Common.randomUUID(null)
                annot.uniqueID = uuid

                //Set flags to the annotation.
                annot.flags = 4

                //Set the modified datetime to the annotation.
                val dateTime = Common.currentDateTime
                annot.modifiedDateTime = dateTime!!
            }
        } catch (e: PDFException) {
            Toast.makeText(mContext, "Add annot error. " + e.message, Toast.LENGTH_LONG)
        } finally {
            return annot
        }
    }
}
