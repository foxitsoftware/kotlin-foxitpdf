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

    internal var mTabsButtons = HashMap<String, IBaseItem>()

    init {
        errCode = Library.initialize(sn, key)
    }

    fun checkLicense(): Boolean {
        when (errCode) {
            Constants.e_ErrSuccess -> {
            }
            Constants.e_ErrInvalidLicense -> {
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

    fun setTabsButton(filter: String, button: IBaseItem) {
        mTabsButtons[filter] = button
    }

    fun getTabsButton(filter: String): IBaseItem? {
        return mTabsButtons[filter]
    }

    companion object {
        init {
            System.loadLibrary("rdk")
        }

        private val sn = "sS1No48GllWOhaww26EpDX+mGXcYdi5zUHFRsdMSGxodGTyLDgaYWA=="
        private val key = "ezKfjl3GtGh397voL2Xsb3l6739eBbVCXwu5VfNUsnrnlvx3zI41B75STKd59TXVpkxEbp+B3UEqUNj1KM66ujQN8Mgkr/mKJOJaqOuqngyfs4ccHXmAWTe4ajKpqKI0Y5clxoTqL8tfYrOQZN7SeznxuJdOMwrg2jDyDQc5ffNZSt8Z6nAjHlI4vjZHNrWeW9M+jFgIcaBMRE/hwgZwwQpr/74cdH/VV289PBrvsLtf+hIagpdc0l3tJJzQf00Q/0/PSPp35eeU+YrKuiXiBIm0sLahXrXBU6kdYOoZgteB9dMaH0v2Ev2EF4hzwtcwExvOI8UxUsC71UTl/KJhIiKs9PdM2fZ4AaseldOQvaHs9dGVwsI2LajSXI21IKT3vwOnMHT10V95hnStG/maORwMHDfLjlAyJepfMlP2aU5x7hTFwRKF9bJRgelGeTzn0c3zJM/GhG5YccdzRPtJZvre4RD9oOYw+vrR6/TKoZtX6Nlu5y/FPg2xlA73kLdaaEqulHtDdec25ki/h9ahvyUP30bIMJKaG5F+SPTCemor1Oy4mtaWNhjPY0cVu807luylcfAd70yu/3neiDUc1JlI424i/OLxRkBGJInLdBMgEeU6gY34Rh5QBfWdKq3lHzKsZnHqL7+MDPu16Os3JX+G4rBWVpRMOKxgGTfnp2bkChAUlzL0tX+/iLjWPyADJwpo3AtVyCckdyyQLgvWr93+6nN34YurHHKqYUTQ0oBeRb0a2DYu3fNyAzDgPZ4lXbkbwtMtS4299A4lUnVJcA21ZBEqC0/mcu/eHHd1UdBBouaD6rkXQ53OzznjMCjibCYbNurh4X0toPxSrqbRU7/LBkzNIbUD+YH1AFAG6Uxi/arFjXBV0Wg0JKCZy1WBVeIfpTW/vtOxAaSsL4FX2930kqZhbIrbTBgOwlsDJO4d5LWFZNuCqjvI8U00ilJExKXAz0w5UTUGfLZraS85ur/zHRs6d8V+psFURmcaCpkLHOE8LrSfT+kat8N6GREjuZItoGs0NOkKYvj/lL963WcRWikieGBNP9Pl/hgpdIXew7nue6U9XGoTgdz2lLR6QtC4EFuVHheMP455C7pRlKJ+7gN9L9+LdoZ1c7LgthMGNg76WWkO129/xwSSDyE7l9z/HbWiAyAtYYYJe02Zl1sInDc30jFrpkXpOocoIa9qnh8EZN859NYJqkQiqJE9CIJ66DA0DNk8eNnaJaBNAzAv2eH+lwEXckM5Re5xjo+69QB0T2Fpx7nFR/cnSw=="
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
