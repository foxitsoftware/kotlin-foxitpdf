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
package com.foxit.pdf.pdfviewer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

import com.foxit.sdk.PDFViewCtrl
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Library
import com.foxit.uiextensions.Module
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.annots.note.NoteModule
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyModule
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutModule
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineModule
import com.foxit.uiextensions.modules.OutlineModule
import com.foxit.uiextensions.modules.PageNavigationModule
import com.foxit.uiextensions.modules.SearchModule
import com.foxit.uiextensions.modules.doc.docinfo.DocInfoModule
import com.foxit.uiextensions.modules.panel.annot.AnnotPanelModule
import com.foxit.uiextensions.modules.thumbnail.ThumbnailModule
import com.foxit.uiextensions.utils.AppUtil.showSystemUI
import com.foxit.uiextensions.utils.UIToast

class MainActivity : FragmentActivity() {

    private var pdfViewCtrl: PDFViewCtrl? = null
    private var uiExtensionsManager: UIExtensionsManager? = null
    private var searchModule: SearchModule? = null
    private var docInfoModule: DocInfoModule? = null
    private var noteModule: NoteModule? = null
    private var highlightModule: HighlightModule? = null
    private var underlineModule: UnderlineModule? = null
    private var strikeoutModule: StrikeoutModule? = null
    private var squigglyModule: SquigglyModule? = null

    private var annotPanelModule: AnnotPanelModule? = null
    private var outlineModule: OutlineModule? = null
    private var thumbnailModule: ThumbnailModule? = null

    private var pageNavigationModule: PageNavigationModule? = null

    private var isUnlock = false
    private var mContext: Context? = null
    private var mActionMode: ActionMode? = null

    private val storageDirectory: String?
        get() {
            var path: String? = null
            val sdExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
            if (sdExist) {
                path = Environment.getExternalStorageDirectory().absolutePath + "/"
            }
            return path
        }

    private val mActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.main, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if (!isUnlock) {
                UIToast.getInstance(applicationContext).show(getString(R.string.fx_unlock_library_failed))
                return false
            }
            showSystemUI()
            uiExtensionsManager!!.triggerDismissMenuEvent()
            val itemId = item.itemId
            if (itemId == R.id.Note || itemId == R.id.Highlight
                    || itemId == R.id.Squiggly || itemId == R.id.Underline
                    || itemId == R.id.StrikeOut) {
                if (!uiExtensionsManager!!.canAddAnnot()) {
                    UIToast.getInstance(applicationContext).show(getString(R.string.fx_the_document_cannot_modify))
                    return false
                }
            }

