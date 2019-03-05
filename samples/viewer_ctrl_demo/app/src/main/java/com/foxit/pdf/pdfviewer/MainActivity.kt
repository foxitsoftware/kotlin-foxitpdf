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
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager

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
import com.foxit.uiextensions.modules.DocInfoModule
import com.foxit.uiextensions.modules.DocInfoView
import com.foxit.uiextensions.modules.OutlineModule
import com.foxit.uiextensions.modules.SearchModule
import com.foxit.uiextensions.modules.SearchView
import com.foxit.uiextensions.modules.connectpdf.account.AccountModule
import com.foxit.uiextensions.modules.panel.annot.AnnotPanelModule
import com.foxit.uiextensions.modules.thumbnail.ThumbnailModule
import com.foxit.uiextensions.utils.UIToast

class MainActivity : FragmentActivity() {

    private var pdfViewCtrl: PDFViewCtrl? = null
    private var uiExtensionsManager: UIExtensionsManager? = null
    private var layoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
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
                    if (layoutMode == PDFViewCtrl.PAGELAYOUTMODE_SINGLE) {
                        pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_CONTINUOUS
                        layoutMode = PDFViewCtrl.PAGELAYOUTMODE_CONTINUOUS
                    } else {
                        pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                        layoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                    }
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
                if (mActionMode == null) {
                    mActionMode = (mContext as Activity).startActionMode(mActionModeCallback)
                } else {
                    mActionMode!!.finish()
                    mActionMode = null
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

        AccountModule.getInstance().onCreate(this, savedInstanceState)

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
        AccountModule.getInstance().onDestroy(this)
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

    companion object {

        private val sn = "qgVK5jS3KYEa5d/7a0aCJpZHuXc1ite9LBERoywRU5LQyMZNcXzL2A=="
        private val key = "ezJvj18mvB539PsXZqXcIklssh9qveZTXwsIO7MBC0bmgMUWD2d+2vk18zJ02pl+5NZ0I/5z+SGPshNpVNR/9bK/6rzAqEiIcptvrDVZFBf8BtPD3DiHacfAC+00W06fFZsc8/upASquVGTB2AE5+vQ7QLOyoYjnLOnKIXprWJ13ieiZTd+dgwEc7qx4AwPm3KohdnBIau1l7ezggBr4ToFyIph4b9kc//TTKBZVmO/naXjUkOXHux8hw1MxsiY3Y3ITf/U41yP50zPDqS71fiuhceUd0uRV1MJ8BE5Fa/DNw1V9EvOvy9nJsml14OAajxnzq7EVB3OkTaZv7hU392bHZJFQm7nrgOFlC0GL9Kpt6LvJJk0nOKe9KIW0OVZuPd5TfOoNTIiPV5RzohOwxBWs0MHhTox48O8rSnETDmyidPUpQpK04i7fMZxIn56MCD5dPkjtS+lwv1aYcVaZCB5eIpcDyvnIZZnvIrFu+DcYhSw6Tj2VfLTwwLlJ6/ImQZAHxVua/6O7hRi2ofImEVOkqhkkj1hOiyIBGW689ZGaKa2MLTbNVmj+EIkmxyXPrSfvkq6LMZRLqLasmFDc5OB+RCldbgzmIFifk5yxTC81HFNBHKYq1l2LcSUTDS5gZGBwv89QNVpjnYK1qzMKxdW2DJb8vsXE+7uXq4vpzfOofjtAp7V4QlLTXXLIrB2V5exBHWxAU9BdxSTviIsjthEICncV0mbrigYj7r7b7oIRlOYBJr1ZRMstI0or0Esl2jbC2da/gFsbiyWy5iNzcYwVevRCS4eL5DTGkSyaPs3sjOfGb4WBxZ03f7BL+S9eiK1Dv14ytEWa9Z/6EApXCsHgCJYzJW13EvkA9bziaden0OzIPBdfk7i9mdklH80BPcI0J5sC1lB2XCRGqmQr8QkCL5/JZY+xoGAh10GHafhXnhpSHZ9qaBSZzhTArIu8h6PEd2UhFSLYkfoXto8/N/6r8/YLBsOqVhMrq/5WoD6cJQQdGm1KLbqQvRnSui7VafTGZ6QbwkcVMzJV7w9hVR/q7hqRo2ZPi1olraM8kVnD+SSpeK6qkD2sgmoTfYzqAEz2AWq8XlNSEEoGTyJzgGzEPgfmpMvHmpkhtGONOKCSA/R36VuOR3GG9psBS4god2Q3gN4+v0guQV+BqFyu9EcsxnV7bEJ1Slr9o+TtUvs6PiP7Reg7rekObp485zQyuRmOZfIbycg7kBC575myg4h1"


        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
