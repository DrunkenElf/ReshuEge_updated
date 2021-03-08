package com.reshuege.Fragments;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.reshuege.R;
import com.reshuege.TeacherActivity;
import com.reshuege.TreeBinders.ChildBinder;
import com.reshuege.TreeBinders.ParentBinder;
import com.reshuege.TreeView.Probcat;
import com.reshuege.utils.Categories;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewAdapter;

public class CreateTestsFragment extends Fragment {
    private RecyclerView rv;
    private TreeViewAdapter adapter;
    private String login;
    private String password;
    private static String href;
    private static String subj;
    private static final String link = "-ege.sdamgia.ru";
    private static Map<String, String> cookies;
    List<TreeNode> nodes = new ArrayList<>();
    private Document doc;
    public Dialog dialog;


    public void setPasAndLogin(String password, String login) {
        this.password = password;
        this.login = login;
    }

    public void setHrefs(String href, String subj) {
        this.href = href;
        this.subj = subj;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    private Map<String, String> listToMap(ArrayList<String> keys, ArrayList<String> values) {
        Map<String, String> map = new HashMap<>();
        if (keys != null && values != null) {
            Iterator i = keys.iterator();
            Iterator j = values.iterator();
            while (i.hasNext() || j.hasNext()) map.put(i.next().toString(), j.next().toString());
        }
        return map;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_teacher_create_test, container, false);
        //setRetainInstance(true);
        href = getArguments().getString("href","");
        subj = getArguments().getString("subj","");
        cookies = listToMap(getArguments().getStringArrayList("keySet"), getArguments().getStringArrayList("values"));
        copyOfWork(root);

        edit_inputs(root);

        create_test(root);

        dialog = new Dialog(root.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loader);
        dialog.setCancelable(false);

        ImageView cus = dialog.findViewById(R.id.custom_loading_imageView);

        Glide.with(root)
                .load(R.drawable.ball)
                .centerCrop()
                .into(cus);


        ImageView gif = root.findViewById(R.id.gif);

        Glide.with(root)
                .load(R.drawable.ball)
                .into(gif);

        gif.setVisibility(View.VISIBLE);

        rv = root.findViewById(R.id.list_cats);
        rv.setLayoutManager(new LinearLayoutManager(root.getContext()));

        new LoadCategory(gif).execute();

        return root;
    }

    // заполнить класс категории,
    // тут нет href of subj and name of subj
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void create_test(View root) {
        AppCompatButton create_home = root.findViewById(R.id.butt_crete_home);
        AppCompatButton create_exam = root.findViewById(R.id.butt_create_exam);

        create_home.setOnClickListener(v -> edit_create(0, root));
        create_exam.setOnClickListener(v -> edit_create(0, root));
    }

    private void edit_create(int type, View root) {
        int counter = 0;
        dialog.show();
        Connection conn = Jsoup.connect("https://" + subj + link + "/test?a=generate")
                .cookies(cookies)
                .data("a", "generate")
                .data("redir", "/test_editor?id=%(id)d")
                .data("defthemes", "true").followRedirects(true).method(Connection.Method.POST);
        switch (type) {
            case 0:
                conn.data("public", "dz");
                for (int i = 0; i < nodes.size(); i++) {
                    Probcat temp = (Probcat) nodes.get(i).getContent();
                    if (Integer.parseInt(temp.num) >= 1)
                        counter++;
                    conn.data(temp.getHref(), temp.num);
                    for (TreeNode node : (List<TreeNode>) nodes.get(i).getChildList()) {
                        Probcat leaf = (Probcat) node.getContent();
                        conn.data("has" + leaf.getHref(), "true");
                        if (leaf.isLeaf)
                            conn.data(leaf.getHref(), "true");
                    }
                }
                break;
            case 1:
                conn.data("public", "True");
                for (int i = 0; i < nodes.size(); i++) {
                    Probcat temp = (Probcat) nodes.get(i).getContent();
                    conn.data(temp.getHref(), temp.num);
                    for (TreeNode node : (List<TreeNode>) nodes.get(i).getChildList()) {
                        Probcat leaf = (Probcat) node.getContent();
                        conn.data("has" + leaf.getHref(), "true");
                        if (leaf.isLeaf)
                            conn.data(leaf.getHref(), "true");
                    }
                }
                break;
        }
        if (counter > 0)
            new checkCon(root, null, null, true, conn).execute();
        else {
            dialog.dismiss();
            Toast.makeText(root.getContext(), "Выберите темы", Toast.LENGTH_LONG).show();
        }
        adapter.notifyDataSetChanged();
    }

