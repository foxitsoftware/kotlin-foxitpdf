/**
 * Copyright (C) 2003-2018, Foxit Software Inc..
 * All Rights Reserved.
 *
 *
 * http://www.foxitsoftware.com
 *
 *
 * The following code is copyrighted and is the proprietary of Foxit Software Inc.. It is not allowed to
 * distribute any parts of Foxit Mobile PDF SDK to third party or public without permission unless an agreement
 * is signed between Foxit Software Inc. and customers to explicitly grant customers permissions.
 * Review legal.txt for additional license and legal information.
 */
package com.foxit.pdf.pdfviewer

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.view.ActionMode
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout

import com.foxit.sdk.PDFViewCtrl
import com.foxit.sdk.common.Library
import com.foxit.sdk.common.PDFError
import com.foxit.sdk.common.PDFException
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.uiextensions.Module
import com.foxit.uiextensions.UIExtensionsManager
import com.foxit.uiextensions.annots.note.NoteModule
import com.foxit.uiextensions.annots.textmarkup.highlight.HighlightModule
import com.foxit.uiextensions.annots.textmarkup.squiggly.SquigglyModule
import com.foxit.uiextensions.annots.textmarkup.strikeout.StrikeoutModule
import com.foxit.uiextensions.annots.textmarkup.underline.UnderlineModule
import com.foxit.uiextensions.controls.dialog.UITextEditDialog
import com.foxit.uiextensions.modules.DocInfoModule
import com.foxit.uiextensions.modules.DocInfoView
import com.foxit.uiextensions.modules.OutlineModule
import com.foxit.uiextensions.modules.SearchModule
import com.foxit.uiextensions.modules.SearchView
import com.foxit.uiextensions.modules.panel.annot.AnnotPanelModule
import com.foxit.uiextensions.modules.signature.SignatureToolHandler
import com.foxit.uiextensions.modules.thumbnail.ThumbnailModule
import com.foxit.uiextensions.utils.UIToast

import java.util.Timer
import java.util.TimerTask

class MainActivity : FragmentActivity() {

    private var pdfViewCtrl: PDFViewCtrl? = null
    private var parent: RelativeLayout? = null
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
    private var mPasswordError = false
    private var mContext: Context? = null
    private var mActionMode: ActionMode? = null
    internal var mPath = String()

    private val storageDirectory: String?
        get() {
            var path: String? = null
            val sdExist = Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
            if (sdExist) {
                path = Environment.getExternalStorageDirectory().absolutePath + "/"
            }
            return path
        }