            when (itemId) {
                R.id.Outline -> {
                    if (outlineModule != null)
                        outlineModule!!.show()
                }
                R.id.ChangeLayout -> {
                    pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                    pdfViewCtrl!!.isContinuous = !pdfViewCtrl!!.isContinuous
                }
                R.id.Search -> {
                    if (searchModule == null) {

                        searchModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SEARCH) as SearchModule
                        if (searchModule == null) {
                            searchModule = SearchModule(mContext!!, uiExtensionsManager!!.rootView, pdfViewCtrl!!, uiExtensionsManager)
                            searchModule!!.loadModule()
                        }
                    }
                    val searchView = searchModule!!.searchView
                    searchView.show()
                }
                R.id.Note -> {
                    if (noteModule == null) {
                        noteModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_NOTE) as NoteModule
                    }
                    uiExtensionsManager!!.currentToolHandler = noteModule!!.toolHandler
                }
                R.id.DocInfo -> {
                    if (docInfoModule == null) {
                        docInfoModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_DOCINFO) as DocInfoModule
                    }

                    val docInfoView = docInfoModule!!.view
                    docInfoView?.show()
                }
                R.id.Highlight -> {
                    if (highlightModule == null)
                        highlightModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_HIGHLIGHT) as HighlightModule
                    uiExtensionsManager!!.currentToolHandler = highlightModule!!.toolHandler
                }
                R.id.Underline -> {
                    if (underlineModule == null) {
                        underlineModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_UNDERLINE) as UnderlineModule
                    }
                    uiExtensionsManager!!.currentToolHandler = underlineModule!!.toolHandler
                }
                R.id.StrikeOut -> {
                    if (strikeoutModule == null) {
                        strikeoutModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_STRIKEOUT) as StrikeoutModule
                    }
                    uiExtensionsManager!!.currentToolHandler = strikeoutModule!!.toolHandler
                }
                R.id.Squiggly -> {
                    if (squigglyModule == null) {
                        squigglyModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SQUIGGLY) as SquigglyModule
                    }
                    uiExtensionsManager!!.currentToolHandler = squigglyModule!!.toolHandler
                }
                R.id.Annotations -> {
                    if (annotPanelModule != null) {
                        annotPanelModule!!.show()
                    }
                }
                R.id.Thumbnail -> {
                    if (thumbnailModule != null) {
                        thumbnailModule!!.show()
                    }
                }
            }
            mode.finish()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val errorCode = Library.initialize(sn, key)
        isUnlock = true

        if (errorCode != Constants.e_ErrSuccess) {
            isUnlock = false

            val errorMsg = if (errorCode == Constants.e_ErrInvalidLicense) getString(R.string.fx_the_license_is_invalid) else getString(R.string.fx_failed_to_initialize_the_library)
            UIToast.getInstance(applicationContext).show(errorMsg)
            return
        }

        pdfViewCtrl = PDFViewCtrl(this.applicationContext)
        pdfViewCtrl!!.attachedActivity = this
        pdfViewCtrl!!.registerDoubleTapEventListener(object : PDFViewCtrl.IDoubleTapEventListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (pageNavigationModule == null)
                    pageNavigationModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_PAGENAV) as PageNavigationModule?

                if (mActionMode == null) {
                    mActionMode = (mContext as Activity).startActionMode(mActionModeCallback)
                    pageNavigationModule!!.changPageNumberState(true)
                } else {
                    mActionMode!!.finish()
                    mActionMode = null
                    pageNavigationModule!!.changPageNumberState(false)
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return false
            }
        })

        uiExtensionsManager = UIExtensionsManager(this.applicationContext, pdfViewCtrl!!)
        uiExtensionsManager!!.enableBottomToolbar(false)
        uiExtensionsManager!!.enableTopToolbar(false)
        uiExtensionsManager!!.attachedActivity = this
        uiExtensionsManager!!.onCreate(this, pdfViewCtrl, savedInstanceState)
        pdfViewCtrl!!.uiExtensionsManager = uiExtensionsManager!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            } else {
                openDocument()
            }
        } else {
            openDocument()
        }

        outlineModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_OUTLINE) as OutlineModule
        if (outlineModule == null) {
            outlineModule = OutlineModule(this, uiExtensionsManager!!.rootView, pdfViewCtrl, uiExtensionsManager)
            outlineModule!!.loadModule()
        }
        annotPanelModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_ANNOTPANEL) as AnnotPanelModule
        if (annotPanelModule == null) {
            annotPanelModule = AnnotPanelModule(this.applicationContext, pdfViewCtrl!!, uiExtensionsManager)
            annotPanelModule!!.loadModule()
        }
        thumbnailModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_THUMBNAIL) as ThumbnailModule
        if (thumbnailModule == null) {
            thumbnailModule = ThumbnailModule(this.applicationContext, pdfViewCtrl, uiExtensionsManager)
            thumbnailModule!!.loadModule()
        }

        if (Build.VERSION.SDK_INT >= 24) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }

        setContentView(uiExtensionsManager!!.contentView)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openDocument()
        } else {
            UIToast.getInstance(applicationContext).show(getString(R.string.fx_permission_denied))
            finish()
        }
    }

    private fun openDocument() {
        // Note: Here, filePath will be set with the total path of file.
        val sdcardPath = storageDirectory
        val filePath = sdcardPath!! + "FoxitSDK/Sample.pdf"
        uiExtensionsManager!!.openDocument(filePath, null)
    }

    public override fun onStart() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onStart(this)
        }
        super.onStart()
    }

    public override fun onStop() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onStop(this)
        }
        super.onStop()
    }

    public override fun onPause() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onPause(this)
        }
        super.onPause()
    }

    public override fun onResume() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onResume(this)
        }
        super.onResume()
    }

    override fun onDestroy() {
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onDestroy(this)
        }
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onConfigurationChanged(this, newConfig)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (uiExtensionsManager != null && uiExtensionsManager!!.onKeyDown(this, keyCode, event)) true else super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pdfViewCtrl!!.handleActivityResult(requestCode, resultCode, data);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    fun showSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }
    companion object {

        private val sn = "XmE4UnLG/IaLL6QwTqHtLVMGcBiJTeRm4fPIWPmzcWt3+hhXfbW5vg=="
        private val key = "ezJvjl3GtGh397voL2Xkb3kutv9oSAOWRQ7cbJfN3y7VglVhz0T2xqzpSWcuWyR+pNaUYD3bs8Es82uVUUh2hXdnXA5MhQD0zlTI5AEgPxpHT7x+TEiQKeeTPfdOfea8Sw9sPPuasmPH8XtcKkdbgcgQQGgcz3CCZIAQdC9YKlNdKtIUTHp9VOeRKXFHWulsn3GyYQ+b2c8V1BjQJHFR+AXpQj1sxykcFQrbCrGGzZtupm2Sn9uxotXtJIpUOokx1y+XZeJ1ZeY72C/3/LlJvZ7EVtyx6agWuyjOqOLTah+Y0KC0quoBrJbTgvebddnygxHXwbBI8oyCNSaTzI4f41KxFnYA0YqfvLOAVOra434F4xQSXeGT2EPwRU279QBDFDxrDa8X0QD8HJ+wUN/+/+OMTZ7GXVeBZNFbUFTt4H5P7cTvdBxTxFCXJdaO54QvW5PLQ6tAUIh3ojOxalTNsBSM8KzTdHX774Q+uD6HWysE2rP0WW3qkjBDXRSniT2H5N32CcUXKa/M/avpHV6AXAOW7hi7fFQWRuV8Bz74wYU/oACWx1TteyVd0ukcd/LnTqoSPDILBftp35KdMYIJmqiCeFSqGdKfoQ6SRe1B9PA77ZFqzQWuzWVtrn9owEnb6Z8yEFHxmHucggf8veSsCWOY6JBvUpHhR/QabOI2GJ6xfb4NFojBdrSZwQrUVvwdb3ThYyx4+Kv2c0SpyQiv9dp1i+bqa//g5xX3p+wsyWKr3k9HXoGSicgDv9ShIgv8t6ekr264EeLvp0muL42ulEsRATz+8I2/rZcfCnLNv4lIgvgLTIQYalV3BIumgRvAoiXWCOBFpjUhpg8nL5IAZdjI5cWkn/x86+380oc8p86IKCCvIx7xBEesYxBg/7EO0123iDbDONxxI2MSM0AJ7Y/HgNiORyPNBWfcgV78pH8A6/dFwUq/Lz7igqpLHy2npaPCVg9+SSciQn7NtfULusJXRpJ9uqzHU2und1t3vZgaEm6o7yeaw/s5NwGWg8J92P0kOV/JyZHhsI5T69Ht0wyPjKLDDSAwOEfnKaHvSx7gx+X42uUGg8e5MLu6BWlCJk2qru5+V4pt7SvYLKP3JbG5XSY9PKjt8wilb6qFHCnRTaI4bZNT7uPI35/rVu0ts7AWlNCbA/3lVK+qNcF8LvUHQlP60QNOPVO6jzsR0g5bGdUTmec2pdW66G0CffmcvC57K9RldfHl1w4bo8sxNFhmi/SpnDDydG/XRzI6D/LE81pZ"


        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
