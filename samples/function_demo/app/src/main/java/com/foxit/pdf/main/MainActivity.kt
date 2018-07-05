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
package com.foxit.pdf.main

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.foxit.pdf.function.Annotation
import com.foxit.pdf.function.Common
import com.foxit.pdf.function.DocInfo
import com.foxit.pdf.function.Outline
import com.foxit.pdf.function.Pdf2text
import com.foxit.pdf.function.Render
import com.foxit.pdf.function.Signature
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library
import org.jetbrains.anko.find

class MainActivity : FragmentActivity() {

    private var pdf2textDemoBtn: Button? = null
    private var outlineDemoBtn: Button? = null
    private var addAnnotationDemoBtn: Button? = null
    private var docInfoDemoBtn: Button? = null
    private var renderDemoBtn: Button? = null
    private var signatureDemoBtn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Common.checkSD()) {
            Toast.makeText(this@MainActivity, "Error: Directory of SD is not exist!", Toast.LENGTH_LONG).show()
            return
        }

        initErrCode = Library.initialize(sn, key)

        showLibraryErrorInfo()
        Common.copyTestFiles(applicationContext)

        pdf2textDemoBtn = find(R.id.pdf2text) as Button
        outlineDemoBtn = find(R.id.outline) as Button
        addAnnotationDemoBtn = find(R.id.addAnnotation) as Button
        docInfoDemoBtn = find(R.id.docInfo) as Button
        renderDemoBtn = find(R.id.render) as Button
        signatureDemoBtn = find(R.id.signature) as Button

        pdf2textDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.getFixFolder() + Common.testInputFile
            val pdf2text = Pdf2text(applicationContext, testFilePath)
            pdf2text.doPdfToText()
        })

        outlineDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.getFixFolder() + Common.outlineInputFile
            val outline = Outline(applicationContext, testFilePath)
            outline.modifyOutline()
        })

        addAnnotationDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.getFixFolder() + Common.anotationInputFile
            val annotation = Annotation(applicationContext, testFilePath)
            annotation.addAnnotation()
        })

        docInfoDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder() + Common.testInputFile
            val info = DocInfo(applicationContext, testFilePath)
            info.outputDocInfo()
        })

        renderDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.getFixFolder() + Common.testInputFile
            val render = Render(applicationContext, testFilePath)
            render.renderPage(0)
        })
        signatureDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.getFixFolder() + Common.signatureInputFile
            val certPath = Common.getFixFolder() + Common.signatureCertification
            val certPassword = "123456"
            val signature = Signature(applicationContext, testFilePath, certPath, certPassword)
            signature.addSignature(0)
        })
    }

    private fun showLibraryErrorInfo() {
        if (initErrCode != Constants.e_ErrSuccess) {
            if (initErrCode == Constants.e_ErrInvalidLicense) {
                Toast.makeText(applicationContext, "The license is invalid!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Failed to initialize the library!", Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    companion object {
        private val sn = "sS1No48GllWOhaww26EpDX+mGXcYdi5zUHFRsdMSGxodGTyLDgaYWA=="
        private val key = "ezKfjl3GtGh397voL2Xsb3l6739eBbVCXwu5VfNUsnrnlvx3zI41B75STKd59TXVpkxEbp+B3UEqUNj1KM66ujQN8Mgkr/mKJOJaqOuqngyfs4ccHXmAWTe4ajKpqKI0Y5clxoTqL8tfYrOQZN7SeznxuJdOMwrg2jDyDQc5ffNZSt8Z6nAjHlI4vjZHNrWeW9M+jFgIcaBMRE/hwgZwwQpr/74cdH/VV289PBrvsLtf+hIagpdc0l3tJJzQf00Q/0/PSPp35eeU+YrKuiXiBIm0sLahXrXBU6kdYOoZgteB9dMaH0v2Ev2EF4hzwtcwExvOI8UxUsC71UTl/KJhIiKs9PdM2fZ4AaseldOQvaHs9dGVwsI2LajSXI21IKT3vwOnMHT10V95hnStG/maORwMHDfLjlAyJepfMlP2aU5x7hTFwRKF9bJRgelGeTzn0c3zJM/GhG5YccdzRPtJZvre4RD9oOYw+vrR6/TKoZtX6Nlu5y/FPg2xlA73kLdaaEqulHtDdec25ki/h9ahvyUP30bIMJKaG5F+SPTCemor1Oy4mtaWNhjPY0cVu807luylcfAd70yu/3neiDUc1JlI424i/OLxRkBGJInLdBMgEeU6gY34Rh5QBfWdKq3lHzKsZnHqL7+MDPu16Os3JX+G4rBWVpRMOKxgGTfnp2bkChAUlzL0tX+/iLjWPyADJwpo3AtVyCckdyyQLgvWr93+6nN34YurHHKqYUTQ0oBeRb0a2DYu3fNyAzDgPZ4lXbkbwtMtS4299A4lUnVJcA21ZBEqC0/mcu/eHHd1UdBBouaD6rkXQ53OzznjMCjibCYbNurh4X0toPxSrqbRU7/LBkzNIbUD+YH1AFAG6Uxi/arFjXBV0Wg0JKCZy1WBVeIfpTW/vtOxAaSsL4FX2930kqZhbIrbTBgOwlsDJO4d5LWFZNuCqjvI8U00ilJExKXAz0w5UTUGfLZraS85ur/zHRs6d8V+psFURmcaCpkLHOE8LrSfT+kat8N6GREjuZItoGs0NOkKYvj/lL963WcRWikieGBNP9Pl/hgpdIXew7nue6U9XGoTgdz2lLR6QtC4EFuVHheMP455C7pRlKJ+7gN9L9+LdoZ1c7LgthMGNg76WWkO129/xwSSDyE7l9z/HbWiAyAtYYYJe02Zl1sInDc30jFrpkXpOocoIa9qnh8EZN859NYJqkQiqJE9CIJ66DA0DNk8eNnaJaBNAzAv2eH+lwEXckM5Re5xjo+69QB0T2Fpx7nFR/cnSw=="

        private var initErrCode = Constants.e_ErrSuccess

        init {
            System.loadLibrary("rdk")
        }
    }
}
