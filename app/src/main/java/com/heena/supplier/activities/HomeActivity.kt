package com.heena.supplier.activities

import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.heena.supplier.R
import com.heena.supplier.fragments.NotificationsFragment
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.exitApp
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.custom_toast_layout.*
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import kotlinx.android.synthetic.main.side_top_view.view.*


class HomeActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var isLogin : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.changeLanguage(
            this,
            SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "")
        )
        setContentView(R.layout.activity_home2)
        initview()
        setUpViews()
    }

    companion object{
        var type:String=""
        var myuserId  :Int = 0
        var clickDirestion = ""
        var notification = false
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }

    private fun initview() {
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.nav_home_host_fragment) as NavHostFragment
        navController=navHostFragment.navController
        bottm_nav.menu.findItem(R.id.itemCategories).setOnMenuItemClickListener {
            openCloseDrawer()
            return@setOnMenuItemClickListener true
        }
        bottm_nav.setupWithNavController(navController)
    }

    private fun setUpViews() {
        isLogin = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.IsLogin, false]
        if(intent != null){
            type=intent.getStringExtra("type").toString()
            notification = intent.getBooleanExtra("notification", false)
            myuserId = intent.getIntExtra(SharedPreferenceUtility.UserId, 0)
        }

        home_layout.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, home_layout)
        }
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val params = navView.layoutParams
        params.width = (((size.x).toDouble()*(3.0/4))).toInt()
        params.height = size.y
        navView.layoutParams = params

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)

        if (isLogin){
            if (notification){
                notification = false
                navController.navigate(R.id.notificationsFragment)
            }
        }

        iv_back.setSafeOnClickListener {
            Log.e("Dest", findNavController(R.id.nav_home_host_fragment).currentDestination?.id.toString())
            onBackPressed()
        }

        iv_notification.setSafeOnClickListener {
            iv_notification.startAnimation(AlphaAnimation(1F, 0.5F))
            findNavController(R.id.nav_home_host_fragment).currentDestination?.id?.let { it1 -> openNotificationFragment(it1) }
        }
    }

    private fun openCloseDrawer() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }


    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        when(findNavController(R.id.nav_home_host_fragment).currentDestination?.id){
            R.id.homeFragment -> exitApp(this, this)
            else-> findNavController(R.id.nav_home_host_fragment).popBackStack()
        }
    }

    private fun openNotificationFragment(destFragment: Int) {
        findNavController(destFragment).navigate(R.id.notificationsFragment)
    }

}