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

object JSNote {
    fun exportToJSON(doc: PDFDoc, annot: Annot): JSONObject? {
        val `object` = JSMarkupAnnot.exportToJSON(doc, annot)
        try {
            val note = Note(annot)
            `object`!!.put("icon", note.iconName)
            if (note.isStateAnnot) {
                `object`.put("statemodel", if (note.stateModel == 1) "Marked" else "Review")
                var IRT: String? = null
                var IRTObj = annot.dict.getElement("IRT")
                if (IRTObj != null) {
                    IRTObj = IRTObj.dict
                    val IRTNum = IRTObj.getObjNum()
                    IRTObj = doc.getIndirectObject(IRTNum).dict.getElement("NM")
                    IRT = IRTObj.wideString
                }
                `object`.put("inreplyto", IRT)
                `object`.put("state", state2String(note.state))
                `object`.remove("contents")
                `object`.remove("color")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return `object`
    }

    private fun state2String(state: Int): String {
        return when (state) {
            Markup.e_StateMarked -> "Marked"
            Markup.e_StateUnmarked -> "Unmarked"
            Markup.e_StateAccepted -> "Accepted"
            Markup.e_StateRejected -> "Rejected"
            Markup.e_StateCancelled -> "Cancelled"
            Markup.e_StateCompleted -> "Completed"
            Markup.e_StateNone -> "None"
            else -> "None"
        }
    }
}