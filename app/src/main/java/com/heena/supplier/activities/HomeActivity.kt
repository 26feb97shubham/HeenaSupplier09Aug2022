package com.heena.supplier.activities

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.heena.supplier.R
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.exitApp
import com.heena.supplier.utils.Utility.Companion.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.side_top_view.view.*


class HomeActivity : AppCompatActivity() {
    private lateinit var navController: NavController
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
        var profile_picture : String = ""
        var myuserId  :Int = 0
        var clickDirestion = ""
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
        if(intent != null){
            type=intent.getStringExtra("type").toString()
            myuserId = intent.getIntExtra(SharedPreferenceUtility.UserId, 0)
        }

        home_layout.setSafeOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, home_layout)
        }

        profile_picture = SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.ProfilePic, ""]
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START)

        val requestOption = RequestOptions().centerCrop()
        Glide.with(this).load(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.ProfilePic, ""])
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                    Log.e("err", p0?.message.toString())
                    return false
                }

                override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                    return false
                }
            }).apply(requestOption).into(menuImg)


        Glide.with(this).load(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.ProfilePic, ""])
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                    Log.e("err", p0?.message.toString())
                    headerView.user_icon_progress_bar_side_top_view.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {
                    headerView.user_icon_progress_bar_side_top_view.visibility = View.GONE
                    return false
                }
            }).apply(requestOption).into(headerView.userIcon)

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