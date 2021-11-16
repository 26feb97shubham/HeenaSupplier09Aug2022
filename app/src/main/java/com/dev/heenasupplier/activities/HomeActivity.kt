package com.dev.heenasupplier.activities

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.dev.heenasupplier.R
import com.dev.heenasupplier.utils.SharedPreferenceUtility
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.side_top_view.view.*

class HomeActivity : AppCompatActivity() {
    var doubleClick:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)
        setUpViews()
    }

    companion object{
        var type:String=""
        var profile_picture : String = ""
        var myuserId  :Int = 0
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
    private fun setUpViews() {
        if(intent != null){
            type=intent.getStringExtra("type").toString()
            myuserId = intent.getIntExtra(SharedPreferenceUtility.UserId, 0)
        }

        home_layout.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, home_layout)
        }

        profile_picture = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ProfilePic,"")
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.START)


        val requestOption = RequestOptions().centerCrop()
        Glide.with(this).load(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ProfilePic, ""))
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


        Glide.with(this).load(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.ProfilePic, ""))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: com.bumptech.glide.request.target.Target<Drawable>?, p3: Boolean): Boolean {
                    Log.e("err", p0?.message.toString())
                    return false
                }

                override fun onResourceReady(p0: Drawable?, p1: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, p4: Boolean): Boolean {


                    return false
                }
            }).apply(requestOption).into(headerView.userIcon)


        headerView.tv_name.text = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.Username, "")
        headerView.tv_address.text = SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.Address, "")
        menuImg.setOnClickListener {
            SharedPreferenceUtility.getInstance().hideSoftKeyBoard(this, menuImg)
            openCloseDrawer()
        }

        iv_back.setOnClickListener {
            onBackPressed()
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
            R.id.homeFragment -> exitApp()
            R.id.myAppointmentsFragment ->{
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.myProfileFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.SubscriptionFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.notificationsFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.SettingsFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.revenuesFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.myCardsFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.myBanksFragment -> {
            itemHome.setImageResource(R.drawable.home_icon_active)
            findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.myLocationsFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.langFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.CMSFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.contactUsFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            R.id.helpFragment -> {
                itemHome.setImageResource(R.drawable.home_icon_active)
                findNavController(R.id.nav_home_host_fragment).navigate(R.id.homeFragment)
            }
            else-> findNavController(R.id.nav_home_host_fragment).popBackStack()
        }
    }

    private fun exitApp() {
        val toast = Toast.makeText(
            this,
            getString(R.string.please_click_back_again_to_exist),
            Toast.LENGTH_SHORT
        )


        if(doubleClick){
            finishAffinity()
            doubleClick=false
        }
        else{

            doubleClick=true
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                toast.show()
                doubleClick=false
            }, 500)
        }
    }

}