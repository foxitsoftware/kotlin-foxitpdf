/**
 * Copyright (C) 2003-2021, Foxit Software Inc..
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
package com.foxit.home

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.foxit.App
import com.foxit.pdfscan.IPDFScanManagerListener
import com.foxit.pdfscan.PDFScanManager
import com.foxit.uiextensions.controls.toolbar.BaseBar
import com.foxit.uiextensions.controls.toolbar.impl.BaseItemImpl
import com.foxit.uiextensions.home.IHomeModule.OnFilePathChangeListener
import com.foxit.uiextensions.home.IHomeModule.onFileItemEventListener
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.home.local.LocalModule.ICompareListener
import com.foxit.uiextensions.home.local.LocalModule.IFinishEditListener
import com.foxit.uiextensions.theme.BaseThemeAdapter
import com.foxit.uiextensions.theme.IThemeChangeObserver
import com.foxit.uiextensions.theme.ThemeConfig
import com.foxit.uiextensions.theme.ThemeUtil
import com.foxit.uiextensions.utils.*

class HomeFragment : Fragment(), IThemeChangeObserver {
    private var mRootView: ViewGroup? = null
    private var mOnFileItemEventListener: onFileItemEventListener? = null
    private var mScanListener: IPDFScanManagerListener? = null
    private var filter: String? = App.FILTER_DEFAULT
    var singleMultiBtn: BaseItemImpl? = null
    var mIvScan: ImageView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is onFileItemEventListener) mOnFileItemEventListener = context
        if (context is IPDFScanManagerListener) mScanListener = context
        if (ThemeConfig.getInstance(context).adapter == null) {
            ThemeConfig.getInstance(context).adapter = BaseThemeAdapter()
        }
        ThemeConfig.getInstance(context).adapter.registerThemeChangeObserver(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (this.arguments != null) {
            filter = requireArguments().getString(BUNDLE_KEY_FILTER)
        }
        val localModule = App.instance().getLocalModule(filter!!)
        localModule!!.setAttachedActivity(activity)
        App.instance().copyGuideFiles(localModule)
        localModule.setFileItemEventListener { fileExtra, filePath ->
            if (mOnFileItemEventListener != null) {
                mOnFileItemEventListener!!.onFileItemClicked(fileExtra, filePath)
            }
        }
        localModule.setCompareListener { state, filePath ->
            val compareListener = activity as ICompareListener?
            compareListener?.onCompareClicked(state, filePath)
        }
        localModule.setOnFilePathChangeListener(OnFilePathChangeListener { path ->
            if (AppFileUtil.needScopedStorageAdaptation()) {
                if (path == null || AppStorageManager.getInstance(context).isRootVolumePath(path)) return@OnFilePathChangeListener
                AppFileUtil.checkCallDocumentTreeUriPermission(activity, REQUEST_OPEN_DOCUMENT_TREE, AppFileUtil.toDocumentUriFromPath(path))
            }
        })
        val view = localModule.getContentView(App.instance().applicationContext)
        val parent = view.parent as ViewGroup?
        parent?.removeView(view)
        mRootView = RelativeLayout(App.instance().applicationContext)
        mRootView!!.addView(view, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        mIvScan = ImageView(context)
        mIvScan!!.setImageResource(R.drawable.fx_floatbutton_scan)
        ThemeUtil.setTintList(mIvScan, ThemeUtil.getItemIconColor(context))
        mIvScan!!.setOnClickListener {
            if (!PDFScanManager.isInitializeScanner()) {
                val framework1: Long = 0
                val framework2: Long = 0
                PDFScanManager.initializeScanner(requireActivity().application, framework1, framework2)
            }
            if (!PDFScanManager.isInitializeCompression()) {
                val compression1: Long = 0
                val compression2: Long = 0
                PDFScanManager.initializeCompression(requireActivity().application, compression1, compression2)
            }
            if (PDFScanManager.isInitializeScanner() && PDFScanManager.isInitializeCompression()) {
                val pdfScanManager = PDFScanManager.instance()
                pdfScanManager.showUI(activity)
            } else {
                UIToast.getInstance(App.instance().applicationContext)
                        .show(AppResource.getString(App.instance().applicationContext, R.string.rv_invalid_license))
            }
        }
        PDFScanManager.registerManagerListener(mManagerListener)
        layoutParams.bottomMargin = 80
        layoutParams.rightMargin = 50
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        mIvScan!!.layoutParams = layoutParams
        mRootView!!.addView(mIvScan)
        return mRootView
    }

    private val mManagerListener = IPDFScanManagerListener { errorCode, path ->
        if (mScanListener != null) {
            updateThumbnail(path)
            mScanListener!!.onDocumentAdded(errorCode, path)
        }
    }

    private fun updateThumbnail(path: String) {
        if (!AppUtil.isEmpty(path)) {
            App.instance().getLocalModule(filter!!)!!.updateThumbnail(path)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MainActivity.REQUEST_EXTERNAL_STORAGE) {
            App.instance().selectDefaultFolderOrNot(activity)
            updateLocalModule()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.instance().setTabsButton(filter!!, null)
        initTabsButton(App.instance().getLocalModule(filter!!), activity)
        App.instance().getLocalModule(filter!!).registerFinishEditListener(mFinishEditListener)
    }

    override fun onDestroy() {
        App.instance().getLocalModule(filter!!).unregisterFinishEditListener(mFinishEditListener)
        App.instance().unloadLocalModule(filter!!)
        PDFScanManager.unregisterManagerListener(mManagerListener)
        ThemeConfig.getInstance(context).adapter.unregisterThemeChangeObserver(this)
        super.onDestroy()
    }

    private fun initTabsButton(localModule: LocalModule, activity: Activity?) {
        singleMultiBtn = App.instance().getTabsButton(filter!!) as BaseItemImpl?
        if (singleMultiBtn != null) {
            if (singleMultiBtn!!.contentView.parent == null) {
                val index = localModule.topToolbar.getItemsCount(BaseBar.TB_Position.Position_RB)
                localModule.topToolbar.addView(singleMultiBtn, BaseBar.TB_Position.Position_RB, index)
            }
            return
        }
        singleMultiBtn = BaseItemImpl(App.instance().applicationContext)
        App.instance().setTabsButton(filter!!, singleMultiBtn)
        if (App.instance().isMultiTab) {
            singleMultiBtn!!.setImageResource(R.drawable.multi_tab_pressed)
            singleMultiBtn!!.id = R.id.rd_multi_tab
        } else {
            singleMultiBtn!!.setImageResource(R.drawable.single_tab_pressed)
            singleMultiBtn!!.id = R.id.rd_single_tab
        }
        val disabled = ThemeConfig.getInstance(context).i2
        val normal = ThemeConfig.getInstance(context).i1
        singleMultiBtn!!.setImageTintList(AppResource.createColorStateList(context, disabled, normal))
        val context = App.instance().applicationContext
        val finalSingleMultiBtn = singleMultiBtn
        singleMultiBtn!!.setOnClickListener { v ->
            val readerMode = if (!App.instance().isMultiTab) getString(R.string.fx_tabs_reader_mode) else getString(R.string.fx_single_reader_mode)
            val msg = getString(R.string.fx_swith_reader_mode_toast, readerMode)
            val title = ""
            val dialog: Dialog = AlertDialog.Builder(activity).setCancelable(true).setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(getString(R.string.fx_string_yes)
                    ) { dialog, which ->
                        if (v.id == R.id.rd_single_tab) {
                            finalSingleMultiBtn!!.setImageResource(R.drawable.multi_tab_pressed)
                            finalSingleMultiBtn!!.id = R.id.rd_multi_tab
                            App.instance().setMultiTabFlag(true)
                        } else if (v.id == R.id.rd_multi_tab) {
                            finalSingleMultiBtn!!.setImageResource(R.drawable.single_tab_pressed)
                            finalSingleMultiBtn!!.id = R.id.rd_single_tab
                            App.instance().setMultiTabFlag(false)
                        }
                        val mFragmentManager = App.instance().getTabsManager(filter!!).fragmentManager
                        if (mFragmentManager != null) {
                            val fragmentTransaction = mFragmentManager.beginTransaction()
                            for ((_, value) in App.instance().getTabsManager(filter!!).fragmentMap) {
                                fragmentTransaction.remove(value!!)
                            }
                            fragmentTransaction.commitAllowingStateLoss()
                        }
                        App.instance().getTabsManager(filter!!).currentFragment = null
                        App.instance().getTabsManager(filter!!).clearFragment()
                        App.instance().getMultiTabView(filter!!).resetData()
                        dialog.dismiss()
                    }.setNegativeButton(getString(R.string.fx_string_no)
                    ) { dialog, which -> dialog.dismiss() }.create()
            dialog.show()
        }
        val index = localModule.topToolbar.getItemsCount(BaseBar.TB_Position.Position_RB)
        localModule.topToolbar.addView(singleMultiBtn, BaseBar.TB_Position.Position_RB, index)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        var animation: TranslateAnimation? = null
        if (transit == FragmentTransaction.TRANSIT_FRAGMENT_OPEN) {
            animation = if (enter) {
                TranslateAnimation(Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            } else {
                TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, -1f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            }
        } else if (FragmentTransaction.TRANSIT_FRAGMENT_CLOSE == transit) {
            animation = if (enter) {
                TranslateAnimation(Animation.RELATIVE_TO_SELF, -1f, Animation.RELATIVE_TO_SELF, 0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            } else {
                TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            }
        }
        if (animation == null) {
            animation = TranslateAnimation(0f, 0f, 0f, 0f)
        }
        animation.duration = 300
        return animation
    }

    fun onKeyDown(activity: Activity?, keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && App.instance().isMultiTab) {
            val launcherIntent = Intent(Intent.ACTION_MAIN)
            launcherIntent.addCategory(Intent.CATEGORY_HOME)
            startActivity(launcherIntent)
            return true
        }
        App.instance().onBack()
        return false
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MainActivity.REQUEST_EXTERNAL_STORAGE_MANAGER) {
            AppFileUtil.updateIsExternalStorageManager()
            if (!AppFileUtil.isExternalStorageManager()) {
                if (activity is MainActivity) {
                    (activity as MainActivity?)!!.checkStorageState()
                }
            }
            updateLocalModule()
        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppStorageManager.getOpenTreeRequestCode() || requestCode == REQUEST_SELECT_DEFAULT_FOLDER) {
                if (activity == null || data == null || data.data == null) return
                val uri = data.data
                val modeFlags = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                requireActivity().contentResolver.takePersistableUriPermission(uri!!, modeFlags)
                val localModule = App.instance().getLocalModule(filter!!)
                val storageManager = AppStorageManager.getInstance(context)
                if (TextUtils.isEmpty(storageManager.defaultFolder)) {
                    val defaultPath = AppFileUtil.toPathFromDocumentTreeUri(uri)
                    storageManager.defaultFolder = defaultPath
                    App.instance().copyGuideFiles(localModule)
                    localModule.setCurrentPath(defaultPath)
                } else {
                    localModule.reloadCurrentFilePath()
                }
            }
        }
    }

    private fun updateLocalModule() {
        if (mRootView == null) return
        mRootView!!.postDelayed({
            val app = App.instance()
            app.copyGuideFiles(App.instance().getLocalModule(filter!!))
            if (AppFileUtil.needScopedStorageAdaptation()) app.getLocalModule(filter!!).setCurrentPath(AppStorageManager.getInstance(context).defaultFolder)
            app.getLocalModule(filter!!).updateStoragePermissionGranted()
            initTabsButton(App.instance().getLocalModule(filter!!), activity)
        }, 200)
    }

    var mFinishEditListener = IFinishEditListener {
        if (App.instance().getLocalModule(filter!!) != null) {
            initTabsButton(App.instance().getLocalModule(filter!!), activity)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (AppDarkUtil.getInstance(context).isSystemModified(newNightMode)) {
            App.instance().getTabsButton(filter!!)!!.setImageTintList(ThemeUtil.getEnableIconColor(context))
            if (singleMultiBtn != null) {
                val disabled = ThemeConfig.getInstance(context).i2
                val normal = ThemeConfig.getInstance(context).i1
                singleMultiBtn!!.setImageTintList(AppResource.createColorStateList(context, disabled, normal))
            }
            if (mIvScan != null) {
                ThemeUtil.setTintList(mIvScan, ThemeUtil.getItemIconColor(context))
            }
        }
        App.instance().getLocalModule(filter!!).onConfigurationChanged(newConfig)
    }

    override fun onThemeChanged(type: String, color: Int) {
        if (singleMultiBtn != null) {
            val disabled = ThemeConfig.getInstance(context).i2
            val normal = ThemeConfig.getInstance(context).i1
            singleMultiBtn!!.setImageTintList(AppResource.createColorStateList(context, disabled, normal))
        }
        if (mIvScan != null) {
            ThemeUtil.setTintList(mIvScan, ThemeUtil.getItemIconColor(context))
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val localModule = App.instance().getLocalModule(filter!!)
            localModule.reloadFileList()
        }
    }

    companion object {
        const val FRAGMENT_NAME = "HOME_FRAGMENT"
        const val BUNDLE_KEY_FILTER = "key_filter"
        const val REQUEST_OPEN_DOCUMENT_TREE = 0xF001
        const val REQUEST_SELECT_DEFAULT_FOLDER = 0xF002
        @JvmStatic
        fun newInstance(filter: String?): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString(BUNDLE_KEY_FILTER, filter)
            fragment.arguments = args
            return fragment
        }
    }
}