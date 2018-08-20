/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
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
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
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
            Toast.makeText(this@MainActivity, "Error: Directory of SD is not exist!", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_LONG).show()
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
                Toast.makeText(applicationContext, "The license is invalid!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Failed to initialize the library!", Toast.LENGTH_LONG).show()
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
        private val sn = "DW6QUNWzT4IF0JluGTcsD/OOs12XAfWdNkVPejrfj72Mi6P3xjaElw=="
        private val key = "ezJvj18mvB539PsXZqX8Iklssh9qvOZTXwsIO/MBC0bmgJ1qz4F0lwleIbZghw4SQD11gx1jqOJvZixkxBpuX+IYO08ZheJYkMQumnRHZ+eSysccHXuESTewCCK1K/MgHY/k54IqDvVbkdKIJu9QmzmxEJhP8zrulCE5v/XtHRKbNVNsvDpNop2HE1XwRtNon3s6YQ+j+c8BACgApAsRalO9SQP2GlkkBEAYqvY2JrrnRhfeZngd25kw9CZGd9p3QToervqkj64UV/3I4sqU/0arSotj32QPFCYAt9roaKCzAYoTeaE/l7zlnpd3dcZwf7NiYwSSrQ2LNpD+r2lHGi8WGr2hO7hwWtX9vdklMHOf/YFo+/05XxoVlnVAtYXRxx7S3MVeSEnhswujY+AswVbBgKGJRGRWzKnv7py3803X3DH5PGqRRayjTceiRq0ddSf7GiNtRQittqcRQSBYet43Rvyca+NyBxa5DddneG1VbBaVtM12C2Xv02/8HNLAf5AF3Vua/6O1gRi2ofIm0dqpk59lj19OiyqBIV6Ma/HZ93SwKLycOxDOHIcn2cVM1UtY+pF7ptUGz4Mq2V9YBaB2wxFB7I3mUEBjGOx1Y1ZAyvpWclWebMpIUct3ku4PmLsuSeX1uAd4iGOggcIFXPiUdYu7RytAbIlnWLtd5FZnwAfsyN/norSSmAtdZMHEv0xmh865YCFDkyo7Lw5ilhUICpZV2qJqqg6n757PdZcyO+M57r5bdcMto40q3M+lmiqOV8Wj/ui9v1h+UHOKQBvCti5TYvI5FWN/biCleETDEXUV1aMvVm/Zcyuu4njBWgL+0FMzCx72Lv0oIHsSl3THc2TS95YL9/3QpYQTAue6VpXdEAN1s3u4rzQVJCmT2QPK4FP/pznBYEP289VheUd1I521v93LZf9TWFDeIUIjE83bEGdtlJRdbqPR2fXccdtLWUeG+Ky97MqncQHy4REqjmBqNxjlo/gvEshBV7VOntNcUmpCLHKyZF+IupSlQ5zO0lJ9RaPShX+VkaI9rx17Oif8q0qvz29nA9s5XyBe87VjQm6BjA7b5hZnixsuZlv+R7ZhyWU5jaTh1BuLbz3zIDAO90rK9qnMP2hm5AFRmy962CqDi/vW0nyQISpgMlSJsGkPUxpg5TuhiGe13TEHMQyHVdBodOcMUBaO1sk4mdeYk7qUm78ek2VL4PhgZHZO3KE+B1ASiVG4iqGAbYiM"

        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}
