/**
 * Copyright (C) 2003-2019, Foxit Software Inc..
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

    internal var mTabsButtons = HashMap<String, IBaseItem>()

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
        private val sn = "qgVK5jS3KYEa5d/7a0aCJpZHuXc1ite9LBERoywRU5LQyMZNcXzL2A=="
        private val key = "ezJvj18mvB539PsXZqXcIklssh9qveZTXwsIO7MBC0bmgMUWD2d+2vk18zJ02pl+5NZ0I/5z+SGPshNpVNR/9bK/6rzAqEiIcptvrDVZFBf8BtPD3DiHacfAC+00W06fFZsc8/upASquVGTB2AE5+vQ7QLOyoYjnLOnKIXprWJ13ieiZTd+dgwEc7qx4AwPm3KohdnBIau1l7ezggBr4ToFyIph4b9kc//TTKBZVmO/naXjUkOXHux8hw1MxsiY3Y3ITf/U41yP50zPDqS71fiuhceUd0uRV1MJ8BE5Fa/DNw1V9EvOvy9nJsml14OAajxnzq7EVB3OkTaZv7hU392bHZJFQm7nrgOFlC0GL9Kpt6LvJJk0nOKe9KIW0OVZuPd5TfOoNTIiPV5RzohOwxBWs0MHhTox48O8rSnETDmyidPUpQpK04i7fMZxIn56MCD5dPkjtS+lwv1aYcVaZCB5eIpcDyvnIZZnvIrFu+DcYhSw6Tj2VfLTwwLlJ6/ImQZAHxVua/6O7hRi2ofImEVOkqhkkj1hOiyIBGW689ZGaKa2MLTbNVmj+EIkmxyXPrSfvkq6LMZRLqLasmFDc5OB+RCldbgzmIFifk5yxTC81HFNBHKYq1l2LcSUTDS5gZGBwv89QNVpjnYK1qzMKxdW2DJb8vsXE+7uXq4vpzfOofjtAp7V4QlLTXXLIrB2V5exBHWxAU9BdxSTviIsjthEICncV0mbrigYj7r7b7oIRlOYBJr1ZRMstI0or0Esl2jbC2da/gFsbiyWy5iNzcYwVevRCS4eL5DTGkSyaPs3sjOfGb4WBxZ03f7BL+S9eiK1Dv14ytEWa9Z/6EApXCsHgCJYzJW13EvkA9bziaden0OzIPBdfk7i9mdklH80BPcI0J5sC1lB2XCRGqmQr8QkCL5/JZY+xoGAh10GHafhXnhpSHZ9qaBSZzhTArIu8h6PEd2UhFSLYkfoXto8/N/6r8/YLBsOqVhMrq/5WoD6cJQQdGm1KLbqQvRnSui7VafTGZ6QbwkcVMzJV7w9hVR/q7hqRo2ZPi1olraM8kVnD+SSpeK6qkD2sgmoTfYzqAEz2AWq8XlNSEEoGTyJzgGzEPgfmpMvHmpkhtGONOKCSA/R36VuOR3GG9psBS4god2Q3gN4+v0guQV+BqFyu9EcsxnV7bEJ1Slr9o+TtUvs6PiP7Reg7rekObp485zQyuRmOZfIbycg7kBC575myg4h1"
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
