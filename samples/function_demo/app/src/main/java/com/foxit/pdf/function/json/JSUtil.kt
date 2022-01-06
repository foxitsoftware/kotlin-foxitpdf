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
import java.util.ArrayList
import java.util.HashMap

internal object JSUtil {
    @Throws(JSONException::class)
    fun parseJSONObject(jsonObject: JSONObject?): Map<String?, Any?>? {
        var objectMap: MutableMap<String?, Any?>? = null
        if (null != jsonObject) {
            objectMap = HashMap()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val `object` = jsonObject.opt(key)
                if (null != `object`) {
                    objectMap[key] = parseValue(`object`)
                }
            }
        }
        return objectMap
    }

    @Throws(JSONException::class)
    fun parseValue(input: Any?): Any? {
        var output: Any? = null
        if (null != input) {
            if (input is JSONArray) {
                output = parseJSONArray(input as JSONArray?)
            } else if (input is JSONObject) {
                output = parseJSONObject(input as JSONObject?)
            } else if (input is String || input is Boolean || input is Int) {
                output = input
            }
        }
        return output
    }

    @Throws(JSONException::class)
    fun parseJSONArray(jsonArray: JSONArray?): List<Any?>? {
        var values: MutableList<Any?>? = null
        if (null != jsonArray) {
            values = ArrayList()
            for (i in 0 until jsonArray.length()) {
                val itemObject = jsonArray[i]
                if (null != itemObject) {
                    values.add(parseValue(itemObject))
                }
            }
        }
        return values
    }
}