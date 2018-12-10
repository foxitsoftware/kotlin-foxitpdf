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
                UIToast.getInstance(applicationContext).show("Unlock Library failed,the menu item is unavailable!")
                return false
            }

            val itemId = item.itemId
            if (itemId == R.id.Note || itemId == R.id.Highlight
                    || itemId == R.id.Squiggly || itemId == R.id.Underline
                    || itemId == R.id.StrikeOut) {
                if (!uiExtensionsManager!!.canAddAnnot()) {
                    UIToast.getInstance(applicationContext).show("The current document is protected,You can't modify it")
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

            val errorMsg = if (errorCode == Constants.e_ErrInvalidLicense) "The license is invalid!" else "Failed to initialize the library!"
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
            UIToast.getInstance(applicationContext).show(getString(R.string.permission_denied))
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

        private val sn = "DW6QUNWzT4IF0JluGTcsD/OOs12XAfWdNkVPejrfj72Mi6P3xjaElw=="
        private val key = "ezJvj18mvB539PsXZqX8Iklssh9qvOZTXwsIO/MBC0bmgJ1qz4F0lwleIbZghw4SQD11gx1jqOJvZixkxBpuX+IYO08ZheJYkMQumnRHZ+eSysccHXuESTewCCK1K/MgHY/k54IqDvVbkdKIJu9QmzmxEJhP8zrulCE5v/XtHRKbNVNsvDpNop2HE1XwRtNon3s6YQ+j+c8BACgApAsRalO9SQP2GlkkBEAYqvY2JrrnRhfeZngd25kw9CZGd9p3QToervqkj64UV/3I4sqU/0arSotj32QPFCYAt9roaKCzAYoTeaE/l7zlnpd3dcZwf7NiYwSSrQ2LNpD+r2lHGi8WGr2hO7hwWtX9vdklMHOf/YFo+/05XxoVlnVAtYXRxx7S3MVeSEnhswujY+AswVbBgKGJRGRWzKnv7py3803X3DH5PGqRRayjTceiRq0ddSf7GiNtRQittqcRQSBYet43Rvyca+NyBxa5DddneG1VbBaVtM12C2Xv02/8HNLAf5AF3Vua/6O1gRi2ofIm0dqpk59lj19OiyqBIV6Ma/HZ93SwKLycOxDOHIcn2cVM1UtY+pF7ptUGz4Mq2V9YBaB2wxFB7I3mUEBjGOx1Y1ZAyvpWclWebMpIUct3ku4PmLsuSeX1uAd4iGOggcIFXPiUdYu7RytAbIlnWLtd5FZnwAfsyN/norSSmAtdZMHEv0xmh865YCFDkyo7Lw5ilhUICpZV2qJqqg6n757PdZcyO+M57r5bdcMto40q3M+lmiqOV8Wj/ui9v1h+UHOKQBvCti5TYvI5FWN/biCleETDEXUV1aMvVm/Zcyuu4njBWgL+0FMzCx72Lv0oIHsSl3THc2TS95YL9/3QpYQTAue6VpXdEAN1s3u4rzQVJCmT2QPK4FP/pznBYEP289VheUd1I521v93LZf9TWFDeIUIjE83bEGdtlJRdbqPR2fXccdtLWUeG+Ky97MqncQHy4REqjmBqNxjlo/gvEshBV7VOntNcUmpCLHKyZF+IupSlQ5zO0lJ9RaPShX+VkaI9rx17Oif8q0qvz29nA9s5XyBe87VjQm6BjA7b5hZnixsuZlv+R7ZhyWU5jaTh1BuLbz3zIDAO90rK9qnMP2hm5AFRmy962CqDi/vW0nyQISpgMlSJsGkPUxpg5TuhiGe13TEHMQyHVdBodOcMUBaO1sk4mdeYk7qUm78ek2VL4PhgZHZO3KE+B1ASiVG4iqGAbYiM"


        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}
