/**
 * Copyright (C) 2003-2019, Foxit Software Inc..
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
import com.foxit.pdf.main.R

import com.foxit.sdk.common.DateTime
import com.foxit.sdk.PDFException
import com.foxit.sdk.common.Progressive
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.PDFPage

import java.util.Calendar

class Signature(var context: Context, var docPath: String, var certPath: String, var certPassword: String) {

    fun addSignature(pageIndex: Int) {
        val indexPdf = docPath.lastIndexOf(".")
        val indexSep = docPath.lastIndexOf("/")
        val filenameWithoutPdf = docPath.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.getOutputFilesFolder(Common.signatureModuleName) + filenameWithoutPdf + "_add.pdf"
        val doc = Common.loadPDFDoc(context, docPath, null) ?: return

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
            val value: String? = null
            val rect = RectF(100f, 100f, 300f, 300f)

            //set current time to dateTime.
            val dateTime = DateTime()
            val c = Calendar.getInstance()
            val timeZone = c.timeZone
            val offset = timeZone.rawOffset
            val tzHour = offset / (3600 * 1000)
            val tzMinute = offset / (1000 * 60) % 60
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DATE)
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)
            dateTime.set(year, month, day, hour, minute, second, 0, tzHour.toShort(), tzMinute)

            val pageCount = doc.pageCount
            if (pageIndex > pageCount || pageIndex < 0) {
                Toast.makeText(context, context.getString(R.string.fx_the_page_index_out_of_range), Toast.LENGTH_LONG).show()
                return
            }

            val pdfPage = Common.loadPage(context, doc, pageIndex, PDFPage.e_ParsePageNormal)
            if (pdfPage == null || pdfPage.isEmpty) {
                return
            }

            var signature: com.foxit.sdk.pdf.Signature = pdfPage.addSignature(rect)
            signature.filter = filter
            signature.subFilter = subfilter

            signature.setKeyValue(com.foxit.sdk.pdf.Signature.e_KeyNameDN, dn)
            signature.setKeyValue(com.foxit.sdk.pdf.Signature.e_KeyNameLocation, location)
            signature.setKeyValue(com.foxit.sdk.pdf.Signature.e_KeyNameReason, reason)
            signature.setKeyValue(com.foxit.sdk.pdf.Signature.e_KeyNameContactInfo, contactInfo)
            signature.setKeyValue(com.foxit.sdk.pdf.Signature.e_KeyNameSigner, signer)
            signature.setKeyValue(com.foxit.sdk.pdf.Signature.e_KeyNameText, text)
            signature.signTime = dateTime
            val flags = (com.foxit.sdk.pdf.Signature.e_APFlagSigningTime or com.foxit.sdk.pdf.Signature.e_APFlagFoxitFlag or
                    com.foxit.sdk.pdf.Signature.e_APFlagLocation or com.foxit.sdk.pdf.Signature.e_APFlagBitmap or
                    com.foxit.sdk.pdf.Signature.e_APFlagReason or com.foxit.sdk.pdf.Signature.e_APFlagSigner or
                    com.foxit.sdk.pdf.Signature.e_APFlagText or com.foxit.sdk.pdf.Signature.e_APFlagDN
                    or com.foxit.sdk.pdf.Signature.e_APFlagLabel)

            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.BLUE)
            signature.bitmap = bitmap

            signature.appearanceFlags = flags

            var progressive = signature.startSign(certPath, certPassword.toByteArray(), com.foxit.sdk.pdf.Signature.e_DigestSHA1, outputFilePath, null, null)
            var progress = Progressive.e_ToBeContinued
            while (progress == Progressive.e_ToBeContinued) {
                progress = progressive.resume()
            }

            state = signature.state.toLong()
            if (state != com.foxit.sdk.pdf.Signature.e_StateSigned.toLong() || !signature.isSigned) {
                Toast.makeText(context, context.getString(R.string.fx_sign_failed), Toast.LENGTH_LONG).show()
                return
            }

            val signedDoc = Common.loadPDFDoc(context, outputFilePath, null)
            val count = signedDoc!!.signatureCount
            if (count <= 0)
                return
            signature = signedDoc.getSignature(0)

            if (!signature.isSigned) {
                Toast.makeText(context, context.getString(R.string.fx_doc_not_signed), Toast.LENGTH_LONG).show()
                return
            }


            progressive = signature.startVerify(null, null)
            progress = Progressive.e_ToBeContinued
            while (progress == Progressive.e_ToBeContinued) {
                progress = progressive.resume()
            }

            state = signature.state.toLong()
            if (state and com.foxit.sdk.pdf.Signature.e_StateVerifyValid.toLong() != com.foxit.sdk.pdf.Signature.e_StateVerifyValid.toLong()) {
                Toast.makeText(context, context.getString(R.string.fx_verify_failed), Toast.LENGTH_LONG).show()
                return
            }
            Toast.makeText(context, Common.getSuccessInfo(context, outputFilePath), Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(context, context.getString(R.string.fx_failed_to_sign_the_page, pageIndex, e.message), Toast.LENGTH_LONG).show()
        }
    }
}
