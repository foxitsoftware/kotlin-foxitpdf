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
        private val sn = "qgVK5jS3KYEa5d/7a0aCJpZHuXc1ite9LBERoywRU5LQyMZNcXzL2A=="
        private val key = "ezJvj18mvB539PsXZqXcIklssh9qveZTXwsIO7MBC0bmgMUWD2d+2vk18zJ02pl+5NZ0I/5z+SGPshNpVNR/9bK/6rzAqEiIcptvrDVZFBf8BtPD3DiHacfAC+00W06fFZsc8/upASquVGTB2AE5+vQ7QLOyoYjnLOnKIXprWJ13ieiZTd+dgwEc7qx4AwPm3KohdnBIau1l7ezggBr4ToFyIph4b9kc//TTKBZVmO/naXjUkOXHux8hw1MxsiY3Y3ITf/U41yP50zPDqS71fiuhceUd0uRV1MJ8BE5Fa/DNw1V9EvOvy9nJsml14OAajxnzq7EVB3OkTaZv7hU392bHZJFQm7nrgOFlC0GL9Kpt6LvJJk0nOKe9KIW0OVZuPd5TfOoNTIiPV5RzohOwxBWs0MHhTox48O8rSnETDmyidPUpQpK04i7fMZxIn56MCD5dPkjtS+lwv1aYcVaZCB5eIpcDyvnIZZnvIrFu+DcYhSw6Tj2VfLTwwLlJ6/ImQZAHxVua/6O7hRi2ofImEVOkqhkkj1hOiyIBGW689ZGaKa2MLTbNVmj+EIkmxyXPrSfvkq6LMZRLqLasmFDc5OB+RCldbgzmIFifk5yxTC81HFNBHKYq1l2LcSUTDS5gZGBwv89QNVpjnYK1qzMKxdW2DJb8vsXE+7uXq4vpzfOofjtAp7V4QlLTXXLIrB2V5exBHWxAU9BdxSTviIsjthEICncV0mbrigYj7r7b7oIRlOYBJr1ZRMstI0or0Esl2jbC2da/gFsbiyWy5iNzcYwVevRCS4eL5DTGkSyaPs3sjOfGb4WBxZ03f7BL+S9eiK1Dv14ytEWa9Z/6EApXCsHgCJYzJW13EvkA9bziaden0OzIPBdfk7i9mdklH80BPcI0J5sC1lB2XCRGqmQr8QkCL5/JZY+xoGAh10GHafhXnhpSHZ9qaBSZzhTArIu8h6PEd2UhFSLYkfoXto8/N/6r8/YLBsOqVhMrq/5WoD6cJQQdGm1KLbqQvRnSui7VafTGZ6QbwkcVMzJV7w9hVR/q7hqRo2ZPi1olraM8kVnD+SSpeK6qkD2sgmoTfYzqAEz2AWq8XlNSEEoGTyJzgGzEPgfmpMvHmpkhtGONOKCSA/R36VuOR3GG9psBS4god2Q3gN4+v0guQV+BqFyu9EcsxnV7bEJ1Slr9o+TtUvs6PiP7Reg7rekObp485zQyuRmOZfIbycg7kBC575myg4h1"

        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}
