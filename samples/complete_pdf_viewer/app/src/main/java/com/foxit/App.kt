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
        private val sn = "BW3CdOL9RzR/4rAyb2+Ze4s5sP8katpRwE+DOEB3k2rQZtFD5HJG5Q=="
        private val key = "ezJvj93HvBh39LusL0W0ja4g+PQUKROlWYrFaqfVeOP9qnxzhL01J+KHAmohNqVQ+DF+mvMHx0afiHvfYfpythLfEBEraoB6ODghaWXcBpU1+RmsaTiEau5SSVXbEg1tGbao3l6g7DLOSO3p4qazFrs/TvSy39FzXlmnGAGYf5vY3S4eKTqaRBYgzlJ526WedxUbq1BYK8+QZYjA6GPncrqOGH5OAW6Pz0NpDLpoJG+ZHeDTdOR9QP05XiVwBWW6ol+/hO7lHNGNu20rXf1GpMxkDLscPoFG4+N9kLtThf2Z4KCEtOphI7v4Zb92eSOdT0LEoCv/NgCvbXzwHzbSvE6MqM+s6IWYN/KBVXqZQIcfQatk3+KT2EP2RUXb9QBDBD1rDa8b0YD8HJ9QF4Ip/oN7aiu9kaD7Ih9+oVv40WbIllNZVbtreEpw0fBGb9OsS1RFrGl33JbgO6MgPQSTziyTE6VaFvjFjVgsuQUeaRfAF+x51hTKqEpQCxR9RQWF/SL9DcWhpOc5gO7JfWv8ZXYh41TthV8TBmg+2MTtUHY7Jbug+lAsMZd8qTpcviwYGzbAroqbinxaxOprGK7sGnRKHw79JYhKKjpvli6xpaaXw6aggjEaC/DQ633pmWrETK2aWXBRBrfnjHRky7urcjkuzg6TIDjxQ9Gl4mV0Ue8V/I4FXmqSBd3km6p9yn7oMgrcxXtbznW547+uyE11h12exYlwlkGCFf/tHtH39l7LJ6wIJDY67arTMmafyIAEYDKABvje7oYkUZzFUM4Uei7My/Nx4aAjTNFWuIZrJUAfVa0EbSeOjhJfxi8tIDVmF0lDQfj9AKKu686SzzosAw97LO62iA+00fysyxby10xLTITnGb/wuwIElKXo3e+6rN3q+7hfj85iM7csgvD9im2JHTIZj6TV9xN0MsevzHzqEO2VMhdukPkdDp5EiGhDsPn4KS4oLekRL148IhN21oEA03tb/WVvWmkFzMIxsspvpg+HcBiwPYm6ahvcSf83fJjmRb+Gk0LOTMYLCXqRiGKQSOVD12X9Uu2VueKnIKoPRo46it0EJAR/YKlWrZY0DlICsLRsYXDg/lmOS2BFu+nyPRi9V9ND0WxjqbNWieEJmf4wz1TR3VTL1BSBcqBq+SXVXLnuDjbuzulrjtIXS3UG5VPpwAX+/wVN3F5gUklRr5DJ7KiG7MM997QsTQpMwPe++zlVDmucBJ24HzuIhVw="
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
