package com.example.wordcapture.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.wordcapture.R
import com.example.wordcapture.data.AppDatabase
import com.example.wordcapture.domain.CaptionedImagesAdapter
import com.example.wordcapture.fragment.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    ExpressionsFragment.ExpressionUpdatedListener {
    private var shareActionProvider: ShareActionProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerAdapter = SectionsPagerAdapter(supportFragmentManager, applicationContext)
        val pager = findViewById<View>(R.id.pager) as ViewPager
        pager.adapter = pagerAdapter

        val tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(pager)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

//        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
//        val toggle = ActionBarDrawerToggle(
//            this,
//            drawer,
//            toolbar,
//            R.string.nav_open_drawer,
//            R.string.nav_close_drawer
//        )
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()

        val fab = findViewById<View>(R.id.fab)
        fab.setOnClickListener{ view -> fabClick(view) }

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)
        var language = sharedPref.getString(getString(R.string.default_language_key), null)

        if(language == null){

            val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.default_language_message)
                .setItems(R.array.languages) { dialog, which ->
                    language = resources.getStringArray(R.array.languages)[which]
                }
                .create()

            dialog.setOnDismissListener{ dialog ->
                with (sharedPref.edit()) {
                    putString(getString(R.string.default_language_key), language)
                    apply()
                }
            }
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu:Menu): Boolean {
        return true

        //TODO do I want options menu?
        menuInflater.inflate(R.menu.menu_main, menu)
        val menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = MenuItemCompat.getActionProvider(menuItem) as ShareActionProvider?
        setShareActionIntent("Unfulfillable");
        return super.onCreateOptionsMenu(menu)
    }

    private fun setShareActionIntent(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        shareActionProvider!!.setShareIntent(intent)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.action_create_order -> {
                val intent = Intent(this, ActionActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun fabClick(view: View) {
        val intent = Intent(this, NewExpressionActivity::class.java)
        resultLauncherNewExpression.launch(intent)
    }


    // TODO hack works, but very slow
    private fun updateExpressionsList(result: ActivityResult){
        if (result.resultCode == Activity.RESULT_OK ) {
            val inserted = result.data?.extras?.getBoolean(ViewExpressionFragment.EXPRESSION_INSERTED, false)
            val updated = result.data?.extras?.getBoolean(ViewExpressionFragment.EXPRESSION_UPDATED, false)

            if ( (inserted != null && inserted == true) || (updated != null && updated == true)) {
                val itemRecycler = findViewById<RecyclerView>(R.id.expressions_recycler)
                val adapter = itemRecycler.adapter!! as CaptionedImagesAdapter

                CoroutineScope(Dispatchers.IO).launch {
                    val expressions = AppDatabase.get(applicationContext).expressionDao().getAll()

                    runOnUiThread {
                        adapter.expressions = expressions
                        adapter.notifyDataSetChanged()

//                        if(inserted != null && inserted == true){
//                            adapter.notifyItemInserted(expressions.size-1)
//                        }
//                        else if(updated != null && updated == true){
//                            val position = result.data?.extras?.getInt(ViewExpressionFragment.EXPRESSION_UPDATED_POS)
//
//                        }

                    }

                }

                // TODO doesnt work because adapter doesnt have acces with databae
//                val noItems = itemRecycler.adapter!!.itemCount
//                itemRecycler.adapter!!.notifyItemInserted(noItems)
            }
        }

    }

    override fun expressionUpdated(result: ActivityResult) {
        updateExpressionsList(result)
    }

    val resultLauncherNewExpression = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> updateExpressionsList(result) }


    private class SectionsPagerAdapter(
        fm: FragmentManager?,
        var applicationContext: Context
    ) : FragmentPagerAdapter(fm!!) {

        override fun getCount(): Int {
            return 1
        }
        override fun getItem(position: Int): Fragment {
            when (position) {
                1 -> {
                    return StatisticsFragment()
                }
                0 -> {
                    return ExpressionsFragment()
                }
            }
            return StatisticsFragment()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return applicationContext.getText(R.string.main_tab_name)
                1 -> return applicationContext.getText(R.string.stat_tab_name)
            }
            return null
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val intent: Intent? = when (item.itemId) {
            R.id.drawer_option_1 -> null
            R.id.drawer_option_2 -> null
            R.id.drawer_option_3 -> null
            else -> null
        }

        if(intent != null){
            startActivity(intent)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }


}