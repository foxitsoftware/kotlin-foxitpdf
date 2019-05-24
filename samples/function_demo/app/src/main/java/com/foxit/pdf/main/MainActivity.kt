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
        private val sn = "BW3CdOL9RzR/4rAyb2+Ze4s5sP8katpRwE+DOEB3k2rQZtFD5HJG5Q=="
        private val key = "ezJvj93HvBh39LusL0W0ja4g+PQUKROlWYrFaqfVeOP9qnxzhL01J+KHAmohNqVQ+DF+mvMHx0afiHvfYfpythLfEBEraoB6ODghaWXcBpU1+RmsaTiEau5SSVXbEg1tGbao3l6g7DLOSO3p4qazFrs/TvSy39FzXlmnGAGYf5vY3S4eKTqaRBYgzlJ526WedxUbq1BYK8+QZYjA6GPncrqOGH5OAW6Pz0NpDLpoJG+ZHeDTdOR9QP05XiVwBWW6ol+/hO7lHNGNu20rXf1GpMxkDLscPoFG4+N9kLtThf2Z4KCEtOphI7v4Zb92eSOdT0LEoCv/NgCvbXzwHzbSvE6MqM+s6IWYN/KBVXqZQIcfQatk3+KT2EP2RUXb9QBDBD1rDa8b0YD8HJ9QF4Ip/oN7aiu9kaD7Ih9+oVv40WbIllNZVbtreEpw0fBGb9OsS1RFrGl33JbgO6MgPQSTziyTE6VaFvjFjVgsuQUeaRfAF+x51hTKqEpQCxR9RQWF/SL9DcWhpOc5gO7JfWv8ZXYh41TthV8TBmg+2MTtUHY7Jbug+lAsMZd8qTpcviwYGzbAroqbinxaxOprGK7sGnRKHw79JYhKKjpvli6xpaaXw6aggjEaC/DQ633pmWrETK2aWXBRBrfnjHRky7urcjkuzg6TIDjxQ9Gl4mV0Ue8V/I4FXmqSBd3km6p9yn7oMgrcxXtbznW547+uyE11h12exYlwlkGCFf/tHtH39l7LJ6wIJDY67arTMmafyIAEYDKABvje7oYkUZzFUM4Uei7My/Nx4aAjTNFWuIZrJUAfVa0EbSeOjhJfxi8tIDVmF0lDQfj9AKKu686SzzosAw97LO62iA+00fysyxby10xLTITnGb/wuwIElKXo3e+6rN3q+7hfj85iM7csgvD9im2JHTIZj6TV9xN0MsevzHzqEO2VMhdukPkdDp5EiGhDsPn4KS4oLekRL148IhN21oEA03tb/WVvWmkFzMIxsspvpg+HcBiwPYm6ahvcSf83fJjmRb+Gk0LOTMYLCXqRiGKQSOVD12X9Uu2VueKnIKoPRo46it0EJAR/YKlWrZY0DlICsLRsYXDg/lmOS2BFu+nyPRi9V9ND0WxjqbNWieEJmf4wz1TR3VTL1BSBcqBq+SXVXLnuDjbuzulrjtIXS3UG5VPpwAX+/wVN3F5gUklRr5DJ7KiG7MM997QsTQpMwPe++zlVDmucBJ24HzuIhVw="

        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}
