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

object JSAnnot {
    fun exportToJSON(annot: Annot): JSONObject {
        val `object` = JSONObject()
        try {
            `object`.put("type", JSAnnotUtil.type2String(annot))
            `object`.put("color", JSAnnotUtil.colorConvertor(annot.borderColor and 0x00ffffff))
            `object`.put("flags", JSAnnotUtil.flags2String(annot))
            `object`.put("date", JSAnnotUtil.formatDocumentDate(annot.modifiedDateTime))
            `object`.put("name", annot.uniqueID)
            `object`.put("page", annot.page.index)
            `object`.put("rect", JSAnnotUtil.rect2String(annot))
            `object`.put("contents", annot.content)
            `object`.put("width", annot.borderInfo.width.toDouble())
            val customEntries = JSONObject()
            `object`.put("customEntries", customEntries)
            if (annot.dict != null && annot.dict.hasKey("customEntries")) {
                try {
                    val customEntriesStr = annot.dict.getElement("customEntries").wideString
                    val customJson = JSONObject(customEntriesStr)
                    val jsonMap = JSUtil.parseJSONObject(customJson)
                    for ((key, value) in jsonMap!!) {
                        customEntries.put(key, value)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            `object`.put("style", JSAnnotUtil.borderStyle2String(annot))
            val borderInfo = annot.borderInfo
            if (borderInfo.style == BorderInfo.e_Dashed) {
                `object`.put("dashPhase", borderInfo.dash_phase.toDouble())
                val dashes = StringBuilder()
                for (i in 0 until borderInfo.dashes.size) {
                    dashes.append(borderInfo.dashes.getAt(i))
                    dashes.append(",")
                }
                `object`.put("dashes", dashes.substring(0, dashes.length - 1))
            } else if (borderInfo.style == BorderInfo.e_Cloudy) {
                `object`.put("intensity", borderInfo.cloud_intensity.toDouble())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return `object`
    }
}