    private void edit_inputs(View root) {
        AppCompatButton stand_test = root.findViewById(R.id.butt_standar_test);
        AppCompatButton clear = root.findViewById(R.id.butt_clear_inps);
        AppCompatButton zad_b = root.findViewById(R.id.butt_zad_b);
        AppCompatButton zad_c = root.findViewById(R.id.butt_zad_c);


        // check doc!=null
        stand_test.setOnClickListener(v -> edit_children(3));
        clear.setOnClickListener(v -> edit_children(0));
        zad_b.setOnClickListener(v -> edit_children(1));
        zad_c.setOnClickListener(v -> edit_children(2));
    }

    // 0 - clear; 1 - zad_b; 2 - zad_c, 3 - stand
    private void edit_children(int type) {
        Element el;
        String[] bounds;
        switch (type) {
            case 0:
                el = doc.select("input[value=\"Стандартный вариант\"]").first();
                for (int i = 0; i <= nodes.size() - 1; i++) {
                    Probcat temp = (Probcat) nodes.get(i).getContent();
                    temp.num = "0";
                }
                break;
            case 1:
                el = doc.select("input[value=\"Задания В\"]").first();
                bounds = getBounds(el.attr("onclick").split(" ")[1]);
                for (int i = 0; i <= nodes.size() - 1; i++) {
                    Probcat temp = (Probcat) nodes.get(i).getContent();
                    if (Integer.parseInt(bounds[0]) - 1 <= i && i <= Integer.parseInt(bounds[1]) - 1)
                        temp.num = "1";
                    else
                        temp.num = "0";
                }
                break;
            case 2:
                el = doc.select("input[value=\"Задания С\"]").first();
                bounds = getBounds(el.attr("onclick").split(" ")[1]);
                for (int i = 0; i <= nodes.size() - 1; i++) {
                    Probcat temp = (Probcat) nodes.get(i).getContent();
                    if (Integer.parseInt(bounds[0]) - 1 <= i && i <= Integer.parseInt(bounds[1]) - 1)
                        temp.num = "1";
                    else
                        temp.num = "0";
                }
                break;
            case 3:
                el = doc.select("input[value=\"Стандартный вариант\"]").first();
                bounds = getBounds(el.attr("onclick").split(" ")[1]);
                for (int i = 0; i <= nodes.size() - 1; i++) {
                    Probcat temp = (Probcat) nodes.get(i).getContent();
                    if (Integer.parseInt(bounds[0]) - 1 <= i && i <= Integer.parseInt(bounds[1]) - 1)
                        temp.num = "1";
                    else
                        temp.num = "0";
                }
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private String[] getBounds(String str) {
        str = str.replace("(i=", "")
                .replace(";i<=", " ")
                .replace(";i++)", "");
        return str.split(" ");
    }


    private void copyOfWork(View root) {
        Button copy_home = root.findViewById(R.id.butt_home_copy);
        Button copy_exam = root.findViewById(R.id.butt_exam_copy);

        EditText input_copy = root.findViewById(R.id.input_copy);
        TextInputLayout input_layout = root.findViewById(R.id.input_lay);
        copy_home.setOnClickListener(v -> {
            if (input_copy.getText() == null || input_copy.getText().toString().equals("")) {
                input_layout.setError("Введите номер работы");
                input_copy.requestFocus();
            } else {
                dialog.show();
                new checkCon(root, "2", input_copy.getText().toString(), false, null).execute();
            }
        });
        copy_exam.setOnClickListener(v -> {
            if (input_copy.getText() == null || input_copy.getText().toString().equals("")) {
                input_layout.setError(Html.fromHtml("Введите номер работы"));
                input_copy.requestFocus();
            } else {
                dialog.show();
                new checkCon(root, "1", input_copy.getText().toString(), false, null).execute();
            }
        });
    }

    class Pair {
        boolean isExist;
        String url;

        Pair(boolean isExist, String url) {
            this.isExist = isExist;
            this.url = url;
        }
    }


    class checkCon extends AsyncTask<Void, Void, Pair> {
        private View v;
        private String pub;
        private String id;
        private Map<String, String> tmpCookies;
        private boolean isCreate;
        private Connection conn;


        public checkCon(View v, String pub, String id, boolean isCreate, Connection conn) {
            this.v = v;
            this.pub = pub;
            this.id = id;
            this.isCreate = isCreate;
            this.conn = conn;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Pair doInBackground(Void... voids) {
            Connection.Response resp = null;
            try {
                if (isCreate) {
                    resp = conn.execute();
                } else {
                    resp = Jsoup.connect("https://" + subj + link + "/test_editor?a=clone")
                            .cookies(cookies)
                            .data("a", "clone")
                            .data("pub", pub)
                            .data("id", id)
                            .method(Connection.Method.POST)
                            .followRedirects(true)
                            .execute();
                }
                //Log.d("URL", Jsoup.connect(resp.url().toString()).cookies(cookies).get().select("div.content").text());
                Element el = Jsoup.connect(resp.url().toString()).ignoreHttpErrors(true).ignoreContentType(true).cookies(cookies).get()
                        .select("div.content").first();
                Log.d("URL", resp.url().toString());
                if (el.text().contains("Неверный номер варианта")) {
                    return new Pair(false, null);
                } else {
                    return new Pair(true, resp.url().toString());
                }
            } catch (IOException e) {
                //e.printStackTrace();
                if (resp != null)
                    Log.d("CODE", "" + resp.statusCode());
                Log.d("Url", resp.url().toString());
            }
            return new Pair(false, null);
        }

        @Override
        protected void onPostExecute(Pair pair) {
            super.onPostExecute(pair);
            dialog.dismiss();
            if (!pair.isExist)
                Toast.makeText(v.getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
            else {
                Intent intent = new Intent(v.getContext(), TeacherActivity.class);
                intent.putStringArrayListExtra("keySet", new ArrayList<>(cookies.keySet()));
                intent.putStringArrayListExtra("values", new ArrayList<>(cookies.values()));
                intent.putExtra("url", pair.url);
                if (Build.VERSION.SDK_INT > 20) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(requireActivity());
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            }
        }
    }

    private class LoadCategory extends AsyncTask<Void, Void, Categories> {
        private ImageView gif;

        public LoadCategory(ImageView gif) {
            this.gif = gif;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Categories doInBackground(Void... voids) {
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> hrefs = new ArrayList<>();
            ArrayList<TreeMap<String, String>> children = new ArrayList<>();
            try {
                Document page = Jsoup.connect("https://" + subj + "-ege.sdamgia.ru" + href)
                        .timeout(20000).ignoreHttpErrors(true).ignoreContentType(true).userAgent("Mozilla/5.0").cookies(cookies).get();
                doc = page;
                Elements parents = page
                        .select("table.zebra")
                        .select("tbody")
                        .select("td[style=\"text-align:left;padding-left:10px\"]");

                for (Element parent : parents) {
                    Element tr = parent.parent();
                    Element input = tr.select("td").last(); //input
                    Element podtema = tr.nextElementSibling();
                    Elements pods = podtema.select("tr > td")
                            .select("td > div")
                            .select("div > table")
                            .select("table > tbody")
                            .select("tbody > tr").select("tr > td[style=\"padding-left:10px\"]");
                    names.add(parent.text());
                    hrefs.add(tr.select("td").last().select("td > input").attr("name"));
                    TreeMap<String, String> map = new TreeMap<>();
                    for (Element child : pods) {
                        map.put(child.text(), child.select("input.theme_cb").attr("name"));
                    }
                    children.add(map);

                }
                Categories categories = new Categories(names, hrefs, children);
                for (int i = 0; i < names.size(); i++) {
                    TreeNode<Probcat> parent = new TreeNode<>(new Probcat(names.get(i), hrefs.get(i), false));
                    for (Map.Entry entry : children.get(i).entrySet()) {
                        parent.addChild(new TreeNode<>(new Probcat(entry.getKey().toString(),
                                entry.getValue().toString(), true)));

                    }
                    nodes.add(parent);
                }
                return categories;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Categories categories) {
            super.onPostExecute(categories);
            if (categories == null)
                Log.d("CATS", "NULL");
            adapter = new TreeViewAdapter(nodes, Arrays.asList(new ParentBinder(), new ChildBinder()));
            adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
                @Override
                public boolean onClick(TreeNode treeNode, RecyclerView.ViewHolder viewHolder) {
                    if (treeNode.isLeaf()) {
                        //try to toggle
                        Log.d("CLICK", "child");
                        Probcat leaf = (Probcat) treeNode.getContent();
                        CheckBox check = viewHolder.itemView.findViewById(R.id.child_check);
                        if (leaf.isLeaf) leaf.setLeaf(false);
                        else leaf.setLeaf(true);
                        adapter.notifyDataSetChanged();
                    } else {
                        //nothing
                        if (!treeNode.isExpand())
                            adapter.collapseBrotherNode(treeNode);
                        onToggle(!treeNode.isExpand(), viewHolder);
                    }

                    return false;
                }

                @Override
                public void onToggle(boolean b, RecyclerView.ViewHolder viewHolder) {

                }
            });


            gif.setVisibility(View.GONE);
            rv.setAdapter(adapter);
            final LayoutAnimationController controller =
                    AnimationUtils.loadLayoutAnimation(rv.getContext(), R.anim.layout_anim_fall_down);

            rv.setLayoutAnimation(controller);
            rv.getAdapter().notifyDataSetChanged();
            rv.scheduleLayoutAnimation();
        }
    }
}
