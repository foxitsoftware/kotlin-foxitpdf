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

object JSMarkupAnnot {
    fun exportToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        val `object` = JSAnnot.exportToJSON(annot)
        try {
            val markup = Markup(annot)
            `object`!!.put("opacity", markup.opacity.toDouble())
            `object`.put("creationdate", JSAnnotUtil.formatDocumentDate(markup.creationDateTime))
            `object`.put("subject", markup.subject)
            `object`.put("title", markup.title)
            val intent = markup.intent
            if (!TextUtils.isEmpty(intent)) {
                `object`.put("IT", intent)
                `object`.put("intent", intent)
            }
            val dict = markup.dict
            var RT: String? = null
            var IRT: String? = null
            var IRTObj = dict.getElement("IRT")
            if (IRTObj != null) {
                IRTObj = IRTObj.dict
                val IRTNum = IRTObj.getObjNum()
                IRTObj = doc.getIndirectObject(IRTNum).dict.getElement("NM")
                IRT = IRTObj.wideString
                val RTObj = dict.getElement("RT")
                if (!dict.hasKey("StateModel") && RTObj != null) {
                    RT = RTObj.name
                    RT = RT ?: "R"
                } else {
                    RT = "R"
                }
            }
            if (RT != null && IRT != null) {
                `object`.put("inreplyto", IRT)
                `object`.put("replyType", RT.toLowerCase())
            }
            if (markup.popup != null && !markup.popup.isEmpty) {
                val popupObject = JSPopup.exportToJSON(markup.popup)
                `object`.put("popup", popupObject)
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return `object`
    }
}