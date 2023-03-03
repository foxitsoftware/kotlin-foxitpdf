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
import com.foxit.sdk.common.DateTime
import com.foxit.sdk.pdf.annots.FileAttachment
import com.foxit.sdk.pdf.annots.TextMarkup
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal object JSAnnotUtil {
    fun type2String(annot: Annot): String {
        try {
            val type = annot.type
            return when (type) {
                Annot.e_Note -> "text"
                Annot.e_Link -> "link"
                Annot.e_FreeText -> "freetext"
                Annot.e_Line -> "line"
                Annot.e_Square -> "square"
                Annot.e_Circle -> "circle"
                Annot.e_Polygon -> "polygon"
                Annot.e_PolyLine -> "polyline"
                Annot.e_Highlight -> "highlight"
                Annot.e_Underline -> "underline"
                Annot.e_Squiggly -> "squiggly"
                Annot.e_StrikeOut -> "strikeout"
                Annot.e_Stamp -> "stamp"
                Annot.e_Caret -> "caret"
                Annot.e_Ink -> "ink"
                Annot.e_PSInk -> "psink"
                Annot.e_FileAttachment -> "fileattachment"
                Annot.e_Sound -> "sound"
                Annot.e_Movie -> "movie"
                Annot.e_Widget -> "widget"
                Annot.e_Screen -> "screen"
                Annot.e_PrinterMark -> "printermark"
                Annot.e_TrapNet -> "trapnet"
                Annot.e_Watermark -> "watermark"
                Annot.e_3D -> "3D"
                Annot.e_Popup -> "popup"
                Annot.e_Redact -> "redact"
                Annot.e_RichMedia -> "richmedia"
                else -> "unknown"
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return "unknown"
    }

    fun flags2String(annot: Annot): String {
        try {
            val flags = annot.flags
            val builder = StringBuilder()
            if (flags and Annot.e_FlagInvisible == Annot.e_FlagInvisible) {
                builder.append("invisible")
                builder.append(",")
            }
            if (flags and Annot.e_FlagHidden == Annot.e_FlagHidden) {
                builder.append("hidden")
                builder.append(",")
            }
            if (flags and Annot.e_FlagPrint == Annot.e_FlagPrint) {
                builder.append("print")
                builder.append(",")
            }
            if (flags and Annot.e_FlagNoZoom == Annot.e_FlagNoZoom) {
                builder.append("nozoom")
                builder.append(",")
            }
            if (flags and Annot.e_FlagNoRotate == Annot.e_FlagNoRotate) {
                builder.append("norotate")
                builder.append(",")
            }
            if (flags and Annot.e_FlagNoView == Annot.e_FlagNoView) {
                builder.append("noview")
                builder.append(",")
            }
            if (flags and Annot.e_FlagReadOnly == Annot.e_FlagReadOnly) {
                builder.append("readonly")
                builder.append(",")
            }
            if (flags and Annot.e_FlagLocked == Annot.e_FlagLocked) {
                builder.append("locked")
                builder.append(",")
            }
            if (flags and Annot.e_FlagToggleNoView == Annot.e_FlagToggleNoView) {
                builder.append("togglenoview")
                builder.append(",")
            }
            if (flags and Annot.e_FlagLockedContents == Annot.e_FlagLockedContents) {
                builder.append("lockedcontents")
                builder.append(",")
            }
            val flagsStr = builder.toString()
            return if (flagsStr.contains(",") && flagsStr.split(",").toTypedArray().size == 1) flagsStr.substring(0, flagsStr.length - 1) else flagsStr
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return ""
    }

    fun borderStyle2String(annot: Annot): String {
        try {
            val style = annot.borderInfo.style
            return when (style) {
                BorderInfo.e_Solid -> "solid"
                BorderInfo.e_Dashed -> "dashed"
                BorderInfo.e_UnderLine -> "underline"
                BorderInfo.e_Beveled -> "beveled"
                BorderInfo.e_Inset -> "inset"
                BorderInfo.e_Cloudy -> "cloudy"
                else -> "none"
            }
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return "none"
    }

    fun makeupEndingStyle(style: Int): String {
        return when (style) {
            Markup.e_EndingStyleNone -> "None"
            Markup.e_EndingStyleSquare -> "Square"
            Markup.e_EndingStyleCircle -> "Circle"
            Markup.e_EndingStyleDiamond -> "Diamond"
            Markup.e_EndingStyleOpenArrow -> "OpenArrow"
            Markup.e_EndingStyleClosedArrow -> "ClosedArrow"
            Markup.e_EndingStyleButt -> "Butt"
            Markup.e_EndingStyleROpenArrow -> "ROpenArrow"
            Markup.e_EndingStyleRClosedArrow -> "RClosedArrow"
            Markup.e_EndingStyleSlash -> "Slash"
            else -> "None"
        }
    }

    fun rect2String(annot: Annot): String {
        try {
            val rectF = annot.rect
            return rectF.left.toString() +
                    "," +
                    rectF.bottom +
                    "," +
                    rectF.right +
                    "," +
                    rectF.top
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return ""
    }

    fun colorConvertor(color: Int): String {
        val r = color and 0xff0000 shr 16
        val g = color and 0xff00 shr 8
        val b = color and 0xff
        val sb = StringBuilder()
        var R = Integer.toHexString(r)
        var G = Integer.toHexString(g)
        var B = Integer.toHexString(b)
        R = if (R.length == 1) "0$R" else R
        G = if (G.length == 1) "0$G" else G
        B = if (B.length == 1) "0$B" else B
        sb.append("#")
        sb.append(R)
        sb.append(G)
        sb.append(B)
        return sb.toString()
    }

    fun convertFromNumberToHex(color: Int): String {
        return String.format("#%06X", 0xffffff and color)
    }

    fun convertFromNumberToRGBA(color: Int): IntArray {
        val a = color ushr 24
        val r = color and 0xff0000 shr 16
        val g = color and 0xff00 shr 8
        val b = color and 0xff
        return intArrayOf(r, g, b, a)
    }

    private const val MICROSECONDS_PER_MINUTE = 60000
    private const val MICROSECONDS_PER_HOUR = 3600000
    fun formatDocumentDate(pattern: String?, dateTime: DateTime): String {
        if (isZero(dateTime)) {
            return "0"
        }
        val date = documentDateToJavaDate(dateTime)
        val dateFormat: DateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun formatDocumentDate(dateTime: DateTime): String {
        return if (isZero(dateTime)) {
            "D:19700101080000+08'00'"
        } else String.format(Locale.getDefault(), "D:%d%02d%02d%02d%02d%02d+%02d'%02d'",
                dateTime.year,
                dateTime.month,
                dateTime.day,
                dateTime.hour,
                dateTime.minute,
                dateTime.second,
                dateTime.utc_hour_offset,
                dateTime.utc_minute_offset)
    }

    fun isZero(dateTime: DateTime): Boolean {
        return dateTime.year == 0 && dateTime.month == 0 && dateTime.day == 0 && dateTime.hour == 0 && dateTime.minute == 0 && dateTime.second == 0 && dateTime.utc_hour_offset.toInt() == 0 && dateTime.utc_minute_offset == 0
    }

    fun documentDateToJavaDate(dateTime: DateTime?): Date? {
        if (dateTime == null) return null
        val calendar = Calendar.getInstance()
        calendar[Calendar.YEAR] = dateTime.year
        calendar[Calendar.MONTH] = dateTime.month - 1
        calendar[Calendar.DAY_OF_MONTH] = dateTime.day
        calendar[Calendar.HOUR_OF_DAY] = dateTime.hour
        calendar[Calendar.MINUTE] = dateTime.minute
        calendar[Calendar.SECOND] = dateTime.second
        val rawOffset = dateTime.utc_minute_offset * MICROSECONDS_PER_MINUTE + dateTime.utc_hour_offset * MICROSECONDS_PER_HOUR - TimeZone.getDefault().rawOffset
        return Date(calendar.timeInMillis - rawOffset)
    }

    fun retriveDict(json: JSONObject, root: PDFObject): Any? {
        try {
            val jsonObject = JSONObject()
            var dict: PDFDictionary? = null
            val hexCode = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
            when (root.type) {
                1 -> return root.wideString
                2 -> {
                    return if (Math.abs(root.integer - root.float) < 0.001) {
                        root.integer
                    } else root.float
                }
                3 -> {
                    val buffer = root.string
                    val sb = StringBuilder()
                    sb.append("<")
                    for (ch in buffer) {
                        sb.append(hexCode[ch.toInt() and 0xf0 shr 4])
                        sb.append(hexCode[ch.toInt() and 0x0f])
                    }
                    sb.append(">")
                    return sb.toString()
                }
                4 -> return "/" + root.name
                5 -> {
                    // array:
                    val jsonArray = JSONArray()
                    val pdfArr = root.array
                    val size = pdfArr.elementCount
                    var i = 0
                    while (i < size) {
                        jsonArray.put(retriveDict(json, pdfArr.getElement(i)))
                        i++
                    }
                    return jsonArray
                }
                7 -> {
                    run {
                        // e_Stream
                        val stream = root.stream
                        dict = stream.dictionary
                        val size = stream.getDataSize(true)
                        if (size > 0) {
                            val temp = ByteArray(size)
                            stream.getData(true, size.toLong(), temp)
                            val rawData = StringBuilder()
                            for (ch in temp) {
                                rawData.append(hexCode[ch.toInt() and 0xf0 shr 4])
                                rawData.append(hexCode[ch.toInt() and 0x0f])
                            }
                            jsonObject.put("stream", rawData.toString())
                        }
                    }
                    run {
                        // dict:
                        if (dict == null) dict = root.dict
                        if (dict == null) return jsonObject
                        val type = dict!!.getElement("Type")
                        if (type != null && type.wideString == "OCG") {
                            return JSONObject()
                        }
                        var pos = dict!!.moveNext(0)
                        while (pos != 0L) {
                            val key2 = dict!!.getKey(pos)
                            val obj = dict!!.getElement(key2)
                            jsonObject.put(key2, retriveDict(json, obj))
                            pos = dict!!.moveNext(pos)
                        }
                        return jsonObject
                    }
                }
                6 -> {
                    if (dict == null) dict = root.dict
                    if (dict == null) return jsonObject
                    val type = dict!!.getElement("Type")
                    if (type != null && type.wideString == "OCG") {
                        return JSONObject()
                    }
                    var pos = dict!!.moveNext(0)
                    while (pos != 0L) {
                        val key2 = dict!!.getKey(pos)
                        val obj = dict!!.getElement(key2)
                        jsonObject.put(key2, retriveDict(json, obj))
                        pos = dict!!.moveNext(pos)
                    }
                    return jsonObject
                }
                8 -> return null
                9 -> {
                    // reference
                    val objNumber = root.directObject.objNum
                    val objNote = objNumber.toString() + "R"
                    json.put(objNumber.toString() + "", retriveDict(json, root.directObject))
                    return objNote
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return null
    }
}