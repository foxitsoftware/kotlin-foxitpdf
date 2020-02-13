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
        private val sn = "XmE4UnLG/IaLL6QwTqHtLVMGcBiJTeRm4fPIWPmzcWt3+hhXfbW5vg=="
        private val key = "ezJvjl3GtGh397voL2Xkb3kutv9oSAOWRQ7cbJfN3y7VglVhz0T2xqzpSWcuWyR+pNaUYD3bs8Es82uVUUh2hXdnXA5MhQD0zlTI5AEgPxpHT7x+TEiQKeeTPfdOfea8Sw9sPPuasmPH8XtcKkdbgcgQQGgcz3CCZIAQdC9YKlNdKtIUTHp9VOeRKXFHWulsn3GyYQ+b2c8V1BjQJHFR+AXpQj1sxykcFQrbCrGGzZtupm2Sn9uxotXtJIpUOokx1y+XZeJ1ZeY72C/3/LlJvZ7EVtyx6agWuyjOqOLTah+Y0KC0quoBrJbTgvebddnygxHXwbBI8oyCNSaTzI4f41KxFnYA0YqfvLOAVOra434F4xQSXeGT2EPwRU279QBDFDxrDa8X0QD8HJ+wUN/+/+OMTZ7GXVeBZNFbUFTt4H5P7cTvdBxTxFCXJdaO54QvW5PLQ6tAUIh3ojOxalTNsBSM8KzTdHX774Q+uD6HWysE2rP0WW3qkjBDXRSniT2H5N32CcUXKa/M/avpHV6AXAOW7hi7fFQWRuV8Bz74wYU/oACWx1TteyVd0ukcd/LnTqoSPDILBftp35KdMYIJmqiCeFSqGdKfoQ6SRe1B9PA77ZFqzQWuzWVtrn9owEnb6Z8yEFHxmHucggf8veSsCWOY6JBvUpHhR/QabOI2GJ6xfb4NFojBdrSZwQrUVvwdb3ThYyx4+Kv2c0SpyQiv9dp1i+bqa//g5xX3p+wsyWKr3k9HXoGSicgDv9ShIgv8t6ekr264EeLvp0muL42ulEsRATz+8I2/rZcfCnLNv4lIgvgLTIQYalV3BIumgRvAoiXWCOBFpjUhpg8nL5IAZdjI5cWkn/x86+380oc8p86IKCCvIx7xBEesYxBg/7EO0123iDbDONxxI2MSM0AJ7Y/HgNiORyPNBWfcgV78pH8A6/dFwUq/Lz7igqpLHy2npaPCVg9+SSciQn7NtfULusJXRpJ9uqzHU2und1t3vZgaEm6o7yeaw/s5NwGWg8J92P0kOV/JyZHhsI5T69Ht0wyPjKLDDSAwOEfnKaHvSx7gx+X42uUGg8e5MLu6BWlCJk2qru5+V4pt7SvYLKP3JbG5XSY9PKjt8wilb6qFHCnRTaI4bZNT7uPI35/rVu0ts7AWlNCbA/3lVK+qNcF8LvUHQlP60QNOPVO6jzsR0g5bGdUTmec2pdW66G0CffmcvC57K9RldfHl1w4bo8sxNFhmi/SpnDDydG/XRzI6D/LE81pZ"
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
