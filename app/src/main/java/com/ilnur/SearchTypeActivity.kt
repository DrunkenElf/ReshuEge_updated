package com.ilnur

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.Window

import java.util.ArrayList
import java.util.Arrays

import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewBinder
import com.ilnur.TreeBinders.Head
import com.ilnur.TreeBinders.HeadBinder
import com.ilnur.TreeBinders.Tail
import com.ilnur.TreeBinders.TailBinder
import com.ilnur.TreeBinders.Tail_check
import com.ilnur.TreeBinders.Tail_check_binder

class SearchTypeActivity : AppCompatActivity() {

    private var subject_prefix: String? = null
    private var section: String? = null
    private val nodes = ArrayList<TreeNode<*>>()
    private fun setupAnim() {
        if (Build.VERSION.SDK_INT >= 21) {
            with(window) {
                requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
                val toRight = Slide()
                toRight.slideEdge = Gravity.RIGHT
                toRight.duration = 300

                val toLeft = Slide()
                toLeft.slideEdge = Gravity.LEFT
                toLeft.duration = 300

                //когда переходишь на новую
                exitTransition = toRight
                enterTransition = toRight
                allowEnterTransitionOverlap = true
                allowReturnTransitionOverlap = true

                //когда нажимаешь с другого назад и открываешь со старого
                returnTransition = toRight
                reenterTransition = toRight
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAnim()
        setContentView(R.layout.activity_search_type)

        val rw = findViewById<RecyclerView>(R.id.rw_search)
        rw.layoutManager = LinearLayoutManager(this)

        if (nodes.isEmpty())
            initNode()
        subject_prefix = intent.getStringExtra("subject_prefix")
        section = intent.getStringExtra("section")

        Log.i("SECandPREf", "$section $subject_prefix")
        val adapter = TreeViewAdapter(nodes, Arrays.asList<TreeViewBinder<out TreeViewBinder.ViewHolder>>(HeadBinder(),
                TailBinder(this, subject_prefix!!, section!!),
                Tail_check_binder(this, subject_prefix!!, section!!)))

        adapter.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {
            override fun onClick(treeNode: TreeNode<*>, viewHolder: RecyclerView.ViewHolder): Boolean {
                if (!treeNode.isLeaf) {
                    //onToggle(!node.isExpand(), holder);
                    if (!treeNode.isExpand)
                        adapter.collapseBrotherNode(treeNode)

                    //Head head = (Head) treeNode.getContent();
                } else {

                }
                return false
            }

            override fun onToggle(b: Boolean, viewHolder: RecyclerView.ViewHolder) {

            }
        })

        rw.adapter = adapter



        subject_prefix = intent.getStringExtra("subject_prefix")
        section = intent.getStringExtra("section")
        val title = intent.getStringExtra("subject_name")
        supportActionBar?.title = "$title. $section"
        /*val subject = resources.getStringArray(R.array.subjects)
        val subjects = resources.getStringArray(R.array.subjects_prefix)
        for (i in subjects.indices) {
            if (subjects[i] != null && subjects[i].contentEquals(subject_prefix!!)) {
                supportActionBar?.title = subject[i] + ". " + section
            }
        }*/


    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //this.finishAfterTransition()
            this.supportFinishAfterTransition()
        } else{
            this.finish()
        }
    }

    private fun initNode() {
        val `var` = TreeNode(Head(R.drawable.ic_tests, "По номеру варианта"))
        nodes.add(`var`)
        val tail = TreeNode(Tail_check(0, "Введите номер варианта"))
        `var`.addChild(tail)

        val zad = TreeNode(Head(R.drawable.ic_training, "По номеру задания"))
        nodes.add(zad)
        val tail1 = TreeNode(Tail(1, "Введите номер задания"))
        zad.addChild(tail1)

        val word = TreeNode(Head(R.drawable.ic_keyword, "По ключевым словам"))
        nodes.add(word)
        val tail2 = TreeNode(Tail(2, "Введите ключевые слова"))
        word.addChild(tail2)
    }

    /*public void initializeStartPage() {

        TextView variantsSearch = (TextView) findViewById(R.id.variantsSearch);
        TextView questionNumberSearch = (TextView) findViewById(R.id.questionNumberSearch);
        TextView keywordSearch = (TextView) findViewById(R.id.keywordSearch);

        variantsSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSearch(1, "Введите номер варианта");
            }
        });

        questionNumberSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSearch(2, "Введите номер задания");
            }
        });

        keywordSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSearch(3, "Введите ключевые слова");
            }
        });
    }*/

    private fun goToSearch(seachType: Int, searchHint: String) {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("type", seachType)
        intent.putExtra("hint", searchHint)
        intent.putExtra("subject_prefix", subject_prefix)
        intent.putExtra("section", section)
        startActivity(intent)
    }
}
