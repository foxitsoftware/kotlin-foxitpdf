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
package com.foxit.pdfreader.fragment

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.foxit.App
import com.foxit.home.R
import com.foxit.sdk.PDFViewCtrl
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.pdfreader.impl.PDFReader

import java.io.InputStream

class PDFReaderFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val stream = activity.applicationContext.resources.openRawResource(R.raw.uiextensions_config)
        val config = UIExtensionsManager.Config(stream)
        if (!config.isLoadDefaultReader) {
            val dialog = AlertDialog.Builder(activity).setMessage("Default reader could not be loaded.").setPositiveButton("Sure") { dialog, which -> activity.finish() }.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
            return null
        }
        val pdfViewerCtrl = PDFViewCtrl(activity.applicationContext)
        val uiExtensionsManager = UIExtensionsManager(activity.applicationContext, null, pdfViewerCtrl, config)

        pdfViewerCtrl.uiExtensionsManager = uiExtensionsManager
        uiExtensionsManager.attachedActivity = activity
        uiExtensionsManager.registerModule(App.instance().localModule) // use to refresh file list
        mPDFReader = uiExtensionsManager.pdfReader as PDFReader
        mPDFReader!!.onCreate(activity, pdfViewerCtrl, savedInstanceState)
        mPDFReader!!.openDocument(path, null)
        mPDFReader!!.setOnFinishListener(onFinishListener)
        name = mPDFReader!!.name
        return mPDFReader!!.contentView

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (mPDFReader != null) {
            mPDFReader!!.onConfigurationChanged(activity, newConfig)
        }
    }

    companion object {

        private val TAG = PDFReaderFragment::class.java!!.getSimpleName()
    }


}
