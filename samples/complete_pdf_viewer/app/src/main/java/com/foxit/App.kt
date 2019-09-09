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
        private val sn = "ptdYvfJKfJI06xrDhS1piwU+L1J8HIojWN350Cmyhj5hR8byu+uRVQ=="
        private val key = "ezJvjl3GtGh397voL2Xkb3kutv+31UPMKx4UUPMGs3rntpNxieh6l2mC/z+riZ22Dv2cwp8pAAIvGQDbBE5R7wdG50qvdPq6gDSL+bHZNSqWEJEc/HmGcW93mzbwpxkqO0fn54buD6tHAhTw2gfMhbJRInGo0pFzRk/zJYd2Bi/rSZCwqvAij9SIX25bM7WeRxX7qPFYK5/ApEzBCh9ndbqOeH5PgW+Pz0PvTLJoJG+ZnQ7tsb596qrt07ngxqcAikoIwWq+hgstTljeDTV+GVEGSD81T0cdru18Gx8rus3wsCOXfTQmYZ3kZnCi5MG/f/Ams0vYFUe5lYIreT46cmFDbQUKtXg/p8yoQw+DECqciJKdT3MZXAx0s2wFOvNMJ7Z5Gxlxjb3MUrx5lPIdIZDQ9zO882Co+uECQEewioovulJWLoSHsFskCXvGSOgsTMRCrml3wJ1mOt3hT8T8CmsMsKfgdDqYbjrZOTpSUImq2EPjVS3KsrxIxorh7awh9nloRyFsrohXOW19+azLobZauEwCncrWdXpLhe8/zzaxQ9vyy5gJEAG820+vFpToDW03L2sWR1dDKC13wg5r1zIwZ9Xdfmpd1sFgGFa/VGUd3835gfd/QcURM2mWBbPqtxB0HRHOL9repYaoky3dme3Rot6C0TEKZyOMYXtuLVgnAweVUkoh4Lcb3Q3UVPzd0lv5fVvcLRHUFbJAZRnc4lwMhb3EeUjyGdZ5ADQqrwvOpFg40KRo3BZYM/iabF5Cd3UYUkMFF+Pq4m+9ZHGu9xgzxce9NEyuAz3DVThIJGVv+a4PQdLhLAvHLkUBrBvgXmS2WVuvxIMyK+rNCbnS25i45cek31nmoHVFB6GBuZO4KFCzMxrRBEOto5Fv/7OO4105+s39jUGnupqRkYIGm/aRDbUiExprG6tW3NsCqaxG+1guO+LIz6QJcklOiEY+Z43itg9uSSAicn7KteoPuqJ3+S6xxhjaa0wmLRmQXcQgdHu52om08yKmYsIf65VUlTf/kw3T6ay4lpnpBXl5PgtZg6cJq+peJhJXl0CukMQtHAQPgWbGgUkFHY/qthm7UoqSbv1lIW1v7Sva7KT3ZzG2XyY9PKr47Jks3rrexuyzh+zHFEAx+HTAnrPWts57T5yvvJE3AvWltKwpM4HVpta9Y+EouAT50uSIJnkR7uwP+E2fh44/jH+rw9LVu28me6HnhHUOoCIWJ+zeaunoRP8pdtWV7y/MgF1VYcql3og7I+/Uyg=="
        private val INSTANCE = App()
        fun instance(): App {
            return INSTANCE
        }

        val FILTER_DEFAULT = "default_filter"
    }
}
