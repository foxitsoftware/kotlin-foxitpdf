/**
 * Copyright (C) 2003-2025, Foxit Software Inc..
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
import com.foxit.sdk.pdf.annots.Annot
import org.json.JSONObject
import com.foxit.pdf.function.json.JSMarkupAnnot
import com.foxit.sdk.pdf.annots.Ink
import org.json.JSONException
import com.foxit.sdk.PDFException
import com.foxit.pdf.function.json.JSAnnotUtil
import com.foxit.sdk.pdf.objects.PDFObject
import com.foxit.pdf.function.json.JSNote
import com.foxit.sdk.pdf.annots.Markup
import kotlin.Throws
import org.json.JSONArray
import com.foxit.sdk.pdf.annots.BorderInfo
import com.foxit.sdk.pdf.annots.Sound
import com.foxit.sdk.pdf.annots.Stamp
import com.foxit.sdk.pdf.annots.Redact
import com.foxit.sdk.pdf.annots.QuadPointsArray
import com.foxit.sdk.pdf.annots.QuadPoints
import com.foxit.sdk.pdf.annots.DefaultAppearance
import com.foxit.sdk.pdf.annots.Square
import com.foxit.sdk.common.fxcrt.PointFArray
import com.foxit.sdk.pdf.annots.FreeText
import com.foxit.sdk.pdf.annots.PolyLine
import com.foxit.sdk.pdf.objects.PDFDictionary
import com.foxit.sdk.pdf.objects.PDFArray
import com.foxit.sdk.pdf.objects.PDFStream
import com.foxit.pdf.function.json.JSAnnot
import android.text.TextUtils
import com.foxit.pdf.function.json.JSPopup
import com.foxit.sdk.pdf.annots.FileAttachment
import com.foxit.sdk.pdf.annots.TextMarkup
import java.lang.StringBuilder

object JSTextMarkupAnnot {
    fun exportToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        val `object` = JSMarkupAnnot.exportToJSON(doc, annot)
        try {
            val textMarkup = TextMarkup(annot)
            val quadPoints = textMarkup.quadPoints
            val jsonQP = StringBuilder()
            for (i in 0 until quadPoints.size) {
                val quadPoint = quadPoints.getAt(i)
                jsonQP.append(quadPoint.first.x)
                        .append(",")
                        .append(quadPoint.first.y)
                        .append(",")
                jsonQP.append(quadPoint.second.x)
                        .append(",")
                        .append(quadPoint.second.y)
                        .append(",")
                jsonQP.append(quadPoint.third.x)
                        .append(",")
                        .append(quadPoint.third.y)
                        .append(",")
                jsonQP.append(quadPoint.fourth.x)
                        .append(",")
                        .append(quadPoint.fourth.y)
                        .append(",")
            }
            `object`!!.put("coords", jsonQP.substring(0, jsonQP.length - 1))
        } catch (e: PDFException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return `object`
    }
}