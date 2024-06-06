package com.cookandroid.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.login.Fragment.ImagePredict;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private List<ImagePredict> menuList;

    public MenuAdapter(List<ImagePredict> menuList) {
        this.menuList = menuList;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        ImagePredict imagePredict = menuList.get(position);
        String timeOnly = imagePredict.getDateTime().split(" ")[1]; // 시간만 가져오기
        holder.dateTextView.setText(timeOnly);
        holder.mealTimeTextView.setText(imagePredict.getEatTime());
        holder.menuNameTextView.setText(imagePredict.getImageName());
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView mealTimeTextView;
        TextView menuNameTextView;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            mealTimeTextView = itemView.findViewById(R.id.mealTimeTextView);
            menuNameTextView = itemView.findViewById(R.id.menuNameTextView);
        }
    }
}
