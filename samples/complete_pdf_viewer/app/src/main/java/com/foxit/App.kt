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

    fun onBack() {
        mTabsButtons.clear()
        mTabsManagers.clear()
        mMultiTabViews.clear()
    }

    companion object {
        private val sn = "DW6QUNWzT4IF0JluGTcsD/OOs12XAfWdNkVPejrfj72Mi6P3xjaElw=="
        private val key = "ezJvj18mvB539PsXZqX8Iklssh9qvOZTXwsIO/MBC0bmgJ1qz4F0lwleIbZghw4SQD11gx1jqOJvZixkxBpuX+IYO08ZheJYkMQumnRHZ+eSysccHXuESTewCCK1K/MgHY/k54IqDvVbkdKIJu9QmzmxEJhP8zrulCE5v/XtHRKbNVNsvDpNop2HE1XwRtNon3s6YQ+j+c8BACgApAsRalO9SQP2GlkkBEAYqvY2JrrnRhfeZngd25kw9CZGd9p3QToervqkj64UV/3I4sqU/0arSotj32QPFCYAt9roaKCzAYoTeaE/l7zlnpd3dcZwf7NiYwSSrQ2LNpD+r2lHGi8WGr2hO7hwWtX9vdklMHOf/YFo+/05XxoVlnVAtYXRxx7S3MVeSEnhswujY+AswVbBgKGJRGRWzKnv7py3803X3DH5PGqRRayjTceiRq0ddSf7GiNtRQittqcRQSBYet43Rvyca+NyBxa5DddneG1VbBaVtM12C2Xv02/8HNLAf5AF3Vua/6O1gRi2ofIm0dqpk59lj19OiyqBIV6Ma/HZ93SwKLycOxDOHIcn2cVM1UtY+pF7ptUGz4Mq2V9YBaB2wxFB7I3mUEBjGOx1Y1ZAyvpWclWebMpIUct3ku4PmLsuSeX1uAd4iGOggcIFXPiUdYu7RytAbIlnWLtd5FZnwAfsyN/norSSmAtdZMHEv0xmh865YCFDkyo7Lw5ilhUICpZV2qJqqg6n757PdZcyO+M57r5bdcMto40q3M+lmiqOV8Wj/ui9v1h+UHOKQBvCti5TYvI5FWN/biCleETDEXUV1aMvVm/Zcyuu4njBWgL+0FMzCx72Lv0oIHsSl3THc2TS95YL9/3QpYQTAue6VpXdEAN1s3u4rzQVJCmT2QPK4FP/pznBYEP289VheUd1I521v93LZf9TWFDeIUIjE83bEGdtlJRdbqPR2fXccdtLWUeG+Ky97MqncQHy4REqjmBqNxjlo/gvEshBV7VOntNcUmpCLHKyZF+IupSlQ5zO0lJ9RaPShX+VkaI9rx17Oif8q0qvz29nA9s5XyBe87VjQm6BjA7b5hZnixsuZlv+R7ZhyWU5jaTh1BuLbz3zIDAO90rK9qnMP2hm5AFRmy962CqDi/vW0nyQISpgMlSJsGkPUxpg5TuhiGe13TEHMQyHVdBodOcMUBaO1sk4mdeYk7qUm78ek2VL4PhgZHZO3KE+B1ASiVG4iqGAbYiM"
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
