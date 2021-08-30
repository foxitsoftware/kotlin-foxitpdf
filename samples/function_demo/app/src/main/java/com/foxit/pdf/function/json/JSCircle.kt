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
package com.foxit.pdf.function.json

import com.foxit.sdk.pdf.PDFDoc
import org.json.JSONObject
import com.foxit.pdf.function.json.JSMarkupAnnot
import org.json.JSONException
import com.foxit.sdk.PDFException
import com.foxit.pdf.function.json.JSAnnotUtil
import com.foxit.sdk.pdf.objects.PDFObject
import com.foxit.pdf.function.json.JSNote
import kotlin.Throws
import org.json.JSONArray
import com.foxit.sdk.common.fxcrt.PointFArray
import com.foxit.sdk.pdf.objects.PDFDictionary
import com.foxit.sdk.pdf.objects.PDFArray
import com.foxit.sdk.pdf.objects.PDFStream
import com.foxit.pdf.function.json.JSAnnot
import android.text.TextUtils
import com.foxit.pdf.function.json.JSPopup
import com.foxit.sdk.pdf.annots.*

object JSCircle {
    fun exportToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        val `object` = JSMarkupAnnot.exportToJSON(doc, annot)
        try {
            val circle = Circle(annot)
            val pdfRect = circle.rect
            val innerRect = circle.innerRect
            var fringe = (innerRect.left - pdfRect.left).toString()
            fringe += ","
            fringe += (innerRect.bottom - pdfRect.bottom).toString()
            fringe += ","
            fringe += (pdfRect.right - innerRect.right).toString()
            fringe += ","
            fringe += (pdfRect.top - innerRect.top).toString()
            `object`!!.put("fringe", fringe)
            if (circle.fillColor != 0) {
                val fillColor = JSAnnotUtil.colorConvertor(circle.fillColor and 0x00ffffff)
                `object`.put("interior-color", fillColor)
                `object`.put("interiorColor", fillColor)
            }
            if ("CircleDimension" == circle.intent) {
                `object`.put("measureConversionFactor", circle.getMeasureConversionFactor(Markup.e_MeasureTypeD).toDouble())
                `object`.put("measureRatio", circle.measureRatioW)
                `object`.put("measureUnit", circle.getMeasureUnit(Markup.e_MeasureTypeD))
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return `object`
    }
}