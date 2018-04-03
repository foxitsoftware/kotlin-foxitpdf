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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.foxit.uiextensions.pdfreader.impl.PDFReader

open class BaseFragment : Fragment() {

    var mPDFReader: PDFReader? = null

    open var name: String = ""

    var path: String? = null

    var fId: Long = 0

    var onFinishListener: PDFReader.OnFinishListener? = null

    override fun onStart() {
        super.onStart()
        if (mPDFReader != null)
            mPDFReader!!.onStart(activity)
    }

    override fun onStop() {
        super.onStop()
        if (mPDFReader != null) {
            mPDFReader!!.onStop(activity)
        }
    }

    override fun onPause() {
        super.onPause()
        if (mPDFReader != null)
            mPDFReader!!.onPause(activity)
    }

    override fun onResume() {
        super.onResume()
        if (mPDFReader != null)
            mPDFReader!!.onResume(activity)
    }

    override fun onDetach() {
        super.onDetach()
        if (mPDFReader != null)
            mPDFReader!!.onDestroy(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
