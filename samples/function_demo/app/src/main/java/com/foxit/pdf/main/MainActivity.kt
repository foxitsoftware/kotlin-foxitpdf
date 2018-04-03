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

import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

import com.foxit.pdf.function.Annotation
import com.foxit.pdf.function.Common
import com.foxit.pdf.function.DocInfo
import com.foxit.pdf.function.Render
import com.foxit.pdf.function.Outline
import com.foxit.pdf.function.Pdf2text
import com.foxit.pdf.function.Signature
import com.foxit.sdk.common.Library
import com.foxit.sdk.common.PDFError
import com.foxit.sdk.common.PDFException

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

        if (false == Common.CheckSD()) {
            Toast.makeText(this@MainActivity, "Error: Directory of SD is not exist!", Toast.LENGTH_LONG).show()
            return
        }

        try {
            Library.init(sn, key)
        } catch (e: PDFException) {
            initErrCode = e.lastError
        }

        showLibraryErrorInfo()
        Common.copyTestFiles(applicationContext)

        pdf2textDemoBtn = findViewById(R.id.pdf2text) as Button
        outlineDemoBtn = findViewById(R.id.outline) as Button
        addAnnotationDemoBtn = findViewById(R.id.addAnnotation) as Button
        docInfoDemoBtn = findViewById(R.id.docInfo) as Button
        renderDemoBtn = findViewById(R.id.render) as Button
        signatureDemoBtn = findViewById(R.id.signature) as Button

        pdf2textDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != PDFError.NO_ERROR.code) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.GetFixFolder() + Common.testInputFile
            val pdf2text = Pdf2text(this@MainActivity, testFilePath)
            pdf2text.doPdfToText()
        })

        outlineDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != PDFError.NO_ERROR.code) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.GetFixFolder() + Common.outlineInputFile
            val outline = Outline(this@MainActivity, testFilePath)
            outline.modifyOutline()
        })

        addAnnotationDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != PDFError.NO_ERROR.code) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.GetFixFolder() + Common.anotationInputFile
            val annotation = Annotation(this@MainActivity, testFilePath)
            annotation.addAnnotation()
        })

        docInfoDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != PDFError.NO_ERROR.code) {
                showLibraryErrorInfo()
                return@OnClickListener
            }

            val testFilePath = Common.GetFixFolder() + Common.testInputFile
            val info = DocInfo(this@MainActivity, testFilePath)
            info.outputDocInfo()
        })

        renderDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != PDFError.NO_ERROR.code) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.GetFixFolder() + Common.testInputFile
            val render = Render(this@MainActivity, testFilePath)
            render.renderPage(0)
        })
        signatureDemoBtn!!.setOnClickListener(View.OnClickListener {
            if (initErrCode != PDFError.NO_ERROR.code) {
                showLibraryErrorInfo()
                return@OnClickListener
            }
            val testFilePath = Common.GetFixFolder() + Common.signatureInputFile
            val certPath = Common.GetFixFolder() + Common.signatureCertification
            val certPassword = "123456"
            val signature = Signature(this@MainActivity, testFilePath, certPath, certPassword)
            signature.addSignature(0)
        })
    }

    private fun showLibraryErrorInfo() {
        if (initErrCode != PDFError.NO_ERROR.code) {
            if (initErrCode == PDFError.LICENSE_INVALID.code) {
                Toast.makeText(this@MainActivity, "The license is invalid!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Failed to initialize the library!", Toast.LENGTH_LONG).show()
            }
            return
        }
    }

    companion object {
        private val sn = "cIwVF7AUSAakiEAihzb85vrmwVdOhUoXKg6IwosV7MwAw0FKEQyPAQ=="
        private val key = "ezKXjt8ntBh39DvoP0WQjY5U/oy/u0HLS16ctI9QPpxzh8j1xFBGxKzpATgyxl/xG5GuEi73n6bGooc+9epFT3VUozHpJ2k/5BYfyZ9qbUDfpKrcWFOUIWeQoraXjc6hyJSmte3+YYcqS8dP6frqA4bTWWpHAYBgmG1kY5d/7if3S5ahlGj7fflXmO7a+5SvLP15L6KuMY22qzWkhRhNbtmC1sAMpIL65j4yzEv+64rxyokDDDJeP1A6JTBC5pSPJs2gFGOlhXzRe4f4J3sSe3jrrPRhsvZ6RrmVcSHyvJUq55A08pRmORZFvLvIXpxS9UZO7sg/qYn7mM2KztGQXntfM3DtKrPTXILilO/GP6PGcYI1VyZR6BzuJQ8y1RK2yuHHynjGtOrw3snn0WLpcv0XnaB86ZtuB357gEXxwUfuzYf+gxFDyVAQI8km6YPjgysbtbcWw6wTSoqBwK5c1sF8qOlj9dnSCeJzMA9ZRZNTq9kN1+Xeasgn8Rsr8Yf+vHc4QQPIF96y8g7NxAqCgo6uGCxa+b3sHtwe8McQv7muqaqvtL3+Off1o+trQIssmocVwC640o8l13+XEjydO4s8TQtv+eAsZg1uMiiLDA1M9b7PvLos19hj7xq6XEU5/gX5R4kVQ/dIPK3wQW12POaQPBiIdSz5sNKFC7wjodCMnfm7/GgC5sb2P5Y6cgAxj0Ju5vaElcq5HJywxjelU0IembiGQlq6kOBEMxakxH7gaCPmqyS3GO+kaCNbh4KkxC1hMGL2quSdMhMBhGpb/MP9zajM0ZHocb0vrKn8vrV6vn/htsuR5T+lwYGTKKo4c0/PPqPPQ+x30UkJ3wQBgAW4fCbVexhUAW6ggOvZbYoPF/HjskRxoWFG768PphebdvS/QI0QOH5E840iuyoEMFEaL0PQ534RE1NvTw0yYWPC6QgtKhFvEOT1+2JlO/7ZRqg4lBivz7UAwOlMfrcp+D2vvnjW+FsFc9mGIB+uFMsD2WZRRqRgYi/X39kF9Dh7BpAs2STmHinEKtMeRe4Bcqv7UupEfv51r6CRKiLfluFPSdyZS8ppOoz5l1XGeHQA/OSCe+vHuQhlpUG9gst5OFqj1Dy1HzsyJ/ZFgbJ0vwgrzyVMVtFZsAbhxorUQBTqaaUmRQqbJ+SGcIbV1xbQ74fPkIDsZBxroxgDZKt82fEjlYPlvlBg9g3Id3jN5y6S5Ydr/N8C"

        private var initErrCode = PDFError.NO_ERROR.code

        init {
            System.loadLibrary("rdk")
        }
    }
}
