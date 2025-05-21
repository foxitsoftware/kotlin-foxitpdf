/**
 * Copyright (C) 2003-2025, Foxit Software Inc..
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
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.foxit.pdf.function.Common
import com.foxit.pdf.function.Common.checkSD
import com.foxit.pdf.function.Common.copyTestFiles
import com.foxit.pdf.function.DocInfo
import com.foxit.pdf.function.GraphicsObjects
import com.foxit.pdf.function.Image2Pdf
import com.foxit.pdf.function.Outline
import com.foxit.pdf.function.Pdf2text
import com.foxit.pdf.function.Render
import com.foxit.pdf.function.Search
import com.foxit.pdf.function.Signature
import com.foxit.pdf.function.Watermark
import com.foxit.pdf.function.annotation.Annotation
import com.foxit.pdf.function_demo.R
import com.foxit.pdf.main.FunctionAdapter.FunctionItemBean
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library

class MainActivity : FragmentActivity() {
    private var initErrCode = Constants.e_ErrSuccess
    private var isPermissionDenied = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!checkSD()) {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.fx_the_sdcard_not_exist),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        checkPermission()
        initLibrary()
        initView()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.setData(Uri.parse("package:" + applicationContext.packageName))
                startActivityForResult(intent, REQUEST_ALL_FILES_ACCESS_PERMISSION)
            } else {
                onPermissionGranted()
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )
            } else {
                onPermissionGranted()
            }
        } else {
            onPermissionGranted()
        }
    }

    private fun initLibrary() {
        initErrCode = Library.initialize(sn, key)
        showLibraryErrorInfo()
    }


    private fun onPermissionGranted() {
        isPermissionDenied = false
        copyTestFiles(applicationContext)
    }

    private fun showLibraryErrorInfo() {
        if (initErrCode != Constants.e_ErrSuccess) {
            if (initErrCode == Constants.e_ErrInvalidLicense) {
                Toast.makeText(
                    applicationContext,
                    getStr(R.string.fx_the_license_is_invalid),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    getStr(R.string.fx_failed_to_initalize),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun initView() {
        val recyclerView = findViewById<RecyclerView>(R.id.function_list)
        val adapter = FunctionAdapter(
            applicationContext,
            functionItems
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setOnItemClickListener(object : FunctionAdapter.OnItemClickListener {
            override fun onItemClick(positon: Int, itemBean: FunctionItemBean?) {
                if (initErrCode != Constants.e_ErrSuccess) {
                    showLibraryErrorInfo()
                    return
                }

                if (isPermissionDenied) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.fx_permission_denied),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val type = itemBean!!.type
                when (type) {
                    Common.ANNOTATION -> {
                        val annotation = Annotation(
                            applicationContext
                        )
                        annotation.addAnnotation()
                    }

                    Common.OUTLINE -> {
                        val outline = Outline(
                            applicationContext
                        )
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
                        val signature = Signature(
                            applicationContext
                        )
                        signature.addSignature(0)
                    }

                    Common.WATERMARK -> {
                        val watermark = Watermark(
                            applicationContext
                        )
                        watermark.addWatermark()
                    }

                    Common.SEARCH -> {
                        val search = Search(applicationContext)
                        search.startSearch()
                    }

                    Common.GRAPHICS_OBJECTS -> {
                        val graphicsObjects = GraphicsObjects(
                            applicationContext
                        )
                        graphicsObjects.addGraphicsObjects()
                    }

                    else -> {}
                }
            }
        })
    }

    private val functionItems: List<FunctionItemBean>
        get() {
            val functions: MutableList<FunctionItemBean> =
                ArrayList()
            //pdf2txt
            val pdf2txt = FunctionItemBean(
                Common.PDF_TO_TXT,
                getStr(R.string.pdf2text),
                getStr(R.string.pdf2textInfo)
            )
            functions.add(pdf2txt)
            //outline
            val outline = FunctionItemBean(
                Common.OUTLINE,
                getStr(R.string.outline),
                getStr(R.string.outlineInfo)
            )
            functions.add(outline)
            //annotation
            val annotation = FunctionItemBean(
                Common.ANNOTATION,
                getStr(R.string.addAnnotation),
                getStr(R.string.addAnnotationInfo)
            )
            functions.add(annotation)
            //docinfo
            val docInfo = FunctionItemBean(
                Common.DOCINFO,
                getStr(R.string.docInfo),
                getStr(R.string.docInfoDemoInfo)
            )
            functions.add(docInfo)
            //pdf2image
            val pdf2image = FunctionItemBean(
                Common.PDF_TO_IMAGE,
                getStr(R.string.render),
                getStr(R.string.renderInfo)
            )
            functions.add(pdf2image)
            //signature
            val signature = FunctionItemBean(
                Common.SIGNATURE,
                getStr(R.string.signature),
                getStr(R.string.signatureInfo)
            )
            functions.add(signature)
            //image2pdf
            val image2pdf = FunctionItemBean(
                Common.IMAGE_TO_PDF,
                getStr(R.string.image2pdf),
                getStr(R.string.image2pdfInfo)
            )
            functions.add(image2pdf)
            //watermark
            val watermark = FunctionItemBean(
                Common.WATERMARK,
                getStr(R.string.watermark),
                getStr(R.string.watermarkInfo)
            )
            functions.add(watermark)
            //search
            val search = FunctionItemBean(
                Common.SEARCH,
                getStr(R.string.search),
                getStr(R.string.searchInfo)
            )
            functions.add(search)
            //graphics
            val graphics = FunctionItemBean(
                Common.GRAPHICS_OBJECTS,
                getStr(R.string.graphics),
                getStr(R.string.graphicsInfo)
            )
            functions.add(graphics)
            return functions
        }

    private fun getStr(resId: Int): String {
        return applicationContext.getString(resId)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                isPermissionDenied = true
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ALL_FILES_ACCESS_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    onPermissionGranted()
                } else {
                    isPermissionDenied = true
                }
            }
        }
    }

    companion object {
        private val sn = "l5uLRkyIIDIyKJQZBChK2tXW/BikAnJozYEi1ApEyOR7i8W3U0ZlKQ=="
        private val key = "ezJvjl8mvB539NviuavWvpsxZwdMWZ2hvkmJNQZ8S/CwnxmS4c9F6U69I385uOe2wT4Fg2fJksQtXtnFsJ6lZR6RmsquC9T+GuC1YcZfAx/DRZivPTAkOaYoOwHQhGkkeTytiGg4KlolOVjyyRy5ZjzBBuwgODp1AcJAdTSvFlZnl+iCoYbPEKxUo/2+grZrhLICAXhrEioM4AwgIp1FxhQGlTLdv6OmuczqP0jt4IAEEJ1VhL5rh8X1fTGpx8fR8i0o0Ez/X307CCLaHBYLVXWMaZRn0XCsA1cOtcnD7XME1T4rHm4e4F+leLLPeylUoAMA/x1LwHj2yky9b2IclJxYcXRVdZOjCZsNPLpUDZS/UvAdTNrbkDl8fS/Vx75QOW+2z8//pjK4UR23WMi9yuvhXpfyi5Etv0aZDe969Pmc1vt2zK2Ddz2EAO5BslqcPDw2eBfCMBQL+iz3p9xg0XI9pAI6DnRDuqHkqHh6EVZ6zN20BupuDOdTg+PemU739fedBXY7TQz7ORE6BtzvPIlpyG1mNKC7A3bOIzyTDbVfSq3bPj5qoas7brtGTce1j0EHfzF3rzyFsKbvxcTcBRKzV+bAvtNofD4qPtqz7edHNbJKVcugoARzikVFW3dD7d14p7QUV+d6QkQf12KvzocGRfY1cHC/+Cey25k0+UtFQ/KdhaU/EVOfprWqeJLUyqX/GV9WX7I3A3OF8nTqeh7UpaOin8pA3T5k3tzAcnzFf9jFXjZeT1cRhClLSbWR4fGn+rxeLr2lwTOa9kBR1BY/iwItyY7uxCj1LcxtLKNC+BFRK4tXTsFlCjQPJOreF0oBxAhSp8dTmeXsdb/QVJMlR1iuJwqIWoxfg9+zHBNPUHpK33weRQ/j2gRPGBV2eW3+Wqcx+5VyB3PtCxaheJ3jMgXD2/1UBh24JVUVwgL0oQ3fi7EhleoALwQaulCWP5TTCOioPJFjVGBMo5BfH4o4rU1JNDse/QIauw1EkQQHlzfazCpU9gHnP4nBNKAgn+fNc+hwDBEP0dmlIEeHvy4kGEQQCwtMuV6Ezam1BAUwjKp8Lw5d2B/8d65mUCj1kZl2cXLEAnrwFCyZ8+RHe4XK+DZGCbwjzcyzJdQ+3qUrVgf9iseJm9XpOZp1azqo5nfOThl4lJAcEty7lsbXRpldNiFb8VE/hMkm/cFR9PNj40N4Zq+EvdiSO1ZwaEyM67OHwgo6i0QtGhp1SNA6Enq6OVNEy9J0QF5e2XT4UoZNN7roRKkP1ADvQA=="

        private const val REQUEST_EXTERNAL_STORAGE = 1000
        private const val REQUEST_ALL_FILES_ACCESS_PERMISSION = 2000
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}
