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
package com.foxit.pdfreader.fragment

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.foxit.App
import com.foxit.home.HomeFragment
import com.foxit.home.MainActivity
import com.foxit.home.R
import com.foxit.sdk.PDFViewCtrl
import com.foxit.sdk.PDFViewCtrl.IDocEventListener
import com.foxit.sdk.common.Constants
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.uiextensions.IPDFReader.BackEventListener
import com.foxit.uiextensions.Module
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.config.Config
import com.foxit.uiextensions.controls.dialog.AppDialogManager
import com.foxit.uiextensions.controls.dialog.MatchDialog
import com.foxit.uiextensions.controls.dialog.UIDialog
import com.foxit.uiextensions.controls.dialog.UITextEditDialog
import com.foxit.uiextensions.controls.dialog.fileselect.UIFolderSelectDialog
import com.foxit.uiextensions.home.IHomeModule
import com.foxit.uiextensions.home.local.LocalModule
import com.foxit.uiextensions.modules.dynamicxfa.DynamicXFAModule
import com.foxit.uiextensions.pdfreader.config.ReadStateConfig
import com.foxit.uiextensions.utils.*
import java.io.File

class PDFReaderFragment : BaseFragment() {
    var pdfViewCtrl: PDFViewCtrl? = null
        private set
    private var mSaveAlertDlg: AlertDialog? = null
    private var mProgressDlg: ProgressDialog? = null
    private var mFragmentEvent: IFragmentEvent? = null
    private var mFolderSelectDialog: UIFolderSelectDialog? = null
    private var mSavePath: String? = null
    private var mProgressMsg: String? = null
    private var mDocPath: String? = null
    private var currentFileCachePath: String? = null
    private var isSaveDocInCurPath = false
    private var isCloseDocAfterSaving = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mUiExtensionsManager != null) {
            return mUiExtensionsManager!!.contentView
        }
        if (savedInstanceState != null) {
            filter = savedInstanceState.getString(HomeFragment.BUNDLE_KEY_FILTER)
            path = savedInstanceState.getString(IHomeModule.FILE_EXTRA)
        }
        val stream =
            requireActivity().applicationContext.resources.openRawResource(R.raw.uiextensions_config)
        val config = Config(stream)
        pdfViewCtrl = PDFViewCtrl(requireActivity().applicationContext)
        mUiExtensionsManager =
            UIExtensionsManager(requireActivity().applicationContext, pdfViewCtrl, config)
        if (App.instance().isMultiTab) {
            pdfViewCtrl!!.registerDocEventListener(mDocEventListener)
        }
        pdfViewCtrl!!.uiExtensionsManager = mUiExtensionsManager
        pdfViewCtrl!!.attachedActivity = activity
        mUiExtensionsManager!!.attachedActivity = activity
        mUiExtensionsManager!!.registerModule(
            App.instance().getLocalModule(filter!!)
        ) // use to refresh file list
        mUiExtensionsManager!!.onCreate(activity, pdfViewCtrl, savedInstanceState)
        mUiExtensionsManager!!.openDocument(path, null)
        mUiExtensionsManager!!.setOnFinishListener(onFinishListener)
        name = ""
        mUiExtensionsManager!!.backEventListener = mBackEventListener
        return mUiExtensionsManager!!.contentView
    }

    fun openDocument() {
        mUiExtensionsManager!!.openDocument(path, null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(IHomeModule.FILE_EXTRA, path)
        outState.putString(HomeFragment.BUNDLE_KEY_FILTER, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (App.instance().isMultiTab) {
            mUiExtensionsManager!!.mainFrame.topActionView.removeView(
                App.instance().getMultiTabView(filter!!).tabView
            )
            if (pdfViewCtrl != null) {
                pdfViewCtrl!!.unregisterDocEventListener(mDocEventListener)
            }
            mUiExtensionsManager!!.backEventListener = null
        }
        mDocEventListener = null
        mBackEventListener = null
    }

    var mDocEventListener: IDocEventListener? = object : IDocEventListener {
        override fun onDocWillOpen() {}
        override fun onDocOpened(document: PDFDoc, errCode: Int) {
            isOpenSuccess = errCode == Constants.e_ErrSuccess
            if (App.instance().isMultiTab && errCode == Constants.e_ErrSuccess) {
                mDocPath = pdfViewCtrl!!.filePath
                val fragment = App.instance()
                    .getTabsManager(filter!!).fragmentMap[mDocPath] as PDFReaderFragment?
                if (!AppStorageManager.getInstance(context)
                        .isEquivalentExternalFilePath(path, mDocPath)
                ) {
                    //Remove the same file that has been opened
                    if (fragment != null) {
                        App.instance().getMultiTabView(filter!!).historyFileNames.remove(mDocPath)
                        val mFragmentManager =
                            App.instance().getTabsManager(filter!!).fragmentManager
                        val fragmentTransaction = mFragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(fragment).commitAllowingStateLoss()
                        App.instance().getTabsManager(filter!!).fragmentMap.remove(mDocPath)
                    }
                    App.instance().getTabsManager(filter!!).fragmentMap.remove(path)
                    App.instance().getTabsManager(filter!!)
                        .addFragment(mDocPath!!, this@PDFReaderFragment)
                    App.instance().getTabsManager(filter!!).filePath = mDocPath
                    val index =
                        App.instance().getMultiTabView(filter!!).historyFileNames.indexOf(path)
                    App.instance().getMultiTabView(filter!!).historyFileNames[index] = mDocPath
                    path = mDocPath
                    App.instance().getMultiTabView(filter!!).refreshTopBar(mDocPath)
                } else {
                    if (fragment != null && fragment !== this@PDFReaderFragment) {
                        val mFragmentManager =
                            App.instance().getTabsManager(filter!!).fragmentManager
                        val fragmentTransaction = mFragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(fragment).commitAllowingStateLoss()
                        App.instance().getTabsManager(filter!!)
                            .addFragment(path!!, this@PDFReaderFragment)
                    }
                    App.instance().getMultiTabView(filter!!).refreshTopBar(path)
                }
                val h = mUiExtensionsManager!!.mainFrame.topToolbar.contentView.height
                val params =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2 * h / 3)
                params.topMargin = -10
                val parent = App.instance().getMultiTabView(filter!!).tabView!!.parent as ViewGroup?
                parent?.removeView(App.instance().getMultiTabView(filter!!).tabView)
                mUiExtensionsManager!!.mainFrame.topActionView.addView(
                    App.instance().getMultiTabView(filter!!).tabView, params
                )
            }
        }

        override fun onDocWillClose(document: PDFDoc) {}
        override fun onDocClosed(document: PDFDoc?, errCode: Int) {
            dismissProgressDlg()
            if (isSaveDocInCurPath) {
                val file = File(currentFileCachePath!!)
                val docFile = File(mDocPath!!)
                val context = App.instance().applicationContext
                if (file.exists()) {
                    docFile.delete()
                    if (!file.renameTo(docFile)) UIToast.getInstance(context)
                        .show(AppResource.getString(context, R.string.fx_save_file_failed))
                } else {
                    UIToast.getInstance(context)
                        .show(AppResource.getString(context, R.string.fx_save_file_failed))
                }
            }
            if (errCode == Constants.e_ErrSuccess && isSaveDocInCurPath) {
                updateThumbnail(mSavePath)
            }
            if (mFragmentEvent != null) {
                mFragmentEvent!!.onRemove()
            }
        }

        override fun onDocWillSave(document: PDFDoc) {}
        override fun onDocSaved(document: PDFDoc, errCode: Int) {
            dismissProgressDlg()
            if (errCode == Constants.e_ErrSuccess) {
                val activity: Activity? = activity
                if (activity != null) {
                    val storageManager = AppStorageManager.getInstance(activity)
                    if (!isSaveDocInCurPath) {
                        if (AppFileUtil.needScopedStorageAdaptation()) {
                            storageManager.copyDocument(currentFileCachePath, mSavePath, true)
                        }
                        updateThumbnail(mSavePath)
                    } else if (path != null && path != mSavePath && currentFileCachePath != null) {
                        storageManager.copyDocument(currentFileCachePath, path, false)
                    }
                }
            }
            if (isCloseDocAfterSaving) {
                closeAndSaveDoc(mFragmentEvent)
            }
        }
    }
    private var mBackEventListener: BackEventListener? = BackEventListener {
        if (App.instance().isMultiTab) {
            if (mUiExtensionsManager!!.state == ReadStateConfig.STATE_COMPARE) {
                val tabView = App.instance().getMultiTabView(filter!!)
                tabView.removeTab(path)
                return@BackEventListener true
            }
            if (mUiExtensionsManager!!.currentToolHandler != null) mUiExtensionsManager!!.currentToolHandler =
                null
            val fragmentManager = App.instance().getTabsManager(
                filter!!
            ).fragmentManager
            val fragmentTransaction = fragmentManager!!.beginTransaction()
            val currentFrag = App.instance().getTabsManager(filter!!).currentFragment
            fragmentTransaction.hide(currentFrag!!).commit()
            if (activity != null) (activity as MainActivity?)!!.changeReaderState(MainActivity.READER_STATE_HOME)
            return@BackEventListener true
        }
        false
    }

    fun doClose(callback: IFragmentEvent?) {
        if (pdfViewCtrl == null) return
        if (pdfViewCtrl!!.isDynamicXFA) {
            val dynamicXFAModule =
                mUiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_DYNAMICXFA) as DynamicXFAModule
            if (dynamicXFAModule != null && dynamicXFAModule.currentXFAWidget != null) {
                dynamicXFAModule.currentXFAWidget = null
            }
        }
        val context = App.instance().applicationContext
        if (pdfViewCtrl!!.doc == null || !mUiExtensionsManager!!.documentManager.isDocModified) {
            mProgressMsg = AppResource.getString(context, R.string.fx_string_closing)
            closeAndSaveDoc(callback)
            return
        }
        if (mUiExtensionsManager!!.isAutoSaveDoc) {
            saveToOriginalFile(context!!, callback)
            return
        }
        val builder = AlertDialog.Builder(requireActivity())
        val items = arrayOf(
            AppResource.getString(context, R.string.rv_back_save_to_original_file),
            AppResource.getString(context, R.string.rv_back_save_to_new_file),
            AppResource.getString(context, R.string.rv_back_discard_modify)
        )
        builder.setItems(items, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                when (which) {
                    0 -> saveToOriginalFile(context!!, callback)
                    1 -> {
                        mProgressMsg = AppResource.getString(context, R.string.fx_string_saving)
                        onSaveAsClicked()
                    }
                    2 -> {
                        mProgressMsg = AppResource.getString(context, R.string.fx_string_closing)
                        closeAndSaveDoc(callback)
                    }
                    else -> {
                    }
                }
                dialog.dismiss()
                mSaveAlertDlg = null
            }

            fun showInputFileNameDialog(fileFolder: String) {
                val newFilePath = fileFolder + "/" + AppFileUtil.getFileName(pdfViewCtrl!!.filePath)
                val filePath = AppFileUtil.getFileDuplicateName(newFilePath)
                val fileName = AppFileUtil.getFileNameWithoutExt(filePath)
                val rmDialog = UITextEditDialog(activity)
                rmDialog.setPattern("[/\\:*?<>|\"\n\t]")
                rmDialog.setTitle(AppResource.getString(context, R.string.fx_string_saveas))
                rmDialog.promptTextView.visibility = View.GONE
                rmDialog.inputEditText.setText(fileName)
                rmDialog.inputEditText.selectAll()
                rmDialog.show()
                AppUtil.showSoftInput(rmDialog.inputEditText)
                rmDialog.okButton.setOnClickListener { v ->
                    rmDialog.dismiss()
                    val inputName = rmDialog.inputEditText.text.toString()
                    var newPath: String? = "$fileFolder/$inputName"
                    newPath += ".pdf"
                    if (AppFileUtil.existsFileOrDocument(v.context, newPath)) {
                        showAskReplaceDialog(fileFolder, newPath)
                    } else {
                        isCloseDocAfterSaving = true
                        mSavePath = newPath
                        if (AppFileUtil.needScopedStorageAdaptation()) {
                            newPath = getCacheFile(newPath)
                        }
                        pdfViewCtrl!!.saveDoc(newPath, mUiExtensionsManager!!.saveDocFlag)
                        mFragmentEvent = callback
                        showProgressDialog()
                    }
                }
            }

            fun showAskReplaceDialog(fileFolder: String, newPath: String?) {
                val rmDialog = UITextEditDialog(activity, UIDialog.NO_INPUT)
                rmDialog.setTitle(AppResource.getString(context, R.string.fx_string_saveas))
                rmDialog.promptTextView.text =
                    AppResource.getString(context, R.string.fx_string_filereplace_warning)
                rmDialog.show()
                rmDialog.okButton.setOnClickListener {
                    rmDialog.dismiss()
                    isCloseDocAfterSaving = true
                    mSavePath = newPath
                    if (newPath.equals(pdfViewCtrl!!.filePath, ignoreCase = true)) {
                        isSaveDocInCurPath = true
                        pdfViewCtrl!!.saveDoc(
                            getCacheFile(null),
                            mUiExtensionsManager!!.saveDocFlag
                        )
                    } else {
                        isSaveDocInCurPath = false
                        var savePath = newPath
                        if (AppFileUtil.needScopedStorageAdaptation()) {
                            savePath = getCacheFile(newPath)
                        }
                        pdfViewCtrl!!.saveDoc(savePath, mUiExtensionsManager!!.saveDocFlag)
                    }
                    mFragmentEvent = callback
                    showProgressDialog()
                }
                rmDialog.cancelButton.setOnClickListener {
                    rmDialog.dismiss()
                    showInputFileNameDialog(fileFolder)
                }
            }

            fun onSaveAsClicked() {
                mFolderSelectDialog = UIFolderSelectDialog(activity)
                mFolderSelectDialog!!.setFileFilter { pathname ->
                    !(pathname.isHidden || !AppFileUtil.canRead(
                        pathname
                    ))
                }
                mFolderSelectDialog!!.setListener(object : MatchDialog.DialogListener {
                    override fun onResult(btType: Long) {}
                    override fun onBackClick() {}
                    override fun onTitleRightButtonClick() {
                        val fileFolder = mFolderSelectDialog!!.currentPath
                        showInputFileNameDialog(fileFolder)
                        mFolderSelectDialog!!.dismiss()
                    }
                })
                mFolderSelectDialog!!.showDialog()
            }
        })
        mSaveAlertDlg = builder.create()
        mSaveAlertDlg!!.setCanceledOnTouchOutside(true)
        mSaveAlertDlg!!.show()
    }

    private fun saveToOriginalFile(context: Context, callback: IFragmentEvent?) {
        isCloseDocAfterSaving = true
        val userSavePath = mUiExtensionsManager!!.savePath
        if (userSavePath != null && userSavePath.length > 0 && !userSavePath.equals(
                mDocPath,
                ignoreCase = true
            )
        ) {
            val userSaveFile = File(userSavePath)
            val defaultSaveFile = File(mDocPath)
            var savePath = userSavePath
            if (userSaveFile.parent.equals(defaultSaveFile.parent, ignoreCase = true)) {
                isSaveDocInCurPath = true
                mSavePath = userSavePath
                if (AppFileUtil.needScopedStorageAdaptation()) {
                    savePath = getCacheFile(null)
                }
            } else {
                isSaveDocInCurPath = false
                if (AppFileUtil.needScopedStorageAdaptation()) {
                    savePath = getCacheFile(mSavePath)
                }
            }
            pdfViewCtrl!!.saveDoc(savePath, mUiExtensionsManager!!.saveDocFlag)
        } else {
            isSaveDocInCurPath = true
            pdfViewCtrl!!.saveDoc(getCacheFile(null), mUiExtensionsManager!!.saveDocFlag)
        }
        isSaveDocInCurPath = true
        mProgressMsg = context.getString(R.string.fx_string_saving)
        mFragmentEvent = callback
        showProgressDialog()
    }

    private fun getCacheFile(path: String?): String? {
        mSavePath = path ?: mDocPath
        var file = File(mDocPath)
        val dir = file.parent + "/"
        while (file.exists()) {
            currentFileCachePath = dir + AppDmUtil.randomUUID(null) + ".pdf"
            file = File(currentFileCachePath)
        }
        return currentFileCachePath
    }

    private fun showProgressDialog() {
        if (mProgressDlg == null && activity != null) {
            mProgressDlg = ProgressDialog(activity)
            mProgressDlg!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            mProgressDlg!!.setCancelable(false)
            mProgressDlg!!.isIndeterminate = false
            mProgressDlg!!.setMessage(mProgressMsg)
            AppDialogManager.getInstance().showAllowManager(mProgressDlg, null)
        }
    }

    private fun dismissProgressDlg() {
        if (mProgressDlg != null && mProgressDlg!!.isShowing) {
            AppDialogManager.getInstance().dismiss(mProgressDlg)
            mProgressDlg = null
        }
    }

    private fun closeAndSaveDoc(callback: IFragmentEvent?) {
        showProgressDialog()
        pdfViewCtrl!!.closeDoc()
        mFragmentEvent = callback
    }

    private fun updateThumbnail(path: String?) {
        val module = mUiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_LOCAL) as LocalModule
        if (module != null && path != null) {
            module.updateThumbnail(path)
        }
    }

    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mUiExtensionsManager!!.handleActivityResult(activity, requestCode, resultCode, data)
    }

    override fun handleRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        mUiExtensionsManager!!.handleRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mFolderSelectDialog != null && mFolderSelectDialog!!.isShowing) {
            mFolderSelectDialog!!.resetWH()
            mFolderSelectDialog!!.showDialog()
        }
    }

    companion object {
        private val TAG = PDFReaderFragment::class.java.simpleName
    }
}