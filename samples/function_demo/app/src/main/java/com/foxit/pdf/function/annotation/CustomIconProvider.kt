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
package com.foxit.pdf.function.annotation

import android.content.Context
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Constants
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.pdf.annots.Annot
import com.foxit.sdk.pdf.annots.IconProviderCallback
import com.foxit.sdk.pdf.annots.ShadingColor
import java.util.*

internal class CustomIconProvider(context: Context) : IconProviderCallback() {
    private val mStampDocMap: HashMap<String, PDFDoc>
    private var mIsDynamicStamp: Boolean
    private val mContext: Context
    override fun release() {}

    // If one icon provider offers different style icon for one icon name of a kind of annotaion,
    // please use different provider ID or version in order to distinguish different style for Foxit PDF SDK.
    // Otherwise, only the first style icon for the same icon name of same kind of annotation will have effect.
    override fun getProviderID(): String {
        return if (mIsDynamicStamp) {
            "Simple Demo Dynamic IconProvider"
        } else {
            "Simple Demo IconProvider"
        }
    }

    override fun getProviderVersion(): String {
        return "1.0.0"
    }

    override fun hasIcon(annot_type: Int, icon_name: String): Boolean {
        if (annot_type != Annot.e_Stamp) return false
        val assetsPath: String
        assetsPath = if (mIsDynamicStamp) {
            "DynamicStamps/$icon_name.pdf"
        } else {
            "StaticStamps/$icon_name.pdf"
        }
        try {
            var doc = mStampDocMap[assetsPath]
            if (doc != null && !doc.isEmpty) {
                return doc.pageCount >= 1
            }
            val `is` = mContext.assets.open(assetsPath)
            val length = `is`.available()
            val buffer = ByteArray(length)
            `is`.read(buffer)
            doc = PDFDoc(buffer)
            val error_code = doc.load(null)
            if (Constants.e_ErrSuccess == error_code) {
                mStampDocMap[assetsPath] = doc
            }
            `is`.close()
            return !doc.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun canChangeColor(annot_type: Int, icon_name: String): Boolean {
        return false
    }

    override fun getIcon(annot_type: Int, icon_name: String, color: Int): PDFPage? {
        if (annot_type != Annot.e_Stamp) return null
        val assetsPath: String
        assetsPath = if (mIsDynamicStamp) {
            "DynamicStamps/$icon_name.pdf"
        } else {
            "StaticStamps/$icon_name.pdf"
        }
        try {
            val doc = mStampDocMap[assetsPath]
            return if (doc == null || doc.isEmpty || doc.pageCount < 1) null else doc.getPage(0)
        } catch (e: PDFException) {
            e.printStackTrace()
        }
        return null
    }

    override fun getShadingColor(
        annot_type: Int,
        icon_name: String,
        referenced_color: Int,
        shading_index: Int,
        out_shading_color: ShadingColor
    ): Boolean {
        return false
    }

    override fun getDisplayWidth(annot_type: Int, icon_name: String): Float {
        return 0.0f
    }

    override fun getDisplayHeight(annot_type: Int, icon_name: String): Float {
        return 0.0f
    }

    fun setUseDynamicStamp(isDynamicStamp: Boolean) {
        mIsDynamicStamp = isDynamicStamp
    }

    init {
        mStampDocMap = HashMap()
        mIsDynamicStamp = false
        mContext = context
    }
}