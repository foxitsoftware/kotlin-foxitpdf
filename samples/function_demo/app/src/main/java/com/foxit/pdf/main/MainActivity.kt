/**
 * Copyright (C) 2003-2020, Foxit Software Inc..
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
package com.foxit.pdf.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

import com.foxit.pdf.function.Annotation
import com.foxit.pdf.function.Common
import com.foxit.pdf.function.DocInfo
import com.foxit.pdf.function.Outline
import com.foxit.pdf.function.Pdf2text
import com.foxit.pdf.function.Render
import com.foxit.pdf.function.Signature
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library

class MainActivity : FragmentActivity() {

    private var pdf2textDemoBtn: Button? = null
    private var outlineDemoBtn: Button? = null
    private var addAnnotationDemoBtn: Button? = null
    private var docInfoDemoBtn: Button? = null
    private var renderDemoBtn: Button? = null
    private var signatureDemoBtn: Button? = null

    private var initErrCode = Constants.e_ErrSuccess
    private var isPermissionDenied = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Common.checkSD()) {
            Toast.makeText(this@MainActivity, getString(R.string.fx_the_sdcard_not_exist), Toast.LENGTH_LONG).show()
            return
        }

        initErrCode = Library.initialize(sn, key)
        showLibraryErrorInfo()

        if (Build.VERSION.SDK_INT >= 23) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            } else {
                isPermissionDenied = false
                Common.copyTestFiles(applicationContext)
            }
        } else {
            isPermissionDenied = false
            Common.copyTestFiles(applicationContext)
        }

        pdf2textDemoBtn = findViewById<View>(R.id.pdf2text) as Button
        outlineDemoBtn = findViewById<View>(R.id.outline) as Button
        addAnnotationDemoBtn = findViewById<View>(R.id.addAnnotation) as Button
        docInfoDemoBtn = findViewById<View>(R.id.docInfo) as Button
        renderDemoBtn = findViewById<View>(R.id.render) as Button
        signatureDemoBtn = findViewById<View>(R.id.signature) as Button

        pdf2textDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            if (isPermissionDenied) {
                Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder()!! + Common.testInputFile
            val pdf2text = Pdf2text(applicationContext, testFilePath)
            pdf2text.doPdfToText()
        })

        outlineDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            if (isPermissionDenied) {
                Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder()!! + Common.outlineInputFile
            val outline = Outline(applicationContext, testFilePath)
            outline.modifyOutline()
        })

        addAnnotationDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            if (isPermissionDenied) {
                Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder()!! + Common.anotationInputFile
            val annotation = Annotation(applicationContext, testFilePath)
            annotation.addAnnotation()
        })

        docInfoDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            if (isPermissionDenied) {
                Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder()!! + Common.testInputFile
            val info = DocInfo(applicationContext, testFilePath)
            info.outputDocInfo()
        })

        renderDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            if (isPermissionDenied) {
                Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder()!! + Common.testInputFile
            val render = Render(applicationContext, testFilePath)
            render.renderPage(0)
        })
        signatureDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != Constants.e_ErrSuccess) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            if (isPermissionDenied) {
                Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            val testFilePath = Common.getFixFolder()!! + Common.signatureInputFile
            val certPath = Common.getFixFolder()!! + Common.signatureCertification
            val certPassword = "123456"
            val signature = Signature(applicationContext, testFilePath, certPath, certPassword)
            signature.addSignature(0)
        })
    }

    private fun showLibraryErrorInfo() {
        if (initErrCode != Constants.e_ErrSuccess) {
            if (initErrCode == Constants.e_ErrInvalidLicense) {
                Toast.makeText(applicationContext, getString(R.string.fx_the_license_is_invalid), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, getString(R.string.fx_failed_to_initalize), Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isPermissionDenied = false
            Common.copyTestFiles(applicationContext)
        } else {
            isPermissionDenied = true
        }
    }

    companion object {
        private val sn = "XmE4UnLG/IaLL6QwTqHtLVMGcBiJTeRm4fPIWPmzcWt3+hhXfbW5vg=="
        private val key = "ezJvjl3GtGh397voL2Xkb3kutv9oSAOWRQ7cbJfN3y7VglVhz0T2xqzpSWcuWyR+pNaUYD3bs8Es82uVUUh2hXdnXA5MhQD0zlTI5AEgPxpHT7x+TEiQKeeTPfdOfea8Sw9sPPuasmPH8XtcKkdbgcgQQGgcz3CCZIAQdC9YKlNdKtIUTHp9VOeRKXFHWulsn3GyYQ+b2c8V1BjQJHFR+AXpQj1sxykcFQrbCrGGzZtupm2Sn9uxotXtJIpUOokx1y+XZeJ1ZeY72C/3/LlJvZ7EVtyx6agWuyjOqOLTah+Y0KC0quoBrJbTgvebddnygxHXwbBI8oyCNSaTzI4f41KxFnYA0YqfvLOAVOra434F4xQSXeGT2EPwRU279QBDFDxrDa8X0QD8HJ+wUN/+/+OMTZ7GXVeBZNFbUFTt4H5P7cTvdBxTxFCXJdaO54QvW5PLQ6tAUIh3ojOxalTNsBSM8KzTdHX774Q+uD6HWysE2rP0WW3qkjBDXRSniT2H5N32CcUXKa/M/avpHV6AXAOW7hi7fFQWRuV8Bz74wYU/oACWx1TteyVd0ukcd/LnTqoSPDILBftp35KdMYIJmqiCeFSqGdKfoQ6SRe1B9PA77ZFqzQWuzWVtrn9owEnb6Z8yEFHxmHucggf8veSsCWOY6JBvUpHhR/QabOI2GJ6xfb4NFojBdrSZwQrUVvwdb3ThYyx4+Kv2c0SpyQiv9dp1i+bqa//g5xX3p+wsyWKr3k9HXoGSicgDv9ShIgv8t6ekr264EeLvp0muL42ulEsRATz+8I2/rZcfCnLNv4lIgvgLTIQYalV3BIumgRvAoiXWCOBFpjUhpg8nL5IAZdjI5cWkn/x86+380oc8p86IKCCvIx7xBEesYxBg/7EO0123iDbDONxxI2MSM0AJ7Y/HgNiORyPNBWfcgV78pH8A6/dFwUq/Lz7igqpLHy2npaPCVg9+SSciQn7NtfULusJXRpJ9uqzHU2und1t3vZgaEm6o7yeaw/s5NwGWg8J92P0kOV/JyZHhsI5T69Ht0wyPjKLDDSAwOEfnKaHvSx7gx+X42uUGg8e5MLu6BWlCJk2qru5+V4pt7SvYLKP3JbG5XSY9PKjt8wilb6qFHCnRTaI4bZNT7uPI35/rVu0ts7AWlNCbA/3lVK+qNcF8LvUHQlP60QNOPVO6jzsR0g5bGdUTmec2pdW66G0CffmcvC57K9RldfHl1w4bo8sxNFhmi/SpnDDydG/XRzI6D/LE81pZ"

        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}