    internal var docListener: PDFViewCtrl.IDocEventListener = object : PDFViewCtrl.IDocEventListener {
        override fun onDocWillOpen() {}

        override fun onDocOpened(pdfDoc: PDFDoc, errCode: Int) {
            //switch case require constant value
            if (errCode == PDFError.NO_ERROR.code) {
                mPasswordError = false
            } else if (errCode == PDFError.PASSWORD_INVALID.code) {
                var tips: String? = null
                if (mPasswordError) {
                    tips = "The password is incorrect, please try again"
                } else {
                    tips = "This file is password protected, please enter password below"
                }
                val uiTextEditDialog = UITextEditDialog(this@MainActivity)
                uiTextEditDialog.dialog.setCanceledOnTouchOutside(false)
                uiTextEditDialog.inputEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                uiTextEditDialog.setTitle("Please Input password")
                uiTextEditDialog.promptTextView.text = tips
                uiTextEditDialog.show()
                uiTextEditDialog.okButton.setOnClickListener { v ->
                    uiTextEditDialog.dismiss()
                    val inputManager = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(v.windowToken, 0)
                    val pw = uiTextEditDialog.inputEditText.text.toString()
                    pdfViewCtrl!!.openDoc(mPath, pw.toByteArray())
                    mPasswordError = true
                }

                uiTextEditDialog.cancelButton.setOnClickListener { v ->
                    uiTextEditDialog.dismiss()
                    val inputManager = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(v.windowToken, 0)
                    onExit()
                }

                uiTextEditDialog.dialog.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        uiTextEditDialog.dialog.cancel()
                        onExit()
                        return@OnKeyListener true
                    }
                    false
                })
                uiTextEditDialog.show()
            } else {
                showDialog(PDFException.getErrorMessage(errCode))
            }

        }

        override fun onDocWillClose(pdfDoc: PDFDoc?) {}

        override fun onDocClosed(pdfDoc: PDFDoc?, i: Int) {
            try {
                Library.release()
            } catch (e: PDFException) {
                e.printStackTrace()
            }

        }

        override fun onDocWillSave(pdfDoc: PDFDoc?) {}

        override fun onDocSaved(pdfDoc: PDFDoc?, i: Int) {}

        override fun onDocModified(pdfDoc: PDFDoc?) {

        }
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
                if (false == uiExtensionsManager!!.canAddAnnot()) {
                    UIToast.getInstance(applicationContext).show("The current document is protected,You can't modify it")
                    return false
                }
            }

            if (itemId == R.id.Outline) {
                if (outlineModule != null)
                    outlineModule!!.show()
            } else if (itemId == R.id.ChangeLayout) {
                if (layoutMode == PDFViewCtrl.PAGELAYOUTMODE_SINGLE) {
                    pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_CONTINUOUS
                    layoutMode = PDFViewCtrl.PAGELAYOUTMODE_CONTINUOUS
                } else {
                    pdfViewCtrl!!.pageLayoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                    layoutMode = PDFViewCtrl.PAGELAYOUTMODE_SINGLE
                }
            } else if (itemId == R.id.Search) {
                if (searchModule == null) {

                    searchModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SEARCH) as SearchModule
                    if (searchModule == null) {
                        searchModule = SearchModule(mContext!!, parent!!, pdfViewCtrl!!, uiExtensionsManager)
                        searchModule!!.loadModule()
                    }
                }
                val searchView = searchModule!!.searchView
                searchView.show()
            } else if (itemId == R.id.Note) {
                if (noteModule == null) {
                    noteModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_NOTE) as NoteModule
                }
                uiExtensionsManager!!.currentToolHandler = noteModule!!.toolHandler
            } else if (itemId == R.id.DocInfo) {
                if (docInfoModule == null) {
                    docInfoModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_DOCINFO) as DocInfoModule
                }

                val docInfoView = docInfoModule!!.view
                docInfoView?.show()
            } else if (itemId == R.id.Highlight) {
                if (highlightModule == null)
                    highlightModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_HIGHLIGHT) as HighlightModule
                uiExtensionsManager!!.currentToolHandler = highlightModule!!.toolHandler
            } else if (itemId == R.id.Underline) {
                if (underlineModule == null) {
                    underlineModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_UNDERLINE) as UnderlineModule
                }
                uiExtensionsManager!!.currentToolHandler = underlineModule!!.toolHandler
            } else if (itemId == R.id.StrikeOut) {
                if (strikeoutModule == null) {
                    strikeoutModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_STRIKEOUT) as StrikeoutModule
                }
                uiExtensionsManager!!.currentToolHandler = strikeoutModule!!.toolHandler
            } else if (itemId == R.id.Squiggly) {
                if (squigglyModule == null) {
                    squigglyModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_SQUIGGLY) as SquigglyModule
                }
                uiExtensionsManager!!.currentToolHandler = squigglyModule!!.toolHandler
            } else if (itemId == R.id.Annotations) {
                if (annotPanelModule != null) {
                    annotPanelModule!!.show()
                }
            } else if (itemId == R.id.Thumbnail) {
                if (thumbnailModule != null) {
                    thumbnailModule!!.show()
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

        setContentView(R.layout.activity_main)

        try {
            Library.init(sn, key)
            isUnlock = true
        } catch (e: PDFException) {
            if (e.lastError == PDFError.LICENSE_INVALID.code) {
                UIToast.getInstance(applicationContext).show("The license is invalid!")
            } else {
                UIToast.getInstance(applicationContext).show("Failed to initialize the library!")
            }
            isUnlock = false
            return
        }

        pdfViewCtrl = findViewById(R.id.pdfviewer) as PDFViewCtrl
        parent = findViewById(R.id.rd_main_id) as RelativeLayout
        uiExtensionsManager = UIExtensionsManager(this.applicationContext, parent, pdfViewCtrl!!)
        uiExtensionsManager!!.attachedActivity = this
        pdfViewCtrl!!.uiExtensionsManager = uiExtensionsManager!!

        // Note: Here, filePath will be set with the total path of file.
        val sdcardPath = storageDirectory
        val filePath = sdcardPath!! + "FoxitSDK/Sample.pdf"

        mPath = filePath
        parent = findViewById(R.id.rd_main_id) as RelativeLayout

        outlineModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_OUTLINE) as OutlineModule
        if (outlineModule == null) {
            outlineModule = OutlineModule(this, parent, pdfViewCtrl, uiExtensionsManager)
            outlineModule!!.loadModule()
        }
        annotPanelModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_ANNOTPANEL) as AnnotPanelModule
        if (annotPanelModule == null) {
            annotPanelModule = AnnotPanelModule(mContext!!, pdfViewCtrl!!, uiExtensionsManager)
            annotPanelModule!!.loadModule()
        }
        thumbnailModule = uiExtensionsManager!!.getModuleByName(Module.MODULE_NAME_THUMBNAIL) as ThumbnailModule
        if (thumbnailModule == null) {
            thumbnailModule = ThumbnailModule(mContext, pdfViewCtrl, uiExtensionsManager)
            thumbnailModule!!.loadModule()
        }

        pdfViewCtrl!!.registerDocEventListener(docListener)
        pdfViewCtrl!!.openDoc(filePath, null)

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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val permission = ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            }
        }

        if (Build.VERSION.SDK_INT >= 24) {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showDialog(msg: String) {
        val uiTextEditDialog = UITextEditDialog(this)
        uiTextEditDialog.dialog.setCanceledOnTouchOutside(false)
        uiTextEditDialog.inputEditText.inputType = InputType.TYPE_CLASS_TEXT
        uiTextEditDialog.setTitle("Warning")
        uiTextEditDialog.inputEditText.visibility = View.GONE
        uiTextEditDialog.cancelButton.visibility = View.GONE
        uiTextEditDialog.promptTextView.text = "Faile to open $mPath.\n$msg"
        uiTextEditDialog.okButton.isEnabled = true
        uiTextEditDialog.okButton.setOnClickListener { onExit() }

        uiTextEditDialog.dialog.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                uiTextEditDialog.dialog.cancel()
                onExit()
                return@OnKeyListener true
            }
            false
        })

        uiTextEditDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (uiExtensionsManager != null) {
            uiExtensionsManager!!.onConfigurationChanged(newConfig)
        }
    }

    override fun onResume() {
        super.onResume()
        if (pdfViewCtrl != null)
            pdfViewCtrl!!.requestLayout()
    }

    private fun onExit() {
        if (isUnlock) {
            pdfViewCtrl!!.closeDoc()
        }
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((pdfViewCtrl!!.uiExtensionsManager as UIExtensionsManager).currentToolHandler is SignatureToolHandler) {
                (pdfViewCtrl!!.uiExtensionsManager as UIExtensionsManager).currentToolHandler = null
                pdfViewCtrl!!.invalidate()
                return true
            }
            var timer: Timer? = null
            if (isExit == false) {
                isExit = true
                UIToast.getInstance(this).show("Press again to exit.")
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        isExit = false
                    }
                }, 2000)

            } else {
                onExit()
            }
        }
        return false
    }

    companion object {

        private val sn = "cIwVF7AUSAakiEAihzb85vrmwVdOhUoXKg6IwosV7MwAw0FKEQyPAQ=="
        private val key = "ezKXjt8ntBh39DvoP0WQjY5U/oy/u0HLS16ctI9QPpxzh8j1xFBGxKzpATgyxl/xG5GuEi73n6bGooc+9epFT3VUozHpJ2k/5BYfyZ9qbUDfpKrcWFOUIWeQoraXjc6hyJSmte3+YYcqS8dP6frqA4bTWWpHAYBgmG1kY5d/7if3S5ahlGj7fflXmO7a+5SvLP15L6KuMY22qzWkhRhNbtmC1sAMpIL65j4yzEv+64rxyokDDDJeP1A6JTBC5pSPJs2gFGOlhXzRe4f4J3sSe3jrrPRhsvZ6RrmVcSHyvJUq55A08pRmORZFvLvIXpxS9UZO7sg/qYn7mM2KztGQXntfM3DtKrPTXILilO/GP6PGcYI1VyZR6BzuJQ8y1RK2yuHHynjGtOrw3snn0WLpcv0XnaB86ZtuB357gEXxwUfuzYf+gxFDyVAQI8km6YPjgysbtbcWw6wTSoqBwK5c1sF8qOlj9dnSCeJzMA9ZRZNTq9kN1+Xeasgn8Rsr8Yf+vHc4QQPIF96y8g7NxAqCgo6uGCxa+b3sHtwe8McQv7muqaqvtL3+Off1o+trQIssmocVwC640o8l13+XEjydO4s8TQtv+eAsZg1uMiiLDA1M9b7PvLos19hj7xq6XEU5/gX5R4kVQ/dIPK3wQW12POaQPBiIdSz5sNKFC7wjodCMnfm7/GgC5sb2P5Y6cgAxj0Ju5vaElcq5HJywxjelU0IembiGQlq6kOBEMxakxH7gaCPmqyS3GO+kaCNbh4KkxC1hMGL2quSdMhMBhGpb/MP9zajM0ZHocb0vrKn8vrV6vn/htsuR5T+lwYGTKKo4c0/PPqPPQ+x30UkJ3wQBgAW4fCbVexhUAW6ggOvZbYoPF/HjskRxoWFG768PphebdvS/QI0QOH5E840iuyoEMFEaL0PQ534RE1NvTw0yYWPC6QgtKhFvEOT1+2JlO/7ZRqg4lBivz7UAwOlMfrcp+D2vvnjW+FsFc9mGIB+uFMsD2WZRRqRgYi/X39kF9Dh7BpAs2STmHinEKtMeRe4Bcqv7UupEfv51r6CRKiLfluFPSdyZS8ppOoz5l1XGeHQA/OSCe+vHuQhlpUG9gst5OFqj1Dy1HzsyJ/ZFgbJ0vwgrzyVMVtFZsAbhxorUQBTqaaUmRQqbJ+SGcIbV1xbQ74fPkIDsZBxroxgDZKt82fEjlYPlvlBg9g3Id3jN5y6S5Ydr/N8C"


        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        init {
            System.loadLibrary("rdk")
        }

        private var isExit: Boolean? = false
    }
}
