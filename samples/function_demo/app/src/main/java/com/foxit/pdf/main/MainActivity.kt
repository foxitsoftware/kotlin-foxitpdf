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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foxit.pdf.function.*
import com.foxit.pdf.function.Common.checkSD
import com.foxit.pdf.function.Common.copyTestFiles
import com.foxit.pdf.function.annotation.Annotation
import com.foxit.pdf.main.FunctionAdapter.FunctionItemBean
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library
import java.util.*

class MainActivity : FragmentActivity() {
    private var initErrCode = Constants.e_ErrSuccess
    private var isPermissionDenied = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!checkSD()) {
            Toast.makeText(this@MainActivity, getString(R.string.fx_the_sdcard_not_exist), Toast.LENGTH_LONG).show()
            return
        }
        checkPermission()
        initLibrary()
        initView()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            } else {
                isPermissionDenied = false
                copyTestFiles(applicationContext)
            }
        } else {
            isPermissionDenied = false
            copyTestFiles(applicationContext)
        }
    }

    private fun initLibrary() {
        initErrCode = Library.initialize(sn, key)
        showLibraryErrorInfo()
    }

    private fun showLibraryErrorInfo() {
        if (initErrCode != Constants.e_ErrSuccess) {
            if (initErrCode == Constants.e_ErrInvalidLicense) {
                Toast.makeText(applicationContext, getStr(R.string.fx_the_license_is_invalid), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, getStr(R.string.fx_failed_to_initalize), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initView() {
        val recyclerView: RecyclerView = findViewById(R.id.function_list)
        val adapter = FunctionAdapter(applicationContext, functionItems)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        adapter.setOnItemClickListener(object : FunctionAdapter.OnItemClickListener {
            override fun onItemClick(positon: Int, itemBean: FunctionItemBean?) {
                if (initErrCode != Constants.e_ErrSuccess) {
                    showLibraryErrorInfo()
                    return
                }
                if (isPermissionDenied) {
                    Toast.makeText(applicationContext, getString(R.string.fx_permission_denied), Toast.LENGTH_LONG).show()
                    return
                }
                val type = itemBean!!.type
                when (type) {
                    Common.ANNOTATION -> {
                        val annotation = Annotation(applicationContext)
                        annotation.addAnnotation()
                    }
                    Common.OUTLINE -> {
                        val outline = Outline(applicationContext)
                        outline.modifyOutline()
                    }
                    Common.DOCINFO -> {
                        val info = DocInfo(applicationContext)
                        info.outputDocInfo()
                    }
                    Common.PDF_TO_TXT -> {
                        val pdf2text = Pdf2text(applicationContext)
                        pdf2text.doPdfToText()
                    }
                    Common.PDF_TO_IMAGE -> {
                        val render = Render(applicationContext)
                        render.renderPage(0)
                    }
                    Common.IMAGE_TO_PDF -> {
                        val image2Pdf = Image2Pdf(applicationContext)
                        image2Pdf.doImage2Pdf()
                    }
                    Common.SIGNATURE -> {
                        val signature = Signature(applicationContext)
                        signature.addSignature(0)
                    }
                    Common.WATERMARK -> {
                        val watermark = Watermark(applicationContext)
                        watermark.addWatermark()
                    }
                    Common.SEARCH -> {
                        val search = Search(applicationContext)
                        search.startSearch()
                    }
                    Common.GRAPHICS_OBJECTS -> {
                        val graphicsObjects = GraphicsObjects(applicationContext)
                        graphicsObjects.addGraphicsObjects()
                    }
                    else -> {
                    }
                }
            }
        })
    }

    //annotation
    private val functionItems: List<FunctionItemBean>
        get() {
            val functions: MutableList<FunctionItemBean> = ArrayList()
            //annotation
            val annotation = FunctionItemBean(Common.ANNOTATION, getStr(R.string.addAnnotation), getStr(R.string.addAnnotationInfo))
            functions.add(annotation)
            //outline
            val outline = FunctionItemBean(Common.OUTLINE, getStr(R.string.outline), getStr(R.string.outlineInfo))
            functions.add(outline)
            //pdf2txt
            val pdf2txt = FunctionItemBean(Common.PDF_TO_TXT, getStr(R.string.pdf2text), getStr(R.string.pdf2textInfo))
            functions.add(pdf2txt)
            //docinfo
            val docInfo = FunctionItemBean(Common.DOCINFO, getStr(R.string.docInfo), getStr(R.string.docInfoDemoInfo))
            functions.add(docInfo)
            //pdf2image
            val pdf2image = FunctionItemBean(Common.PDF_TO_IMAGE, getStr(R.string.render), getStr(R.string.renderInfo))
            functions.add(pdf2image)
            //signature
            val signature = FunctionItemBean(Common.SIGNATURE, getStr(R.string.signature), getStr(R.string.signatureInfo))
            functions.add(signature)
            //image2pdf
            val image2pdf = FunctionItemBean(Common.IMAGE_TO_PDF, getStr(R.string.image2pdf), getStr(R.string.image2pdfInfo))
            functions.add(image2pdf)
            //watermark
            val watermark = FunctionItemBean(Common.WATERMARK, getStr(R.string.watermark), getStr(R.string.watermarkInfo))
            functions.add(watermark)
            //search
            val search = FunctionItemBean(Common.SEARCH, getStr(R.string.search), getStr(R.string.searchInfo))
            functions.add(search)
            //graphics
            val graphics = FunctionItemBean(Common.GRAPHICS_OBJECTS, getStr(R.string.graphics), getStr(R.string.graphicsInfo))
            functions.add(graphics)
            return functions
        }

    private fun getStr(resId: Int): String {
        return applicationContext.getString(resId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isPermissionDenied = false
            copyTestFiles(applicationContext)
        } else {
            isPermissionDenied = true
        }
    }

    companion object {
        private const val sn = "clgU6VDvO5QSp9JjIjVgnD9gk50ei1Kd+ClH4BtQhuP4jWF557RqDA=="
        private const val key = "ezJvjlnatG53NzsCGirXpdq5jdadAYRnZ2UKOn1rmFCXE4tfh4A1R/2BvaDJ6PAAUFgncfPh3UkzYrnWq9BeTW6nSPLqTUteJgWVYYdvhzLebfWUVXS00fg8N6QMFkUNtTtsvUgpVo0Kj66RDOU1ZgIDcU0FMawop0xytctF9CNYLcePvUTHd48W8UbAsKvzDEkD4b7C6zqBdAqcVUvBjEyIhVTq1SxiQljkCuZb7fiq7HQjoYuZbsiVOxPQpprmqGW4sDyY/a6xaYNEgjCudhZ6b23qoQQkxzaPrSb18vxEhq+hrhH9HDLm9LOWuqUZHAGr7kEXpzqitCqOBcCoxyVBw3sw4XpRqdFQ7lDdKuLQRX46fcCMA6W4dOnwOC6qfSobaf3V/KmQ36xj+hv6G9Fa4ziEf5SutEmz7BxreRkgMg+ExeTQJlLEHNmgdricp3eOh7wWYEUJTMuN/j0iCjrzPJfvse2icduMrQLOLa0eSVrNL2zGYBIRNMvk+ZACO5QjgQOi0c9PRAkqVb2huMbAnPNZPKm4Z9F6cFvaGiiRqzC6yMivVcu/j6akCjax/eWKTc9Bhp8sh2f5jJQRdjNKT0gLP3HeB5QuturafcjMDI0biN/yaN+gRb+sXGETysID7RcxVSUTKjL3xL9U/Rj25qlgcUI3VUWFQNMHCnOFuzGB98qUyTVm5mHjURK/cR4l0vuiMqrh1uXg5neckcpsiIqieuGZXhH2TN/CF6Doy/B/U36SoBPLX9//XigQtoP1RZqTIYNilOHK4KjpdxVMuqR2kxSjV7+/yAtoyMbGxmXyFcuG3RYTXFZF2kq5VxNuozl4x1f0Ki+R6ILhoaRZjZzmnLFbvjCc+wypOQGWBQA0pwU2NFqyXWmbrpopN1NAVfBRtn18xK24ia5+p7fPpFkIEmMO45a1LvJ6FxzJEkBP9hidOf+tR5YbQEAa1fHqdS3iQj3sOAhbqgqppegi7oAaZypYesd1yEKAjxxSPltK3tzwF8Q5JXygBRgqNQ7hyiQJH8Dviwm4cr2CTuU1Ok0eACfQhLjpgmFPxlSGyGX3KOYWNdh1Q1v7lbDa4gkDyjZeUoy86BR2OI4AO+eiz0SGiLmf4a2Kw0sBuNYIXd8rQ9H4jO0u3nLkLvBwMZ3szlfKMiGeBpZDIGGuqvkebshKAoy+iDixtHJSqb6tYOiVlE6uRB3ufciz+XXOtoKYs9kn/9glcPVdKiv3ysqU2iQ3zZNnhhFIIu3HXgRF/LjlMeHZ2coz46IsRqVz"
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}