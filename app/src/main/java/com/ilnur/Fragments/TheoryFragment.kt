package com.ilnur.Fragments

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ilnur.DataBase.MyDB

import java.util.ArrayList
import java.util.Arrays

import tellh.com.recyclertreeview_lib.TreeNode
import tellh.com.recyclertreeview_lib.TreeViewAdapter
import tellh.com.recyclertreeview_lib.TreeViewBinder
//import com.ilnur.DataBase.MyDB1
import com.ilnur.R
import com.ilnur.ShowTheoryActivity
import com.ilnur.TreeBinders.DirNodeBinder
import com.ilnur.TreeBinders.FileNodeBinder
import com.ilnur.TreeView.Dir
import com.ilnur.TreeView.File

class TheoryFragment : Fragment() {

    internal var subject_prefix: String? = null
    private var adapter: TreeViewAdapter? = null
    internal var nodes: MutableList<TreeNode<*>> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        subject_prefix = arguments?.getString("subject_prefix")
        Log.i("subjectTH", subject_prefix)
        val root = inflater.inflate(R.layout.fragment_theory, container, false)

        val rw = root.findViewById<RecyclerView>(R.id.rv_theory)
        rw.layoutManager = LinearLayoutManager(context)
        rw.isSaveEnabled = true
        Log.d("TeorFra", "Q"+subject_prefix+"Q")
        if (nodes.isEmpty())
            setTree()

        adapter = TreeViewAdapter(nodes, Arrays.asList<TreeViewBinder<out TreeViewBinder.ViewHolder>>(FileNodeBinder(), DirNodeBinder()))
        adapter!!.setOnTreeNodeListener(object : TreeViewAdapter.OnTreeNodeListener {
            //long lastClicktime = 0;
            override fun onClick(node: TreeNode<*>, holder: RecyclerView.ViewHolder): Boolean {
                if (!node.isLeaf) {
                    onToggle(!node.isExpand, holder)
                    /*if (!node.isExpand())
                        adapter.collapseBrotherNode(node);*/
                } else {
                    val dir = node.content as File
                    /*if (SystemClock.elapsedRealtime()-lastClicktime<2000){
                        return false;
                    }*/
                    try {
                        val intent = Intent(activity, ShowTheoryActivity::class.java)
                        intent.putExtra("subj_pref", subject_prefix)
                        intent.putExtra("id", dir.fileId)
                        if (Build.VERSION.SDK_INT > 20) {
                            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity())
                            startActivity(intent, options.toBundle())
                        } else {
                            startActivity(intent)
                        }
                        //Log.i("ELSE", "Intent");

                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                        //Log.i("EX", "int");
                        Toast.makeText(context, "Проблемы", Toast.LENGTH_SHORT).show()

                    }

                }
                return false
            }

            override fun onToggle(isExpand: Boolean, holder: RecyclerView.ViewHolder) {
                val dirViewHolder = holder as DirNodeBinder.ViewHolder
                val ivArrow = dirViewHolder.ivArrow
                val rotateDegree = if (isExpand) 90 else -90
                ivArrow.animate().rotationBy(rotateDegree.toFloat())
                        .start()
            }
        })
        rw.adapter = adapter

        return root
    }

    private fun setTree() {
        val mas = MyDB.getRootNames(subject_prefix!!)
        var temp: Array<String>
        for (tema in mas) {
            temp = tema.split(" / ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (MyDB.getTemasNames(subject_prefix!!, temp[1])[0].contains("{")) {
                //Log.i("initfile", temp[0]);
                val tmp = TreeNode(File(temp[0], temp[1]))
                nodes.add(tmp)
            } else {
                //Log.i("initdir", temp[0]);
                val tmp = TreeNode(Dir(temp[0], temp[1], true))
                nodes.add(tmp)
                addTema(tmp, temp[0], temp[1], 1)
                tmp.toggle()
            }
        }
    }

    private fun addTema(tmp: TreeNode<*>, name: String, id: String, k: Int) {
        val mas = MyDB.getTemasNames(subject_prefix!!, id)
        val l: TreeNode<File>
        var ar: Array<String>
        if (mas[0].contains("{")) {
            l = TreeNode(File(name, id))
            tmp.addChild(l)
        } else {
            if (k != 1) {
                val d = TreeNode(Dir(name, id, false))
                tmp.addChild(d)
                for (s in mas) {
                    ar = s.split(" / ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    addTema(d, ar[0], ar[1], 0)
                    //l = new TreeNode<>(new File(ar[0], ar[1]));
                    //tmp.addChild(l);
                }
            } else {
                for (s in mas) {
                    ar = s.split(" / ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    addTema(tmp, ar[0], ar[1], 0)
                    //l = new TreeNode<>(new File(ar[0], ar[1]));
                    //tmp.addChild(l);
                }
            }
        }
    }


    fun setSubject(subject: String) {
        subject_prefix = subject
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("subject_prefix", subject_prefix)
        super.onSaveInstanceState(outState)
    }
}
