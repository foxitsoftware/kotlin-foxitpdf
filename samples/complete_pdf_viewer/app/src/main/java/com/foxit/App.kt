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
package com.foxit

import android.content.Context
import android.os.Environment
import com.foxit.home.R

import com.foxit.pdfreader.MultiTabView
import com.foxit.pdfreader.fragment.AppTabsManager
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library
import com.foxit.uiextensions.controls.toolbar.IBaseItem
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.utils.UIToast

import java.io.File
import java.util.HashMap

class App private constructor() {

    var applicationContext: Context? = null
    private var errCode = Constants.e_ErrSuccess

    internal var mLocalModules = HashMap<String, LocalModule>()

    internal var mMultiTabViews = HashMap<String, MultiTabView>()

    var isMultiTab = false
        internal set

    internal var mTabsManagers = HashMap<String, AppTabsManager>()

    internal var mTabsButtons = HashMap<String, IBaseItem?>()

    init {
        errCode = Library.initialize(sn, key)
    }

    fun checkLicense(): Boolean {
        when (errCode) {
            Constants.e_ErrSuccess -> {
            }
            Constants.e_ErrInvalidLicense -> {
                UIToast.getInstance(applicationContext).show(applicationContext!!.getString(R.string.fx_the_license_is_invalid))
                return false
            }
            else -> {
                UIToast.getInstance(applicationContext).show(applicationContext!!.getString(R.string.fx_failed_to_initialize_the_library))
                return false
            }
        }
        return true
    }

    fun getLocalModule(filter: String): LocalModule {
        if (mLocalModules[filter] == null) {
            val module = LocalModule(applicationContext)
            module.loadModule()
            mLocalModules[filter] = module
        }
        return mLocalModules[filter]!!
    }


    fun onDestroy() {
        for (module in mLocalModules.values) {
            module.unloadModule()
        }

        mLocalModules.clear()
    }

    fun unloadLocalModule(filter: String) {
        val module = mLocalModules[filter]
        if (module != null) {
            module.unloadModule()

            mLocalModules.remove(filter)
        }
    }

    fun getMultiTabView(filter: String): MultiTabView {
        if (mMultiTabViews[filter] == null) {
            val view = MultiTabView()
            view.initialize()
            mMultiTabViews[filter] = view
        }
        return mMultiTabViews[filter]!!
    }

    fun setMultiTabFlag(isMultiTab: Boolean) {
        this.isMultiTab = isMultiTab
    }

    fun getTabsManager(filter: String): AppTabsManager {
        if (mTabsManagers[filter] == null) {
            val manager = AppTabsManager()
            mTabsManagers[filter] = manager
        }
        return mTabsManagers[filter]!!
    }

    fun copyGuideFiles(localModule: LocalModule) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val curPath = Environment.getExternalStorageDirectory().path + File.separator + "FoxitSDK"
            val file = File(curPath)
            if (!file.exists())
                file.mkdirs()
            val sampleFile = File(curPath + File.separator + "Sample.pdf")
            if (!sampleFile.exists()) {
                localModule.copyFileFromAssertsToTargetFile(sampleFile)
            }

            val guideFile = File(curPath + File.separator + "complete_pdf_viewer_guide_android.pdf")
            if (!guideFile.exists()) {
                localModule.copyFileFromAssertsToTargetFile(guideFile)
            }
        }
    }

    fun setTabsButton(filter: String, button: IBaseItem?) {
        mTabsButtons[filter] = button
    }

    fun getTabsButton(filter: String): IBaseItem? {
        return mTabsButtons[filter]
    }

    fun onBack() {
        mTabsButtons.clear()
        mTabsManagers.clear()
        mMultiTabViews.clear()
    }

    companion object {
        private val sn = "GZQeo8vOgo2jsuQDaUgY4afaYwh4NFrYzB3yqw+cA5QBSUjLNK5tcw=="
        private val key = "ezJvjt/HtGh39NviuavWHie40JbgwYm0845o0hI8jTHfp8FdjT/lmrXSWu1DXvCVw0gFaF0eTEJLnpjhwMhU3VD/Y52VWLB2rjEslQM5fPT9bBOUQUjAcSqd/Yu9Lj14k7/fhhbI4VAnigucfLeIH3B9ciQaykMsRT7D9AVmRlxSVfAN9+f3HXDgVQRrN8rqyoEe5+JSmHpndJycOPtglPOwEfFpsE+TDR6DSW8EdFLM/4vxqBULOR2j3fAWFAGDZbJidytTgzvo/JYkgc999xpsPDoWOU4BwoxCDxMbPR2aGTkNe8M5aNGprtC7VKH3yzaAhbLCY8oCwqMsmRdBZGhxMYfUrAGx59uUOasSVdjaDL+uWUewjypCoFOBiPuSlZtYGwoFzZvNEJ1DsUq5x06mDmommszXtM/T/nxOCQZ1Eda6ROQEcONXSLl2HuNu7gf8CM+3B3xiXMG/2PvEYtQdt5eGbootyIEFZp04OorzfiEpppFwC+53b3i2leG3k0WNAJ0nSI6fUJrLU3dpu0fvexuLwOs0rdP0YiA9odsY23lplPQGILLCWat++y9Z5Pi1SoCG8HR1f7EpeafqkhqdvBBc61Gkvrr1pXeCnyBFUhLWCCjQktu46vv7o4pjTACQOfPlyo4WG9rBmxTU0APUEEjgkkwlLMpJMacXLJFJiwhumP/kSuXwTrLhyC0hc1s4H98EepXF5DH/6Py53LcjHw4+yY5fVJNRry68jJyg775OcxuPaI8BdWuTuyZO2cpVZXe6zu/Q9s5TndyC7bbbi4+JZZb2mJNrsmUlfVWI3z/Lx4iWBJrI44nkHZbr/kCj1AM1XSpNF9hC1k6SXoo/h7b7HF8EsdN3hE118mQ02D1mJ6mSyotU3ktrWGdX9EPM8LwF5YYxpEFBKpqWhtRr+RL/3pRfJoU+O8ZMhizbUk3+dwWYAigD+iShchMamkdI+U3eGZZW5wMqOek+NRqt7ns+QWQXi4xqL7wbwOCIADtKYwE/sUmnzX1jzeBZklVhACcMmdxfOYZh7vyW0qYr9jLzxh1ZdEL1Ta524el56YuwdnGLBTkTfFp31/r9Tfx9i8dDiXRBX3OHa67RA1zfj532RS+BGGJTBlfoCkbjrrOhVhLICEFMDVzTUzFVPx0Q/ZxGlJqKf6NmLZ6WuBUZYYipVoJgqzjmJeo7an+7y4Yibb+PjYe1ngYa6qrThword3Sp0xQMpDkSWBu67Ey6vpDDCnqJb8OgwmQ+SpmFess1KwZzD5A="
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
