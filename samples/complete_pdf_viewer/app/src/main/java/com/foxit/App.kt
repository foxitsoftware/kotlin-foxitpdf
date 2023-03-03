/**
 * Copyright (C) 2003-2023, Foxit Software Inc..
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

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import com.foxit.home.HomeFragment
import com.foxit.home.R
import com.foxit.pdfreader.MultiTabView
import com.foxit.pdfreader.fragment.AppTabsManager
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library
import com.foxit.uiextensions.controls.toolbar.IBaseItem
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.utils.AppDarkUtil
import com.foxit.uiextensions.utils.AppFileUtil
import com.foxit.uiextensions.utils.AppStorageManager
import com.foxit.uiextensions.utils.UIToast
import java.io.File
import java.util.*

class App private constructor() {
    private var mContext: Context? = null
    private var errCode = Constants.e_ErrSuccess
    fun checkLicense(): Boolean {
        when (errCode) {
            Constants.e_ErrSuccess -> {
            }
            Constants.e_ErrInvalidLicense -> {
                UIToast.getInstance(mContext).show(mContext!!.getString(R.string.fx_the_license_is_invalid))
                return false
            }
            else -> {
                UIToast.getInstance(mContext).show(mContext!!.getString(R.string.fx_failed_to_initialize_the_library))
                return false
            }
        }
        return true
    }

    var applicationContext: Context?
        get() = mContext
        set(context) {
            mContext = context
            AppDarkUtil.getInstance(mContext).curNightMode = mContext!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        }

    internal var mLocalModules = HashMap<String, LocalModule>()
    fun getLocalModule(filter: String): LocalModule {
        if (mLocalModules[filter] == null) {
            val module = LocalModule(mContext)
            module.loadModule()
            mLocalModules[filter] = module
        }
        return mLocalModules[filter]!!
    }

    fun onDestroy() {
        for (module in mLocalModules.values) {
            module!!.unloadModule()
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

    private var mMultiTabViews = HashMap<String, MultiTabView?>()
    fun getMultiTabView(filter: String): MultiTabView {
        if (mMultiTabViews[filter] == null) {
            val view = MultiTabView()
            view.initialize()
            mMultiTabViews[filter] = view
        }
        return mMultiTabViews[filter]!!
    }

    var isMultiTab = false
    fun setMultiTabFlag(isMultiTab: Boolean) {
        this.isMultiTab = isMultiTab
    }

    private var mTabsManagers = HashMap<String, AppTabsManager?>()
    fun getTabsManager(filter: String): AppTabsManager {
        if (mTabsManagers[filter] == null) {
            val manager = AppTabsManager()
            mTabsManagers[filter] = manager
        }
        return mTabsManagers[filter]!!
    }

    fun copyGuideFiles(localModule: LocalModule) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            var curPath = AppStorageManager.getInstance(mContext).defaultFolder
            if (!AppFileUtil.needScopedStorageAdaptation()) {
                curPath = AppFileUtil.getDefaultDocumentDirectory()
                val file = File(curPath)
                if (!file.exists()) {
                    if (!file.mkdirs()) return
                }
                val sampleFile = File(curPath + File.separator + "Sample.pdf")
                if (!sampleFile.exists()) {
                    localModule.copyFileFromAssertsToTargetFile(sampleFile)
                }
                val guideFile = File(curPath + File.separator + "complete_pdf_viewer_guide_android.pdf")
                if (!guideFile.exists()) {
                    localModule.copyFileFromAssertsToTargetFile(guideFile)
                }
            } else if (!TextUtils.isEmpty(curPath)) {
                val uri = AppFileUtil.toDocumentUriFromPath(curPath)
                if (AppFileUtil.isDocumentTreeUri(uri)) {
                    val directory = AppStorageManager.getInstance(mContext).getExistingDocumentFile(uri)
                            ?: return
                    var fileName = "Sample.pdf"
                    var file = directory.findFile(fileName)
                    if (file == null) {
                        file = directory.createFile(AppFileUtil.getMimeType(fileName), fileName)
                        localModule.copyFileFromAssertsToTargetFile(file)
                    }
                    fileName = "complete_pdf_viewer_guide_android.pdf"
                    file = directory.findFile(fileName)
                    if (file == null) {
                        file = directory.createFile(AppFileUtil.getMimeType(fileName), fileName)
                        localModule.copyFileFromAssertsToTargetFile(file)
                    }
                }
                localModule.setCurrentPath(curPath)
            }
        }
    }

    private var mTabsButtons = HashMap<String, IBaseItem?>()
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

    fun selectDefaultFolderOrNot(activity: Activity?) {
        if (AppFileUtil.needScopedStorageAdaptation()) {
            if (activity != null && TextUtils.isEmpty(AppStorageManager.getInstance(activity).defaultFolder)) {
                AppFileUtil.checkCallDocumentTreeUriPermission(activity, HomeFragment.REQUEST_SELECT_DEFAULT_FOLDER,
                        Uri.parse(AppFileUtil.getExternalRootDocumentTreeUriPath()))
                UIToast.getInstance(activity).show(R.string.select_default_folder_toast_content)
            }
        }
    }

    companion object {
        private val sn = "l5uLRkyIIDIyKJQZBChK2tXW/BikAnJozYEi1ApEyOR7i8W3U0ZlKQ=="
        private val key = "ezJvjl8mvB539NviuavWvpsxZwdMWZ2hvkmJNQZ8S/CwnxmS4c9F6U69I385uOe2wT4Fg2fJksQtXtnFsJ6lZR6RmsquC9T+GuC1YcZfAx/DRZivPTAkOaYoOwHQhGkkeTytiGg4KlolOVjyyRy5ZjzBBuwgODp1AcJAdTSvFlZnl+iCoYbPEKxUo/2+grZrhLICAXhrEioM4AwgIp1FxhQGlTLdv6OmuczqP0jt4IAEEJ1VhL5rh8X1fTGpx8fR8i0o0Ez/X307CCLaHBYLVXWMaZRn0XCsA1cOtcnD7XME1T4rHm4e4F+leLLPeylUoAMA/x1LwHj2yky9b2IclJxYcXRVdZOjCZsNPLpUDZS/UvAdTNrbkDl8fS/Vx75QOW+2z8//pjK4UR23WMi9yuvhXpfyi5Etv0aZDe969Pmc1vt2zK2Ddz2EAO5BslqcPDw2eBfCMBQL+iz3p9xg0XI9pAI6DnRDuqHkqHh6EVZ6zN20BupuDOdTg+PemU739fedBXY7TQz7ORE6BtzvPIlpyG1mNKC7A3bOIzyTDbVfSq3bPj5qoas7brtGTce1j0EHfzF3rzyFsKbvxcTcBRKzV+bAvtNofD4qPtqz7edHNbJKVcugoARzikVFW3dD7d14p7QUV+d6QkQf12KvzocGRfY1cHC/+Cey25k0+UtFQ/KdhaU/EVOfprWqeJLUyqX/GV9WX7I3A3OF8nTqeh7UpaOin8pA3T5k3tzAcnzFf9jFXjZeT1cRhClLSbWR4fGn+rxeLr2lwTOa9kBR1BY/iwItyY7uxCj1LcxtLKNC+BFRK4tXTsFlCjQPJOreF0oBxAhSp8dTmeXsdb/QVJMlR1iuJwqIWoxfg9+zHBNPUHpK33weRQ/j2gRPGBV2eW3+Wqcx+5VyB3PtCxaheJ3jMgXD2/1UBh24JVUVwgL0oQ3fi7EhleoALwQaulCWP5TTCOioPJFjVGBMo5BfH4o4rU1JNDse/QIauw1EkQQHlzfazCpU9gHnP4nBNKAgn+fNc+hwDBEP0dmlIEeHvy4kGEQQCwtMuV6Ezam1BAUwjKp8Lw5d2B/8d65mUCj1kZl2cXLEAnrwFCyZ8+RHe4XK+DZGCbwjzcyzJdQ+3qUrVgf9iseJm9XpOZp1azqo5nfOThl4lJAcEty7lsbXRpldNiFb8VE/hMkm/cFR9PNj40N4Zq+EvdiSO1ZwaEyM67OHwgo6i0QtGhp1SNA6Enq6OVNEy9J0QF5e2XT4UoZNN7roRKkP1ADvQA=="
        private val INSTANCE = App()
        @JvmStatic
        fun instance(): App {
            return INSTANCE
        }

        const val FILTER_DEFAULT = "default_filter"
    }

    init {
        errCode = Library.initialize(sn, key)
    }
}