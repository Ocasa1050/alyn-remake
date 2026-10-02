package ro.alynsampmobile.game.ui.widgets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ro.alynsampmobile.launcher.R;

public class DialogMenuAdapter extends RecyclerView.Adapter<DialogMenuAdapter.DialogMenuHolder> {
    private final List<DataDialogMenu> list;
    private final OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void click(DataDialogMenu dataDialogMenu, View view);
    }

    public DialogMenuAdapter(List<DataDialogMenu> list, OnUserClickListener onUserClickListener) {
        this.list = list;
        this.onUserClickListener = onUserClickListener;
    }

    @Override
    public DialogMenuHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_action_item, parent, false);
        return new DialogMenuHolder(view);
    }

    @Override
    public void onBindViewHolder(DialogMenuHolder holder, int position) {
        DataDialogMenu item = list.get(position);
        holder.name.setText(item.getNameButton());
        holder.image.setImageResource(item.getImgDrawableButton());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DialogMenuHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name;

        public DialogMenuHolder(View view) {
            super(view);
            name = view.findViewById(R.id.item_menu_name_button);
            image = view.findViewById(R.id.item_menu_image);
            view.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                onUserClickListener.click(list.get(pos), v);
            });
        }
    }
}