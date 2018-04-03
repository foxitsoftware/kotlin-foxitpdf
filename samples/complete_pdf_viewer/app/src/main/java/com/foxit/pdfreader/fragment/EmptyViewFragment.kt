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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.foxit.home.R

class EmptyViewFragment : BaseFragment() {

    private var myView: View? = null

    override var name: String
        get() = "Error page."
        set(value: String) {
            super.name = value
        }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        myView = inflater!!.inflate(R.layout.fragment_empty, container, false)
        return myView
    }

    companion object {

        private val TAG = EmptyViewFragment::class.java!!.getSimpleName()
    }
}