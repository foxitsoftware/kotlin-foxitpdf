/**
 * Copyright (C) 2003-2025, Foxit Software Inc..
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

import com.foxit.App.Companion.instance
import com.foxit.uiextensions.FoxitApplication

class MainApplication : FoxitApplication() {
    override fun onCreate() {
        super.onCreate()
        instance().applicationContext = this
        if (!instance().checkLicense()) {
            return
        }
    }
}