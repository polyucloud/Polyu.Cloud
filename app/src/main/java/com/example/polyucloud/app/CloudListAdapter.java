package com.example.polyucloud.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Tom on 4/19/14.
 */
public class CloudListAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private final ArrayList<CloudExplorer.File> files;

    public CloudListAdapter(Context context, ArrayList<CloudExplorer.File> files)
    {
        //Clone the list
        this.files = new ArrayList<CloudExplorer.File>(files);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public Object getItem(int i) {
        return files.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class ViewTag
    {
        TextView fileName;
        ImageView thumb;

        public ViewTag(TextView fileName, ImageView thumb) {
            this.fileName = fileName;
            this.thumb = thumb;
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewTag viewTag;

        if(view == null) {
            view = inflater.inflate(R.layout.file_list_item, null);
            viewTag = new ViewTag( (TextView) view.findViewById(R.id.list_item_file_name), (ImageView) view.findViewById(R.id.list_item_thumb) );
            view.setTag(viewTag);
        } else {
            viewTag = (ViewTag) view.getTag();
        }

        viewTag.fileName.setText(files.get(i).NAME);

        if(files.get(i).IS_DIR) {
            viewTag.thumb.setImageResource(R.drawable.ic_folder);
        } else {
                viewTag.thumb.setImageResource(R.drawable.ic_file);
        }
        return view;
    }






}
