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
package com.foxit.pdfreader.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.foxit.uiextensions.UIExtensionsManager

open class BaseFragment : Fragment() {

    open var name: String = ""

    var path: String? = null

    var fId: Long = 0

    var isOpenSuccess = false
    var onFinishListener: UIExtensionsManager.OnFinishListener? = null

    var mUiExtensionsManager: UIExtensionsManager? = null
    var filter: String = ""

    override fun onStart() {
        super.onStart()
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager!!.onStart(activity)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager!!.onStop(activity)
        }
    }

    override fun onPause() {
        super.onPause()
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager!!.onPause(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager!!.onResume(activity)
        }
    }

    override fun onDetach() {
        super.onDetach()
        onFinishListener = null
        if (mUiExtensionsManager != null) {
            mUiExtensionsManager!!.onDestroy(activity)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    interface IFragmentEvent {
        fun onRemove()
    }
}