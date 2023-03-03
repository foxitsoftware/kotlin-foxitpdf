/**
 * Copyright (C) 2003-2023, Foxit Software Inc..
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

object JSLine {
    fun exportToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        val `object` = JSMarkupAnnot.exportToJSON(doc, annot)
        try {
            val line = Line(annot)
            if (line.styleFillColor != 0) {
                val color = JSAnnotUtil.convertFromNumberToHex(line.styleFillColor and 0x00ffffff)
                `object`!!.put("interior-color", color)
                `object`.put("interiorColor", color)
            }
            `object`!!.put("start", String.format("%s,%s",
                    line.startPoint.x, line.startPoint.y))
            `object`.put("end", String.format("%s,%s",
                    line.endPoint.x, line.endPoint.y))
            `object`.put("head", JSAnnotUtil.makeupEndingStyle(line.lineStartStyle))
            `object`.put("tail", JSAnnotUtil.makeupEndingStyle(line.lineEndStyle))
            `object`.put("leaderLength", line.leaderLineLength.toDouble())
            `object`.put("leaderExtend", line.leaderLineExtensionLength.toDouble())
            `object`.put("caption", if (line.hasCaption()) "yes" else "no")
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return `object`
    }
}