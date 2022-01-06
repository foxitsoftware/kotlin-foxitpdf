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
import java.util.*

object JSFreeText {
    fun exportToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        val `object` = JSMarkupAnnot.exportToJSON(doc, annot)
        try {
            val freeText = FreeText(annot)
            `object`!!.put("justification", freeText.alignment)
            val defaultAppearance = freeText.defaultAppearance
            var DA = ""
            if (defaultAppearance.font != null && !defaultAppearance.font.isEmpty) {
                DA += "/" + defaultAppearance.font.name + " "
            }
            DA += defaultAppearance.text_size.toString() + " Tf "
            val rgba = JSAnnotUtil.convertFromNumberToRGBA(freeText.borderColor)
            DA += String.format(Locale.getDefault(), "%.2f", rgba!![0] / 255f) + " " + String.format(Locale.getDefault(), "%.2f", rgba[1] / 255f) + " " + String.format(Locale.getDefault(), "%.2f", rgba[2] / 255f) + " rg"
            `object`.put("defaultappearance", DA)
            `object`.put("fontColor", JSAnnotUtil.colorConvertor(defaultAppearance.text_color and 0x00ffffff))
            if (freeText.fillColor != 0) {
                val fillColor = JSAnnotUtil.convertFromNumberToHex(freeText.fillColor and 0x00ffffff)
                `object`.put("interior-color", fillColor)
                `object`.put("interiorColor", fillColor)
            }
            val intent = freeText.intent
            if ("FreeTextCallout" == intent) {
                val pdfRect = freeText.rect
                val innerRect = freeText.innerRect
                var fringe = (innerRect.left - pdfRect.left).toString()
                fringe += ","
                fringe += (innerRect.bottom - pdfRect.bottom).toString()
                fringe += ","
                fringe += (pdfRect.right - innerRect.right).toString()
                fringe += ","
                fringe += (pdfRect.top - innerRect.top).toString()
                `object`.put("fringe", fringe)
                val pointFArray = freeText.calloutLinePoints
                var callout = pointFArray.getAt(0).x.toString() + "," + pointFArray.getAt(0).y
                callout += "," + pointFArray.getAt(1).x + "," + pointFArray.getAt(1).y
                callout += "," + pointFArray.getAt(2).x + "," + pointFArray.getAt(2).y
                `object`.put("callout", callout)
                `object`.put("head", JSAnnotUtil.makeupEndingStyle(freeText.calloutLineEndingStyle))
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return `object`
    }
}