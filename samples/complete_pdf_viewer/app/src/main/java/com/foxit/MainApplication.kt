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
package com.foxit

import android.content.res.Configuration
import com.foxit.App.Companion.instance
import androidx.multidex.MultiDexApplication
import com.foxit.sdk.Localization

class MainApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        instance().applicationContext = this
        Localization.setCurrentLanguage(this, Localization.getCurrentLanguage(this))
        if (!instance().checkLicense()) {
            return
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Localization.setCurrentLanguage(this, Localization.getCurrentLanguage(this))
    }
}