package com.official.senestro.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.official.senestro.core.utils.AdvanceUtils;
import com.official.senestro.core.utils.FileUtils;
import com.official.senestro.core.utils.StorageUtils;
import com.official.senestro.core.utils.AdvanceTimer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class FilePicker {
    private final Context context;
    private final Activity activity;
    private String mime = null;
    private boolean multiSelection = false;
    private String rootPath = null;
    private String currentPath = null;
    private final ArrayList<HashMap<String, Object>> lists = new ArrayList<>();
    private AlertDialog dialog = null;
    private final HashMap<Integer, String> selections = new HashMap<>();
    private View view;
    private ListView listView = null;
    private TextView navigationView = null;
    private TextView selectionView = null;
    private final String selectionColor = "#80A2C6E3";
    private Button backButton;
    private Button cancelButton;
    private Button confirmButton;
    private StorageUtils storageUtils;
    private MaterialToast materialToast;

    public FilePicker(@NonNull Context context, @NonNull Activity activity) {
        this.context = context;
        this.activity = activity;
        storageUtils = new StorageUtils(this.context);
        materialToast = new MaterialToast(this.context);
    }

    public void setPath(@NonNull String absolutePath) {
        this.rootPath = absolutePath;
    }

    public void setMime(@Nullable String mime) {
        this.mime = mime;
    }

    public void multiSelection(boolean allow) {
        this.multiSelection = allow;
    }

    @SuppressLint("InflateParams")
    public void startPicker(@NonNull CallbackListener callback) {
        AdvanceTimer.schedule(() -> activity.runOnUiThread(() -> createDialog(callback)), 100);
    }

    @SuppressLint("InflateParams")
    private void createDialog(@NonNull CallbackListener callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        view = activity.getLayoutInflater().inflate(R.layout.file_picker_layout, null);
        initializeViews(view);
        setButtonListeners(callback);
        setDialogAttributes(builder, view);
        dialog.show();
        scheduleListPath(rootPath);
    }

    private void initializeViews(View view) {
        listView = view.findViewById(R.id.files_picker_view);
        navigationView = view.findViewById(R.id.files_picker_nav_current_path);
        selectionView = view.findViewById(R.id.files_picker_list_view_selection_log);
    }

    private void setButtonListeners(CallbackListener callback) {
        backButton = view.findViewById(R.id.files_picker_list_view_back_button);
        cancelButton = view.findViewById(R.id.files_picker_cancel_button);
        confirmButton = view.findViewById(R.id.files_picker_confirm_button);
        backButton.setOnClickListener(v -> {
            if (!currentPath.equals(rootPath)) {
                listPath(AdvanceUtils.getParentPath(currentPath));
            }
        });
        cancelButton.setOnClickListener(v -> {
            dialog.dismiss();
            assert callback != null;
            callback.onCancel("Selection has been canceled");
        });
        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();
            assert callback != null;
            callback.onCompleted(getSelections());
        });
        designButtons(backButton, cancelButton, confirmButton);
    }

    private void designButtons(@NonNull Button... buttons) {
        for (Button button : buttons) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            int color = Color.TRANSPARENT;
            if (button == backButton) {
                color = Color.parseColor("#0F3CC3");
            } else if (button == cancelButton) {
                color = Color.parseColor("#FF0000");
            } else if (button == confirmButton) {
                color = Color.parseColor("#009D06");
            }
            gradientDrawable.setColor(color);
            gradientDrawable.setCornerRadius(10);
            button.setBackground(gradientDrawable);
        }
    }

    private void setDialogAttributes(AlertDialog.Builder builder, View view) {
        builder.setView(view).setCancelable(false);
        dialog = builder.create();
    }

    private void scheduleListPath(@NonNull String path) {
        AdvanceTimer.schedule(() -> listPath(path), 200);
    }

    private void listPath(@NonNull String absolutePath) {
        String absPath = new File(absolutePath).getAbsolutePath();
        if (AdvanceUtils.isDirectory(absPath)) {
            activity.runOnUiThread(() -> {
                selections.clear();
                lists.clear();
                countSelections();
                currentPath = absPath;
                navigationView.setText(currentPath);
                List<HashMap<String, Object>> listedFiles = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q ? AdvanceUtils.listDir(context, new File(absolutePath), false) : AdvanceUtils.listDir(new File(absolutePath), false);
                for (HashMap<String, Object> listedMap : listedFiles) {
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("realPath", listedMap.get("realPath"));
                    lists.add(item);
                }
                listView.setAdapter(new ListViewAdapter(listMPath()));
                ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
            });
        }
    }

    private ArrayList<HashMap<String, Object>> listMPath() {
        ArrayList<HashMap<String, Object>> newLists = new ArrayList<>(lists);
        lists.clear();
        if (mime != null) {
            for (HashMap<String, Object> map : newLists) {
                FileUtils infoUtils = new FileUtils(Objects.requireNonNull(map.get("absolutePath")).toString());
                String realPath = infoUtils.getPath();
                String realMime = infoUtils.getMime();
                if (infoUtils.isDirectory()) {
                    lists.add(map);
                } else if (realMime.startsWith(Objects.requireNonNull(selectionMime())) || realMime.equals(Objects.requireNonNull(selectionMime()))) {
                    lists.add(map);
                }
            }
        } else {
            lists.addAll(newLists);
        }
        return lists;
    }

    private String selectionMime() {
        return mime != null ? (mime.contains("/*") ? mime.replace("/*", "") : (mime.contains("\\*") ? mime.replace("\\*", "") : null)) : null;
    }

    private void countSelections() {
        int selected = selections.size();
        selectionView.setText(selected > 0 ? "Selected " + selected : "");
    }

    private ArrayList<String> getSelections() {
        return new ArrayList<>(selections.values());
    }

    private void clickEvent(View view, int index, String realPath) {
        view.setOnClickListener(v -> {
            if (AdvanceUtils.isDirectory(realPath)) {
                listPath(realPath);
            } else {
                if (selections.isEmpty()) {
                    selections.put(index, realPath);
                    view.setBackgroundColor(Color.parseColor(selectionColor));
                } else if (!selections.containsKey(index) && multiSelection) {
                    selections.put(index, realPath);
                    view.setBackgroundColor(Color.parseColor(selectionColor));
                } else {
                    selections.remove(index);
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
                countSelections();
            }
        });
    }

    private void normalToast(String message, int length) {
        materialToast.normalToast(message, length).show();
    }

    private void successToast(String message, int length) {
        materialToast.successToast(message, length).show();
    }

    private void errorToast(String message, int length) {
        materialToast.errorToast(message, length).show();
    }

    private void infoToast(String message, int length) {
        materialToast.infoToast(message, length).show();
    }

    public interface CallbackListener {
        void onCompleted(ArrayList<String> lists);

        void onError(String message);

        void onCancel(String message);
    }

    public static abstract class SimpleOnCallbackListener implements CallbackListener {
        public void onCompleted(ArrayList<String> lists) {
        }

        public void onError(String message) {
        }

        public void onCancel(String message) {
        }
    }

    public class ListViewAdapter extends BaseAdapter {
        ArrayList<HashMap<String, Object>> data;

        public ListViewAdapter(ArrayList<HashMap<String, Object>> array) {
            data = array;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public HashMap<String, Object> getItem(int index) {
            return data.get(index);
        }

        @Override
        public long getItemId(int index) {
            return index;
        }

        @SuppressLint({"InflateParams", "ViewHolder", "SetTextI18n"})
        @Override
        public View getView(int index, View convertView, ViewGroup parent) {
            LayoutInflater inflater = activity.getLayoutInflater();
            ListViewHolder holder;
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.file_picker_list_view_layout, null);
                holder = new ListViewHolder();
                holder.thumbnail = convertView.findViewById(R.id.files_picker_list_view_thumbnail);
                holder.name = convertView.findViewById(R.id.files_picker_list_view_name);
                holder.size = convertView.findViewById(R.id.files_picker_list_view_size);
                convertView.setTag(holder);
            } else {
                holder = (ListViewHolder) convertView.getTag();
            }
            String realPath = Objects.requireNonNull(lists.get(index).get("realPath")).toString();
            holder.name.setText(AdvanceUtils.getPathLastSegment(realPath));
            holder.size.setText(AdvanceUtils.readableSize(realPath));
            final View currentView = convertView;
            activity.runOnUiThread(() -> {
                if (AdvanceUtils.isDirectory(realPath)) {
                    holder.thumbnail.setImageResource(R.mipmap.files_picker_ic_folder_black);
                } else {
                    if (AdvanceUtils.validImage(realPath)) {
                        holder.thumbnail.setImageResource(R.mipmap.files_picker_ic_image_black);
                    } else if (AdvanceUtils.validVideo(realPath)) {
                        holder.thumbnail.setImageResource(R.mipmap.files_picker_ic_movie_black);
                    } else {
                        holder.thumbnail.setImageResource(R.mipmap.files_picker_ic_extension_black);
                    }
                }
                clickEvent(currentView, index, realPath);
            });
            return convertView;
        }
    }

    private static class ListViewHolder {
        ImageView thumbnail;
        TextView name;
        TextView size;
    }
}