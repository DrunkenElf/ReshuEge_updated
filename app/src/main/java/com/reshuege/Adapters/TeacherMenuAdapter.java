package com.reshuege.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.navigation.Navigation;


import com.reshuege.Fragments.TeacherFragment;
import com.reshuege.R;
import com.reshuege.TeacherActivity;

import java.util.ArrayList;

public class TeacherMenuAdapter extends BaseAdapter {
    private final int[] imgs;
    private final String[] names;
    private final String[] hrefs;
    private final Context cont;
    private TeacherFragment parent;
    private Bundle savedInstance;

    public TeacherMenuAdapter(int[] imgs, String[] names, String[] hrefs,
                              Context cont, TeacherFragment fr, Bundle savedInstance){
        this.imgs = imgs;
        this.names = names;
        this.hrefs = hrefs;
        this.cont = cont;
        this.parent = fr;
        this.savedInstance = savedInstance;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root = convertView;
        if (root == null)
            root = LayoutInflater.from(cont).inflate(R.layout.teacher_menu_item, parent, false);

        TextView tv = root.findViewById(R.id.start_item_text);
        CardView card = root.findViewById(R.id.start_card_item);

        tv.setText(names[position]);

        card.setOnClickListener(v -> {
            Log.d("POS", ""+position);
            if (!TeacherFragment.logged){
                //msg about waiting
                Toast.makeText(cont, "Дождитесь получения пользовательских данных", Toast.LENGTH_LONG).show();
            } else {
                Intent intent;
                switch (position) {
                    case 0://first(create tests)
                        Log.d("POS", "switch "+position);
                        Bundle bundle = new Bundle();
                        bundle.putString("href", hrefs[position]);
                        bundle.putString("subj", TeacherFragment.href);
                        bundle.putStringArrayList("keySet", new ArrayList<>(TeacherFragment.cookies.keySet()));
                        bundle.putStringArrayList("values", new ArrayList<>(TeacherFragment.cookies.values()));
                        Navigation.findNavController(v).navigate(R.id.action_nav_teacher2_to_createTestsFragment, bundle);
                        Log.d("WIIWIWIWI", "TUTYUTY");
                        break;
                    case 1:
                    case 3:
                    case 4:
                        intent = new Intent(v.getContext(), TeacherActivity.class);
                        intent.putStringArrayListExtra("keySet", new ArrayList<>(TeacherFragment.cookies.keySet()));
                        intent.putStringArrayListExtra("values", new ArrayList<>(TeacherFragment.cookies.values()));
                        intent.putExtra("url", "https://"+ TeacherFragment.href +"-ege.sdamgia.ru/teacher"+hrefs[position]);
                        if (Build.VERSION.SDK_INT > 20) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this.parent.requireActivity());
                            this.parent.startActivity(intent, options.toBundle());
                        } else {
                            this.parent.startActivity(intent);
                        }
                        break;
                    case 2:
                    case 5:
                    case 6:
                        intent = new Intent(v.getContext(), TeacherActivity.class);
                        intent.putStringArrayListExtra("keySet", new ArrayList<>(TeacherFragment.cookies.keySet()));
                        intent.putStringArrayListExtra("values", new ArrayList<>(TeacherFragment.cookies.values()));
                        intent.putExtra("url", "https://"+ TeacherFragment.href +"-ege.sdamgia.ru"+hrefs[position]);
                        if (Build.VERSION.SDK_INT > 20) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this.parent.requireActivity());
                            this.parent.startActivity(intent, options.toBundle());
                        } else {
                            this.parent.startActivity(intent);
                        }
                        break;
                }
            }
        });
        return root;
    }

    @Override
    public int getCount() {
        return hrefs.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
