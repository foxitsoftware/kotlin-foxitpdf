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
package com.foxit.pdfreader.fragment

import androidx.fragment.app.FragmentManager

import java.util.HashMap

class AppTabsManager {
    var fragmentManager: FragmentManager? = null

    var filePath: String? = null

    val fragmentMap = HashMap<String, BaseFragment>()

    var currentFragment: BaseFragment? = null

    fun addFragment(key: String, value: BaseFragment) {
        fragmentMap[key] = value
    }

    fun removeFragment(key: String) {
        fragmentMap.remove(key)
    }

    fun clearFragment() {
        fragmentMap.clear()
    }
}
