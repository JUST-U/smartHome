package cn.edu.zstu.smarthome.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import cn.edu.zstu.smarthome.R;

public class AdapterBrandList extends BaseAdapter implements SectionIndexer {

    private LayoutInflater mInflater;

    private List<AdapterPYinItem> mItems;

    public  AdapterBrandList(Context context, List<AdapterPYinItem> list) {
        mInflater = LayoutInflater.from(context);
        mItems = list;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup par) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.fragment_brand_list_item,
                    null);
            holder = new ViewHolder();
            holder.file_title = (TextView) convertView
                    .findViewById(R.id.contactitem_catalog);
            holder.file_name = ((TextView) convertView
                    .findViewById(R.id.text_item_name));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String catalog = mItems.get(position).getPyin()
                .substring(0, 1).toUpperCase(Locale.getDefault());
        ;
        if (position == 0) {
            holder.file_title.setVisibility(View.VISIBLE);
            holder.file_title.setText(catalog);
        } else {
            String title =
                    mItems.get(position - 1).getPyin().substring(0, 1).toUpperCase(Locale.getDefault());
            if (catalog.equals(title)) {
                holder.file_title.setVisibility(View.GONE);
            } else {
                holder.file_title.setVisibility(View.VISIBLE);
                holder.file_title.setText(catalog);
            }
        }
        holder.file_name.setText(mItems.get(position).getName());
        return convertView;
    }

    private class ViewHolder {
        TextView file_title;
        TextView file_name;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public int getPositionForSection(int section) {
        for (int i = 0; i < mItems.size(); i++) {
            String l = mItems.get(i).getPyin()
                    .substring(0, 1);
            char firstChar = l.toUpperCase(Locale.getDefault()).charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getSectionForPosition(int arg0) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        return null;
    }

}