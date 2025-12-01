package com.darwin.cloudfile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.ViewHolder> {
    private List<FtpFileItem> fileList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FtpFileItem item);
        void onItemLongClick(FtpFileItem item);
    }

    public FtpFileAdapter(List<FtpFileItem> fileList, OnItemClickListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ftp_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FtpFileItem item = fileList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public void updateData(List<FtpFileItem> newFileList) {
        fileList = newFileList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;
        private TextView details;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            details = itemView.findViewById(R.id.details);
        }

        public void bind(final FtpFileItem item, final OnItemClickListener listener) {
            name.setText(item.getName());

            if (item.isDirectory()) {
                icon.setImageResource(R.drawable.ic_folder);
                details.setText("Папка");
            } else {
                icon.setImageResource(R.drawable.ic_file);
                details.setText(formatFileSize(item.getSize()) + " | " + item.getTimestamp());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemLongClick(item);
                    return true;
                }
            });
        }

        private String formatFileSize(long size) {
            if (size <= 0) return "0 байт";
            final String[] units = new String[]{"байт", "Кб", "Мб", "Гб", "Тб"};
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }
}