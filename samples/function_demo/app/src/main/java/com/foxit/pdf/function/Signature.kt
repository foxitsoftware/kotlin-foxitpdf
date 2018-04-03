/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
 * All Rights Reserved.
 *
 *
 * http://www.foxitsoftware.com
 *
 *
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit Mobile PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.foxit.pdf.function

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.widget.Toast

import com.foxit.sdk.common.CommonDefines
import com.foxit.sdk.common.DateTime
import com.foxit.sdk.common.Library
import com.foxit.sdk.common.PDFException
import com.foxit.sdk.pdf.PDFPage

import java.util.Calendar

class Signature(context: Context, docPath: String, certPath: String, certPassword: String) {
    private var mContext: Context? = null
    private var mDocPath: String? = null
    private var mCertPath: String? = null
    private var mCertPassword: String? = null

    init {
        mContext = context
        mDocPath = docPath
        mCertPath = certPath
        mCertPassword = certPassword
    }

    fun addSignature(pageIndex: Int) {
        val indexPdf = mDocPath!!.lastIndexOf(".")
        val indexSep = mDocPath!!.lastIndexOf("/")
        val filenameWithoutPdf = mDocPath!!.substring(indexSep + 1, indexPdf)
        val outputFilePath = Common.GetOutputFilesFolder(Common.signatureModuleName) + filenameWithoutPdf + "_add.pdf"
        val doc = Common.loadPDFDoc(mContext!!, mDocPath, null) ?: return

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
            val rect = RectF()
            rect.set(100f, 300f, 300f, 100f)

            //set current time to dateTime.
            val dateTime = DateTime()
            val c = Calendar.getInstance()
            val timeZone = c.timeZone
            val offset = timeZone.rawOffset
            val tzHour = offset / (3600 * 1000)
            val tzMinute = offset / 1000 % 3600
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH) + 1
            val day = c.get(Calendar.DATE)
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)
            dateTime.set(year, month, day, hour, minute, second, 0, tzHour.toShort(), tzMinute)

            val ret = Library.registerDefaultSignatureHandler()
            if (ret == false)
                return


            val pageCount = doc.pageCount
            if (pageIndex > pageCount || pageIndex < 0) {
                Toast.makeText(mContext, String.format("The page index is out of range!"), Toast.LENGTH_LONG).show()
                return
            }

            val pdfPage = Common.loadPage(mContext!!, doc, pageIndex, PDFPage.e_parsePageNormal)
                    ?: return
            var signature: com.foxit.sdk.pdf.signature.Signature = pdfPage.addSignature(rect)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameFilter, filter)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameSubFilter, subfilter)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameDN, dn)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameLocation, location)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameReason, reason)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameContactInfo, contactInfo)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameSigner, signer)
            signature.setKeyValue(com.foxit.sdk.pdf.signature.Signature.e_signatureKeyNameText, text)
            signature.signingTime = dateTime
            val flags = (com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagSigningTime or com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagFoxitFlag or
                    com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagLocation or com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagBitmap or
                    com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagReason or com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagSigner or
                    com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagText or com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagDN
                    or com.foxit.sdk.pdf.signature.Signature.e_signatureAPFlagLabel).toLong()

            val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.BLUE)
            signature.bitmap = bitmap

            signature.appearanceFlags = flags
            try {
                val progressive = signature.startSign(mCertPath, mCertPassword!!.toByteArray(), com.foxit.sdk.pdf.signature.Signature.e_digestSHA1, null, null, outputFilePath)
                var progress = CommonDefines.e_progressToBeContinued
                while (progress == CommonDefines.e_progressToBeContinued) {
                    progress = progressive.continueProgress()
                }
                progressive.release()
            } catch (e: PDFException) {
            }

            state = signature.state
            if (state != com.foxit.sdk.pdf.signature.Signature.e_signatureStateSigned.toLong() || !signature.isSigned) {
                Toast.makeText(mContext, String.format("This document sign failed !!!"), Toast.LENGTH_LONG).show()
                return
            }
            doc.closePage(pageIndex)
            val signedDoc = Common.loadPDFDoc(mContext!!, outputFilePath, null)
            val count = signedDoc!!.signatureCount
            if (count <= 0)
                return
            signature = signedDoc.getSignature(0)

            if (!signature.isSigned) {
                Toast.makeText(mContext, String.format("This document isn`t signed !!!"), Toast.LENGTH_LONG).show()
                return
            }

            try {
                val progressive = signature.startVerify(null, null)
                var progress = CommonDefines.e_progressToBeContinued
                while (progress == CommonDefines.e_progressToBeContinued) {
                    progress = progressive.continueProgress()
                }
                progressive.release()
            } catch (e: PDFException) {
            }

            state = signature.state
            if (state != com.foxit.sdk.pdf.signature.Signature.e_signatureStateVerifyValid.toLong()) {
                Toast.makeText(mContext, String.format("This document verify failed !!!"), Toast.LENGTH_LONG).show()
                return
            }
            Toast.makeText(mContext, Common.runSuccesssInfo + outputFilePath, Toast.LENGTH_LONG).show()
        } catch (e: PDFException) {
            Toast.makeText(mContext, String.format("Failed to sign the page No.%d! %s", pageIndex, e.message), Toast.LENGTH_LONG).show()
        } finally {
            Common.releaseDoc(mContext!!, doc)
        }
    }
}
