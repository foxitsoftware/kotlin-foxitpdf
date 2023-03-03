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
package com.foxit.pdf.function

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import com.foxit.pdf.function.Common.fixFolder
import com.foxit.pdf.function.Common.getOutputFilesFolder
import com.foxit.pdf.function.Common.getSuccessInfo
import com.foxit.pdf.function.Common.loadPDFDoc
import com.foxit.pdf.function.Common.loadPage
import com.foxit.pdf.main.R
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.DateTime
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.PDFPage
import com.foxit.sdk.pdf.Signature
import java.util.*

class Signature(private val mContext: Context) {
    fun addSignature(pageIndex: Int) {
        val inputPath = fixFolder + "AboutFoxit.pdf"
        val certPath = fixFolder + "foxit_all.pfx"
        val certPassword = "123456"
        val outputPath = getOutputFilesFolder(Common.SIGNATURE) + "Sample_addSign.pdf"
        val doc = loadPDFDoc(mContext, inputPath, null) ?: return
        try {
            val filter = "Adobe.PPKLite"
            val subfilter = "adbe.pkcs7.detached"
            val dn = "dn"
            val location = "location"
            val reason = "reason"
            val contactInfo = "contactInfo"
            val signer = "signer"
            val text = "text"
            var state: Long = 0
            val rect = RectF(100f, 100f, 300f, 300f)

            //set current time to dateTime.
            val dateTime = DateTime()
            val c = Calendar.getInstance()
            val timeZone = c.timeZone
            val offset = timeZone.rawOffset
            val tzHour = offset / (3600 * 1000)
            val tzMinute = offset / (1000 * 60) % 60
            val year = c[Calendar.YEAR]
            val month = c[Calendar.MONTH] + 1
            val day = c[Calendar.DATE]
            val hour = c[Calendar.HOUR]
            val minute = c[Calendar.MINUTE]
            val second = c[Calendar.SECOND]
            dateTime[year, month, day, hour, minute, second, 0, tzHour.toShort()] = tzMinute
            val pageCount = doc.pageCount
            if (pageIndex > pageCount || pageIndex < 0) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_the_page_index_out_of_range),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            val pdfPage = loadPage(mContext, doc, pageIndex, PDFPage.e_ParsePageNormal)
            if (pdfPage == null || pdfPage.isEmpty) {
                return
            }
            var signature = pdfPage.addSignature(rect)
            signature.filter = filter
            signature.subFilter = subfilter
            signature.setKeyValue(Signature.e_KeyNameDN, dn)
            signature.setKeyValue(Signature.e_KeyNameLocation, location)
            signature.setKeyValue(Signature.e_KeyNameReason, reason)
            signature.setKeyValue(Signature.e_KeyNameContactInfo, contactInfo)
            signature.setKeyValue(Signature.e_KeyNameSigner, signer)
            signature.setKeyValue(Signature.e_KeyNameText, text)
            signature.signTime = dateTime
            val flags = (Signature.e_APFlagSigningTime or Signature.e_APFlagFoxitFlag or
                    Signature.e_APFlagLocation or Signature.e_APFlagBitmap or
                    Signature.e_APFlagReason or Signature.e_APFlagSigner or
                    Signature.e_APFlagText or Signature.e_APFlagDN
                    or Signature.e_APFlagLabel)
            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.BLUE)
            signature.bitmap = bitmap
            signature.appearanceFlags = flags
            var progressive = signature.startSign(
                certPath,
                certPassword.toByteArray(),
                Signature.e_DigestSHA1,
                outputPath,
                null,
                null
            )
            var progress = Progressive.e_ToBeContinued
            while (progress == Progressive.e_ToBeContinued) {
                progress = progressive.resume()
            }
            state = signature.state.toLong()
            if (state != Signature.e_StateSigned.toLong() || !signature.isSigned) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_sign_failed),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            val signedDoc = loadPDFDoc(mContext, outputPath, null) ?: return
            val count = signedDoc.signatureCount
            if (count <= 0) return
            signature = signedDoc.getSignature(0)
            if (!signature.isSigned) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_doc_not_signed),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            progressive = signature.startVerify(null, null)
            progress = Progressive.e_ToBeContinued
            while (progress == Progressive.e_ToBeContinued) {
                progress = progressive.resume()
            }
            state = signature.state.toLong()
            if (state and Signature.e_StateVerifyNoChange.toLong() != Signature.e_StateVerifyNoChange.toLong()) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.fx_verify_failed),
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            Toast.makeText(mContext, getSuccessInfo(mContext, outputPath), Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(
                mContext,
                mContext.getString(R.string.fx_failed_to_sign_the_page, pageIndex, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}