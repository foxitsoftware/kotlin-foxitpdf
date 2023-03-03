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
package com.foxit.pdf.function.annotation

import com.foxit.sdk.ActionCallback
import com.foxit.sdk.pdf.PDFDoc
import com.foxit.sdk.IdentityProperties
import com.foxit.sdk.MenuListArray
import com.foxit.sdk.MenuItemExArray
import com.foxit.sdk.MenuItemEx
import com.foxit.sdk.common.Constants
import com.foxit.sdk.common.Range
import com.foxit.sdk.common.fxcrt.RectF
import com.foxit.sdk.pdf.Signature

internal class CustomActionCallback : ActionCallback() {
    override fun release() {}
    override fun invalidateRect(document: PDFDoc, page_index: Int, pdf_rect: RectF): Boolean {
        return false
    }

    override fun getCurrentPage(document: PDFDoc): Int {
        return 0
    }

    override fun setCurrentPage(document: PDFDoc, page_index: Int) {}
    override fun getPageRotation(document: PDFDoc, page_index: Int): Int {
        return Constants.e_Rotation0
    }

    override fun setPageRotation(document: PDFDoc, page_index: Int, rotation: Int): Boolean {
        return false
    }

    override fun executeNamedAction(document: PDFDoc, named_action: String): Boolean {
        return false
    }

    override fun setDocChangeMark(document: PDFDoc, change_mark: Boolean): Boolean {
        return false
    }

    override fun getDocChangeMark(document: PDFDoc): Boolean {
        return false
    }

    override fun getOpenedDocCount(): Int {
        return 1
    }

    override fun getOpenedDoc(index: Int): PDFDoc? {
        return null
    }

    override fun getCurrentDoc(): PDFDoc? {
        return null
    }

    override fun createBlankDoc(): PDFDoc? {
        return null
    }

    override fun openDoc(file_path: String, password: String): Boolean {
        return false
    }

    override fun beep(type: Int): Boolean {
        return false
    }

    override fun response(
        question: String,
        title: String,
        default_value: String,
        label: String,
        is_password: Boolean
    ): String? {
        return null
    }

    override fun getFilePath(document: PDFDoc): String? {
        return null
    }

    override fun print(
        document: PDFDoc,
        is_ui: Boolean,
        page_range: Range,
        is_silent: Boolean,
        is_shrunk_to_fit: Boolean,
        is_printed_as_image: Boolean,
        is_reversed: Boolean,
        is_to_print_annots: Boolean
    ): Boolean {
        return false
    }

    override fun submitForm(
        document: PDFDoc,
        form_data: ByteArray,
        url: String,
        file_format_type: Int
    ): Boolean {
        return false
    }

    override fun launchURL(url: String): Boolean {
        return false
    }

    override fun browseFile(): String? {
        return null
    }

    override fun getLanguage(): Int {
        return e_LanguageCHS
    }

    override fun alert(msg: String, title: String, type: Int, icon: Int): Int {
        return 0
    }

    override fun getIdentityProperties(): IdentityProperties {
        return IdentityProperties(
            "foxitsoftware",
            "simple_demo@foxitsoftware.cn",
            "simple demo",
            "Simple",
            "",
            "",
            "",
            ""
        )
    }

    override fun popupMenu(menus: MenuListArray, is_selected_item: Boolean): String? {
        return null
    }

    override fun popupMenuEx(menus: MenuItemExArray, is_selected_item: Boolean): MenuItemEx? {
        return null
    }

    override fun getAppInfo(type: Int): String? {
        return null
    }

    override fun mailData(
        data: Any,
        is_ui: Boolean,
        to: String,
        subject: String,
        cc: String,
        bcc: String,
        message: String
    ): Boolean {
        return false
    }

    override fun verifySignature(document: PDFDoc, pdf_signature: Signature): Int {
        return Signature.e_StateUnknown
    }
}