package com.example.polyucloud.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by EricTangET on 15/4/14.
 */
public class FileListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    ArrayList<HashMap> fileList = null;

    public FileListAdapter(Context context, ArrayList<HashMap> fileList) {
        this.fileList = fileList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int i) {
        return fileList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
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

        viewTag.fileName.setText( (String) fileList.get(i).get("f_name"));

        String type = (String) fileList.get(i).get("f_type");
        if(type.equalsIgnoreCase("DIR")) {
            viewTag.thumb.setImageResource(R.drawable.ic_folder);
        } else {
            String suffix = android.webkit.MimeTypeMap.getFileExtensionFromUrl((String) fileList.get(i).get("f_name"));
            if(!checkImage(suffix))
                viewTag.thumb.setImageResource(R.drawable.ic_file);
            else {
                File ff = (File) fileList.get(i).get("f_path");
                byte[] imageData = null;

                try
                {

                    final int THUMBNAIL_SIZE = 64;

                    FileInputStream fis = new FileInputStream(ff.getAbsolutePath());
                    Bitmap imageBitmap = BitmapFactory.decodeStream(fis);


                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

                    viewTag.thumb.setImageBitmap(imageBitmap);

                }
                catch(Exception ex) {

                }
            }
        }

        return view;
    }

    private boolean checkImage(String suffix)
    {
        String[] imgSuffixs = {"jpg","jpeg","png","gif","bmp"};
        for(int i=0;i<imgSuffixs.length;i++)
            if(imgSuffixs[i].equalsIgnoreCase(suffix))
                return true;
        return false;
    }

    public class ViewTag {
        TextView fileName;
        ImageView thumb;

        public ViewTag(TextView fileName, ImageView thumb) {
            this.fileName = fileName;
            this.thumb = thumb;
        }
    }
}
