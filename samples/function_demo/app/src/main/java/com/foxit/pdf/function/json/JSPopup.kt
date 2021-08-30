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

object JSPopup {
    fun exportToJSON(annot: Annot?): JSONObject {
        val `object` = JSONObject()
        try {
            val popup = Popup(annot)
            `object`.put("flags", JSAnnotUtil.flags2String(popup))
            `object`.put("name", popup.uniqueID)
            `object`.put("page", popup.page.index)
            `object`.put("open", if (popup.openStatus) "yes" else "no")
            val rect = popup.rect
            `object`.put("rect", String.format("%s,%s,%s,%s",
                    rect.left, rect.bottom, rect.right, rect.top))
            `object`.put("type", "popup")
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return `object`
    }
}