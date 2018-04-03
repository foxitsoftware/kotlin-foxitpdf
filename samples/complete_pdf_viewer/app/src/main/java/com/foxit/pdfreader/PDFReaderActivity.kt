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
package com.foxit.pdfreader

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.KeyEvent
import android.view.WindowManager

import com.foxit.App
import com.foxit.home.R
import com.foxit.pdfreader.fragment.BaseFragment
import com.foxit.pdfreader.fragment.EmptyViewFragment
import com.foxit.pdfreader.fragment.PDFReaderFragment
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.pdfreader.impl.PDFReader
import com.foxit.uiextensions.utils.AppFileUtil
import com.foxit.uiextensions.utils.AppTheme

class PDFReaderActivity : FragmentActivity(), PDFReader.OnFinishListener {
    private var mFragmentManager: FragmentManager? = null

    private var currentFragment: BaseFragment? = null

    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppTheme.setThemeFullScreen(this)
        AppTheme.setThemeNeedMenuKey(this)
        setContentView(R.layout.activity_reader)
        filePath = AppFileUtil.getFilePath(this, intent, IHomeModule.FILE_EXTRA)
        mFragmentManager = supportFragmentManager
        if (!App.instance().checkLicense())
            openEmptyView()
        else {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

            openDocView()
        }
    }

    /**
     * open a Doc View use Fragment
     */
    private fun openDocView() {
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        val fragment = PDFReaderFragment()
        fragment.path = filePath
        fragment.onFinishListener = this
        fragmentTransaction.replace(R.id.reader_container, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    /**
     * when App license is valid, it should open a empty view use Fragment also.
     */
    private fun openEmptyView() {
        val fragmentTransaction = mFragmentManager!!.beginTransaction()
        fragmentTransaction.replace(R.id.reader_container, EmptyViewFragment())
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val currentFrag = supportFragmentManager.findFragmentById(R.id.reader_container) as BaseFragment
        setSelectedFragment(currentFrag)
        return if (this.currentFragment!!.mPDFReader != null && this.currentFragment!!.mPDFReader!!.onKeyDown(this, keyCode, event)) true else super.onKeyDown(keyCode, event)
    }

    private fun setSelectedFragment(fragment: BaseFragment) {
        this.currentFragment = fragment
    }

    private fun finishActivity() {
        this.finish()
    }

    override fun onFinish() {
        finish()
    }
}
