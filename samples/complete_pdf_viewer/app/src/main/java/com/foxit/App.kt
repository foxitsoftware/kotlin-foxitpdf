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
package com.foxit

import android.content.Context
import android.os.Environment

import com.foxit.sdk.common.Library
import com.foxit.sdk.common.PDFError
import com.foxit.sdk.common.PDFException
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.utils.UIToast

import java.io.File

class App private constructor() {

    var applicationContext: Context? = null
    private var errCode = PDFError.NO_ERROR.code

    internal var mLocalModule: LocalModule? = null
    val localModule: LocalModule?
        get() {
            if (mLocalModule == null) {
                mLocalModule = LocalModule(applicationContext)
                mLocalModule!!.loadModule()

                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val curPath = Environment.getExternalStorageDirectory().path + File.separator + "FoxitSDK"
                    val file = File(curPath)
                    if (!file.exists())
                        file.mkdirs()
                    val sampleFile = File(curPath + File.separator + "Sample.pdf")
                    if (!sampleFile.exists()) {
                        mLocalModule!!.copyFileFromAssertsToTargetFile(sampleFile)
                    }

                    val guideFile = File(curPath + File.separator + "complete_pdf_viewer_guide_android.pdf")
                    if (!guideFile.exists()) {
                        mLocalModule!!.copyFileFromAssertsToTargetFile(guideFile)
                    }
                }

            }
            return mLocalModule
        }

    init {
        try {
            Library.init(sn, key)
        } catch (e: PDFException) {
            errCode = e.lastError
        }

    }

    fun checkLicense(): Boolean {
        when (PDFError.valueOf(errCode)) {
            PDFError.NO_ERROR -> {
            }
            PDFError.LICENSE_INVALID -> {
                UIToast.getInstance(applicationContext).show("The License is invalid!")
                return false
            }
            else -> {
                UIToast.getInstance(applicationContext).show("Failed to initialize the library!")
                return false
            }
        }
        return true
    }

    fun onDestroy() {
        if (mLocalModule != null) {
            mLocalModule!!.unloadModule()
            mLocalModule = null
        }
    }

    companion object {
        init {
            System.loadLibrary("rdk")
        }

        private val sn = "cIwVF7AUSAakiEAihzb85vrmwVdOhUoXKg6IwosV7MwAw0FKEQyPAQ=="
        private val key = "ezKXjt8ntBh39DvoP0WQjY5U/oy/u0HLS16ctI9QPpxzh8j1xFBGxKzpATgyxl/xG5GuEi73n6bGooc+9epFT3VUozHpJ2k/5BYfyZ9qbUDfpKrcWFOUIWeQoraXjc6hyJSmte3+YYcqS8dP6frqA4bTWWpHAYBgmG1kY5d/7if3S5ahlGj7fflXmO7a+5SvLP15L6KuMY22qzWkhRhNbtmC1sAMpIL65j4yzEv+64rxyokDDDJeP1A6JTBC5pSPJs2gFGOlhXzRe4f4J3sSe3jrrPRhsvZ6RrmVcSHyvJUq55A08pRmORZFvLvIXpxS9UZO7sg/qYn7mM2KztGQXntfM3DtKrPTXILilO/GP6PGcYI1VyZR6BzuJQ8y1RK2yuHHynjGtOrw3snn0WLpcv0XnaB86ZtuB357gEXxwUfuzYf+gxFDyVAQI8km6YPjgysbtbcWw6wTSoqBwK5c1sF8qOlj9dnSCeJzMA9ZRZNTq9kN1+Xeasgn8Rsr8Yf+vHc4QQPIF96y8g7NxAqCgo6uGCxa+b3sHtwe8McQv7muqaqvtL3+Off1o+trQIssmocVwC640o8l13+XEjydO4s8TQtv+eAsZg1uMiiLDA1M9b7PvLos19hj7xq6XEU5/gX5R4kVQ/dIPK3wQW12POaQPBiIdSz5sNKFC7wjodCMnfm7/GgC5sb2P5Y6cgAxj0Ju5vaElcq5HJywxjelU0IembiGQlq6kOBEMxakxH7gaCPmqyS3GO+kaCNbh4KkxC1hMGL2quSdMhMBhGpb/MP9zajM0ZHocb0vrKn8vrV6vn/htsuR5T+lwYGTKKo4c0/PPqPPQ+x30UkJ3wQBgAW4fCbVexhUAW6ggOvZbYoPF/HjskRxoWFG768PphebdvS/QI0QOH5E840iuyoEMFEaL0PQ534RE1NvTw0yYWPC6QgtKhFvEOT1+2JlO/7ZRqg4lBivz7UAwOlMfrcp+D2vvnjW+FsFc9mGIB+uFMsD2WZRRqRgYi/X39kF9Dh7BpAs2STmHinEKtMeRe4Bcqv7UupEfv51r6CRKiLfluFPSdyZS8ppOoz5l1XGeHQA/OSCe+vHuQhlpUG9gst5OFqj1Dy1HzsyJ/ZFgbJ0vwgrzyVMVtFZsAbhxorUQBTqaaUmRQqbJ+SGcIbV1xbQ74fPkIDsZBxroxgDZKt82fEjlYPlvlBg9g3Id3jN5y6S5Ydr/N8C";
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }
    }
}
